package com.github.atomicblom.eightyone.world.structure;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import java.util.List;

public class RoomTemplate {
    private final ResourceLocation location;
    private final NxNTemplate template;
    private final Rotation rotation;
    private final Mirror mirror;

    public RoomTemplate(ResourceLocation location, NxNTemplate template, Rotation rotation, Mirror mirror) {
        this.location = location;
        this.template = template;
        this.rotation = rotation;
        this.mirror = mirror;
    }

    public Mirror getMirror() {
        return mirror;
    }

    public Rotation getRotation() {
        return rotation;
    }

    public NxNTemplate getTemplate() {
        return template;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    public void addBlocksToWorld(World world, BlockPos blockPos, PlacementSettings placementSettings) {
        blockPos = updateTemplatePlacement(blockPos, placementSettings);

        template.addBlocksToWorld(world, template.offset(blockPos), placementSettings);
    }

    @Override
    public String toString()
    {
        return "[" + template.getResourceLocation() + ", rotation=" + rotation + ", mirror=" + mirror + "]";
    }

    public void addBlocksToChunkPrimer(ChunkPrimer primer, List<TileEntity> tileEntitiesToAdd, World world, BlockPos blockPos, PlacementSettings placementSettings)
    {
        blockPos = updateTemplatePlacement(blockPos, placementSettings);

        template.addBlocksToWorld(primer, tileEntitiesToAdd, world, template.offset(blockPos), placementSettings);
    }

    public void addEntitiesToWorld(World world, BlockPos blockPos, PlacementSettings placementSettings)
    {
        blockPos = updateTemplatePlacement(blockPos, placementSettings);

        template.addEntitiesToWorld(world, template.offset(blockPos), placementSettings.getMirror(), placementSettings.getRotation(), placementSettings.getBoundingBox());
    }

    public BlockPos updateTemplatePlacement(BlockPos blockPos, PlacementSettings placementSettings)
    {
        placementSettings.setMirror(mirror);
        placementSettings.setRotation(rotation);
        final BlockPos templateSize = template.getSize();

        switch (rotation) {
            case CLOCKWISE_90:
                blockPos = blockPos.add(templateSize.getX() - 1, 0, 0);
                break;
            case CLOCKWISE_180:
                blockPos = blockPos.add(templateSize.getX() - 1, 0, templateSize.getZ() - 1);
                break;
            case COUNTERCLOCKWISE_90:
                blockPos = blockPos.add(0, 0, templateSize.getZ() - 1);
                break;
        }
        return blockPos;
    }
}
