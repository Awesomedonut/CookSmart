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

        holder.images.setImageResource(categoryItem.categoryImage!!)
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

//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import android.widget.Toast
//import androidx.recyclerview.widget.RecyclerView
//import com.example.cooksmart.R
//
//class CategoryAdapter(private val myCategoryData: Array<CategoryData>, private val context: Context?) :
//    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val layoutInflater = LayoutInflater.from(parent.context)
//        val view: View = layoutInflater.inflate(R.layout.category_list, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val myCategoryDataList: CategoryData = myCategoryData[position]
//        holder.textViewName.text = myCategoryDataList.getCategoryName()
//        myCategoryDataList.getCategoryImage()?.let { holder.movieImage.setImageResource(it) }
//        holder.itemView.setOnClickListener {
//            Toast.makeText(
//                context,
//                myCategoryDataList.getCategoryName(),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }
//
//    override fun getItemCount(): Int {
//        return myCategoryData.size
//    }
//
//    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        var movieImage: ImageView = itemView.findViewById(R.id.imageview)
//        var textViewName: TextView = itemView.findViewById(R.id.textName)
//    }
//}