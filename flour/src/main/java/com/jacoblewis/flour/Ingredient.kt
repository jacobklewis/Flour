package com.jacoblewis.flour

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.SOURCE)
annotation class Ingredient(
    val serializedName: String = ""
)