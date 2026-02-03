package net.minecraft.client.util;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.JsonUtils;
import org.lwjgl.opengl.GL14;

public class JsonBlendingMode {
    private static JsonBlendingMode field_148118_a;
    private final int field_148116_b;
    private final int field_148117_c;
    private final int field_148114_d;
    private final int field_148115_e;
    private final int field_148112_f;
    private final boolean field_148113_g;
    private final boolean field_148119_h;

    private JsonBlendingMode(boolean p_i45084_1_, boolean p_i45084_2_, int p_i45084_3_, int p_i45084_4_, int p_i45084_5_, int p_i45084_6_, int p_i45084_7_) {
        this.field_148113_g = p_i45084_1_;
        this.field_148116_b = p_i45084_3_;
        this.field_148114_d = p_i45084_4_;
        this.field_148117_c = p_i45084_5_;
        this.field_148115_e = p_i45084_6_;
        this.field_148119_h = p_i45084_2_;
        this.field_148112_f = p_i45084_7_;
    }

    public JsonBlendingMode() {
        this(false, true, 1, 0, 1, 0, 32774);
    }

    public JsonBlendingMode(int p_i45085_1_, int p_i45085_2_, int p_i45085_3_) {
        this(false, false, p_i45085_1_, p_i45085_2_, p_i45085_1_, p_i45085_2_, p_i45085_3_);
    }

    public JsonBlendingMode(int p_i45086_1_, int p_i45086_2_, int p_i45086_3_, int p_i45086_4_, int p_i45086_5_) {
        this(true, false, p_i45086_1_, p_i45086_2_, p_i45086_3_, p_i45086_4_, p_i45086_5_);
    }

    public void func_148109_a() {
        if (!this.equals(field_148118_a)) {
            if (null == JsonBlendingMode.field_148118_a || this.field_148119_h != field_148118_a.func_148111_b()) {
                field_148118_a = this;

                if (this.field_148119_h) {
                    GlStateManager.disableBlend();
                    return;
                }

                GlStateManager.enableBlend();
            }

            GL14.glBlendEquation(this.field_148112_f);

            if (this.field_148113_g) {
                GlStateManager.tryBlendFuncSeparate(this.field_148116_b, this.field_148114_d, this.field_148117_c, this.field_148115_e);
            } else {
                GlStateManager.blendFunc(this.field_148116_b, this.field_148114_d);
            }
        }
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        } else if (!(p_equals_1_ instanceof JsonBlendingMode)) {
            return false;
        } else {
            JsonBlendingMode jsonblendingmode = (JsonBlendingMode) p_equals_1_;
            return this.field_148112_f == jsonblendingmode.field_148112_f && (this.field_148115_e == jsonblendingmode.field_148115_e && (this.field_148114_d == jsonblendingmode.field_148114_d && (this.field_148119_h == jsonblendingmode.field_148119_h && (this.field_148113_g == jsonblendingmode.field_148113_g && (this.field_148117_c == jsonblendingmode.field_148117_c && this.field_148116_b == jsonblendingmode.field_148116_b)))));
        }
    }

    public int hashCode() {
        int i = this.field_148116_b;
        i = 31 * i + this.field_148117_c;
        i = 31 * i + this.field_148114_d;
        i = 31 * i + this.field_148115_e;
        i = 31 * i + this.field_148112_f;
        i = 31 * i + (this.field_148113_g ? 1 : 0);
        i = 31 * i + (this.field_148119_h ? 1 : 0);
        return i;
    }

    public boolean func_148111_b() {
        return this.field_148119_h;
    }

    public static JsonBlendingMode func_148110_a(JsonObject p_148110_0_) {
        if (null == p_148110_0_) {
            return new JsonBlendingMode();
        } else {
            int i = 32774;
            int j = 1;
            int k = 0;
            int l = 1;
            int i1 = 0;
            boolean flag = true;
            boolean flag1 = false;

            if (JsonUtils.isString(p_148110_0_, "func")) {
                i = func_148108_a(p_148110_0_.get("func").getAsString());

                if (32774 != i) {
                    flag = false;
                }
            }

            if (JsonUtils.isString(p_148110_0_, "srcrgb")) {
                j = func_148107_b(p_148110_0_.get("srcrgb").getAsString());

                if (1 != j) {
                    flag = false;
                }
            }

            if (JsonUtils.isString(p_148110_0_, "dstrgb")) {
                k = func_148107_b(p_148110_0_.get("dstrgb").getAsString());

                if (0 != k) {
                    flag = false;
                }
            }

            if (JsonUtils.isString(p_148110_0_, "srcalpha")) {
                l = func_148107_b(p_148110_0_.get("srcalpha").getAsString());

                if (1 != l) {
                    flag = false;
                }

                flag1 = true;
            }

            if (JsonUtils.isString(p_148110_0_, "dstalpha")) {
                i1 = func_148107_b(p_148110_0_.get("dstalpha").getAsString());

                if (0 != i1) {
                    flag = false;
                }

                flag1 = true;
            }

            return flag ? new JsonBlendingMode() : (flag1 ? new JsonBlendingMode(j, k, l, i1, i) : new JsonBlendingMode(j, k, i));
        }
    }

    private static int func_148108_a(String p_148108_0_) {
        String s = p_148108_0_.trim().toLowerCase();
        return "add".equals(s) ? 32774 : ("subtract".equals(s) ? 32778 : ("reversesubtract".equals(s) ? 32779 : ("reverse_subtract".equals(s) ? 32779 : ("min".equals(s) ? 32775 : ("max".equals(s) ? 32776 : 32774)))));
    }

    private static int func_148107_b(String p_148107_0_) {
        String s = p_148107_0_.trim().toLowerCase();
        s = s.replaceAll("_", "");
        s = s.replaceAll("one", "1");
        s = s.replaceAll("zero", "0");
        s = s.replaceAll("minus", "-");
        return "0".equals(s) ? 0 : ("1".equals(s) ? 1 : ("srccolor".equals(s) ? 768 : ("1-srccolor".equals(s) ? 769 : ("dstcolor".equals(s) ? 774 : ("1-dstcolor".equals(s) ? 775 : ("srcalpha".equals(s) ? 770 : ("1-srcalpha".equals(s) ? 771 : ("dstalpha".equals(s) ? 772 : ("1-dstalpha".equals(s) ? 773 : -1)))))))));
    }
}