package io.starflow.modules.audio;

import net.minecraft.client.audio.ISound;

/**
 * @author LiuStar2233
 * @Package io.starflow.modules.audio
 * @ClassName DelayedSound
 * @description 延迟播放的音频类，用于在指定的游戏 tick 后播放音频。
 * @since 2026-01-29
 */

public class DelayedSound implements Comparable<DelayedSound> {
    private final ISound sound;
    private final int playTime;

    public DelayedSound(ISound sound, int playTime) {
        this.sound = sound;
        this.playTime = playTime;
    }

    public ISound getSound() {
        return sound;
    }

    public int getPlayTime() {
        return playTime;
    }

    @Override
    public int compareTo(DelayedSound o) {
        return Integer.compare(this.playTime, o.playTime);
    }
}