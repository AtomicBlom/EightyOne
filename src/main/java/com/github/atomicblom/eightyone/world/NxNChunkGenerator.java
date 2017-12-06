package com.github.atomicblom.eightyone.world;

import com.github.atomicblom.eightyone.util.EntranceHelper;
import com.github.atomicblom.eightyone.util.Point2D;
import com.github.atomicblom.eightyone.world.structure.NxNTemplate;
import com.github.atomicblom.eightyone.world.structure.RoomTemplate;
import com.github.atomicblom.eightyone.world.structure.TemplateManager;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NxNChunkGenerator implements IChunkGenerator
{
	private static final int ROOM_SET_SIZE = 5;
	private static final int BASE_HEIGHT = 32;
	private final World world;
	private final long seed;
	private final boolean mapFeaturesEnabled;
	private final Random rand;
	private final NoiseGeneratorSimplex noiseGen;
	private int roomId = 0;
	private final LoadingCache<Point2D, Room> roomCache;

	public NxNChunkGenerator(World world, long seed, boolean mapFeaturesEnabled)
	{
		this.world = world;
		this.seed = seed;
		this.mapFeaturesEnabled = mapFeaturesEnabled;
		this.rand = new Random(seed);

		this.noiseGen = new NoiseGeneratorSimplex(rand);

		this.roomCache = CacheBuilder.newBuilder()
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.maximumSize(1000)
				.build(new Point2DRoomCacheLoader());
	}

	@Override
	public Chunk generateChunk(int x, int z)
	{
		rand.setSeed(x * 0x4f9939f508L + z * 0x1ef1565bd5L);
		final ChunkPrimer primer = new ChunkPrimer();

		generateChunk(x, z, primer);

		final Chunk chunk = new Chunk(world, primer, x, z);

		chunk.generateSkylightMap();
		return chunk;
	}

	public Room getRoomAt(int x, int z) {
		int roomX = x / 10 * 10;
		if (x < 0 && x % 10 != 0) roomX -= 10;
		int roomZ = z / 10 * 10;
		if (z < 0 && z % 10 != 0) roomZ -= 10;
		final Point2D point2D = new Point2D(roomX, roomZ);
		try
		{
			return roomCache.get(point2D);
		} catch (final ExecutionException e) {
			return null;
		}
	}

	private void generateChunk(int chunkX, int chunkZ, ChunkPrimer primer)
	{
		final boolean renderRoof = true;
		final int height = 6;
		final int posX = chunkX << 4;
		final int posZ = chunkZ << 4;

		for (int z = 0; z < 16; ++z)
		{
			for (int x = 0; x < 16; ++x)
			{
				final Room room = getRoomAt(posX + x, posZ + z);
				if (!room.isPresent()) continue;

				//final RoomTemplate template;
				//template = TemplateManager.getTemplateByChance(room.getCharacteristics(), room.getTemplateChance());
				final RoomTemplate template = room.getTemplate();
				if (template != null && template.getTemplate().isCustomRoom()) continue;

				if (room.contains(posX + x, posZ + z))
				{
					primer.setBlockState(x, BASE_HEIGHT, z, Blocks.COBBLESTONE.getDefaultState());

					final int xOffset = room.getXOffset(posX + x);
					final int zOffset = room.getZOffset(posZ + z);

					final int roomHeight = BASE_HEIGHT + template.getTemplate().getHeight() + 1;
					if (renderRoof)
					{
						if ((xOffset == 2 || xOffset == 3 || xOffset == 5 || xOffset == 6) &&
								(zOffset == 2 || zOffset == 3 || zOffset == 5 || zOffset == 6))
						{
							primer.setBlockState(x, roomHeight, z, Blocks.GLASS.getDefaultState());
						} else
						{
							primer.setBlockState(x, roomHeight, z, Blocks.COBBLESTONE.getDefaultState());
						}
					}

					if (room.isWall(posX + x, posZ + z))
					{
						for (int y = BASE_HEIGHT; y < roomHeight; ++y)
						{
							primer.setBlockState(x, y, z, Blocks.COBBLESTONE.getDefaultState());
						}
					}
				}
			}
		}
	}

	@Override
	public void populate(int chunkX, int chunkZ)
	{

		final PlacementSettings placementSettings = new PlacementSettings();
		placementSettings.setChunk(new ChunkPos(chunkX, chunkZ));

		BlockFalling.fallInstantly = false;

		final int chunkCornerX  = chunkX << 4;
		final int chunkCornerZ  = chunkZ << 4;

		final int nextChunkX = chunkCornerX + 15;
		final int nextChunkZ = chunkCornerZ + 15;

		final IBlockState dirt = Blocks.DIRT.getDefaultState();
		final IBlockState air = Blocks.AIR.getDefaultState();

		final Room cornerRoom = getRoomAt(chunkCornerX - 10, chunkCornerZ - 10);
		for (int roomX = cornerRoom.getX(); roomX < nextChunkX + 10; roomX += 10) {
			for (int roomZ = cornerRoom.getZ(); roomZ < nextChunkZ + 10; roomZ += 10) {

				final Room room = getRoomAt(roomX, roomZ);

				if (!room.isPresent()) continue;

				if (room.isDoorwayPresent(EnumFacing.SOUTH)) {
					//Generate Vertical exit

					final int xPos = room.getX() + 4;
					final int zPos = room.getZ() + 8;
					final MutableBlockPos pos = new MutableBlockPos(xPos, BASE_HEIGHT + 7, zPos);

					for (int i = zPos; i < room.getZ() + 8 + 3; ++i) {
						for (int y = BASE_HEIGHT; y < BASE_HEIGHT + 4; ++y) {
							pos.setPos(xPos - 2, y, i);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
							pos.setPos(xPos + 2, y, i);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						}

						pos.setPos(xPos - 1, BASE_HEIGHT, i);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						pos.setPos(xPos, BASE_HEIGHT, i);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						pos.setPos(xPos + 1, BASE_HEIGHT, i);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);

						pos.setPos(xPos - 1, BASE_HEIGHT + 4, i);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						pos.setPos(xPos, BASE_HEIGHT + 4, i);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						pos.setPos(xPos + 1, BASE_HEIGHT + 4, i);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);

						for (int y = BASE_HEIGHT + 1; y < BASE_HEIGHT + 4; ++y) {
							pos.setPos(xPos - 1, y, i);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, air);
							pos.setPos(xPos + 1, y, i);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, air);
							pos.setPos(xPos, y, i);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, air);
						}
					}

				}
				if (room.isDoorwayPresent(EnumFacing.EAST)) {
					//Generate

					final int xPos = room.getX() + 8;
					final int zPos = room.getZ() + 4;
					final MutableBlockPos pos = new MutableBlockPos(xPos, BASE_HEIGHT + 7, zPos);

					for (int i = xPos; i < room.getX() + 8 + 3; ++i) {
						for (int z = BASE_HEIGHT; z < BASE_HEIGHT + 4; ++z) {
							pos.setPos(i, z, zPos - 2);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
							pos.setPos(i, z, zPos + 2);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						}


						pos.setPos(i, BASE_HEIGHT, zPos - 1);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						pos.setPos(i, BASE_HEIGHT, zPos);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						pos.setPos(i, BASE_HEIGHT, zPos + 1);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);

						pos.setPos(i, BASE_HEIGHT + 4, zPos - 1);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						pos.setPos(i, BASE_HEIGHT + 4, zPos);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);
						pos.setPos(i, BASE_HEIGHT + 4, zPos + 1);
						placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, dirt);

						for (int z = BASE_HEIGHT + 1; z < BASE_HEIGHT + 4; ++z) {
							pos.setPos(i, z, zPos - 1);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, air);
							pos.setPos(i, z, zPos + 1);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, air);
							pos.setPos(i, z, zPos);
							placeBlockIfInChunk(chunkCornerX, chunkCornerZ, pos, air);
						}
					}
				}

				final RoomTemplate template = room.getTemplate();
				//template = TemplateManager.getTemplateByChance(room.getCharacteristics(), room.getTemplateChance());

				if (template != null)
				{
					final BlockPos blockPos = new BlockPos(room.getX(), BASE_HEIGHT, room.getZ());
					template.addBlocksToWorldChunk(
							world,
							blockPos,
							placementSettings);

//					final BlockPos sheepPos = blockPos.add(4, template.getTemplate().getHeight(), 4);
//					final List<EntitySheep> entitiesWithinAABB = world.getEntitiesWithinAABB(EntitySheep.class, new AxisAlignedBB(sheepPos));
//					if (entitiesWithinAABB.isEmpty())
//					{
//						//Uncomment this to spawn sheep with labels at a specific point for debugging purposes
//
//						final BlockPos blockpos = sheepPos;
//						final EntitySheep sheep = new EntitySheep(world);
//						sheep.setCustomNameTag(template.getTemplate().getResourceLocation().toString());
//						sheep.setNoAI(true);
//						sheep.setLocationAndAngles(blockpos.getX(), blockpos.getY(), blockpos.getZ(), 0, 0);
//						sheep.setEntityInvulnerable(true);
//						sheep.setInvisible(true);
//						sheep.setAlwaysRenderNameTag(true);
//						sheep.setSilent(true);
//						sheep.setNoGravity(true);
//						world.spawnEntity(sheep);
//					}
				}
			}
		}

		BlockFalling.fallInstantly = false;
	}

	private void placeBlockIfInChunk(int chunkCornerX, int chunkCornerZ, BlockPos pos, IBlockState blockState)
	{
		final int nextChunkX = chunkCornerX + 16;
		if (pos.getX() >= chunkCornerX && pos.getX() < nextChunkX)
		{
			final int nextChunkZ = chunkCornerZ + 16;
			if (pos.getZ() >= chunkCornerZ && pos.getZ() < nextChunkZ)
			{
				world.setBlockState(pos, blockState, 16 | 2);
			}
		}
	}

	@Override
	public boolean generateStructures(Chunk chunkIn, int x, int z)
	{
		return false;
	}

	@Override
	public List<Biome.SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos)
	{
		return Lists.newArrayList();
	}

	@Nullable
	@Override
	public BlockPos getNearestStructurePos(World worldIn, String structureName, BlockPos position, boolean findUnexplored)
	{
		return null;
	}

	@Override
	public void recreateStructures(Chunk chunkIn, int x, int z)
	{

	}

	@Override
	public boolean isInsideStructure(World worldIn, String structureName, BlockPos pos)
	{
		return false;
	}

	public int getFloorHeight() {
		return BASE_HEIGHT;
	}

	private class Point2DRoomCacheLoader extends CacheLoader<Point2D, Room>
	{
		List<RoomSet> roomSets = Lists.newArrayList();

		private Point2DRoomCacheLoader() {
			MakeRooms();
		}

		private void MakeRooms()
		{
			roomSets.clear();
			RoomSet e;

//			e = new RoomSet();
//			e.presentRooms = new boolean[] {
//					true, false, false,
//					false, false, false,
//					false, false, false
//			};
//			roomSets.add(e);

			e = new RoomSet();
			e.presentRooms = new int[] {
					1, 1, 1, 0, 1,
					0, 0, 1, 0, 0,
					0, 0, 1, 0, 0,
					0, 0, 1, 1, 1,
					0, 0, 0, 0, 1
			};
			roomSets.add(e);

			e = new RoomSet();
			e.presentRooms = new int[] {
					1, 1, 0, 0, 1,
					0, 1, 0, 0, 1,
					0, 1, 1, 1, 1,
					0, 0, 1, 0, 0,
					0, 0, 1, 1, 1,
			};
			roomSets.add(e);

			e = new RoomSet();
			e.presentRooms = new int[] {
					1, 1, 1, 0, 0,
					0, 1, 0, 0, 0,
					0, 1, 0, 0, 0,
					1, 1, 1, 1, 0,
					1, 0, 0, 0, 0
			};
			roomSets.add(e);

			e = new RoomSet();
			e.presentRooms = new int[] {
					1, 1, 0, 1, 1,
					0, 1, 0, 1, 0,
					0, 1, 1, 1, 0,
					0, 0, 0, 1, 0,
					0, 0, 0, 1, 1
			};
			roomSets.add(e);

			e = new RoomSet();
			e.presentRooms = new int[] {
					1, 1, 0, 0, 1,
					0, 1, 0, 0, 1,
					0, 1, 1, 0, 0,
					0, 1, 0, 0, 0,
					1, 1, 0, 0, 0
			};
			roomSets.add(e);
		}

		@Override
		public Room load(Point2D key) throws Exception
		{
			final int x = key.getX() / 10;
			final int z = key.getZ() / 10;

			final Room room = new Room(roomId, key.getX(), key.getZ(), 9, 9, noiseGen.getValue(x, z));
			final boolean roomPresent = isRoomPresent(x, z);
			room.setPresent(roomPresent);
			room.setDoorwayPresent(EnumFacing.SOUTH, isRoomPresent(x, z + 1));
			room.setDoorwayPresent(EnumFacing.NORTH, isRoomPresent(x, z - 1));
			room.setDoorwayPresent(EnumFacing.EAST, isRoomPresent(x + 1, z));
			room.setDoorwayPresent(EnumFacing.WEST, isRoomPresent(x - 1, z));

			if (roomPresent)
			{
				final RoomTemplate roomTemplate;
				roomTemplate = TemplateManager.getTemplateByChance(room.getCharacteristics(), room.getTemplateChance());
				if (roomTemplate == null) {
					room.setPresent(false);
				} else {
					room.setTemplate(roomTemplate);
				}
			}
			roomId++;
			return room;
		}

		public boolean isRoomPresent(int x, int z) {
			final int roomSetX = x / ROOM_SET_SIZE;
			final int roomSetZ = z / ROOM_SET_SIZE;
			double noise = noiseGen.getValue(roomSetX, roomSetZ);
			final int roomSetIndex = (int) (Math.abs(noise) * roomSets.size());
			RoomSet roomSet = roomSets.get(roomSetIndex);

			int roomX = x % ROOM_SET_SIZE;
			if (roomX < 0) roomX += ROOM_SET_SIZE;
			int roomZ = z % ROOM_SET_SIZE;
			if (roomZ < 0) roomZ += ROOM_SET_SIZE;
			return roomSet.isPresent(roomX, roomZ);
		}
	}

	private class RoomSet {
		int[] presentRooms = new int[ROOM_SET_SIZE * ROOM_SET_SIZE];

		public boolean isPresent(int x, int z)
		{
			return presentRooms[x * ROOM_SET_SIZE + z] != 0;
		}
	}
}
