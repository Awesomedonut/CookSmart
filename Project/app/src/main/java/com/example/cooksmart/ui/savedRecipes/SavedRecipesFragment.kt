package com.example.cooksmart.ui.savedRecipes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import com.google.android.material.floatingactionbutton.FloatingActionButton


class SavedRecipesFragment : Fragment() {
    private lateinit var savedRecipeViewModel: SavedRecipeViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved_recipes, container, false)

        // RecyclerView
        val adapter = SavedRecipesListAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recipe_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        savedRecipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

        savedRecipeViewModel.readAllRecipes.observe(viewLifecycleOwner) { recipe ->
            adapter.setData(recipe)
        }

        // insert sample recipe to  when plus button is pressed
        view.findViewById<FloatingActionButton>(R.id.recipe_add).setOnClickListener{
            insertRecipe()
        }

        return view
    }

    private fun insertRecipe() {
        val name = "Chicken Alfredo"
        val ingredients = "Chicken breast (cut), penne (255g), alfredo sauce"
        val currentDate = System.currentTimeMillis()
        val instructions = "Cut chicken. Cook chicken and pasta. While pasta is cooking, heat sauce in saucepan. Mix together and serve."
        val isFavorite = true
        val recipe = Recipe(0, name ,ingredients, instructions, currentDate, isFavorite)
        savedRecipeViewModel.insertRecipe(recipe)
        Toast.makeText(requireContext(), "Successfully added recipe!", Toast.LENGTH_SHORT).show()
    }
}