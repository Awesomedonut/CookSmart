package com.example.cooksmart.ui.recipe

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.ui.recipe.RecipeDetailActivity

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
        val recipe = getItem(position) as Recipe
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.recipe_list_item, parent, false)

        val recipeListId = view.findViewById<TextView>(R.id.recipe_list_id)
        val recipeListTitle = view.findViewById<TextView>(R.id.recipe_list_title)

        recipeListId.text = recipe.id.toString()
        recipeListTitle.text = recipe.name

        view.setOnClickListener {
            val intent = Intent(context, RecipeDetailActivity::class.java)
            intent.putExtra("recipeID", recipe.id)
            println(recipe.id)
            context.startActivity(intent)
        }

        return view
    }
}



//package com.example.cooksmart.ui.recipe
//
//import android.content.Context
//import android.content.Intent
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.BaseAdapter
//import android.widget.Toast
//import com.example.cooksmart.database.Recipe
//import com.example.cooksmart.databinding.RecipeListItemBinding
//
//class RecipesAdapter(private val context: Context, private val data: List<Recipe>) : BaseAdapter() {
//
//    override fun getCount(): Int {
//        return data.size
//    }
//
//    override fun getItem(position: Int): Any {
//        return data[position]
//    }
//
//    override fun getItemId(position: Int): Long {
//        return position.toLong()
//    }
//
//    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
//        val binding: RecipeListItemBinding
//        val recipe = getItem(position) as Recipe
//
//        if (convertView == null) {
//            // Inflate the layout and create a binding object
//            binding = RecipeListItemBinding.inflate(LayoutInflater.from(context), parent, false)
//            binding.root.tag = binding
//        } else {
//            // Recycling the view, retrieve the binding object
//            binding = convertView.tag as RecipeListItemBinding
//        }
//
//        // Bind the data to the layout
//        binding.recipeListId.text = recipe.id.toString()
//        binding.recipeListTitle.text = recipe.name
//
//        // Set a click listener for the whole item
//        binding.root.setOnClickListener {
//            // Handle item click, start RecipeDetailActivity with intent
//            val intent = Intent(context, RecipeDetailActivity::class.java)
//            intent.putExtra("recipeID", recipe.id)
//            println(position)
//            context.startActivity(intent)
//        }
//
//        return binding.root
//    }
//}
