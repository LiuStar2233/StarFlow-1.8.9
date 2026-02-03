package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;

import java.util.List;

public class CommandTestFor extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "testfor";
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
        return "commands.testfor.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (1 > args.length) {
            throw new WrongUsageException("commands.testfor.usage");
        } else {
            Entity entity = getEntity(sender, args[0]);
            NBTTagCompound nbttagcompound = null;

            if (2 <= args.length) {
                try {
                    nbttagcompound = JsonToNBT.getTagFromJson(buildString(args, 1));
                } catch (NBTException nbtexception) {
                    throw new CommandException("commands.testfor.tagError", nbtexception.getMessage());
                }
            }

            if (null != nbttagcompound) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                entity.writeToNBT(nbttagcompound1);

                if (!NBTUtil.func_181123_a(nbttagcompound, nbttagcompound1, true)) {
                    throw new CommandException("commands.testfor.failure", entity.getName());
                }
            }

            notifyOperators(sender, this, "commands.testfor.success", entity.getName());
        }
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] args, int index) {
        return 0 == index;
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 1 == args.length ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }
}