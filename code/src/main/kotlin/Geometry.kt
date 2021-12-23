class Vector(var x: Double = 0.0, var y: Double = 0.0) {
    override fun toString(): String {
        return "($x, $y)"
    }

    fun toSVG(): String {
        return "$x,$y"
    }

    operator fun plus(vector: Vector): Vector {
        return Vector(x + vector.x, y + vector.y)
    }

    operator fun minus(vector: Vector): Vector {
        return Vector(x - vector.x, y - vector.y)
    }
}
