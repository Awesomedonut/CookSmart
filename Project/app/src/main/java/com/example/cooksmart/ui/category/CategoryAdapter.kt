package com.example.cooksmart.ui.category

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import java.util.Collections

class CategoryAdapter(var context: Context, var arrayList: ArrayList<CategoryData>) :
    RecyclerView.Adapter<CategoryAdapter.ItemHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        val viewHolder = LayoutInflater.from(parent.context)
            .inflate(R.layout.category_list, parent, false)
        return ItemHolder(viewHolder)
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {

        val categoryItem: CategoryData = arrayList.get(position)

        holder.images.setImageResource(categoryItem.categoryImage ?: R.drawable.ic_launcher_foreground)
        holder.titles.text = categoryItem.categoryName

        holder.titles.setOnClickListener {
            Toast.makeText(context, categoryItem.categoryName, Toast.LENGTH_LONG).show()
        }

    }
    class ItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var images = itemView.findViewById<ImageView>(R.id.imageview)
        var titles = itemView.findViewById<TextView>(R.id.textName)

    }
}
