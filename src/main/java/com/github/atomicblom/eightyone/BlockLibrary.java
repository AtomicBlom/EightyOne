package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.DungeonBlock;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class BlockLibrary
{
	public static final Block portal;
	public static final DungeonBlock dungeon_block;

	static {
		portal = null;
		dungeon_block = null;
	}

}
