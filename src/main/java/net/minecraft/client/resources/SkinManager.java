package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SkinManager {
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue());
    private final TextureManager textureManager;
    private final File skinCacheDir;
    private final MinecraftSessionService sessionService;
    private final LoadingCache<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> skinCacheLoader;

    public SkinManager(final TextureManager textureManagerInstance, final File skinCacheDirectory, final MinecraftSessionService sessionService) {
        textureManager = textureManagerInstance;
        skinCacheDir = skinCacheDirectory;
        this.sessionService = sessionService;
        skinCacheLoader = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<GameProfile, Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>>() {
            public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> load(final GameProfile p_load_1_) throws Exception {
                return Minecraft.getMinecraft().getSessionService().getTextures(p_load_1_, false);
            }
        });
    }

    /**
     * Used in the Skull renderer to fetch a skin. May download the skin if it's not in the cache
     */
    public ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final MinecraftProfileTexture.Type p_152792_2_) {
        return loadSkin(profileTexture, p_152792_2_, null);
    }

    /**
     * May download the skin if its not in the cache, can be passed a SkinManager#SkinAvailableCallback for handling
     */
    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type p_152789_2_, SkinManager.SkinAvailableCallback skinAvailableCallback) {
        ResourceLocation resourcelocation = new ResourceLocation("skins/" + profileTexture.getHash());
        final ITextureObject itextureobject = textureManager.getTexture(resourcelocation);

        if (null != itextureobject) {
            if (null != skinAvailableCallback) {
                skinAvailableCallback.skinAvailable(p_152789_2_, resourcelocation, profileTexture);
            }
        } else {
            final File file1 = new File(skinCacheDir, 2 < profileTexture.getHash().length() ? profileTexture.getHash().substring(0, 2) : "xx");
            final File file2 = new File(file1, profileTexture.getHash());
            IImageBuffer iimagebuffer = MinecraftProfileTexture.Type.SKIN == p_152789_2_ ? new ImageBufferDownload() : null;
            final ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(file2, profileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkinLegacy(), new IImageBuffer() {
                public BufferedImage parseUserSkin(BufferedImage image) {
                    if (null != iimagebuffer) {
                        image = iimagebuffer.parseUserSkin(image);
                    }

                    return image;
                }

                public void skinAvailable() {
                    if (null != iimagebuffer) {
                        iimagebuffer.skinAvailable();
                    }

                    if (null != skinAvailableCallback) {
                        skinAvailableCallback.skinAvailable(p_152789_2_, resourcelocation, profileTexture);
                    }
                }
            });
            textureManager.loadTexture(resourcelocation, threaddownloadimagedata);
        }

        return resourcelocation;
    }

    public void loadProfileTextures(GameProfile profile, SkinManager.SkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
        SkinManager.THREAD_POOL.submit(new Runnable() {
            public void run() {
                Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = Maps.newHashMap();

                try {
                    map.putAll(sessionService.getTextures(profile, requireSecure));
                } catch (final InsecureTextureException var3) {
                }

                if (map.isEmpty() && profile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId())) {
                    profile.getProperties().clear();
                    profile.getProperties().putAll(Minecraft.getMinecraft().getProfileProperties());
                    map.putAll(sessionService.getTextures(profile, false));
                }

                Minecraft.getMinecraft().addScheduledTask(new Runnable() {
                    public void run() {
                        if (map.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                            loadSkin(map.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN, skinAvailableCallback);
                        }

                        if (map.containsKey(MinecraftProfileTexture.Type.CAPE)) {
                            loadSkin(map.get(MinecraftProfileTexture.Type.CAPE), MinecraftProfileTexture.Type.CAPE, skinAvailableCallback);
                        }
                    }
                });
            }
        });
    }

    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(final GameProfile profile) {
        return skinCacheLoader.getUnchecked(profile);
    }

    public interface SkinAvailableCallback {
        void skinAvailable(MinecraftProfileTexture.Type p_180521_1_, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }
}