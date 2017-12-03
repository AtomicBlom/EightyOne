package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.blocks.DungeonBlock;
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
		StateMapperBase ignoreState = new StateMapperBase() {
			@Override
			protected ModelResourceLocation getModelResourceLocation(IBlockState iBlockState) {
				if (iBlockState.getValue(DungeonBlock.SOURCEIMAGE)) {
					return DungeonBakedModel.lockTag;
				}
				return DungeonBakedModel.variantTag;
			}
		};
		ModelLoader.setCustomStateMapper(BlockLibrary.dungeon_block, ignoreState);

		final int DEFAULT_ITEM_SUBTYPE = 0;

		ModelLoader.setCustomModelResourceLocation(ItemLibrary.dungeon_block, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(Reference.Blocks.DUNGEON_BLOCK, "inventory"));
		ModelLoader.setCustomModelResourceLocation(ItemLibrary.portal, DEFAULT_ITEM_SUBTYPE, new ModelResourceLocation(Reference.Blocks.PORTAL, "inventory"));
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
		map.registerSprite(new ResourceLocation(Reference.MOD_ID, "blocks/portal_frame"));
	}
}
