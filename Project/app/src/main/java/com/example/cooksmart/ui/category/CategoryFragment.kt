package com.example.cooksmart.ui.category

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R

class CategoryFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private var arrayList: ArrayList<CategoryData> = ArrayList()
    private var gridLayoutManager: GridLayoutManager? = null
    private var categoryAdapter: CategoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragement_category, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        gridLayoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.setHasFixedSize(true)

        arrayList = ArrayList(
            listOf(
                CategoryData("Meat/Seafood", R.drawable.meat_seafood),
                CategoryData("Produce", R.drawable.produce),
                CategoryData("Dairy/Cheese/Eggs", R.drawable.dairy_chees_eggs),
                CategoryData("Bakery", R.drawable.bakery),
                CategoryData("Deli", R.drawable.deli),
                CategoryData("Nuts/Seeds/Dried Fruit", R.drawable.nuts_seeds_dried_fruit),
                CategoryData("Butter/Honey/Jam", R.drawable.butter_honey_jam),
                CategoryData("Baking/Spices", R.drawable.baking_spices),
                CategoryData("Beverages", R.drawable.beverages),
                CategoryData("Coffee/Tea", R.drawable.coffee_tea)
            )
        )

        //println("size"+arrayList.size)

        categoryAdapter = CategoryAdapter(requireContext(), arrayList)
        recyclerView?.adapter = categoryAdapter


        return view
    }

}