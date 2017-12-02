package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.DungeonBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class ItemLibrary
{
	public static final ItemBlock portal;
	public static final ItemBlock dungeon_block;

	static {
		portal = null;
		dungeon_block = null;
	}
}
