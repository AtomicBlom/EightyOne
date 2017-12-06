package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.DungeonBlock;
import com.github.atomicblom.eightyone.blocks.PlaceholderLootChest;
import com.github.atomicblom.eightyone.blocks.SecretBlock;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

@SuppressWarnings("AssignmentToNull")
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class BlockLibrary
{
	public static final Block portal;
	public static final DungeonBlock dungeon_block;
	public static final SecretBlock secret_block;
	public static final PlaceholderLootChest placeholder_loot_chest;

	static {
		portal = null;
		dungeon_block = null;
		secret_block = null;
		placeholder_loot_chest = null;
	}

}
