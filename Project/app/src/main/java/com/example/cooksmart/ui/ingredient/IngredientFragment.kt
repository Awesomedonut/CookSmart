/** "IngredientFragment.kt"
 *  Description: Fragment for the ingredient class. Displays the ingredients
 *               currently added to the application, allows users to search
 *               and sort through them, and allows users to select ingredients
 *               for recipe generation
 *  Last Modified: December 1, 2023
 *
 * */
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
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
                // Set up searching UI components
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
                when (menuItem.itemId) {
                    // Perform camera operations
                    // Set up delete all ingredients button
                    R.id.delete_menu -> {
                        deleteAllIngredients()
                    }
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // RecyclerView and unique adapter view
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

        // Navigate to generate recipes fragment with the checked ingredients
        layout.findViewById<Button>(R.id.generate_recipes_button).setOnClickListener {
            val checkedIngredients = adapter.getCheckedIngredients()
            if (checkedIngredients.isEmpty()) {
                Toast.makeText(context, "No ingredients chosen!\nPlease select some ingredients!", Toast.LENGTH_SHORT).show()
            } else {
                val action = IngredientFragmentDirections.actionNavigationIngredientToFragmentIngredientGeneratedRecipe(checkedIngredients.toTypedArray())
                findNavController().navigate(action)
            }
        }

        // Spinner initialization
        val spinnerLists = resources.getStringArray(R.array.ingredientSpinner)
        ingredientSpinner = layout.findViewById(R.id.ingredient_sort)

        val sprAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item, spinnerLists
        )
        ingredientSpinner.adapter = sprAdapter

        // Set-up Ingredient list sorting
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

                when (position) {
                    0 -> showAllIngredients()
                    1 -> showAddedDayNewest()
                    2 -> showNameAlphabetically()
                    3 -> showBestDayOldest()
                    4 -> showBestDayNewest()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do not perform any operations if nothing is selected
            }
        }


        return layout
    }

    /** "showAllIngredients"
     *  Description: Retrieves all ingredients from the database
     * */
    private fun showAllIngredients() {
        ingredientViewModel.readAllIngredients.observe(viewLifecycleOwner) { ingredients ->
            adapter.setData(ingredients)
        }
    }

    /** "showNameAlphabetically"
     *  Description: Retrieves an alphabetically sorted list of ingredients
     * */
    private fun showNameAlphabetically() {
        ingredientViewModel.getIngredientSortedByAlphabet().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /** "showBestDayOldest"
     *  Description: Retrieves a list of ingredients, sorted by their best before date descending
     * */
    private fun showBestDayOldest() {
        ingredientViewModel.showBestDayOldest().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /** "showBestDayNewest"
     *  Description: Retrieves a list of ingredients, sorted by their best before date ascending
     */
    private fun showBestDayNewest() {
        ingredientViewModel.showBestDayNewest().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /** "showBestDayNewest"
     *  Description: Retrieves a list of ingredients, sorted by insertion date
     */
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

    /** "searchQuery"
     *  Description: Uses an input string to search for an ingredient with that
     *               string in the database
     * */

    private fun searchQuery(query: String) {
        // Search for the query with any surrounding letters
        val searchQuery = "%$query%"
        ingredientViewModel.searchIngredient(searchQuery).observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    /** "deleteAllIngredients"
     *  Description: Confirms the users would like to delete all ingredients, then
     *               performs the database operation if yes
     * */
    private fun deleteAllIngredients() {
        // Show alert dialog to confirm deletion or not
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            ingredientViewModel.deleteAllIngredients()
            Toast.makeText(requireContext(), "All ingredients have been removed!", Toast.LENGTH_SHORT).show()
        }
        // Don't delete if they say no
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete all ingredients?")
        builder.setMessage("Are you sure you want to remove all of your saved ingredients?")
        builder.create().show()
    }
}