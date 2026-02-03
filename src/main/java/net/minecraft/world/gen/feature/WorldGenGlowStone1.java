package net.minecraft.world.gen.feature;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenGlowStone1 extends WorldGenerator {
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        if (!worldIn.isAirBlock(position)) {
            return false;
        } else if (worldIn.getBlockState(position.up()).getBlock() != Blocks.netherrack) {
            return false;
        } else {
            worldIn.setBlockState(position, Blocks.glowstone.getDefaultState(), 2);

            for (int i = 0; 1500 > i; ++i) {
                BlockPos blockpos = position.add(rand.nextInt(8) - rand.nextInt(8), -rand.nextInt(12), rand.nextInt(8) - rand.nextInt(8));

                if (worldIn.getBlockState(blockpos).getBlock().getMaterial() == Material.air) {
                    int j = 0;

                    for (EnumFacing enumfacing : EnumFacing.values()) {
                        if (worldIn.getBlockState(blockpos.offset(enumfacing)).getBlock() == Blocks.glowstone) {
                            ++j;
                        }

                        if (1 < j) {
                            break;
                        }
                    }

                    if (1 == j) {
                        worldIn.setBlockState(blockpos, Blocks.glowstone.getDefaultState(), 2);
                    }
                }
            }

            return true;
        }
    }
}