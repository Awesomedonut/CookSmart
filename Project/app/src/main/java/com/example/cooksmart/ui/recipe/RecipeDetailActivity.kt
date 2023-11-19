package com.example.cooksmart.ui.recipe
// RecipeDetailFragment

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.cooksmart.R


class RecipeDetailActivity : AppCompatActivity() {

    private lateinit var instructions: TextView
    private lateinit var ingredients: TextView
    private lateinit var title: TextView
    private lateinit var viewModel: RecipeDetailViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        instructions = findViewById(R.id.instructionsText)
        ingredients = findViewById(R.id.ingredientsText)
        title = findViewById(R.id.recipeName)

        val recipeId = intent.getLongExtra("recipeID", -1)
        val recipeName = intent.getStringExtra("recipeName")
        val recipeIngredients = intent.getStringExtra("recipeIngredients")
        val recipeInstructions = intent.getStringExtra("recipeInstructions")


        if (recipeId.toInt() != -1) {
            viewModel = ViewModelProvider(this).get(RecipeDetailViewModel::class.java)

            // Observe the LiveData for the selected recipe
            viewModel.getRecipeById(recipeId).observe(this) { recipe ->
                if (recipe != null) {
                    // Update UI with recipe details
                    title.text = recipeName
                    instructions.text = recipeIngredients
                    ingredients.text = recipeInstructions
                } else {
                    // Handle error, recipe not found
                    Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity if the recipe is not found
                }
            }
        } else {
            // Handle error, invalid recipeId
            Toast.makeText(this, "Invalid Recipe ID", Toast.LENGTH_SHORT).show()
            finish() // Close the activity if the recipe ID is invalid
        }

    }
}
