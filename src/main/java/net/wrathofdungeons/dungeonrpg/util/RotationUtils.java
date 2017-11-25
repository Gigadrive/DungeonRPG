package net.wrathofdungeons.dungeonrpg.util;

import org.bukkit.util.Vector;

// https://www.spigotmc.org/posts/2683817
public final class RotationUtils {

    private RotationUtils() {}

    private static final MatrixApplication X_ROTATION = (vector, theta) -> vector
            .setY(vector.getY() * (Math.cos(theta) - Math.sin(theta)))
            .setZ(vector.getZ() * (Math.sin(theta) + Math.cos(theta)));
    private static final MatrixApplication Y_ROTATION = (vector, theta) -> vector
            .setX(vector.getX() * (Math.cos(theta) + Math.sin(theta)))
            .setZ(vector.getZ() * (-Math.sin(theta) + Math.cos(theta)));
    private static final MatrixApplication Z_ROTATION = (vector, theta) -> vector
            .setX(vector.getX() * (Math.cos(theta) - Math.sin(theta)))
            .setY(vector.getY() * (Math.sin(theta) + Math.cos(theta)));

    /**
     * Apply a rotation to a vector on each individual axis for the provided amount
     * of degrees
     *
     * @param vector the vector to rotate
     * @param angleX the angle in degrees to rotate the vector by on the x axis
     * @param angleY the angle in degrees to rotate the vector by on the y axis
     * @param angleZ the angle in degrees to rotate the vector by on the z axis
     *
     * @return the resulting rotated vector
     */
    public static Vector applyRotation(Vector vector, double angleX, double angleY, double angleZ) {
        X_ROTATION.applyRotation(vector, Math.toRadians(angleX));
        Y_ROTATION.applyRotation(vector, Math.toRadians(angleY));
        Z_ROTATION.applyRotation(vector, Math.toRadians(angleZ));
        return vector;
    }

    /**
     * Apply a rotation to a vector on all axis for the provided amount of degrees
     *
     * @param vector the vector to rotate
     * @param angle the angle in degrees to rotate the vector by
     *
     * @return the resulting rotated vector
     */
    public static Vector applyRotation(Vector vector, double angle) {
        return applyRotation(vector, angle, angle, angle);
    }

    @FunctionalInterface
    private interface MatrixApplication {

        public Vector applyRotation(Vector vector, double angle);

    }

}