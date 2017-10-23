package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.world.WorldProvider;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = Reference.MOD_ID, name=Reference.MOD_NAME, version = Reference.VERSION, acceptedMinecraftVersions = "[1.12, 1.13)")
public class EightyOne
{
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        DimensionManager.registerDimension(81, WorldProvider.initDimensionType());


        // some example code
        System.out.println("DIRT BLOCK >> "+Blocks.DIRT.getUnlocalizedName());
    }
}
