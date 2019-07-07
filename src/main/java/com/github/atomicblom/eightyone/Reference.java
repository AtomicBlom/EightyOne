package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.properties.MimicBlockProperty;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.property.Properties;

public final class Reference {
	public static final String MOD_ID = "eightyone";
	public static final String MOD_NAME = "Eighty One";
	public static final String VERSION = "@MOD_VERSION@";

	public static final int DIMENSION_ID = 81;
	public static final int ORIGIN_DIMENSION_ID = 0;
	public static final String WORLD_NAME = "labyrinth";

	public static final ResourceLocation DUNGEON_RECIPE = resource("dungeon_recipe");

	public static final CreativeTabs CREATIVE_TAB = new CreativeTabs(MOD_ID) {
		public ItemStack _creativeTabIcon;

		@Override
		public ItemStack createIcon() {
			return (_creativeTabIcon != null ? _creativeTabIcon : (_creativeTabIcon = new ItemStack(ItemLibrary.portal)));
		}
	};

	public static final class Blocks {
		public static final ResourceLocation PORTAL = resource("portal");
		public static final ResourceLocation DUNGEON_BLOCK = resource("dungeon_block");
		public static final ResourceLocation SECRET_BLOCK = resource("secret_block");
		public static final ResourceLocation SECRET_PRESSURE_PLATE = resource("secret_pressure_plate");
		public static final ResourceLocation PLACEHOLDER_LOOT_CHEST = resource("placeholder_loot_chest");
		public static final ResourceLocation DARK_AIR = resource("dark_air");


		public static final IProperty<Boolean> OVERLAY = PropertyBool.create("overlay");
		public static final IUnlistedProperty<IBlockState> MIMIC = new MimicBlockProperty();
		public static final IProperty<Integer> VARIATION = PropertyInteger.create("variation", 0, 15);
	}

	public static final class TileEntities {
		public static final ResourceLocation PORTAL = resource("te_portal");
		public static final ResourceLocation DUNGEON_BLOCK = resource("te_dungeon_block");
		public static final ResourceLocation PLACEHOLDER_LOOT_CHEST = resource("te_placeholder_loot_chest");
	}

	private Reference() {}

	private static ResourceLocation resource(String path) {
		return new ResourceLocation(Reference.MOD_ID, path);
	}
}
