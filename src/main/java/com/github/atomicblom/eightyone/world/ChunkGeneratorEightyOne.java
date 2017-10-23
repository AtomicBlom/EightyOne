package com.github.atomicblom.eightyone.world;

import com.github.atomicblom.eightyone.util.Point2D;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockFalling;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.NoiseGeneratorSimplex;
import org.apache.commons.lang3.concurrent.ConcurrentException;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ChunkGeneratorEightyOne implements IChunkGenerator
{
	private final World world;
	private final long seed;
	private final boolean mapFeaturesEnabled;
	private final Random rand;
	private final NoiseGeneratorSimplex noiseGen;
	private int roomId = 0;
	private final LoadingCache<Point2D, Room> roomCache;

	public ChunkGeneratorEightyOne(World world, long seed, boolean mapFeaturesEnabled)
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

	private Room getRoomAt(int x, int z) {
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
		final int height = 6;
		final int posX = chunkX << 4;
		final int posZ = chunkZ << 4;

		for (int z = 0; z < 16; ++z)
		{
			for (int x = 0; x < 16; ++x)
			{
				final Room room = getRoomAt(posX + x, posZ + z);
				if (room.hasProperty(RoomProperties.IsPresent)) continue;
				if (room.contains(posX + x, posZ + z))
				{

					primer.setBlockState(x, 64, z, Blocks.COBBLESTONE.getDefaultState());

					final int xOffset = room.getXOffset(posX + x);
					final int zOffset = room.getZOffset(posZ + z);

					if ((xOffset == 2 || xOffset == 3 || xOffset == 5 || xOffset == 6) &&
						(zOffset == 2 || zOffset == 3 || zOffset == 5 || zOffset == 6))
					{
						primer.setBlockState(x, 64 + height, z, Blocks.GLASS.getDefaultState());
					} else {
						primer.setBlockState(x, 64 + height, z, Blocks.COBBLESTONE.getDefaultState());
					}

					if (room.isWall(posX + x, posZ + z))
					{
						for (int y = 64; y < 64 + height; ++y)
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
		BlockFalling.fallInstantly = true;

		if (world.getChunkFromChunkCoords(chunkX, chunkZ).isPopulated()) return;

		/*if (!world.getChunkProvider().isChunkGeneratedAt(chunkX, chunkZ + 1) ||
				!world.getChunkProvider().isChunkGeneratedAt(chunkX + 1, chunkZ) ||
				!world.getChunkProvider().isChunkGeneratedAt(chunkX + 1, chunkZ + 1)) {
			return;
		}*/

		int x  = (chunkX << 4);// + 8;
		int z  = (chunkZ << 4);// + 8;

		final Room room = getRoomAt(x, z);
		if (room.hasProperty(RoomProperties.IsPresent)) return;
		if (room.hasProperty(RoomProperties.VerticalExit)) {
			//Generate Vertical exit

			world.setBlockState(new BlockPos(room.getX() + 4, 72, room.getZ()), Blocks.GOLD_BLOCK.getDefaultState());

		} else if (room.hasProperty(RoomProperties.HorizontalExit)) {
			//Generate

			world.setBlockState(new BlockPos(room.getX(), 72, room.getZ() + 4), Blocks.DIAMOND_BLOCK.getDefaultState());
		}

		//populate(chunkX, chunkZ+1);
		//populate(chunkX+1, chunkZ);
		//populate(chunkX+1, chunkZ+1);

		BlockFalling.fallInstantly = false;
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

	private class Point2DRoomCacheLoader extends CacheLoader<Point2D, Room>
	{
		@Override
		public Room load(Point2D key) throws Exception
		{
			double noise = noiseGen.getValue(key.getX(), key.getZ());
			long properties = Double.doubleToRawLongBits(noise);
			final Room result = new Room(roomId, key.getX(), key.getZ(), 9, 9, properties);
			roomId++;
			return result;
		}
	}
}
