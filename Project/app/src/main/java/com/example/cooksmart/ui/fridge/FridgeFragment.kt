package com.example.cooksmart.ui.fridge

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cooksmart.R
import com.example.cooksmart.database.IngredientViewModel
import com.example.cooksmart.databinding.FragmentFridgeBinding

class FridgeFragment : Fragment() {

    private var _binding: FragmentFridgeBinding? = null
    private lateinit var ingredientViewModel: IngredientViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_fridge,container,false)

        // RecyclerView
        val adapter = ListAdapter()
        val recyclerView = layout.findViewById<RecyclerView>(R.id.ingredients_list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]
        ingredientViewModel.readAllIngredients.observe(viewLifecycleOwner) { ingredient ->
            adapter.setData(ingredient)
        }

        // Navigate to FridgeInsert when plus button is pressed
        layout.findViewById<Button>(R.id.ingredient_add).setOnClickListener{
            findNavController().navigate(R.id.action_navigation_fridge_to_fridgeInsert2)
        }
        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}