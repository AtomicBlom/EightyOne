package com.github.atomicblom.eightyone.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;

public class WorldProvider extends net.minecraft.world.WorldProvider
{

	@Override
	public BiomeProvider getBiomeProvider()
	{
		return super.getBiomeProvider();
	}

	@Override
	public IChunkGenerator createChunkGenerator()
	{
		return new ChunkGeneratorEightyOne(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled());
	}

	private static DimensionType _dimensionType;

	public static DimensionType initDimensionType() {
		_dimensionType = DimensionType.register("EightyOne", "_eightyone", 81, WorldProvider.class, false);
		return _dimensionType;
	}

	@Override
	public DimensionType getDimensionType()
	{
		return _dimensionType;
	}
}
