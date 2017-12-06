package com.github.atomicblom.eightyone;

import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;

public final class Reference {
	public static final String MOD_ID = "eightyone";
	public static final String MOD_NAME = "Eighty One";
	public static final String VERSION = "@MOD_VERSION@";

	public static final int DIMENSION_ID = 81;
	public static BlockRenderLayer CURRENT_RENDER_LAYER;
	public static ResourceLocation DUNGEON_RECIPE = resource("dungeon_recipe");

	public static final class Blocks {
		public static final ResourceLocation PORTAL = resource("portal");
		public static final ResourceLocation DUNGEON_BLOCK = resource("dungeon_block");
		public static final ResourceLocation PLACEHOLDER_LOOT_CHEST = resource("placeholder_loot_chest");
	}

	private Reference() {}

	private static ResourceLocation resource(String path) {
		return new ResourceLocation(Reference.MOD_ID, path);
	}
}
