package com.jacoblewis.flour

import com.google.common.truth.Truth.assertThat
import com.jacoblewis.flour.models.*
import org.json.JSONObject
import org.junit.Test
import java.util.*

class JSONParsingTests {
    @Test
    fun simpleModelParsingTest() {
        val json = "{\"name\":\"John\",\"age\":12}"
        val modelForm = SimpleModel1Recipe.fromJSON(json)
        assertThat(modelForm).isEqualTo(SimpleModel1("John", 12))
        val json2 = SimpleModel1Recipe.toJSON(modelForm)
        assertThat(json2).isEqualTo(json)
    }

    @Test
    fun listModelParsingTest() {
        val json = JSONObject("{\"name\":\"John\",\"payments\":[1,5]}").toString()
        val modelForm = ListModel1Recipe.fromJSON(json)
        assertThat(modelForm).isEqualTo(ListModel1("John", listOf(1f, 5f)))
        val json2 = ListModel1Recipe.toJSON(modelForm)
        assertThat(json2).isEqualTo(json)
    }

    @Test
    fun mapModelParsingTest() {
        val json = JSONObject("{\"name\":\"John\",\"grades\":{\"Jake\":85,\"Kate\":89}}").toString()
        val modelForm = MapModel1Recipe.fromJSON(json)
        assertThat(modelForm).isEqualTo(MapModel1("John", mapOf("Jake" to 85f, "Kate" to 89f)))
        val json2 = MapModel1Recipe.toJSON(modelForm)
        assertThat(json2).isEqualTo(json)
    }

    @Test
    fun nestedModelParsingTest() {
        val json =
            JSONObject("{\"list\":{\"name\":\"John\",\"payments\":[1,5]},\"map\":{\"name\":\"John\",\"grades\":{\"Jake\":85,\"Kate\":89}}}").toString()
        val modelForm = NestedModel1Recipe.fromJSON(json)
        assertThat(modelForm).isEqualTo(
            NestedModel1(
                ListModel1("John", listOf(1f, 5f)),
                MapModel1("John", mapOf("Jake" to 85f, "Kate" to 89f))
            )
        )
        val json2 = NestedModel1Recipe.toJSON(modelForm)
        assertThat(json2).isEqualTo(json)
    }

    @Test
    fun nestedModel2ParsingTest() {
        val json =
            JSONObject("{\"list\":[{\"name\":\"John\",\"payments\":[1,5]}],\"map\":{\"first\":{\"name\":\"John\",\"grades\":{\"Jake\":85,\"Kate\":89}}}}").toString()
        val modelForm = NestedModel2Recipe.fromJSON(json)
        assertThat(modelForm).isEqualTo(
            NestedModel2(
                listOf(ListModel1("John", listOf(1f, 5f))),
                mapOf("first" to MapModel1("John", mapOf("Jake" to 85f, "Kate" to 89f)))
            )
        )
        val json2 = NestedModel2Recipe.toJSON(modelForm)
        assertThat(json2).isEqualTo(json)
    }

    @Test
    fun modelWithAdapter1ParsingTest() {
        val json = JSONObject("{\"time\":\"205400\"}").toString()
        val modelForm = ModelWithAdapter1Recipe.fromJSON(json)
        assertThat(modelForm).isEqualTo(ModelWithAdapter1(Date(205400L)))
        val json2 = ModelWithAdapter1Recipe.toJSON(modelForm)
        assertThat(json2).isEqualTo(json)
    }

    @Test
    fun modelWithAdapter2ParsingTest() {
        val json =
            JSONObject("{\"scoreDetails\":{\"205400\":{\"name\":\"James\",\"age\":54}, \"205480\":{\"name\":\"Sam\",\"age\":36}}}").toString()
        val modelForm = ModelWithAdapter2Recipe.fromJSON(json)
        assertThat(modelForm).isEqualTo(
            ModelWithAdapter2(
                mapOf(
                    Date(205400L) to SimpleModel1("James", 54),
                    Date(205480L) to SimpleModel1("Sam", 36)
                )
            )
        )
        val json2 = ModelWithAdapter2Recipe.toJSON(modelForm)
        assertThat(json2).isEqualTo(json)
    }
}