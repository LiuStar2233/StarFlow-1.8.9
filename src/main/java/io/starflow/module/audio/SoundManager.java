package io.starflow.module.audio;

import io.starflow.tools.SoundLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.*;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.compress.utils.Lists;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.lwjgl.openal.*;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.lwjgl.openal.EXTDisconnect.ALC_CONNECTED;

/**
 * @author StarFlow Team -> LiuStar2233
 * @Package io.starflow.module.audio
 * @ClassName SoundManager
 * @description 负责处理游戏中的音频播放、管理和控制。
 * @since 2026-01-27
 */

public class SoundManager {
    private static final Logger logger = LogManager.getLogger("StarFlow");
    private static final Marker LOG_MARKER = MarkerManager.getMarker("SOUND_SYSTEM");

    enum ChannelState {
        FREE,       // 空闲
        INITIALIZING, // 正在异步准备资源
        PLAYING,    // 正在播放
        PAUSED,     // 暂停
        STOPPED     // 已停止但尚未清理
    }

    class SoundChannel {
        int sourceId;
        ISound sound;
        ChannelState state = ChannelState.FREE;

        public void stop() {
            if (0 != sourceId) {
                AL10.alSourceStop(this.sourceId);
                AL10.alSourcei(this.sourceId, AL10.AL_BUFFER, 0);
            }
            this.cleanup();
        }

        public void cleanup() {
            this.sound = null;
            this.state = ChannelState.FREE;
        }
    }

    private static final int MAX_SOURCES = 256;
    private long device;
    private long context;

    private final SoundHandler sndHandler;
    private final GameSettings gameSettings;
    private final SoundChannel[] channels;
    private final List<ITickableSound> tickableSounds;
    private final PriorityQueue<DelayedSound> delayedQueue;
    private final Map<ResourceLocation, Integer> bufferCache = new LinkedHashMap<ResourceLocation, Integer>(128, 0.75F, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<ResourceLocation, Integer> eldest) {
            if (128 < this.size()) {
                int bufferId = eldest.getValue();
                soundExecutor.execute(() -> {
                    if (AL10.alIsBuffer(bufferId)) {
                        AL10.alDeleteBuffers(bufferId);
                    }
                });
                return true;
            }
            return false;
        }
    };

    private volatile boolean loaded;
    private int playTime;

    private final ExecutorService soundExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Sound_Runtime_Thread");
        thread.setDaemon(true);
        return thread;
    });

    public SoundManager(SoundHandler soundHandler, GameSettings gameSettings) {
        this.sndHandler = soundHandler;
        this.gameSettings = gameSettings;
        this.channels = new SoundChannel[MAX_SOURCES];
        for (int i = 0; MAX_SOURCES > i; i++) {
            this.channels[i] = new SoundChannel();
        }

        this.tickableSounds = Lists.newArrayList();
        this.delayedQueue = new PriorityQueue<>();

        logger.info(LOG_MARKER, "已成功使用 LWJGL3 OpenAL 初始化 SoundManager");
    }

    private void loadSoundSystem() {
        if (this.loaded) return;

        soundExecutor.execute(() -> {
            try {
                logger.info(LOG_MARKER, "正在分配 OpenAL 资源...");

                device = ALC10.alcOpenDevice((ByteBuffer) null);
                if (0L == this.device) {
                    logger.error(LOG_MARKER, "打开 OpenAL 设备失败");
                    throw new IllegalStateException("打开 OpenAL 设备失败");
                }

                ALCCapabilities deviceCaps = ALC.createCapabilities(device);
                context = ALC10.alcCreateContext(device, (IntBuffer) null);
                ALC10.alcMakeContextCurrent(context);
                AL.createCapabilities(deviceCaps);

                for (int i = 0; MAX_SOURCES > i; i++) {
                    int sid = AL10.alGenSources();
                    if (AL10.AL_NO_ERROR == AL10.alGetError()) {
                        channels[i].sourceId = sid;
                        channels[i].state = ChannelState.FREE;
                    }
                }
                this.loaded = true;
                logger.info(LOG_MARKER, "OpenAL 后端准备就绪，已成功分配 {} 个声道", MAX_SOURCES);
            } catch (Exception e) {
                logger.error(LOG_MARKER, "OpenAL 初始化期间发生严重错误", e);
            }
        });
    }

    public void reloadSoundSystem() {
        logger.info(LOG_MARKER, "正在重新加载音频系统...");
        this.unloadSoundSystem();
        this.loadSoundSystem();
    }

    private void handleDeviceChange() {
        soundExecutor.execute(() -> {
            if (ALC10.alcIsExtensionPresent(device, "ALC_SOFT_reopen_device")) {
                logger.info(LOG_MARKER, "正在通过 ALC_SOFT_reopen_device 扩展热切换音频输出...");

                boolean success = SOFTReopenDevice.alcReopenDeviceSOFT(device, (ByteBuffer) null, (IntBuffer) null);

                if (success) {
                    logger.info(LOG_MARKER, "音频输出设备已无损切换");
                    return;
                }
            }

            logger.warn(LOG_MARKER, "音频设备热切换方案失败，正在尝试硬重载...");
            this.reloadSoundSystem();
        });
    }

    private void checkDeviceHealth() {
        if (!this.loaded || 0L == this.device) return;
        int connected = ALC10.alcGetInteger(device, ALC_CONNECTED);
        if (ALC10.ALC_FALSE == connected) {
            logger.warn(LOG_MARKER, "检测到音频设备断开，正在尝试恢复...");
            this.handleDeviceChange();
        }
    }

    private SoundChannel findFreeChannel() {
        SoundChannel bestToReplace = null;
        float minWeight = Float.MAX_VALUE;

        for (SoundChannel channel : channels) {
            if (ChannelState.FREE == channel.state) return channel;
            if (ChannelState.INITIALIZING == channel.state) continue;

            // 计算重要性：音量越小、距离越远，权重越低（越容易被替换）采用距离平方避免开根号开销
            float weight = (float) Minecraft.getMinecraft().thePlayer.getDistanceSq(
                    channel.sound.getXPosF(), channel.sound.getYPosF(), channel.sound.getZPosF());

            if (0 < weight && weight < minWeight) {
                minWeight = weight;
                bestToReplace = channel;
            }
        }
        return bestToReplace;
    }

    private int getOrCreateBuffer(ResourceLocation location) {
        if (bufferCache.containsKey(location)) {
            return bufferCache.get(location);
        }
        int bufferId = AL10.alGenBuffers();
        try {
            IResource resource = Minecraft.getMinecraft().getResourceManager().getResource(location);
            SoundLoader.AudioData data = SoundLoader.load(location, resource.getInputStream());
            if (null != data) {
                AL10.alBufferData(bufferId, data.format, data.pcmData, data.sampleRate);
                data.free();
                bufferCache.put(location, bufferId);
                return bufferId;
            }
        } catch (Exception e) {
            logger.error(LOG_MARKER, "音频加载失败 [{}]: {}", location, e.getMessage());
            AL10.alDeleteBuffers(bufferId);
        }
        return 0;
    }

    public float getSoundCategoryVolume(SoundCategory category) {
        float masterVolume = this.gameSettings.getSoundLevel(SoundCategory.MASTER);

        if (null == category || SoundCategory.MASTER == category) {
            return masterVolume;
        } else {
            float categoryVolume = this.gameSettings.getSoundLevel(category);
            return categoryVolume * masterVolume;
        }
    }

    public void setSoundCategoryVolume(SoundCategory category, float volume) {
        if (!this.loaded) return;

        soundExecutor.execute(() -> {
            for (SoundChannel channel : channels) {
                if ((ChannelState.FREE != channel.state) && null != channel.sound) {
                    SoundEventAccessorComposite accessor = this.sndHandler.getSound(channel.sound.getSoundLocation());
                    if (null != accessor && (null == category || accessor.getSoundCategory() == category)) {
                        float newVol = this.getNormalizedVolume(channel.sound, accessor.cloneEntry(), accessor.getSoundCategory());
                        AL10.alSourcef(channel.sourceId, AL10.AL_GAIN, newVol);
                    }
                }
            }
        });
    }

    public void unloadSoundSystem() {
        if (!this.loaded) return;

        soundExecutor.execute(() -> {
            this.loaded = false;

            for (SoundChannel channel : channels) {
                if (0 != channel.sourceId) {
                    AL10.alSourceStop(channel.sourceId);
                    AL10.alDeleteSources(channel.sourceId);
                    channel.sourceId = 0;
                }
            }

            bufferCache.forEach((loc, id) -> {
                if (0 != id) AL10.alDeleteBuffers(id);
            });
            bufferCache.clear();

            if (0L != this.context) {
                ALC10.alcMakeContextCurrent(0L);
                ALC10.alcDestroyContext(context);
                context = 0L;
            }
            if (0L != this.device) {
                ALC10.alcCloseDevice(device);
                device = 0L;
            }
            logger.info(LOG_MARKER, "SoundSystem 已安全卸载，所有 Native 资源已释放");
        });
    }

    public void stopAllSounds() {
        if (!this.loaded) return;
        soundExecutor.execute(() -> {
            for (SoundChannel channel : channels) {
                if (ChannelState.FREE != channel.state) {
                    channel.stop();
                }
            }
            this.delayedQueue.clear();
            this.tickableSounds.clear();
            logger.info(LOG_MARKER, "已停止所有音频播放");
        });
    }

    public void updateAllSounds() {
        if (!this.loaded) return;
        this.playTime++;

        while (!this.delayedQueue.isEmpty() && this.delayedQueue.peek().getPlayTime() <= this.playTime) {
            DelayedSound delayed = this.delayedQueue.poll();
            if (null != delayed) {
                this.playSound(delayed.getSound());
            }
        }

        java.util.Iterator<ITickableSound> iterator = this.tickableSounds.iterator();
        while (iterator.hasNext()) {
            ITickableSound tickable = iterator.next();
            tickable.update();

            if (tickable.isDonePlaying()) {
                this.stopInternal(tickable);
                iterator.remove();
            } else {
                this.updateSoundPosition(tickable);
            }
        }

        soundExecutor.execute(() -> {
            for (SoundChannel channel : channels) {
                if (ChannelState.FREE != channel.state) {
                    if (AL10.AL_STOPPED == AL10.alGetSourcei(channel.sourceId, AL10.AL_SOURCE_STATE)) {
                        channel.cleanup();
                    }
                }
            }
        });

        this.updateListener();
    }

    private void stopInternal(ISound sound) {
        soundExecutor.execute(() -> {
            for (SoundChannel channel : channels) {
                if ((ChannelState.FREE != channel.state) && channel.sound == sound) {
                    channel.stop();
                }
            }
        });
    }

    public void setListener(EntityPlayer player, float partialTicks) {
        if (!this.loaded || null == player) return;

        double x = player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks;
        double y = player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks + (double) player.getEyeHeight();
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks;

        float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
        float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;

        float yawRad = -yaw * 0.017453292F - (float) Math.PI;
        float pitchRad = -pitch * 0.017453292F;

        float f = MathHelper.cos(yawRad);
        float f1 = MathHelper.sin(yawRad);
        float f2 = -MathHelper.cos(pitchRad);
        float f3 = MathHelper.sin(pitchRad);

        final float lookX = f1 * f2;
        final float lookY = f3;
        final float lookZ = -(f * f2);

        soundExecutor.execute(() -> {
            AL10.alListener3f(AL10.AL_POSITION, (float) x, (float) y, (float) z);
            float[] orientation = {lookX, lookY, lookZ, 0.0F, 1.0F, 0.0F};
            AL10.alListenerfv(AL10.AL_ORIENTATION, orientation);
        });
    }

    public void updateListener() {
        Minecraft mc = Minecraft.getMinecraft();
        if (null != mc.thePlayer) {
            this.setListener(mc.thePlayer, mc.timer.renderPartialTicks);
        }
    }

    public boolean isSoundPlaying(ISound sound) {
        if (!this.loaded || null == sound) return false;
        for (SoundChannel channel : channels) {
            if ((ChannelState.FREE != channel.state) && channel.sound == sound) {
                int state = AL10.alGetSourcei(channel.sourceId, AL10.AL_SOURCE_STATE);
                return AL10.AL_PLAYING == state;
            }
        }
        return false;
    }

    public void stopSound(ISound sound) {
        if (!this.loaded || null == sound) return;
        soundExecutor.execute(() -> {
            for (SoundChannel channel : channels) {
                if ((ChannelState.FREE != channel.state) && channel.sound == sound) {
                    channel.stop();
                }
            }
        });
    }

    /**
     * 在 OpenAL 上下文中播放音效。
     * * @param pSound 待播放的音效实例
     *
     * @see #checkALError(String)
     */
    public void playSound(ISound pSound) {
        if (!this.loaded) return;

        SoundEventAccessorComposite accessor = this.sndHandler.getSound(pSound.getSoundLocation());
        if (null == accessor) return;

        SoundPoolEntry entry = accessor.cloneEntry();
        if (null == entry) return;

        float volume = this.getNormalizedVolume(pSound, entry, accessor.getSoundCategory());
        float pitch = this.getNormalizedPitch(pSound, entry);

        SoundChannel channel = findFreeChannel();
        if (null == channel) return;
        channel.state = ChannelState.INITIALIZING;

        new Thread(() -> {
            try {
                IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(entry.getSoundPoolEntryLocation());
                SoundLoader.AudioData audioData = SoundLoader.load(entry.getSoundPoolEntryLocation(), iresource.getInputStream());

                soundExecutor.execute(() -> {
                    if (ChannelState.INITIALIZING != channel.state) return;

                    int bufferId = AL10.alGenBuffers();
                    AL10.alBufferData(bufferId, audioData.format, audioData.pcmData, audioData.sampleRate);
                    audioData.free();
                    applySourceProperties(channel, pSound, bufferId);
                    AL10.alSourcePlay(channel.sourceId);
                    channel.state = ChannelState.PLAYING;
                });
            } catch (Exception e) {
                channel.cleanup();
                logger.error("音频解码异常: " + pSound.getSoundLocation(), e);
            }
        }, "StarFlow-Loader-Thread").start();
    }

    /**
     * [P4 修复] 应用音频源属性
     * 核心职责：将 Minecraft 的 ISound 属性映射到 OpenAL Source 状态机
     * * @param channel 目标声道
     *
     * @param sound 音效实例
     * @param bid   已绑定的 OpenAL Buffer ID
     */
    private void applySourceProperties(SoundChannel channel, ISound sound, int bid) {
        int sid = channel.sourceId;

        AL10.alSourcei(sid, AL10.AL_BUFFER, bid);
        AL10.alSourcef(sid, AL10.AL_GAIN, sound.getVolume());
        AL10.alSourcef(sid, AL10.AL_PITCH, sound.getPitch());

        if (ISound.AttenuationType.NONE == sound.getAttenuationType()) {
            AL10.alSourcei(sid, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSource3f(sid, AL10.AL_POSITION, 0.0F, 0.0F, 0.0F);
        } else {
            AL10.alSourcei(sid, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
            AL10.alSource3f(sid, AL10.AL_POSITION,
                    sound.getXPosF(),
                    sound.getYPosF(),
                    -sound.getZPosF()
            );
        }

        AL10.alSourcei(sid, AL10.AL_LOOPING, sound.canRepeat() ? AL10.AL_TRUE : AL10.AL_FALSE);
        this.checkALError("应用属性: " + sound.getSoundLocation());
    }

    /**
     * 配置 OpenAL 源参数。
     * <p>性能注意：坐标更新逻辑需考虑 {@link ISound.AttenuationType#NONE} 的相对位置锁定。</p>
     */
    private void setupSource(SoundChannel channel, ISound sound, int bufferId, float volume, float pitch) {
        int sid = channel.sourceId;
        AL10.alSourcei(sid, AL10.AL_BUFFER, bufferId);
        AL10.alSourcef(sid, AL10.AL_GAIN, volume);
        AL10.alSourcef(sid, AL10.AL_PITCH, pitch);

        if (ISound.AttenuationType.NONE == sound.getAttenuationType()) {
            AL10.alSourcei(sid, AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);
            AL10.alSource3f(sid, AL10.AL_POSITION, 0F, 0F, 0F);
        } else {
            AL10.alSourcei(sid, AL10.AL_SOURCE_RELATIVE, AL10.AL_FALSE);
            AL10.alSource3f(sid, AL10.AL_POSITION, sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
        }

        AL10.alSourcei(sid, AL10.AL_LOOPING, sound.canRepeat() ? AL10.AL_TRUE : AL10.AL_FALSE);
    }

    /**
     * 获取音效的播放音量。
     * <p>性能注意：音量计算逻辑需考虑 {@link SoundCategory} 的音量设置。</p>
     */
    private float getNormalizedPitch(ISound sound, SoundPoolEntry entry) {
        double totalPitch = (double) sound.getPitch() * entry.getPitch();
        return (float) MathHelper.clamp_double(totalPitch, 0.5D, 2.0D);
    }

    /**
     * 获取音效的播放音量。
     * <p>性能注意：音量计算逻辑需考虑 {@link SoundCategory} 的音量设置。</p>
     */
    private float getNormalizedVolume(ISound sound, SoundPoolEntry entry, SoundCategory category) {
        double rawVolume = (double) sound.getVolume() * entry.getVolume();
        float clampedVolume = (float) MathHelper.clamp_double(rawVolume, 0.0D, 1.0D);
        return clampedVolume * this.getSoundCategoryVolume(category);
    }

    public void pauseAllSounds() {
        if (!this.loaded) return;
        soundExecutor.execute(() -> {
            for (SoundChannel channel : channels) {
                if (ChannelState.FREE != channel.state) {
                    int state = AL10.alGetSourcei(channel.sourceId, AL10.AL_SOURCE_STATE);
                    if (AL10.AL_PLAYING == state) {
                        AL10.alSourcePause(channel.sourceId);
                    }
                }
            }
        });
    }

    public void resumeAllSounds() {
        if (!this.loaded) return;
        soundExecutor.execute(() -> {
            for (SoundChannel channel : channels) {
                if (ChannelState.FREE != channel.state) {
                    int state = AL10.alGetSourcei(channel.sourceId, AL10.AL_SOURCE_STATE);
                    if (AL10.AL_PAUSED == state) {
                        AL10.alSourcePlay(channel.sourceId);
                    }
                }
            }
        });
    }

    private void updateSoundPosition(ISound sound) {
        if (!this.loaded) return;
        for (SoundChannel channel : channels) {
            if ((ChannelState.FREE != channel.state) && channel.sound == sound) {
                AL10.alSource3f(channel.sourceId, AL10.AL_POSITION,
                        sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
            }
        }
    }

    public void playDelayedSound(ISound sound, int delay) {
        this.delayedQueue.add(new DelayedSound(sound, this.playTime + delay));
    }

    private static URL getURLForSoundResource(final ResourceLocation location) {
        String s = String.format("mcsounddomain:%s:%s", location.getResourceDomain(), location.getResourcePath());

        try {
            return new URL(null, s, new java.net.URLStreamHandler() {
                @Override
                protected java.net.URLConnection openConnection(URL u) {
                    return null;
                }
            });
        } catch (java.net.MalformedURLException e) {
            return null;
        }
    }

    private void checkALError(String operation) {
        int error = AL10.alGetError();
        if (AL10.AL_NO_ERROR != error) {
            String errorName;
            switch (error) {
                case AL10.AL_INVALID_NAME:
                    errorName = "AL_INVALID_NAME";
                    break;
                case AL10.AL_INVALID_ENUM:
                    errorName = "AL_INVALID_ENUM";
                    break;
                case AL10.AL_INVALID_VALUE:
                    errorName = "AL_INVALID_VALUE";
                    break;
                case AL10.AL_INVALID_OPERATION:
                    errorName = "AL_INVALID_OPERATION";
                    break;
                case AL10.AL_OUT_OF_MEMORY:
                    errorName = "AL_OUT_OF_MEMORY";
                    break;
                default:
                    errorName = "Unknown Error";
                    break;
            }
            logger.error(LOG_MARKER, "OpenAL 错误于 [{}]: {} (代码: {})", operation, errorName, error);
        }
    }

    class SoundSystemStarterThread {
        public boolean playing(String name) {
            return false;
        }
    }
}