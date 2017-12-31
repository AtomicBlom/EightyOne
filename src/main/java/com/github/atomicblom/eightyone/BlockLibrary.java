package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.*;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;

@SuppressWarnings("AssignmentToNull")
@GameRegistry.ObjectHolder(Reference.MOD_ID)
public class BlockLibrary
{
	public static final Block portal;
	public static final DungeonBlock dungeon_block;
	public static final SecretBlock secret_block;
	public static final SecretPressurePlate secret_pressure_plate;
	public static final PlaceholderLootChest placeholder_loot_chest;
	public static final DarkAirBlock dark_air;

	static {
		portal = null;
		dungeon_block = null;
		secret_block = null;
		secret_pressure_plate = null;
		placeholder_loot_chest = null;
		dark_air = null;
	}

}
