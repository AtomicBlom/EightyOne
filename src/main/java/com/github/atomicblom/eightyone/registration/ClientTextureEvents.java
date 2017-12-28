package com.github.atomicblom.eightyone.registration;

import com.github.atomicblom.eightyone.Reference;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientTextureEvents
{
	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		final TextureMap map = event.getMap();
		map.registerSprite(new ResourceLocation(Reference.MOD_ID, "blocks/portal_active"));
		map.registerSprite(new ResourceLocation(Reference.MOD_ID, "blocks/portal_disabled"));
		map.registerSprite(new ResourceLocation(Reference.MOD_ID, "blocks/portal_frame"));
	}
}
