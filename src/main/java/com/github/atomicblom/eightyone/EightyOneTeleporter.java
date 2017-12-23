package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.util.SpiralIterable;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import com.github.atomicblom.eightyone.world.NxNChunkGenerator;
import com.github.atomicblom.eightyone.world.NxNWorldProvider;
import com.github.atomicblom.eightyone.world.Room;
import com.github.atomicblom.eightyone.world.structure.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;

import java.util.Arrays;

public class EightyOneTeleporter extends Teleporter {
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
    public boolean makePortal(Entity entityIn) {
		Logger.info("makePortal: %s", entityIn);

		final WorldProvider provider = entityIn.getEntityWorld().provider;
        BlockPos position = entityIn.getPosition().down();
        if (provider instanceof NxNWorldProvider) {
            final NxNChunkGenerator chunkGenerator = (NxNChunkGenerator) provider.createChunkGenerator();
            final Room startingRoom = chunkGenerator.getRoomAt(position.getX(), position.getZ());
            final BlockPos startPos = new BlockPos(startingRoom.getX(), chunkGenerator.getFloorHeight(), startingRoom.getZ());
            for (final BlockPos pos : new SpiralIterable(startPos, 10) ) {
                final Room spawnRoom = chunkGenerator.getRoomAt(pos.getX(), pos.getZ());
                Logger.info("makePortal: Checking if room present at %d,%d", spawnRoom.getX(), spawnRoom.getZ());
                if (spawnRoom.isPresent()) {
					int height = entityIn.world.getHeight(pos.getX(), pos.getZ());
					position = new BlockPos(pos.getX(), height, pos.getZ());



                    renderPortalToWorld(entityIn.world, position, false, spawnRoom.getCharacteristics());
                    return true;
                }
            }
        } else {
        	Logger.info("makePortal: Creating portal at player location");
			int height = entityIn.world.getHeight(position.getX() + 1, position.getZ() + 1);
            position = new BlockPos(position.getX() + 1, height, position.getZ() + 1);

            TemplateCharacteristics templateCharacteristics = new TemplateCharacteristics(Shape.Straight, Arrays.asList(net.minecraft.util.Rotation.values()));
            renderPortalToWorld(entityIn.world, position, true, templateCharacteristics);
            return true;
        }
        return false;
    }

    private void renderPortalToWorld(World world, BlockPos blockPos, boolean fillUnderneath, TemplateCharacteristics characteristics) {
		Logger.info("renderPortalToWorld: %s fillUnderneath: %s", blockPos, fillUnderneath);
	    double chance = world.rand.nextDouble();
	    final RoomTemplate spawn = TemplateManager.getTemplateByChance(characteristics, chance, RoomPurpose.PORTAL);
        final PlacementSettings placementSettings = new PlacementSettings();
        if (spawn == null) {
        	Logger.severe("Could not load portal template!");
        }
        final NxNTemplate template = spawn.getTemplate();
        template.addBlocksToWorld(world, blockPos, placementSettings);

        if (fillUnderneath)
        {
            final BlockPos size = template.getSize();
            for (final MutableBlockPos pos : BlockPos.getAllInBoxMutable(blockPos, blockPos.add(size)))
            {
                final IBlockState blockStateToUse = world.getBlockState(pos);
                final MutableBlockPos fillPos = new MutableBlockPos(pos.down());
                while (fillPos.getY() >= 0 && world.getBlockState(fillPos).getBlock() == Blocks.AIR)
                {
                    world.setBlockState(fillPos, blockStateToUse);
                    fillPos.setY(fillPos.getY() - 1);
                }
            }
        }
    }

	/**
	 * Finds a portal near the entity's current coordinates and places the entity there, creating it if necessary.
	 */
	public void placeInPortal(Entity entityIn, float rotationYaw)
	{
		Logger.info("placeInPortal: %s", entityIn);
		if (world.provider.getDimensionType().getId() != 1)
		{
			Logger.info("placeInPortal: world is not the overworld");
			if (!placeInExistingPortal(entityIn, rotationYaw))
			{
				Logger.info("placeInPortal: placeInExistingPortal failed, attempting to make a new portal");
				makePortal(entityIn);
				placeInExistingPortal(entityIn, rotationYaw);
			}
		}
		else
		{
			Logger.info("placeInPortal: world is the overworld");
			final int i = MathHelper.floor(entityIn.posX);
			final int j = MathHelper.floor(entityIn.posY) - 1;
			final int k = MathHelper.floor(entityIn.posZ);
			final int l = 1;
			final int i1 = 0;

			for (int j1 = -2; j1 <= 2; ++j1)
			{
				for (int k1 = -2; k1 <= 2; ++k1)
				{
					for (int l1 = -1; l1 < 3; ++l1)
					{
						final int i2 = i + k1 * 1 + j1 * 0;
						final int j2 = j + l1;
						final int k2 = k + k1 * 0 - j1 * 1;
						final boolean flag = l1 < 0;
						world.setBlockState(new BlockPos(i2, j2, k2), flag ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
					}
				}
			}

			entityIn.setLocationAndAngles(i, j, k, entityIn.rotationYaw, 0.0F);
			entityIn.motionX = 0.0D;
			entityIn.motionY = 0.0D;
			entityIn.motionZ = 0.0D;
		}
	}

	public boolean placeInExistingPortal(Entity entityIn, float rotationYaw)
	{
		Logger.info("placeInExistingPortal: %s", entityIn);
		final int scanDistance = 128;
		double currentDistance = -1.0D;
		final int entityX = MathHelper.floor(entityIn.posX);
		final int entityZ = MathHelper.floor(entityIn.posZ);
		boolean flag = true;
		BlockPos blockpos = BlockPos.ORIGIN;
		final long chunkId = ChunkPos.asLong(entityX, entityZ);

		if (destinationCoordinateCache.containsKey(chunkId))
		{
			final PortalPosition portalPosition = destinationCoordinateCache.get(chunkId);
			Logger.info("Portal Position identified from cache %s", portalPosition);
			currentDistance = 0.0D;
			blockpos = portalPosition;
			portalPosition.lastUpdateTime = world.getTotalWorldTime();
			flag = false;
		}
		else
		{
			final BlockPos entityPosition = new BlockPos(entityIn);

			Logger.info("Scanning for portal block at %s +/-%d", entityPosition, scanDistance);

			for (int scanX = -scanDistance; scanX <= scanDistance; ++scanX)
			{

				for (int scanZ = -scanDistance; scanZ <= scanDistance; ++scanZ)
				{
					BlockPos scanPos;
					for (BlockPos selectedPos = entityPosition.add(scanX, world.getActualHeight() - 1 - entityPosition.getY(), scanZ); selectedPos.getY() >= 0; selectedPos = scanPos)
					{
						scanPos = selectedPos.down();

						if (world.getBlockState(selectedPos).getBlock() == BlockLibrary.portal)
						{
//							for (scanPos = selectedPos.down(); world.getBlockState(scanPos).getBlock() == BlockLibrary.portal; scanPos = scanPos.down())
//							{
//								selectedPos = scanPos;
//							}

							final double distanceToEntity = selectedPos.distanceSq(entityPosition);

							if (currentDistance < 0.0D || distanceToEntity < currentDistance)
							{
								currentDistance = distanceToEntity;
								blockpos = selectedPos;
							}
						}
					}
				}
			}
		}

		Logger.info("Distance to portal calculated as %f", currentDistance);

		if (currentDistance >= 0.0D)
		{
			if (flag)
			{
				destinationCoordinateCache.put(chunkId, new PortalPosition(blockpos, world.getTotalWorldTime()));
			}

			double newX = blockpos.getX() + 0.5D;
			double newZ = blockpos.getZ() + 0.5D;
			double newY = blockpos.getY();

			Logger.info("Player location set to %s", blockpos);

			if (entityIn instanceof EntityPlayerMP)
			{
				((EntityPlayerMP)entityIn).connection.setPlayerLocation(newX, newY, newZ, entityIn.rotationYaw, entityIn.rotationPitch);
			}
			else
			{
				entityIn.setLocationAndAngles(newX, newY, newZ, entityIn.rotationYaw, entityIn.rotationPitch);
			}

			return true;
		}
		else
		{
			return false;
		}
	}

}
