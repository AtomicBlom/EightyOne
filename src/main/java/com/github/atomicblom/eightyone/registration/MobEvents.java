package com.github.atomicblom.eightyone.registration;

import com.github.atomicblom.eightyone.world.NxNWorldProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class MobEvents
{
	@SubscribeEvent
	public static void onMobSpawn(WorldEvent.PotentialSpawns potentialSpawns) {
		final World world = potentialSpawns.getWorld();
		if (world.provider instanceof NxNWorldProvider) {
			final BlockPos height = world.getHeight(potentialSpawns.getPos());
			if (potentialSpawns.getPos().getY() >= height.getY()) {
				potentialSpawns.getList().clear();
			}
		}
	}
}
