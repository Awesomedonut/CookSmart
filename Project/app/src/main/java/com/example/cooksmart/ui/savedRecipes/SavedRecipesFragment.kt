/** "SavedRecipesFragment.kt"
 *  Description: Shows the SavedRecipesFragment, with the RecyclerView of recipes
 *               with search and delete all functionality
 *  Last Modified: December 5, 2023
 * */
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
                when (menuItem.itemId) {
                    // Perform camera operations
                    R.id.delete_menu -> {
                        deleteAllRecipes()
                    }
                }
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

    /** "searchQuery"
     *  Description: Searches the given string and sets the adapter to the matching recipe names
     *  @param query String to search the database for
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

    /** "showAllRecipes"
     *  Description: Show all the recipes in the adapter list
     */
    private fun showAllRecipes(){
        savedRecipeViewModel.readAllRecipes.observe(viewLifecycleOwner) { recipe ->
            adapter.setData(recipe)
        }
    }

    /** "showSavedRecipesSortedByName"
     *  Description: Sort the recipes in the adapter list by name (A-Z)
     */
    private fun showSavedRecipesSortedByName() {
        savedRecipeViewModel.getRecipesSortedByName().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /** "showSavedRecipesSortedByDate"
     *  Description: Sort the recipes in the adapter list by date
     */
    private fun showSavedRecipesSortedByDate() {
        savedRecipeViewModel.getRecipesSortedByDate().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /** "showAllFavoriteRecipes"
     *  Description: Filter the recipes in the adapter list by if it's a favorite
     */
    private fun showAllFavoriteRecipes() {
        savedRecipeViewModel.getAllFavoriteRecipes().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /** "deleteAllRecipes"
     *  Description: Deletes all recipe from the data base if user clicks yes on dialog
     */
    private fun deleteAllRecipes() {
        // Show alert dialog to confirm deletion or not
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            savedRecipeViewModel.deleteAllRecipes()
            Toast.makeText(requireContext(), "All recipes have been removed!", Toast.LENGTH_SHORT).show()
        }
        // Don't delete if they say no
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete all recipes?")
        builder.setMessage("Are you sure you want to remove all of your saved recipes?")
        builder.create().show()
    }
}