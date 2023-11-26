package com.example.cooksmart.ui.ingredient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.databinding.FragmentIngredientBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.appcompat.widget.SearchView
class IngredientFragment : Fragment() {

    private var _binding: FragmentIngredientBinding? = null
    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var adapter: IngredientListAdapter
    private lateinit var searchView: SearchView
    private lateinit var ingredientSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_ingredient,container,false)

        // Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)
                searchView = menu.findItem(R.id.search_bar)?.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (query != null) {
                            searchQuery(query)
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        if (newText != null) {
                            searchQuery(newText)
                        }
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // For handling other menu options (but we only have one)
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // RecyclerView
        adapter = IngredientListAdapter()
        val recyclerView = layout.findViewById<RecyclerView>(R.id.ingredients_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]
        ingredientViewModel.readAllIngredients.observe(viewLifecycleOwner) { ingredient ->
            adapter.setData(ingredient)
        }

        // Navigate to IngredientAdd when plus button is pressed
        layout.findViewById<FloatingActionButton>(R.id.ingredient_add).setOnClickListener{
            findNavController().navigate(R.id.action_navigation_ingredient_to_ingredient_add)
        }

        // Spinner
        val spinnerLists = resources.getStringArray(R.array.ingredientSpinner)
        ingredientSpinner = layout.findViewById<Spinner>(R.id.ingredient_sort)
        if (ingredientSpinner != null) {
            val sprAdapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item, spinnerLists
            )
            ingredientSpinner.adapter = sprAdapter

            ingredientSpinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View?, position: Int, id: Long
                ) {
                    println(
                        getString(R.string.selected_item) + " " +
                                "" + position + " " + spinnerLists[position]
                    )

                    // Remove previous observers
                    ingredientViewModel.readAllIngredients.removeObservers(viewLifecycleOwner)

                    if(position == 0) {
                        showAllIngredients()
                    }else if (position == 1){
                        showAddedDayNewest()
                    }else if(position == 2) {
                        showNameAlphabetically()
                    }else if (position == 3){
                        showBestDayOldest()
                    }else if (position == 4){
                        showBestDayNewest()
                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }

        return layout
    }
    private fun showAllIngredients() {
        ingredientViewModel.readAllIngredients.observe(viewLifecycleOwner) { ingredients ->
            adapter.setData(ingredients)
        }
    }
    private fun showNameAlphabetically() {
        ingredientViewModel.getIngredientSortedByCategory().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    private fun showBestDayOldest() {
        ingredientViewModel.showBestDayOldest().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }
    private fun showBestDayNewest() {
        ingredientViewModel.showBestDayNewest().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    private fun showAddedDayNewest() {
        ingredientViewModel.showAddedDayNewest().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun searchQuery(query: String) {
        // Search for the query with any surrounding letters
        val searchQuery = "%$query%"
        ingredientViewModel.searchIngredient(searchQuery).observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }
}