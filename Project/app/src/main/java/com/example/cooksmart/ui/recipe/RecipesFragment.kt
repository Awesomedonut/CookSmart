package com.example.cooksmart.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.databinding.FragmentRecipeBinding

class RecipesFragment : Fragment() {

    private var _binding: FragmentRecipeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recipesViewModel: RecipesViewModel
    private lateinit var adapter: RecipesAdapter
    private val recipeList = mutableListOf<Recipe>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRecipeBinding.inflate(inflater, container, false)
        val layout = binding.root

        // Set up ListView and Adapter
        val listView = layout.findViewById<ListView>(R.id.recipe_list)
        adapter = RecipesAdapter(requireContext(), recipeList)
        listView.adapter = adapter

        // Create ViewModel
        recipesViewModel = ViewModelProvider(this).get(RecipesViewModel::class.java)

        // Insert demo recipes
        insertDemoRecipes()

        // Observe LiveData for recipe updates
        recipesViewModel.readAllRecipes.observe(viewLifecycleOwner) { recipes ->
            recipeList.clear()
            recipeList.addAll(recipes)
            adapter.notifyDataSetChanged()
        }

        // Set a click listener for the ListView items
        listView.setOnItemClickListener { _, _, position, _ ->
            val clickedRecipe = recipeList[position]

            // Handle item click, e.g., navigate to details page
            // Pass the recipe ID to the next fragment/activity
//            val action = RecipesFragmentDirections.actionRecipesFragmentToRecipeDetailsFragment(clickedRecipe.id)
//            findNavController().navigate(action)
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
