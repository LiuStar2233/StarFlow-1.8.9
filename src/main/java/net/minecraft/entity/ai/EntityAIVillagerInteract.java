package net.minecraft.entity.ai;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;

public class EntityAIVillagerInteract extends EntityAIWatchClosest2 {
    /**
     * The delay before the villager throws an itemstack (in ticks)
     */
    private int interactionDelay;
    private final EntityVillager villager;

    public EntityAIVillagerInteract(EntityVillager villagerIn) {
        super(villagerIn, EntityVillager.class, 3.0F, 0.02F);
        this.villager = villagerIn;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        super.startExecuting();

        if (this.villager.canAbondonItems() && this.closestEntity instanceof EntityVillager && ((EntityVillager) this.closestEntity).func_175557_cr()) {
            this.interactionDelay = 10;
        } else {
            this.interactionDelay = 0;
        }
    }

    /**
     * Updates the task
     */
    public void updateTask() {
        super.updateTask();

        if (0 < interactionDelay) {
            --this.interactionDelay;

            if (0 == interactionDelay) {
                InventoryBasic inventorybasic = this.villager.getVillagerInventory();

                for (int i = 0; i < inventorybasic.getSizeInventory(); ++i) {
                    ItemStack itemstack = inventorybasic.getStackInSlot(i);
                    ItemStack itemstack1 = null;

                    if (null != itemstack) {
                        Item item = itemstack.getItem();

                        if ((item == Items.bread || item == Items.potato || item == Items.carrot) && 3 < itemstack.stackSize) {
                            int l = itemstack.stackSize / 2;
                            itemstack.stackSize -= l;
                            itemstack1 = new ItemStack(item, l, itemstack.getMetadata());
                        } else if (item == Items.wheat && 5 < itemstack.stackSize) {
                            int j = itemstack.stackSize / 2 / 3 * 3;
                            int k = j / 3;
                            itemstack.stackSize -= j;
                            itemstack1 = new ItemStack(Items.bread, k, 0);
                        }

                        if (0 >= itemstack.stackSize) {
                            inventorybasic.setInventorySlotContents(i, null);
                        }
                    }

                    if (null != itemstack1) {
                        double d0 = this.villager.posY - 0.30000001192092896D + (double) this.villager.getEyeHeight();
                        EntityItem entityitem = new EntityItem(this.villager.worldObj, this.villager.posX, d0, this.villager.posZ, itemstack1);
                        final float f = 0.3F;
                        float f1 = this.villager.rotationYawHead;
                        float f2 = this.villager.rotationPitch;
                        entityitem.motionX = -MathHelper.sin(f1 / 180.0F * (float) Math.PI) * MathHelper.cos(f2 / 180.0F * (float) Math.PI) * f;
                        entityitem.motionZ = MathHelper.cos(f1 / 180.0F * (float) Math.PI) * MathHelper.cos(f2 / 180.0F * (float) Math.PI) * f;
                        entityitem.motionY = -MathHelper.sin(f2 / 180.0F * (float) Math.PI) * f + 0.1F;
                        entityitem.setDefaultPickupDelay();
                        this.villager.worldObj.spawnEntityInWorld(entityitem);
                        break;
                    }
                }
            }
        }
    }
}