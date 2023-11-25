package com.example.cooksmart.ui.category

class CategoryData(s: String, meatSeafood: Int) {

    var categoryName: String? = null
    var categoryImage: Int? = 0

    fun CategoryData(movieName: String?, movieImage: Int?) {
        this.categoryName = movieName
        this.categoryImage = movieImage
    }

}
//fun getCategoryName(): String? {
//    return categoryName
//}
//
//fun setCategoryName(movieName: String?) {
//    this.categoryName = movieName
//}
//
//fun getCategoryImage(): Int? {
//    return categoryImage
//}
//
//fun setCategoryImage(movieImage: Int?) {
//    this.categoryImage = movieImage
//}

