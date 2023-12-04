package com.example.cooksmart.ui.savedRecipes

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
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Shows the SavedRecipesFragment, with the RecyclerView of recipes
 */
class SavedRecipesFragment : Fragment() {
    private lateinit var savedRecipeViewModel: SavedRecipeViewModel
    private lateinit var adapter: SavedRecipesListAdapter
    private lateinit var searchView: SearchView
    private lateinit var recipeSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_saved_recipes, container, false)

        // Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.search_menu, menu)
                searchView = menu.findItem(R.id.search_bar)?.actionView as SearchView
                // Check if search button on menu is clicked
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    // Search for the query when submit is pressed
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        if (query != null) {
                            searchQuery(query)
                        }
                        return true
                    }
                    // Search for the query while typing
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
        adapter = SavedRecipesListAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recipe_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // SavedRecipeViewModel init
        savedRecipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

        // Saved Recipe sort spinner
        val spinnerLists = resources.getStringArray(R.array.recipeSpinner)
        recipeSpinner = view.findViewById(R.id.spinner)

            val sprAdapter = ArrayAdapter(
                requireActivity(),
                android.R.layout.simple_spinner_item, spinnerLists
            )
            recipeSpinner.adapter = sprAdapter

            recipeSpinner.onItemSelectedListener = object :
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
                    savedRecipeViewModel.readAllRecipes.removeObservers(viewLifecycleOwner)
                    savedRecipeViewModel.getAllFavoriteRecipes().removeObservers(viewLifecycleOwner)

                    // Perform different SQL queries to filter/sort the list
                    when (position) {
                        0 -> showAllRecipes()
                        1 -> showSavedRecipesSortedByDate()
                        2 -> showSavedRecipesSortedByName()
                        3 -> showAllFavoriteRecipes()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

        // Navigate to add recipe fragment for user to add their own recipe if they click the plus button
        view.findViewById<FloatingActionButton>(R.id.recipe_add).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_saved_recipes_to_addRecipe)
        }

        return view
    }

    /**
     * Searches the given string and sets the adapter to the matching recipe names
     */
    private fun searchQuery(query: String) {
        // Search for the query with any surrounding letters
        val searchQuery = "%$query%"
        savedRecipeViewModel.searchRecipe(searchQuery).observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /**
     * Show all the recipes in the adapter list
     */
    private fun showAllRecipes(){
        savedRecipeViewModel.readAllRecipes.observe(viewLifecycleOwner) { recipe ->
            adapter.setData(recipe)
        }
    }


    /**
     * Sort the recipes in the adapter list by name (A-Z)
     */
    private fun showSavedRecipesSortedByName() {
        savedRecipeViewModel.getRecipesSortedByName().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /**
     * Sort the recipes in the adapter list by date
     */
    private fun showSavedRecipesSortedByDate() {
        savedRecipeViewModel.getRecipesSortedByDate().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /**
     * Filter the recipes in the adapter list by if it's a favorite
     */
    private fun showAllFavoriteRecipes() {
        savedRecipeViewModel.getAllFavoriteRecipes().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }
}