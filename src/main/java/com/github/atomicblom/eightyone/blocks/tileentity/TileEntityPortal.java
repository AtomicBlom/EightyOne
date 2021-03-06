package com.github.atomicblom.eightyone.blocks.tileentity;

import com.github.atomicblom.eightyone.EightyOne;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TileEntityPortal extends TileEntity implements ITickable
{
	private float artifactRotation;
	private long pulse;
	private float shadowRotation;

	@Override
	public boolean hasFastRenderer()
	{
		return super.hasFastRenderer();
	}

	public float getArtifactRotation() {
		return artifactRotation;
	}

	public void setArtifactRotation(float artifactRotation) {
		this.artifactRotation = artifactRotation;
	}

	public void setPulse(long pulse)
	{
		this.pulse = pulse;
	}

	public long getPulse()
	{
		return pulse;
	}

	public void setShadowRotation(float shadowRotation)
	{
		this.shadowRotation = shadowRotation;
	}

	public float getShadowRotation()
	{
		return shadowRotation;
	}

	AxisAlignedBB axisAlignedBB;

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (axisAlignedBB == null) {
			axisAlignedBB = new AxisAlignedBB(pos.getX() - 5, pos.getY() - 5, pos.getZ() - 5, pos.getX() + 5, pos.getY() + 5, pos.getZ() + 5);
		}

		return axisAlignedBB;
	}

	@Override
	public boolean shouldRenderInPass(int pass)
	{
		return pass == 0 || pass == 1;
	}

	// For reference
//	private int[][] cornerOffsets = {
//			{-4, 0, -4},
//			{4, 0, -4},
//			{-4, 0, 4},
//			{4, 0, 4},
//			{-4, 5, -4},
//			{4, 5, -4},
//			{-4, 5, 4},
//			{4, 5, 4}
//	};

	private Boolean isValid = null;
	public boolean isValid() {
		if (isValid == null) {
			isValid = checkValidStructure();
		}
		return isValid;
	}

	@Override
	public void update()
	{
		if ((world.getTotalWorldTime() & 3) != 0) return;

		isValid = checkValidStructure();
		if (!world.isRemote)
		{
			final BlockPos underneath = pos.down();
			final IBlockState blockState = world.getBlockState(underneath);
			if (!blockState.getBlock().isAir(blockState, world, underneath))
			{
				world.destroyBlock(underneath, true);
			}
		}
	}

	enum BlockType
	{
		VerticalPillar, LowerRow, UpperRow, Corner
	}

	private PortalProgressData[] progressData;
	public PortalProgressData[] getProgressData() {
		return progressData;
	}

	private final Map<Block, AtomicInteger> possibleCornerBlocks = Maps.newHashMap();
	private final Map<Block, AtomicInteger> possibleVerticalPillarBlocks = Maps.newHashMap();
	private final Map<Block, AtomicInteger> possibleLowerRowBlocks = Maps.newHashMap();
	private final Map<Block, AtomicInteger> possibleUpperRowBlocks = Maps.newHashMap();

	public boolean checkValidStructure()
	{
		if (progressData == null) {
			createProgressData();
		}

		final Iterable<AtomicInteger> blockCounts = Iterables.concat(
				possibleCornerBlocks.values(),
				possibleVerticalPillarBlocks.values(),
				possibleLowerRowBlocks.values(),
				possibleUpperRowBlocks.values()
		);

		for (final AtomicInteger atomicInteger : blockCounts)
		{
			atomicInteger.set(0);
		}

		//First instance of Corner
		Block cornerBlock = Blocks.AIR;
		int cornerBlockCount = 0;
		//First instance of UpperRow
		Block upperRowBlock = Blocks.AIR;
		int upperRowBlockCount = 0;
		//First instance of LowerRow
		Block lowerRowBlock = Blocks.AIR;
		int lowerRowBlockCount = 0;
		//First instance of VerticalPillar
		Block verticalPillarBlock = Blocks.AIR;
		int verticalPillarBlockCount = 0;

		//Calculate the majority blocks for each of the parts.
		for (int i = 0; i < progressData.length; ++i) {
			final PortalProgressData currentProgressData = progressData[i];
			if (currentProgressData == null) continue;
			currentProgressData.updateCurrentBlock(world);
			if (currentProgressData.currentlyAir) continue;
			Block block = currentProgressData.currentBlock;

			switch (currentProgressData.getType()) {
				case Corner:
					final AtomicInteger possibleCornerBlocksCountInt = possibleCornerBlocks.computeIfAbsent(block, (x) -> new AtomicInteger());
					final int possibleCornerBlocksCount = possibleCornerBlocksCountInt.incrementAndGet();
					if (possibleCornerBlocksCount > cornerBlockCount) {
						cornerBlock = block;
						cornerBlockCount = possibleCornerBlocksCount;
					}
					break;
				case VerticalPillar:
					final AtomicInteger possibleVerticalPillarCountInt = possibleVerticalPillarBlocks.computeIfAbsent(block, (x) -> new AtomicInteger());
					final int possibleVerticalPillarCount = possibleVerticalPillarCountInt.incrementAndGet();
					if (possibleVerticalPillarCount > verticalPillarBlockCount) {
						verticalPillarBlock = block;
						verticalPillarBlockCount = possibleVerticalPillarCount;
					}
					break;
				case LowerRow:
					final AtomicInteger possibleLowerRowCountInt = possibleLowerRowBlocks.computeIfAbsent(block, (x) -> new AtomicInteger());
					final int possibleLowerRowCount = possibleLowerRowCountInt.incrementAndGet();
					if (possibleLowerRowCount > lowerRowBlockCount) {
						lowerRowBlock = block;
						lowerRowBlockCount = possibleLowerRowCount;
					}
					break;
				case UpperRow:
					final AtomicInteger possibleUpperRowCountInt = possibleUpperRowBlocks.computeIfAbsent(block, (x) -> new AtomicInteger());
					final int possibleUpperRowCount = possibleUpperRowCountInt.incrementAndGet();
					if (possibleUpperRowCount > upperRowBlockCount) {
						upperRowBlock = block;
						upperRowBlockCount = possibleUpperRowCount;
					}
					break;
			}
		}

		boolean isValid = true;
		for (int i = 0; i < progressData.length; ++i) {
			if (progressData[i] == null) continue;
			Block checkBlock = Blocks.AIR;
			switch (progressData[i].getType()) {
				case Corner:
					checkBlock = cornerBlock; break;
				case VerticalPillar:
					checkBlock = verticalPillarBlock; break;
				case LowerRow:
					checkBlock = lowerRowBlock; break;
				case UpperRow:
					checkBlock = upperRowBlock; break;
			}
			isValid &= progressData[i].checkBlock(checkBlock);
		}

		return EightyOne.DEBUG_FORCE_ALLOW_PORTAL ? true : isValid;
	}

	private void createProgressData()
	{
		final int cornerBlocks = 8;
		final int verticalPillars = 4 * 4;
		final int lowerRowBlocks = 7 * 4;
		final int upperRowBlocks = 7 * 4;

		progressData = new PortalProgressData[cornerBlocks + verticalPillars + upperRowBlocks + lowerRowBlocks];
		int index = 0;
		int renderSet = 0;
		progressData[index++] = new PortalProgressData(BlockType.Corner, pos.add(-4, -2, -4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(-4, -1, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-3, -2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-4, -2, -3), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(-4, 0, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-2, -2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-4, -2, -2), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(-4, 1, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-1, -2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-4, -2, -1), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(-4, 2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(0, -2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-4, -2, 0), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.Corner, pos.add(-4, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(1, -2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-4, -2, 1), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-3, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-4, 3, -3), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(2, -2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-4, -2, 2), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-2, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-4, 3, -2), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(3, -2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-4, -2, 3), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-1, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-4, 3, -1), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.Corner, pos.add(4, -2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.Corner, pos.add(-4, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(0, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-4, 3, 0), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(4, -1, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(-4, -1, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(4, -2, -3), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-3, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(1, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-4, 3, 1), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(4, 0, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(-4, 0, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(4, -2, -2), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-2, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(2, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-4, 3, 2), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(4, 1, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(-4, 1, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(4, -2, -1), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(-1, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(3, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-4, 3, 3), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(4, 2, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(-4, 2, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(4, -2, 0), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(0, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.Corner, pos.add(4, 3, -4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.Corner, pos.add(-4, 3, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(4, -2, 1), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(1, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(4, 3, -3), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-3, 3, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(4, -2, 2), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(2, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(4, 3, -2), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-2, 3, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(4, -2, 3), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.LowerRow, pos.add(3, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(4, 3, -1), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(-1, 3, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.Corner, pos.add(4, -2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(4, 3, 0), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(0, 3, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(4, -1, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(4, 3, 1), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(1, 3, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(4, 0, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(4, 3, 2), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(2, 3, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(4, 1, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(4, 3, 3), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.UpperRow, pos.add(3, 3, 4), renderSet);
		progressData[index++] = new PortalProgressData(BlockType.VerticalPillar, pos.add(4, 2, 4), renderSet);

		renderSet++;
		progressData[index++] = new PortalProgressData(BlockType.Corner, pos.add(4, 3, 4), renderSet);
	}
}
