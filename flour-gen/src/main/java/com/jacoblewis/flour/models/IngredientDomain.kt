package com.jacoblewis.flour.models

import com.jacoblewis.flour.Ingredient
import com.jacoblewis.flour.utils.removedGet
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

data class IngredientDomain(
    val name: String,
    val jsonName: String,
    val parent: String,
    var type: EnclosedType
) {
    companion object {
        fun create(env: ProcessingEnvironment, e: Element): IngredientDomain {
            val name = e.simpleName.toString().removeSuffix("\$annotations")
            val ann = e.getAnnotation(Ingredient::class.java)
            val jsonName = ann.serializedName.takeIf { it.isNotBlank() } ?: name.removedGet
            val parent = e.enclosingElement.simpleName.toString()
            return IngredientDomain(
                name,
                jsonName,
                parent,
                EnclosedType.NotDetermined
            )
        }
    }
}