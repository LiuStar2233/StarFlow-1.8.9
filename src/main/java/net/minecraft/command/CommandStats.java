package net.minecraft.command;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.List;

public class CommandStats extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "stats";
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
        return "commands.stats.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (1 > args.length) {
            throw new WrongUsageException("commands.stats.usage");
        } else {
            boolean flag;

            if ("entity".equals(args[0])) {
                flag = false;
            } else {
                if (!"block".equals(args[0])) {
                    throw new WrongUsageException("commands.stats.usage");
                }

                flag = true;
            }

            int i;

            if (flag) {
                if (5 > args.length) {
                    throw new WrongUsageException("commands.stats.block.usage");
                }

                i = 4;
            } else {
                if (3 > args.length) {
                    throw new WrongUsageException("commands.stats.entity.usage");
                }

                i = 2;
            }

            String s = args[i++];

            if ("set".equals(s)) {
                if (args.length < i + 3) {
                    if (5 == i) {
                        throw new WrongUsageException("commands.stats.block.set.usage");
                    }

                    throw new WrongUsageException("commands.stats.entity.set.usage");
                }
            } else {
                if (!"clear".equals(s)) {
                    throw new WrongUsageException("commands.stats.usage");
                }

                if (args.length < i + 1) {
                    if (5 == i) {
                        throw new WrongUsageException("commands.stats.block.clear.usage");
                    }

                    throw new WrongUsageException("commands.stats.entity.clear.usage");
                }
            }

            CommandResultStats.Type commandresultstats$type = CommandResultStats.Type.getTypeByName(args[i++]);

            if (null == commandresultstats$type) {
                throw new CommandException("commands.stats.failed");
            } else {
                World world = sender.getEntityWorld();
                CommandResultStats commandresultstats;

                if (flag) {
                    BlockPos blockpos = parseBlockPos(sender, args, 1, false);
                    TileEntity tileentity = world.getTileEntity(blockpos);

                    if (null == tileentity) {
                        throw new CommandException("commands.stats.noCompatibleBlock", Integer.valueOf(blockpos.getX()), Integer.valueOf(blockpos.getY()), Integer.valueOf(blockpos.getZ()));
                    }

                    if (tileentity instanceof TileEntityCommandBlock) {
                        commandresultstats = ((TileEntityCommandBlock) tileentity).getCommandResultStats();
                    } else {
                        if (!(tileentity instanceof TileEntitySign)) {
                            throw new CommandException("commands.stats.noCompatibleBlock", Integer.valueOf(blockpos.getX()), Integer.valueOf(blockpos.getY()), Integer.valueOf(blockpos.getZ()));
                        }

                        commandresultstats = ((TileEntitySign) tileentity).getStats();
                    }
                } else {
                    Entity entity = getEntity(sender, args[1]);
                    commandresultstats = entity.getCommandStats();
                }

                if ("set".equals(s)) {
                    String s1 = args[i++];
                    String s2 = args[i];

                    if (0 == s1.length() || 0 == s2.length()) {
                        throw new CommandException("commands.stats.failed");
                    }

                    CommandResultStats.setScoreBoardStat(commandresultstats, commandresultstats$type, s1, s2);
                    notifyOperators(sender, this, "commands.stats.success", commandresultstats$type.getTypeName(), s2, s1);
                } else if ("clear".equals(s)) {
                    CommandResultStats.setScoreBoardStat(commandresultstats, commandresultstats$type, null, null);
                    notifyOperators(sender, this, "commands.stats.cleared", commandresultstats$type.getTypeName());
                }

                if (flag) {
                    BlockPos blockpos1 = parseBlockPos(sender, args, 1, false);
                    TileEntity tileentity1 = world.getTileEntity(blockpos1);
                    tileentity1.markDirty();
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 1 == args.length ? getListOfStringsMatchingLastWord(args, "entity", "block") : (2 == args.length && "entity".equals(args[0]) ? getListOfStringsMatchingLastWord(args, this.func_175776_d()) : (2 <= args.length && 4 >= args.length && "block".equals(args[0]) ? func_175771_a(args, 1, pos) : ((3 != args.length || !"entity".equals(args[0])) && (5 != args.length || !"block".equals(args[0])) ? ((4 != args.length || !"entity".equals(args[0])) && (6 != args.length || !"block".equals(args[0])) ? ((6 != args.length || !"entity".equals(args[0])) && (8 != args.length || !"block".equals(args[0])) ? null : getListOfStringsMatchingLastWord(args, this.func_175777_e())) : getListOfStringsMatchingLastWord(args, CommandResultStats.Type.getTypeNames())) : getListOfStringsMatchingLastWord(args, "set", "clear"))));
    }

    protected String[] func_175776_d() {
        return MinecraftServer.getServer().getAllUsernames();
    }

    protected List<String> func_175777_e() {
        Collection<ScoreObjective> collection = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard().getScoreObjectives();
        List<String> list = Lists.newArrayList();

        for (ScoreObjective scoreobjective : collection) {
            if (!scoreobjective.getCriteria().isReadOnly()) {
                list.add(scoreobjective.getName());
            }
        }

        return list;
    }

    /**
     * Return whether the specified command parameter index is a username parameter.
     */
    public boolean isUsernameIndex(String[] args, int index) {
        return 0 < args.length && "entity".equals(args[0]) && 1 == index;
    }
}