package net.minecraft.world.biome;

import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockFlower;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenBigMushroom;
import net.minecraft.world.gen.feature.WorldGenCanopyTree;
import net.minecraft.world.gen.feature.WorldGenForest;

import java.util.Random;

public class BiomeGenForest extends BiomeGenBase {
    private final int field_150632_aF;
    protected static final WorldGenForest field_150629_aC = new WorldGenForest(false, true);
    protected static final WorldGenForest field_150630_aD = new WorldGenForest(false, false);
    protected static final WorldGenCanopyTree field_150631_aE = new WorldGenCanopyTree(false);

    public BiomeGenForest(int id, int p_i45377_2_) {
        super(id);
        this.field_150632_aF = p_i45377_2_;
        this.theBiomeDecorator.treesPerChunk = 10;
        this.theBiomeDecorator.grassPerChunk = 2;

        if (1 == field_150632_aF) {
            this.theBiomeDecorator.treesPerChunk = 6;
            this.theBiomeDecorator.flowersPerChunk = 100;
            this.theBiomeDecorator.grassPerChunk = 1;
        }

        this.setFillerBlockMetadata(5159473);
        this.setTemperatureRainfall(0.7F, 0.8F);

        if (2 == field_150632_aF) {
            this.field_150609_ah = 353825;
            this.color = 3175492;
            this.setTemperatureRainfall(0.6F, 0.6F);
        }

        if (0 == field_150632_aF) {
            this.spawnableCreatureList.add(new BiomeGenBase.SpawnListEntry(EntityWolf.class, 5, 4, 4));
        }

        if (3 == field_150632_aF) {
            this.theBiomeDecorator.treesPerChunk = -999;
        }
    }

    protected BiomeGenBase func_150557_a(int colorIn, boolean p_150557_2_) {
        if (2 == field_150632_aF) {
            this.field_150609_ah = 353825;
            this.color = colorIn;

            if (p_150557_2_) {
                this.field_150609_ah = (this.field_150609_ah & 16711422) >> 1;
            }

            return this;
        } else {
            return super.func_150557_a(colorIn, p_150557_2_);
        }
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand) {
        return 3 == field_150632_aF && 0 < rand.nextInt(3) ? field_150631_aE : (2 != field_150632_aF && 0 != rand.nextInt(5) ? this.worldGeneratorTrees : field_150630_aD);
    }

    public BlockFlower.EnumFlowerType pickRandomFlower(Random rand, BlockPos pos) {
        if (1 == field_150632_aF) {
            double d0 = MathHelper.clamp_double((1.0D + GRASS_COLOR_NOISE.func_151601_a((double) pos.getX() / 48.0D, (double) pos.getZ() / 48.0D)) / 2.0D, 0.0D, 0.9999D);
            BlockFlower.EnumFlowerType blockflower$enumflowertype = BlockFlower.EnumFlowerType.values()[(int) (d0 * (double) BlockFlower.EnumFlowerType.values().length)];
            return BlockFlower.EnumFlowerType.BLUE_ORCHID == blockflower$enumflowertype ? BlockFlower.EnumFlowerType.POPPY : blockflower$enumflowertype;
        } else {
            return super.pickRandomFlower(rand, pos);
        }
    }

    public void decorate(World worldIn, Random rand, BlockPos pos) {
        if (3 == field_150632_aF) {
            for (int i = 0; 4 > i; ++i) {
                for (int j = 0; 4 > j; ++j) {
                    int k = i * 4 + 1 + 8 + rand.nextInt(3);
                    int l = j * 4 + 1 + 8 + rand.nextInt(3);
                    BlockPos blockpos = worldIn.getHeight(pos.add(k, 0, l));

                    if (0 == rand.nextInt(20)) {
                        WorldGenBigMushroom worldgenbigmushroom = new WorldGenBigMushroom();
                        worldgenbigmushroom.generate(worldIn, rand, blockpos);
                    } else {
                        WorldGenAbstractTree worldgenabstracttree = this.genBigTreeChance(rand);
                        worldgenabstracttree.func_175904_e();

                        if (worldgenabstracttree.generate(worldIn, rand, blockpos)) {
                            worldgenabstracttree.func_180711_a(worldIn, rand, blockpos);
                        }
                    }
                }
            }
        }

        int j1 = rand.nextInt(5) - 3;

        if (1 == field_150632_aF) {
            j1 += 2;
        }

        for (int k1 = 0; k1 < j1; ++k1) {
            int l1 = rand.nextInt(3);

            if (0 == l1) {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.SYRINGA);
            } else if (1 == l1) {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.ROSE);
            } else if (2 == l1) {
                DOUBLE_PLANT_GENERATOR.setPlantType(BlockDoublePlant.EnumPlantType.PAEONIA);
            }

            for (int i2 = 0; 5 > i2; ++i2) {
                int j2 = rand.nextInt(16) + 8;
                int k2 = rand.nextInt(16) + 8;
                int i1 = rand.nextInt(worldIn.getHeight(pos.add(j2, 0, k2)).getY() + 32);

                if (DOUBLE_PLANT_GENERATOR.generate(worldIn, rand, new BlockPos(pos.getX() + j2, i1, pos.getZ() + k2))) {
                    break;
                }
            }
        }

        super.decorate(worldIn, rand, pos);
    }

    public int getGrassColorAtPos(BlockPos pos) {
        int i = super.getGrassColorAtPos(pos);
        return 3 == field_150632_aF ? (i & 16711422) + 2634762 >> 1 : i;
    }

    protected BiomeGenBase createMutatedBiome(final int p_180277_1_) {
        if (this.biomeID == BiomeGenBase.forest.biomeID) {
            BiomeGenForest biomegenforest = new BiomeGenForest(p_180277_1_, 1);
            biomegenforest.setHeight(new BiomeGenBase.Height(this.minHeight, this.maxHeight + 0.2F));
            biomegenforest.setBiomeName("Flower Forest");
            biomegenforest.func_150557_a(6976549, true);
            biomegenforest.setFillerBlockMetadata(8233509);
            return biomegenforest;
        } else {
            return this.biomeID != BiomeGenBase.birchForest.biomeID && this.biomeID != BiomeGenBase.birchForestHills.biomeID ? new BiomeGenMutated(p_180277_1_, this) {
                public void decorate(World worldIn, Random rand, BlockPos pos) {
                    this.baseBiome.decorate(worldIn, rand, pos);
                }
            } : new BiomeGenMutated(p_180277_1_, this) {
                public WorldGenAbstractTree genBigTreeChance(Random rand) {
                    return rand.nextBoolean() ? BiomeGenForest.field_150629_aC : BiomeGenForest.field_150630_aD;
                }
            };
        }
    }
}