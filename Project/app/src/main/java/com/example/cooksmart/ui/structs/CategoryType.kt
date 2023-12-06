/** "CategoryType.kt"
 *  Description: An enum object used for the ingredient type
 *               Spinner in the ingredient insert/update fragments.
 *  Last Modified: November 27, 2023
 * */
package com.example.cooksmart.ui.structs

enum class CategoryType (val asInt:Int, val asString:String){
    MeatSeafood(0,"Meat/Seafood"),
    Produce(1,"Produce"),
    DairyCheeseEggs(2,"Dairy/Cheese/Eggs"),
    Bakery(3,"Bakery"),
    Deli(4,"Deli"),
    OilsSaucesCondiments(5,"Oils/Sauces/Condiments"),
    RicesGrainsBeans(6,"Rices/Grains/Beans"),
    PastaNoodles(7, "Pasta/Noodles"),
    BakingSpices(8,"Baking/Spices"),
    CannedGoods(9,"Canned Goods"),
    NutsSeedsDriedFruit(10,"Nuts/Seeds/Dried Fruit"),
    International(11,"International"),
    ButterHoneyJam(12,"Butter/Honey/Jam"),
    Beverages(13,"Beverages"),
    CoffeeTea(14,"Coffee/Tea"),
    Frozen(15,"Frozen"),
    Snacks(16,"Snacks"),
    Other(17,"Other");

    companion object{
        fun fromString(string: String) = values().first { it.asString == string }
    }
}