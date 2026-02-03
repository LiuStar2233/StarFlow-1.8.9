package net.minecraft.client.settings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;

import java.io.*;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GameSettings {
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType() {
        public Type[] getActualTypeArguments() {
            return new Type[]{String.class};
        }

        public Type getRawType() {
            return List.class;
        }

        public Type getOwnerType() {
            return null;
        }
    };

    /**
     * GUI scale values
     */
    private static final String[] GUISCALES = {"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = {"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = {"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] CLOUDS_TYPES = {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    public boolean fboEnable = true;
    public int limitFramerate = 120;

    /**
     * Clouds flag
     */
    public int clouds = 2;
    public boolean fancyGraphics = true;

    /**
     * Smooth Lighting
     */
    public int ambientOcclusion = 2;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> incompatibleResourcePacks = Lists.newArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    public boolean chatColours = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public float chatOpacity = 1.0F;
    public boolean snooperEnabled = true;
    public boolean fullScreen;
    public boolean enableVsync = true;
    public boolean useVbo;
    public boolean allowBlockAlternatives = true;
    public boolean reducedDebugInfo;
    public boolean hideServerAddress;

    /**
     * Whether to show advanced information on item tooltips, toggled by F3+H
     */
    public boolean advancedItemTooltips;

    /**
     * Whether to pause when the game loses focus, toggled by F3+P
     */
    public boolean pauseOnLostFocus = true;
    private final Set<EnumPlayerModelParts> setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
    public boolean touchscreen;
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatHeightFocused = 1.0F;
    public boolean showInventoryAchievementHint = true;
    public int mipmapLevels = 4;
    private final Map<SoundCategory, Float> mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
    public boolean useNativeTransport = true;
    public boolean entityShadows = true;
    public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
    public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
    public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
    public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
    public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
    public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
    public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
    public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
    public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
    public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
    public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
    public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
    public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
    public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
    public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
    public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
    public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
    public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
    public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
    public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
    public KeyBinding[] keyBindsHotbar = {new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
    public KeyBinding[] keyBindings;
    protected Minecraft mc;
    private File optionsFile;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;

    /**
     * true if debug info should be displayed instead of version
     */
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean showLagometer;

    /**
     * The lastServer string.
     */
    public String lastServer;

    /**
     * Smooth Camera Toggle
     */
    public boolean smoothCamera;
    public boolean debugCamEnable;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;

    /**
     * GUI scale
     */
    public int guiScale;

    /**
     * Determines amount of particles. 0 = All, 1 = Decreased, 2 = Minimal
     */
    public int particleSetting;

    /**
     * Game settings language
     */
    public String language;
    public boolean forceUnicodeFont;

    public GameSettings(Minecraft mcIn, File optionsFileIn) {
        this.keyBindings = ArrayUtils.addAll(new KeyBinding[]{this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen, this.keyBindSpectatorOutlines}, this.keyBindsHotbar);
        this.difficulty = EnumDifficulty.NORMAL;
        this.lastServer = "";
        this.fovSetting = 70.0F;
        this.language = "en_US";
        this.forceUnicodeFont = false;
        this.mc = mcIn;
        this.optionsFile = new File(optionsFileIn, "options.txt");

        if (mcIn.isJava64bit() && 1000000000L <= Runtime.getRuntime().maxMemory()) {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
        } else {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
        }

        this.renderDistanceChunks = mcIn.isJava64bit() ? 12 : 8;
        this.loadOptions();
    }

    public GameSettings() {
        this.keyBindings = ArrayUtils.addAll(new KeyBinding[]{this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen, this.keyBindSpectatorOutlines}, this.keyBindsHotbar);
        this.difficulty = EnumDifficulty.NORMAL;
        this.lastServer = "";
        this.fovSetting = 70.0F;
        this.language = "en_US";
        this.forceUnicodeFont = false;
    }

    /**
     * Represents a key or mouse button as a string. Args: key
     *
     * @param key The key to display
     */
    public static String getKeyDisplayString(int key) {
        return 0 > key ? I18n.format("key.mouseButton", Integer.valueOf(key + 101)) : (256 > key ? Keyboard.getKeyName(key) : String.format("%c", Character.valueOf((char) (key - 256))).toUpperCase());
    }

    /**
     * Returns whether the specified key binding is currently being pressed.
     *
     * @param key The key tested
     */
    public static boolean isKeyDown(KeyBinding key) {
        return 0 != key.getKeyCode() && (0 > key.getKeyCode() ? Mouse.isButtonDown(key.getKeyCode() + 100) : Keyboard.isKeyDown(key.getKeyCode()));
    }

    /**
     * Sets a key binding and then saves all settings.
     *
     * @param key     The key that the option will be set
     * @param keyCode The option (keycode) to set.
     */
    public void setOptionKeyBinding(KeyBinding key, int keyCode) {
        key.setKeyCode(keyCode);
        this.saveOptions();
    }

    /**
     * If the specified option is controlled by a slider (float value), this will set the float value.
     *
     * @param settingsOption The option to set to a value
     * @param value          The value that the option will take
     */
    public void setOptionFloatValue(GameSettings.Options settingsOption, float value) {
        if (Options.SENSITIVITY == settingsOption) {
            this.mouseSensitivity = value;
        }

        if (Options.FOV == settingsOption) {
            this.fovSetting = value;
        }

        if (Options.GAMMA == settingsOption) {
            this.gammaSetting = value;
        }

        if (Options.FRAMERATE_LIMIT == settingsOption) {
            this.limitFramerate = (int) value;
        }

        if (Options.CHAT_OPACITY == settingsOption) {
            this.chatOpacity = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (Options.CHAT_HEIGHT_FOCUSED == settingsOption) {
            this.chatHeightFocused = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (Options.CHAT_HEIGHT_UNFOCUSED == settingsOption) {
            this.chatHeightUnfocused = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (Options.CHAT_WIDTH == settingsOption) {
            this.chatWidth = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (Options.CHAT_SCALE == settingsOption) {
            this.chatScale = value;
            this.mc.ingameGUI.getChatGUI().refreshChat();
        }

        if (Options.MIPMAP_LEVELS == settingsOption) {
            int i = this.mipmapLevels;
            this.mipmapLevels = (int) value;

            if ((float) i != value) {
                this.mc.getTextureMapBlocks().setMipmapLevels(this.mipmapLevels);
                this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                this.mc.getTextureMapBlocks().setBlurMipmapDirect(false, 0 < mipmapLevels);
                this.mc.scheduleResourcesRefresh();
            }
        }

        if (Options.BLOCK_ALTERNATIVES == settingsOption) {
            this.allowBlockAlternatives = !this.allowBlockAlternatives;
            this.mc.renderGlobal.loadRenderers();
        }

        if (Options.RENDER_DISTANCE == settingsOption) {
            this.renderDistanceChunks = (int) value;
            this.mc.renderGlobal.setDisplayListEntitiesDirty();
        }
    }

    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     *
     * @param settingsOption The option to set to a value
     * @param value          The value that the option will take
     */
    public void setOptionValue(GameSettings.Options settingsOption, int value) {
        if (Options.INVERT_MOUSE == settingsOption) {
            this.invertMouse = !this.invertMouse;
        }

        if (Options.GUI_SCALE == settingsOption) {
            this.guiScale = this.guiScale + value & 3;
        }

        if (Options.PARTICLES == settingsOption) {
            this.particleSetting = (this.particleSetting + value) % 3;
        }

        if (Options.VIEW_BOBBING == settingsOption) {
            this.viewBobbing = !this.viewBobbing;
        }

        if (Options.RENDER_CLOUDS == settingsOption) {
            this.clouds = (this.clouds + value) % 3;
        }

        if (Options.FORCE_UNICODE_FONT == settingsOption) {
            this.forceUnicodeFont = !this.forceUnicodeFont;
            this.mc.fontRendererObj.setUnicodeFlag(this.mc.getLanguageManager().isCurrentLocaleUnicode() || this.forceUnicodeFont);
        }

        if (Options.FBO_ENABLE == settingsOption) {
            this.fboEnable = !this.fboEnable;
        }

        if (Options.ANAGLYPH == settingsOption) {
            this.anaglyph = !this.anaglyph;
            this.mc.refreshResources();
        }

        if (Options.GRAPHICS == settingsOption) {
            this.fancyGraphics = !this.fancyGraphics;
            this.mc.renderGlobal.loadRenderers();
        }

        if (Options.AMBIENT_OCCLUSION == settingsOption) {
            this.ambientOcclusion = (this.ambientOcclusion + value) % 3;
            this.mc.renderGlobal.loadRenderers();
        }

        if (Options.CHAT_VISIBILITY == settingsOption) {
            this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((this.chatVisibility.getChatVisibility() + value) % 3);
        }

        if (Options.CHAT_COLOR == settingsOption) {
            this.chatColours = !this.chatColours;
        }

        if (Options.CHAT_LINKS == settingsOption) {
            this.chatLinks = !this.chatLinks;
        }

        if (Options.CHAT_LINKS_PROMPT == settingsOption) {
            this.chatLinksPrompt = !this.chatLinksPrompt;
        }

        if (Options.SNOOPER_ENABLED == settingsOption) {
            this.snooperEnabled = !this.snooperEnabled;
        }

        if (Options.TOUCHSCREEN == settingsOption) {
            this.touchscreen = !this.touchscreen;
        }

        if (Options.USE_FULLSCREEN == settingsOption) {
            this.fullScreen = !this.fullScreen;

            if (this.mc.isFullScreen() != this.fullScreen) {
                this.mc.toggleFullscreen();
            }
        }

        if (Options.ENABLE_VSYNC == settingsOption) {
            this.enableVsync = !this.enableVsync;
            Display.setVSyncEnabled(this.enableVsync);
        }

        if (Options.USE_VBO == settingsOption) {
            this.useVbo = !this.useVbo;
            this.mc.renderGlobal.loadRenderers();
        }

        if (Options.BLOCK_ALTERNATIVES == settingsOption) {
            this.allowBlockAlternatives = !this.allowBlockAlternatives;
            this.mc.renderGlobal.loadRenderers();
        }

        if (Options.REDUCED_DEBUG_INFO == settingsOption) {
            this.reducedDebugInfo = !this.reducedDebugInfo;
        }

        if (Options.ENTITY_SHADOWS == settingsOption) {
            this.entityShadows = !this.entityShadows;
        }

        this.saveOptions();
    }

    public float getOptionFloatValue(GameSettings.Options settingOption) {
        if (settingOption == Options.FOV) {
            return this.fovSetting;
        }
        if (settingOption == Options.GAMMA) {
            return this.gammaSetting;
        }
        if (settingOption == Options.SATURATION) {
            return this.saturation;
        }
        if (settingOption == Options.SENSITIVITY) {
            return this.mouseSensitivity;
        }
        if (settingOption == Options.CHAT_OPACITY) {
            return this.chatOpacity;
        }
        if (settingOption == Options.CHAT_HEIGHT_FOCUSED) {
            return this.chatHeightFocused;
        }
        if (settingOption == Options.CHAT_HEIGHT_UNFOCUSED) {
            return this.chatHeightUnfocused;
        }
        if (settingOption == Options.CHAT_SCALE) {
            return this.chatScale;
        }
        if (settingOption == Options.CHAT_WIDTH) {
            return this.chatWidth;
        }
        if (settingOption == Options.FRAMERATE_LIMIT) {
            return (float) this.limitFramerate;
        }
        if (settingOption == Options.MIPMAP_LEVELS) {
            return (float) this.mipmapLevels;
        }
        if (settingOption == Options.RENDER_DISTANCE) {
            return (float) this.renderDistanceChunks;
        }

        return 0.0F;
    }

    public boolean getOptionOrdinalValue(GameSettings.Options settingOption) {
        switch (settingOption) {
            case INVERT_MOUSE:
                return this.invertMouse;

            case VIEW_BOBBING:
                return this.viewBobbing;

            case ANAGLYPH:
                return this.anaglyph;

            case FBO_ENABLE:
                return this.fboEnable;

            case CHAT_COLOR:
                return this.chatColours;

            case CHAT_LINKS:
                return this.chatLinks;

            case CHAT_LINKS_PROMPT:
                return this.chatLinksPrompt;

            case SNOOPER_ENABLED:
                return this.snooperEnabled;

            case USE_FULLSCREEN:
                return this.fullScreen;

            case ENABLE_VSYNC:
                return this.enableVsync;

            case USE_VBO:
                return this.useVbo;

            case TOUCHSCREEN:
                return this.touchscreen;

            case FORCE_UNICODE_FONT:
                return this.forceUnicodeFont;

            case BLOCK_ALTERNATIVES:
                return this.allowBlockAlternatives;

            case REDUCED_DEBUG_INFO:
                return this.reducedDebugInfo;

            case ENTITY_SHADOWS:
                return this.entityShadows;

            default:
                return false;
        }
    }

    /**
     * Returns the translation of the given index in the given String array. If the index is smaller than 0 or greater
     * than/equal to the length of the String array, it is changed to 0.
     *
     * @param strArray The array of string containing the string to translate
     * @param index    The index in the array of the string to translate
     */
    private static String getTranslation(String[] strArray, int index) {
        if (0 > index || index >= strArray.length) {
            index = 0;
        }

        return I18n.format(strArray[index]);
    }

    /**
     * Gets a key binding.
     *
     * @param settingOption The KeyBinding is generated from this option
     */
    public String getKeyBinding(GameSettings.Options settingOption) {
        String s = I18n.format(settingOption.getEnumString()) + ": ";

        if (settingOption.getEnumFloat()) {
            float f1 = this.getOptionFloatValue(settingOption);
            float f = settingOption.normalizeValue(f1);

            if (settingOption == Options.SENSITIVITY) {
                if (f == 0.0F) return s + I18n.format("options.sensitivity.min");
                if (f == 1.0F) return s + I18n.format("options.sensitivity.max");
                return s + (int) (f * 200.0F) + "%";
            }

            if (settingOption == Options.FOV) {
                if (f1 == 70.0F) return s + I18n.format("options.fov.min");
                if (f1 == 110.0F) return s + I18n.format("options.fov.max");
                return s + (int) f1;
            }

            if (settingOption == Options.FRAMERATE_LIMIT) {
                if (f1 == settingOption.valueMax) return s + I18n.format("options.framerateLimit.max");
                return s + (int) f1 + " fps";
            }

            if (settingOption == Options.RENDER_CLOUDS) {
                if (f1 == settingOption.valueMin) return s + I18n.format("options.cloudHeight.min");
                return s + ((int) f1 + 128);
            }

            if (settingOption == Options.GAMMA) {
                if (f == 0.0F) return s + I18n.format("options.gamma.min");
                if (f == 1.0F) return s + I18n.format("options.gamma.max");
                return s + "+" + (int) (f * 100.0F) + "%";
            }

            if (settingOption == Options.SATURATION) {
                return s + (int) (f * 400.0F) + "%";
            }

            if (settingOption == Options.CHAT_OPACITY) {
                return s + (int) (f * 90.0F + 10.0F) + "%";
            }

            if (settingOption == Options.CHAT_HEIGHT_UNFOCUSED || settingOption == Options.CHAT_HEIGHT_FOCUSED) {
                return s + GuiNewChat.calculateChatboxHeight(f) + "px";
            }

            if (settingOption == Options.CHAT_WIDTH) {
                return s + GuiNewChat.calculateChatboxWidth(f) + "px";
            }

            if (settingOption == Options.RENDER_DISTANCE) {
                return s + (int) f1 + " chunks";
            }

            if (settingOption == Options.MIPMAP_LEVELS) {
                if (f1 == 0.0F) return s + I18n.format("options.off");
                return s + (int) f1;
            }

            return f == 0.0F ? s + I18n.format("options.off") : s + (int) (f * 100.0F) + "%";
        }

        // 处理开关类型的选项 (Boolean)
        if (settingOption.getEnumBoolean()) {
            boolean flag = this.getOptionOrdinalValue(settingOption);
            return flag ? s + I18n.format("options.on") : s + I18n.format("options.off");
        }

        // 处理多选列表类型的选项 (Ordinal/Enum)
        if (settingOption == Options.GUI_SCALE) {
            return s + getTranslation(GUISCALES, this.guiScale);
        }

        if (settingOption == Options.CHAT_VISIBILITY) {
            return s + I18n.format(this.chatVisibility.getResourceKey());
        }

        if (settingOption == Options.PARTICLES) {
            return s + getTranslation(PARTICLES, this.particleSetting);
        }

        if (settingOption == Options.AMBIENT_OCCLUSION) {
            return s + getTranslation(AMBIENT_OCCLUSIONS, this.ambientOcclusion);
        }

        if (settingOption == Options.RENDER_CLOUDS) {
            return s + getTranslation(CLOUDS_TYPES, this.clouds);
        }

        if (settingOption == Options.GRAPHICS) {
            return this.fancyGraphics ? s + I18n.format("options.graphics.fancy") : s + I18n.format("options.graphics.fast");
        }

        return s;
    }

    /**
     * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
     */
    public void loadOptions() {
        try {
            if (!this.optionsFile.exists()) {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new FileReader(this.optionsFile));
            String s = "";
            this.mapSoundLevels.clear();

            while (null != (s = bufferedreader.readLine())) {
                try {
                    String[] astring = s.split(":");

                    if ("mouseSensitivity".equals(astring[0])) {
                        this.mouseSensitivity = this.parseFloat(astring[1]);
                    }

                    if ("fov".equals(astring[0])) {
                        this.fovSetting = this.parseFloat(astring[1]) * 40.0F + 70.0F;
                    }

                    if ("gamma".equals(astring[0])) {
                        this.gammaSetting = this.parseFloat(astring[1]);
                    }

                    if ("saturation".equals(astring[0])) {
                        this.saturation = this.parseFloat(astring[1]);
                    }

                    if ("invertYMouse".equals(astring[0])) {
                        this.invertMouse = "true".equals(astring[1]);
                    }

                    if ("renderDistance".equals(astring[0])) {
                        this.renderDistanceChunks = Integer.parseInt(astring[1]);
                    }

                    if ("guiScale".equals(astring[0])) {
                        this.guiScale = Integer.parseInt(astring[1]);
                    }

                    if ("particles".equals(astring[0])) {
                        this.particleSetting = Integer.parseInt(astring[1]);
                    }

                    if ("bobView".equals(astring[0])) {
                        this.viewBobbing = "true".equals(astring[1]);
                    }

                    if ("anaglyph3d".equals(astring[0])) {
                        this.anaglyph = "true".equals(astring[1]);
                    }

                    if ("maxFps".equals(astring[0])) {
                        this.limitFramerate = Integer.parseInt(astring[1]);
                    }

                    if ("fboEnable".equals(astring[0])) {
                        this.fboEnable = "true".equals(astring[1]);
                    }

                    if ("difficulty".equals(astring[0])) {
                        this.difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(astring[1]));
                    }

                    if ("fancyGraphics".equals(astring[0])) {
                        this.fancyGraphics = "true".equals(astring[1]);
                    }

                    if ("ao".equals(astring[0])) {
                        if ("true".equals(astring[1])) {
                            this.ambientOcclusion = 2;
                        } else if ("false".equals(astring[1])) {
                            this.ambientOcclusion = 0;
                        } else {
                            this.ambientOcclusion = Integer.parseInt(astring[1]);
                        }
                    }

                    if ("renderClouds".equals(astring[0])) {
                        if ("true".equals(astring[1])) {
                            this.clouds = 2;
                        } else if ("false".equals(astring[1])) {
                            this.clouds = 0;
                        } else if ("fast".equals(astring[1])) {
                            this.clouds = 1;
                        }
                    }

                    if ("resourcePacks".equals(astring[0])) {
                        this.resourcePacks = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                        if (null == resourcePacks) {
                            this.resourcePacks = Lists.newArrayList();
                        }
                    }

                    if ("incompatibleResourcePacks".equals(astring[0])) {
                        this.incompatibleResourcePacks = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                        if (null == incompatibleResourcePacks) {
                            this.incompatibleResourcePacks = Lists.newArrayList();
                        }
                    }

                    if ("lastServer".equals(astring[0]) && 2 <= astring.length) {
                        this.lastServer = s.substring(s.indexOf(58) + 1);
                    }

                    if ("lang".equals(astring[0]) && 2 <= astring.length) {
                        this.language = astring[1];
                    }

                    if ("chatVisibility".equals(astring[0])) {
                        this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(astring[1]));
                    }

                    if ("chatColors".equals(astring[0])) {
                        this.chatColours = "true".equals(astring[1]);
                    }

                    if ("chatLinks".equals(astring[0])) {
                        this.chatLinks = "true".equals(astring[1]);
                    }

                    if ("chatLinksPrompt".equals(astring[0])) {
                        this.chatLinksPrompt = "true".equals(astring[1]);
                    }

                    if ("chatOpacity".equals(astring[0])) {
                        this.chatOpacity = this.parseFloat(astring[1]);
                    }

                    if ("snooperEnabled".equals(astring[0])) {
                        this.snooperEnabled = "true".equals(astring[1]);
                    }

                    if ("fullscreen".equals(astring[0])) {
                        this.fullScreen = "true".equals(astring[1]);
                    }

                    if ("enableVsync".equals(astring[0])) {
                        this.enableVsync = "true".equals(astring[1]);
                    }

                    if ("useVbo".equals(astring[0])) {
                        this.useVbo = "true".equals(astring[1]);
                    }

                    if ("hideServerAddress".equals(astring[0])) {
                        this.hideServerAddress = "true".equals(astring[1]);
                    }

                    if ("advancedItemTooltips".equals(astring[0])) {
                        this.advancedItemTooltips = "true".equals(astring[1]);
                    }

                    if ("pauseOnLostFocus".equals(astring[0])) {
                        this.pauseOnLostFocus = "true".equals(astring[1]);
                    }

                    if ("touchscreen".equals(astring[0])) {
                        this.touchscreen = "true".equals(astring[1]);
                    }

                    if ("overrideHeight".equals(astring[0])) {
                        this.overrideHeight = Integer.parseInt(astring[1]);
                    }

                    if ("overrideWidth".equals(astring[0])) {
                        this.overrideWidth = Integer.parseInt(astring[1]);
                    }

                    if ("heldItemTooltips".equals(astring[0])) {
                        this.heldItemTooltips = "true".equals(astring[1]);
                    }

                    if ("chatHeightFocused".equals(astring[0])) {
                        this.chatHeightFocused = this.parseFloat(astring[1]);
                    }

                    if ("chatHeightUnfocused".equals(astring[0])) {
                        this.chatHeightUnfocused = this.parseFloat(astring[1]);
                    }

                    if ("chatScale".equals(astring[0])) {
                        this.chatScale = this.parseFloat(astring[1]);
                    }

                    if ("chatWidth".equals(astring[0])) {
                        this.chatWidth = this.parseFloat(astring[1]);
                    }

                    if ("showInventoryAchievementHint".equals(astring[0])) {
                        this.showInventoryAchievementHint = "true".equals(astring[1]);
                    }

                    if ("mipmapLevels".equals(astring[0])) {
                        this.mipmapLevels = Integer.parseInt(astring[1]);
                    }

                    if ("forceUnicodeFont".equals(astring[0])) {
                        this.forceUnicodeFont = "true".equals(astring[1]);
                    }

                    if ("allowBlockAlternatives".equals(astring[0])) {
                        this.allowBlockAlternatives = "true".equals(astring[1]);
                    }

                    if ("reducedDebugInfo".equals(astring[0])) {
                        this.reducedDebugInfo = "true".equals(astring[1]);
                    }

                    if ("useNativeTransport".equals(astring[0])) {
                        this.useNativeTransport = "true".equals(astring[1]);
                    }

                    if ("entityShadows".equals(astring[0])) {
                        this.entityShadows = "true".equals(astring[1]);
                    }

                    for (KeyBinding keybinding : this.keyBindings) {
                        if (astring[0].equals("key_" + keybinding.getKeyDescription())) {
                            keybinding.setKeyCode(Integer.parseInt(astring[1]));
                        }
                    }

                    for (SoundCategory soundcategory : SoundCategory.values()) {
                        if (astring[0].equals("soundCategory_" + soundcategory.getCategoryName())) {
                            this.mapSoundLevels.put(soundcategory, Float.valueOf(this.parseFloat(astring[1])));
                        }
                    }

                    for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                        if (astring[0].equals("modelPart_" + enumplayermodelparts.getPartName())) {
                            this.setModelPartEnabled(enumplayermodelparts, "true".equals(astring[1]));
                        }
                    }
                } catch (Exception var8) {
                    logger.warn("Skipping bad option: " + s);
                }
            }

            KeyBinding.resetKeyBindingArrayAndHash();
            bufferedreader.close();
        } catch (Exception exception) {
            logger.error("Failed to load options", exception);
        }
    }

    /**
     * Parses a string into a float.
     *
     * @param str The string to parse
     */
    private float parseFloat(String str) {
        return "true".equals(str) ? 1.0F : ("false".equals(str) ? 0.0F : Float.parseFloat(str));
    }

    /**
     * Saves the options to the options file.
     */
    public void saveOptions() {
        try {
            PrintWriter printwriter = new PrintWriter(new FileWriter(this.optionsFile));
            printwriter.println("invertYMouse:" + this.invertMouse);
            printwriter.println("mouseSensitivity:" + this.mouseSensitivity);
            printwriter.println("fov:" + (this.fovSetting - 70.0F) / 40.0F);
            printwriter.println("gamma:" + this.gammaSetting);
            printwriter.println("saturation:" + this.saturation);
            printwriter.println("renderDistance:" + this.renderDistanceChunks);
            printwriter.println("guiScale:" + this.guiScale);
            printwriter.println("particles:" + this.particleSetting);
            printwriter.println("bobView:" + this.viewBobbing);
            printwriter.println("anaglyph3d:" + this.anaglyph);
            printwriter.println("maxFps:" + this.limitFramerate);
            printwriter.println("fboEnable:" + this.fboEnable);
            printwriter.println("difficulty:" + this.difficulty.getDifficultyId());
            printwriter.println("fancyGraphics:" + this.fancyGraphics);
            printwriter.println("ao:" + this.ambientOcclusion);

            switch (this.clouds) {
                case 0:
                    printwriter.println("renderClouds:false");
                    break;

                case 1:
                    printwriter.println("renderClouds:fast");
                    break;

                case 2:
                    printwriter.println("renderClouds:true");
            }

            printwriter.println("resourcePacks:" + gson.toJson(this.resourcePacks));
            printwriter.println("incompatibleResourcePacks:" + gson.toJson(this.incompatibleResourcePacks));
            printwriter.println("lastServer:" + this.lastServer);
            printwriter.println("lang:" + this.language);
            printwriter.println("chatVisibility:" + this.chatVisibility.getChatVisibility());
            printwriter.println("chatColors:" + this.chatColours);
            printwriter.println("chatLinks:" + this.chatLinks);
            printwriter.println("chatLinksPrompt:" + this.chatLinksPrompt);
            printwriter.println("chatOpacity:" + this.chatOpacity);
            printwriter.println("snooperEnabled:" + this.snooperEnabled);
            printwriter.println("fullscreen:" + this.fullScreen);
            printwriter.println("enableVsync:" + this.enableVsync);
            printwriter.println("useVbo:" + this.useVbo);
            printwriter.println("hideServerAddress:" + this.hideServerAddress);
            printwriter.println("advancedItemTooltips:" + this.advancedItemTooltips);
            printwriter.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            printwriter.println("touchscreen:" + this.touchscreen);
            printwriter.println("overrideWidth:" + this.overrideWidth);
            printwriter.println("overrideHeight:" + this.overrideHeight);
            printwriter.println("heldItemTooltips:" + this.heldItemTooltips);
            printwriter.println("chatHeightFocused:" + this.chatHeightFocused);
            printwriter.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            printwriter.println("chatScale:" + this.chatScale);
            printwriter.println("chatWidth:" + this.chatWidth);
            printwriter.println("showInventoryAchievementHint:" + this.showInventoryAchievementHint);
            printwriter.println("mipmapLevels:" + this.mipmapLevels);
            printwriter.println("forceUnicodeFont:" + this.forceUnicodeFont);
            printwriter.println("allowBlockAlternatives:" + this.allowBlockAlternatives);
            printwriter.println("reducedDebugInfo:" + this.reducedDebugInfo);
            printwriter.println("useNativeTransport:" + this.useNativeTransport);
            printwriter.println("entityShadows:" + this.entityShadows);

            for (KeyBinding keybinding : this.keyBindings) {
                printwriter.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.values()) {
                printwriter.println("soundCategory_" + soundcategory.getCategoryName() + ":" + this.getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                printwriter.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + this.setModelParts.contains(enumplayermodelparts));
            }

            printwriter.close();
        } catch (Exception exception) {
            logger.error("Failed to save options", exception);
        }

        this.sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory sndCategory) {
        return this.mapSoundLevels.containsKey(sndCategory) ? this.mapSoundLevels.get(sndCategory).floatValue() : 1.0F;
    }

    public void setSoundLevel(SoundCategory sndCategory, float soundLevel) {
        this.mc.getSoundHandler().setSoundLevel(sndCategory, soundLevel);
        this.mapSoundLevels.put(sndCategory, Float.valueOf(soundLevel));
    }

    /**
     * Send a client info packet with settings information to the server
     */
    public void sendSettingsToServer() {
        if (null != mc.thePlayer) {
            int i = 0;

            for (EnumPlayerModelParts enumplayermodelparts : this.setModelParts) {
                i |= enumplayermodelparts.getPartMask();
            }

            this.mc.thePlayer.sendQueue.addToSendQueue(new C15PacketClientSettings(this.language, this.renderDistanceChunks, this.chatVisibility, this.chatColours, i));
        }
    }

    public Set<EnumPlayerModelParts> getModelParts() {
        return ImmutableSet.copyOf(this.setModelParts);
    }

    public void setModelPartEnabled(EnumPlayerModelParts modelPart, boolean enable) {
        if (enable) {
            this.setModelParts.add(modelPart);
        } else {
            this.setModelParts.remove(modelPart);
        }

        this.sendSettingsToServer();
    }

    public void switchModelPartEnabled(EnumPlayerModelParts modelPart) {
        if (!this.getModelParts().contains(modelPart)) {
            this.setModelParts.add(modelPart);
        } else {
            this.setModelParts.remove(modelPart);
        }

        this.sendSettingsToServer();
    }

    /**
     * Return true if the clouds should be rendered
     */
    public int shouldRenderClouds() {
        return 4 <= renderDistanceChunks ? this.clouds : 0;
    }

    /**
     * Return true if the client connect to a server using the native transport system
     */
    public boolean isUsingNativeTransport() {
        return this.useNativeTransport;
    }

    public enum Options {
        INVERT_MOUSE("options.invertMouse", false, true),
        SENSITIVITY("options.sensitivity", true, false),
        FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
        GAMMA("options.gamma", true, false),
        SATURATION("options.saturation", true, false),
        RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
        VIEW_BOBBING("options.viewBobbing", false, true),
        ANAGLYPH("options.anaglyph", false, true),
        FRAMERATE_LIMIT("options.framerateLimit", true, false, 10.0F, 260.0F, 10.0F),
        FBO_ENABLE("options.fboEnable", false, true),
        RENDER_CLOUDS("options.renderClouds", false, false),
        GRAPHICS("options.graphics", false, false),
        AMBIENT_OCCLUSION("options.ao", false, false),
        GUI_SCALE("options.guiScale", false, false),
        PARTICLES("options.particles", false, false),
        CHAT_VISIBILITY("options.chat.visibility", false, false),
        CHAT_COLOR("options.chat.color", false, true),
        CHAT_LINKS("options.chat.links", false, true),
        CHAT_OPACITY("options.chat.opacity", true, false),
        CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
        SNOOPER_ENABLED("options.snooper", false, true),
        USE_FULLSCREEN("options.fullscreen", false, true),
        ENABLE_VSYNC("options.vsync", false, true),
        USE_VBO("options.vbo", false, true),
        TOUCHSCREEN("options.touchscreen", false, true),
        CHAT_SCALE("options.chat.scale", true, false),
        CHAT_WIDTH("options.chat.width", true, false),
        CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
        CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
        MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
        FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
        BLOCK_ALTERNATIVES("options.blockAlternatives", false, true),
        REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
        ENTITY_SHADOWS("options.entityShadows", false, true);

        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private final float valueMin;
        private float valueMax;

        public static GameSettings.Options getEnumOptions(int ordinal) {
            for (GameSettings.Options gamesettings$options : values()) {
                if (gamesettings$options.returnEnumOrdinal() == ordinal) {
                    return gamesettings$options;
                }
            }

            return null;
        }

        Options(String str, boolean isFloat, boolean isBoolean) {
            this(str, isFloat, isBoolean, 0.0F, 1.0F, 0.0F);
        }

        Options(String str, boolean isFloat, boolean isBoolean, float valMin, float valMax, float valStep) {
            this.enumString = str;
            this.enumFloat = isFloat;
            this.enumBoolean = isBoolean;
            this.valueMin = valMin;
            this.valueMax = valMax;
            this.valueStep = valStep;
        }

        public boolean getEnumFloat() {
            return this.enumFloat;
        }

        public boolean getEnumBoolean() {
            return this.enumBoolean;
        }

        public int returnEnumOrdinal() {
            return this.ordinal();
        }

        public String getEnumString() {
            return this.enumString;
        }

        public float getValueMax() {
            return this.valueMax;
        }

        public void setValueMax(float value) {
            this.valueMax = value;
        }

        public float normalizeValue(float value) {
            return MathHelper.clamp_float((this.snapToStepClamp(value) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float value) {
            return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(value, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float value) {
            value = this.snapToStep(value);
            return MathHelper.clamp_float(value, this.valueMin, this.valueMax);
        }

        private float snapToStep(float value) {
            if (0.0F < valueStep) {
                value = this.valueStep * (float) Math.round(value / this.valueStep);
            }

            return value;
        }
    }
}