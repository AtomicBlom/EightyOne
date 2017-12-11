package com.github.atomicblom.eightyone.world.structure;

import com.github.atomicblom.eightyone.BlockLibrary;
import com.github.atomicblom.eightyone.Logger;
import com.github.atomicblom.eightyone.Reference;
import com.github.atomicblom.eightyone.util.EntranceHelper;
import com.github.atomicblom.eightyone.util.IterableHelpers;
import com.github.atomicblom.eightyone.util.TemplateCharacteristics;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class NxNTemplate extends Template
{
	private final boolean[] openEntrances = new boolean[EnumFacing.HORIZONTALS.length];
	private int yOffset;
	private boolean spawnable = true;
	private TemplateCharacteristics characteristics;
	private ResourceLocation resourceLocation;
	private RoomPurpose purpose = RoomPurpose.ROOM;

	private NBTTagCompound sourceTagCompound;

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

	public boolean areBlocksAvailable()
	{
		final NBTTagList palette = sourceTagCompound.getTagList("palette", 10);
		for (int i = 0; i < palette.tagCount(); ++i)
		{
			final NBTTagCompound paletteEntry = palette.getCompoundTagAt(i);
			//We're going to use minecraft air to detect blocks that can't be used, so we need to explicitly ok air here
			if (paletteEntry.getString("Name") == "minecraft:air") continue;

			final IBlockState iBlockState = NBTUtil.readBlockState(paletteEntry);
			if (iBlockState == Blocks.AIR) {
				Logger.warning("Cannot use structure " + resourceLocation + " because blockstate " + iBlockState + " is not present.");
				return false;
			}
		}

		return true;
	}

	public List<NBTTagCompound> getMimicBlockStates() {
		final NBTTagList palette = sourceTagCompound.getTagList("palette", 10);

		final String dungeonBlockRegistryName = Reference.Blocks.DUNGEON_BLOCK.toString();
		final String secretBlockRegistryName = Reference.Blocks.SECRET_BLOCK.toString();

		String[] blockNames = new String[palette.tagCount()];

		boolean hasMimicBlocks = false;
		for (int i = 0; i < palette.tagCount(); i++)
		{
			final NBTTagCompound blockStateNbt = palette.getCompoundTagAt(i);
			final String name = blockStateNbt.getString("Name");

			if (name.equals(dungeonBlockRegistryName) || name.equals(secretBlockRegistryName)) {
				blockNames[i] = name;
				hasMimicBlocks = true;
			}
		}

		final List<NBTTagCompound> returnedList = Lists.newArrayList();

		if (!hasMimicBlocks) return returnedList;


		final NBTTagList blocks = sourceTagCompound.getTagList("blocks", 10);

		for (int i = 0; i < blocks.tagCount(); i++)
		{
			final NBTTagCompound blockStateNbt = blocks.getCompoundTagAt(i);
			if (blockStateNbt.hasKey("nbt")) {
				final NBTTagCompound tileEntityNbt = blockStateNbt.getCompoundTag("nbt");

				final String id = tileEntityNbt.getString("id");
				if ("minecraft:dungeon_block".equals(id)) {
					if (tileEntityNbt.hasKey("source")) {
						final NBTTagCompound source = tileEntityNbt.getCompoundTag("source").copy();
						source.setString("Type", blockNames[blockStateNbt.getInteger("state")]);
						returnedList.add(source);
					}
				}
			}
		}

		Set<NBTTagCompound> seen = ConcurrentHashMap.newKeySet();
		return Lists.newArrayList(returnedList.stream().filter(x -> IterableHelpers.distinctNbt(x, seen)).iterator());
	}

	@Override
	public void addBlocksToWorld(World worldIn, BlockPos pos, PlacementSettings placementIn, int flags)
	{
		super.addBlocksToWorld(worldIn, pos, placementIn, 12);
	}

	@Override
	public void read(NBTTagCompound compound)
	{
		sourceTagCompound = compound;

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
	 * @param pos The origin position for the structure
	 * @param placementIn Placement settings to use
	 */
	public void addBlocksToWorld(ChunkPrimer primer, List<TileEntity> tileEntitiesToAdd, World world, BlockPos pos, PlacementSettings placementIn)
	{
		placementIn.setBoundingBoxFromChunk();
		Random rand = new Random();
		StructureBoundingBox boundingBox = placementIn.getBoundingBox();
		rand.setSeed((boundingBox.minX >> 4) * 0x4f9939f508L + (boundingBox.minZ >> 4) * 0x1ef1565bd5L);

		if (!isEmpty())
		{
			final Block block = placementIn.getReplacedBlock();
			final StructureBoundingBox structureboundingbox = boundingBox;

			for (BlockInfo templateBlockInfo : blocks)
			{
				templateBlockInfo = getAlternateBlockInfo(templateBlockInfo, rand);

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

	private boolean isEmpty()
	{
		if (blocks.isEmpty()) return true;
		if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) return true;
		return false;
	}

	private BlockInfo getAlternateBlockInfo(BlockInfo templateBlockInfo, Random r) {
		Block block = templateBlockInfo.blockState.getBlock();
		if (block == BlockLibrary.placeholder_loot_chest) {
			NBTTagCompound tileEntityData = templateBlockInfo.tileentityData;

			String lootTable = tileEntityData.getString("LootTable");
			if (lootTable.isEmpty()) lootTable = "eightyone:loot_chest";
			BlockChest.Type chestType = BlockChest.Type.BASIC;
			Block chestBlock;
			try {
				chestType = BlockChest.Type.valueOf(tileEntityData.getString("ChestType").toUpperCase());
			} catch (Exception e) { }

			chestBlock = chestType == BlockChest.Type.BASIC ? Blocks.CHEST : Blocks.TRAPPED_CHEST;

			IBlockState chestBlockState = chestBlock.getDefaultState()
					.withProperty(
							BlockHorizontal.FACING,
							templateBlockInfo.blockState.getValue(BlockHorizontal.FACING)
					);

			TileEntityChest tileEntityChest = new TileEntityChest();
			ResourceLocation lootTableRL = new ResourceLocation(lootTable);
			tileEntityChest.setLootTable(new ResourceLocation(lootTableRL.getResourceDomain(), "chests/" + lootTableRL.getResourcePath()), r.nextLong());
			tileEntityChest.setPos(templateBlockInfo.pos);
			NBTTagCompound chestNbt = new NBTTagCompound();
			tileEntityChest.writeToNBT(chestNbt);

			templateBlockInfo = new BlockInfo(templateBlockInfo.pos, chestBlockState, chestNbt);
		}
		return templateBlockInfo;
	}

	public RoomPurpose getPurpose()
	{
		return purpose;
	}
}
