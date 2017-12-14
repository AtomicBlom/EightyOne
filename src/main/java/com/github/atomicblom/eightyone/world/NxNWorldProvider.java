package com.github.atomicblom.eightyone.world;

import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class NxNWorldProvider extends net.minecraft.world.WorldProvider
{

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
}
