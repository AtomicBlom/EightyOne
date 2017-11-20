package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.world.structure.TemplateAndProperties;
import com.github.atomicblom.eightyone.world.structure.TemplateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;

public class EightyOneTeleporter extends Teleporter
{
	public static EightyOneTeleporter getTeleporterForDim(MinecraftServer server, int dim) {
		final WorldServer ws = server.getWorld(dim);

		for (final Teleporter t : ws.customTeleporters) {
			if (t instanceof EightyOneTeleporter) {
				return (EightyOneTeleporter) t;
			}
		}

		final EightyOneTeleporter tp = new EightyOneTeleporter(ws);
		ws.customTeleporters.add(tp);
		return tp;
	}

	private EightyOneTeleporter(WorldServer dest) {
		super(dest);
	}

	@Override
	public void placeInPortal(Entity par1Entity, float facing) {
		if (!placeInExistingPortal(par1Entity, facing)) {
			makePortal(par1Entity);
			placeInExistingPortal(par1Entity, facing);
		}
	}

	// [VanillaCopy] copy of super, edits noted
	@Override
	public boolean placeInExistingPortal(Entity entity, float rotationYaw) {
		final int scanRadius = 200; // TF - scan radius up to 200, and also un-inline this variable back into below
		double distance = -1.0D;
		final int j = MathHelper.floor(entity.posX);
		final int k = MathHelper.floor(entity.posZ);
		boolean flag = true;
		BlockPos blockpos = BlockPos.ORIGIN;
		final long l = ChunkPos.asLong(j, k);

		if (destinationCoordinateCache.containsKey(l)) {
			final PortalPosition portalPosition = destinationCoordinateCache.get(l);
			distance = 0.0D;
			blockpos = portalPosition;
			portalPosition.lastUpdateTime = world.getTotalWorldTime();
			flag = false;
		} else {
			final BlockPos entityPosition = new BlockPos(entity);

			for (int x = -scanRadius; x <= scanRadius; ++x) {
				BlockPos scanLocation;

				for (int z = -scanRadius; z <= scanRadius; ++z) {
					BlockPos blockpos1 = entityPosition.add(x, world.getActualHeight() - 1 - entityPosition.getY(), z);
					while (blockpos1.getY() >= 0)
					{
						scanLocation = blockpos1.down();

						// TF - use our portal block
						if (world.getBlockState(blockpos1).getBlock() == BlockLibrary.portal) {
							scanLocation = blockpos1.down();
							while (world.getBlockState(scanLocation).getBlock() == BlockLibrary.portal)
							{
								blockpos1 = scanLocation;
								scanLocation = scanLocation.down();
							}

							final double d1 = blockpos1.distanceSq(entityPosition);

							if (distance < 0.0D || d1 < distance) {
								distance = d1;
								blockpos = blockpos1;
							}
						}
						blockpos1 = scanLocation;
					}
				}
			}
		}

		if (distance >= 0.0D) {
			if (flag) {
				destinationCoordinateCache.put(l, new PortalPosition(blockpos, world.getTotalWorldTime()));
			}

			// TF - replace with our own placement logic
			double portalX = blockpos.getX() + 0.5D;
			final double portalY = blockpos.getY() + 0.5D;
			double portalZ = blockpos.getZ() + 0.5D;
			if (isBlockPortal(world, blockpos.west())) {
				portalX -= 0.5D;
			}
			if (isBlockPortal(world, blockpos.east())) {
				portalX += 0.5D;
			}
			if (isBlockPortal(world, blockpos.north())) {
				portalZ -= 0.5D;
			}
			if (isBlockPortal(world, blockpos.south())) {
				portalZ += 0.5D;
			}
			int xOffset = 0;
			int zOffset = 0;
			while (xOffset == zOffset && xOffset == 0) {
				xOffset = random.nextInt(3) - random.nextInt(3);
				zOffset = random.nextInt(3) - random.nextInt(3);
			}

			entity.motionX = entity.motionY = entity.motionZ = 0.0D;

			if (entity instanceof EntityPlayerMP) {
				((EntityPlayerMP) entity).connection.setPlayerLocation(portalX + xOffset, portalY + 1, portalZ + zOffset, entity.rotationYaw, entity.rotationPitch);
			} else {
				entity.setLocationAndAngles(portalX + xOffset, portalY + 1, portalZ + zOffset, entity.rotationYaw, entity.rotationPitch);
			}

			return true;
		} else {
			return false;
		}
	}

	private boolean isBlockPortal(World world, BlockPos pos) {
		return world.getBlockState(pos).getBlock() == TFBlocks.portal;
	}

	@Override
	public boolean makePortal(Entity entity) {
		BlockPos spot = findPortalCoords(entity, true);

		if (spot != null) {
			Logger.trace("Found ideal portal spot");
			makePortalAt(world, spot);
			return true;
		}

		Logger.trace("Did not find ideal portal spot, shooting for okay one");
		spot = findPortalCoords(entity, false);
		if (spot != null) {
			Logger.trace("Found okay portal spot");
			makePortalAt(world, spot);
			return true;
		}

		// well I don't think we can actally just return false and fail here
		Logger.trace("Did not even find an okay portal spot, just making a random one");

		// adjust the portal height based on what world we're traveling to
		final double yFactor = world.provider.getDimension() == 0 ? 2 : 0.5;
		// modified copy of base Teleporter method:
		makePortalAt(world, new BlockPos(entity.posX, entity.posY * yFactor, entity.posZ));

		return false;
	}

	private BlockPos findPortalCoords(Entity entity, boolean ideal) {
		// adjust the portal height based on what world we're traveling to
		final double yFactor = world.provider.getDimension() == 0 ? 2 : 0.5;
		// modified copy of base Teleporter method:
		final int entityX = MathHelper.floor(entity.posX);
		final int entityZ = MathHelper.floor(entity.posZ);

		double spotWeight = -1.0D;

		BlockPos spot = null;

		final byte range = 16;
		for (int rx = entityX - range; rx <= entityX + range; rx++) {
			final double xWeight = (rx + 0.5D) - entity.posX;
			for (int rz = entityZ - range; rz <= entityZ + range; rz++) {
				final double zWeight = (rz + 0.5D) - entity.posZ;

				for (int ry = 128 - 1; ry >= 0; ry--) {
					BlockPos pos = new BlockPos(rx, ry, rz);

					if (!world.isAirBlock(pos)) {
						continue;
					}

					while (pos.getY() > 0 && world.isAirBlock(pos.down())) pos = pos.down();

					if (ideal ? isIdealPortal(pos) : isOkayPortal(pos)) {
						final double yWeight = (pos.getY() + 0.5D) - entity.posY * yFactor;
						final double rPosWeight = xWeight * xWeight + yWeight * yWeight + zWeight * zWeight;


						if (spotWeight < 0.0D || rPosWeight < spotWeight) {
							spotWeight = rPosWeight;
							spot = pos;
						}
					}
				}
			}
		}

		return spot;
	}

	private void makePortalAt(World world, BlockPos pos) {
		final TemplateAndProperties spawnTemplate = TemplateManager.getTemplateByName("portal_spawn");
		final PlacementSettings placementIn = new PlacementSettings();
		spawnTemplate.getTemplate().addBlocksToWorld(world, pos, placementIn);

	}
}

