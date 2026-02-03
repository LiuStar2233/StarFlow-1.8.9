package net.minecraft.command.server;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.*;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class CommandSetBlock extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "setblock";
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
        return "commands.setblock.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (4 > args.length) {
            throw new WrongUsageException("commands.setblock.usage");
        } else {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos blockpos = parseBlockPos(sender, args, 0, false);
            Block block = CommandBase.getBlockByText(sender, args[3]);
            int i = 0;

            if (5 <= args.length) {
                i = parseInt(args[4], 0, 15);
            }

            World world = sender.getEntityWorld();

            if (!world.isBlockLoaded(blockpos)) {
                throw new CommandException("commands.setblock.outOfWorld");
            } else {
                NBTTagCompound nbttagcompound = new NBTTagCompound();
                boolean flag = false;

                if (7 <= args.length && block.hasTileEntity()) {
                    String s = getChatComponentFromNthArg(sender, args, 6).getUnformattedText();

                    try {
                        nbttagcompound = JsonToNBT.getTagFromJson(s);
                        flag = true;
                    } catch (NBTException nbtexception) {
                        throw new CommandException("commands.setblock.tagError", nbtexception.getMessage());
                    }
                }

                if (6 <= args.length) {
                    if ("destroy".equals(args[5])) {
                        world.destroyBlock(blockpos, true);

                        if (block == Blocks.air) {
                            notifyOperators(sender, this, "commands.setblock.success");
                            return;
                        }
                    } else if ("keep".equals(args[5]) && !world.isAirBlock(blockpos)) {
                        throw new CommandException("commands.setblock.noChange");
                    }
                }

                TileEntity tileentity1 = world.getTileEntity(blockpos);

                if (null != tileentity1) {
                    if (tileentity1 instanceof IInventory) {
                        ((IInventory) tileentity1).clear();
                    }

                    world.setBlockState(blockpos, Blocks.air.getDefaultState(), block == Blocks.air ? 2 : 4);
                }

                IBlockState iblockstate = block.getStateFromMeta(i);

                if (!world.setBlockState(blockpos, iblockstate, 2)) {
                    throw new CommandException("commands.setblock.noChange");
                } else {
                    if (flag) {
                        TileEntity tileentity = world.getTileEntity(blockpos);

                        if (null != tileentity) {
                            nbttagcompound.setInteger("x", blockpos.getX());
                            nbttagcompound.setInteger("y", blockpos.getY());
                            nbttagcompound.setInteger("z", blockpos.getZ());
                            tileentity.readFromNBT(nbttagcompound);
                        }
                    }

                    world.notifyNeighborsRespectDebug(blockpos, iblockstate.getBlock());
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 1);
                    notifyOperators(sender, this, "commands.setblock.success");
                }
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 0 < args.length && 3 >= args.length ? func_175771_a(args, 0, pos) : (4 == args.length ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : (6 == args.length ? getListOfStringsMatchingLastWord(args, "replace", "destroy", "keep") : null));
    }
}