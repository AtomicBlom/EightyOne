package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.util.EntranceHelper;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NxNTemplate extends Template
{
	private final boolean[] openEntrances = new boolean[EnumFacing.HORIZONTALS.length];
	private int yOffset;
	private boolean spawnable = true;
	private TemplateCharacteristics characteristics;
	private ResourceLocation resourceLocation;
	private RoomPurpose purpose = RoomPurpose.ROOM;

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

		boolean setYOffsetFromDoorway = false;
		boolean setYOffsetFromPurpose = false;
		int doorwayYOffset = 0;
		int purposeYOffset = 0;
		int yOffset = 0;

		final PlacementSettings placementSettings = new PlacementSettings();
		final Map<BlockPos, String> dataBlocks = getDataBlocks(BlockPos.ORIGIN, placementSettings);
		for (final Entry<BlockPos, String> dataBlock : dataBlocks.entrySet()) {
			final String dataValue = dataBlock.getValue().toLowerCase();
			final BlockPos location = dataBlock.getKey();
			if (dataValue.startsWith("doorway")) {
				doorwayYOffset = -location.getY();
				setYOffsetFromDoorway = true;

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
			} else if (dataValue.startsWith("purpose:")) {
				try
				{
					purpose = RoomPurpose.valueOf(dataValue.substring("purpose:".length()).trim().toUpperCase());
					purposeYOffset = -location.getY();
					setYOffsetFromPurpose = true;
					Logger.info("    data block at location %s set the purpose of the template to ", location, purpose);
				} catch (Exception e) {
					Logger.severe("Could not parse purpose data block");
				}
			}
		}

		if (setYOffsetFromDoorway) {

			this.yOffset = doorwayYOffset;
			Logger.info("    yOffset has been set to %d because of doorways", this.yOffset);
		}
		else if (setYOffsetFromPurpose) {
			this.yOffset = purposeYOffset;
			Logger.info("    yOffset has been set to %d because of purpose", this.yOffset);
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
	 * @param placementIn Placement settings to use
	 */
	public void addBlocksToWorld(ChunkPrimer primer, List<TileEntity> tileEntitiesToAdd, World world, BlockPos pos, PlacementSettings placementIn)
	{
		placementIn.setBoundingBoxFromChunk();

		if (!blocks.isEmpty() && size.getX() >= 1 && size.getY() >= 1 && size.getZ() >= 1)
		{
			final Block block = placementIn.getReplacedBlock();
			final StructureBoundingBox structureboundingbox = placementIn.getBoundingBox();

			for (final BlockInfo templateBlockInfo : blocks)
			{
				final BlockPos blockpos = transformedBlockPos(placementIn, templateBlockInfo.pos).add(pos);

				final Block processedBlock = templateBlockInfo.blockState.getBlock();

				if ((block == null || block != processedBlock) && (!placementIn.getIgnoreStructureBlock() || processedBlock != Blocks.STRUCTURE_BLOCK) && (structureboundingbox == null || structureboundingbox.isVecInside(blockpos)))
				{
					final IBlockState mirrorState = templateBlockInfo.blockState.withMirror(placementIn.getMirror());
					final IBlockState finalState = mirrorState.withRotation(placementIn.getRotation());

					if (primer != null)
					{
						primer.setBlockState(blockpos.getX() & 15, blockpos.getY(), blockpos.getZ() & 15, finalState);
					}

					if (templateBlockInfo.tileentityData != null && processedBlock instanceof ITileEntityProvider) {

						final TileEntity tileEntity = ((ITileEntityProvider) processedBlock).createNewTileEntity(world, processedBlock.getMetaFromState(finalState));
						if (tileEntity != null) {
							templateBlockInfo.tileentityData.setInteger("x", blockpos.getX());
							templateBlockInfo.tileentityData.setInteger("y", blockpos.getY());
							templateBlockInfo.tileentityData.setInteger("z", blockpos.getZ());
							tileEntity.readFromNBT(templateBlockInfo.tileentityData);
							tileEntity.mirror(placementIn.getMirror());
							tileEntity.rotate(placementIn.getRotation());
							tileEntitiesToAdd.add(tileEntity);
						}
					}
				}
			}
		}
	}

	public RoomPurpose getPurpose()
	{
		return purpose;
	}
}
