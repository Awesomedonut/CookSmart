package com.example.cooksmart.ui.ingredient

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.cooksmart.database.CookSmartDatabase
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.database.IngredientRepository
import com.example.cooksmart.database.Recipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class IngredientViewModel(application: Application): AndroidViewModel(application) {
    val readAllIngredients: LiveData<List<Ingredient>>
    private val repository: IngredientRepository

    init {
        val ingredientDao = CookSmartDatabase.getCookSmartDatabase(application).ingredientDao()
        repository = IngredientRepository(ingredientDao)
        readAllIngredients = repository.allIngredients
    }

    fun insertIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertIngredient(ingredient)
        }
    }

    fun updateIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateIngredient(ingredient)
        }
    }

    fun deleteIngredient(ingredient: Ingredient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteIngredient(ingredient)
        }
    }

    fun deleteAllIngredients() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllIngredients()
        }
    }

    fun searchIngredient(searchQuery: String): LiveData<List<Ingredient>> {
        return repository.searchIngredient(searchQuery)
    }

    fun getIngredeintSortedByName(): LiveData<List<Ingredient>> {
        return repository.getIngredeintSortedByName()
    }
    fun showBestDayOldest(): LiveData<List<Ingredient>> {
        return repository.showBestDayOldest()
    }
    fun showBestDayNewest(): LiveData<List<Ingredient>> {
        return repository.showBestDayNewest()
    }

    fun showAddedDayNewest(): LiveData<List<Ingredient>> {
        return repository.showAddedDayNewest()
    }
}