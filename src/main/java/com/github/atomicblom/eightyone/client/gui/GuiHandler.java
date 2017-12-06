package com.github.atomicblom.eightyone.client.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler
{

	public static final int PLACEHOLDER_LOOT_CHEST_GUI = 0;

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == PLACEHOLDER_LOOT_CHEST_GUI) {
			return new LootChestGui(world, x, y, z);
		}
		return null;
	}
}