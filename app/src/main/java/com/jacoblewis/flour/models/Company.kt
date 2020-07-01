package com.jacoblewis.flour.models

import com.jacoblewis.flour.Ingredient
import com.jacoblewis.flour.Recipe
import java.util.*

@Recipe
data class Company(
    @Ingredient
    val name: String,
    @Ingredient
    val address: String,
    @Ingredient
    val language: Locale
)