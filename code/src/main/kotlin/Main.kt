import kotlin.collections.listOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

const val STEP: Int = 20

@Serializable
enum class ToolType {
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
}

class ColorScheme {
    fun getColor(type: ToolType): String {
        return when (type) {
            ToolType.CONCOLIC -> "#E4D3C2"
            ToolType.CONCOLIC_ONE_PATH -> "#CAB7A3"
            ToolType.SAT -> "#ADC57D"
            ToolType.SMT -> "#C8DAA4"
            ToolType.MODEL_CHECKER -> "#E0BBBB"
            ToolType.STATIC_SYMBOLIC_EXECUTION -> "#C4DAD2"
            ToolType.STATIC_INSTRUMENTATION -> "#E0CFEB"
            ToolType.DYNAMIC_INSTRUMENTATION -> "#CFB6E0"
            ToolType.FUZZER -> "#E4E1A8"
        }
    }

    fun getLanguageColor(language: String): String {
        return when (language) {
            "binary" -> "#887f73"
            "C" -> "#555555"
            "C++" -> "#f34b7d"
            "Java" -> "#B07219"
            "Java bytecode" -> "#B07219"
            "Fortran" -> "#4d41b1"
            "Lisp" -> "#3fb68b"
            ".NET" -> "#178600"
            "JavaScript" -> "#F1E05A"
            "PL/I" -> "#6d3b9f"
            "Python" -> "#3572a5"
            "Rosette" -> "#22228f"
            "SMT-LIB 1.2" -> "#C8DAA4"
            "SMT-LIB 2" -> "#C8DAA4"
            else -> "#888888"
        }
    }
}

@Serializable
class Config(val tools: List<Tool>)

@Serializable
open class Tool(
    val since: Int = 1994,
    val type: ToolType = ToolType.CONCOLIC,
    val name: String,
    val authors: List<String> = ArrayList(),
    val description: String = "",
    val languages: List<String> = ArrayList(),
)

data class ToolPicture(val tool: Tool, var position: Vector = Vector(), var size: Vector = Vector(100.0, 150.0)) {

    init {
        size = Vector(120.0, if (tool.description == "") 60.0 else 74.0)
    }

    fun draw(svg: SVG, scheme: ColorScheme) {
        svg.add(Rectangle(position, size, fill = scheme.getColor(tool.type)))
        svg.add(
            Text(
                tool.name,
                position + Vector(10.0, 30.0),
                letterSpacing = if (tool.name.uppercase() == tool.name) 1.8 else 0.0,
                fontFamily = "Roboto",
                fontWeight = 300,
                fontSize = 18.0,
                fill = "#363228",
            )
        )
        svg.add(
            Text(
                tool.since.toString(),
                position + Vector(10.0, 44.0),
                fontFamily = "Roboto",
                fontWeight = 300,
                fontSize = 11.0,
                fill = "#363228",
            )
        )
        svg.add(
            Text(
                tool.description,
                position + Vector(10.0, 56.0),
                fontFamily = "Roboto",
                fontWeight = 300,
                fontSize = 11.0,
                fill = "#363228",
            )
        )
        for (author in tool.authors) {
            svg.add(
                Text(
                    author,
                    position + Vector(0.0, size.y + 20.0),
                    fontFamily = "Roboto",
                    fontWeight = 300,
                    fontSize = 11.0,
                    fill = "#363228"
                )
            )
        }
        if (tool.languages.isNotEmpty()) {
            var x = 0.0
            for (language in tool.languages) {
                val width = language.length * 6.0 + 20.0
                svg.add(
                    Rectangle(
                        position + Vector(x, -25.0),
                        Vector(width, 20.0),
                        scheme.getLanguageColor(language)
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

    var minYear = 1994;
    var maxYear = 1994;

    init {
        for (tool in tools) {
            toolMap[tool.name] = ToolPicture(tool)
            minYear = minYear.coerceAtMost(tool.since)
            maxYear = maxYear.coerceAtLeast(tool.since)
        }
        for (toolPicture in toolMap.values) {
            toolPicture.position.y = 20 + getY(toolPicture.tool.since).toDouble()
        }

        for (line in configuration) {
            val parts = line.split(" ")

            if (parts[0] == "->") {
                var tool = toolMap[parts[1].replace("_", " ")] ?: throw NotImplementedError(parts[1])
                for (i in 2 until parts.size) {
                    val tool2 = toolMap[parts[i].replace("_", " ")] ?: throw NotImplementedError(parts[i])
                    tool2.position.x = tool.position.x + tool.size.x + STEP
                    tool = tool2
                }

            } else if (parts[0] == "v") {
                var tool = toolMap[parts[1].replace("_", " ")] ?: throw NotImplementedError(parts[1])
                for (i in 2 until parts.size) {
                    val tool2 = toolMap[parts[i].replace("_", " ")] ?: throw NotImplementedError(parts[i])
                    tool2.position.x = tool.position.x
                }

            } else if (parts.size == 2) {
                val tool = toolMap[parts[0].replace("_", " ")] ?: throw NotImplementedError(parts[0])
                tool.position.x = parts[1].toDouble()

            } else if (parts.size == 3) {
                val tool = toolMap[parts[0].replace("_", " ")] ?: throw NotImplementedError(parts[0])
                val tool2 = toolMap[parts[2].replace("_", " ")] ?: throw NotImplementedError(parts[2])

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
                val affiliation = parts[0]
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

    val timeline = Timeline(
        config.tools, scheme, listOf(
            "VeriSoft 1130",
            "VeriSoft -> SPIN",
            "SPIN v DART",
            "DART -> CUTE",
            "CUTE v jCUTE",
            "SVC 410",
            "v SVC CVC CVC_Lite EXE KLEE MINESTRONE UC-KLEE",
            "SVC -> Chord",
            "v Chord EGT STP",

            "HAMPI 730",
            "HAMPI -> jFuzz",
            "jFuzz -> Z3",
            "Z3 ^ Yices",
            "Z3 -> Pex",
            "Z3 v Rex",
            "Rex -> SAGAN + JobCenter",
            "Pex -> Splat",
            "Pex v Lean",
            "Pex v PyExZ3",
            "Splat -> SAGE",
            "SAGE -> BitBlaze",
            "BitBlaze v LESE",
            "LESE v DTA++",
            "v DTA++ FuzzBALL Rosette Galactica",
            "Rosette <- Jalangi",

            "DTA++ -> AEG",
            "AEG -> BAP",
            "AEG v Mayhem",
            "Mayhem v MergePoint",
            "MergePoint v ISSTAC",
            "BAP v dReal",

            "BAP -> EFFIGY",
            "-> EFFIGY SELECT massachusetts.fortran PathCrawler GlassTT Peach JNuke Autodafé",
            "v EFFIGY Java_PathFinder JPF-SE Symbolic_PathFinder JDart",
            "v SELECT Dyninst DynamoRIO Dytan Cinger Green_Solver Pathgrind Firmalice angr",
            "v massachusetts.fortran Valgrind PIN AXGEN Debugger ILLITHID",

            "Stanford [ SVC Chord UC-KLEE ]",
            "Bell_labs [ VeriSoft SPIN DART ]",
            "MIT [ HAMPI jFuzz ]",

            "GlassTT v Flayer",
        )
    )
    timeline.draw(svg)
}
