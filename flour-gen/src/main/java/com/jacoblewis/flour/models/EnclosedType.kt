package com.jacoblewis.flour.models

import com.squareup.kotlinpoet.ClassName

sealed class EnclosedType {
    data class Normal(val className: ClassName) : EnclosedType()
    data class List(val enclosedType: EnclosedType) : EnclosedType() {
        val genericType: String
            get() = when (enclosedType) {
                is Normal -> enclosedType.className.simpleName.let {
                    if (it == "Integer") "Int" else it
                }
                is Serializable -> enclosedType.className.simpleName
                is List -> "List<${enclosedType.genericType}>"
                is Map -> "Map<${enclosedType.genericTypeKey},${enclosedType.genericTypeValue}>"
                else -> ""
            }
    }

    data class Map(val enclosedTypeKey: EnclosedType, val enclosedTypeValue: EnclosedType) :
        EnclosedType() {
        val genericTypeKey: String
            get() = when (enclosedTypeKey) {
                is Normal -> enclosedTypeKey.className.simpleName.let {
                    if (it == "Integer") "Int" else it
                }
                is Serializable -> enclosedTypeKey.className.simpleName
                is List -> "List<${enclosedTypeKey.genericType}>"
                is Map -> "Map<${enclosedTypeKey.genericTypeKey},${enclosedTypeKey.genericTypeValue}>"
                else -> ""
            }
        val genericTypeValue: String
            get() = when (enclosedTypeValue) {
                is Normal -> enclosedTypeValue.className.simpleName.let {
                    if (it == "Integer") "Int" else it
                }
                is Serializable -> enclosedTypeValue.className.simpleName
                is List -> "List<${enclosedTypeValue.genericType}>"
                is Map -> "Map<${enclosedTypeValue.genericTypeKey},${enclosedTypeValue.genericTypeValue}>"
                else -> ""
            }
    }

    data class Serializable(
        val className: ClassName,
        val fromJSONMapping: String,
        val toJSONMapping: String
    ) : EnclosedType()

    object NotDetermined : EnclosedType()
}