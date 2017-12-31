package com.github.atomicblom.eightyone.world;

import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

public class NxNWorldProvider extends WorldProvider
{
	@Override
	protected void init()
	{
		super.init();
		world.setSeaLevel(192);
	}

	@Override
	public BiomeProvider getBiomeProvider()
	{
		return super.getBiomeProvider();
	}

	@Override
	public IChunkGenerator createChunkGenerator()
	{
		return new NxNChunkGenerator(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled());
	}

	private static DimensionType _dimensionType;

	public static DimensionType initDimensionType() {
		_dimensionType = DimensionType.register("EightyOne", "_eightyone", 81, NxNWorldProvider.class, false);
		return _dimensionType;
	}

	@Override
	public Biome getBiomeForCoords(BlockPos pos)
	{
		return Biomes.DEFAULT;
	}

	@Override
	public DimensionType getDimensionType()
	{
		return _dimensionType;
	}

	@Override
	public double getMovementFactor() {
		return 9D;
	}

	@Override
	public double getHorizon()
	{
		return 0.0D;
	}

	@Override
	public float getCloudHeight()
	{
		return 256;
	}

	@Override
	public double getVoidFogYFactor()
	{
		return 0.75;
	}

	@Override
	public BlockPos getRandomizedSpawnPoint()
	{
		BlockPos ret = this.world.getSpawnPoint();


		return ret;
	}
}
