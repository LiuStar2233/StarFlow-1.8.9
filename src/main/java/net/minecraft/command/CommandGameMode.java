package net.minecraft.command;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSettings;

import java.util.List;

public class CommandGameMode extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "gamemode";
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
        return "commands.gamemode.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (0 >= args.length) {
            throw new WrongUsageException("commands.gamemode.usage");
        } else {
            WorldSettings.GameType worldsettings$gametype = this.getGameModeFromCommand(sender, args[0]);
            EntityPlayer entityplayer = 2 <= args.length ? getPlayer(sender, args[1]) : getCommandSenderAsPlayer(sender);
            entityplayer.setGameType(worldsettings$gametype);
            entityplayer.fallDistance = 0.0F;

            if (sender.getEntityWorld().getGameRules().getBoolean("sendCommandFeedback")) {
                entityplayer.addChatMessage(new ChatComponentTranslation("gameMode.changed"));
            }

            IChatComponent ichatcomponent = new ChatComponentTranslation("gameMode." + worldsettings$gametype.getName());

            if (entityplayer != sender) {
                notifyOperators(sender, this, 1, "commands.gamemode.success.other", entityplayer.getName(), ichatcomponent);
            } else {
                notifyOperators(sender, this, 1, "commands.gamemode.success.self", ichatcomponent);
            }
        }
    }

    /**
     * Gets the Game Mode specified in the command.
     */
    protected WorldSettings.GameType getGameModeFromCommand(ICommandSender p_71539_1_, String p_71539_2_) throws CommandException {
        return !p_71539_2_.equalsIgnoreCase(WorldSettings.GameType.SURVIVAL.getName()) && !"s".equalsIgnoreCase(p_71539_2_) ? (!p_71539_2_.equalsIgnoreCase(WorldSettings.GameType.CREATIVE.getName()) && !"c".equalsIgnoreCase(p_71539_2_) ? (!p_71539_2_.equalsIgnoreCase(WorldSettings.GameType.ADVENTURE.getName()) && !"a".equalsIgnoreCase(p_71539_2_) ? (!p_71539_2_.equalsIgnoreCase(WorldSettings.GameType.SPECTATOR.getName()) && !"sp".equalsIgnoreCase(p_71539_2_) ? WorldSettings.getGameTypeById(parseInt(p_71539_2_, 0, WorldSettings.GameType.values().length - 2)) : WorldSettings.GameType.SPECTATOR) : WorldSettings.GameType.ADVENTURE) : WorldSettings.GameType.CREATIVE) : WorldSettings.GameType.SURVIVAL;
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 1 == args.length ? getListOfStringsMatchingLastWord(args, "survival", "creative", "adventure", "spectator") : (2 == args.length ? getListOfStringsMatchingLastWord(args, this.getListOfPlayerUsernames()) : null);
    }

    /**
     * Returns String array containing all player usernames in the server.
     */
    protected String[] getListOfPlayerUsernames() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] args, int index) {
        return 1 == index;
    }
}