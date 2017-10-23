package com.github.atomicblom.eightyone.util;

public class Point2D
{
	private final int x;
	private final int z;

	public Point2D(int x, int z) {

		this.x = x;
		this.z = z;
	}

	public int getX() {
		return x;
	}

	public int getZ() {
		return z;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final Point2D point2D = (Point2D) o;

		if (x != point2D.x) return false;
		return z == point2D.z;
	}

	@Override
	public int hashCode()
	{
		int result = x;
		result = 31 * result + z;
		return result;
	}
}
