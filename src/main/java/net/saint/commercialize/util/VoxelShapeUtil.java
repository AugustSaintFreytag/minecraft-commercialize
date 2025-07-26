package net.saint.commercialize.util;

import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public final class VoxelShapeUtil {

	public static VoxelShape rotateShape(VoxelShape shape, Direction direction) {
		if (direction == Direction.NORTH) {
			return shape;
		}

		var result = new AtomicReference<>(VoxelShapes.empty());

		shape.forEachBox((x1, y1, z1, x2, y2, z2) -> {
			double nx1, nz1, nx2, nz2;
			switch (direction) {
			case EAST:
				nx1 = 1 - z2;
				nz1 = x1;
				nx2 = 1 - z1;
				nz2 = x2;
				break;
			case SOUTH:
				nx1 = 1 - x2;
				nz1 = 1 - z2;
				nx2 = 1 - x1;
				nz2 = 1 - z1;
				break;
			case WEST:
				nx1 = z1;
				nz1 = 1 - x2;
				nx2 = z2;
				nz2 = 1 - x1;
				break;
			default:
				nx1 = x1;
				nz1 = z1;
				nx2 = x2;
				nz2 = z2;
			}

			result.set(VoxelShapes.union(result.get(), VoxelShapes.cuboid(nx1, y1, nz1, nx2, y2, nz2)));
		});

		return result.get();
	}

}
