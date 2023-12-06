/** "QuantityType.kt"
 *  Description: An enum object used for the quantity
 *               Spinner in the ingredient insert/update fragments.
 *  Last Modified: November 30, 2023
 * */
package com.example.cooksmart.ui.structs

enum class QuantityType (val asInt:Int, val asString:String){
    Grams(0,"g"),
    Kilograms(1,"kg"),
    Millilitres(2,"mL"),
    Litres(3,"L"),
    Quantity(4, "quantity"),
    Pounds(5,"lb"),
    Cups(6, "cup"),
    Pints(7, "pint"),
    Ounces(8,"oz"),
    FluidOunce(9,"fl oz"),
    Other(10,"Other");

    companion object{
        fun fromString(string: String) = values().first { it.asString == string }
    }
}