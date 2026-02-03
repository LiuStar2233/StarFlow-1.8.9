package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

public class EntityAIRunAroundLikeCrazy extends EntityAIBase {
    private final EntityHorse horseHost;
    private final double speed;
    private double targetX;
    private double targetY;
    private double targetZ;

    public EntityAIRunAroundLikeCrazy(EntityHorse horse, double speedIn) {
        this.horseHost = horse;
        this.speed = speedIn;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (!this.horseHost.isTame() && null != horseHost.riddenByEntity) {
            Vec3 vec3 = RandomPositionGenerator.findRandomTarget(this.horseHost, 5, 4);

            if (null == vec3) {
                return false;
            } else {
                this.targetX = vec3.xCoord;
                this.targetY = vec3.yCoord;
                this.targetZ = vec3.zCoord;
                return true;
            }
        } else {
            return false;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.horseHost.getNavigator().tryMoveToXYZ(this.targetX, this.targetY, this.targetZ, this.speed);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return !this.horseHost.getNavigator().noPath() && null != horseHost.riddenByEntity;
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        if (0 == horseHost.getRNG().nextInt(50)) {
            if (this.horseHost.riddenByEntity instanceof EntityPlayer) {
                int i = this.horseHost.getTemper();
                int j = this.horseHost.getMaxTemper();

                if (0 < j && this.horseHost.getRNG().nextInt(j) < i) {
                    this.horseHost.setTamedBy((EntityPlayer) this.horseHost.riddenByEntity);
                    this.horseHost.worldObj.setEntityState(this.horseHost, (byte) 7);
                    return;
                }

                this.horseHost.increaseTemper(5);
            }

            this.horseHost.riddenByEntity.mountEntity(null);
            this.horseHost.riddenByEntity = null;
            this.horseHost.makeHorseRearWithSound();
            this.horseHost.worldObj.setEntityState(this.horseHost, (byte) 6);
        }
    }
}