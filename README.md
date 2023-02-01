Flour
============

![Logo](website/flour-icon.png)

A JSON Parsing Generator Library that utilizes the built-in JSON libraries for Android.

 * Free yourself from being forced to use a specific JSON parsing library.
 * All generated code uses Android's built-in `org.json:json:20+` library.
 * Support for nested objects, lists, and maps.
 * Support for custom adapters to bridge the gap between certain types.

```kotlin
@Recipe
data class Warrior(
    @Ingredient
    val name: String,
    @Ingredient
    val health: Int,
    @Ingredient
    val weapons: List<Weapon>
)

@Recipe
data class Weapon(
    @Ingredient
    val type: String,
    @Ingredient
    val damage: Float
)

fun main() {
    val warriorJSON = "{\"name\":\"Harlow\", \"health\":94, \"weapons\":[{\"type\":\"sword\", \"damage\":54.2}]}"
    // WarriorRecipe is automatically generated during build
    val warrior = WarriorRecipe.fromJSON(warriorJSON)
}
```

### Custom Adapters

Place a `flour-settings.json` file in your root gradle folder to define the custom adapters. Populate the `fromString` and `toString` fields with the appropriate code to transform the objects *from* and *to* a String.

**Note**: Custom Adapters only convert *from* and *to* Strings.

```json
{
  "adapters": [
    {
      "type": "Locale",
      "package": "java.util",
      "fromString": "it.split(\"-\").let{ ls -> Locale(ls.first(), ls.last()) }",
      "toString": "\"${it.language}-${it.country}\""
    },
    {
      "type": "Date",
      "package": "java.util",
      "fromString": "Date(it.toLong())",
      "toString": "it.time.toString()"
    }
  ]
}
```

Then use Flour like normal throughout your project.

```kotlin
@Recipe
data class MyContract(
    @Ingredient
    val signedTime: Date,
    @Ingredient
    val locale: Locale,
    @Ingredient
    val contactText: String
)
```



Download
--------

```groovy
android {
  ...
  // Flour might require Java 8 (optional)
  compileOptions {
    sourceCompatibility JavaVersion.VERSION_1_8
    targetCompatibility JavaVersion.VERSION_1_8
  }
}

dependencies {
  compileOnly 'me.jacoblewis.flour:flour:0.1.0'
  kapt 'me.jacoblewis.flour:flour-gen:0.1.0'
}
```

License
-------

    Copyright 2020 Jacob Lewis
    
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
       http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

