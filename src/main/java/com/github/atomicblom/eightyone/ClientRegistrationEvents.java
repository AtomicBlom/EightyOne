package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.PortalTileEntity;
import com.github.atomicblom.eightyone.client.PortalTESR;
import net.minecraft.item.Item;
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
}
