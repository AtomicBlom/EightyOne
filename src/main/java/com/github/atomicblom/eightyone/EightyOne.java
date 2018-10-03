package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.client.gui.GuiHandler;
import com.github.atomicblom.eightyone.command.DescribeRoomCommand;
import com.github.atomicblom.eightyone.command.ReloadRoomsCommand;
import com.github.atomicblom.eightyone.registration.MaterializeMimicBlocks;
import com.github.atomicblom.eightyone.registration.PacketHandler;
import com.github.atomicblom.eightyone.world.NxNWorldProvider;
import com.github.atomicblom.eightyone.world.NxNWorldType;
import com.github.atomicblom.eightyone.world.structure.TemplateManager;
import net.minecraft.world.WorldType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Reference.MOD_ID, name=Reference.MOD_NAME, version = Reference.VERSION, acceptedMinecraftVersions = "[1.12.2, 1.13)")
public class EightyOne
{
    @Instance
    public static EightyOne instance;
    private WorldType worldType;

    public static boolean DEBUG_FORCE_ALLOW_PORTAL = false;
    public static boolean DEBUG_SHOW_ROOM_INFO = true;

    public static final String IS_CI_BUILD = "@CI_BUILD@";

    public static boolean isReleaseBuild() { return Boolean.parseBoolean(IS_CI_BUILD); }

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (isReleaseBuild())
        {
            //Force developer derps off.
            DEBUG_FORCE_ALLOW_PORTAL = false;
            //TODO: Disable for v1
            DEBUG_SHOW_ROOM_INFO = true;
        } else
        {
            Logger.info("You are not running a release build of EightyOne. This message is purely for informational purposes.");
        }

        TemplateManager.setConfigurationDirectory(event.getModConfigurationDirectory());

        TemplateManager.findTemplates(false);
        PacketHandler.registerMessages(Reference.MOD_ID);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.post(new MaterializeMimicBlocks());
        DimensionManager.registerDimension(81, NxNWorldProvider.initDimensionType());
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        worldType = new NxNWorldType();
        TemplateManager.reload();
        TemplateManager.catalogueValidStructures();
    }

    @EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new DescribeRoomCommand());
        event.registerServerCommand(new ReloadRoomsCommand());
    }
}
