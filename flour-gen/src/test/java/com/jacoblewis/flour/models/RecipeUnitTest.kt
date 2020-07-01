package com.jacoblewis.flour.models

import com.squareup.kotlinpoet.ClassName
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class RecipeUnitTest {
    @Test
    fun findFirstIndexOfSeparator_Map1() {
        val v1 = "String,String"
        val i = RecipeDomain.findFirstIndexOfSeparator(v1)
        assertEquals(6, i)
    }

    @Test
    fun findFirstIndexOfSeparator_Map2() {
        val v1 = "java.util.Map<String,String>,String"
        val i = RecipeDomain.findFirstIndexOfSeparator(v1)
        assertEquals(28, i)
    }

    @Test
    fun findFirstIndexOfSeparator_Map3() {
        val v1 = "java.util.Map<java.util.List<Int>,String>,String"
        val i = RecipeDomain.findFirstIndexOfSeparator(v1)
        assertEquals(41, i)
    }

    @Test
    fun findEnclosedType_Map() {
        val v1 = "java.util.Map<java.util.Map<java.util.List<Int>,String>,String>"
        val type = RecipeDomain.findEnclosedType(v1, FlourSettings(emptyList()))
        assertEquals(
            EnclosedType.Map(
                EnclosedType.Map(
                    EnclosedType.List(
                        EnclosedType.Normal(
                            ClassName("", "Int")
                        )
                    ), EnclosedType.Normal(ClassName("kotlin", "String"))
                ), EnclosedType.Normal(ClassName("kotlin", "String"))
            ), type
        )
    }

}