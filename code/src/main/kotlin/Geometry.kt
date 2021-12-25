/**
 * Simple 2D vector.
 */
class Vector(var x: Double = 0.0, var y: Double = 0.0) {

    override fun toString(): String = "($x, $y)"

    fun toSVG(): String = "$x,$y"
    fun toSVGCoordinates(): List<XMLParameter> = listOf(XMLParameter("x", x), XMLParameter("y", y))
    fun toSVGSize(): List<XMLParameter> = listOf(XMLParameter("width", x), XMLParameter("height", y))

    operator fun plus(vector: Vector): Vector = Vector(x + vector.x, y + vector.y)
    operator fun minus(vector: Vector): Vector = Vector(x - vector.x, y - vector.y)
}
