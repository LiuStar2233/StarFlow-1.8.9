package net.minecraft.command.server;

import com.mojang.authlib.GameProfile;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;

import java.util.List;

public class CommandWhitelist extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "whitelist";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 3;
    }

    /**
     * Gets the usage string for the command.
     */
    public String getCommandUsage(ICommandSender sender) {
        return "commands.whitelist.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (1 > args.length) {
            throw new WrongUsageException("commands.whitelist.usage");
        } else {
            MinecraftServer minecraftserver = MinecraftServer.getServer();

            if ("on".equals(args[0])) {
                minecraftserver.getConfigurationManager().setWhiteListEnabled(true);
                notifyOperators(sender, this, "commands.whitelist.enabled");
            } else if ("off".equals(args[0])) {
                minecraftserver.getConfigurationManager().setWhiteListEnabled(false);
                notifyOperators(sender, this, "commands.whitelist.disabled");
            } else if ("list".equals(args[0])) {
                sender.addChatMessage(new ChatComponentTranslation("commands.whitelist.list", Integer.valueOf(minecraftserver.getConfigurationManager().getWhitelistedPlayerNames().length), Integer.valueOf(minecraftserver.getConfigurationManager().getAvailablePlayerDat().length)));
                String[] astring = minecraftserver.getConfigurationManager().getWhitelistedPlayerNames();
                sender.addChatMessage(new ChatComponentText(joinNiceString(astring)));
            } else if ("add".equals(args[0])) {
                if (2 > args.length) {
                    throw new WrongUsageException("commands.whitelist.add.usage");
                }

                GameProfile gameprofile = minecraftserver.getPlayerProfileCache().getGameProfileForUsername(args[1]);

                if (null == gameprofile) {
                    throw new CommandException("commands.whitelist.add.failed", args[1]);
                }

                minecraftserver.getConfigurationManager().addWhitelistedPlayer(gameprofile);
                notifyOperators(sender, this, "commands.whitelist.add.success", args[1]);
            } else if ("remove".equals(args[0])) {
                if (2 > args.length) {
                    throw new WrongUsageException("commands.whitelist.remove.usage");
                }

                GameProfile gameprofile1 = minecraftserver.getConfigurationManager().getWhitelistedPlayers().getBannedProfile(args[1]);

                if (null == gameprofile1) {
                    throw new CommandException("commands.whitelist.remove.failed", args[1]);
                }

                minecraftserver.getConfigurationManager().removePlayerFromWhitelist(gameprofile1);
                notifyOperators(sender, this, "commands.whitelist.remove.success", args[1]);
            } else if ("reload".equals(args[0])) {
                minecraftserver.getConfigurationManager().loadWhiteList();
                notifyOperators(sender, this, "commands.whitelist.reloaded");
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (1 == args.length) {
            return getListOfStringsMatchingLastWord(args, "on", "off", "list", "add", "remove", "reload");
        } else {
            if (2 == args.length) {
                if ("remove".equals(args[0])) {
                    return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getConfigurationManager().getWhitelistedPlayerNames());
                }

                if ("add".equals(args[0])) {
                    return getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getPlayerProfileCache().getUsernames());
                }
            }

            return null;
        }
    }
}