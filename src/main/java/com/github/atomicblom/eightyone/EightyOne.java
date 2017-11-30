package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.command.DescribeRoomCommand;
import com.github.atomicblom.eightyone.command.ReloadRoomsCommand;
import com.github.atomicblom.eightyone.world.NxNWorldProvider;
import com.github.atomicblom.eightyone.world.structure.TemplateManager;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Reference.MOD_ID, name=Reference.MOD_NAME, version = Reference.VERSION, acceptedMinecraftVersions = "[1.12, 1.13)")
public class EightyOne
{
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        TemplateManager.setConfigurationDirectory(event.getModConfigurationDirectory());
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        DimensionManager.registerDimension(81, NxNWorldProvider.initDimensionType());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        TemplateManager.getValidTemplates(false);
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new DescribeRoomCommand());
        event.registerServerCommand(new ReloadRoomsCommand());
    }
}
