package com.github.atomicblom.eightyone;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.minecart.MinecartCollisionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber()
public class PlayerEvents
{
	@SubscribeEvent
	public static void onHitByMinecart(MinecartCollisionEvent event) {
		final Entity toTeleport = event.getCollider();
		int dimensionIn = 81;
		if (toTeleport instanceof EntityPlayerMP)
		{
			if (!toTeleport.world.isRemote && !toTeleport.isDead) {
				if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(toTeleport, dimensionIn)) return;
				toTeleport.world.profiler.startSection("changeDimension");
				MinecraftServer minecraftserver = toTeleport.getServer();
				int currentDimensionId = toTeleport.dimension;
				WorldServer worldserver = minecraftserver.getWorld(currentDimensionId);
				WorldServer worldserver1 = minecraftserver.getWorld(dimensionIn);
				toTeleport.dimension = dimensionIn;

				if (currentDimensionId == 1 && dimensionIn == 1) {
					worldserver1 = minecraftserver.getWorld(0);
					toTeleport.dimension = 0;
				}

				toTeleport.world.removeEntity(toTeleport);
				toTeleport.isDead = false;
				toTeleport.world.profiler.startSection("reposition");
				BlockPos blockpos;

				if (dimensionIn == 1) {
					blockpos = worldserver1.getSpawnCoordinate();
				} else {
					double d0 = toTeleport.posX;
					double d1 = toTeleport.posZ;
					double d2 = 8.0D;

					// Tf - remove 8x scaling for nether
					d0 = MathHelper.clamp(d0, worldserver1.getWorldBorder().minX() + 16.0D, worldserver1.getWorldBorder().maxX() - 16.0D);
					d1 = MathHelper.clamp(d1, worldserver1.getWorldBorder().minZ() + 16.0D, worldserver1.getWorldBorder().maxZ() - 16.0D);

					d0 = (double) MathHelper.clamp((int) d0, -29999872, 29999872);
					d1 = (double) MathHelper.clamp((int) d1, -29999872, 29999872);
					float f = toTeleport.rotationYaw;
					toTeleport.setLocationAndAngles(d0, toTeleport.posY, d1, 90.0F, 0.0F);
					//Teleporter teleporter = TFTeleporter.getTeleporterForDim(minecraftserver, dimensionIn); // TF - custom teleporter
					//teleporter.placeInExistingPortal(toTeleport, f);
					blockpos = new BlockPos(toTeleport);
				}

				worldserver.updateEntityWithOptionalForce(toTeleport, false);
				toTeleport.world.profiler.endStartSection("reloading");
				Entity entity = EntityList.newEntity(toTeleport.getClass(), worldserver1);

				if (entity != null) {
					entity.copyDataFromOld(toTeleport);

					if (currentDimensionId == 1 && dimensionIn == 1) {
						BlockPos blockpos1 = worldserver1.getTopSolidOrLiquidBlock(worldserver1.getSpawnPoint());
						entity.moveToBlockPosAndAngles(blockpos1, entity.rotationYaw, entity.rotationPitch);
					} else {
						// TF - inline moveToBlockPosAndAngles without +0.5 offsets, since teleporter already took care of it
						entity.setLocationAndAngles((double) blockpos.getX(), (double) blockpos.getY(), (double) blockpos.getZ(), entity.rotationYaw, entity.rotationPitch);
					}

					boolean flag = entity.forceSpawn;
					entity.forceSpawn = true;
					worldserver1.spawnEntity(entity);
					entity.forceSpawn = flag;
					worldserver1.updateEntityWithOptionalForce(entity, false);
				}

				toTeleport.isDead = true;
				toTeleport.world.profiler.endSection();
				worldserver.resetUpdateEntityTick();
				worldserver1.resetUpdateEntityTick();
				toTeleport.world.profiler.endSection();
			}
		}
	}
}
