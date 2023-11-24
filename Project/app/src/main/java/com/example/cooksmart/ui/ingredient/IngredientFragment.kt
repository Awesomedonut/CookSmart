package com.example.cooksmart.ui.ingredient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.databinding.FragmentIngredientBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class IngredientFragment : Fragment() {

    private var _binding: FragmentIngredientBinding? = null
    private lateinit var ingredientViewModel: IngredientViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_ingredient,container,false)

        // RecyclerView
        val adapter = IngredientListAdapter()
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
        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}