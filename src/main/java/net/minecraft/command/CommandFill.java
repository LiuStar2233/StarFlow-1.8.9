package net.minecraft.command;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class CommandFill extends CommandBase {
    /**
     * Gets the name of the command
     */
    public String getCommandName() {
        return "fill";
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
        return "commands.fill.usage";
    }

    /**
     * Callback when the command is invoked
     */
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        if (7 > args.length) {
            throw new WrongUsageException("commands.fill.usage");
        } else {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, 0);
            BlockPos blockpos = parseBlockPos(sender, args, 0, false);
            BlockPos blockpos1 = parseBlockPos(sender, args, 3, false);
            Block block = CommandBase.getBlockByText(sender, args[6]);
            int i = 0;

            if (8 <= args.length) {
                i = parseInt(args[7], 0, 15);
            }

            BlockPos blockpos2 = new BlockPos(Math.min(blockpos.getX(), blockpos1.getX()), Math.min(blockpos.getY(), blockpos1.getY()), Math.min(blockpos.getZ(), blockpos1.getZ()));
            BlockPos blockpos3 = new BlockPos(Math.max(blockpos.getX(), blockpos1.getX()), Math.max(blockpos.getY(), blockpos1.getY()), Math.max(blockpos.getZ(), blockpos1.getZ()));
            int j = (blockpos3.getX() - blockpos2.getX() + 1) * (blockpos3.getY() - blockpos2.getY() + 1) * (blockpos3.getZ() - blockpos2.getZ() + 1);

            if (32768 < j) {
                throw new CommandException("commands.fill.tooManyBlocks", Integer.valueOf(j), Integer.valueOf(32768));
            } else if (0 <= blockpos2.getY() && 256 > blockpos3.getY()) {
                World world = sender.getEntityWorld();

                for (int k = blockpos2.getZ(); k < blockpos3.getZ() + 16; k += 16) {
                    for (int l = blockpos2.getX(); l < blockpos3.getX() + 16; l += 16) {
                        if (!world.isBlockLoaded(new BlockPos(l, blockpos3.getY() - blockpos2.getY(), k))) {
                            throw new CommandException("commands.fill.outOfWorld");
                        }
                    }
                }

                NBTTagCompound nbttagcompound = new NBTTagCompound();
                boolean flag = false;

                if (10 <= args.length && block.hasTileEntity()) {
                    String s = getChatComponentFromNthArg(sender, args, 9).getUnformattedText();

                    try {
                        nbttagcompound = JsonToNBT.getTagFromJson(s);
                        flag = true;
                    } catch (NBTException nbtexception) {
                        throw new CommandException("commands.fill.tagError", nbtexception.getMessage());
                    }
                }

                List<BlockPos> list = Lists.newArrayList();
                j = 0;

                for (int i1 = blockpos2.getZ(); i1 <= blockpos3.getZ(); ++i1) {
                    for (int j1 = blockpos2.getY(); j1 <= blockpos3.getY(); ++j1) {
                        for (int k1 = blockpos2.getX(); k1 <= blockpos3.getX(); ++k1) {
                            BlockPos blockpos4 = new BlockPos(k1, j1, i1);

                            if (9 <= args.length) {
                                if (!"outline".equals(args[8]) && !"hollow".equals(args[8])) {
                                    if ("destroy".equals(args[8])) {
                                        world.destroyBlock(blockpos4, true);
                                    } else if ("keep".equals(args[8])) {
                                        if (!world.isAirBlock(blockpos4)) {
                                            continue;
                                        }
                                    } else if ("replace".equals(args[8]) && !block.hasTileEntity()) {
                                        if (9 < args.length) {
                                            Block block1 = CommandBase.getBlockByText(sender, args[9]);

                                            if (world.getBlockState(blockpos4).getBlock() != block1) {
                                                continue;
                                            }
                                        }

                                        if (10 < args.length) {
                                            int l1 = CommandBase.parseInt(args[10]);
                                            IBlockState iblockstate = world.getBlockState(blockpos4);

                                            if (iblockstate.getBlock().getMetaFromState(iblockstate) != l1) {
                                                continue;
                                            }
                                        }
                                    }
                                } else if (k1 != blockpos2.getX() && k1 != blockpos3.getX() && j1 != blockpos2.getY() && j1 != blockpos3.getY() && i1 != blockpos2.getZ() && i1 != blockpos3.getZ()) {
                                    if ("hollow".equals(args[8])) {
                                        world.setBlockState(blockpos4, Blocks.air.getDefaultState(), 2);
                                        list.add(blockpos4);
                                    }

                                    continue;
                                }
                            }

                            TileEntity tileentity1 = world.getTileEntity(blockpos4);

                            if (null != tileentity1) {
                                if (tileentity1 instanceof IInventory) {
                                    ((IInventory) tileentity1).clear();
                                }

                                world.setBlockState(blockpos4, Blocks.barrier.getDefaultState(), block == Blocks.barrier ? 2 : 4);
                            }

                            IBlockState iblockstate1 = block.getStateFromMeta(i);

                            if (world.setBlockState(blockpos4, iblockstate1, 2)) {
                                list.add(blockpos4);
                                ++j;

                                if (flag) {
                                    TileEntity tileentity = world.getTileEntity(blockpos4);

                                    if (null != tileentity) {
                                        nbttagcompound.setInteger("x", blockpos4.getX());
                                        nbttagcompound.setInteger("y", blockpos4.getY());
                                        nbttagcompound.setInteger("z", blockpos4.getZ());
                                        tileentity.readFromNBT(nbttagcompound);
                                    }
                                }
                            }
                        }
                    }
                }

                for (BlockPos blockpos5 : list) {
                    Block block2 = world.getBlockState(blockpos5).getBlock();
                    world.notifyNeighborsRespectDebug(blockpos5, block2);
                }

                if (0 >= j) {
                    throw new CommandException("commands.fill.failed");
                } else {
                    sender.setCommandStat(CommandResultStats.Type.AFFECTED_BLOCKS, j);
                    notifyOperators(sender, this, "commands.fill.success", Integer.valueOf(j));
                }
            } else {
                throw new CommandException("commands.fill.outOfWorld");
            }
        }
    }

    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return 0 < args.length && 3 >= args.length ? func_175771_a(args, 0, pos) : (3 < args.length && 6 >= args.length ? func_175771_a(args, 3, pos) : (7 == args.length ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : (9 == args.length ? getListOfStringsMatchingLastWord(args, "replace", "destroy", "keep", "hollow", "outline") : (10 == args.length && "replace".equals(args[8]) ? getListOfStringsMatchingLastWord(args, Block.blockRegistry.getKeys()) : null))));
    }
}