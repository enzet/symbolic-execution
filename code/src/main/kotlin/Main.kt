import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

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
    val since: Int = 1994,
    val type: ElementType? = null,
    val name: String,
    val id: String = name,
    val authors: List<String> = ArrayList(),
    val description: String = "",
    val languages: List<String> = ArrayList(),
    val uses: List<String> = ArrayList(),
    val based: List<String> = ArrayList(),
    val ir: List<String> = ArrayList(),
)

data class ToolPicture(
    val tool: Tool,
    val toolMap: Map<String, ToolPicture>,
    var position: Vector = Vector(0.0, 0.0),
    var size: Vector = Vector(100.0, 150.0),
    var descriptions: List<String> = listOf(),
    var names: List<String> = listOf(),
) {

    init {
        descriptions = if (tool.description == "") listOf() else tool.description.split("\n")
        names = if (tool.name == "") listOf() else tool.name.split("\n")
        size = Vector(120.0, 40.0 + descriptions.size * 14.0 + names.size * 20.0)
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

    fun draw(xml: XML, scheme: ColorScheme, toolMap: Map<String, ToolPicture>) {

        if (position.x == 0.0) return

        var y = 10.0
        val stroke = if (tool.type == ElementType.PAPER) "#888888" else "none"

        xml.add(
            "rect", mapOf(
                "fill" to scheme.getColor(tool.type), "stroke" to stroke, "rx" to 5.0
            ) + position.toMap() + size.toSizeMap()
        )
        for (text in names) {
            y += 20.0
            addText(
                xml,
                text,
                position + Vector(10.0, y),
                letterSpacing = if (tool.name.uppercase() == tool.name) 1.8 else 0.0,
                fontSize = 20.0,
            )
        }
        addText(xml, tool.since.toString(), position + Vector(10.0, y + 14.0))
        for (description in descriptions) {
            addText(xml, description, position + Vector(10.0, y + 26.0))
            y += 14.0
        }

        y = size.y + 5.0

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

                val fill = toolMap[use]?.tool?.type?.let { scheme.getColor(it) } ?: "#E0DAD5"
                val width = use.length * 6.0 + 20.0

                xml.add(
                    "rect", (position + Vector(0.0, y)).toMap() + Vector(width, 20.0).toSizeMap() + mapOf(
                        "fill" to fill, "rx" to 2.5
                    )
                )
                xml.add(
                    "text",
                    (position + Vector(width / 2.0, y + 14.0)).toMap() + mapOf(
                        "font-size" to 12.0,
                        "font-family" to "Roboto",
                        "font-weight" to 300,
                        "text-anchor" to "middle",
                    ),
                    use,
                )
                y += 25.0
            }
        }
        for (author in tool.authors) {
            y += 12
            addText(xml, author, position + Vector(0.0, y))
        }
        size.y = y
    }
}

class AffiliationPicture {

    private val padding = Vector(20.0, 20.0)

    private var start = Vector(3000.0, 3000.0)
    private var end = Vector(0.0, 0.0)

    fun add(toolPicture: ToolPicture) {
        start.x = start.x.coerceAtMost(toolPicture.position.x)
        start.y = start.y.coerceAtMost(toolPicture.position.y)
        val toolEnd = (toolPicture.position + toolPicture.size)
        end.x = end.x.coerceAtLeast(toolEnd.x)
        end.y = end.y.coerceAtLeast(toolEnd.y)
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
                val tool = toolMap[parts[1]] ?: throw NotImplementedError(parts[1])
                for (i in 2 until parts.size) {
                    val tool2 = toolMap[parts[i]] ?: throw NotImplementedError(parts[i])
                    tool2.position.x = tool.position.x
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
                        "v" -> tool2.position.x = tool.position.x
                        "^" -> tool2.position.x = tool.position.x
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
            println("<$affiliationPictures>")
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
