package com.example.cooksmart.ui.recipe

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.databinding.FragmentRecipeBinding

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipesViewModel: RecipesViewModel
    private lateinit var listView: ListView
    private lateinit var adapter: RecipesAdapter
    private val recipeList = mutableListOf<Recipe>()

    private var hasInserted: Boolean
        get() = requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
            .getBoolean("hasInserted", false)
        set(value) {
            requireContext().getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("hasInserted", value)
                .apply()
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        val layout = binding.root

        // Set up ListView and Adapter
        listView = layout.findViewById(R.id.recipe_list)
        adapter = RecipesAdapter(requireContext(), recipeList)
        listView.adapter = adapter

        // Create ViewModel
        recipesViewModel = ViewModelProvider(this).get(RecipesViewModel::class.java)

        // Insert demo recipes
        if(!hasInserted) {
            insertDemoRecipes()
            Toast.makeText(requireContext(), "INSERTED", Toast.LENGTH_SHORT).show()
            hasInserted = true
        }

        // Observe LiveData for recipe updates
        recipesViewModel.readAllRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeList.clear()
            recipeList.addAll(recipes)
            adapter.notifyDataSetChanged()
        }

        // Set a click listener for the ListView items
        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedRecipe = recipeList[position]
            Toast.makeText(requireContext(), "CLICKED $position", Toast.LENGTH_SHORT).show()

            // Handle item click, start RecipeDetailActivity with intent
            val intent = Intent(requireContext(), RecipeDetailActivity::class.java)
            intent.putExtra("recipeID", clickedRecipe.id)
            intent.putExtra("recipeName", clickedRecipe.name)
            intent.putExtra("recipeIngredients", clickedRecipe.ingredients)
            intent.putExtra("recipeInstructions", clickedRecipe.instructions)
            startActivity(intent)
        }

        return layout
    }

    private fun insertDemoRecipes() {
        val demoRecipes = listOf(
            Recipe(id=0, name = "Spaghetti Bolognese", ingredients = "Pasta, Tomatoes, Beef", instructions = "Cook and enjoy"),
            Recipe(id=0, name = "Caesar Salad", ingredients = "Lettuce, Croutons, Chicken", instructions = "Toss and serve"),
            // Add more demo recipes as needed
        )

        for (recipe in demoRecipes) {
            recipesViewModel.insertRecipe(recipe)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

