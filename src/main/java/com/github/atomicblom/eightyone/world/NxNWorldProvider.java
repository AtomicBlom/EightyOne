package com.github.atomicblom.eightyone.world;

import net.minecraft.entity.Entity;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.client.IRenderHandler;

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

	@Override
	public boolean isSurfaceWorld()
	{
		return super.isSurfaceWorld();
	}

	@Override
	public float getCloudHeight()
	{
		return super.getCloudHeight();
	}

	@Override
	public boolean doesXZShowFog(int x, int z)
	{
		return super.doesXZShowFog(x, z);
	}

	@Override
	public boolean hasSkyLight()
	{
		return super.hasSkyLight();
	}

	@Override
	public IRenderHandler getSkyRenderer()
	{
		return super.getSkyRenderer();
	}

	@Override
	public Vec3d getSkyColor(Entity cameraEntity, float partialTicks)
	{
		return super.getSkyColor(cameraEntity, partialTicks);
	}

	@Override
	public int getActualHeight()
	{
		return 128;
	}

	@Override
	public double getHorizon()
	{
		return super.getHorizon();
	}
}
