import kotlin.collections.listOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

const val STEP: Int = 10

@Serializable
enum class ToolType {
    /** Concolic (dynamic symbolic execution) with path exploration. */
    @SerialName("concolic")
    CONCOLIC,

    /** Concolic (dynamic symbolic execution) within one path. */
    @SerialName("concolic_one_path")
    CONCOLIC_ONE_PATH,

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

    @SerialName("DSE")
    DSE,
}

class ColorScheme {
    fun getColor(type: ToolType): String {
        return when (type) {
            ToolType.CONCOLIC -> "#E4D3C2"
            ToolType.CONCOLIC_ONE_PATH -> "#CAB7A3"
            ToolType.SAT -> "#ADC57D"
            ToolType.SMT -> "#C8DAA4"
            ToolType.MODEL_CHECKER -> "#000000"
            ToolType.STATIC_INSTRUMENTATION -> "#E0CFEB"
            ToolType.DYNAMIC_INSTRUMENTATION -> "#CFB6E0"
            ToolType.DSE -> "#000000"
        }
    }
}

@Serializable
class Config(val tools: List<Tool>)

@Serializable
open class Tool(
    val since: Int = 0,
    val type: ToolType = ToolType.CONCOLIC,
    val name: String,
) {
    fun draw(scheme: ColorScheme) {
        println(scheme.getColor(type))
    }
}

data class ToolPicture(val tool: Tool, val x: Int = 0, var y: Int = 0, var width: Int = 0, var height: Int = 0)

private data class Timeline(val tools: List<Tool>, val scheme: ColorScheme, val configuration: List<String>) {

    val toolMap: HashMap<String, ToolPicture> = HashMap()

    init {
        for (tool in tools)
            toolMap[tool.name] = ToolPicture(tool)

        for (line in configuration) {
            val parts = line.split(" ")
            val tool = toolMap[parts[0]] ?: continue

            if (parts.size == 3) {
                val tool2 = toolMap[parts[2]] ?: continue

                when (parts[1]) {
                    "→" -> tool2.y = tool.y + tool.width + STEP
                    "←" -> tool2.y = tool.y - tool2.width - STEP
                    "|" -> tool2.y = tool.y
                }
            }
        }
    }

    fun draw() {
        for (tool in tools) tool.draw(scheme)
    }
}

fun main() {

    val json = Json { ignoreUnknownKeys = true }

    val path = "../tools/tools.json"
    val config = json.decodeFromString<Config>(File(path).readText(Charsets.UTF_8))
    val scheme = ColorScheme()

    val timeline = Timeline(config.tools, scheme, listOf("a", "b"))
    timeline.draw()
}
