package com.example.foodexpiredreminder

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class EditProductActivity : AppCompatActivity() {
    private lateinit var editTextProductName: TextInputEditText
    private lateinit var spinnerProductType: Spinner
    private lateinit var editTextQuantity: TextInputEditText
    private lateinit var editTextPurchaseDate: TextInputEditText
    private lateinit var editTextExpiryDate: TextInputEditText
    private lateinit var buttonSave: Button
    private lateinit var alarmScheduler: AlarmScheduler

    private var currentProduct: Product? = null
    private var productPosition: Int = -1

    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        @Suppress("DEPRECATION")
        currentProduct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("EXTRA_PRODUCT", Product::class.java)
        } else {
            intent.getSerializableExtra("EXTRA_PRODUCT") as? Product
        }

        productPosition = intent.getIntExtra("EXTRA_PRODUCT_POSITION", -1)

        if (currentProduct == null || productPosition == -1) {
            Toast.makeText(this, getString(R.string.load_product_error), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        editTextProductName = findViewById(R.id.editTextProductName)
        spinnerProductType = findViewById(R.id.spinnerProductType)
        editTextQuantity = findViewById(R.id.editTextQuantity)
        editTextPurchaseDate = findViewById(R.id.editTextPurchaseDate)
        editTextExpiryDate = findViewById(R.id.editTextExpiryDate)
        buttonSave = findViewById(R.id.buttonSave)
        alarmScheduler = AlarmScheduler(this)

        setupSpinner()
        setupDatePickers()
        populateForm(currentProduct!!)

        buttonSave.setOnClickListener {
            saveChanges()
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
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, productTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProductType.adapter = adapter
    }

    private fun setupDatePickers() {
        editTextPurchaseDate.setOnClickListener {
            showDatePickerDialog(editTextPurchaseDate, currentProduct?.purchaseDate ?: System.currentTimeMillis())
        }
        editTextExpiryDate.setOnClickListener {
            showDatePickerDialog(editTextExpiryDate, currentProduct?.expiryDate ?: System.currentTimeMillis())
        }
    }

    private fun showDatePickerDialog(editText: TextInputEditText, initialTimestamp: Long) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = initialTimestamp
        }

        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            editText.setText(dateFormat.format(calendar.time))
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun populateForm(product: Product) {
        editTextProductName.setText(product.name)
        editTextQuantity.setText(product.quantity.toString())
        editTextPurchaseDate.setText(dateFormat.format(Date(product.purchaseDate)))
        editTextExpiryDate.setText(dateFormat.format(Date(product.expiryDate)))

        val productTypePosition = product.type.ordinal
        spinnerProductType.setSelection(productTypePosition)
    }

    private fun saveChanges() {
        val oldProduct = currentProduct!!
        val name = editTextProductName.text.toString()
        val quantityStr = editTextQuantity.text.toString()
        val purchaseDateStr = editTextPurchaseDate.text.toString()
        val expiryDateStr = editTextExpiryDate.text.toString()

        if (name.isBlank() || quantityStr.isBlank() || purchaseDateStr.isBlank() || expiryDateStr.isBlank()) {
            Toast.makeText(this, getString(R.string.all_fields_required), Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val quantity = quantityStr.toInt()
            val purchaseDate = dateFormat.parse(purchaseDateStr)?.time ?: 0L
            val expiryDate = dateFormat.parse(expiryDateStr)?.time ?: 0L
            val type = ProductType.values()[spinnerProductType.selectedItemPosition]

            // Preserve the original ID by using copy()
            val updatedProduct = oldProduct.copy(
                name = name,
                type = type,
                quantity = quantity,
                purchaseDate = purchaseDate,
                expiryDate = expiryDate
            )

            alarmScheduler.cancel(oldProduct)
            alarmScheduler.schedule(updatedProduct)

            val resultIntent = Intent()
            resultIntent.putExtra("UPDATED_PRODUCT", updatedProduct)
            resultIntent.putExtra("PRODUCT_POSITION", productPosition)
            setResult(Activity.RESULT_OK, resultIntent)

            Toast.makeText(this, getString(R.string.product_updated), Toast.LENGTH_SHORT).show()
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