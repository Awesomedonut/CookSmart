package com.example.cooksmart.ui.structs

enum class CategoryType (val asInt:Int, val asString:String){
    Meat_Seafood(0,"Meat/Seafood"),
    Produce(1,"Produce"),
    Dairy_Cheese_Eggs(2,"Dairy/Cheese/Eggs"),
    Bakery(3,"Bakery"),
    Deli(4,"Deli"),
    Oils_Sauces_Condiments(5,"Oils/Sauces/Condiments"),
    Rices_Grains_Beans(6,"Rices/Grains/Beans"),
    Pasta_Noodles(7, "Pasta/Noodles"),
    Baking_Spices(8,"Baking/Spices"),
    Canned_goods(9,"Canned Goods"),
    Nuts_Seeds_DriedFruit(10,"Nuts/Seeds/Dried Fruit"),
    International(11,"International"),
    Butter_Honey_Jam(12,"Butter/Honey/Jam"),
    Beverages(13,"Beverages"),
    Coffee_Tea(14,"Coffee/Tea"),
    Frozen(15,"Frozen"),
    Snacks(16,"Snacks"),
    Other(17,"Other");

    companion object{
        fun stringFromInt(int: Int) : String {return fromInt(int).asString}
        fun fromInt(int: Int) = values().first() {it.asInt == int}
        fun fromString(string: String) = values().first() { it.asString == string }
    }
}