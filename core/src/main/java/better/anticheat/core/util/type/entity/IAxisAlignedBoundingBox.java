package better.anticheat.core.util.type.entity;

import better.anticheat.core.util.type.Pair;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3d;

public interface IAxisAlignedBoundingBox {
    IAxisAlignedBoundingBox expand(double x, double y, double z);

    IAxisAlignedBoundingBox expandMax(double x, double y, double z);

    IAxisAlignedBoundingBox expandMin(double x, double y, double z);

    IAxisAlignedBoundingBox add(double x, double y, double z);

    boolean collides(IAxisAlignedBoundingBox other);

    boolean collidesUnder(IAxisAlignedBoundingBox other);

    boolean collidesAbove(IAxisAlignedBoundingBox other);

    boolean collidesHorizontally(IAxisAlignedBoundingBox other);

    Vector3d calculateIntercept(Vector3d vecA, Vector3d vecB);

    Pair<Vector3d, BlockFace> calculateInterceptWithDirection(Vector3d vecA, Vector3d vecB);

    IAxisAlignedBoundingBox copy();

    double posX();

    double posY();

    double posZ();

    IAxisAlignedBoundingBox offset(double x, double y, double z);

    /**
     * @param vec The com.github.retrooper.packetevents.protocol.world.Location
     * @return if the vector is inside the current entity
     */
    boolean isVecInYZ(Vector3d vec);

    boolean isVecInXZ(Vector3d vec);

    boolean isVecInXY(Vector3d vec);

    Vector3d getIntermediateWithXValue(Vector3d original, Vector3d vec, double x);

    Vector3d getIntermediateWithYValue(Vector3d original, Vector3d vec, double y);

    Vector3d getIntermediateWithZValue(Vector3d original, Vector3d vec, double z);

    double squareDistanceTo(Vector3d original, Vector3d vec);

    int hashCode();

    boolean equals(Object o);

    IAxisAlignedBoundingBox clone();

    String toString();

    double getMinX();

    void setMinX(double minX);

    double getMinY();

    void setMinY(double minY);

    double getMinZ();

    void setMinZ(double minZ);

    double getMaxX();

    void setMaxX(double maxX);

    double getMaxY();

    void setMaxY(double maxY);

    double getMaxZ();

    void setMaxZ(double maxZ);

    boolean intersectsWith(IAxisAlignedBoundingBox other);

    IAxisAlignedBoundingBox addCoord(double x, double y, double z);

    double calculateXOffset(IAxisAlignedBoundingBox other, double offsetX);

    double calculateYOffset(IAxisAlignedBoundingBox other, double offsetY);

    double calculateZOffset(IAxisAlignedBoundingBox other, double offsetZ);

    IAxisAlignedBoundingBox subtractMin(final double x, final double y, final double z);

    IAxisAlignedBoundingBox subtractMax(final double x, final double y, final double z);
}
