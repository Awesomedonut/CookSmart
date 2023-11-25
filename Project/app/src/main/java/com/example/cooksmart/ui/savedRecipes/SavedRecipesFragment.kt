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
        adapter = SavedRecipesListAdapter()
        val recyclerView = view.findViewById<RecyclerView>(R.id.recipe_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        savedRecipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

//        savedRecipeViewModel.readAllRecipes.observe(viewLifecycleOwner) { recipe ->
//            adapter.setData(recipe)
//        }

        // Spinner
        val spinnerLists = resources.getStringArray(R.array.recipeSpinner)
        recipeSpinner = view.findViewById<Spinner>(R.id.spinner)
        if (recipeSpinner != null) {
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

                    if(position == 0) {
                        showAllRecipes()
                    }else if(position == 1) {
                        showSavedRecipesSortedByName()
                    }else if (position == 2){
                        showSavedRecipesSortedByDate()
                    }else if (position == 3){
                        showAllFavoriteRecipes()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        }



        // Navigate to add recipe fragment for user to add their own recipe
        view.findViewById<FloatingActionButton>(R.id.recipe_add).setOnClickListener {
            findNavController().navigate(R.id.action_navigation_saved_recipes_to_addRecipe)
        }

        return view
    }

    private fun searchQuery(query: String) {
        // Search for the query with any surrounding letters
        val searchQuery = "%$query%"
        savedRecipeViewModel.searchRecipe(searchQuery).observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    private fun showAllRecipes(){
        savedRecipeViewModel.readAllRecipes.observe(viewLifecycleOwner) { recipe ->
            adapter.setData(recipe)
        }
    }
    private fun showSavedRecipesSortedByName() {
        savedRecipeViewModel.getRecipesSortedByName().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    private fun showSavedRecipesSortedByDate() {
        savedRecipeViewModel.getRecipesSortedByDate().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

    private fun showAllFavoriteRecipes() {
        savedRecipeViewModel.getAllFavoriteRecipes().observe(viewLifecycleOwner) { list ->
            list?.let {
                adapter.setData(it)
            }
        }
    }

}