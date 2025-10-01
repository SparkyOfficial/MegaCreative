package com.megacreative.particles;

/**
 * A 3D vector class for particle math operations
 */
public class Vector3D {
    public double x, y, z;
    
    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vector3D add(Vector3D other) {
        return new Vector3D(x + other.x, y + other.y, z + other.z);
    }
    
    public Vector3D subtract(Vector3D other) {
        return new Vector3D(x - other.x, y - other.y, z - other.z);
    }
    
    public Vector3D multiply(double scalar) {
        return new Vector3D(x * scalar, y * scalar, z * scalar);
    }
    
    public Vector3D divide(double scalar) {
        if (scalar == 0) throw new IllegalArgumentException("Cannot divide by zero");
        return new Vector3D(x / scalar, y / scalar, z / scalar);
    }
    
    public double dot(Vector3D other) {
        return x * other.x + y * other.y + z * other.z;
    }
    
    public Vector3D cross(Vector3D other) {
        return new Vector3D(
            y * other.z - z * other.y,
            z * other.x - x * other.z,
            x * other.y - y * other.x
        );
    }
    
    public double magnitude() {
        return Math.sqrt(x * x + y * y + z * z);
    }
    
    public Vector3D normalize() {
        double mag = magnitude();
        if (mag == 0) return new Vector3D(0, 0, 0);
        return divide(mag);
    }
    
    public Vector3D rotateX(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3D(
            x,
            y * cos - z * sin,
            y * sin + z * cos
        );
    }
    
    public Vector3D rotateY(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3D(
            x * cos + z * sin,
            y,
            -x * sin + z * cos
        );
    }
    
    public Vector3D rotateZ(double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector3D(
            x * cos - y * sin,
            x * sin + y * cos,
            z
        );
    }
    
    @Override
    public String toString() {
        return String.format("Vector3D(%.2f, %.2f, %.2f)", x, y, z);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector3D vector3D = (Vector3D) obj;
        return Double.compare(vector3D.x, x) == 0 &&
               Double.compare(vector3D.y, y) == 0 &&
               Double.compare(vector3D.z, z) == 0;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y, z);
    }
}