package net.minecraft.world.gen.structure;

import net.minecraft.util.MathHelper;

import java.util.Map;

public class MapGenMineshaft extends MapGenStructure {
    private double field_82673_e = 0.004D;

    public MapGenMineshaft() {
    }

    public String getStructureName() {
        return "Mineshaft";
    }

    public MapGenMineshaft(Map<String, String> p_i2034_1_) {
        for (Map.Entry<String, String> entry : p_i2034_1_.entrySet()) {
            if ("chance".equals(entry.getKey())) {
                this.field_82673_e = MathHelper.parseDoubleWithDefault(entry.getValue(), this.field_82673_e);
            }
        }
    }

    protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
        return this.rand.nextDouble() < this.field_82673_e && this.rand.nextInt(80) < Math.max(Math.abs(chunkX), Math.abs(chunkZ));
    }

    protected StructureStart getStructureStart(int chunkX, int chunkZ) {
        return new StructureMineshaftStart(this.worldObj, this.rand, chunkX, chunkZ);
    }
}