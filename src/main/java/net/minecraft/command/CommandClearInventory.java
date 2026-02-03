package net.minecraft.command;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;

public class CommandClearInventory extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "clear";
    }

    /**
     * Gets the usage string for the command.
     */
    public String getCommandUsage(ICommandSender sender) {
        return "commands.clear.usage";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 2;
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        EntityPlayerMP entityplayermp = 0 == args.length ? getCommandSenderAsPlayer(sender) : getPlayer(sender, args[0]);
        Item item = 2 <= args.length ? getItemByText(sender, args[1]) : null;
        int i = 3 <= args.length ? parseInt(args[2], -1) : -1;
        int j = 4 <= args.length ? parseInt(args[3], -1) : -1;
        NBTTagCompound nbttagcompound = null;

        if (5 <= args.length) {
            try {
                nbttagcompound = JsonToNBT.getTagFromJson(buildString(args, 4));
            } catch (NBTException nbtexception) {
                throw new CommandException("commands.clear.tagError", nbtexception.getMessage());
            }
        }

        if (2 <= args.length && null == item) {
            throw new CommandException("commands.clear.failure", entityplayermp.getName());
        } else {
            int k = entityplayermp.inventory.clearMatchingItems(item, i, j, nbttagcompound);
            entityplayermp.inventoryContainer.detectAndSendChanges();

            if (!entityplayermp.capabilities.isCreativeMode) {
                entityplayermp.updateHeldItem();
            }

            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, k);

            if (0 == k) {
                throw new CommandException("commands.clear.failure", entityplayermp.getName());
            } else {
                if (0 == j) {
                    sender.addChatMessage(new ChatComponentTranslation("commands.clear.testing", entityplayermp.getName(), Integer.valueOf(k)));
                } else {
                    notifyOperators(sender, this, "commands.clear.success", entityplayermp.getName(), Integer.valueOf(k));
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 1 == args.length ? getListOfStringsMatchingLastWord(args, this.func_147209_d()) : (2 == args.length ? getListOfStringsMatchingLastWord(args, Item.itemRegistry.getKeys()) : null);
    }

    protected String[] func_147209_d() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] args, int index) {
        return 0 == index;
    }
}