package net.minecraft.entity.ai;

import net.minecraft.entity.passive.EntityAnimal;

import java.util.List;

public class EntityAIFollowParent extends EntityAIBase {
    /**
     * The child that is following its parent.
     */
    EntityAnimal childAnimal;
    EntityAnimal parentAnimal;
    double moveSpeed;
    private int delayCounter;

    public EntityAIFollowParent(EntityAnimal animal, double speed) {
        this.childAnimal = animal;
        this.moveSpeed = speed;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        if (0 <= childAnimal.getGrowingAge()) {
            return false;
        } else {
            List<EntityAnimal> list = this.childAnimal.worldObj.getEntitiesWithinAABB(this.childAnimal.getClass(), this.childAnimal.getEntityBoundingBox().expand(8.0D, 4.0D, 8.0D));
            EntityAnimal entityanimal = null;
            double d0 = Double.MAX_VALUE;

            for (EntityAnimal entityanimal1 : list) {
                if (0 <= entityanimal1.getGrowingAge()) {
                    double d1 = this.childAnimal.getDistanceSqToEntity(entityanimal1);

                    if (d1 <= d0) {
                        d0 = d1;
                        entityanimal = entityanimal1;
                    }
                }
            }

            if (null == entityanimal) {
                return false;
            } else if (9.0D > d0) {
                return false;
            } else {
                this.parentAnimal = entityanimal;
                return true;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting() {
        if (0 <= childAnimal.getGrowingAge()) {
            return false;
        } else if (!this.parentAnimal.isEntityAlive()) {
            return false;
        } else {
            double d0 = this.childAnimal.getDistanceSqToEntity(this.parentAnimal);
            return 9.0D <= d0 && 256.0D >= d0;
        }
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.delayCounter = 0;
    }

    /**
     * Resets the task
     */
    public void resetTask() {
        this.parentAnimal = null;
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        if (0 >= --delayCounter) {
            this.delayCounter = 10;
            this.childAnimal.getNavigator().tryMoveToEntityLiving(this.parentAnimal, this.moveSpeed);
        }
    }
}