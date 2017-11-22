package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.world.NxNChunkGenerator;
import com.github.atomicblom.eightyone.world.NxNWorldProvider;
import com.github.atomicblom.eightyone.world.Room;
import com.github.atomicblom.eightyone.world.RoomProperties;
import com.github.atomicblom.eightyone.world.structure.NxNTemplate;
import com.github.atomicblom.eightyone.world.structure.TemplateManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

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
        final WorldProvider provider = entityIn.getEntityWorld().provider;
        BlockPos position = entityIn.getPosition().down();
        if (provider instanceof NxNWorldProvider) {
            final NxNChunkGenerator chunkGenerator = (NxNChunkGenerator) provider.createChunkGenerator();
            final Room startingRoom = chunkGenerator.getRoomAt(position.getX(), position.getZ());
            final BlockPos startPos = new BlockPos(startingRoom.getX(), chunkGenerator.getFloorHeight(), startingRoom.getZ());
            for (final BlockPos pos : new SpiralIterable(startPos, 10) ) {
                final Room spawnRoom = chunkGenerator.getRoomAt(pos.getX(), pos.getZ());
                if (spawnRoom.hasProperty(RoomProperties.IsPresent)) {
                    makePortal(entityIn.world, new BlockPos(pos), false);
                    return true;
                }
            }
        } else {
            position = position.add(1, 0, 1);
            makePortal(entityIn.world, position, true);
            return true;
        }
        return false;
    }

    private void makePortal(World world, BlockPos blockPos, boolean fillUnderneath) {
        final NxNTemplate spawn = TemplateManager.getTemplateByName("spawn");
        final PlacementSettings placementSettings = new PlacementSettings();
        final Template template = spawn;
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
                    fillPos.offset(EnumFacing.DOWN);
                }
            }
        }
    }

	/**
	 * Finds a portal near the entity's current coordinates and places the entity there, creating it if necessary.
	 */
	public void placeInPortal(Entity entityIn, float rotationYaw)
	{
		if (world.provider.getDimensionType().getId() != 1)
		{
			if (!placeInExistingPortal(entityIn, rotationYaw))
			{
				makePortal(entityIn);
				placeInExistingPortal(entityIn, rotationYaw);
			}
		}
		else
		{
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
			currentDistance = 0.0D;
			blockpos = portalPosition;
			portalPosition.lastUpdateTime = world.getTotalWorldTime();
			flag = false;
		}
		else
		{
			final BlockPos entityPosition = new BlockPos(entityIn);

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
							for (scanPos = selectedPos.down(); world.getBlockState(scanPos).getBlock() == BlockLibrary.portal; scanPos = scanPos.down())
							{
								selectedPos = scanPos;
							}

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

		if (currentDistance >= 0.0D)
		{
			if (flag)
			{
				destinationCoordinateCache.put(chunkId, new PortalPosition(blockpos, world.getTotalWorldTime()));
			}

			double d5 = blockpos.getX() + 0.5D;
			double d7 = blockpos.getZ() + 0.5D;
			double d6 = blockpos.getY();
			/*final PatternHelper patternHelper = Blocks.PORTAL.createPatternHelper(world, blockpos);
			final boolean flag1 = patternHelper.getForwards().rotateY().getAxisDirection() == AxisDirection.NEGATIVE;
			double d2 = patternHelper.getForwards().getAxis() == Axis.X ? patternHelper.getFrontTopLeft().getZ() : patternHelper.getFrontTopLeft().getX();
			final double d6 = (patternHelper.getFrontTopLeft().getY() + 1) - entityIn.getLastPortalVec().y * patternHelper.getHeight();

			if (flag1)
			{
				++d2;
			}

			if (patternHelper.getForwards().getAxis() == Axis.X)
			{
				d7 = d2 + (1.0D - entityIn.getLastPortalVec().x) * patternHelper.getWidth() * patternHelper.getForwards().rotateY().getAxisDirection().getOffset();
			}
			else
			{
				d5 = d2 + (1.0D - entityIn.getLastPortalVec().x) * patternHelper.getWidth() * patternHelper.getForwards().rotateY().getAxisDirection().getOffset();
			}

			float f = 0.0F;
			float f1 = 0.0F;
			float f2 = 0.0F;
			float f3 = 0.0F;

			if (patternHelper.getForwards().getOpposite() == entityIn.getTeleportDirection())
			{
				f = 1.0F;
				f1 = 1.0F;
			}
			else if (patternHelper.getForwards().getOpposite() == entityIn.getTeleportDirection().getOpposite())
			{
				f = -1.0F;
				f1 = -1.0F;
			}
			else if (patternHelper.getForwards().getOpposite() == entityIn.getTeleportDirection().rotateY())
			{
				f2 = 1.0F;
				f3 = -1.0F;
			}
			else
			{
				f2 = -1.0F;
				f3 = 1.0F;
			}

			final double d3 = entityIn.motionX;
			final double d4 = entityIn.motionZ;
			entityIn.motionX = d3 * f + d4 * f3;
			entityIn.motionZ = d3 * f2 + d4 * f1;
			entityIn.rotationYaw = rotationYaw - (entityIn.getTeleportDirection().getOpposite().getHorizontalIndex() * 90) + (patternHelper.getForwards().getHorizontalIndex() * 90);
			*/
			if (entityIn instanceof EntityPlayerMP)
			{
				((EntityPlayerMP)entityIn).connection.setPlayerLocation(d5, d6, d7, entityIn.rotationYaw, entityIn.rotationPitch);
			}
			else
			{
				entityIn.setLocationAndAngles(d5, d6, d7, entityIn.rotationYaw, entityIn.rotationPitch);
			}

			return true;
		}
		else
		{
			return false;
		}
	}

}
