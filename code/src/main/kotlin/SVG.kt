import java.io.File

class XMLParameter(private val key: String, private val value: Any?) {

    override fun toString() = "$key=\"$value\""
}

class XMLTag(private val tag: String, private val parameters: List<XMLParameter>, private val value: String? = null) {

    override fun toString() = "<$tag" + parameters.joinToString(" ", " ") + (value?.let { ">$value</$tag>" } ?: "/>")

}

open class XML {

    /**
     * Graphical elements.
     */
    var elements: ArrayList<XMLTag> = ArrayList()

    /**
     * Add graphical element to the drawing.
     */
    fun add(tag: String, parameters: Map<String, Any>, text: String? = null) {

        val xmlParameters = parameters.toList().map { XMLParameter(it.first, it.second) }
        elements.add(XMLTag(tag, xmlParameters, text))
    }
}

/**
 * Write XML as an SVG drawing to the file with [filePath] path.
 *
 * @property filePath output SVG file path
 */
fun XML.writeSVG(filePath: String) = File(filePath).printWriter().use { out ->
    out.println("<svg width=\"3550\" height=\"2520\" xmlns=\"http://www.w3.org/2000/svg\">")
    out.println("")
    for (element in elements) out.println(element)
    out.println("</svg>")
}