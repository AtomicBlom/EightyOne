package com.github.atomicblom.eightyone.blocks;

import com.github.atomicblom.eightyone.EightyOneTeleporter;
import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityPortal;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import javax.annotation.Nullable;
import java.util.Random;

public class PortalBlock extends Block implements ITileEntityProvider
{
	public PortalBlock()
	{
		super(Material.ROCK, MapColor.GRAY);
		setTickRandomly(true);
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		final TileEntity tileEntity = worldIn.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntityPortal))
		{
			return false;
		}
		TileEntityPortal tileEntityPortal = (TileEntityPortal)tileEntity;

		if (player.isSneaking()) return false;

		if (!player.isRiding() && player.getPassengers().isEmpty() && player.timeUntilPortal <= 0) {
			if (!tileEntityPortal.checkValidStructure()) {
				return false;
			}

			if (player instanceof EntityPlayerMP) {
				final EntityPlayerMP playerMP = (EntityPlayerMP) player;

				if (playerMP.timeUntilPortal > 0) {
					// do not switch dimensions if the player has any time on this thinger
					playerMP.timeUntilPortal = 10;
				} else {

					// send to the labyrinth
					if (playerMP.dimension != Reference.DIMENSION_ID) {
						if (!ForgeHooks.onTravelToDimension(playerMP, Reference.DIMENSION_ID)) return false;

						Logger.info("Player touched the portal block.  Sending the player to dimension {}", Reference.DIMENSION_ID);

						playerMP.mcServer.getPlayerList().transferPlayerToDimension(playerMP, Reference.DIMENSION_ID, EightyOneTeleporter.getTeleporterForDim(playerMP.mcServer, Reference.DIMENSION_ID));

						playerMP.setSpawnChunk(new BlockPos(playerMP), true, Reference.DIMENSION_ID);
					} else {
						if (!ForgeHooks.onTravelToDimension(playerMP, 0)) return false;

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
	//@SuppressWarnings("unused")
	private void changeDimension(Entity entityToTeleport, int newDimensionId) {
		if (!entityToTeleport.world.isRemote && !entityToTeleport.isDead) {
			if (!ForgeHooks.onTravelToDimension(entityToTeleport, newDimensionId)) return;
			entityToTeleport.world.profiler.startSection("changeDimension");
			final MinecraftServer minecraftserver = entityToTeleport.getServer();
			final int currentDimensionId = entityToTeleport.dimension;
			final WorldServer currentDimensionWorldServer = minecraftserver.getWorld(currentDimensionId);
			WorldServer newDimensionWorldServer = minecraftserver.getWorld(newDimensionId);
			entityToTeleport.dimension = newDimensionId;

			if (currentDimensionId == 1 && newDimensionId == 1) {
				newDimensionWorldServer = minecraftserver.getWorld(0);
				entityToTeleport.dimension = 0;
			}

			entityToTeleport.world.removeEntity(entityToTeleport);
			entityToTeleport.isDead = false;
			entityToTeleport.world.profiler.startSection("reposition");
			final BlockPos blockpos;

			if (newDimensionId == 1) {
				blockpos = newDimensionWorldServer.getSpawnCoordinate();
			} else {
				double posX = entityToTeleport.posX;
				double posZ = entityToTeleport.posZ;

				// Tf - remove 8x scaling for nether
				posX = MathHelper.clamp(posX, newDimensionWorldServer.getWorldBorder().minX() + 16.0D, newDimensionWorldServer.getWorldBorder().maxX() - 16.0D);
				posZ = MathHelper.clamp(posZ, newDimensionWorldServer.getWorldBorder().minZ() + 16.0D, newDimensionWorldServer.getWorldBorder().maxZ() - 16.0D);

				posX = MathHelper.clamp((int) posX, -29999872, 29999872);
				posZ = MathHelper.clamp((int) posZ, -29999872, 29999872);
				final float f = entityToTeleport.rotationYaw;
				entityToTeleport.setLocationAndAngles(posX, entityToTeleport.posY, posZ, 90.0F, 0.0F);
				final Teleporter teleporter = EightyOneTeleporter.getTeleporterForDim(minecraftserver, newDimensionId); // TF - custom teleporter
				teleporter.placeInExistingPortal(entityToTeleport, f);
				blockpos = new BlockPos(entityToTeleport);
			}

			currentDimensionWorldServer.updateEntityWithOptionalForce(entityToTeleport, false);
			entityToTeleport.world.profiler.endStartSection("reloading");
			final Entity entity = EntityList.newEntity(entityToTeleport.getClass(), newDimensionWorldServer);

			if (entity != null) {
				entity.copyDataFromOld(entityToTeleport);

				if (currentDimensionId == 1 && newDimensionId == 1) {
					final BlockPos blockpos1 = newDimensionWorldServer.getTopSolidOrLiquidBlock(newDimensionWorldServer.getSpawnPoint());
					entity.moveToBlockPosAndAngles(blockpos1, entity.rotationYaw, entity.rotationPitch);
				} else {
					entity.setLocationAndAngles(blockpos.getX(), blockpos.getY(), blockpos.getZ(), entity.rotationYaw, entity.rotationPitch);
				}

				final boolean flag = entity.forceSpawn;
				entity.forceSpawn = true;
				newDimensionWorldServer.spawnEntity(entity);
				entity.forceSpawn = flag;
				newDimensionWorldServer.updateEntityWithOptionalForce(entity, false);
			}

			entityToTeleport.isDead = true;
			entityToTeleport.world.profiler.endSection();
			currentDimensionWorldServer.resetUpdateEntityTick();
			newDimensionWorldServer.resetUpdateEntityTick();
			entityToTeleport.world.profiler.endSection();
		}
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}

	@Nullable
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta)
	{
		return new TileEntityPortal();
	}

	@Override
	@Deprecated
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer)
	{
		Reference.CURRENT_RENDER_LAYER = layer;
		return (layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.TRANSLUCENT);
	}

	@Override
	@Deprecated
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	@Override
	@Deprecated
	public boolean isFullBlock(IBlockState state)
	{
		return false;
	}
}
