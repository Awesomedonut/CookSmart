/** "IngredientAdd.kt"
 *  Description: Allows users to create an ingredient and insert it into
 *               the ingredient table in the database.
 *  Last Modified: December 4, 2023
 * */
package com.example.cooksmart.ui.ingredient

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.cooksmart.R
import com.example.cooksmart.database.Ingredient
import com.example.cooksmart.infra.services.NotificationWorker
import com.example.cooksmart.ui.structs.CategoryType
import com.example.cooksmart.ui.structs.QuantityType
import com.example.cooksmart.utils.ConvertUtils
import com.example.cooksmart.utils.PermissionCheck
import java.util.Calendar
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class IngredientAdd : Fragment() {
    private lateinit var categoriesSpinner: Spinner
    private lateinit var categoriesAdapter: SpinnerAdapter
    private lateinit var quantityTypeSpinner : Spinner
    private lateinit var quantityTypeAdapter : SpinnerAdapter
    private lateinit var ingredientViewModel: IngredientViewModel
    private lateinit var view: View
    private lateinit var selectedDate: Calendar
    private lateinit var notifyIcon : MenuItem
    private var isNotified = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_ingredient_insert, container, false)

        // Locate UI components
        val confirmButton = view.findViewById<Button>(R.id.button_confirm)
        val editDate = view.findViewById<Button>(R.id.best_before_date_picker)

        // Setting up menu option from https://stackoverflow.com/questions/74858799/how-to-inflate-menu-inside-a-fragment
        val menuHost = requireActivity() as MenuHost
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.notification_menu, menu)
                // Set the notification icon to the correct state
                notifyIcon = menu.findItem(R.id.notification_menu)
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
                    // Go back to previous page if user clicks back button on menu toolbar
                    android.R.id.home -> findNavController().navigateUp()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        ingredientViewModel = ViewModelProvider(this)[IngredientViewModel::class.java]

        // Assuming your Spinner is defined in fridge_insert layout
        categoriesSpinner = view.findViewById(R.id.category)

        // Set up the Spinner adapter
        categoriesAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            CategoryType.values().map { it.asString }
        )
        categoriesSpinner.adapter = categoriesAdapter

        // Set up Quantity spinner
        quantityTypeSpinner = view.findViewById(R.id.quantityType)
        quantityTypeAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            QuantityType.values().map { it.asString}
        )
        quantityTypeSpinner.adapter = quantityTypeAdapter

        // Set date picker to the button
        selectedDate = Calendar.getInstance()
        editDate.setOnClickListener {
            datePickerDialog()
        }

        // Set up the confirmation button
        confirmButton.setOnClickListener {
            insertIngredient()
        }

        return view
    }

    /** "datePickerDialog"
     *  Description: Create a datepicker dialog
     * */
    private fun datePickerDialog() {
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
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    /** "insertIngredient"
     *  Description: Retrieve data from user fields and insert a new ingredient into
     *               the database
     * */
    private fun insertIngredient() {
        val category = view.findViewById<Spinner>(R.id.category).selectedItem.toString()
        val name = view.findViewById<EditText>(R.id.name_ingredient).text.toString()
        val quantity = view.findViewById<EditText>(R.id.quantity).text.toString()
        val quantityType = view.findViewById<Spinner>(R.id.quantityType).selectedItem.toString()
        val currentDate = System.currentTimeMillis()
        val bestBefore = selectedDate.timeInMillis
        var notifID : UUID? = null
        // If the user would like a notification, schedule a notification to be sent
        // a day before the expiry date
        if(isNotified) {
            // Add notification to the queue
            var daysToExpiry = ConvertUtils.daysExpiry(bestBefore, currentDate) - 1
            // If the ingredient is already expired, immediately send a notification
            if (daysToExpiry < 0) {
                daysToExpiry = 0
            }
            val notificationWorkReq = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(daysToExpiry, TimeUnit.DAYS)
                .build()
            // Built from: https://developer.android.com/guide/background/persistent/getting-started/define-work
            WorkManager.getInstance(requireContext()).enqueue(notificationWorkReq)
            notifID = notificationWorkReq.id
        }
        // Check that user input is valid
        if (!isNotValidInput(name, quantity)) {
            val ingredient = Ingredient(0, name, category, quantity, quantityType, currentDate, bestBefore, notifID)
            ingredientViewModel.insertIngredient(ingredient)
            Toast.makeText(requireContext(), "Ingredient added!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_navigation_ingredient_add_to_navigation_ingredient)
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

    /** "updateBestBeforeText"
     *  Description: Updates the best before text to the user selection
     * */
    private fun updateBestBeforeText() {
        val bestBeforeText = view.findViewById<TextView>(R.id.date_input_current)
        val formattedDate = ConvertUtils.longToDateString(selectedDate.timeInMillis)
        bestBeforeText.text = formattedDate.uppercase(Locale.getDefault())
    }
}