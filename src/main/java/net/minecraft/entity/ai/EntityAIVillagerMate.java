package net.minecraft.entity.ai;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.BlockPos;
import net.minecraft.village.Village;
import net.minecraft.world.World;

public class EntityAIVillagerMate extends EntityAIBase {
    private final EntityVillager villagerObj;
    private EntityVillager mate;
    private final World worldObj;
    private int matingTimeout;
    Village villageObj;

    public EntityAIVillagerMate(EntityVillager villagerIn) {
        this.villagerObj = villagerIn;
        this.worldObj = villagerIn.worldObj;
        this.setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (0 != villagerObj.getGrowingAge()) {
            return false;
        } else if (0 != villagerObj.getRNG().nextInt(500)) {
            return false;
        } else {
            this.villageObj = this.worldObj.getVillageCollection().getNearestVillage(new BlockPos(this.villagerObj), 0);

            if (null == villageObj) {
                return false;
            } else if (this.checkSufficientDoorsPresentForNewVillager() && this.villagerObj.getIsWillingToMate(true)) {
                Entity entity = this.worldObj.findNearestEntityWithinAABB(EntityVillager.class, this.villagerObj.getEntityBoundingBox().expand(8.0D, 3.0D, 8.0D), this.villagerObj);

                if (null == entity) {
                    return false;
                } else {
                    this.mate = (EntityVillager) entity;
                    return 0 == mate.getGrowingAge() && this.mate.getIsWillingToMate(true);
                }
            } else {
                return false;
            }
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.matingTimeout = 300;
        this.villagerObj.setMating(true);
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.villageObj = null;
        this.mate = null;
        this.villagerObj.setMating(false);
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        return 0 <= matingTimeout && this.checkSufficientDoorsPresentForNewVillager() && 0 == villagerObj.getGrowingAge() && this.villagerObj.getIsWillingToMate(false);
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        --this.matingTimeout;
        this.villagerObj.getLookHelper().setLookPositionWithEntity(this.mate, 10.0F, 30.0F);

        if (2.25D < villagerObj.getDistanceSqToEntity(mate)) {
            this.villagerObj.getNavigator().tryMoveToEntityLiving(this.mate, 0.25D);
        } else if (0 == matingTimeout && this.mate.isMating()) {
            this.giveBirth();
        }

        if (0 == villagerObj.getRNG().nextInt(35)) {
            this.worldObj.setEntityState(this.villagerObj, (byte) 12);
        }
    }

    private boolean checkSufficientDoorsPresentForNewVillager() {
        if (!this.villageObj.isMatingSeason()) {
            return false;
        } else {
            int i = (int) ((double) ((float) this.villageObj.getNumVillageDoors()) * 0.35D);
            return this.villageObj.getNumVillagers() < i;
        }
    }

    private void giveBirth() {
        EntityVillager entityvillager = this.villagerObj.createChild(this.mate);
        this.mate.setGrowingAge(6000);
        this.villagerObj.setGrowingAge(6000);
        this.mate.setIsWillingToMate(false);
        this.villagerObj.setIsWillingToMate(false);
        entityvillager.setGrowingAge(-24000);
        entityvillager.setLocationAndAngles(this.villagerObj.posX, this.villagerObj.posY, this.villagerObj.posZ, 0.0F, 0.0F);
        this.worldObj.spawnEntityInWorld(entityvillager);
        this.worldObj.setEntityState(entityvillager, (byte) 12);
    }
}