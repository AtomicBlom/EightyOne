package com.github.atomicblom.eightyone.world;

import com.github.atomicblom.eightyone.Reference;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Random;

public class NxNWorldType extends WorldType
{
	public NxNWorldType() {super(Reference.WORLD_NAME);}

	/**
	 * Gets the translation key for the name of this world type.
	 */
	@SideOnly(Side.CLIENT)
	public String getTranslationKey()
	{
		return Reference.MOD_ID + ":generator." + getName();
	}

	/**
	 * Gets the translation key for the info text for this world type.
	 */
	@SideOnly(Side.CLIENT)
	public String getInfoTranslationKey()
	{
		return getTranslationKey() + ".info";
	}

	@Override
	public IChunkGenerator getChunkGenerator(World world, String generatorOptions)
	{
		return new NxNChunkGenerator(world, world.getSeed(), false);
	}

	@Override
	public boolean isVersioned()
	{
		return false;
	}

	public double getHorizon(World world)
	{
		return 0.0D;
	}

	public double voidFadeMagnitude()
	{
		return 1.0D;
	}

	public boolean handleSlimeSpawnReduction(Random random, World world)
	{
		return random.nextInt(4) != 1;
	}
}
