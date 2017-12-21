package com.github.atomicblom.eightyone.blocks.tileentity;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

	@Override
	public void update()
	{
		checkValidStructure();
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
		public double distanceFromPlayer;

		public PortalProgressData(BlockType type, BlockPos pos, int renderSet)
		{
			this.type = type;
			this.pos = pos;
			this.renderSet = renderSet;
		}

		public boolean checkBlock(World world, Block expectedBlock) {
			currentlyValid = false;
			if (expectedBlock != null && expectedBlock != Blocks.AIR)
			{
				if (world.getBlockState(pos).getBlock() == expectedBlock) {
					currentlyValid = true;
				}
			}
			return currentlyValid;
		}
	}

	private PortalProgressData[] progressData;
	public PortalProgressData[] getProgressData() {
		return progressData;
	}

	public boolean checkValidStructure()
	{
		if (progressData == null) {
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

		//First instance of Corner
		Block cornerBlock = world.getBlockState(progressData[0].pos).getBlock();
		//First instance of UpperRow
		Block upperRowBlock = world.getBlockState(progressData[16].pos).getBlock();
		//First instance of LowerRow
		Block lowerRowBlock = world.getBlockState(progressData[2].pos).getBlock();
		//First instance of VerticalPillar
		Block verticalPillarBlock = world.getBlockState(progressData[1].pos).getBlock();

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
			isValid |= progressData[i].checkBlock(world, checkBlock);
		}

		return isValid;
	}
}
