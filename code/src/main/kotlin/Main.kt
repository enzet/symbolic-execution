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
    @SerialName("concolic_one_path")
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

    @SerialName("static_instrumentation")
    STATIC_INSTRUMENTATION,

    @SerialName("dynamic instrumentation")
    DYNAMIC_INSTRUMENTATION,

    @SerialName("fuzzer")
    FUZZER,

    /**
     * White paper without instrument.
     */
    @SerialName("paper")
    PAPER
}

class ColorScheme {
    fun getColor(type: ElementType): String {
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
            else -> "none"
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
    val type: ElementType = ElementType.CONCOLIC,
    val name: String,
    val id: String = name,
    val authors: List<String> = ArrayList(),
    val description: String = "",
    val languages: List<String> = ArrayList(),
)

data class ToolPicture(val tool: Tool, var position: Vector = Vector(), var size: Vector = Vector(100.0, 150.0)) {

    init {
        size = Vector(120.0, if (tool.description == "") 60.0 else 74.0)
    }

    private fun addText(
        svg: SVG, text: String, position: Vector, fontSize: Double = 11.0, letterSpacing: Double = 0.0
    ) {
        val textElement = Text(
            text,
            position,
            fontFamily = "Roboto",
            fontWeight = 300,
            fontSize = fontSize,
            fill = "#363228",
            letterSpacing = letterSpacing,
        )
        svg.add(textElement)
    }

    fun draw(svg: SVG, scheme: ColorScheme) {
        val stroke = if (tool.type == ElementType.PAPER) "#888888" else "none"
        svg.add(Rectangle(position, size, fill = scheme.getColor(tool.type), stroke = stroke, rx = 5.0))
        addText(
            svg,
            tool.name,
            position + Vector(10.0, 30.0),
            letterSpacing = if (tool.name.uppercase() == tool.name) 1.8 else 0.0,
            fontSize = 18.0,
        )
        addText(svg, tool.since.toString(), position + Vector(10.0, 44.0), fontSize = 11.0)
        addText(svg, tool.description, position + Vector(10.0, 56.0), fontSize = 11.0)

        var y = size.y + 15.0
        for (author in tool.authors) {
            addText(svg, author, position + Vector(0.0, y), fontSize = 11.0)
            y += 12
        }
        if (tool.languages.isNotEmpty()) {
            var x = 0.0
            for (language in tool.languages) {
                val width = language.length * 6.0 + 20.0
                svg.add(
                    Rectangle(
                        position + Vector(x, -25.0),
                        Vector(width, 20.0),
                        scheme.getLanguageColor(language),
                        rx = 5.0,
                    )
                )
                svg.add(
                    Text(
                        language,
                        position + Vector(width / 2.0 + x, -10.0),
                        fontSize = 12.0,
                        fontFamily = "Roboto",
                        fill = "#FFFFFF",
                        textAnchor = "middle",
                        fontWeight = 700,
                    )
                )
                x += width + 5
            }
        }
    }
}

class AffiliationPicture {

    private var start: Vector = Vector(3000.0, 3000.0)
    private var end: Vector = Vector()

    fun add(toolPicture: ToolPicture) {
        start.x = start.x.coerceAtMost(toolPicture.position.x)
        start.y = start.y.coerceAtMost(toolPicture.position.y)
        val toolEnd = (toolPicture.position + toolPicture.size)
        end.x = end.x.coerceAtLeast(toolEnd.x)
        end.y = end.y.coerceAtLeast(toolEnd.y)
    }

    fun draw(svg: SVG) {
        svg.add(Rectangle(start - Vector(10.0, 10.0), end - start + Vector(20.0, 20.0), opacity = 0.05, rx = 15.0))
    }

    override fun toString(): String {
        return "$start -- $end"
    }
}

private data class Timeline(val tools: List<Tool>, val scheme: ColorScheme, val configuration: List<String>) {

    val toolMap: HashMap<String, ToolPicture> = HashMap()
    val affiliations: ArrayList<AffiliationPicture> = ArrayList()

    var minYear = 1994
    var maxYear = 1994

    init {
        for (tool in tools) {
            toolMap[tool.id] = ToolPicture(tool)
            minYear = minYear.coerceAtMost(tool.since)
            maxYear = maxYear.coerceAtLeast(tool.since)
        }
        for (toolPicture in toolMap.values) {
            toolPicture.position.y = 20 + getY(toolPicture.tool.since).toDouble()
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

                when (parts[1]) {
                    "->" -> tool2.position.x = tool.position.x + tool.size.x + STEP
                    "<-" -> tool2.position.x = tool.position.x - tool2.size.x - STEP
                    "v" -> {
                        // tool2.position.y = tool.position.y + tool2.size.y + STEP
                        tool2.position.x = tool.position.x
                    }
                    "^" -> {
                        // tool2.position.y = tool.position.y - tool2.size.y - STEP
                        tool2.position.x = tool.position.x
                    }
                    "|" -> tool2.position.y = tool.position.y
                }

            } else {
                // TODO: parts[0] is affiliation.
                val picture = AffiliationPicture()
                for (i in 2 until parts.size) {
                    toolMap[parts[i]]?.let { picture.add(it) }
                }
                affiliations.add(picture)
            }
        }
    }

    fun draw(svg: SVG) {
        for (affiliationPicture in affiliations) affiliationPicture.draw(svg)
        for (toolPicture in toolMap.values) toolPicture.draw(svg, scheme)
        svg.write()
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
    for (yr in 1970..year) {
        y += getYearHeight(yr)
    }
    return y
}

fun main() {

    val json = Json { ignoreUnknownKeys = true }

    val path = "tools/tools.json"
    val config = json.decodeFromString<Config>(File(path).readText(Charsets.UTF_8))
    val scheme = ColorScheme()
    val svg = SVG()

    val timeline = Timeline(config.tools, scheme, File("diagram/config").readLines())
    timeline.draw(svg)
}
