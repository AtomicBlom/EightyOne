package com.github.atomicblom.eightyone.blocks.tileentity;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TileEntityPortal extends TileEntity implements ITickable
{
	private float yRotation;
	private long pulse;
	private float pulseRotation;
	private long nextUpdate;

	@Override
	public boolean hasFastRenderer()
	{
		return super.hasFastRenderer();
	}

	public float getYRotation() {
		return yRotation;
	}

	public void setYRotation(float yRotation) {
		this.yRotation = yRotation;
	}

	public void setPulse(long pulse)
	{
		this.pulse = pulse;
	}

	public long getPulse()
	{
		return pulse;
	}

	public void setPulseRotation(float pulseRotation)
	{
		this.pulseRotation = pulseRotation;
	}

	public float getPulseRotation()
	{
		return pulseRotation;
	}

//	@Override
//	public void update()
//	{
//		if (world.isRemote) {
//			if (world.getTotalWorldTime() > nextUpdate) {
//
//				nextUpdate = world.getTotalWorldTime() + 100; // 5 seconds from now
//				MinecraftForge.EVENT_BUS.post(new CheckTileEntityVisibleEvent(this));
//			}
//		}
//	}


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

	private int[][] cornerOffsets = {
			{-4, 0, -4},
			{4, 0, -4},
			{-4, 0, 4},
			{4, 0, 4},
			{-4, 5, -4},
			{4, 5, -4},
			{-4, 5, 4},
			{4, 5, 4}
	};

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

	public static class PortalProgressData {
		private final BlockType type;
		public final BlockPos pos;

		public final int renderSet;
		public boolean currentlyValid = false;
		public boolean currentlyAir = true;
		public double distanceFromPlayer;
		public Block currentBlock;
		private IBlockState currentBlockState;

		public PortalProgressData(BlockType type, BlockPos pos, int renderSet)
		{
			this.type = type;
			this.pos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
			this.renderSet = 21 - renderSet;
		}

		public boolean checkBlock(Block expectedBlock) {
			if (currentlyAir) { return false; }
			currentlyValid = false;
			currentlyAir = false;
			if (expectedBlock == null)
			{
				currentlyAir = true;
				return false;
			}

			if (currentBlock == expectedBlock) {
				currentlyValid = true;
			}
			return currentlyValid;
		}

		public void updateCurrentBlock(World world)
		{
			currentBlockState = world.getBlockState(pos);
			currentBlock = currentBlockState.getBlock();
			currentlyAir = currentBlock.isAir(currentBlockState, world, pos);
			if (currentlyAir) {
				currentlyValid = false;
			}
		}
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
		Block cornerBlock = null;
		int cornerBlockCount = 0;
		//First instance of UpperRow
		Block upperRowBlock = null;
		int upperRowBlockCount = 0;
		//First instance of LowerRow
		Block lowerRowBlock = null;
		int lowerRowBlockCount = 0;
		//First instance of VerticalPillar
		Block verticalPillarBlock = null;
		int verticalPillarBlockCount = 0;

		for (int i = 0; i < progressData.length; ++i) {
			final PortalProgressData currentProgressData = progressData[i];
			if (currentProgressData == null) continue;
			currentProgressData.updateCurrentBlock(world);
			if (currentProgressData.currentlyAir) continue;
			Block block = currentProgressData.currentBlock;

			switch (currentProgressData.type) {
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
			Block checkBlock = null;
			switch (progressData[i].type) {
				case Corner:
					checkBlock = cornerBlock; break;
				case VerticalPillar:
					checkBlock = verticalPillarBlock; break;
				case LowerRow:
					checkBlock = lowerRowBlock; break;
				case UpperRow:
					checkBlock = upperRowBlock; break;
			}
			if (checkBlock == null) continue;
			isValid &= progressData[i].checkBlock(checkBlock);
		}

		return isValid;
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
