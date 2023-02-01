package com.jacoblewis.flour.utils

val String.removedGet: String
    get() {
        if (length > 3 && startsWith("get") && get(3).isUpperCase()) {
            val removedGet = removePrefix("get")
            return removedGet[0].lowercase() + removedGet.substring(1)
        }
        return this
    }