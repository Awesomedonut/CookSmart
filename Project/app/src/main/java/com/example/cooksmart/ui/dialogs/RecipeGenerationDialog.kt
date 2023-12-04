package com.example.cooksmart.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.cooksmart.R

class RecipeGenerationDialog : DialogFragment() {
    companion object {
        const val TAG = "RecipeGenDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_fragment_recipe_generation, null)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)
        return builder.create()
    }
}