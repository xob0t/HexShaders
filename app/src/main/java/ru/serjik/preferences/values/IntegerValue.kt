package ru.serjik.preferences.values

class IntegerValue @JvmOverloads constructor(var value: Int = 0) {

    constructor(str: String) : this() {
        parse(str)
    }

    fun parse(str: String) {
        value = str.toInt()
    }

    override fun toString(): String = value.toString()
}
