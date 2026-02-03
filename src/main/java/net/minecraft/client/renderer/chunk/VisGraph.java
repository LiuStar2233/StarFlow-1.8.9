package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntegerCache;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.Queue;
import java.util.Set;

public class VisGraph {
    private static final int field_178616_a = (int) Math.pow(16.0D, 0.0D);
    private static final int field_178614_b = (int) Math.pow(16.0D, 1.0D);
    private static final int field_178615_c = (int) Math.pow(16.0D, 2.0D);
    private final BitSet field_178612_d = new BitSet(4096);
    private static final int[] field_178613_e = new int[1352];
    private int field_178611_f = 4096;

    public void func_178606_a(BlockPos pos) {
        this.field_178612_d.set(getIndex(pos), true);
        --this.field_178611_f;
    }

    private static int getIndex(BlockPos pos) {
        return getIndex(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
    }

    private static int getIndex(int x, int y, int z) {
        return x << 0 | y << 8 | z << 4;
    }

    public SetVisibility computeVisibility() {
        SetVisibility setvisibility = new SetVisibility();

        if (256 > 4096 - field_178611_f) {
            setvisibility.setAllVisible(true);
        } else if (0 == field_178611_f) {
            setvisibility.setAllVisible(false);
        } else {
            for (int i : field_178613_e) {
                if (!this.field_178612_d.get(i)) {
                    setvisibility.setManyVisible(this.func_178604_a(i));
                }
            }
        }

        return setvisibility;
    }

    public Set<EnumFacing> func_178609_b(BlockPos pos) {
        return this.func_178604_a(getIndex(pos));
    }

    private Set<EnumFacing> func_178604_a(int p_178604_1_) {
        Set<EnumFacing> set = EnumSet.noneOf(EnumFacing.class);
        Queue<Integer> queue = Lists.newLinkedList();
        queue.add(IntegerCache.getInteger(p_178604_1_));
        this.field_178612_d.set(p_178604_1_, true);

        while (!queue.isEmpty()) {
            int i = queue.poll().intValue();
            this.func_178610_a(i, set);

            for (EnumFacing enumfacing : EnumFacing.values()) {
                int j = this.func_178603_a(i, enumfacing);

                if (0 <= j && !this.field_178612_d.get(j)) {
                    this.field_178612_d.set(j, true);
                    queue.add(IntegerCache.getInteger(j));
                }
            }
        }

        return set;
    }

    private void func_178610_a(int p_178610_1_, Set<EnumFacing> p_178610_2_) {
        int i = p_178610_1_ >> 0 & 15;

        if (0 == i) {
            p_178610_2_.add(EnumFacing.WEST);
        } else if (15 == i) {
            p_178610_2_.add(EnumFacing.EAST);
        }

        int j = p_178610_1_ >> 8 & 15;

        if (0 == j) {
            p_178610_2_.add(EnumFacing.DOWN);
        } else if (15 == j) {
            p_178610_2_.add(EnumFacing.UP);
        }

        int k = p_178610_1_ >> 4 & 15;

        if (0 == k) {
            p_178610_2_.add(EnumFacing.NORTH);
        } else if (15 == k) {
            p_178610_2_.add(EnumFacing.SOUTH);
        }
    }

    private int func_178603_a(int p_178603_1_, EnumFacing p_178603_2_) {
        switch (p_178603_2_) {
            case DOWN:
                if (0 == (p_178603_1_ >> 8 & 15)) {
                    return -1;
                }

                return p_178603_1_ - field_178615_c;

            case UP:
                if (15 == (p_178603_1_ >> 8 & 15)) {
                    return -1;
                }

                return p_178603_1_ + field_178615_c;

            case NORTH:
                if (0 == (p_178603_1_ >> 4 & 15)) {
                    return -1;
                }

                return p_178603_1_ - field_178614_b;

            case SOUTH:
                if (15 == (p_178603_1_ >> 4 & 15)) {
                    return -1;
                }

                return p_178603_1_ + field_178614_b;

            case WEST:
                if (0 == (p_178603_1_ >> 0 & 15)) {
                    return -1;
                }

                return p_178603_1_ - field_178616_a;

            case EAST:
                if (15 == (p_178603_1_ >> 0 & 15)) {
                    return -1;
                }

                return p_178603_1_ + field_178616_a;

            default:
                return -1;
        }
    }

    static {
        final int i = 0;
        final int j = 15;
        int k = 0;

        for (int l = 0; 16 > l; ++l) {
            for (int i1 = 0; 16 > i1; ++i1) {
                for (int j1 = 0; 16 > j1; ++j1) {
                    if (0 == l || 15 == l || 0 == i1 || 15 == i1 || 0 == j1 || 15 == j1) {
                        field_178613_e[k++] = getIndex(l, i1, j1);
                    }
                }
            }
        }
    }
}