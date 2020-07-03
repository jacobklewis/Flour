package com.jacoblewis.flour

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jacoblewis.flour.models.Person
import com.jacoblewis.flour.models.PersonRecipe
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var p: Person
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val obj = JSONObject()
        val json = PersonRecipe.toJSON(p)

        val timeStr = "123455"
        Date(timeStr.toLong())
        val time = Date().time.toString()
    }
}