package com.github.atomicblom.eightyone.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import java.util.Iterator;

public class SpiralIterable implements Iterable<BlockPos> {

    private final BlockPos startingPoint;
    private final int stepSize;

    public SpiralIterable(BlockPos startingPoint, int stepSize) {
        this.startingPoint = startingPoint;
        this.stepSize = stepSize;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new SpiralIterator(startingPoint, stepSize);
    }

    public static class SpiralIterator implements Iterator<BlockPos>
    {

        private final int stepSize;
        private int dx;
        private int dz;
        private int segment_length;
        private int x;
        private int z;
        private int segment_passed;
        private final MutableBlockPos returnedLocation;

        SpiralIterator(BlockPos startingPoint, int stepSize)
        {
            this.stepSize = stepSize;
            // (dx, dz) is a vector - direction in which we move right now
            dx = 1;
            dz = 0;
            // length of current segment
            segment_length = 1;

            // current position (x, z) and how much of current segment we passed
            x = startingPoint.getX();
            z = startingPoint.getZ();
            segment_passed = 0;
            returnedLocation = new MutableBlockPos(startingPoint);
        }

        @Override
        public boolean hasNext()
        {
            return true;
        }

        @Override
        public BlockPos next()
        {
            returnedLocation.setPos(x, returnedLocation.getY(), z);

            // make a step, add 'direction' vector (dx, dz) to current position (x, z)
            x += dx * stepSize;
            z += dz * stepSize;
            ++segment_passed;

            if (segment_passed == segment_length)
            {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                final int buffer = dx;
                dx = -dz;
                dz = buffer;

                // increase segment length if necessary
                if (dz == 0)
                {
                    ++segment_length;
                }
            }


            return returnedLocation;

        }
    }
}
