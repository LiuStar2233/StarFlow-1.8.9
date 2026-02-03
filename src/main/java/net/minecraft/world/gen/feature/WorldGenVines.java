package net.minecraft.world.gen.feature;

import net.minecraft.block.BlockVine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenVines extends WorldGenerator {
    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (; 128 > position.getY(); position = position.up()) {
            if (worldIn.isAirBlock(position)) {
                for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL.facings()) {
                    if (Blocks.vine.canPlaceBlockOnSide(worldIn, position, enumfacing)) {
                        IBlockState iblockstate = Blocks.vine.getDefaultState().withProperty(BlockVine.NORTH, Boolean.valueOf(EnumFacing.NORTH == enumfacing)).withProperty(BlockVine.EAST, Boolean.valueOf(EnumFacing.EAST == enumfacing)).withProperty(BlockVine.SOUTH, Boolean.valueOf(EnumFacing.SOUTH == enumfacing)).withProperty(BlockVine.WEST, Boolean.valueOf(EnumFacing.WEST == enumfacing));
                        worldIn.setBlockState(position, iblockstate, 2);
                        break;
                    }
                }
            } else {
                position = position.add(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4));
            }
        }

        return true;
    }
}