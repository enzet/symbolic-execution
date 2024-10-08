import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.max
import kotlin.math.min

const val STEP: Int = 20

/**
 * Type of diagram element: instrument or white paper.
 */
@Serializable
enum class ElementType {
    /** Concolic (dynamic symbolic execution) with path exploration. */
    @SerialName("concolic")
    CONCOLIC,

    /** Concolic (dynamic symbolic execution) within one path. */
    @SerialName("concolic one path")
    CONCOLIC_ONE_PATH,

    @SerialName("static symbolic execution")
    STATIC_SYMBOLIC_EXECUTION,

    /** Satisfiability modulo theories solver. */
    @SerialName("SMT")
    SMT,

    /** Satisfiability solver. */
    @SerialName("SAT")
    SAT,

    @SerialName("model checker")
    MODEL_CHECKER,

    @SerialName("static instrumentation")
    STATIC_INSTRUMENTATION,

    @SerialName("dynamic instrumentation")
    DYNAMIC_INSTRUMENTATION,

    @SerialName("fuzzer")
    FUZZER,

    /**
     * White paper without instrument.
     */
    @SerialName("paper")
    PAPER,

    /**
     * Dynamic analysis within one execution path.
     */
    @SerialName("DA one path")
    DYNAMIC_ANALYSIS_ONE_PATH,
}

class ColorScheme {
    fun getColor(type: ElementType?): String {
        return when (type) {
            ElementType.CONCOLIC -> "#E4D3C2"
            ElementType.CONCOLIC_ONE_PATH -> "#CAB7A3"
            ElementType.DYNAMIC_INSTRUMENTATION -> "#CFB6E0"
            ElementType.FUZZER -> "#E4E1A8"
            ElementType.MODEL_CHECKER -> "#E0BBBB"
            ElementType.SAT -> "#ADC57D"
            ElementType.SMT -> "#C8DAA4"
            ElementType.STATIC_INSTRUMENTATION -> "#E0CFEB"
            ElementType.STATIC_SYMBOLIC_EXECUTION -> "#C4DAD2"
            ElementType.PAPER -> "none"
            ElementType.DYNAMIC_ANALYSIS_ONE_PATH -> "#D4CD89"
            else -> "#E0DAD5"
        }
    }

    /**
     * Language colors extracted from Linguist project.
     *
     * See [`languages.yml`](https://github.com/github/linguist/blob/master/lib/linguist/languages.yml) file.
     */
    fun getLanguageColor(language: String): String {
        return when (language) {
            ".NET" -> "#178600"
            "C" -> "#555555"
            "C++" -> "#f34b7d"
            "Fortran" -> "#4d41b1"
            "Java bytecode" -> "#B07219"
            "Java" -> "#B07219"
            "JavaScript" -> "#F1E05A"
            "Lisp" -> "#3fb68b"
            "PL/I" -> "#6d3b9f"
            "Python" -> "#3572a5"
            "Rosette" -> "#22228f"
            "SMT-LIB 1.2" -> "#C8DAA4"
            "SMT-LIB 2" -> "#C8DAA4"
            "binary" -> "#887f73"
            else -> "#888888"
        }
    }
}

@Serializable
class Config(val tools: List<Tool>)

@Serializable
open class Tool(
    /** Start year. E.g. 2000. */
    val since: Int = 1994,
    val type: ElementType? = null,
    val name: String,
    /** Tool name. E.g. “DART”. */
    val id: String = name,
    /** Tool authors. E.g. “P. Godefroid (B)\nK. Sen (I)”. */
    val authors: List<String> = ArrayList(),
    /** Tool description. E.g. “Random testing and direct execution”. */
    val description: String = "",
    /** Target language. E.g. “C”. */
    val languages: List<String> = ArrayList(),
    /** Tools used by the tool. E.g. “Z3” for Z3 solver used to solve paths. */
    val uses: List<String> = ArrayList(),
    val based: List<String> = ArrayList(),
    val ir: List<String> = ArrayList(),
)

/**
 * Graphical element representing a tool or article.
 */
data class ToolPicture(
    val tool: Tool,
    val toolMap: Map<String, ToolPicture>,
    var position: Vector = Vector(0.0, 0.0),
    var size: Vector = Vector(100.0, 150.0),
    /** Tool name. E.g. “DART”. */
    var names: List<String> = listOf(),
    /** Tool description. E.g. “Random testing and direct execution”. */
    var descriptions: List<String> = listOf(),
) {
    private val padding = 10.0
    private val nameHeight = 20.0
    private val textHeight = 14.0

    private val paperBorderColor = "#888888"

    init {
        names = if (tool.name == "") listOf() else tool.name.split("\n")
        descriptions = if (tool.description == "") listOf() else tool.description.split("\n")
        size = Vector(
            120.0,
            padding * 2 +
                    (descriptions.size + 1) * textHeight +
                    names.size * nameHeight
        )
    }

    private fun addText(
        xml: XML, text: String, position: Vector, fontSize: Double = 12.0, letterSpacing: Double = 0.0
    ) = xml.add(
        "text", position.toMap() + mapOf(
            "font-family" to "Roboto",
            "font-weight" to 300,
            "font-size" to fontSize,
            "fill" to "#363228",
            "letter-spacing" to letterSpacing,
        ), text
    )

    /** Get total height of all bottom elements: tools and authors. */
    fun getBottomHeight(): Double {
        return (tool.uses + tool.based + tool.ir).size * 25 +
                (if (tool.authors.isNotEmpty()) 5.0 else 0.0) +
                (tool.authors).size * textHeight
    }

    /** Get total height of all top language element. */
    fun getTopHeight(): Double {
        return if (tool.languages.isNotEmpty()) 25.0 else 0.0
    }

    fun draw(xml: XML, scheme: ColorScheme, toolMap: Map<String, ToolPicture>) {

        if (position.x == 0.0) return

        val stroke = if (tool.type == ElementType.PAPER) paperBorderColor else "none"

        xml.add(
            "rect",
            mapOf(
                "fill" to scheme.getColor(tool.type),
                "stroke" to stroke,
                "rx" to 5.0) +
                    position.toMap() +
                    size.toSizeMap()
        )
        var y = padding

        for (text in names) {
            y += nameHeight
            addText(
                xml,
                text,
                position + Vector(padding, y),
                letterSpacing = if (tool.name.uppercase() == tool.name) 1.8 else 0.0,
                fontSize = 20.0,
            )
        }
        y += textHeight
        addText(xml, tool.since.toString(), position + Vector(padding, y))
        for (description in descriptions) {
            y += textHeight
            addText(xml, description, position + Vector(padding, y))
        }
        y += padding

        if (tool.languages.isNotEmpty()) {
            var x = 0.0
            for (language in tool.languages) {
                val width = language.length * 6.0 + 20.0
                xml.add(
                    "rect", (position + Vector(x, -25.0)).toMap() + Vector(width, 20.0).toSizeMap() + mapOf(
                        "fill" to scheme.getLanguageColor(language), "rx" to 2.5
                    )
                )
                xml.add(
                    "text",
                    (position + Vector(width / 2.0 + x, -11.0)).toMap() + mapOf(
                        "font-size" to 12.0,
                        "font-family" to "Roboto",
                        "fill" to "#FFFFFF",
                        "text-anchor" to "middle",
                        "font-weight" to 700,
                    ),
                    language,
                )
                x += width + 5
            }
        }
        if ((tool.uses + tool.based + tool.ir).isNotEmpty()) {

            for (use in tool.uses + tool.based + tool.ir) {

                y += 5.0

                val fill = toolMap[use]?.tool?.type?.let { scheme.getColor(it) } ?: "#E0DAD5"
                /** Very rough estimate of a text width. */
                val width = use.length * 6.0 + 20.0

                xml.add(
                    "rect",
                    (position + Vector(0.0, y)).toMap() +
                            Vector(width, 20.0).toSizeMap() +
                            mapOf("fill" to fill, "rx" to 2.5)
                )
                xml.add(
                    "text",
                    (position + Vector(width / 2.0, y + textHeight)).toMap() + mapOf(
                        "font-size" to 12.0,
                        "font-family" to "Roboto",
                        "font-weight" to 300,
                        "text-anchor" to "middle",
                    ),
                    use,
                )
                y += 20.0
            }
        }
        if (tool.authors.isNotEmpty()) {
            y += 5.0
            for (author in tool.authors) {
                y += textHeight
                addText(xml, author, position + Vector(0.0, y))
            }
        }
        size.y = y
    }

    /** Put this tool below the specified tool. */
    fun putBelow(tool: ToolPicture) {
        position.x = tool.position.x
        val y = tool.position.y +
                tool.size.y +
                tool.getBottomHeight() +
                10.0 +
                getTopHeight()
        position.y = max(position.y, y)
    }

    /** Put this tool above the specified tool. */
    fun putAbove(tool: ToolPicture) {
        position.x = tool.position.x
        val y = tool.position.y -
                size.y -
                getBottomHeight() -
                10.0 -
                tool.getTopHeight()
        position.y = min(position.y, y)
    }
}

/**
 * Shape around all tools with the same affiliation.
 */
class AffiliationPicture {

    private val padding = Vector(20.0, 20.0)

    private var start = Vector(Double.MAX_VALUE, Double.MAX_VALUE)
    private var end = Vector(Double.MIN_VALUE, Double.MIN_VALUE)

    /** Add tool with this affiliation. */
    fun add(toolPicture: ToolPicture) {
        start.coerceAtMost(toolPicture.position - Vector(0.0, toolPicture.getTopHeight()))
        end.coerceAtLeast(toolPicture.position + toolPicture.size + Vector(0.0, toolPicture.getBottomHeight()))
    }

    fun draw(xml: XML) = xml.add(
        "rect", (start - padding).toMap() + (end - start + 2.0 * padding).toSizeMap() + mapOf(
            "opacity" to 0.07, "rx" to 20.0, "fill" to "#816647"
        )
    )

    fun getPolygon(): Polygon = Polygon(arrayListOf(start, Vector(end.x, start.y), end, Vector(start.x, end.y)))
}

private data class Timeline(val tools: List<Tool>, val scheme: ColorScheme, val configuration: List<String>) {

    val toolMap: HashMap<String, ToolPicture> = HashMap()
    val affiliations: HashMap<String, HashSet<AffiliationPicture>> = HashMap()

    init {
        for (tool in tools) {
            toolMap[tool.id] = ToolPicture(tool, toolMap)
        }
        for (toolPicture in toolMap.values) {
            toolPicture.position.y = getY(toolPicture.tool.since).toDouble()
        }

        for (line in configuration) {
            val parts = line.split(" ")

            if (parts[0] == "->") {
                var tool = toolMap[parts[1]] ?: throw NotImplementedError(parts[1])
                for (i in 2 until parts.size) {
                    val tool2 = toolMap[parts[i]] ?: throw NotImplementedError(parts[i])
                    tool2.position.x = tool.position.x + tool.size.x + STEP
                    tool = tool2
                }

            } else if (parts[0] == "v") {
                for (i in 2 until parts.size) {
                    val tool = toolMap[parts[i - 1]] ?: throw NotImplementedError(parts[i - 1])
                    val tool2 = toolMap[parts[i]] ?: throw NotImplementedError(parts[i])

                    tool2.putBelow(tool)
                }

            } else if (parts.size == 2) {
                val tool = toolMap[parts[0]] ?: throw NotImplementedError(parts[0])
                tool.position.x = parts[1].toDouble()

            } else if (parts.size == 3) {
                val tool = toolMap[parts[0]] ?: throw NotImplementedError(parts[0])
                val tool2 = toolMap[parts[2]] ?: throw NotImplementedError(parts[2])

                if (parts[1].startsWith("->")) {
                    val offset = if (parts[1].length > 2) parts[1].substring(2).toDouble() else 0.0
                    tool2.position.x = tool.position.x + tool.size.x + STEP * (1 + offset)
                } else {
                    when (parts[1]) {
                        "<-" -> tool2.position.x = tool.position.x - tool2.size.x - STEP
                        "v" -> tool2.putBelow(tool)
                        "^" -> tool2.putAbove(tool)
                        "|" -> tool2.position.y = tool.position.y
                    }
                }

            } else if (parts.size > 3) {
                val affiliation = parts[0]
                val picture = AffiliationPicture()
                for (i in 2 until parts.size) {
                    toolMap[parts[i]]?.let { picture.add(it) }
                }
                if (!affiliations.containsKey(affiliation)) {
                    affiliations[affiliation] = hashSetOf(picture)
                }
                affiliations[affiliation]!!.add(picture)
            }
        }
    }

    fun draw(xml: XML, filePath: String) {
        for (affiliationPictures in affiliations.keys) {
            var polygon: Polygon? = null
            for (affiliationPicture in affiliations[affiliationPictures]!!) {
                affiliationPicture.draw(xml)
//                val polygon2 = affiliationPicture.getPolygon()
//                polygon = polygon?.join(polygon2) ?: polygon2
//                polygon.toSVG(xml)
            }
        }
        for (toolPicture in toolMap.values) toolPicture.draw(xml, scheme, toolMap)
        xml.writeSVG(filePath)
    }
}

fun getYearHeight(year: Int): Int {
    if (year <= 1990) return 2
    if (year <= 2000) return 20
    return when (year) {
        2001 -> 40
        2002, 2003, 2004 -> 60
        else -> 140
    }
}

fun getY(year: Int): Int {
    var y = 0
    for (yr in 1906 until year) {
        y += getYearHeight(yr)
    }
    return y
}

fun main() {

    val toolsPath = "tools/tools.json"
    val diagramConfigurationPath = "diagram/config"
    val outputFilePath = "diagram.svg"

    val json = Json { ignoreUnknownKeys = true }

    val config = json.decodeFromString<Config>(File(toolsPath).readText(Charsets.UTF_8))
    val scheme = ColorScheme()
    val xml = XML()

    val timeline = Timeline(config.tools, scheme, File(diagramConfigurationPath).readLines())
    timeline.draw(xml, outputFilePath)
}
