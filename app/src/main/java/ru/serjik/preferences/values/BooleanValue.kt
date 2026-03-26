package ru.serjik.preferences.values

class BooleanValue @JvmOverloads constructor(var value: Boolean = false) {

    constructor(str: String) : this() {
        parse(str)
    }

    fun parse(str: String) {
        value = str == "true"
    }

    override fun toString(): String = if (value) "true" else "false"
}
