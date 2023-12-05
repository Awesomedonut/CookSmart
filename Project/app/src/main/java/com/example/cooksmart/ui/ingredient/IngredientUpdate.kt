/** "IngredientUpdate.kt"
 *  Description: Allows users to update the data fields of an existing
 *               Ingredient entity in the database
 *  Last Modified: December 4, 2023
 * */
package com.example.cooksmart.ui.ingredient

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.infra.services.NotificationWorker
import java.util.Locale
import com.example.cooksmart.ui.structs.CategoryType
import com.example.cooksmart.ui.structs.QuantityType
import com.example.cooksmart.utils.ConvertUtils
import com.example.cooksmart.utils.PermissionCheck
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.UUID
import java.util.concurrent.TimeUnit

class IngredientUpdate : Fragment() {
    // Declare lateinit variables and fragment variables
    private lateinit var categoriesSpinner: Spinner
    private lateinit var categoriesAdapter: SpinnerAdapter
    private lateinit var quantityTypeSpinner : Spinner
    private lateinit var quantityTypeAdapter : SpinnerAdapter
    private lateinit var view: View
    private lateinit var selectedDate: Calendar
    private lateinit var ingredientViewModel: IngredientViewModel
    private val args by navArgs<IngredientUpdateArgs>()
    private lateinit var notifyIcon : MenuItem
    private var isNotified = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_ingredient_update, container, false)

        // Locate UI components
        val confirmButton = view.findViewById<Button>(R.id.update_button_confirm)
        val editDate = view.findViewById<Button>(R.id.update_best_before_date_picker)

        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]
        // Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.delete_menu, menu)
                // Set the notification icon to the correct state
                notifyIcon = menu.findItem(R.id.notification_menu)

                // Check that a user has notifications enabled, if they want notifications
                val notifyId = args.currentIngredient.notifId
                if(notifyId != null){
                    isNotified = true
                }

                if (isNotified) {
                    notifyIcon.setIcon(R.drawable.baseline_notifications_active_24)
                } else {
                    notifyIcon.setIcon(R.drawable.baseline_notifications_none_24)
                }
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.notification_menu -> {
                        // Inverse the current state of the isNotified boolean
                        isNotified = !isNotified
                        // Check that a user gave notification permission if they want notifications
                        if(isNotified && !PermissionCheck.checkNotificationPermission(requireActivity())){
                            Toast.makeText(requireContext(), "Please give CookSmart notification permissions to use this feature!", Toast.LENGTH_SHORT).show()
                            isNotified = false
                        }
                        // Set the respective bell icon (filled/unfilled)
                        if (isNotified) {
                            notifyIcon.setIcon(R.drawable.baseline_notifications_active_24)
                        } else {
                            notifyIcon.setIcon(R.drawable.baseline_notifications_none_24)
                        }
                    }

                    // Delete dialog if user clicks delete button on menu toolbar
                    R.id.delete_menu -> deleteIngredient()
                    // Go back to previous page if user clicks back button on menu toolbar
                    android.R.id.home -> findNavController().navigateUp()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        // Setup spinner
        // Assuming your Spinner is defined in fridge_insert layout
        categoriesSpinner = view.findViewById(R.id.update_category)

        // Set up the Spinner adapter
        categoriesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            CategoryType.values().map { it.asString }
        )
        categoriesSpinner.adapter = categoriesAdapter

        // Set up Quantity spinner
        quantityTypeSpinner = view.findViewById(R.id.update_quantityType)
        quantityTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            QuantityType.values().map { it.asString}
        )
        quantityTypeSpinner.adapter = quantityTypeAdapter


        // Get the previously set date and set to calendar object
        val currentBestBefore = args.currentIngredient.bestBefore
        selectedDate = Calendar.getInstance()
        selectedDate.timeInMillis = currentBestBefore
        // Set date picker to the button
        editDate.setOnClickListener {
            datePickerDialog()
        }

        confirmButton.setOnClickListener {
            updateIngredient()
        }

        // Populate fields with the saved values from args
        val position = categoryStringToInt(args.currentIngredient.category)
        view.findViewById<Spinner>(R.id.update_category).setSelection(position)
        view.findViewById<EditText>(R.id.update_name_ingredient).setText(args.currentIngredient.name)
        view.findViewById<EditText>(R.id.update_quantity).setText(args.currentIngredient.quantity)
        val qPosition = quantityTypeStringToInt(args.currentIngredient.quantityType)
        view.findViewById<Spinner>(R.id.update_quantityType).setSelection(qPosition)
        val date = args.currentIngredient.bestBefore
        val formattedDate = ConvertUtils.longToDateString(date)
        view.findViewById<TextView>(R.id.update_date_input_current).text = formattedDate

        return view
    }

    /**
     * Updates the selected ingredient with the filled out fields
     */
    private fun updateIngredient() {
        val category = view.findViewById<Spinner>(R.id.update_category).selectedItem.toString()
        val name = view.findViewById<EditText>(R.id.update_name_ingredient).text.toString()
        val quantity = view.findViewById<EditText>(R.id.update_quantity).text.toString()
        val quantityType = view.findViewById<Spinner>(R.id.update_quantityType).selectedItem.toString()
        val currentDate = args.currentIngredient.dateAdded
        val bestBefore = selectedDate.timeInMillis
        var notifID : UUID? = args.currentIngredient.notifId
        // Schedule a notification for the day before expected expiry
        if(isNotified) {
            var daysToExpiry = daysExpiry(bestBefore, currentDate) - 1
            if (daysToExpiry < 0) {
                daysToExpiry = 0
            }
            // Built from: https://developer.android.com/guide/background/persistent/getting-started/define-work
            val notificationWorkReq = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(daysToExpiry, TimeUnit.DAYS)
                .build()
            // If a notification exists,
            // cancel the previous notification and queue a new one, replacing
            // the field in the ingredient
            if (notifID != null) {
                WorkManager.getInstance(requireContext()).cancelWorkById(notifID)
            }
            WorkManager.getInstance(requireContext()).enqueue(notificationWorkReq)
            notifID = notificationWorkReq.id
        }
        // If the user no longer wants a notification, cancel the notification
        else {
            if (notifID != null) {
                WorkManager.getInstance(requireContext()).cancelWorkById(notifID)
                notifID = null
            }
        }
        // Checks if the fields are filled, if not, don't do anything, otherwise, update the ingredient in the database
        if (!isNotValidInput(name, quantity)) {
            val updatedIngredient = Ingredient(args.currentIngredient.id, name, category, quantity, quantityType, currentDate, bestBefore, notifID)
            ingredientViewModel.updateIngredient(updatedIngredient)
            Toast.makeText(requireContext(), "Ingredient updated!", Toast.LENGTH_SHORT).show()
            findNavController().navigateUp()
        } else {
            Toast.makeText(requireContext(), "Please fill all the fields!", Toast.LENGTH_SHORT).show()
        }
    }

    /** "isNotValidInput"
     * Description: Checks if name or quantity fields are empty
     */
    private fun isNotValidInput(name: String, quantity: String): Boolean {
        // Returns true if fields are empty
        return (name == "" || quantity == "")
    }

    /** "datePickerDialog"
     *  Description: Create a datepicker dialog
     * */
    private fun datePickerDialog() {
        // Set date object as previously set date and update if it is changed
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                updateBestBeforeText()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH),
        )
        datePicker.show()
    }

    /** "updateBestBeforeText"
     *  Description: Updates the best before text to the user selection
     * */
    private fun updateBestBeforeText() {
        val bestBeforeText = view.findViewById<TextView>(R.id.update_date_input_current)
        val formattedDate = ConvertUtils.longToDateString(selectedDate.timeInMillis)
        bestBeforeText.text = formattedDate.uppercase(Locale.getDefault())
    }

    /** "deleteIngredient"
     *  Description: Confirms a user wants to delete an ingredient with an alert dialog
     *               and begins delete database operations if they agree
     * */
    private fun deleteIngredient() {
        // Show alert dialog to confirm deletion or not
        val builder = AlertDialog.Builder(requireContext())
        builder.setPositiveButton("Yes") { _, _ ->
            ingredientViewModel.deleteIngredient(args.currentIngredient)
            Toast.makeText(requireContext(), "${args.currentIngredient.name} has been removed!", Toast.LENGTH_SHORT).show()
            // After deleting, go back to ingredients list fragment
            findNavController().navigate(R.id.action_navigation_ingredient_update_to_navigation_ingredient)
        }
        builder.setNegativeButton("No") { _, _ -> }
        builder.setTitle("Delete ingredient?")
        builder.setMessage("Are you sure you want to remove ${args.currentIngredient.name} from your ingredients?")
        builder.create().show()
    }

    /** "categoryStringToInt"
     *  Description: Takes a string and converts it to its corresponding integer
     *               type from the Category model
     * */
    private fun categoryStringToInt(string: String): Int {
        val categoryEnum = CategoryType.fromString(string)
        return categoryEnum.asInt
    }

    /** "quantityTypeStringToInt"
     *  Description: Takes a string and converts it to its corresponding integer
     *               type from the QuantityType model
     * */
    private fun quantityTypeStringToInt(quantityType: String): Int {
        val quantityTypeEnum = QuantityType.fromString(quantityType)
        return quantityTypeEnum.asInt
    }

    /** "daysExpiry"
     *  Description: Calculates the number of days until ingredient expiry
     * */
    fun daysExpiry(expiryDateLong : Long, selectedDate : Long): Long {
        val expiryDate
                = convertLongtoDate(expiryDateLong)
        val currentDate = convertLongtoDate(selectedDate)
        return ChronoUnit.DAYS.between(currentDate, expiryDate)
    }

    /** "convertLongtoDate"
     *  Description: Converts a long into a local date object
     * */
    fun convertLongtoDate(dateMilli : Long): LocalDate {
        val date = Instant.ofEpochMilli(dateMilli)
        return date.atZone(ZoneId.systemDefault()).toLocalDate()
    }
}
