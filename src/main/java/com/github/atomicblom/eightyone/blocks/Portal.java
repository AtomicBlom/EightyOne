package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.EightyOneTeleporter;
import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import java.util.Random;

public class Portal extends Block
{
	public Portal()
	{
		super(Material.ROCK, MapColor.GRAY);
		setTickRandomly(true);
	}

	@Override
	public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random)
	{
		CheckValidStructure(worldIn, pos);
	}

	int[][] cornerOffsets = {
			{-4, 0, -4},
			{4, 0, -4},
			{-4, 0, 4},
			{4, 0, 4},
			{-4, 5, -4},
			{4, 5, -4},
			{-4, 5, 4},
			{4, 5, 4}
	};

	@Override
	public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand)
	{
		if (CheckValidStructure(worldIn, pos)) return;

		pos = pos.down(2);

		worldIn.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() - 4 + 0.5, pos.getY() + 1, pos.getZ() - 4 + 0.5, 0, 0.01, 0);
		worldIn.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() + 4 + 0.5, pos.getY() + 1, pos.getZ() - 4 + 0.5, 0, 0.01, 0);
		worldIn.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() - 4 + 0.5, pos.getY() + 1, pos.getZ() + 4 + 0.5, 0, 0.01, 0);
		worldIn.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() + 4 + 0.5, pos.getY() + 1, pos.getZ() + 4 + 0.5, 0, 0.01, 0);

		worldIn.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() - 4 + 0.5, pos.getY() + 5 + 1, pos.getZ() - 4 + 0.5, 0, 0.01, 0);
		worldIn.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() + 4 + 0.5, pos.getY() + 5 + 1, pos.getZ() - 4 + 0.5, 0, 0.01, 0);
		worldIn.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() - 4 + 0.5, pos.getY() + 5 + 1, pos.getZ() + 4 + 0.5, 0, 0.01, 0);
		worldIn.spawnParticle(EnumParticleTypes.PORTAL, pos.getX() + 4 + 0.5, pos.getY() + 5 + 1, pos.getZ() + 4 + 0.5, 0, 0.01, 0);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if (!CheckValidStructure(worldIn, pos)) {
			return false;
		}

		if (!player.isRiding() && player.getPassengers().isEmpty() && player.timeUntilPortal <= 0) {
			if (player instanceof EntityPlayerMP) {
				EntityPlayerMP playerMP = (EntityPlayerMP) player;

				if (playerMP.timeUntilPortal > 0) {
					// do not switch dimensions if the player has any time on this thinger
					playerMP.timeUntilPortal = 10;
				} else {

					// send to twilight
					if (playerMP.dimension != Reference.DIMENSION_ID) {
						if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(playerMP, Reference.DIMENSION_ID)) return false;

						//PlayerHelper.grantAdvancement(playerMP, new ResourceLocation(TwilightForestMod.ID, "twilight_portal"));
						Logger.info("Player touched the portal block.  Sending the player to dimension {}", Reference.DIMENSION_ID);

						playerMP.mcServer.getPlayerList().transferPlayerToDimension(playerMP, Reference.DIMENSION_ID, EightyOneTeleporter.getTeleporterForDim(playerMP.mcServer, Reference.DIMENSION_ID));

						// set respawn point for TF dimension to near the arrival portal
						playerMP.setSpawnChunk(new BlockPos(playerMP), true, Reference.DIMENSION_ID);
					} else {
						if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(playerMP, 0)) return false;

						playerMP.mcServer.getPlayerList().transferPlayerToDimension(playerMP, 0, EightyOneTeleporter.getTeleporterForDim(playerMP.mcServer, 0));
					}
				}
			} else {
				if (player.dimension != Reference.DIMENSION_ID) {
					changeDimension(player, Reference.DIMENSION_ID);
				} else {
					changeDimension(player, 0);
				}
			}
		}
		return true;
	}

	/**
	 * [VanillaCopy] Entity.changeDimension. Relevant edits noted.
	 * `this` -> `toTeleport`
	 * return value Entity -> void
	 */
	@SuppressWarnings("unused")
	private void changeDimension(Entity toTeleport, int dimensionIn) {
		if (!toTeleport.world.isRemote && !toTeleport.isDead) {
			if (!net.minecraftforge.common.ForgeHooks.onTravelToDimension(toTeleport, dimensionIn)) return;
			toTeleport.world.profiler.startSection("changeDimension");
			MinecraftServer minecraftserver = toTeleport.getServer();
			int i = toTeleport.dimension;
			WorldServer worldserver = minecraftserver.getWorld(i);
			WorldServer worldserver1 = minecraftserver.getWorld(dimensionIn);
			toTeleport.dimension = dimensionIn;

			if (i == 1 && dimensionIn == 1) {
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

				d0 = MathHelper.clamp((int) d0, -29999872, 29999872);
				d1 = MathHelper.clamp((int) d1, -29999872, 29999872);
				float f = toTeleport.rotationYaw;
				toTeleport.setLocationAndAngles(d0, toTeleport.posY, d1, 90.0F, 0.0F);
				Teleporter teleporter = EightyOneTeleporter.getTeleporterForDim(minecraftserver, dimensionIn); // TF - custom teleporter
				teleporter.placeInExistingPortal(toTeleport, f);
				blockpos = new BlockPos(toTeleport);
			}

			worldserver.updateEntityWithOptionalForce(toTeleport, false);
			toTeleport.world.profiler.endStartSection("reloading");
			Entity entity = EntityList.newEntity(toTeleport.getClass(), worldserver1);

			if (entity != null) {
				entity.copyDataFromOld(toTeleport);

				if (i == 1 && dimensionIn == 1) {
					BlockPos blockpos1 = worldserver1.getTopSolidOrLiquidBlock(worldserver1.getSpawnPoint());
					entity.moveToBlockPosAndAngles(blockpos1, entity.rotationYaw, entity.rotationPitch);
				} else {
					// TF - inline moveToBlockPosAndAngles without +0.5 offsets, since teleporter already took care of it
					entity.setLocationAndAngles(blockpos.getX(), blockpos.getY(), blockpos.getZ(), entity.rotationYaw, entity.rotationPitch);
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

	private boolean CheckValidStructure(World worldIn, BlockPos pos)
	{
		Block checkBlock;
		pos = pos.down();
		checkBlock = worldIn.getBlockState(pos).getBlock();
		if (checkBlock == Blocks.AIR) { return false; }

		pos = pos.down();

		Block cornerBlock = null;
		Block topRowBlock = null;
		Block bottomRowBlock = null;
		Block columnBlock = null;

		for (final int[] offset : cornerOffsets)
		{
			checkBlock = worldIn.getBlockState(pos.add(offset[0], offset[1], offset[2])).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (cornerBlock == null) {
				cornerBlock = checkBlock;
			} else if (checkBlock != cornerBlock) {
				return false;
			}
		}

		for (int i = 1; i <= 4; ++i) {
			checkBlock = worldIn.getBlockState(pos.add(-4, i, -4)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (columnBlock == null)
			{
				columnBlock = checkBlock;
			} else if (checkBlock != columnBlock)
			{
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(4, i, -4)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != columnBlock)
			{
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(4, i, 4)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != columnBlock)
			{
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(-4, i, 4)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != columnBlock)
			{
				return false;
			}
		}

		for (int i = -3; i <= 3; ++i) {
			checkBlock = worldIn.getBlockState(pos.add(i, 0, -4)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (bottomRowBlock == null) {
				bottomRowBlock = checkBlock;
			} else if (checkBlock != bottomRowBlock) {
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(i, 0, 4)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != bottomRowBlock) {
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(4, 0, i)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != bottomRowBlock) {
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(-4, 0, i)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != bottomRowBlock) {
				return false;
			}

			checkBlock = worldIn.getBlockState(pos.add(i, 5, -4)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (topRowBlock == null) {
				topRowBlock = checkBlock;
			} else if (checkBlock != topRowBlock) {
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(i, 5, 4)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != topRowBlock) {
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(4, 5, i)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != topRowBlock) {
				return false;
			}
			checkBlock = worldIn.getBlockState(pos.add(-4, 5, i)).getBlock();
			if (checkBlock == Blocks.AIR) { return false; }
			if (checkBlock != topRowBlock) {
				return false;
			}
		}

		return true;
	}
}
