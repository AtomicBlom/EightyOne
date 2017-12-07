package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.util.EntranceHelper;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.ITemplateProcessor;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Map.Entry;

public class NxNTemplate extends Template
{
	private final boolean[] openEntrances = new boolean[EnumFacing.HORIZONTALS.length];
	private int yOffset;
	private boolean spawnable = true;
	private TemplateCharacteristics characteristics;
	private ResourceLocation resourceLocation;

	public NxNTemplate(ResourceLocation resourceLocation) {
		characteristics = EntranceHelper.calculateCharacteristics(openEntrances);
		this.resourceLocation = resourceLocation;
	}

	public int getHeight()
	{
		return getSize().getY();
	}

	public boolean isCustomRoom() {
		final BlockPos size = getSize();
		return size.getX() > 7 || size.getZ() > 7;
	}

	@Override
	public void addBlocksToWorldChunk(World worldIn, BlockPos pos, PlacementSettings placementIn)
	{
		placementIn.setBoundingBoxFromChunk();
		super.addBlocksToWorld(worldIn, pos, placementIn, 16 | 2);
	}

	public BlockPos offset(BlockPos pos)
	{
		final int xOffset = (9 - getSize().getX()) / 2;
		final int zOffset = (9 - getSize().getZ()) / 2;
		return pos.add(xOffset, yOffset + 1, zOffset);
	}

	public boolean isSpawnable()
	{
		return spawnable;
	}

	@Override
	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn, int flags)
	{
		super.addBlocksToWorld(worldIn, pos, placementIn, 12);
	}

	@Override
	public void read(NBTTagCompound compound)
	{
		super.read(compound);

		boolean setYOffsetFromDoorway = true;
		int doorwayYOffset = 0;
		int yOffset = 0;

		final PlacementSettings placementSettings = new PlacementSettings();
		final Map<BlockPos, String> dataBlocks = getDataBlocks(BlockPos.ORIGIN, placementSettings);
		for (final Entry<BlockPos, String> dataBlock : dataBlocks.entrySet()) {
			final String dataValue = dataBlock.getValue().toLowerCase();
			final BlockPos location = dataBlock.getKey();
			if (dataValue.startsWith("doorway")) {
				doorwayYOffset = -location.getY();
				final EnumFacing quadrant = getQuadrant(location, getSize().getZ());
				if (quadrant == null) {
					Logger.warning("    It looks like a structure has an doorway placed on a diagonal. This is not supported, location %s", location);
				} else
				{
					Logger.info("    data block at location %s is an entrance for %s", location, quadrant);
					addEntrance(quadrant);
				}
			} else if (dataValue.startsWith("not_spawnable")){
				spawnable = false;
			} else if (dataValue.startsWith("y_offset:")) {
				yOffset = Integer.getInteger(dataValue.substring("y_offset:".length()).trim());
				setYOffsetFromDoorway = false;
			}
		}

		if (setYOffsetFromDoorway) {

			this.yOffset = doorwayYOffset;
			Logger.info("    yOffset has been set to %d because of doorways (or lack there of)", this.yOffset);
		}
		else {
			this.yOffset = yOffset;
			Logger.info("    yOffset has been set to %d because of an explicit y_offset data block", this.yOffset);
		}
	}

	private static EnumFacing getQuadrant(BlockPos location, int length) {
		final int qa = location.getX() - location.getZ();
		final int qb = location.getX() - ((length - 1) - location.getZ());

		if (qa > 0 && qb < 0) {
			return EnumFacing.NORTH;
		}
		if (qa > 0 && qb > 0) {
			return EnumFacing.EAST;
		}
		if (qa <0 && qb < 0) {
			return EnumFacing.WEST;
		}
		if (qa < 0 && qb > 0) {
			return EnumFacing.SOUTH;
		}

		return null;
	}

	public void addEntrance(EnumFacing direction) {
		openEntrances[direction.getHorizontalIndex()] = true;
		characteristics = EntranceHelper.calculateCharacteristics(openEntrances);
	}

	public TemplateCharacteristics getCharacteristics()
	{
		return characteristics;
	}

	public ResourceLocation getResourceLocation()
	{
		return resourceLocation;
	}

	/**
	 * Adds blocks and entities from this structure to the given world.
	 *
	 * @param worldIn The world to use
	 * @param pos The origin position for the structure
	 * @param templateProcessor The template processor to use
	 * @param placementIn Placement settings to use
	 * @param flags Flags to pass to {@link World#setBlockState(BlockPos, IBlockState, int)}
	 */
	public void addBlocksToWorld(World worldIn, BlockPos pos, @Nullable ITemplateProcessor templateProcessor, PlacementSettings placementIn, int flags)
	{
		BlockPos size = this.getSize();

		if ((!this.blocks.isEmpty() || !placementIn.getIgnoreEntities() && !this.entities.isEmpty()) && size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1)
		{
			Block block = placementIn.getReplacedBlock();
			StructureBoundingBox structureboundingbox = placementIn.getBoundingBox();

			for (Template.BlockInfo templateBlockInfo : this.blocks)
			{
				BlockPos blockpos = transformedBlockPos(placementIn, templateBlockInfo.pos).add(pos);
				Template.BlockInfo processedBlockInfo = templateProcessor != null ? templateProcessor.processBlock(worldIn, blockpos, templateBlockInfo) : templateBlockInfo;

				if (processedBlockInfo != null)
				{
					Block processedBlock = processedBlockInfo.blockState.getBlock();

					if ((block == null || block != processedBlock) && (!placementIn.getIgnoreStructureBlock() || processedBlock != Blocks.STRUCTURE_BLOCK) && (structureboundingbox == null || structureboundingbox.isVecInside(blockpos)))
					{
						IBlockState mirrorState = processedBlockInfo.blockState.withMirror(placementIn.getMirror());
						IBlockState finalState = mirrorState.withRotation(placementIn.getRotation());

						if (processedBlockInfo.tileentityData != null)
						{
							TileEntity tileentity = worldIn.getTileEntity(blockpos);

							if (tileentity != null)
							{
								if (tileentity instanceof IInventory)
								{
									((IInventory)tileentity).clear();
								}

								worldIn.setBlockState(blockpos, Blocks.BARRIER.getDefaultState(), 4);
							}
						}

						if (worldIn.setBlockState(blockpos, finalState, flags) && processedBlockInfo.tileentityData != null)
						{
							TileEntity tileentity = worldIn.getTileEntity(blockpos);

							if (tileentity != null)
							{
								processedBlockInfo.tileentityData.setInteger("x", blockpos.getX());
								processedBlockInfo.tileentityData.setInteger("y", blockpos.getY());
								processedBlockInfo.tileentityData.setInteger("z", blockpos.getZ());
								tileentity.readFromNBT(processedBlockInfo.tileentityData);
								tileentity.mirror(placementIn.getMirror());
								tileentity.rotate(placementIn.getRotation());
							}
						}
					}
				}
			}

			for (Template.BlockInfo blockInfo : this.blocks)
			{
				if (block == null || block != blockInfo.blockState.getBlock())
				{
					BlockPos blockpos1 = transformedBlockPos(placementIn, blockInfo.pos).add(pos);

					if (structureboundingbox == null || structureboundingbox.isVecInside(blockpos1))
					{
						//worldIn.notifyNeighborsRespectDebug(blockpos1, blockInfo.blockState.getBlock(), false);

						if (blockInfo.tileentityData != null)
						{
							TileEntity tileentity1 = worldIn.getTileEntity(blockpos1);

							if (tileentity1 != null)
							{
								//ileentity1.markDirty();
							}
						}
					}
				}
			}

			if (!placementIn.getIgnoreEntities())
			{
				this.addEntitiesToWorld(worldIn, pos, placementIn.getMirror(), placementIn.getRotation(), structureboundingbox);
			}
		}
	}
}
