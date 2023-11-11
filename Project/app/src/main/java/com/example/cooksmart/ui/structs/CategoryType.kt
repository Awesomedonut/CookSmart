package com.example.cooksmart.ui.structs

enum class CategoryType (val asInt:Int, val asString:String){
    Meat_Seafood(0,"Meat/Seafood"),
    Produce(1,"Produce"),
    Dairy_Cheese_Eggs(2,"Dairy/Cheese/Eggs"),
    Bakery(3,"Bakery"),
    Deli(4,"Deli"),
    Nuts_Seeds_DriedFruit(5,"Nuts/Seeds/Dried Fruit"),
    Butter_Honey_Jam(6,"Butter/Honey/Jam"),
    Baking_Spices(7,"Baking/Spices"),
    Beverages(8,"Beverages"),
    Coffee_Tea(9,"Coffee/Tea"),
    Frozen(10,"Frozen"),
    Rices_Grains_Beans(11,"Rices/Grains/Beans"),
    Canned_goods(12,"Canned Goods"),
    Oils_Sauces_Condiments(13,"Oils/Sauces/Condiments"),
    International(14,"International"),
    Snacks(15,"Snacks"),
    Other(16,"Other");

    companion object{
        fun stringFromInt(int: Int) : String {return fromInt(int).asString}
        fun fromInt(int: Int) = values().first() {it.asInt == int}
        fun fromString(string: String) = values().first() { it.asString == string }
    }
}