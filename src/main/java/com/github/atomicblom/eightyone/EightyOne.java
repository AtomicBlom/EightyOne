package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.world.WorldProvider;
import com.github.atomicblom.eightyone.world.structure.TemplateManager;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

@Mod(modid = Reference.MOD_ID, name=Reference.MOD_NAME, version = Reference.VERSION, acceptedMinecraftVersions = "[1.12, 1.13)")
public class EightyOne
{
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        DimensionManager.registerDimension(81, WorldProvider.initDimensionType());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        TemplateManager.getValidTemplates();
    }
}
