package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigateGround;

public class EntityAITempt extends EntityAIBase {
    /**
     * The entity using this AI that is tempted by the player.
     */
    private final EntityCreature temptedEntity;
    private final double speed;

    /**
     * X position of player tempting this mob
     */
    private double targetX;

    /**
     * Y position of player tempting this mob
     */
    private double targetY;

    /**
     * Z position of player tempting this mob
     */
    private double targetZ;

    /**
     * Tempting player's pitch
     */
    private double pitch;

    /**
     * Tempting player's yaw
     */
    private double yaw;

    /**
     * The player that is tempting the entity that is using this AI.
     */
    private EntityPlayer temptingPlayer;

    /**
     * A counter that is decremented each time the shouldExecute method is called. The shouldExecute method will always
     * return false if delayTemptCounter is greater than 0.
     */
    private int delayTemptCounter;

    /**
     * True if this EntityAITempt task is running
     */
    private boolean isRunning;
    private final Item temptItem;

    /**
     * Whether the entity using this AI will be scared by the tempter's sudden movement.
     */
    private final boolean scaredByPlayerMovement;
    private boolean avoidWater;

    public EntityAITempt(EntityCreature temptedEntityIn, double speedIn, Item temptItemIn, boolean scaredByPlayerMovementIn) {
        this.temptedEntity = temptedEntityIn;
        this.speed = speedIn;
        this.temptItem = temptItemIn;
        this.scaredByPlayerMovement = scaredByPlayerMovementIn;
        this.setMutexBits(3);

        if (!(temptedEntityIn.getNavigator() instanceof PathNavigateGround)) {
            throw new IllegalArgumentException("Unsupported mob type for TemptGoal");
        }
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (0 < delayTemptCounter) {
            --this.delayTemptCounter;
            return false;
        } else {
            this.temptingPlayer = this.temptedEntity.worldObj.getClosestPlayerToEntity(this.temptedEntity, 10.0D);

            if (null == temptingPlayer) {
                return false;
            } else {
                ItemStack itemstack = this.temptingPlayer.getCurrentEquippedItem();
                return null != itemstack && itemstack.getItem() == this.temptItem;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        if (this.scaredByPlayerMovement) {
            if (36.0D > temptedEntity.getDistanceSqToEntity(temptingPlayer)) {
                if (0.010000000000000002D < temptingPlayer.getDistanceSq(targetX, targetY, targetZ)) {
                    return false;
                }

                if (5.0D < Math.abs((double) temptingPlayer.rotationPitch - pitch) || 5.0D < Math.abs((double) temptingPlayer.rotationYaw - yaw)) {
                    return false;
                }
            } else {
                this.targetX = this.temptingPlayer.posX;
                this.targetY = this.temptingPlayer.posY;
                this.targetZ = this.temptingPlayer.posZ;
            }

            this.pitch = this.temptingPlayer.rotationPitch;
            this.yaw = this.temptingPlayer.rotationYaw;
        }

        return this.shouldExecute();
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.targetX = this.temptingPlayer.posX;
        this.targetY = this.temptingPlayer.posY;
        this.targetZ = this.temptingPlayer.posZ;
        this.isRunning = true;
        this.avoidWater = ((PathNavigateGround) this.temptedEntity.getNavigator()).getAvoidsWater();
        ((PathNavigateGround) this.temptedEntity.getNavigator()).setAvoidsWater(false);
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.temptingPlayer = null;
        this.temptedEntity.getNavigator().clearPathEntity();
        this.delayTemptCounter = 100;
        this.isRunning = false;
        ((PathNavigateGround) this.temptedEntity.getNavigator()).setAvoidsWater(this.avoidWater);
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        this.temptedEntity.getLookHelper().setLookPositionWithEntity(this.temptingPlayer, 30.0F, (float) this.temptedEntity.getVerticalFaceSpeed());

        if (6.25D > temptedEntity.getDistanceSqToEntity(temptingPlayer)) {
            this.temptedEntity.getNavigator().clearPathEntity();
        } else {
            this.temptedEntity.getNavigator().tryMoveToEntityLiving(this.temptingPlayer, this.speed);
        }
    }

    /**
     * @see #isRunning
     */
    public boolean isRunning() {
        return this.isRunning;
    }
}