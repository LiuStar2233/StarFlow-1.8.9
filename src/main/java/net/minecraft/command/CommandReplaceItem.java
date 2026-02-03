package net.minecraft.command;

import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public class CommandReplaceItem extends CommandBase {
    private static final Map<String, Integer> SHORTCUTS = Maps.newHashMap();

    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "replaceitem";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     * Gets the usage string for the command.
     */
    public String getCommandUsage(ICommandSender sender) {
        return "commands.replaceitem.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (1 > args.length) {
            throw new WrongUsageException("commands.replaceitem.usage");
        } else {
            boolean flag;

            if ("entity".equals(args[0])) {
                flag = false;
            } else {
                if (!"block".equals(args[0])) {
                    throw new WrongUsageException("commands.replaceitem.usage");
                }

                flag = true;
            }

            int i;

            if (flag) {
                if (6 > args.length) {
                    throw new WrongUsageException("commands.replaceitem.block.usage");
                }

                i = 4;
            } else {
                if (4 > args.length) {
                    throw new WrongUsageException("commands.replaceitem.entity.usage");
                }

                i = 2;
            }

            int j = this.getSlotForShortcut(args[i++]);
            Item item;

            try {
                item = getItemByText(sender, args[i]);
            } catch (NumberInvalidException numberinvalidexception) {
                if (Block.getBlockFromName(args[i]) != Blocks.air) {
                    throw numberinvalidexception;
                }

                item = null;
            }

            ++i;
            int k = args.length > i ? parseInt(args[i++], 1, 64) : 1;
            int l = args.length > i ? parseInt(args[i++]) : 0;
            ItemStack itemstack = new ItemStack(item, k, l);

            if (args.length > i) {
                String s = getChatComponentFromNthArg(sender, args, i).getUnformattedText();

                try {
                    itemstack.setTagCompound(JsonToNBT.getTagFromJson(s));
                } catch (NBTException nbtexception) {
                    throw new CommandException("commands.replaceitem.tagError", nbtexception.getMessage());
                }
            }

            if (null == itemstack.getItem()) {
                itemstack = null;
            }

            if (flag) {
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);
                BlockPos blockpos = parseBlockPos(sender, args, 1, false);
                World world = sender.getEntityWorld();
                TileEntity tileentity = world.getTileEntity(blockpos);

                if (null == tileentity || !(tileentity instanceof IInventory)) {
                    throw new CommandException("commands.replaceitem.noContainer", Integer.valueOf(blockpos.getX()), Integer.valueOf(blockpos.getY()), Integer.valueOf(blockpos.getZ()));
                }

                IInventory iinventory = (IInventory) tileentity;

                if (0 <= j && j < iinventory.getSizeInventory()) {
                    iinventory.setInventorySlotContents(j, itemstack);
                }
            } else {
                Entity entity = getEntity(sender, args[1]);
                sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);

                if (entity instanceof EntityPlayer) {
                    ((EntityPlayer) entity).inventoryContainer.detectAndSendChanges();
                }

                if (!entity.replaceItemInInventory(j, itemstack)) {
                    throw new CommandException("commands.replaceitem.failed", Integer.valueOf(j), Integer.valueOf(k), null == itemstack ? "Air" : itemstack.getChatComponent());
                }

                if (entity instanceof EntityPlayer) {
                    ((EntityPlayer) entity).inventoryContainer.detectAndSendChanges();
                }
            }

            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, k);
            notifyOperators(sender, this, "commands.replaceitem.success", Integer.valueOf(j), Integer.valueOf(k), null == itemstack ? "Air" : itemstack.getChatComponent());
        }
    }

    private int getSlotForShortcut(String shortcut) throws CommandException {
        if (!SHORTCUTS.containsKey(shortcut)) {
            throw new CommandException("commands.generic.parameter.invalid", shortcut);
        } else {
            return SHORTCUTS.get(shortcut).intValue();
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 1 == args.length ? getListOfStringsMatchingLastWord(args, "entity", "block") : (2 == args.length && "entity".equals(args[0]) ? getListOfStringsMatchingLastWord(args, this.getUsernames()) : (2 <= args.length && 4 >= args.length && "block".equals(args[0]) ? func_175771_a(args, 1, pos) : ((3 != args.length || !"entity".equals(args[0])) && (5 != args.length || !"block".equals(args[0])) ? ((4 != args.length || !"entity".equals(args[0])) && (6 != args.length || !"block".equals(args[0])) ? null : getListOfStringsMatchingLastWord(args, Item.itemRegistry.getKeys())) : getListOfStringsMatchingLastWord(args, SHORTCUTS.keySet()))));
    }

    protected String[] getUsernames() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] args, int index) {
        return 0 < args.length && "entity".equals(args[0]) && 1 == index;
    }

    static {
        for (int i = 0; 54 > i; ++i) {
            SHORTCUTS.put("slot.container." + i, Integer.valueOf(i));
        }

        for (int j = 0; 9 > j; ++j) {
            SHORTCUTS.put("slot.hotbar." + j, Integer.valueOf(j));
        }

        for (int k = 0; 27 > k; ++k) {
            SHORTCUTS.put("slot.inventory." + k, Integer.valueOf(9 + k));
        }

        for (int l = 0; 27 > l; ++l) {
            SHORTCUTS.put("slot.enderchest." + l, Integer.valueOf(200 + l));
        }

        for (int i1 = 0; 8 > i1; ++i1) {
            SHORTCUTS.put("slot.villager." + i1, Integer.valueOf(300 + i1));
        }

        for (int j1 = 0; 15 > j1; ++j1) {
            SHORTCUTS.put("slot.horse." + j1, Integer.valueOf(500 + j1));
        }

        SHORTCUTS.put("slot.weapon", Integer.valueOf(99));
        SHORTCUTS.put("slot.armor.head", Integer.valueOf(103));
        SHORTCUTS.put("slot.armor.chest", Integer.valueOf(102));
        SHORTCUTS.put("slot.armor.legs", Integer.valueOf(101));
        SHORTCUTS.put("slot.armor.feet", Integer.valueOf(100));
        SHORTCUTS.put("slot.horse.saddle", Integer.valueOf(400));
        SHORTCUTS.put("slot.horse.armor", Integer.valueOf(401));
        SHORTCUTS.put("slot.horse.chest", Integer.valueOf(499));
    }
}