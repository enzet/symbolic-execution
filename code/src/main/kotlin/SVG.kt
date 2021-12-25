import java.io.File

/**
 * Graphical object.
 */
abstract class SVGElement {
    abstract fun toSVG(): String
}

/**
 * SVG path element.
 */
class Path(private val commands: String, private val fill: String = "black") : SVGElement() {

    override fun toSVG(): String {
        return "<path d=\"$commands\" fill=\"$fill\" />"
    }
}

class XMLParameter(private val key: String, private val value: Any?) {

    override fun toString(): String = "$key=\"$value\""
}

class XMLTag(private val tag: String, private val parameters: List<XMLParameter>, private val value: String? = null) {

    override fun toString(): String {
        return "<$tag" + formatParameters() + if (value == null) "/>" else ">$value</$tag>"
    }

    private fun formatParameters(): String {
        return if (parameters.isEmpty()) "" else " " + parameters.joinToString(separator = " ")
    }
}

class Rectangle(
    private val start: Vector,
    private val end: Vector,
    private val fill: String = "black",
    private val stroke: String = "none",
    private val opacity: Double = 1.0,
    private val rx: Double,
) : SVGElement() {

    override fun toSVG(): String {
        return XMLTag(
            "rect",
            listOf(
                XMLParameter("rx", rx),
                XMLParameter("fill", fill),
                XMLParameter("stroke", stroke),
                XMLParameter("opacity", opacity),
            ) + start.toSVGCoordinates() + end.toSVGSize()).toString()
    }
}

class Text(
    private val text: String,
    private val position: Vector,
    private val letterSpacing: Double = 0.0,
    private val fontFamily: String = "sans",
    private val fontWeight: Int = 400,
    private val fontSize: Double = 10.0,
    private val opacity: Double = 1.0,
    private val fill: String,
    private val textAnchor: String = "start",
) : SVGElement() {

    override fun toSVG(): String {
        return XMLTag(
            "text",
            listOf(
                XMLParameter("letter-spacing", letterSpacing),
                XMLParameter("font-family", fontFamily),
                XMLParameter("font-weight", fontWeight),
                XMLParameter("font-size", fontSize),
                XMLParameter("fill", fill),
                XMLParameter("opacity", opacity),
                XMLParameter("text-anchor", textAnchor),
            ) + position.toSVGCoordinates(),
            text).toString()
    }
}

/**
 * Simple SVG drawing.
 *
 * @property filePath output SVG file path; will be used by [write] method
 */
class SVG(private val filePath: String) {

    /**
     * Graphical elements.
     */
    private var elements: ArrayList<SVGElement> = ArrayList()

    /**
     * Add graphical element to the drawing.
     */
    fun add(element: SVGElement) = elements.add(element)

    /**
     * Write drawing to the file with [filePath] path.
     */
    fun write() {
        File(filePath).printWriter().use { out ->
            out.println("<svg width=\"5000\" height=\"5000\" xmlns=\"http://www.w3.org/2000/svg\">")
            for (element in elements) out.println(element.toSVG())
            out.println("</svg>")
        }
    }
}