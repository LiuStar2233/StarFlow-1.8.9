package net.minecraft.client.gui.achievement;

import com.google.common.collect.Lists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GuiStats extends GuiScreen implements IProgressMeter {
    protected GuiScreen parentScreen;
    protected String screenTitle = "Select world";
    private GuiStats.StatsGeneral generalStats;
    private GuiStats.StatsItem itemStats;
    private GuiStats.StatsBlock blockStats;
    private GuiStats.StatsMobsList mobStats;
    private final StatFileWriter field_146546_t;
    private GuiSlot displaySlot;

    /**
     * When true, the game will be paused when the gui is shown
     */
    private boolean doesGuiPauseGame = true;

    public GuiStats(GuiScreen p_i1071_1_, StatFileWriter p_i1071_2_) {
        this.parentScreen = p_i1071_1_;
        this.field_146546_t = p_i1071_2_;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    public void initGui() {
        this.screenTitle = I18n.format("gui.stats");
        this.doesGuiPauseGame = true;
        this.mc.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
    }

    /**
     * Handles mouse input.
     */
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        if (null != displaySlot) {
            this.displaySlot.handleMouseInput();
        }
    }

    public void func_175366_f() {
        this.generalStats = new GuiStats.StatsGeneral(this.mc);
        this.generalStats.registerScrollButtons(1, 1);
        this.itemStats = new GuiStats.StatsItem(this.mc);
        this.itemStats.registerScrollButtons(1, 1);
        this.blockStats = new GuiStats.StatsBlock(this.mc);
        this.blockStats.registerScrollButtons(1, 1);
        this.mobStats = new GuiStats.StatsMobsList(this.mc);
        this.mobStats.registerScrollButtons(1, 1);
    }

    public void createButtons() {
        this.buttonList.add(new GuiButton(0, this.width / 2 + 4, this.height - 28, 150, 20, I18n.format("gui.done")));
        this.buttonList.add(new GuiButton(1, this.width / 2 - 160, this.height - 52, 80, 20, I18n.format("stat.generalButton")));
        GuiButton guibutton;
        this.buttonList.add(guibutton = new GuiButton(2, this.width / 2 - 80, this.height - 52, 80, 20, I18n.format("stat.blocksButton")));
        GuiButton guibutton1;
        this.buttonList.add(guibutton1 = new GuiButton(3, this.width / 2, this.height - 52, 80, 20, I18n.format("stat.itemsButton")));
        GuiButton guibutton2;
        this.buttonList.add(guibutton2 = new GuiButton(4, this.width / 2 + 80, this.height - 52, 80, 20, I18n.format("stat.mobsButton")));

        if (0 == blockStats.getSize()) {
            guibutton.enabled = false;
        }

        if (0 == itemStats.getSize()) {
            guibutton1.enabled = false;
        }

        if (0 == mobStats.getSize()) {
            guibutton2.enabled = false;
        }
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (0 == button.id) {
                this.mc.displayGuiScreen(this.parentScreen);
            } else if (1 == button.id) {
                this.displaySlot = this.generalStats;
            } else if (3 == button.id) {
                this.displaySlot = this.itemStats;
            } else if (2 == button.id) {
                this.displaySlot = this.blockStats;
            } else if (4 == button.id) {
                this.displaySlot = this.mobStats;
            } else {
                this.displaySlot.actionPerformed(button);
            }
        }
    }

    /**
     * Draws the screen and all the components in it. Args : mouseX, mouseY, renderPartialTicks
     */
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.doesGuiPauseGame) {
            this.drawDefaultBackground();
            this.drawCenteredString(this.fontRendererObj, I18n.format("multiplayer.downloadingStats"), this.width / 2, this.height / 2, 16777215);
            this.drawCenteredString(this.fontRendererObj, lanSearchStates[(int) (Minecraft.getSystemTime() / 150L % (long) lanSearchStates.length)], this.width / 2, this.height / 2 + this.fontRendererObj.FONT_HEIGHT * 2, 16777215);
        } else {
            this.displaySlot.drawScreen(mouseX, mouseY, partialTicks);
            this.drawCenteredString(this.fontRendererObj, this.screenTitle, this.width / 2, 20, 16777215);
            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }

    public void doneLoading() {
        if (this.doesGuiPauseGame) {
            this.func_175366_f();
            this.createButtons();
            this.displaySlot = this.generalStats;
            this.doesGuiPauseGame = false;
        }
    }

    /**
     * Returns true if this GUI should pause the game when it is displayed in single-player
     */
    public boolean doesGuiPauseGame() {
        return !this.doesGuiPauseGame;
    }

    private void drawStatsScreen(int p_146521_1_, int p_146521_2_, Item p_146521_3_) {
        this.drawButtonBackground(p_146521_1_ + 1, p_146521_2_ + 1);
        GlStateManager.enableRescaleNormal();
        RenderHelper.enableGUIStandardItemLighting();
        this.itemRender.renderItemIntoGUI(new ItemStack(p_146521_3_, 1, 0), p_146521_1_ + 2, p_146521_2_ + 2);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableRescaleNormal();
    }

    /**
     * Draws a gray box that serves as a button background.
     */
    private void drawButtonBackground(int p_146531_1_, int p_146531_2_) {
        this.drawSprite(p_146531_1_, p_146531_2_, 0, 0);
    }

    /**
     * Draws a sprite from assets/textures/gui/container/stats_icons.png
     */
    private void drawSprite(int p_146527_1_, int p_146527_2_, int p_146527_3_, int p_146527_4_) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(Gui.statIcons);
        final float f = 0.0078125F;
        final float f1 = 0.0078125F;
        final int i = 18;
        final int j = 18;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(p_146527_1_, p_146527_2_ + 18, this.zLevel).tex((float) (p_146527_3_) * 0.0078125F, (float) (p_146527_4_ + 18) * 0.0078125F).endVertex();
        worldrenderer.pos(p_146527_1_ + 18, p_146527_2_ + 18, this.zLevel).tex((float) (p_146527_3_ + 18) * 0.0078125F, (float) (p_146527_4_ + 18) * 0.0078125F).endVertex();
        worldrenderer.pos(p_146527_1_ + 18, p_146527_2_, this.zLevel).tex((float) (p_146527_3_ + 18) * 0.0078125F, (float) (p_146527_4_) * 0.0078125F).endVertex();
        worldrenderer.pos(p_146527_1_, p_146527_2_, this.zLevel).tex((float) (p_146527_3_) * 0.0078125F, (float) (p_146527_4_) * 0.0078125F).endVertex();
        tessellator.draw();
    }

    abstract class Stats extends GuiSlot {
        protected int field_148218_l = -1;
        protected List<StatCrafting> statsHolder;
        protected Comparator<StatCrafting> statSorter;
        protected int field_148217_o = -1;
        protected int field_148215_p;

        protected Stats(Minecraft mcIn) {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 20);
            this.setShowSelectionBox(false);
            this.setHasListHeader(true, 20);
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        }

        protected boolean isSelected(int slotIndex) {
            return false;
        }

        protected void drawBackground() {
            GuiStats.this.drawDefaultBackground();
        }

        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {
            if (!Mouse.isButtonDown(0)) {
                this.field_148218_l = -1;
            }

            if (0 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 0, 0);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 0, 18);
            }

            if (1 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 0, 0);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 0, 18);
            }

            if (2 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 0, 0);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 0, 18);
            }

            if (-1 != field_148217_o) {
                int i = 79;
                int j = 18;

                if (1 == field_148217_o) {
                    i = 129;
                } else if (2 == field_148217_o) {
                    i = 179;
                }

                if (1 == field_148215_p) {
                    j = 36;
                }

                GuiStats.this.drawSprite(p_148129_1_ + i, p_148129_2_ + 1, j, 0);
            }
        }

        protected void func_148132_a(int p_148132_1_, int p_148132_2_) {
            this.field_148218_l = -1;

            if (79 <= p_148132_1_ && 115 > p_148132_1_) {
                this.field_148218_l = 0;
            } else if (129 <= p_148132_1_ && 165 > p_148132_1_) {
                this.field_148218_l = 1;
            } else if (179 <= p_148132_1_ && 215 > p_148132_1_) {
                this.field_148218_l = 2;
            }

            if (0 <= field_148218_l) {
                this.func_148212_h(this.field_148218_l);
                this.mc.getSoundHandler().playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1.0F));
            }
        }

        protected final int getSize() {
            return this.statsHolder.size();
        }

        protected final StatCrafting func_148211_c(int p_148211_1_) {
            return this.statsHolder.get(p_148211_1_);
        }

        protected abstract String func_148210_b(int p_148210_1_);

        protected void func_148209_a(StatBase p_148209_1_, int p_148209_2_, int p_148209_3_, boolean p_148209_4_) {
            if (null != p_148209_1_) {
                String s = p_148209_1_.format(GuiStats.this.field_146546_t.readStat(p_148209_1_));
                GuiStats.this.drawString(GuiStats.this.fontRendererObj, s, p_148209_2_ - GuiStats.this.fontRendererObj.getStringWidth(s), p_148209_3_ + 5, p_148209_4_ ? 16777215 : 9474192);
            } else {
                final String s1 = "-";
                GuiStats.this.drawString(GuiStats.this.fontRendererObj, s1, p_148209_2_ - GuiStats.this.fontRendererObj.getStringWidth(s1), p_148209_3_ + 5, p_148209_4_ ? 16777215 : 9474192);
            }
        }

        protected void func_148142_b(int p_148142_1_, int p_148142_2_) {
            if (p_148142_2_ >= this.top && p_148142_2_ <= this.bottom) {
                int i = this.getSlotIndexFromScreenCoords(p_148142_1_, p_148142_2_);
                int j = this.width / 2 - 92 - 16;

                if (0 <= i) {
                    if (p_148142_1_ < j + 40 || p_148142_1_ > j + 40 + 20) {
                        return;
                    }

                    StatCrafting statcrafting = this.func_148211_c(i);
                    this.func_148213_a(statcrafting, p_148142_1_, p_148142_2_);
                } else {
                    String s = "";

                    if (p_148142_1_ >= j + 115 - 18 && p_148142_1_ <= j + 115) {
                        s = this.func_148210_b(0);
                    } else if (p_148142_1_ >= j + 165 - 18 && p_148142_1_ <= j + 165) {
                        s = this.func_148210_b(1);
                    } else {
                        if (p_148142_1_ < j + 215 - 18 || p_148142_1_ > j + 215) {
                            return;
                        }

                        s = this.func_148210_b(2);
                    }

                    s = (I18n.format(s)).trim();

                    if (0 < s.length()) {
                        int k = p_148142_1_ + 12;
                        int l = p_148142_2_ - 12;
                        int i1 = GuiStats.this.fontRendererObj.getStringWidth(s);
                        GuiStats.this.drawGradientRect(k - 3, l - 3, k + i1 + 3, l + 8 + 3, -1073741824, -1073741824);
                        GuiStats.this.fontRendererObj.drawStringWithShadow(s, (float) k, (float) l, -1);
                    }
                }
            }
        }

        protected void func_148213_a(StatCrafting p_148213_1_, int p_148213_2_, int p_148213_3_) {
            if (null != p_148213_1_) {
                Item item = p_148213_1_.func_150959_a();
                ItemStack itemstack = new ItemStack(item);
                String s = itemstack.getUnlocalizedName();
                String s1 = (I18n.format(s + ".name")).trim();

                if (0 < s1.length()) {
                    int i = p_148213_2_ + 12;
                    int j = p_148213_3_ - 12;
                    int k = GuiStats.this.fontRendererObj.getStringWidth(s1);
                    GuiStats.this.drawGradientRect(i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
                    GuiStats.this.fontRendererObj.drawStringWithShadow(s1, (float) i, (float) j, -1);
                }
            }
        }

        protected void func_148212_h(int p_148212_1_) {
            if (p_148212_1_ != this.field_148217_o) {
                this.field_148217_o = p_148212_1_;
                this.field_148215_p = -1;
            } else if (-1 == field_148215_p) {
                this.field_148215_p = 1;
            } else {
                this.field_148217_o = -1;
                this.field_148215_p = 0;
            }

            Collections.sort(this.statsHolder, this.statSorter);
        }
    }

    class StatsBlock extends GuiStats.Stats {
        public StatsBlock(Minecraft mcIn) {
            super(mcIn);
            this.statsHolder = Lists.newArrayList();

            for (StatCrafting statcrafting : StatList.objectMineStats) {
                boolean flag = false;
                int i = Item.getIdFromItem(statcrafting.func_150959_a());

                if (0 < field_146546_t.readStat(statcrafting)) {
                    flag = true;
                } else if (null != StatList.objectUseStats[i] && 0 < field_146546_t.readStat(StatList.objectUseStats[i])) {
                    flag = true;
                } else if (null != StatList.objectCraftStats[i] && 0 < field_146546_t.readStat(StatList.objectCraftStats[i])) {
                    flag = true;
                }

                if (flag) {
                    this.statsHolder.add(statcrafting);
                }
            }

            this.statSorter = new Comparator<StatCrafting>() {
                public int compare(StatCrafting p_compare_1_, StatCrafting p_compare_2_) {
                    int j = Item.getIdFromItem(p_compare_1_.func_150959_a());
                    int k = Item.getIdFromItem(p_compare_2_.func_150959_a());
                    StatBase statbase = null;
                    StatBase statbase1 = null;

                    if (2 == field_148217_o) {
                        statbase = StatList.mineBlockStatArray[j];
                        statbase1 = StatList.mineBlockStatArray[k];
                    } else if (0 == field_148217_o) {
                        statbase = StatList.objectCraftStats[j];
                        statbase1 = StatList.objectCraftStats[k];
                    } else if (1 == field_148217_o) {
                        statbase = StatList.objectUseStats[j];
                        statbase1 = StatList.objectUseStats[k];
                    }

                    if (null != statbase || null != statbase1) {
                        if (null == statbase) {
                            return 1;
                        }

                        if (null == statbase1) {
                            return -1;
                        }

                        int l = GuiStats.this.field_146546_t.readStat(statbase);
                        int i1 = GuiStats.this.field_146546_t.readStat(statbase1);

                        if (l != i1) {
                            return (l - i1) * StatsBlock.this.field_148215_p;
                        }
                    }

                    return j - k;
                }
            };
        }

        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {
            super.drawListHeader(p_148129_1_, p_148129_2_, p_148129_3_);

            if (0 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18 + 1, p_148129_2_ + 1 + 1, 18, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 18, 18);
            }

            if (1 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18 + 1, p_148129_2_ + 1 + 1, 36, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 36, 18);
            }

            if (2 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18 + 1, p_148129_2_ + 1 + 1, 54, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 54, 18);
            }
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            StatCrafting statcrafting = this.func_148211_c(entryID);
            Item item = statcrafting.func_150959_a();
            GuiStats.this.drawStatsScreen(p_180791_2_ + 40, p_180791_3_, item);
            int i = Item.getIdFromItem(item);
            this.func_148209_a(StatList.objectCraftStats[i], p_180791_2_ + 115, p_180791_3_, 0 == entryID % 2);
            this.func_148209_a(StatList.objectUseStats[i], p_180791_2_ + 165, p_180791_3_, 0 == entryID % 2);
            this.func_148209_a(statcrafting, p_180791_2_ + 215, p_180791_3_, 0 == entryID % 2);
        }

        protected String func_148210_b(int p_148210_1_) {
            return 0 == p_148210_1_ ? "stat.crafted" : (1 == p_148210_1_ ? "stat.used" : "stat.mined");
        }
    }

    class StatsGeneral extends GuiSlot {
        public StatsGeneral(Minecraft mcIn) {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, 10);
            this.setShowSelectionBox(false);
        }

        protected int getSize() {
            return StatList.generalStats.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        }

        protected boolean isSelected(int slotIndex) {
            return false;
        }

        protected int getContentHeight() {
            return this.getSize() * 10;
        }

        protected void drawBackground() {
            GuiStats.this.drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            StatBase statbase = StatList.generalStats.get(entryID);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, statbase.getStatName().getUnformattedText(), p_180791_2_ + 2, p_180791_3_ + 1, 0 == entryID % 2 ? 16777215 : 9474192);
            String s = statbase.format(GuiStats.this.field_146546_t.readStat(statbase));
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, s, p_180791_2_ + 2 + 213 - GuiStats.this.fontRendererObj.getStringWidth(s), p_180791_3_ + 1, 0 == entryID % 2 ? 16777215 : 9474192);
        }
    }

    class StatsItem extends GuiStats.Stats {
        public StatsItem(Minecraft mcIn) {
            super(mcIn);
            this.statsHolder = Lists.newArrayList();

            for (StatCrafting statcrafting : StatList.itemStats) {
                boolean flag = false;
                int i = Item.getIdFromItem(statcrafting.func_150959_a());

                if (0 < field_146546_t.readStat(statcrafting)) {
                    flag = true;
                } else if (null != StatList.objectBreakStats[i] && 0 < field_146546_t.readStat(StatList.objectBreakStats[i])) {
                    flag = true;
                } else if (null != StatList.objectCraftStats[i] && 0 < field_146546_t.readStat(StatList.objectCraftStats[i])) {
                    flag = true;
                }

                if (flag) {
                    this.statsHolder.add(statcrafting);
                }
            }

            this.statSorter = new Comparator<StatCrafting>() {
                public int compare(StatCrafting p_compare_1_, StatCrafting p_compare_2_) {
                    int j = Item.getIdFromItem(p_compare_1_.func_150959_a());
                    int k = Item.getIdFromItem(p_compare_2_.func_150959_a());
                    StatBase statbase = null;
                    StatBase statbase1 = null;

                    if (0 == field_148217_o) {
                        statbase = StatList.objectBreakStats[j];
                        statbase1 = StatList.objectBreakStats[k];
                    } else if (1 == field_148217_o) {
                        statbase = StatList.objectCraftStats[j];
                        statbase1 = StatList.objectCraftStats[k];
                    } else if (2 == field_148217_o) {
                        statbase = StatList.objectUseStats[j];
                        statbase1 = StatList.objectUseStats[k];
                    }

                    if (null != statbase || null != statbase1) {
                        if (null == statbase) {
                            return 1;
                        }

                        if (null == statbase1) {
                            return -1;
                        }

                        int l = GuiStats.this.field_146546_t.readStat(statbase);
                        int i1 = GuiStats.this.field_146546_t.readStat(statbase1);

                        if (l != i1) {
                            return (l - i1) * StatsItem.this.field_148215_p;
                        }
                    }

                    return j - k;
                }
            };
        }

        protected void drawListHeader(int p_148129_1_, int p_148129_2_, Tessellator p_148129_3_) {
            super.drawListHeader(p_148129_1_, p_148129_2_, p_148129_3_);

            if (0 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18 + 1, p_148129_2_ + 1 + 1, 72, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 115 - 18, p_148129_2_ + 1, 72, 18);
            }

            if (1 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18 + 1, p_148129_2_ + 1 + 1, 18, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 165 - 18, p_148129_2_ + 1, 18, 18);
            }

            if (2 == field_148218_l) {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18 + 1, p_148129_2_ + 1 + 1, 36, 18);
            } else {
                GuiStats.this.drawSprite(p_148129_1_ + 215 - 18, p_148129_2_ + 1, 36, 18);
            }
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            StatCrafting statcrafting = this.func_148211_c(entryID);
            Item item = statcrafting.func_150959_a();
            GuiStats.this.drawStatsScreen(p_180791_2_ + 40, p_180791_3_, item);
            int i = Item.getIdFromItem(item);
            this.func_148209_a(StatList.objectBreakStats[i], p_180791_2_ + 115, p_180791_3_, 0 == entryID % 2);
            this.func_148209_a(StatList.objectCraftStats[i], p_180791_2_ + 165, p_180791_3_, 0 == entryID % 2);
            this.func_148209_a(statcrafting, p_180791_2_ + 215, p_180791_3_, 0 == entryID % 2);
        }

        protected String func_148210_b(int p_148210_1_) {
            return 1 == p_148210_1_ ? "stat.crafted" : (2 == p_148210_1_ ? "stat.used" : "stat.depleted");
        }
    }

    class StatsMobsList extends GuiSlot {
        private final List<EntityList.EntityEggInfo> field_148222_l = Lists.newArrayList();

        public StatsMobsList(Minecraft mcIn) {
            super(mcIn, GuiStats.this.width, GuiStats.this.height, 32, GuiStats.this.height - 64, GuiStats.this.fontRendererObj.FONT_HEIGHT * 4);
            this.setShowSelectionBox(false);

            for (EntityList.EntityEggInfo entitylist$entityegginfo : EntityList.entityEggs.values()) {
                if (0 < field_146546_t.readStat(entitylist$entityegginfo.field_151512_d) || 0 < field_146546_t.readStat(entitylist$entityegginfo.field_151513_e)) {
                    this.field_148222_l.add(entitylist$entityegginfo);
                }
            }
        }

        protected int getSize() {
            return this.field_148222_l.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        }

        protected boolean isSelected(int slotIndex) {
            return false;
        }

        protected int getContentHeight() {
            return this.getSize() * GuiStats.this.fontRendererObj.FONT_HEIGHT * 4;
        }

        protected void drawBackground() {
            GuiStats.this.drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            EntityList.EntityEggInfo entitylist$entityegginfo = this.field_148222_l.get(entryID);
            String s = I18n.format("entity." + EntityList.getStringFromID(entitylist$entityegginfo.spawnedID) + ".name");
            int i = GuiStats.this.field_146546_t.readStat(entitylist$entityegginfo.field_151512_d);
            int j = GuiStats.this.field_146546_t.readStat(entitylist$entityegginfo.field_151513_e);
            String s1 = I18n.format("stat.entityKills", Integer.valueOf(i), s);
            String s2 = I18n.format("stat.entityKilledBy", s, Integer.valueOf(j));

            if (0 == i) {
                s1 = I18n.format("stat.entityKills.none", s);
            }

            if (0 == j) {
                s2 = I18n.format("stat.entityKilledBy.none", s);
            }

            GuiStats.this.drawString(GuiStats.this.fontRendererObj, s, p_180791_2_ + 2 - 10, p_180791_3_ + 1, 16777215);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, s1, p_180791_2_ + 2, p_180791_3_ + 1 + GuiStats.this.fontRendererObj.FONT_HEIGHT, 0 == i ? 6316128 : 9474192);
            GuiStats.this.drawString(GuiStats.this.fontRendererObj, s2, p_180791_2_ + 2, p_180791_3_ + 1 + GuiStats.this.fontRendererObj.FONT_HEIGHT * 2, 0 == j ? 6316128 : 9474192);
        }
    }
}