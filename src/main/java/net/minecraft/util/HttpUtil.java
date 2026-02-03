package net.minecraft.util;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpUtil {
    public static final ListeningExecutorService field_180193_a = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool((new ThreadFactoryBuilder()).setDaemon(true).setNameFormat("Downloader %d").build()));

    /**
     * The number of download threads that we have started so far.
     */
    private static final AtomicInteger downloadThreadsStarted = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();

    /**
     * Builds an encoded HTTP POST content string from a string map
     */
    public static String buildPostString(Map<String, Object> data) {
        StringBuilder stringbuilder = new StringBuilder();

        for (final Map.Entry<String, Object> entry : data.entrySet()) {
            if (0 < stringbuilder.length()) {
                stringbuilder.append('&');
            }

            try {
                stringbuilder.append(URLEncoder.encode((String) entry.getKey(), "UTF-8"));
            } catch (final UnsupportedEncodingException unsupportedencodingexception1) {
                unsupportedencodingexception1.printStackTrace();
            }

            if (null != entry.getValue()) {
                stringbuilder.append('=');

                try {
                    stringbuilder.append(URLEncoder.encode(entry.getValue().toString(), "UTF-8"));
                } catch (final UnsupportedEncodingException unsupportedencodingexception) {
                    unsupportedencodingexception.printStackTrace();
                }
            }
        }

        return stringbuilder.toString();
    }

    /**
     * Sends a POST to the given URL using the map as the POST args
     */
    public static String postMap(final URL url, final Map<String, Object> data, final boolean skipLoggingErrors) {
        return HttpUtil.post(url, HttpUtil.buildPostString(data), skipLoggingErrors);
    }

    /**
     * Sends a POST to the given URL
     */
    private static String post(final URL url, final String content, final boolean skipLoggingErrors) {
        try {
            Proxy proxy = null == MinecraftServer.getServer() ? null : MinecraftServer.getServer().getServerProxy();

            if (null == proxy) {
                proxy = Proxy.NO_PROXY;
            }

            final HttpURLConnection httpurlconnection = (HttpURLConnection) url.openConnection(proxy);
            httpurlconnection.setRequestMethod("POST");
            httpurlconnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpurlconnection.setRequestProperty("Content-Length", "" + content.getBytes().length);
            httpurlconnection.setRequestProperty("Content-Language", "en-US");
            httpurlconnection.setUseCaches(false);
            httpurlconnection.setDoInput(true);
            httpurlconnection.setDoOutput(true);
            final DataOutputStream dataoutputstream = new DataOutputStream(httpurlconnection.getOutputStream());
            dataoutputstream.writeBytes(content);
            dataoutputstream.flush();
            dataoutputstream.close();
            final BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
            final StringBuffer stringbuffer = new StringBuffer();
            String s;

            while (null != (s = bufferedreader.readLine())) {
                stringbuffer.append(s);
                stringbuffer.append('\r');
            }

            bufferedreader.close();
            return stringbuffer.toString();
        } catch (final Exception exception) {
            if (!skipLoggingErrors) {
                HttpUtil.logger.error("Could not post to " + url, exception);
            }

            return "";
        }
    }

    public static ListenableFuture<Object> downloadResourcePack(File saveFile, String packUrl, Map<String, String> p_180192_2_, int maxSize, IProgressUpdate p_180192_4_, Proxy p_180192_5_) {
        final ListenableFuture<?> listenablefuture = HttpUtil.field_180193_a.submit(new Runnable() {
            public void run() {
                HttpURLConnection httpurlconnection = null;
                InputStream inputstream = null;
                OutputStream outputstream = null;

                if (null != p_180192_4_) {
                    p_180192_4_.resetProgressAndMessage("Downloading Resource Pack");
                    p_180192_4_.displayLoadingString("Making Request...");
                }

                try {
                    try {
                        final byte[] abyte = new byte[4096];
                        final URL url = new URL(packUrl);
                        httpurlconnection = (HttpURLConnection) url.openConnection(p_180192_5_);
                        float f = 0.0F;
                        float f1 = (float) p_180192_2_.size();

                        for (Map.Entry<String, String> entry : p_180192_2_.entrySet()) {
                            httpurlconnection.setRequestProperty(entry.getKey(), entry.getValue());

                            if (null != p_180192_4_) {
                                p_180192_4_.setLoadingProgress((int) (++f / f1 * 100.0F));
                            }
                        }

                        inputstream = httpurlconnection.getInputStream();
                        f1 = (float) httpurlconnection.getContentLength();
                        int i = httpurlconnection.getContentLength();

                        if (null != p_180192_4_) {
                            p_180192_4_.displayLoadingString(String.format("Downloading file (%.2f MB)...", Float.valueOf(f1 / 1000.0F / 1000.0F)));
                        }

                        if (saveFile.exists()) {
                            long j = saveFile.length();

                            if (j == (long) i) {
                                if (null != p_180192_4_) {
                                    p_180192_4_.setDoneWorking();
                                }

                                return;
                            }

                            HttpUtil.logger.warn("Deleting " + saveFile + " as it does not match what we currently have (" + i + " vs our " + j + ").");
                            FileUtils.deleteQuietly(saveFile);
                        } else if (null != saveFile.getParentFile()) {
                            saveFile.getParentFile().mkdirs();
                        }

                        outputstream = new DataOutputStream(new FileOutputStream(saveFile));

                        if (0 < maxSize && f1 > (float) maxSize) {
                            if (null != p_180192_4_) {
                                p_180192_4_.setDoneWorking();
                            }

                            throw new IOException("Filesize is bigger than maximum allowed (file is " + f + ", limit is " + maxSize + ")");
                        }

                        int k = 0;

                        while (0 <= (k = inputstream.read(abyte))) {
                            f += (float) k;

                            if (null != p_180192_4_) {
                                p_180192_4_.setLoadingProgress((int) (f / f1 * 100.0F));
                            }

                            if (0 < maxSize && f > (float) maxSize) {
                                if (null != p_180192_4_) {
                                    p_180192_4_.setDoneWorking();
                                }

                                throw new IOException("Filesize was bigger than maximum allowed (got >= " + f + ", limit was " + maxSize + ")");
                            }

                            if (Thread.interrupted()) {
                                HttpUtil.logger.error("INTERRUPTED");

                                if (null != p_180192_4_) {
                                    p_180192_4_.setDoneWorking();
                                }

                                return;
                            }

                            outputstream.write(abyte, 0, k);
                        }

                        if (null != p_180192_4_) {
                            p_180192_4_.setDoneWorking();
                        }
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();

                        if (null != httpurlconnection) {
                            InputStream inputstream1 = httpurlconnection.getErrorStream();

                            try {
                                HttpUtil.logger.error(IOUtils.toString(inputstream1));
                            } catch (IOException ioexception) {
                                ioexception.printStackTrace();
                            }
                        }

                        if (null != p_180192_4_) {
                            p_180192_4_.setDoneWorking();
                        }
                    }
                } finally {
                    IOUtils.closeQuietly(inputstream);
                    IOUtils.closeQuietly(outputstream);
                }
            }
        });
        return (ListenableFuture<Object>) listenablefuture;
    }

    public static int getSuitableLanPort() throws IOException {
        ServerSocket serversocket = null;
        int i = -1;

        try {
            serversocket = new ServerSocket(0);
            i = serversocket.getLocalPort();
        } finally {
            try {
                if (null != serversocket) {
                    serversocket.close();
                }
            } catch (IOException var8) {
            }
        }

        return i;
    }

    /**
     * Send a GET request to the given URL.
     */
    public static String get(URL url) throws IOException {
        HttpURLConnection httpurlconnection = (HttpURLConnection) url.openConnection();
        httpurlconnection.setRequestMethod("GET");
        BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(httpurlconnection.getInputStream()));
        StringBuilder stringbuilder = new StringBuilder();
        String s;

        while (null != (s = bufferedreader.readLine())) {
            stringbuilder.append(s);
            stringbuilder.append('\r');
        }

        bufferedreader.close();
        return stringbuilder.toString();
    }
}