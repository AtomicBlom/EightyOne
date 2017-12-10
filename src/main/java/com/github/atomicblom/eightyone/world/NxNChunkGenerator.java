package com.github.atomicblom.eightyone.world;

import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.blocks.tileentity.TileEntityDungeonBlock;
import com.github.atomicblom.eightyone.util.EntranceHelper;
import com.github.atomicblom.eightyone.util.Point2D;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import com.github.atomicblom.eightyone.world.structure.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import javafx.scene.input.ScrollEvent;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.storage.WorldInfo;
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
		final List<TileEntity> tileEntitiesToAdd = Lists.newArrayList();
		generateChunk(x, z, primer, tileEntitiesToAdd);

		final Chunk chunk = new Chunk(world, primer, x, z);
		for (final TileEntity tileEntity : tileEntitiesToAdd)
		{
			//if (tileEntity instanceof TileEntityDungeonBlock)
			//{
				chunk.addTileEntity(tileEntity);
			//}
		}

		chunk.generateSkylightMap();
		return chunk;
	}

	@Override
	public void populate(int x, int z)
	{
		/*Chunk chunk = world.getChunkFromChunkCoords(x, z);
		final List<TileEntity> tileEntitiesToAdd = Lists.newArrayList();
		generateChunk(x, z, null, tileEntitiesToAdd);

		for (final TileEntity tileEntity : tileEntitiesToAdd)
		{
			if (!(tileEntity instanceof TileEntityDungeonBlock))
			{
				chunk.addTileEntity(tileEntity);
			}
		}*/
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

	private void generateChunk(int chunkX, int chunkZ, @Nullable ChunkPrimer primer, List<TileEntity> tileEntitiesToAdd)
	{
		List<Rotation> horizontalRotations = Lists.newArrayList(Rotation.CLOCKWISE_90);
		List<Rotation> verticalRotations = Lists.newArrayList(Rotation.NONE);

		final int chunkCornerX  = chunkX << 4;
		final int chunkCornerZ  = chunkZ << 4;

		final int nextChunkX = chunkCornerX + 15;
		final int nextChunkZ = chunkCornerZ + 15;

		final Room cornerRoom = getRoomAt(chunkCornerX - 10, chunkCornerZ - 10);
		for (int roomX = cornerRoom.getX(); roomX < nextChunkX + 10; roomX += 10) {
			for (int roomZ = cornerRoom.getZ(); roomZ < nextChunkZ + 10; roomZ += 10) {

				final Room room = getRoomAt(roomX, roomZ);

				if (!room.isPresent()) continue;

				final RoomTemplate template = room.getTemplate();
				if (template != null && template.getTemplate().isCustomRoom())
				{
					final PlacementSettings placementIn = new PlacementSettings();
					placementIn.setChunk(new ChunkPos(chunkX, chunkZ));

					final BlockPos templatePosition = new BlockPos(room.getX(), BASE_HEIGHT, room.getZ());
					template.addBlocksToChunkPrimer(primer, tileEntitiesToAdd, world, templatePosition, placementIn);

					if (room.isDoorwayPresent(EnumFacing.SOUTH)) {
						final RoomTemplate southPassageTemplate = TemplateManager.getTemplateByChance(new TemplateCharacteristics(Shape.Closed, horizontalRotations), room.getTemplateChance(), RoomPurpose.PASSAGE);
						if (southPassageTemplate != null)
						{
							final BlockPos passagePosition = templatePosition.add(2, 0, 7);

							southPassageTemplate.addBlocksToChunkPrimer(primer, tileEntitiesToAdd, world, passagePosition, placementIn);
						} else {
							Logger.severe("Could not create doorway, passage not found");
						}
					}

					if (room.isDoorwayPresent(EnumFacing.EAST)) {
						final RoomTemplate southPassageTemplate = TemplateManager.getTemplateByChance(new TemplateCharacteristics(Shape.Closed, verticalRotations), room.getTemplateChance(), RoomPurpose.PASSAGE);
						if (southPassageTemplate != null)
						{
							final BlockPos passagePosition = templatePosition.add(5, 0, 0);

							southPassageTemplate.addBlocksToChunkPrimer(primer, tileEntitiesToAdd, world, passagePosition, placementIn);
						} else {
							Logger.severe("Could not create doorway, passage not found");
						}
					}
				}
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
		Biome biome = world.getBiome(pos);
		return biome.getSpawnableList(creatureType);
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
			roomSets.add(new RoomSet(null,
					"11101",
					"00100",
					"00100",
					"00111",
					"00001"
			));
			roomSets.add(new RoomSet(null,
					"11001",
					"01001",
					"01111",
					"00100",
					"00111"
			));
			roomSets.add(new RoomSet(null,
					"11100",
					"01000",
					"01000",
					"11110",
					"10000"
			));
			roomSets.add(new RoomSet(null,
					"11011",
					"01010",
					"01110",
					"00010",
					"00011"
			));
			roomSets.add(new RoomSet(null,
					"11001",
					"01001",
					"01100",
					"01000",
					"11000"
			));
			roomSets.add(new RoomSet(null,
					"11100",
					"00100",
					"11111",
					"00100",
					"11100"
			));
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
				roomTemplate = TemplateManager.getTemplateByChance(room.getCharacteristics(), room.getTemplateChance(), RoomPurpose.ROOM);
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

		public RoomSet(String... roomDefinition) {
			int z = 0;
			for (int i = 0; i < roomDefinition.length; i++) {
				if (roomDefinition[i] == null || roomDefinition[i].length() == 0) continue;

				char[] chars = roomDefinition[i].toCharArray();
				for (int x = 0; x < chars.length; x++) {
					presentRooms[z * ROOM_SET_SIZE + x] = chars[x] != '0' ? 1 : 0;
				}
				z++;
			}
		}

		public boolean isPresent(int x, int z)
		{
			return presentRooms[x * ROOM_SET_SIZE + z] != 0;
		}
	}
}
