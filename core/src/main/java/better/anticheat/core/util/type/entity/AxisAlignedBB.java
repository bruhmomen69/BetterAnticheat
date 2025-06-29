package better.anticheat.core.util.type.entity;

import better.anticheat.core.util.type.Pair;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AxisAlignedBB implements IAxisAlignedBoundingBox {
    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    public AxisAlignedBB(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(maxX, minX);
        this.maxY = Math.max(maxY, minY);
        this.maxZ = Math.max(maxZ, minZ);
    }

    public AxisAlignedBB(final double x, final double y, final double z, final double width, final double height) {
        this(x, y, z, x + (width / 2.0F), y + height, z + (width / 2.0F));
    }

    @Override
    public AxisAlignedBB expand(final double x, final double y, final double z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;

        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;

        return this;
    }

    @Override
    public AxisAlignedBB expandMax(final double x, final double y, final double z) {
        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;

        return this;
    }

    @Override
    public AxisAlignedBB expandMin(final double x, final double y, final double z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;

        return this;
    }


    @Override
    public AxisAlignedBB add(final double x, final double y, final double z) {
        this.minX += x;
        this.minY += y;
        this.minZ += z;

        this.maxX += x;
        this.maxY += y;
        this.maxZ += z;

        return this;
    }

    @Override
    public boolean collides(IAxisAlignedBoundingBox other) {
        return other.getMaxX() >= this.minX
                && other.getMinX() <= this.maxX
                && other.getMaxY() >= this.minY
                && other.getMinY() <= this.maxY
                && other.getMaxZ() >= this.minZ
                && other.getMinZ() <= this.maxZ;
    }

    @Override
    public boolean collidesUnder(IAxisAlignedBoundingBox other) {
        return maxY == other.getMinY()
                && minZ < other.getMaxZ()
                && minX < other.getMaxX()
                && maxZ > other.getMinZ()
                && maxX > other.getMinX();
    }

    @Override
    public boolean collidesAbove(IAxisAlignedBoundingBox other) {
        return minY == other.getMaxY()
                && minZ < other.getMaxZ()
                && minX < other.getMaxX()
                && maxZ > other.getMinZ()
                && maxX > other.getMinX();
    }

    @Override
    public boolean collidesHorizontally(IAxisAlignedBoundingBox other) {
        boolean vertical = maxY > other.getMinY() && minY < other.getMaxY();
        boolean horizontal = minX == other.getMaxX() || maxX == other.getMinX() || minZ == other.getMaxX() || maxZ == other.getMinZ();

        return vertical && horizontal;
    }

    @Override
    public Vector3d calculateIntercept(Vector3d vecA, Vector3d vecB) {
        var vec3 = getIntermediateWithXValue(vecA, vecB, this.minX);
        var vec31 = getIntermediateWithXValue(vecA, vecB, this.maxX);
        var vec32 = getIntermediateWithYValue(vecA, vecB, this.minY);
        var vec33 = getIntermediateWithYValue(vecA, vecB, this.maxY);
        var vec34 = getIntermediateWithZValue(vecA, vecB, this.minZ);
        var vec35 = getIntermediateWithZValue(vecA, vecB, this.maxZ);

        if (!this.isVecInYZ(vec3)) {
            vec3 = null;
        }

        if (!this.isVecInYZ(vec31)) {
            vec31 = null;
        }

        if (!this.isVecInXZ(vec32)) {
            vec32 = null;
        }

        if (!this.isVecInXZ(vec33)) {
            vec33 = null;
        }

        if (!this.isVecInXY(vec34)) {
            vec34 = null;
        }

        if (!this.isVecInXY(vec35)) {
            vec35 = null;
        }

        Vector3d vec36 = null;

        if (vec3 != null) {
            vec36 = vec3;
        }

        if (vec31 != null && (vec36 == null || squareDistanceTo(vecA, vec31) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec31;
        }

        if (vec32 != null && (vec36 == null || squareDistanceTo(vecA, vec32) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec32;
        }

        if (vec33 != null && (vec36 == null || squareDistanceTo(vecA, vec33) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec33;
        }

        if (vec34 != null && (vec36 == null || squareDistanceTo(vecA, vec34) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec34;
        }

        if (vec35 != null && (vec36 == null || squareDistanceTo(vecA, vec35) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec35;
        }

        return vec36;
    }

    @Override
    public Pair<Vector3d, BlockFace> calculateInterceptWithDirection(Vector3d vecA, Vector3d vecB) {
        var vec3 = getIntermediateWithXValue(vecA, vecB, this.minX);
        var vec31 = getIntermediateWithXValue(vecA, vecB, this.maxX);
        var vec32 = getIntermediateWithYValue(vecA, vecB, this.minY);
        var vec33 = getIntermediateWithYValue(vecA, vecB, this.maxY);
        var vec34 = getIntermediateWithZValue(vecA, vecB, this.minZ);
        var vec35 = getIntermediateWithZValue(vecA, vecB, this.maxZ);

        if (!this.isVecInYZ(vec3)) {
            vec3 = null;
        }

        if (!this.isVecInYZ(vec31)) {
            vec31 = null;
        }

        if (!this.isVecInXZ(vec32)) {
            vec32 = null;
        }

        if (!this.isVecInXZ(vec33)) {
            vec33 = null;
        }

        if (!this.isVecInXY(vec34)) {
            vec34 = null;
        }

        if (!this.isVecInXY(vec35)) {
            vec35 = null;
        }

        Vector3d vec36 = null;

        if (vec3 != null) {
            vec36 = vec3;
        }

        if (vec31 != null && (vec36 == null || squareDistanceTo(vecA, vec31) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec31;
        }

        if (vec32 != null && (vec36 == null || squareDistanceTo(vecA, vec32) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec32;
        }

        if (vec33 != null && (vec36 == null || squareDistanceTo(vecA, vec33) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec33;
        }

        if (vec34 != null && (vec36 == null || squareDistanceTo(vecA, vec34) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec34;
        }

        if (vec35 != null && (vec36 == null || squareDistanceTo(vecA, vec35) < squareDistanceTo(vecA, vec36))) {
            vec36 = vec35;
        }

        if (vec36 == null) {
            return new Pair<>(null, null);
        } else {
            BlockFace enumfacing;

            if (vec36 == vec3) {
                enumfacing = BlockFace.WEST;
            } else if (vec36 == vec31) {
                enumfacing = BlockFace.EAST;
            } else if (vec36 == vec32) {
                enumfacing = BlockFace.DOWN;
            } else if (vec36 == vec33) {
                enumfacing = BlockFace.UP;
            } else if (vec36 == vec34) {
                enumfacing = BlockFace.NORTH;
            } else {
                enumfacing = BlockFace.SOUTH;
            }

            return new Pair<>(vec36, enumfacing);
        }
    }

    @Override
    public IAxisAlignedBoundingBox copy() {
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public double posX() {
        return (maxX + minX) / 2.0;
    }

    @Override
    public double posY() {
        return minY;
    }

    @Override
    public double posZ() {
        return (maxZ + minZ) / 2.0;
    }

    @Override
    public IAxisAlignedBoundingBox offset(double x, double y, double z) {
        return new AxisAlignedBB(this.minX + x, this.minY + y, this.minZ + z, this.maxX + x, this.maxY + y, this.maxZ + z);
    }

    /**
     * @param vec The com.github.retrooper.packetevents.protocol.world.Location
     * @return if the vector is inside the current entity
     */
    @Override
    public boolean isVecInYZ(Vector3d vec) {
        return vec != null && vec.y >= this.minY && vec.y <= this.maxY && vec.z >= this.minZ && vec.z <= this.maxZ;
    }

    @Override
    public boolean isVecInXZ(Vector3d vec) {
        return vec != null && vec.x >= this.minX && vec.x <= this.maxX && vec.z >= this.minZ && vec.z <= this.maxZ;
    }

    @Override
    public boolean isVecInXY(Vector3d vec) {
        return vec != null && vec.x >= this.minX && vec.x <= this.maxX && vec.y >= this.minY && vec.y <= this.maxY;
    }

    @Override
    public Vector3d getIntermediateWithXValue(Vector3d original, Vector3d vec, double x) {
        var d0 = vec.x - original.x;
        var d1 = vec.y - original.y;
        var d2 = vec.z - original.z;

        if (d0 * d0 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (x - original.x) / d0;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(original.x + d0 * d3, original.y + d1 * d3, original.z + d2 * d3) : null;
        }
    }

    @Override
    public Vector3d getIntermediateWithYValue(Vector3d original, Vector3d vec, double y) {
        double d0 = vec.x - original.x;
        double d1 = vec.y - original.y;
        double d2 = vec.z - original.z;

        if (d1 * d1 < 1.0000000116860974E-7D) {
            return null;
        } else {
            double d3 = (y - original.y) / d1;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(original.x + d0 * d3, original.y + d1 * d3, original.z + d2 * d3) : null;
        }
    }

    @Override
    public Vector3d getIntermediateWithZValue(Vector3d original, Vector3d vec, double z) {
        var d0 = vec.x - original.x;
        var d1 = vec.y - original.y;
        var d2 = vec.z - original.z;

        if (d2 * d2 < 1.0000000116860974E-7D) {
            return null;
        } else {
            var d3 = (z - original.z) / d2;
            return d3 >= 0.0D && d3 <= 1.0D ? new Vector3d(original.x + d0 * d3, original.y + d1 * d3, original.z + d2 * d3) : null;
        }
    }

    @Override
    public double squareDistanceTo(Vector3d original, Vector3d vec) {
        double d0 = vec.x - original.x;
        double d1 = vec.y - original.y;
        double d2 = vec.z - original.z;
        return d0 * d0 + d1 * d1 + d2 * d2;
    }

    /**
     * Gets the distance to another box
     *
     * @param box the other box
     * @return la distance
     */
    public double distance(final AxisAlignedBB box) {
        double minDiffX = box.minX - minX;
        double maxDiffX = box.maxX - maxX;
        double minDiffY = box.minY - minY;
        double maxDiffY = box.maxY - maxY;
        double minDiffZ = box.minZ - minZ;
        double maxDiffZ = box.maxZ - maxZ;

        final double dx = Math.min(minDiffX * minDiffX, maxDiffX * maxDiffX);
        final double dy = Math.min(minDiffY * minDiffY, maxDiffY * maxDiffY);
        final double dz = Math.min(minDiffZ * minDiffZ, maxDiffZ * maxDiffZ);
        return Math.sqrt(dx + dy + dz);
    }

    /**
     * Returns whether the given bounding box intersects with this one. Args: axisAlignedBB
     */
    public boolean intersectsWith(IAxisAlignedBoundingBox other) {
        return other.getMaxX() >= this.minX && other.getMinX() <= this.maxX && (other.getMaxY() >= this.minY && other.getMinY() <= this.maxY && other.getMaxZ() > this.minZ && other.getMinZ() <= this.maxZ);
    }

    public AxisAlignedBB addCoord(double x, double y, double z) {
        double d0 = this.minX;
        double d1 = this.minY;
        double d2 = this.minZ;
        double d3 = this.maxX;
        double d4 = this.maxY;
        double d5 = this.maxZ;

        if (x < 0.0D) {
            d0 += x;
        } else if (x > 0.0D) {
            d3 += x;
        }

        if (y < 0.0D) {
            d1 += y;
        } else if (y > 0.0D) {
            d4 += y;
        }

        if (z < 0.0D) {
            d2 += z;
        } else if (z > 0.0D) {
            d5 += z;
        }

        return new AxisAlignedBB(d0, d1, d2, d3, d4, d5);
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and Z dimensions, calculate the offset between them
     * in the X dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateXOffset(IAxisAlignedBoundingBox other, double offsetX) {
        if (other.getMaxY() > this.minY && other.getMinY() < this.maxY && other.getMaxZ() > this.minZ && other.getMinZ() < this.maxZ) {
            if (offsetX > 0.0D && other.getMaxX() <= this.minX) {
                double d1 = this.minX - other.getMaxX();

                if (d1 < offsetX) {
                    offsetX = d1;
                }
            } else if (offsetX < 0.0D && other.getMinX() >= this.maxX) {
                double d0 = this.maxX - other.getMinX();

                if (d0 > offsetX) {
                    offsetX = d0;
                }
            }

        }
        return offsetX;
    }

    /**
     * if instance and the argument bounding boxes overlap in the X and Z dimensions, calculate the offset between them
     * in the Y dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateYOffset(IAxisAlignedBoundingBox other, double offsetY) {
        if (other.getMaxX() > this.minX && other.getMinX() < this.maxX && other.getMaxZ() > this.minZ && other.getMinZ() < this.maxZ) {
            if (offsetY > 0.0D && other.getMaxY() <= this.minY) {
                double d1 = this.minY - other.getMaxY();

                if (d1 < offsetY) {
                    offsetY = d1;
                }
            } else if (offsetY < 0.0D && other.getMinY() >= this.maxY) {
                double d0 = this.maxY - other.getMinY();

                if (d0 > offsetY) {
                    offsetY = d0;
                }
            }

        }
        return offsetY;
    }

    /**
     * if instance and the argument bounding boxes overlap in the Y and X dimensions, calculate the offset between them
     * in the Z dimension.  return var2 if the bounding boxes do not overlap or if var2 is closer to 0 then the
     * calculated offset.  Otherwise return the calculated offset.
     */
    public double calculateZOffset(IAxisAlignedBoundingBox other, double offsetZ) {
        if (other.getMaxX() > this.minX && other.getMinX() < this.maxX && other.getMaxY() > this.minY && other.getMinY() < this.maxY) {
            if (offsetZ > 0.0D && other.getMaxZ() <= this.minZ) {
                double d1 = this.minZ - other.getMaxZ();

                if (d1 < offsetZ) {
                    offsetZ = d1;
                }
            } else if (offsetZ < 0.0D && other.getMinZ() >= this.maxZ) {
                double d0 = this.maxZ - other.getMinZ();

                if (d0 > offsetZ) {
                    offsetZ = d0;
                }
            }

        }
        return offsetZ;
    }

    public AxisAlignedBB subtractMin(final double x, final double y, final double z) {
        this.minX -= x;
        this.minY -= y;
        this.minZ -= z;
        return this;
    }

    public AxisAlignedBB subtractMax(final double x, final double y, final double z) {
        this.maxX -= x;
        this.maxY -= y;
        this.maxZ -= z;
        return this;
    }

    @Override
    public int hashCode() {
        int result = Double.hashCode(minX);
        result = 31 * result + Double.hashCode(minY);
        result = 31 * result + Double.hashCode(minZ);
        result = 31 * result + Double.hashCode(maxX);
        result = 31 * result + Double.hashCode(maxY);
        result = 31 * result + Double.hashCode(maxZ);
        return result;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AxisAlignedBB)) return false;

        AxisAlignedBB axisAlignedBB = (AxisAlignedBB) o;
        return Double.compare(minX, axisAlignedBB.minX) == 0 && Double.compare(minY, axisAlignedBB.minY) == 0 && Double.compare(minZ, axisAlignedBB.minZ) == 0 && Double.compare(maxX, axisAlignedBB.maxX) == 0 && Double.compare(maxY, axisAlignedBB.maxY) == 0 && Double.compare(maxZ, axisAlignedBB.maxZ) == 0;
    }

    @Override
    public AxisAlignedBB clone() {
        return new AxisAlignedBB(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public String toString() {
        return "AxisAlignedBB(minX=" + this.getMinX() + ", minY=" + this.getMinY() + ", minZ=" + this.getMinZ() + ", maxX=" + this.getMaxX() + ", maxY=" + this.getMaxY() + ", maxZ=" + this.getMaxZ() + ")";
    }
}