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

class Rectangle(
    private val start: Vector,
    private val end: Vector,
    private val fill: String = "black",
    private val opacity: Double = 1.0,
    private val rx: Double,
) : SVGElement() {

    override fun toSVG(): String {
        return "<rect " +
                "rx=\"$rx\" " +
                "${start.toSVGCoordinates()} " +
                "${end.toSVGSize()} " +
                "fill=\"$fill\" " +
                "opacity=\"$opacity\" " +
                "/>"
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
        return "<text " +
                "x=\"${position.x}\" " +
                "y=\"${position.y}\" " +
                "letter-spacing=\"$letterSpacing\" " +
                "font-family=\"$fontFamily\" " +
                "font-weight=\"$fontWeight\" " +
                "font-size=\"$fontSize\" " +
                "fill=\"$fill\" " +
                "opacity=\"$opacity\" " +
                "text-anchor=\"$textAnchor\" " +
                ">$text</text>"
    }
}

/**
 * Simple SVG drawing.
 */
class SVG {

    /**
     * Graphical elements.
     */
    private var elements: ArrayList<SVGElement> = ArrayList()


    fun add(element: SVGElement) {
        elements.add(element)
    }

    /**
     * Write drawing to the file.
     */
    fun write() {
        File("graph.svg").printWriter().use { out ->
            out.println("<svg width=\"5000\" height=\"5000\" xmlns=\"http://www.w3.org/2000/svg\">")
            for (element in elements) {
                out.println(element.toSVG())
            }
            out.println("</svg>")
        }
    }
}