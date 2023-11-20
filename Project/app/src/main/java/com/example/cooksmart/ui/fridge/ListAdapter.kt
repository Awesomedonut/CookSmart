package com.example.cooksmart.ui.fridge

import android.database.DataSetObserver
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import java.text.SimpleDateFormat
import java.util.Locale

class ListAdapter: RecyclerView.Adapter<ListAdapter.MyViewHolder>(){
    private var ingredientList = emptyList<Ingredient>()

    class MyViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.ingredient_list_row, parent, false))
    }

    override fun getItemCount(): Int {
        return ingredientList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentIngredient = ingredientList[position]
        // Display the row info
        holder.itemView.findViewById<TextView>(R.id.list_category).text = currentIngredient.category.toString()
        holder.itemView.findViewById<TextView>(R.id.list_name).text = currentIngredient.name.toString()
        holder.itemView.findViewById<TextView>(R.id.list_quantity).text = currentIngredient.quantity.toString()
        val date = currentIngredient.bestBefore
        val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
        val formattedDate = dateFormat.format(date).uppercase(Locale.getDefault())
        holder.itemView.findViewById<TextView>(R.id.list_best_before).text = formattedDate

        holder.itemView.findViewById<ConstraintLayout>(R.id.rowLayout).setOnClickListener {
            val action = FridgeFragmentDirections.actionNavigationFridgeToIngredientUpdate(currentIngredient)
            holder.itemView.findNavController().navigate(action)
        }
    }

    fun setData(ingredient: List<Ingredient>) {
        this.ingredientList = ingredient
        notifyDataSetChanged()
    }
}