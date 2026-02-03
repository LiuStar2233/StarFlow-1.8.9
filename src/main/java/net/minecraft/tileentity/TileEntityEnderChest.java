package net.minecraft.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.ITickable;

public class TileEntityEnderChest extends TileEntity implements ITickable {
    public float lidAngle;

    /**
     * The angle of the ender chest lid last tick
     */
    public float prevLidAngle;
    public int numPlayersUsing;
    private int ticksSinceSync;

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update() {
        if (0 == ++ticksSinceSync % 20 * 4) {
            this.worldObj.addBlockEvent(this.pos, Blocks.ender_chest, 1, this.numPlayersUsing);
        }

        this.prevLidAngle = this.lidAngle;
        int i = this.pos.getX();
        int j = this.pos.getY();
        int k = this.pos.getZ();
        final float f = 0.1F;

        if (0 < numPlayersUsing && 0.0F == lidAngle) {
            double d0 = (double) i + 0.5D;
            double d1 = (double) k + 0.5D;
            this.worldObj.playSoundEffect(d0, (double) j + 0.5D, d1, "random.chestopen", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (0 == numPlayersUsing && 0.0F < lidAngle || 0 < numPlayersUsing && 1.0F > lidAngle) {
            float f2 = this.lidAngle;

            if (0 < numPlayersUsing) {
                this.lidAngle += f;
            } else {
                this.lidAngle -= f;
            }

            if (1.0F < lidAngle) {
                this.lidAngle = 1.0F;
            }

            final float f1 = 0.5F;

            if (f1 > lidAngle && f1 <= f2) {
                double d3 = (double) i + 0.5D;
                double d2 = (double) k + 0.5D;
                this.worldObj.playSoundEffect(d3, (double) j + 0.5D, d2, "random.chestclosed", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (0.0F > lidAngle) {
                this.lidAngle = 0.0F;
            }
        }
    }

    public boolean receiveClientEvent(int id, int type) {
        if (1 == id) {
            this.numPlayersUsing = type;
            return true;
        } else {
            return super.receiveClientEvent(id, type);
        }
    }

    /**
     * invalidates a tile entity
     */
    public void invalidate() {
        this.updateContainingBlockInfo();
        super.invalidate();
    }

    public void openChest() {
        ++this.numPlayersUsing;
        this.worldObj.addBlockEvent(this.pos, Blocks.ender_chest, 1, this.numPlayersUsing);
    }

    public void closeChest() {
        --this.numPlayersUsing;
        this.worldObj.addBlockEvent(this.pos, Blocks.ender_chest, 1, this.numPlayersUsing);
    }

    public boolean canBeUsed(EntityPlayer p_145971_1_) {
        return this.worldObj.getTileEntity(this.pos) == this && 64.0D >= p_145971_1_.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D);
    }
}