package com.example.foodexpiredreminder

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddProductActivity : AppCompatActivity() {

    private lateinit var editTextProductName: TextInputEditText
    private lateinit var editTextQuantity: TextInputEditText
    private lateinit var autoCompleteProductType: AutoCompleteTextView
    private lateinit var editTextPurchaseDate: TextInputEditText
    private lateinit var editTextExpiryDate: TextInputEditText
    private lateinit var buttonSave: Button
    private lateinit var alarmScheduler: AlarmScheduler

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        editTextProductName = findViewById(R.id.editTextName)
        editTextQuantity = findViewById(R.id.editTextQuantity)
        autoCompleteProductType = findViewById(R.id.autoCompleteProductType)
        editTextPurchaseDate = findViewById(R.id.editTextPurchaseDate)
        editTextExpiryDate = findViewById(R.id.editTextExpiryDate)
        buttonSave = findViewById(R.id.buttonSave)
        alarmScheduler = AlarmScheduler(this)

        setupSpinner()
        setupDatePicker()

        buttonSave.setOnClickListener {
            saveNewProduct()
        }
    }

    private fun setupSpinner() {
        val productTypes = ProductType.values().map { 
            val resId = when(it) {
                ProductType.KERING -> R.string.sort_dry
                ProductType.BASAH -> R.string.sort_wet
                ProductType.BEKU -> R.string.sort_frozen
            }
            getString(resId)
        }
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, productTypes)
        autoCompleteProductType.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        editTextPurchaseDate.setOnClickListener {
            showDatePickerDialog(editTextPurchaseDate)
        }
        editTextExpiryDate.setOnClickListener {
            showDatePickerDialog(editTextExpiryDate)
        }
    }

    private fun showDatePickerDialog(targetEditText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val formattedDate = dateFormat.format(calendar.time)
            targetEditText.setText(formattedDate)
        }
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveNewProduct() {
        val name = editTextProductName.text.toString()
        val quantityStr = editTextQuantity.text.toString()
        val purchaseDateStr = editTextPurchaseDate.text.toString()
        val expiryDateStr = editTextExpiryDate.text.toString()
        val typeStr = autoCompleteProductType.text.toString()

        if (name.isBlank() || quantityStr.isBlank() || purchaseDateStr.isBlank() || expiryDateStr.isBlank() || typeStr.isBlank()) {
            Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val quantity = quantityStr.toInt()
            val purchaseDate = dateFormat.parse(purchaseDateStr)?.time ?: 0L
            val expiryDate = dateFormat.parse(expiryDateStr)?.time ?: 0L
            
            val type = ProductType.values().find { getString(it.getResId()) == typeStr } ?: ProductType.KERING

            // Status will be set in MainActivity for consistency
            val newProduct = Product(
                name = name, 
                type = type, 
                quantity = quantity, 
                purchaseDate = purchaseDate, 
                expiryDate = expiryDate, 
                status = ProductStatus.AMAN
            )

            alarmScheduler.schedule(newProduct)

            val resultIntent = Intent()
            resultIntent.putExtra("NEW_PRODUCT", newProduct)
            setResult(Activity.RESULT_OK, resultIntent)

            Toast.makeText(this, getString(R.string.product_added), Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: ParseException) {
            Toast.makeText(this, getString(R.string.date_format_error), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, getString(R.string.quantity_format_error), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.unexpected_error, e.message), Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}

fun ProductType.getResId(): Int {
    return when (this) {
        ProductType.KERING -> R.string.sort_dry
        ProductType.BASAH -> R.string.sort_wet
        ProductType.BEKU -> R.string.sort_frozen
    }
}
