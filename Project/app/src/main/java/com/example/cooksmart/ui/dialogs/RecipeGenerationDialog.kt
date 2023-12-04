package com.example.cooksmart.ui.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.cooksmart.R

class RecipeGenerationDialog : DialogFragment() {
    companion object {
        const val TAG = "RecipeGenDialog"
    }
    // ProgressBar XML info from https://www.digitalocean.com/community/tutorials/android-progressbar-using-kotlin
    private lateinit var progressBar: ProgressBar
    private lateinit var progressPercent: TextView
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_fragment_recipe_generation, null)
        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(view)

        progressPercent = view.findViewById(R.id.dialogProgressPercentage)
        progressBar = view.findViewById(R.id.dialogProgressBar)

        return builder.create()
    }

    /**
     * Update the progress bar and value
     */
    fun updateProgress(progressDouble: Double) {
        if (this::progressBar.isInitialized && this::progressPercent.isInitialized) {
            progressBar.progress = progressDouble.toInt()
            // Format double to two decimals
            val formattedValue = String.format("%.1f", progressDouble)
            progressPercent.text = "$formattedValue %"
        }
    }
}