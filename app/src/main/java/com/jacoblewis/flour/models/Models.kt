package com.jacoblewis.flour.models

import com.jacoblewis.flour.Ingredient
import com.jacoblewis.flour.Recipe
import java.util.*

@Recipe
data class SimpleModel1(
    @Ingredient
    val name: String,
    @Ingredient
    val age: Int
)

@Recipe
data class ListModel1(
    @Ingredient
    val name: String,
    @Ingredient
    val payments: List<Float>
)

@Recipe
data class MapModel1(
    @Ingredient
    val name: String,
    @Ingredient
    val grades: Map<String, Float>
)

@Recipe
data class NestedModel1(
    @Ingredient
    val list: ListModel1,
    @Ingredient
    val map: MapModel1
)

@Recipe
data class NestedModel2(
    @Ingredient
    val list: List<ListModel1>?,
    @Ingredient
    val map: Map<String, MapModel1>?
)

@Recipe
data class ModelWithAdapter1(
    @Ingredient
    val time: Date
)


@Recipe
data class ModelWithAdapter2(
    @Ingredient
    val scoreDetails: Map<Date, SimpleModel1>
)