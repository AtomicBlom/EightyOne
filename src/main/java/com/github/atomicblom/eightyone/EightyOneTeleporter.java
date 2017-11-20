package com.github.atomicblom.eightyone;

import com.github.atomicblom.eightyone.world.NxNChunkGenerator;
import com.github.atomicblom.eightyone.world.NxNWorldProvider;
import com.github.atomicblom.eightyone.world.Room;
import com.github.atomicblom.eightyone.world.RoomProperties;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Teleporter;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;

import java.util.Iterator;

public class EightyOneTeleporter extends Teleporter {
    public static EightyOneTeleporter getTeleporterForDim(MinecraftServer server, int dim) {
        WorldServer ws = server.getWorld(dim);

        for (Teleporter t : ws.customTeleporters) {
            if (t instanceof EightyOneTeleporter) {
                return (EightyOneTeleporter) t;
            }
        }

        EightyOneTeleporter tp = new EightyOneTeleporter(ws);
        ws.customTeleporters.add(tp);
        return tp;
    }

    private EightyOneTeleporter(WorldServer dest) {
        super(dest);
    }

    @Override
    public boolean makePortal(Entity entityIn) {
        WorldProvider provider = entityIn.getEntityWorld().provider;
        BlockPos position = entityIn.getPosition();
        if (provider instanceof NxNWorldProvider) {
            NxNChunkGenerator chunkGenerator = (NxNChunkGenerator) provider.createChunkGenerator();
            Room startingRoom = chunkGenerator.getRoomAt(position.getX(), position.getZ());
            BlockPos startPos = new BlockPos(startingRoom.getX(), chunkGenerator.getFloorHeight(), startingRoom.getZ());
            for (BlockPos pos : new SpiralIterable(startPos, 10) ) {
                Room spawnRoom = chunkGenerator.getRoomAt(pos.getX(), pos.getZ());
                if (startingRoom.hasProperty(RoomProperties.IsPresent)) {
                    makePortal(entityIn.world, new BlockPos(pos));
                    break;
                }
            }
        }
    }

    private void makePortal(World world, BlockPos blockPos) {
        
    }

    public static class SpiralIterable implements Iterable<BlockPos> {

        private final BlockPos startPos;
        private int stepSize;

        public SpiralIterable(BlockPos startPos, int stepSize) {
            this.startPos = startPos;
            this.stepSize = stepSize;
        }

        @Override
        public Iterator<BlockPos> iterator() {
            return new SpiralIterator(startPos, stepSize);
        }
    }

    public static class SpiralIterator implements Iterator<BlockPos> {

        private int dx;
        private int dz;
        private int segment_length;
        private int x;
        private int z;
        private int segment_passed;
        private final BlockPos.MutableBlockPos returnedLocation;
        private final int stepSize;

        public SpiralIterator(BlockPos startingPoint, int stepSize) {
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
            returnedLocation = new BlockPos.MutableBlockPos(startingPoint);
        }

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public BlockPos next() {
            returnedLocation.setPos(x, returnedLocation.getY(), z);

            // make a step, add 'direction' vector (dx, dz) to current position (x, z)
            x += dx * stepSize;
            z += dz * stepSize;
            ++segment_passed;

            if (segment_passed == segment_length) {
                // done with current segment
                segment_passed = 0;

                // 'rotate' directions
                int buffer = dx;
                dx = -dz;
                dz = buffer;

                // increase segment length if necessary
                if (dz == 0) {
                    ++segment_length;
                }
            }


            return returnedLocation;

        }
    }
}
