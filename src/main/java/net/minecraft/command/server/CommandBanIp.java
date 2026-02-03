package net.minecraft.command.server;

import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.IPBanEntry;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandBanIp extends CommandBase {
    public static final Pattern field_147211_a = Pattern.compile("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$");

    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "ban-ip";
    }

    /**
     * Return the required permission level for this command.
     */
    public int getRequiredPermissionLevel() {
        return 3;
    }

    /**
     * Returns true if the given command sender is allowed to use this command.
     */
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return MinecraftServer.getServer().getConfigurationManager().getBannedIPs().isLanServer() && super.canCommandSenderUseCommand(sender);
    }

    /**
     * Gets the usage string for the command.
     */
    public String getCommandUsage(ICommandSender sender) {
        return "commands.banip.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (1 <= args.length && 1 < args[0].length()) {
            IChatComponent ichatcomponent = 2 <= args.length ? getChatComponentFromNthArg(sender, args, 1) : null;
            Matcher matcher = field_147211_a.matcher(args[0]);

            if (matcher.matches()) {
                this.func_147210_a(sender, args[0], null == ichatcomponent ? null : ichatcomponent.getUnformattedText());
            } else {
                EntityPlayerMP entityplayermp = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(args[0]);

                if (null == entityplayermp) {
                    throw new PlayerNotFoundException("commands.banip.invalid");
                }

                this.func_147210_a(sender, entityplayermp.getPlayerIP(), null == ichatcomponent ? null : ichatcomponent.getUnformattedText());
            }
        } else {
            throw new WrongUsageException("commands.banip.usage");
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 1 == args.length ? getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames()) : null;
    }

    protected void func_147210_a(ICommandSender sender, String address, String reason) {
        IPBanEntry ipbanentry = new IPBanEntry(address, null, sender.getName(), null, reason);
        MinecraftServer.getServer().getConfigurationManager().getBannedIPs().addEntry(ipbanentry);
        List<EntityPlayerMP> list = MinecraftServer.getServer().getConfigurationManager().getPlayersMatchingAddress(address);
        String[] astring = new String[list.size()];
        int i = 0;

        for (EntityPlayerMP entityplayermp : list) {
            entityplayermp.playerNetServerHandler.kickPlayerFromServer("You have been IP banned.");
            astring[i++] = entityplayermp.getName();
        }

        if (list.isEmpty()) {
            notifyOperators(sender, this, "commands.banip.success", address);
        } else {
            notifyOperators(sender, this, "commands.banip.success.players", address, joinNiceString(astring));
        }
    }
}