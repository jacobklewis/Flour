package com.jacoblewis.flour.models

import com.squareup.kotlinpoet.ClassName
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier

data class RecipeDomain(
    val name: String,
    val packageName: String,
    val ingredients: List<IngredientDomain>
) {
    companion object {
        var note: (String?) -> Unit = {}
        fun create(
            env: ProcessingEnvironment,
            settings: FlourSettings,
            e: Element,
            possibleIngredients: List<IngredientDomain>,
            note: (String?) -> Unit,
            error: (String?,Element) -> Unit,
        ): RecipeDomain {
            RecipeDomain.note = note
            val name = e.simpleName.toString()
            val pkg = env.elementUtils.getPackageOf(e).qualifiedName.toString()
            val fields = e.enclosedElements
            val ingredients = possibleIngredients.filter {
                if (it.parent == name) {
                    val fField =
                        fields.firstOrNull { f -> f.simpleName.replace(Regex("\\$.*"), "") == it.name }

                    if (fField?.modifiers?.contains(Modifier.PRIVATE) == true) {
                        // It is a private field
                        error("${it.name} is private. You must define a type in the Ingredient Annotation", fField)
                    }
                    val typeStr = fField?.asType()?.toString()?.removePrefix("()")
                    note("${it.name}: $typeStr")
                    it.type = findEnclosedType(typeStr, settings)
                    true
                } else {
                    false
                }
            }

            return RecipeDomain(name, pkg, ingredients)
        }

        internal fun findEnclosedType(typeStr: String?, settings: FlourSettings): EnclosedType {
            return when {
                typeStr?.removePrefix("()")?.startsWith("java.util.List") == true -> {
                    val rawType = typeStr.removePrefix("()").removePrefix("java.util.List<").removeSuffix(">")
                    val enclosedType = findEnclosedType(rawType, settings)
                    EnclosedType.List(enclosedType)
                }
                typeStr?.removePrefix("()")?.startsWith("java.util.Map") == true -> {
                    val rawInner = typeStr.removePrefix("()").removePrefix("java.util.Map<").removeSuffix(">")
                    val sepIndex = findFirstIndexOfSeparator(rawInner)
                    val rawTypeKey = rawInner.substring(0, sepIndex)
                    val rawTypeValue = rawInner.substring(sepIndex + 1)
                    EnclosedType.Map(
                        findEnclosedType(rawTypeKey, settings),
                        findEnclosedType(rawTypeValue, settings)
                    )
                }
                else -> {
                    val type = determineType(typeStr)
                    if (type != null) {
                        EnclosedType.Normal(type)
                    } else {
                        val serType =
                            settings.adapters.firstOrNull { "${it.pack}.${it.type}" == typeStr }
                        if (serType == null) {
                            note("No Adapter for type: $typeStr")
                            EnclosedType.Normal(ClassName.bestGuess(typeStr ?: ""))
                        } else {
                            EnclosedType.Serializable(
                                ClassName(serType.pack, serType.type),
                                serType.fromString,
                                serType.toString
                            )
                        }
                    }
                }
            }
        }

        internal fun findFirstIndexOfSeparator(raw: String): Int {
            var q = 0
            val rawMapped = raw.map {
                when (it) {
                    '<' -> 1
                    '>' -> -1
                    ',' -> 2
                    else -> 0
                }
            }
            return rawMapped.indexOfFirst {
                if (it == 2) {
                    q == 0
                } else {
                    q += it
                    false
                }
            }
        }

        private fun determineType(typeStr: String?): ClassName? =
            when (typeStr?.lowercase()?.removePrefix("()")?.removePrefix("java.lang.")) {
                "int" -> ClassName("", "Int")
                "integer" -> ClassName("", "Int")
                "long" -> ClassName("", "Long")
                "float" -> ClassName("", "Float")
                "double" -> ClassName("", "Double")
                "boolean" -> ClassName("", "Boolean")
                "string" -> ClassName("kotlin", "String")
                else -> null
            }
    }
}