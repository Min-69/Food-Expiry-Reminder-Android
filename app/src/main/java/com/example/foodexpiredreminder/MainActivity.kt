
package com.example.foodexpiredreminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.foodexpiredreminder.SettingsActivity.Companion.NOTIFICATION_DAYS
import com.example.foodexpiredreminder.SettingsActivity.Companion.PREFS_NAME
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var fab: FloatingActionButton
    private lateinit var productList: MutableList<Product>
    private lateinit var fileHelper: FileHelper
    private lateinit var alarmScheduler: AlarmScheduler
    private lateinit var searchView: SearchView

    private val editProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            @Suppress("DEPRECATION")
            val updatedProduct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra("UPDATED_PRODUCT", Product::class.java)
            } else {
                data?.getSerializableExtra("UPDATED_PRODUCT") as? Product
            }
            val position = data?.getIntExtra("PRODUCT_POSITION", -1)

            if (updatedProduct != null && position != null && position != -1) {
                val indexInMasterList = productList.indexOfFirst { it.id == updatedProduct.id }
                if (indexInMasterList != -1) {
                    productList[indexInMasterList] = updatedProduct
                    sortAndRefreshList()
                    alarmScheduler.schedule(updatedProduct) // Reschedule alarm
                    Toast.makeText(this, "Daftar telah diperbarui", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private val addProductLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data: Intent? = result.data
            @Suppress("DEPRECATION")
            val newProduct = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                data?.getSerializableExtra("NEW_PRODUCT", Product::class.java)
            } else {
                data?.getSerializableExtra("NEW_PRODUCT") as? Product
            }

            if (newProduct != null) {
                productList.add(newProduct)
                sortAndRefreshList()
                alarmScheduler.schedule(newProduct) // Schedule alarm for new product
                Toast.makeText(this, "'${newProduct.name}' berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notifikasi tidak akan muncul tanpa izin.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val themeMode = prefs.getInt(THEME_PREF, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(themeMode)

        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerViewProducts)
        fab = findViewById(R.id.fabAddProduct)
        alarmScheduler = AlarmScheduler(this)
        fileHelper = FileHelper(this)
        productList = fileHelper.loadData()

        productAdapter = ProductAdapter(
            productList,
            onEditClick = { product, position ->
                val intent = Intent(this, EditProductActivity::class.java).apply {
                    putExtra("EXTRA_PRODUCT", product)
                    putExtra("EXTRA_PRODUCT_POSITION", position)
                }
                editProductLauncher.launch(intent)
            },
            onDeleteClick = { product, _ -> // Position is not reliable with filtering
                showDeleteConfirmationDialog(product)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = productAdapter

        fab.setOnClickListener {
            val intent = Intent(this, AddProductActivity::class.java)
            addProductLauncher.launch(intent)
        }

        askNotificationPermission()
        checkExactAlarmPermission()
    }

    override fun onResume() {
        super.onResume()
        sortAndRefreshList()
    }

    override fun onPause() {
        super.onPause()
        fileHelper.saveData(productList)
    }

    @SuppressLint("RestrictedApi")
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        if (menu is MenuBuilder) {
            menu.setOptionalIconsVisible(true)
        }

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val themeMode = prefs.getInt(THEME_PREF, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        val themeItem = menu.findItem(R.id.action_theme)

        when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> themeItem.setIcon(R.drawable.ic_light_mode_24)
            AppCompatDelegate.MODE_NIGHT_YES -> themeItem.setIcon(R.drawable.ic_dark_mode_24)
            else -> themeItem.setIcon(R.drawable.ic_theme_system_24)
        }

        when (themeMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> menu.findItem(R.id.theme_light).isChecked = true
            AppCompatDelegate.MODE_NIGHT_YES -> menu.findItem(R.id.theme_dark).isChecked = true
            else -> menu.findItem(R.id.theme_system).isChecked = true
        }

        val searchItem = menu.findItem(R.id.action_search)
        searchView = searchItem.actionView as SearchView
        searchView.queryHint = "Ketik nama produk..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                productAdapter.filter.filter(newText)
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.sort_all -> productAdapter.setCategoryFilter(null)
            R.id.sort_dry -> productAdapter.setCategoryFilter(ProductType.KERING)
            R.id.sort_wet -> productAdapter.setCategoryFilter(ProductType.BASAH)
            R.id.sort_frozen -> productAdapter.setCategoryFilter(ProductType.BEKU)
            R.id.theme_light -> setThemeMode(AppCompatDelegate.MODE_NIGHT_NO)
            R.id.theme_dark -> setThemeMode(AppCompatDelegate.MODE_NIGHT_YES)
            R.id.theme_system -> setThemeMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else -> return super.onOptionsItemSelected(item)
        }
        if (item.groupId == R.id.group_theme) {
            item.isChecked = true
        }
        return true
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Produk")
            .setMessage("Apakah Anda yakin ingin menghapus '${product.name}'?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("Hapus") { _, _ ->
                alarmScheduler.cancel(product)
                productList.removeAll { it.id == product.id }
                sortAndRefreshList()
                Toast.makeText(this, "'${product.name}' telah dihapus", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                AlertDialog.Builder(this)
                    .setTitle("Izin Diperlukan")
                    .setMessage("Aplikasi ini memerlukan izin untuk menyetel alarm agar dapat memberikan notifikasi pengingat kedaluwarsa yang akurat. Izinkan aplikasi ini di pengaturan sistem.")
                    .setPositiveButton("Buka Pengaturan") { _, _ ->
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", packageName, null)
                        }
                        startActivity(intent)
                    }
                    .setNegativeButton("Nanti", null)
                    .show()
            }
        }
    }


    private fun sortAndRefreshList() {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val warningDays = prefs.getString(NOTIFICATION_DAYS, "3")?.toIntOrNull() ?: 3

        productList.forEach { product ->
            product.status = determineProductStatus(product.expiryDate, warningDays)
        }
        productList.sortBy { it.expiryDate }

        productAdapter.updateData(productList)
        //fileHelper.saveData(productList) // Save data on pause instead
    }

    private fun setThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().apply {
            putInt(THEME_PREF, mode)
            apply()
        }
        invalidateOptionsMenu()
    }

    companion object {
        const val THEME_PREF = "THEME_PREF"
        fun determineProductStatus(expiryDate: Long, warningDays: Int): ProductStatus {
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val expiry = Calendar.getInstance().apply { timeInMillis = expiryDate }

            return when {
                expiry.before(today) -> ProductStatus.KADALUWARSA
                else -> {
                    val warningCal = Calendar.getInstance().apply {
                        timeInMillis = today.timeInMillis
                        add(Calendar.DAY_OF_YEAR, warningDays)
                    }
                    if (expiry.before(warningCal)) {
                        ProductStatus.HAMPIR_KADALUWARSA
                    } else {
                        ProductStatus.AMAN
                    }
                }
            }
        }
    }
}