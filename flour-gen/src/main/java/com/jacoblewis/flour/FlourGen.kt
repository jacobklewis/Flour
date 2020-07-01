package com.jacoblewis.flour

import com.google.auto.service.AutoService
import com.jacoblewis.flour.models.*
import com.jacoblewis.flour.utils.CodeBuilder
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.json.JSONObject
import java.io.File
import java.util.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.tools.Diagnostic

@AutoService(Processor::class)
class FlourGen : AbstractProcessor() {

    var logger: Messager? = null
    var settings: FlourSettings? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        logger = processingEnv?.messager
    }

    fun note(message: Any?) {
        message ?: return
        logger?.printMessage(Diagnostic.Kind.NOTE, "$message\r\n")
    }

    fun error(message: Any?, element: Element) {
        message ?: return
        logger?.printMessage(Diagnostic.Kind.ERROR, message.toString(), element)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Recipe::class.java.name, Ingredient::class.java.name)
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment
    ): Boolean {
        loadSettingsFile()

        val elementsWithRecipe = roundEnv.getElementsAnnotatedWith(Recipe::class.java)
        val elementsWithIngredient = roundEnv.getElementsAnnotatedWith(Ingredient::class.java)

        val ingredients = elementsWithIngredient.map { IngredientDomain.create(processingEnv, it) }
        val recipes =
            elementsWithRecipe.map {
                RecipeDomain.create(
                    processingEnv, settings ?: FlourSettings(
                        emptyList()
                    ), it, ingredients, ::note
                )
            }

        recipes.forEach {
            note(it)
        }

        for (recipe in recipes) {
            val obj = buildRecipe(recipe)
            saveRecipe(recipe.packageName, obj, settings?.adapters ?: emptyList())
        }
        return true
    }

    private fun loadSettingsFile() {
        val settingsFile = File("flour-settings.json")
        if (settingsFile.exists()) {
            val settingsStr = settingsFile.inputStream().readBytes().toString(Charsets.UTF_8)
            val settingsJSON = JSONObject(settingsStr)
            val adaptersJSON = settingsJSON.getJSONArray("adapters")
            val adapters = mutableListOf<FlourAdapter>()
            for (adapt in 0 until adaptersJSON.length()) {
                val aObj = adaptersJSON.getJSONObject(adapt)
                adapters.add(
                    FlourAdapter(
                        aObj.optString("type", ""),
                        aObj.optString("package", ""),
                        aObj.optString("fromString", ""),
                        aObj.optString("toString", "")
                    )
                )
            }
            settings = FlourSettings(adapters)
        }
    }

    fun buildRecipe(recipe: RecipeDomain): TypeSpec {
        val objName = "${recipe.name}Recipe"

        val obj = TypeSpec.objectBuilder(objName)
        with(obj) {
            addFunction(buildToJSONFunction(recipe))
            addFunction(buildFromJSONFunction(recipe))
        }

        return obj.build()
    }

    private fun saveRecipe(
        packageName: String,
        obj: TypeSpec,
        additionalImports: List<FlourAdapter>
    ) {
        val name = obj.name ?: return
        val file = FileSpec.builder(packageName = packageName, fileName = name)
            .addType(obj)
        file.addImport("org.json", "JSONObject")
        file.addImport("org.json", "JSONArray")
        for (im in additionalImports) {
            file.addImport(im.pack, im.type)
        }
        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]
        file.build().writeTo(File(kaptKotlinGeneratedDir))
    }

    internal fun buildToJSONFunction(recipe: RecipeDomain): FunSpec {
        val f = FunSpec.builder("toJSON")
            .addParameter("obj", ClassName(recipe.packageName, recipe.name))
            .returns(ClassName("kotlin", "String"))
        f.addCode("val json = JSONObject()\n")
        for (ingredient in recipe.ingredients) {
            val t = ingredient.type
            val encCode = computeTypeEncode(
                t,
                ingredient.name,
                "json",
                ParentWrapper.None(ingredient.jsonName)
            )
            f.addCode(encCode)
        }
        f.addCode("return json.toString()")
        return f.build()
    }

    private fun computeTypeEncode(
        t: EnclosedType,
        propName: String,
        jsonObjName: String,
        parentWrapper: ParentWrapper,
        level: Int = 0
    ): String {
        val cb = CodeBuilder.create(level)
        when (t) {
            is EnclosedType.Normal -> {
                when (parentWrapper) {
                    is ParentWrapper.Array -> cb.addLine("${jsonObjName}.put(${parentWrapper.jsonName})")
                    is ParentWrapper.Map -> cb.addLine("${jsonObjName}.put(${parentWrapper.jsonName}.key, ${parentWrapper.jsonName}.value)")
                    is ParentWrapper.None -> cb.addLine("${jsonObjName}.put(\"${parentWrapper.jsonName}\", obj.${propName})")
                }
            }
            is EnclosedType.List -> {
                val pName = when (parentWrapper) {
                    is ParentWrapper.Array -> propName
                    is ParentWrapper.Map -> "$propName.value"
                    else -> "obj.$propName"
                }
                val i = "i${level}"
                cb.addLine("val ${propName}Arr = JSONArray()")
                cb.addLine("for ($i in $pName) {")
                val codeChuck = computeTypeEncode(
                    t.enclosedType,
                    i,
                    "${propName}Arr",
                    parentWrapper = ParentWrapper.Array(i),
                    level = level + 1
                )
                cb.addLine(codeChuck)
                if (!codeChuck.contains("${propName}Arr.put")) {
                    cb.addLine("${propName}Arr.put(${i}Arr)")
                }
                cb.addLine("}")
                if (parentWrapper is ParentWrapper.None) {
                    cb.addLine("${jsonObjName}.put(\"${parentWrapper.jsonName}\", ${propName}Arr)")
                }
            }
            is EnclosedType.Map -> {
                val pName = when (parentWrapper) {
                    is ParentWrapper.Array -> propName
                    is ParentWrapper.Map -> "$propName.value"
                    else -> "obj.$propName"
                }
                val i = "i${level}"
                cb.addLine("val ${propName}Map = JSONObject()")
                cb.addLine("for ($i in $pName) {")
                val codeChuck = computeTypeEncode(
                    t.enclosedTypeValue,
                    i,
                    "${propName}Map",
                    parentWrapper = ParentWrapper.Map(propName, i),
                    level = level + 1
                )
                cb.addLine(codeChuck)
                if (!codeChuck.contains("${propName}Map.put")) {
                    val objSuffix = if (t.enclosedTypeValue is EnclosedType.Map) "Map" else "Arr"
                    cb.addLine("${propName}Map.put($i.key, $i$objSuffix)")
                }
                cb.addLine("}")
                if (parentWrapper is ParentWrapper.None) {
                    cb.addLine("${jsonObjName}.put(\"${parentWrapper.jsonName}\", ${propName}Map)")
                }
            }
            is EnclosedType.Serializable -> {
                when (parentWrapper) {
                    is ParentWrapper.Array -> cb.addLine("${jsonObjName}.put($propName.let{${t.toJSONMapping}})")
                    is ParentWrapper.Map -> cb.addLine("${jsonObjName}.put(\"${parentWrapper.jsonName}\", $propName.value.let{${t.toJSONMapping}})")
                    is ParentWrapper.None -> cb.addLine("${jsonObjName}.put(\"${parentWrapper.jsonName}\", obj.$propName.let{${t.toJSONMapping}})")
                }
            }
        }
        return cb.build()
    }


    internal fun buildFromJSONFunction(recipe: RecipeDomain): FunSpec {
        val f = FunSpec.builder("fromJSON")
            .addParameter("jsonStr", ClassName("kotlin", "String"))
            .returns(ClassName(recipe.packageName, recipe.name))
        f.addCode("val json = JSONObject(jsonStr)\n")

        val props = mutableListOf<String>()
        for (ingredient in recipe.ingredients) {
            val t = ingredient.type
            val convCode =
                computeTypeConversion(
                    t,
                    ingredient.name,
                    "json",
                    ParentWrapper.None("\"${ingredient.jsonName}\"")
                )
            f.addCode(convCode)
            props.add("${ingredient.name} = ${ingredient.name}")
        }

        f.addCode("return ${recipe.name}(${props.joinToString(", ")})")
        return f.build()
    }

    private fun computeTypeConversion(
        t: EnclosedType,
        propName: String,
        jsonObjName: String,
        parentWrapper: ParentWrapper,
        level: Int = 0
    ): String {
        val cb = CodeBuilder.create(level)
        when (t) {
            is EnclosedType.Normal -> {
                val (convertStr, isCustomObj, getType) = getType(t)
                // Custom Obj is a nested Recipe as an ingredient
                if (isCustomObj) {
                    cb.addLine("val ${propName}JSON = $jsonObjName.$getType(${parentWrapper.jsonName})$convertStr")
                    when (parentWrapper) {
                        is ParentWrapper.Array -> cb.addLine("$propName.add(${t.className.simpleName}Recipe.fromJSON(${propName}JSON.toString()))")
                        is ParentWrapper.None -> cb.addLine("val $propName = ${t.className.simpleName}Recipe.fromJSON(${propName}JSON.toString())")
                    }
                } else {
                    when (parentWrapper) {
                        is ParentWrapper.Array -> cb.addLine("${propName.removeSuffix("0")}.add($jsonObjName.$getType(${parentWrapper.jsonName})$convertStr)")
                        is ParentWrapper.Map -> cb.addLine("${propName.removeSuffix("0")}[${parentWrapper.jsonName}] = $jsonObjName.$getType(${parentWrapper.jsonName})$convertStr")
                        is ParentWrapper.None -> cb.addLine("val $propName = $jsonObjName.$getType(${parentWrapper.jsonName})$convertStr")
                    }
                }
            }
            is EnclosedType.List -> {
                val i = "i$level"
                cb.addLine("val $propName = mutableListOf<${t.genericType}>()")
                cb.addLine("val ${propName}Arr = $jsonObjName.getJSONArray(${parentWrapper.jsonName})")
                cb.addLine("for ($i in 0 until ${propName}Arr.length()) {")
                val codeChuck = computeTypeConversion(
                    t.enclosedType,
                    "${propName}0",
                    "${propName}Arr",
                    parentWrapper = ParentWrapper.Array(i),
                    level = level + 1
                )
                cb.addLine(codeChuck)
                if (!codeChuck.contains("${propName}.add")) {
                    cb.addLine("$propName.add(${propName}0)")
                }
                cb.addLine("}")
            }
            is EnclosedType.Map -> {
                val i = "i$level"
                cb.addLine("val $propName = mutableMapOf<${t.genericTypeKey},${t.genericTypeValue}>()")
                cb.addLine("val ${propName}Map = $jsonObjName.getJSONObject(${parentWrapper.jsonName})")
                cb.addLine("for ($i in ${propName}Map.keys()) {")
                if (t.enclosedTypeKey is EnclosedType.Serializable) {
                    TODO()
                }
                val codeChuck = computeTypeConversion(
                    t.enclosedTypeValue,
                    "${propName}0",
                    "${propName}Map",
                    parentWrapper = ParentWrapper.Map(propName, i),
                    level = level + 1
                )
                cb.addLine(codeChuck)
                if (!codeChuck.contains("${propName}[$i]")) {
                    cb.addLine("${propName}[$i] = ${propName}0")
                }
                cb.addLine("}")
            }
            is EnclosedType.Serializable -> {
                cb.addLine("val $propName = $jsonObjName.getString(${parentWrapper.jsonName}).let{${t.fromJSONMapping}}")
            }
        }
        return cb.build()
    }

    sealed class ParentWrapper(open val jsonName: String) {
        data class None(override val jsonName: String) : ParentWrapper(jsonName)
        data class Array(override val jsonName: String) : ParentWrapper(jsonName)
        data class Map(val mapObj: String, override val jsonName: String) : ParentWrapper(jsonName)
    }

    private fun getType(t: EnclosedType.Normal): Triple<String, Boolean, String> {
        var convertStr = ""
        var isCustomObj = false
        val typeStr = when (t.className.simpleName.toLowerCase(Locale.ENGLISH)) {
            "int" -> "getInt"
            "integer" -> "getInt"
            "long" -> "getLong"
            "float" -> {
                convertStr = ".toFloat()"
                "getDouble"
            }
            "double" -> "getDouble"
            "string" -> "getString"
            "boolean" -> "getBoolean"
            else -> {
                isCustomObj = true
                "getJSONObject"
            }
        }
        return Triple(convertStr, isCustomObj, typeStr)
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}