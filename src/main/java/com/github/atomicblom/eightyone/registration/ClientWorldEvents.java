package com.github.atomicblom.eightyone.registration;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class ClientWorldEvents
{
	private static boolean wasCreativeLastFrame;

	@SubscribeEvent
	public static void onTick(TickEvent.ClientTickEvent event) {
		final Minecraft minecraft = Minecraft.getMinecraft();
		if (minecraft.player == null) return;
		final boolean isCreativeThisFrame = minecraft.player.isCreative();
		if (!wasCreativeLastFrame && isCreativeThisFrame) {
			minecraft.renderGlobal.loadRenderers();
		} else if (wasCreativeLastFrame && !isCreativeThisFrame) {
			minecraft.renderGlobal.loadRenderers();
		}
		wasCreativeLastFrame = isCreativeThisFrame;
	}
}

