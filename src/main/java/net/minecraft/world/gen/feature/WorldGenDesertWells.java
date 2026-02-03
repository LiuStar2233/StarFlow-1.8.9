package net.minecraft.world.gen.feature;

import com.google.common.base.Predicates;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockStateHelper;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenDesertWells extends WorldGenerator {
    private static final BlockStateHelper field_175913_a = BlockStateHelper.forBlock(Blocks.sand).where(BlockSand.VARIANT, Predicates.equalTo(BlockSand.EnumType.SAND));
    private final IBlockState field_175911_b = Blocks.stone_slab.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.SAND).withProperty(BlockSlab.HALF, BlockSlab.EnumBlockHalf.BOTTOM);
    private final IBlockState field_175912_c = Blocks.sandstone.getDefaultState();
    private final IBlockState field_175910_d = Blocks.flowing_water.getDefaultState();

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        while (worldIn.isAirBlock(position) && 2 < position.getY()) {
            position = position.down();
        }

        if (!field_175913_a.apply(worldIn.getBlockState(position))) {
            return false;
        } else {
            for (int i = -2; 2 >= i; ++i) {
                for (int j = -2; 2 >= j; ++j) {
                    if (worldIn.isAirBlock(position.add(i, -1, j)) && worldIn.isAirBlock(position.add(i, -2, j))) {
                        return false;
                    }
                }
            }

            for (int l = -1; 0 >= l; ++l) {
                for (int l1 = -2; 2 >= l1; ++l1) {
                    for (int k = -2; 2 >= k; ++k) {
                        worldIn.setBlockState(position.add(l1, l, k), this.field_175912_c, 2);
                    }
                }
            }

            worldIn.setBlockState(position, this.field_175910_d, 2);

            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                worldIn.setBlockState(position.offset(enumfacing), this.field_175910_d, 2);
            }

            for (int i1 = -2; 2 >= i1; ++i1) {
                for (int i2 = -2; 2 >= i2; ++i2) {
                    if (-2 == i1 || 2 == i1 || -2 == i2 || 2 == i2) {
                        worldIn.setBlockState(position.add(i1, 1, i2), this.field_175912_c, 2);
                    }
                }
            }

            worldIn.setBlockState(position.add(2, 1, 0), this.field_175911_b, 2);
            worldIn.setBlockState(position.add(-2, 1, 0), this.field_175911_b, 2);
            worldIn.setBlockState(position.add(0, 1, 2), this.field_175911_b, 2);
            worldIn.setBlockState(position.add(0, 1, -2), this.field_175911_b, 2);

            for (int j1 = -1; 1 >= j1; ++j1) {
                for (int j2 = -1; 1 >= j2; ++j2) {
                    if (0 == j1 && 0 == j2) {
                        worldIn.setBlockState(position.add(j1, 4, j2), this.field_175912_c, 2);
                    } else {
                        worldIn.setBlockState(position.add(j1, 4, j2), this.field_175911_b, 2);
                    }
                }
            }

            for (int k1 = 1; 3 >= k1; ++k1) {
                worldIn.setBlockState(position.add(-1, k1, -1), this.field_175912_c, 2);
                worldIn.setBlockState(position.add(-1, k1, 1), this.field_175912_c, 2);
                worldIn.setBlockState(position.add(1, k1, -1), this.field_175912_c, 2);
                worldIn.setBlockState(position.add(1, k1, 1), this.field_175912_c, 2);
            }

            return true;
        }
    }
}