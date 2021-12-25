/**
 * Simple 2D vector.
 */
class Vector(var x: Double = 0.0, var y: Double = 0.0) {

    fun toMap(): Map<String, Any> = mapOf("x" to x, "y" to y)
    fun toSizeMap(): Map<String, Any> = mapOf("width" to x, "height" to y)

    operator fun plus(vector: Vector): Vector = Vector(x + vector.x, y + vector.y)
    operator fun minus(vector: Vector): Vector = Vector(x - vector.x, y - vector.y)
}

operator fun Double.times(vector: Vector): Vector = Vector(this * vector.x, this * vector.y)

