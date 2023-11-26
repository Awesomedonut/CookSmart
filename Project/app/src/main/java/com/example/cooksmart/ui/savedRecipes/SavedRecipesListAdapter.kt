package com.example.cooksmart.ui.savedRecipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.utils.ConvertUtils

/**
 * Shows each row in the recipe database in a RecyclerView
 */
class SavedRecipesListAdapter: RecyclerView.Adapter<SavedRecipesListAdapter.MyViewHolder>(){
    private var recipeList = emptyList<Recipe>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recipe_list_row, parent, false))
    }
    override fun getItemCount(): Int {
        return recipeList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentRecipe = recipeList[position]
        // Display the row info
        holder.itemView.findViewById<TextView>(R.id.list_name).text = currentRecipe.name
//        val date = currentRecipe.dateAdded
//        val formattedDate = ConvertUtils.longToDateString(date)
//        holder.itemView.findViewById<TextView>(R.id.list_date_added).text = formattedDate

        var favoriteIcon = holder.itemView.findViewById<ImageView>(R.id.list_isFavorite)
        var borderFavIcon = holder.itemView.findViewById<ImageView>(R.id.list_isNotFavorite)

        if (currentRecipe.isFavorite) {
            // Show the favorite icon
            borderFavIcon.visibility = View.GONE
            favoriteIcon.visibility = View.VISIBLE
        } else {
            // Hide the favorite icon
            favoriteIcon.visibility = View.GONE
            borderFavIcon.visibility = View.VISIBLE
        }

        holder.itemView.findViewById<LinearLayout>(R.id.recipeRowLayout).setOnClickListener {
            val action = SavedRecipesFragmentDirections.actionNavigationSavedRecipesToNavigationViewRecipe(currentRecipe)
            holder.itemView.findNavController().navigate(action)
        }
    }
    fun setData(recipe: List<Recipe>) {
        this.recipeList = recipe
        notifyDataSetChanged()
    }
}