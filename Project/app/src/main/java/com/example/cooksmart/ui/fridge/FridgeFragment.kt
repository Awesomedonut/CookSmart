package com.example.cooksmart.ui.fridge

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.R
import com.example.cooksmart.databinding.FragmentFridgeBinding
import com.example.cooksmart.ui.structs.CategoryType

class FridgeFragment : Fragment() {

    private var _binding: FragmentFridgeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val layout = inflater.inflate(R.layout.fragment_fridge,container,false)

        _binding = FragmentFridgeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        layout.findViewById<Button>(R.id.ingredient_add).setOnClickListener{
            Log.d("addIn","Add Ingerdient\n")
            val intent = Intent(activity,FridgeInsert::class.java)
            activity?.startActivity(intent)
            true
        }

        return layout
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}