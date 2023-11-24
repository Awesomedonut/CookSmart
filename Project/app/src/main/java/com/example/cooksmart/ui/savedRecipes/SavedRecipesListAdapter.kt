package com.example.cooksmart.ui.savedRecipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Shows each row in the recipe database in a RecyclerView
 */
class SavedRecipesListAdapter: RecyclerView.Adapter<SavedRecipesListAdapter.MyViewHolder>(){
    private var recipeList = emptyList<Recipe>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recipe_list_row, parent, false))
    }
    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentRecipe = recipeList[position]
        // Display the row info
        holder.itemView.findViewById<TextView>(R.id.list_name).text = currentRecipe.name.toString()
        val date = currentRecipe.dateAdded
        val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
        val formattedDate = dateFormat.format(date).uppercase(Locale.getDefault())
        holder.itemView.findViewById<TextView>(R.id.list_date_added).text = formattedDate

        holder.itemView.findViewById<ConstraintLayout>(R.id.recipeRowLayout).setOnClickListener {
            val action = SavedRecipesFragmentDirections.actionNavigationSavedRecipesToNavigationViewRecipe(currentRecipe)
            holder.itemView.findNavController().navigate(action)
        }
    }
    fun setData(recipe: List<Recipe>) {
        this.recipeList = recipe
        notifyDataSetChanged()
    }
}