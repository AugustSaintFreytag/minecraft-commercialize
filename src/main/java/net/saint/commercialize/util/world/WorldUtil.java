package net.saint.commercialize.util.world;

import java.util.function.Consumer;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public final class WorldUtil {

	public static void forEachChunkAroundPosition(World world, BlockPos position, int radiusInChunks, Consumer<WorldChunk> consumer) {
		var chunkX = position.getX() >> 4;
		var chunkZ = position.getZ() >> 4;

		for (var dx = -radiusInChunks; dx <= radiusInChunks; dx++) {
			for (var dz = -radiusInChunks; dz <= radiusInChunks; dz++) {
				var currentChunkX = chunkX + dx;
				var currentChunkZ = chunkZ + dz;
				var chunk = world.getChunk(currentChunkX, currentChunkZ);

				consumer.accept(chunk);
			}
		}
	}

	public static <T extends BlockEntity> void forEachBlockEntityInChunk(WorldChunk chunk, Class<T> type, Consumer<T> consumer) {
		chunk.getBlockEntities().values().forEach(blockEntity -> {
			if (type.isInstance(blockEntity)) {
				consumer.accept(type.cast(blockEntity));
			}
		});
	}

}
