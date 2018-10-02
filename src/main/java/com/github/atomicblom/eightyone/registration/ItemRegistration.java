package com.github.atomicblom.eightyone.registration;

import com.github.atomicblom.eightyone.BlockLibrary;
import com.github.atomicblom.eightyone.blocks.UnbreakableBlock;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import java.util.List;

@Mod.EventBusSubscriber
public class ItemRegistration
{
	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		registerItemFromBlock(registry, BlockLibrary.portal);
		registerItemFromBlock(registry, BlockLibrary.dungeon_block);
		registerItemFromBlock(registry, BlockLibrary.secret_block);
		registerItemFromBlock(registry, BlockLibrary.secret_pressure_plate);
		registerItemFromBlock(registry, BlockLibrary.placeholder_loot_chest);


		for (final UnbreakableBlock mimicBlock : mimicBlocks)
		{
			registerItemFromBlock(registry, mimicBlock);
		}
	}

	private static List<UnbreakableBlock> mimicBlocks = Lists.newArrayList();

	@SubscribeEvent
	public static void onRegisterMimicBlock(RegisterMimicBlockEvent event) {
		mimicBlocks.add(event.getBlock());
	}

	private static void registerItemFromBlock(IForgeRegistry<Item> registry, Block block)
	{
		final ResourceLocation registryName = block.getRegistryName();
		registry.register(new ItemBlock(block)
				.setRegistryName(registryName)
				.setTranslationKey(registryName.toString())
		);
	}
}
