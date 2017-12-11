package com.github.atomicblom.eightyone.registration;

import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.DungeonRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class RecipeRegistration
{
	@SubscribeEvent
	public static void onRegisterRecipes(RegistryEvent.Register<IRecipe> event) {
		event.getRegistry().register(new DungeonRecipe().setRegistryName(Reference.DUNGEON_RECIPE));
	}
}
