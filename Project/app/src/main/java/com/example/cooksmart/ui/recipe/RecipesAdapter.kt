package com.example.cooksmart.ui.recipe

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.databinding.RecipeListItemBinding

class RecipesAdapter(private val context: Context, private val data: List<Recipe>) : BaseAdapter() {

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: RecipeListItemBinding
        val recipe = getItem(position) as Recipe

        if (convertView == null) {
            // Inflate the layout and create a binding object
            binding = RecipeListItemBinding.inflate(LayoutInflater.from(context), parent, false)
            binding.root.tag = binding
        } else {
            // Recycling the view, retrieve the binding object
            binding = convertView.tag as RecipeListItemBinding
        }

        // Bind the data to the layout
        binding.recipeListId.text = recipe.id.toString()
        binding.recipeListTitle.text = recipe.name

        // Set a click listener for the whole item
        binding.root.setOnClickListener {
            // Handle item click, e.g., navigate to details page
            // Pass the recipe ID to the next fragment/activity
//            val action = RecipesFragmentDirections.actionRecipesFragmentToRecipeDetailsFragment(recipe.id)
//            (context as AppCompatActivity).findNavController(R.id.nav_host_fragment).navigate(action)
//
        }

        return binding.root
    }
}
