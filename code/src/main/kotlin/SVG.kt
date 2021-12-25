import java.io.File

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

/**
 * Simple SVG drawing.
 *
 * @property filePath output SVG file path; will be used by [write] method
 */
class SVG(private val filePath: String) {

    /**
     * Graphical elements.
     */
    private var elements: ArrayList<XMLTag> = ArrayList()

    /**
     * Add graphical element to the drawing.
     */
    fun add(tag: String, parameters: Map<String, Any>, text: String? = null) {

        val xmlParameters: List<XMLParameter> = parameters.toList().map { XMLParameter(it.first, it.second) }
        elements.add(XMLTag(tag, xmlParameters, text))
    }

    /**
     * Write drawing to the file with [filePath] path.
     */
    fun write() {
        File(filePath).printWriter().use { out ->
            out.println("<svg width=\"5000\" height=\"5000\" xmlns=\"http://www.w3.org/2000/svg\">")
            for (element in elements) out.println(element)
            out.println("</svg>")
        }
    }
}