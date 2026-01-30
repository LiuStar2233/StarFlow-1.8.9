package io.starflow.tools;

import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;

/**
 * @author LiuStar2233
 * @Package io.starflow.tools
 * @ClassName SoundLoader
 * @description 音频加载器
 * @since 2026-01-30
 */

public class SoundLoader {
    private static final Logger logger = LogManager.getLogger("SoundLoader");

    public static class AudioData {
        public final ByteBuffer pcmData;
        public final int format;
        public final int sampleRate;

        public AudioData(ByteBuffer pcmData, int format, int sampleRate) {
            this.pcmData = pcmData;
            this.format = format;
            this.sampleRate = sampleRate;
        }

        public void free() {
            if (pcmData != null) {
                MemoryUtil.memFree(pcmData);
            }
        }
    }

    public static AudioData load(ResourceLocation location, InputStream is) throws Exception {
        String path = location.getResourcePath().toLowerCase();
        byte[] bytes = IOUtils.toByteArray(is);

        ByteBuffer fileBuffer = MemoryUtil.memAlloc(bytes.length);
        fileBuffer.put(bytes).flip();

        try {
            if (fileBuffer.remaining() >= 4) {
                int magic = fileBuffer.getInt(0);
                if (magic == 0x4F676753) {
                    return decodeOgg(fileBuffer);
                } else if (magic == 0x52494646) {
                    return decodeWav(fileBuffer);
                }
            }

            if (path.endsWith(".ogg") || path.endsWith(".oga")) {
                return decodeOgg(fileBuffer);
            } else if (path.endsWith(".wav")) {
                return decodeWav(fileBuffer);
            }

            throw new UnsupportedOperationException("不支持的格式: " + path);
        } finally {
            MemoryUtil.memFree(fileBuffer);
        }
    }

    private static AudioData decodeOgg(ByteBuffer encodedBuffer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer channels = stack.mallocInt(1);
            IntBuffer sampleRate = stack.mallocInt(1);

            ShortBuffer rawAudio = STBVorbis.stb_vorbis_decode_memory(encodedBuffer, channels, sampleRate);
            if (rawAudio == null) {
                return null;
            }

            int channelCount = channels.get(0);

            if (channelCount > 2) {
                logger.warn("不支持多声道音频 ({} channels)，目前仅支持单声道或双声道。", channelCount);
                return null;
            }

            int format = (channelCount == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

            int byteSize = rawAudio.remaining() * 2;
            ByteBuffer pcmBuffer = MemoryUtil.memAlloc(byteSize);
            pcmBuffer.asShortBuffer().put(rawAudio);
            pcmBuffer.flip();

            return new AudioData(pcmBuffer, format, sampleRate.get(0));
        }
    }

    private static AudioData decodeWav(ByteBuffer buffer) {
        if (Integer.reverseBytes(buffer.getInt()) != 0x52494646) return null;
        buffer.getInt();
        if (Integer.reverseBytes(buffer.getInt()) != 0x57415645) return null;

        int format = -1;
        int sampleRate = -1;
        ByteBuffer pcmData = null;

        while (buffer.hasRemaining()) {
            if (buffer.remaining() < 8) break;

            int chunkId = Integer.reverseBytes(buffer.getInt());
            int chunkSize = Integer.reverseBytes(buffer.getInt());

            int nextPosition = buffer.position() + chunkSize;
            if (nextPosition > buffer.limit() || nextPosition < 0) {
                logger.error("WAV 数据损坏或恶意 Chunk: pos={} + size={} > limit={}",
                        buffer.position(), chunkSize, buffer.limit());
                if (pcmData != null) MemoryUtil.memFree(pcmData);
                return null;
            }

            if (chunkId == 0x666d7420) {
                short audioFormat = Short.reverseBytes(buffer.getShort());
                short numChannels = Short.reverseBytes(buffer.getShort());
                sampleRate = Integer.reverseBytes(buffer.getInt());
                buffer.getInt();
                buffer.getShort();
                short bitsPerSample = Short.reverseBytes(buffer.getShort());

                if (audioFormat == 1) {
                    if (numChannels == 1) {
                        format = (bitsPerSample == 8) ? AL_FORMAT_MONO8 : AL_FORMAT_MONO16;
                    } else if (numChannels == 2) {
                        format = (bitsPerSample == 8) ? AL_FORMAT_STEREO8 : AL_FORMAT_STEREO16;
                    }
                }

                buffer.position(nextPosition);
            } else if (chunkId == 0x64617461) {
                pcmData = MemoryUtil.memAlloc(chunkSize);
                int oldLimit = buffer.limit();
                buffer.limit(nextPosition);
                pcmData.put(buffer);
                buffer.limit(oldLimit);
                pcmData.flip();
                break;
            } else {
                buffer.position(nextPosition);
            }
        }

        if (pcmData == null || format == -1) {
            if (pcmData != null) MemoryUtil.memFree(pcmData);
            return null;
        }

        return new AudioData(pcmData, format, sampleRate);
    }
}