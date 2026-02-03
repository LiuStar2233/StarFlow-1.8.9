package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
import net.minecraft.village.Village;
import net.minecraft.village.VillageDoorInfo;

public class EntityAIMoveIndoors extends EntityAIBase {
    private final EntityCreature entityObj;
    private VillageDoorInfo doorInfo;
    private int insidePosX = -1;
    private int insidePosZ = -1;

    public EntityAIMoveIndoors(EntityCreature entityObjIn) {
        this.entityObj = entityObjIn;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        BlockPos blockpos = new BlockPos(this.entityObj);

        if ((!this.entityObj.worldObj.isDaytime() || this.entityObj.worldObj.isRaining() && !this.entityObj.worldObj.getBiomeGenForCoords(blockpos).canRain()) && !this.entityObj.worldObj.provider.getHasNoSky()) {
            if (0 != entityObj.getRNG().nextInt(50)) {
                return false;
            } else if (-1 != insidePosX && 4.0D > entityObj.getDistanceSq(insidePosX, entityObj.posY, insidePosZ)) {
                return false;
            } else {
                Village village = this.entityObj.worldObj.getVillageCollection().getNearestVillage(blockpos, 14);

                if (null == village) {
                    return false;
                } else {
                    this.doorInfo = village.getDoorInfo(blockpos);
                    return null != doorInfo;
                }
            }
        } else {
            return false;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return !this.entityObj.getNavigator().noPath();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.insidePosX = -1;
        BlockPos blockpos = this.doorInfo.getInsideBlockPos();
        int i = blockpos.getX();
        int j = blockpos.getY();
        int k = blockpos.getZ();

        if (256.0D < entityObj.getDistanceSq(blockpos)) {
            Vec3 vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(this.entityObj, 14, 3, new Vec3((double) i + 0.5D, j, (double) k + 0.5D));

            if (null != vec3) {
                this.entityObj.getNavigator().tryMoveToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord, 1.0D);
            }
        } else {
            this.entityObj.getNavigator().tryMoveToXYZ((double) i + 0.5D, j, (double) k + 0.5D, 1.0D);
        }
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.insidePosX = this.doorInfo.getInsideBlockPos().getX();
        this.insidePosZ = this.doorInfo.getInsideBlockPos().getZ();
        this.doorInfo = null;
    }
}