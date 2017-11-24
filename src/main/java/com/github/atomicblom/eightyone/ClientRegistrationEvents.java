package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.PortalTileEntity;
import com.github.atomicblom.eightyone.client.PortalTESR;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientRegistrationEvents
{
	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		ClientRegistry.bindTileEntitySpecialRenderer(PortalTileEntity.class, new PortalTESR());
	}

	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		final TextureMap map = event.getMap();
		map.registerSprite(new ResourceLocation(Reference.MOD_ID, "blocks/portal3"));
	}
}
