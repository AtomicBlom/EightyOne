package com.github.atomicblom.eightyone.util;

import com.github.atomicblom.eightyone.EightyOne;
import com.github.atomicblom.eightyone.Reference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IFixableData;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import java.util.Map;

public class DataFixers
{
	public static final int DATA_FIXER_VERSION = 1;

	public static void init() {
		ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(Reference.MOD_ID, DATA_FIXER_VERSION);
		fixes.registerFix(FixTypes.BLOCK_ENTITY, new NamespaceTEFixer());
	}

	private static class NamespaceTEFixer implements IFixableData {
		private final Map<String, String> tileEntityNames;
		{
			if (EightyOne.isReleaseBuild()) {
				throw new RuntimeException("Steven you derp. You forgot to remove the data fixers!");
			}

			final Builder<String, String> nameMap = ImmutableMap.builder();

			nameMap.put("minecraft:portal", "eightyone:te_portal");
			nameMap.put("minecraft:dungeon_block", "eightyone:te_dungeon_block");
			nameMap.put("minecraft:placeholder_loot_chest", "eightyone:te_placeholder_loot_chest");

			nameMap.put("portal", "eightyone:te_portal");
			nameMap.put("dungeon_block", "eightyone:te_dungeon_block");
			nameMap.put("placeholder_loot_chest", "eightyone:te_placeholder_loot_chest");

			tileEntityNames = nameMap.build();
		}

		@Override
		public int getFixVersion()
		{
			return 1;
		}

		@Override
		public NBTTagCompound fixTagCompound(NBTTagCompound compound)
		{
			final String tileEntityLocation = compound.getString("id");

			compound.setString("id", tileEntityNames.getOrDefault(tileEntityLocation, tileEntityLocation));

			return compound;
		}
	}
}
