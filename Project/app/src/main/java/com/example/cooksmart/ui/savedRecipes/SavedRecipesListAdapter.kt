/** "SavedRecipesListAdapter.kt"
 *  Description: Shows each row in the recipe database in a RecyclerView
 *  Last Modified: December 5, 2023
 * */

package com.example.cooksmart.ui.savedRecipes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe

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

        // If there's an available image, set the image in the row
        // Otherwise, image stays as the default
        if (currentRecipe.image.isNotEmpty()) {
            // Set the recipe image in the row
            Glide.with(holder.itemView /* context */)
                .load(currentRecipe.image)
                .override(80, 80) // replace with desired dimensions
                .into(holder.itemView.findViewById(R.id.responseImageRecipe))
        }

        val favoriteIcon = holder.itemView.findViewById<ImageView>(R.id.list_isFavorite)
        val borderFavIcon = holder.itemView.findViewById<ImageView>(R.id.list_isNotFavorite)

        if (currentRecipe.isFavorite) {
            // Show the favorite icon if it's favorited
            borderFavIcon.visibility = View.GONE
            favoriteIcon.visibility = View.VISIBLE
        } else {
            // Hide the favorite icon if not favorited
            favoriteIcon.visibility = View.GONE
            borderFavIcon.visibility = View.GONE
        }

        // Navigate to the respective recipe if recipe row clicked
        holder.itemView.findViewById<LinearLayout>(R.id.recipeRowLayout).setOnClickListener {
            val action = SavedRecipesFragmentDirections.actionNavigationSavedRecipesToNavigationViewRecipe(currentRecipe)
            holder.itemView.findNavController().navigate(action)
        }
    }

    /** "setData"
     *  Description: Set the recipeList with the given List<Recipe>
     */
    fun setData(recipe: List<Recipe>) {
        this.recipeList = recipe
        notifyDataSetChanged()
    }
}