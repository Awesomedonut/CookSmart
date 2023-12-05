/** "IngredientListAdapter"
 *  Description: A RecyclerView adapter class to display an ingredient list
 *  Last Modified: November 27, 2023
 * */
package com.example.cooksmart.ui.ingredient

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.utils.ConvertUtils

class IngredientListAdapter: RecyclerView.Adapter<IngredientListAdapter.MyViewHolder>(){
    // Declare class variables
    private var ingredientList = emptyList<Ingredient>()
    private val checkedIngredients = mutableListOf<Ingredient>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.ingredient_list_row, parent, false))
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentIngredient = ingredientList[position]
        // Display the row info
        holder.itemView.findViewById<TextView>(R.id.list_category).text = currentIngredient.category
        holder.itemView.findViewById<TextView>(R.id.list_name).text = currentIngredient.name
        holder.itemView.findViewById<TextView>(R.id.list_quantity).text = currentIngredient.quantity + " " + currentIngredient.quantityType
        val date = currentIngredient.bestBefore
        val formattedDate = ConvertUtils.longToDateString(date)
        holder.itemView.findViewById<TextView>(R.id.list_best_before).text = formattedDate

        // Check for clicks on the ingredient and navigate to ingredient update view
        holder.itemView.findViewById<LinearLayout>(R.id.rowLayout).setOnClickListener {
            val action = IngredientFragmentDirections.actionNavigationIngredientToIngredientUpdate(currentIngredient)
            holder.itemView.findNavController().navigate(action)
        }

        // Check if a box is checked
        holder.itemView.findViewById<CheckBox>(R.id.list_checkbox).apply {
            setOnClickListener {
                val isChecked = isChecked
                val currentIngredient = ingredientList[position]
                if (isChecked) {
                    checkedIngredients.add(currentIngredient)
                } else {
                    checkedIngredients.remove(currentIngredient)
                }
            }
        }
    }

    /** "setData"
     *  Description: Setts ingredient list data
     * */
    fun setData(ingredient: List<Ingredient>) {
        this.ingredientList = ingredient
        notifyDataSetChanged()
    }

    /** "getCheckedIngredients"
     *  Description: Returns ingredients that are checkmarked
     * */
    fun getCheckedIngredients(): List<Ingredient> {
        return checkedIngredients
    }
}