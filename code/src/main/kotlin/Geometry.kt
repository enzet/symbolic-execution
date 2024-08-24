import java.lang.Double.min
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Simple 2D vector.
 */
class Vector(var x: Double, var y: Double) {

    fun toSVG() = "$x,$y"
    fun toMap() = mapOf("x" to x, "y" to y)
    fun toSizeMap() = mapOf("width" to x, "height" to y)
    override fun toString() = "($x, $y)"

    operator fun plus(vector: Vector) = Vector(x + vector.x, y + vector.y)
    operator fun minus(vector: Vector) = Vector(x - vector.x, y - vector.y)

    fun corner(other: Vector?) = other?.let { Vector(min(x, other.x), min(y, other.y)) } ?: this
    fun length() = sqrt(x * x + y * y)

    override fun hashCode(): Int = x.hashCode() + y.hashCode()
    override fun equals(other: Any?) = if (other is Vector) x == other.x && y == other.y else false

    fun coerceAtMost(vector: Vector) {
        x = x.coerceAtMost(vector.x)
        y = y.coerceAtMost(vector.y)
    }

    fun coerceAtLeast(vector: Vector) {
        x = x.coerceAtLeast(vector.x)
        y = y.coerceAtLeast(vector.y)
    }
}

operator fun Double.times(vector: Vector): Vector = Vector(this * vector.x, this * vector.y)

class Line(val point1: Vector, val point2: Vector) {

    fun findIntersection(other: Line): Vector? {

        val t =
            ((point1.x - other.point1.x) * (other.point1.y - other.point2.y) -
                    (point1.y - other.point1.y) * (other.point1.x - other.point2.x)) /
            ((point1.x - point2.x) * (other.point1.y - other.point2.y) -
                    (point1.y - point2.y) * (other.point1.x - other.point2.x))

        if (0.0 < t && t < 1.0)
            return Vector(point1.x + t * (point2.x - point1.x), point1.y + t * (point2.y - point1.y))

        // `t` is `NaN` if lines are the same.
        // `t` is `Infinity` or `-Infinity` if they are parallel.

        return null
    }

    private fun getVector() = Vector(point2.x - point1.x, point2.y - point1.y)

    fun getAngle(other: Line): Double {
        val vector = getVector()
        val otherVector = other.getVector()
//        val angle = atan2(otherVector.y - vector.y, otherVector.x - vector.x)
        val dot = vector.x * otherVector.x + vector.y * otherVector.y      // dot product between [x1, y1] and [x2, y2]
        val det = vector.x * otherVector.y - vector.y * otherVector.x      // determinant
        var angle = atan2(det, dot)  // atan2(y, x) or atan2(sin, cos)
        if (angle < 0) {
            angle += 2 * PI
        }
        return angle
    }

    override fun hashCode(): Int = point1.hashCode() + point2.hashCode()
    override fun equals(other: Any?) = if (other is Line) point1 == other.point1 && point2 == other.point2 else false
    override fun toString() = "$point1 -- $point2"
}

class Polygon(private val points: ArrayList<Vector> = arrayListOf()) {

    fun toCommand() = "M " + points.joinToString(" L ") { it.toSVG() } + " Z"

    fun toSVG(svg: XML) = svg.add(
        "path", mapOf(
            "d" to toCommand(), "opacity" to "0.05"
        )
    )

    private fun add(lines: HashMap<Vector, HashSet<Line>>, line: Line) {
        for (point in listOf(line.point1, line.point2)) {
            if (!lines.containsKey(point)) lines[point] = hashSetOf(line)
            lines[point]!!.add(line)
        }
    }

    fun join(other: Polygon): Polygon {

        println(toCommand())
        println(other.toCommand())

        val lines = HashMap<Vector, HashSet<Line>>()

        for (i in 0..points.size - 2) for (j in 0..other.points.size - 2) {

            val line1 = Line(points[i], points[i + 1])
            val line2 = Line(other.points[j], other.points[j + 1])
            val intersection = line1.findIntersection(line2)

            intersection?.let {
                add(lines, Line(points[i], intersection))
                add(lines, Line(intersection, points[i + 1]))
                add(lines, Line(other.points[i], intersection))
                add(lines, Line(intersection, other.points[i + 1]))
            } ?: run {
                add(lines, line1)
                add(lines, line2)
            }
        }

        var corner: Vector? = null

        for (point in points + other.points) {
            corner = point.corner(corner)
        }
        val unwrapCorner = corner!!

        var minLength: Double? = null
        var leftBottom: Vector? = null

        for (point in points + other.points) {

            val length = (point - unwrapCorner).length()
            if (minLength == null || length < minLength) {
                leftBottom = point
                minLength = length
            }
        }
        if (leftBottom == null) {
            throw IllegalAccessError()
        }
        var point: Vector = leftBottom
        var previousPoint = leftBottom + Vector(0.0, 1.0)
        val n = Polygon(arrayListOf(leftBottom))
        println(leftBottom.toSVG())

        while (true) {
            val nextLines = lines[point]

            if (nextLines != null) {
                var minAngle = 2 * PI
                var nextPoint: Vector? = null

                for (nextLine in nextLines) {
                    val currentNextPoint = if (nextLine.point1 == point) nextLine.point2 else nextLine.point1
                    if (n.points.contains(currentNextPoint)) continue
                    println("  try $previousPoint $point $currentNextPoint")
                    val line1 = Line(point, previousPoint)
                    val line2 = Line(point, currentNextPoint)
                    val angle = line1.getAngle(line2)
                    if (angle <= minAngle) {
                        minAngle = angle
                        nextPoint = currentNextPoint
                    }
                    println("   $angle")
                }
                if (nextPoint == null)
                    break
                n.points.add(nextPoint)
                previousPoint = point
                point = nextPoint
                println(nextPoint.toSVG())
                if (nextPoint == leftBottom) {
                    break
                }
            } else {
                break
            }
        }
        return n
    }
}