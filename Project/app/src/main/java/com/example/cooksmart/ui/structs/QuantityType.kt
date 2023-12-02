package com.example.cooksmart.ui.structs

enum class QuantityType (val asInt:Int, val asString:String){
    Kilograms(0,"kg"),
    Grams(1,"g"),
    Litres(2,"L"),
    Millilitres(3,"mL"),
    Pounds(4,"lbs"),
    Ounces(5,"oz"),
    Fluid_Ounce(6,"fl oz"),
    Other(7,"Other");

    companion object{
        fun stringFromInt(int: Int) : String {return fromInt(int).asString}
        fun fromInt(int: Int) = values().first() {it.asInt == int}
        fun fromString(string: String) = values().first() { it.asString == string }
    }
}