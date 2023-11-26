package com.example.cooksmart.ui.savedRecipes

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import com.example.cooksmart.R

/**
 * Used when ingredients are added to the add recipes fragment
 * Updates the listview so users can see and edit previously added ingredients when
 * they're adding a custom recipe
 */
class RecipeIngredientAdapter(context: Context, private val ingredientsList: ArrayList<String>):
    ArrayAdapter<String>(context, R.layout.ingredient_listview_item, ingredientsList) {

    private var onDeleteClickListener: ((Int) -> Unit)? = null
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val row = inflater.inflate(R.layout.ingredient_listview_item, parent, false)

        val rowEditText = row.findViewById<EditText>(R.id.editTextIngredient)
        val deleteButton = row.findViewById<Button>(R.id.deleteIngredient)
        rowEditText.setText(ingredientsList[position])

        // Listen for delete button click for each row
        deleteButton.setOnClickListener {
            onDeleteClickListener?.invoke(position)
        }

        return row
    }

    /**
     * After every additional ingredient, loop through all the filled in ingredients
     * and update the ingredientsList in case they were changed
     */
    fun updateIngredients(listView: ListView) {
        for (i in 0 until listView.childCount) {
            val rowView = listView.getChildAt(i)
            val editText = rowView.findViewById<EditText>(R.id.editTextIngredient)
            ingredientsList[i] = editText.text.toString()
        }
        notifyDataSetChanged()
    }

    fun setOnDeleteClickListener(listener: (Int) -> Unit) {
        onDeleteClickListener = listener
    }
}