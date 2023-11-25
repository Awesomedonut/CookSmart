package com.example.cooksmart.ui.ingredient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.databinding.FragmentIngredientBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton


class IngredientFragment : Fragment() {

    private var _binding: FragmentIngredientBinding? = null

    private var recyclerView: RecyclerView? = null
    private var arrayList: ArrayList<IngredientCardViewFormat> = ArrayList()
    private var gridLayoutManager: GridLayoutManager? = null
    private var ingredientAdapter: IngredientAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_ingredient,container,false)


        recyclerView = layout.findViewById(R.id.ingredients_list)
        gridLayoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
        recyclerView?.layoutManager = gridLayoutManager
        recyclerView?.setHasFixedSize(true)

        arrayList = ArrayList(
            listOf(
                IngredientCardViewFormat("Meat/Seafood", R.drawable.meat_seafood),
                IngredientCardViewFormat("Produce", R.drawable.produce),
                IngredientCardViewFormat("Dairy/Cheese/Eggs", R.drawable.dairy_chees_eggs),
                IngredientCardViewFormat("Bakery", R.drawable.bakery),
                IngredientCardViewFormat("Deli", R.drawable.deli),
                IngredientCardViewFormat("Nuts/Seeds/Dried Fruit", R.drawable.nuts_seeds_dried_fruit),
                IngredientCardViewFormat("Butter/Honey/Jam", R.drawable.butter_honey_jam),
                IngredientCardViewFormat("Baking/Spices", R.drawable.baking_spices),
                IngredientCardViewFormat("Beverages", R.drawable.beverages),
                IngredientCardViewFormat("Coffee/Tea", R.drawable.coffee_tea)
            )
        )

        ingredientAdapter = IngredientAdapter(requireContext(), arrayList)
        recyclerView?.adapter = ingredientAdapter


        // Navigate to IngredientAdd when plus button is pressed
        layout.findViewById<FloatingActionButton>(R.id.ingredient_add).setOnClickListener{
            findNavController().navigate(R.id.action_navigation_ingredient_to_ingredient_add)
        }

        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    private fun searchQuery(query: String) {
//        // Search for the query with any surrounding letters
//        val searchQuery = "%$query%"
//        ingredientViewModel.searchIngredient(searchQuery).observe(viewLifecycleOwner) { list ->
//            list?.let {
//                adapter.setData(it)
//            }
//        }
//    }
}

// Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
//        val menuHost = requireActivity() as MenuHost
//        menuHost.addMenuProvider(object : MenuProvider {
//            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
//                menuInflater.inflate(R.menu.options_menu, menu)
//                searchView = menu.findItem(R.id.search_bar)?.actionView as SearchView
//                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//                    override fun onQueryTextSubmit(query: String?): Boolean {
//                        if (query != null) {
//                            searchQuery(query)
//                        }
//                        return true
//                    }
//
//                    override fun onQueryTextChange(newText: String?): Boolean {
//                        if (newText != null) {
//                            searchQuery(newText)
//                        }
//                        return true
//                    }
//                })
//            }
//
//            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//                // For handling other menu options (but we only have one)
//                return true
//            }
//        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

// RecyclerView
//        adapter = IngredientListAdapter()
//        val recyclerView = layout.findViewById<RecyclerView>(R.id.ingredients_list)
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]
//        ingredientViewModel.readAllIngredients.observe(viewLifecycleOwner) { ingredient ->
//            adapter.setData(ingredient)
//        }
