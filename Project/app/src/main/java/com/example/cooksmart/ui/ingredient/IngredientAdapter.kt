package com.example.cooksmart.ui.ingredient

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import java.text.SimpleDateFormat
import java.util.Locale

class IngredientAdapter(var context: Context, var arrayList: ArrayList<IngredientCardViewFormat>) :
    RecyclerView.Adapter<IngredientAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val viewHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_list, parent, false)
        return ItemHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {

        val categoryItem = arrayList[position]

        holder.images.setImageResource(categoryItem.categoryImage ?: R.drawable.ic_launcher_foreground)
        holder.titles.text = categoryItem.categoryName

        holder.itemView.findViewById<CardView>(R.id.card_view).setOnClickListener {
            val action = IngredientFragmentDirections.actionNavigationIngredientToIngredientDisplay()
            holder.itemView.findNavController().navigate(action)
        }

    }
    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var images = itemView.findViewById<ImageView>(R.id.imageview)
        var titles = itemView.findViewById<TextView>(R.id.textName)

    }
}
