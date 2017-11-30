package com.github.atomicblom.eightyone.command;

import com.github.atomicblom.eightyone.world.NxNChunkGenerator;
import com.github.atomicblom.eightyone.world.NxNWorldProvider;
import com.github.atomicblom.eightyone.world.Room;
import com.github.atomicblom.eightyone.world.structure.RoomTemplate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

public class DescribeRoomCommand extends CommandBase
{
	@Override
	public String getName()
	{
		return "describeRoom";

	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "describeRoom";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		final World entityWorld = sender.getEntityWorld();
		final WorldProvider provider = entityWorld.provider;
		if (provider instanceof NxNWorldProvider) {
			final NxNChunkGenerator chunkGenerator = (NxNChunkGenerator) provider.createChunkGenerator();
			final BlockPos position = sender.getPosition();
			final Room startingRoom = chunkGenerator.getRoomAt(position.getX(), position.getZ());
			final RoomTemplate template = startingRoom.getTemplate();
			sender.sendMessage(new TextComponentString(template.toString()));
		}
	}
}
