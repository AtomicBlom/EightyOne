package com.github.atomicblom.eightyone.registration;

import com.github.atomicblom.eightyone.BlockLibrary;
import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.*;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPlaceholderLootChest;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPortal;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityDungeonBlock;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class RegistrationEvents
{

	@SubscribeEvent
	public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
		event.getRegistry().register(new DungeonRecipe().setRegistryName(Reference.DUNGEON_RECIPE));
	}

	@SubscribeEvent
	public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();

		registerBlock(registry, new PortalBlock(), Reference.Blocks.PORTAL);
		registerBlock(registry, new DungeonBlock(), Reference.Blocks.DUNGEON_BLOCK);
		registerBlock(registry, new SecretBlock(), Reference.Blocks.SECRET_BLOCK);
		registerBlock(registry, new PlaceholderLootChest(), Reference.Blocks.PLACEHOLDER_LOOT_CHEST);

		GameRegistry.registerTileEntity(TileEntityPortal.class, "portal");
		GameRegistry.registerTileEntity(TileEntityDungeonBlock.class, "dungeon_block");
		GameRegistry.registerTileEntity(TileEntityPlaceholderLootChest.class, "placeholder_loot_chest");
	}

	private static void registerBlock(IForgeRegistry<Block> registry, Block block, ResourceLocation registryName)
	{
		registry.register(block
				.setRegistryName(registryName)
				.setUnlocalizedName(registryName.toString())
		);
	}

	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		registerItemFromBlock(registry, BlockLibrary.portal);
		registerItemFromBlock(registry, BlockLibrary.dungeon_block);
		registerItemFromBlock(registry, BlockLibrary.secret_block);
		registerItemFromBlock(registry, BlockLibrary.placeholder_loot_chest);
	}

	private static void registerItemFromBlock(IForgeRegistry<Item> registry, Block block)
	{
		final ResourceLocation registryName = block.getRegistryName();
		registry.register(new ItemBlock(block)
				.setRegistryName(registryName)
				.setUnlocalizedName(registryName.toString())
		);
	}
}
