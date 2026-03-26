package ru.serjik.preferences.values

class RGBValue(var r: Int = 0, var g: Int = 0, var b: Int = 0) {

    constructor(str: String) : this() {
        parse(str)
    }

    fun parse(str: String) {
        val parts = str.split(",")
        r = parts[0].toInt()
        g = parts[1].toInt()
        b = parts[2].toInt()
    }

    override fun toString(): String = "$r,$g,$b"
}
