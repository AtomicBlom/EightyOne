package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.Portal;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber
public class RegistrationEvents
{
	@SubscribeEvent
	public static void onRegisterBlocks(RegistryEvent.Register<Block> event) {
		final IForgeRegistry<Block> registry = event.getRegistry();
		final ResourceLocation registryName = new ResourceLocation(Reference.MOD_ID, "portal");
		registry.register(new Portal()
				.setRegistryName(registryName)
				.setUnlocalizedName(registryName.toString())
		);
	}

	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		final IForgeRegistry<Item> registry = event.getRegistry();
		final ResourceLocation registryName = BlockLibrary.portal.getRegistryName();
		registry.register(new ItemBlock(BlockLibrary.portal)
				.setRegistryName(registryName)
				.setUnlocalizedName(registryName.toString())
		);
	}
}
