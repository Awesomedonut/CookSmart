/** "IngredientViewModel.kt"
 *   Description: Allows users to access the Ingredient repository
 *                and access operations to the Ingredient table in the database
 * */
package com.example.cooksmart.ui.ingredient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.CookSmartDatabase
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.database.IngredientRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IngredientViewModel(application: Application): AndroidViewModel(application) {
    // Declare ingredient list and database object
    val readAllIngredients: LiveData<List<Ingredient>>
    private val repository: IngredientRepository

    init {
        val ingredientDao = CookSmartDatabase.getCookSmartDatabase(application).ingredientDao()
        repository = IngredientRepository(ingredientDao)
        readAllIngredients = repository.allIngredients
    }

    /** "insertIngredient"
     *  Description: Inserts an ingredient entity
     * */
    fun insertIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertIngredient(ingredient)
        }
    }

    /** "updateIngredient"
     *  Description: Updates an ingredient entity
     * */
    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateIngredient(ingredient)
        }
    }

    /** "deleteIngredient"
     *  Description: Deletes a single ingredient entity
     * */
    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteIngredient(ingredient)
        }
    }

    /** "deleteAllIngredient"
     *  Description: Deletes a ingredient entries
     * */
    fun deleteAllIngredients() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllIngredients()
        }
    }

    /** "searchIngredient"
     *  Description: Uses a string to search through the database
     *               and locate an ingredient which matches
     * */
    fun searchIngredient(searchQuery: String): LiveData<List<Ingredient>> {
        return repository.searchIngredient(searchQuery)
    }

    /** "getIngredientSortedByAlphabet"
     *  Description: Returns a sorted ingredient list, sorted alphabetically
     * */
    fun getIngredientSortedByAlphabet(): LiveData<List<Ingredient>> {
        return repository.getIngredientSortedByAlphabet()
    }

    /** "showBestDayOldest"
     *  Description: Retrieves a list of ingredients, sorted by their best before date descending
     * */
    fun showBestDayOldest(): LiveData<List<Ingredient>> {
        return repository.showBestDayOldest()
    }

    /** "showBestDayNewest"
     *  Description: Retrieves a list of ingredients, sorted by their best before date ascending
     */
    fun showBestDayNewest(): LiveData<List<Ingredient>> {
        return repository.showBestDayNewest()
    }


    /** "showBestDayNewest"
     *  Description: Retrieves a list of ingredients, sorted by insertion date
     */
    fun showAddedDayNewest(): LiveData<List<Ingredient>> {
        return repository.showAddedDayNewest()
    }
}