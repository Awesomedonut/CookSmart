package com.example.cooksmart.ui.savedRecipes

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.cooksmart.Constants
import com.example.cooksmart.R
import com.example.cooksmart.database.Recipe
import com.example.cooksmart.databinding.FragmentAddRecipeBinding
import com.example.cooksmart.databinding.FragmentRecipeBinding
import com.example.cooksmart.ui.base.RecipeBaseViewModel
import com.example.cooksmart.utils.CameraHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddRecipe : Fragment() {
    private lateinit var savedRecipeViewModel: SavedRecipeViewModel
    private lateinit var view: View
    private lateinit var ingredientEditText: EditText
    private lateinit var ingredientAddButton: Button
    private lateinit var ingredientListView: ListView
    private lateinit var adapter: RecipeIngredientAdapter
    private val ingredientsList = ArrayList<String>()
    private lateinit var recipeImageView:ImageView
    private lateinit var changeBtn : Button
    private lateinit var confirmButton: Button
    private lateinit var favIcon : MenuItem
    private var isFavorite : Boolean = false
    private val IMAGE_PICK_REQUEST_CODE = 1
    private val CAMERA_CAPTURE_REQUEST_CODE = 2
    private var selectedImage: String? = null

    private var _binding: FragmentAddRecipeBinding? = null
    private val binding get() = _binding!!
    private lateinit var recipeImgUri: Uri
    private val tempImgFileName = "xd_temp_img.jpg"
    protected val cameraHandler = CameraHandler(this)
    protected lateinit var recipebaseViewModel: RecipeBaseViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_add_recipe, container, false)
        confirmButton = view.findViewById(R.id.button_confirm)

        ingredientEditText = view.findViewById(R.id.recipe_ingredients_edittext)
        ingredientAddButton = view.findViewById(R.id.add_ingredient_recipe)
        ingredientListView = view.findViewById(R.id.recipe_ingredients_listview)

        recipeImageView = view.findViewById(R.id.recipeImage)
        changeBtn = view.findViewById(R.id.change_button)
        changeBtn.setOnClickListener(){
            showImageSourceDialog()
        }


        // Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.add_favorite_menu, menu)
                favIcon = menu.findItem(R.id.fav_menu)

                if (isFavorite) {
                    favIcon.setIcon(R.drawable.favorite_icon)
                } else {
                    favIcon.setIcon(R.drawable.favorite_icon_border)
                }
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    // Navigate to update page with the most update version of the current recipe as an argument
                    R.id.fav_menu -> {
                        isFavorite = !isFavorite
                        activity?.invalidateOptionsMenu() // To redraw the menu and call onCreateMenu
                    }

                    // Go back to previous page if user clicks back button on menu toolbar
                    android.R.id.home -> findNavController().navigate(R.id.action_addRecipe_to_navigation_saved_recipes)
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        // Display each ingredient in ingredientsList in a ListView row
        adapter = RecipeIngredientAdapter(requireContext(), ingredientsList)
        ingredientListView.adapter = adapter

        savedRecipeViewModel = ViewModelProvider(this)[SavedRecipeViewModel::class.java]

        // Update the new ingredient the user added from the add ingredient button to the ingredientsList
        ingredientAddButton.setOnClickListener {
            val newIngredient = ingredientEditText.text.toString()
            if (newIngredient.isNotEmpty()) {
                adapter.updateIngredients(ingredientListView)
                ingredientsList.add(newIngredient)
                adapter.notifyDataSetChanged()
                ingredientEditText.text.clear()
            }
        }
        // Add the recipe using all the user input when they press add ingredient button
        confirmButton.setOnClickListener {
            insertRecipe()
        }

        // Delete ingredient row if delete button is clicked
        adapter.setOnDeleteClickListener {
            ingredientsList.removeAt(it)
            adapter.notifyDataSetChanged()
        }


        return view
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Select from Gallery", "Take a Photo")
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Choose an option")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openImagePicker()
                    1 -> openCamera()
                }
            }
        builder.create().show()
    }
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST_CODE)
    }

    private fun openCamera() {
        println("open camera!!")
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(requireActivity().packageManager) != null) {
            // Create a file to save the camera-captured image
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                // Handle the error
                null
            }

            // Continue only if the file was successfully created
            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "com.example.cooksmart.fileprovider",
                    it
                )
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, CAMERA_CAPTURE_REQUEST_CODE)
            }
        }
    }

    private fun setIngreImgUri() {
        val tempImgFile = File(requireContext().getExternalFilesDir(null),
            Constants.INGRE_IMG_FILE_NAME
        )
        recipeImgUri = FileProvider.getUriForFile(requireContext(),
            Constants.PACKAGE_NAME, tempImgFile)
    }
    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        )
    }

    private fun insertRecipe() {
        val title = view.findViewById<EditText>(R.id.title_recipe).text.toString()
        val ingredients = ingredientsList.toString()
        val instructions = view.findViewById<EditText>(R.id.recipe_instructions).text.toString()
        val currentDate = System.currentTimeMillis()
        // Check all fields have input and then save into database as Recipe entity
        if (!isNotValidInput(title, ingredientsList, instructions)) {
            val recipe = Recipe(0, title, ingredients, instructions, currentDate, isFavorite)
            savedRecipeViewModel.insertRecipe(recipe)
            Toast.makeText(requireContext(), "Recipe added!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_addRecipe_to_navigation_saved_recipes)
        } else {
            Toast.makeText(requireContext(), "Please fill all the fields!", Toast.LENGTH_SHORT).show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                IMAGE_PICK_REQUEST_CODE -> handleGalleryImage(data)
                CAMERA_CAPTURE_REQUEST_CODE -> handleCameraImage(data)
            }
        }
    }
    private fun handleGalleryImage(data: Intent?) {
        val selectedImageUri: Uri = data?.data ?: return
        selectedImage = getRealPathFromURI(selectedImageUri)
        loadImage(selectedImage)
    }
    private fun getRealPathFromURI(uri: Uri): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity?.contentResolver?.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            it.moveToFirst()
            return it.getString(columnIndex)
        }
        return ""
    }

    private fun handleCameraImage(data: Intent?) {
        val photo: Bitmap = data?.extras?.get("data") as Bitmap
        // Save the photo to a file or convert to a string as needed
        // For simplicity, we'll use a temporary file path here
        selectedImage = saveImageToFile(photo)
        loadImage(selectedImage)
    }

    private fun saveImageToFile(bitmap: Bitmap): String {
        val file = File(requireContext().externalCacheDir, "temp_image.jpg")
        val fileOutputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()
        return file.absolutePath
    }

    private fun loadImage(imagePath: String?) {
        imagePath?.let {
            // Load and display the selected image using a library like Glide or Picasso
            Glide.with(this).load(imagePath).into(recipeImageView)
        }
    }

    /**
     * Checks if name or quantity fields are empty
     */
    private fun isNotValidInput(title: String, ingredients: ArrayList<String>, instructions: String): Boolean {
        // Returns true if fields are empty
        return (title == "" || ingredients.isEmpty() || instructions == "")
    }
}