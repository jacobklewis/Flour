package com.jacoblewis.flour.models

import com.jacoblewis.flour.Ingredient
import com.jacoblewis.flour.Recipe
import java.util.*

@Recipe
data class Person(
    @Ingredient
    val name: String,
    @Ingredient
    val age: Int,
    @Ingredient
    val ageMillis: Long,
    @Ingredient
    val salary: Float,
    @Ingredient
    val favNum: Map<String, Map<String, Locale>>,
    @Ingredient
    val employed: Boolean,
    @Ingredient("stubs")
    val payments: List<List<Int>>,
    @Ingredient
    val company: Company
)