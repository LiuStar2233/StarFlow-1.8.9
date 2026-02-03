package net.minecraft.command.server;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;

public class CommandSaveAll extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "save-all";
    }

    /**
     * Gets the usage string for the command.
     */
    public String getCommandUsage(ICommandSender sender) {
        return "commands.save.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        MinecraftServer minecraftserver = MinecraftServer.getServer();
        sender.addChatMessage(new ChatComponentTranslation("commands.save.start"));

        if (null != minecraftserver.getConfigurationManager()) {
            minecraftserver.getConfigurationManager().saveAllPlayerData();
        }

        try {
            for (int i = 0; i < minecraftserver.worldServers.length; ++i) {
                if (null != minecraftserver.worldServers[i]) {
                    WorldServer worldserver = minecraftserver.worldServers[i];
                    boolean flag = worldserver.disableLevelSaving;
                    worldserver.disableLevelSaving = false;
                    worldserver.saveAllChunks(true, (IProgressUpdate) null);
                    worldserver.disableLevelSaving = flag;
                }
            }

            if (0 < args.length && "flush".equals(args[0])) {
                sender.addChatMessage(new ChatComponentTranslation("commands.save.flushStart"));

                for (int j = 0; j < minecraftserver.worldServers.length; ++j) {
                    if (null != minecraftserver.worldServers[j]) {
                        WorldServer worldserver1 = minecraftserver.worldServers[j];
                        boolean flag1 = worldserver1.disableLevelSaving;
                        worldserver1.disableLevelSaving = false;
                        worldserver1.saveChunkData();
                        worldserver1.disableLevelSaving = flag1;
                    }
                }

                sender.addChatMessage(new ChatComponentTranslation("commands.save.flushEnd"));
            }
        } catch (MinecraftException minecraftexception) {
            notifyOperators(sender, this, "commands.save.failed", minecraftexception.getMessage());
            return;
        }

        notifyOperators(sender, this, "commands.save.success");
    }
}