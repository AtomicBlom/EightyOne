package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.PortalTileEntity;
import com.github.atomicblom.eightyone.client.DungeonBakedModel;
import com.github.atomicblom.eightyone.client.PortalTESR;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientRegistrationEvents
{

	@SubscribeEvent
	public static void onRenderingReady(ModelRegistryEvent evt) {
		// We need to tell Forge how to map our BlockCamouflage's IBlockState to a ModelResourceLocation.
		// For example, the BlockStone granite variant has a BlockStateMap entry that looks like
		//   "stone[variant=granite]" (iBlockState)  -> "minecraft:granite#normal" (ModelResourceLocation)
		// For the camouflage block, we ignore the iBlockState completely and always return the same ModelResourceLocation,
		//   which is done using the anonymous class below.
		StateMapperBase ignoreState = new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
				return DungeonBakedModel.variantTag;
			}
		};
		ModelLoader.setCustomStateMapper(BlockLibrary.dungeon_block, ignoreState);
		// NB If your block has multiple variants and you want vanilla to load a model for each variant, you don't need a
		//   custom state mapper.
		// You can see examples of vanilla custom state mappers in BlockModelShapes.registerAllBlocks()

		// This step is necessary in order to make your block render properly when it is an item (i.e. in the inventory
		//   or in your hand or thrown on the ground).
		// It must be done on client only, and must be done after the block has been created in Common.preinit().
		ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation("eightyone:dungeon_block", "inventory");
		final int DEFAULT_ITEM_SUBTYPE = 0;
		ModelLoader.setCustomModelResourceLocation(ItemLibrary.dungeon_block, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation);
	}

	// Called after all the other baked block models have been added to the modelRegistry
	// Allows us to manipulate the modelRegistry before BlockModelShapes caches them.
	@SubscribeEvent
	public static void onModelBakeEvent(ModelBakeEvent event)
	{
		// Find the existing mapping for CamouflageBakedModel - it will have been added automatically because
		//  we registered a custom BlockStateMapper for it (using ModelLoader.setCustomStateMapper)
		// Replace the mapping with our CamouflageBakedModel.
		Object object =  event.getModelRegistry().getObject(DungeonBakedModel.variantTag);
		if (object instanceof IBakedModel) {
			IBakedModel existingModel = (IBakedModel)object;
			DungeonBakedModel customModel = new DungeonBakedModel(existingModel);
			event.getModelRegistry().putObject(DungeonBakedModel.variantTag, customModel);
		}
	}



	@SubscribeEvent
	public static void onRegisterItems(RegistryEvent.Register<Item> event) {
		ClientRegistry.bindTileEntitySpecialRenderer(PortalTileEntity.class, new PortalTESR());
	}

	@SubscribeEvent
	public static void onTextureStitch(TextureStitchEvent.Pre event) {
		final TextureMap map = event.getMap();
		map.registerSprite(new ResourceLocation(Reference.MOD_ID, "blocks/portal3"));
	}
}
