package net.minecraft.command;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.List;

public class CommandTime extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "time";
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
        return "commands.time.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (1 < args.length) {
            if ("set".equals(args[0])) {
                int l;

                if ("day".equals(args[1])) {
                    l = 1000;
                } else if ("night".equals(args[1])) {
                    l = 13000;
                } else {
                    l = parseInt(args[1], 0);
                }

                this.setTime(sender, l);
                notifyOperators(sender, this, "commands.time.set", Integer.valueOf(l));
                return;
            }

            if ("add".equals(args[0])) {
                int k = parseInt(args[1], 0);
                this.addTime(sender, k);
                notifyOperators(sender, this, "commands.time.added", Integer.valueOf(k));
                return;
            }

            if ("query".equals(args[0])) {
                if ("daytime".equals(args[1])) {
                    int j = (int) (sender.getEntityWorld().getWorldTime() % 2147483647L);
                    sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, j);
                    notifyOperators(sender, this, "commands.time.query", Integer.valueOf(j));
                    return;
                }

                if ("gametime".equals(args[1])) {
                    int i = (int) (sender.getEntityWorld().getTotalWorldTime() % 2147483647L);
                    sender.setCommandStat(CommandResultStats.Type.QUERY_RESULT, i);
                    notifyOperators(sender, this, "commands.time.query", Integer.valueOf(i));
                    return;
                }
            }
        }

        throw new WrongUsageException("commands.time.usage");
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 1 == args.length ? getListOfStringsMatchingLastWord(args, "set", "add", "query") : (2 == args.length && "set".equals(args[0]) ? getListOfStringsMatchingLastWord(args, "day", "night") : (2 == args.length && "query".equals(args[0]) ? getListOfStringsMatchingLastWord(args, "daytime", "gametime") : null));
    }

    /**
     * Set the time in the server object.
     */
    protected void setTime(ICommandSender sender, int time) {
        for (int i = 0; i < MinecraftServer.getServer().worldServers.length; ++i) {
            MinecraftServer.getServer().worldServers[i].setWorldTime(time);
        }
    }

    /**
     * Adds (or removes) time in the server object.
     */
    protected void addTime(ICommandSender sender, int time) {
        for (int i = 0; i < MinecraftServer.getServer().worldServers.length; ++i) {
            WorldServer worldserver = MinecraftServer.getServer().worldServers[i];
            worldserver.setWorldTime(worldserver.getWorldTime() + (long) time);
        }
    }
}