package com.github.atomicblom.eightyone.command;

import com.github.atomicblom.eightyone.world.structure.TemplateManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class ReloadRoomsCommand extends CommandBase
{
	@Override
	public String getName()
	{
		return "reloadRooms";

	}

	@Override
	public String getUsage(ICommandSender sender)
	{
		return "reloadRooms [useConfigDir]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
	{
		boolean readFromConfigDirectory = false;

		if (args.length >= 1) {
			readFromConfigDirectory = Boolean.parseBoolean(args[0]);
		}

		TemplateManager.findTemplates(readFromConfigDirectory);
	}
}
