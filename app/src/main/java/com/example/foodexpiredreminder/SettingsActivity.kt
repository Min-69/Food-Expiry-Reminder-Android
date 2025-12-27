package com.example.foodexpiredreminder

import android.os.Bundle
import android.text.InputType
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            preferenceManager.sharedPreferencesName = PREFS_NAME
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            // Set input type for EditTextPreference to number to show a numeric keyboard
            findPreference<EditTextPreference>(NOTIFICATION_DAYS)?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            findPreference<EditTextPreference>(NOTIFICATION_TIME)?.setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }

            findPreference<EditTextPreference>(NOTIFICATION_TIME)?.setOnPreferenceChangeListener { _, newValue ->
                val timeStr = newValue as String
                if (timeStr.length == 4 && timeStr.all { it.isDigit() }) {
                    val hour = timeStr.substring(0, 2).toInt()
                    val minute = timeStr.substring(2, 4).toInt()
                    if (hour in 0..23 && minute in 0..59) {
                        true
                    } else {
                        Toast.makeText(requireContext(), "Format waktu tidak valid (HHmm)", Toast.LENGTH_SHORT).show()
                        false
                    }
                } else {
                    Toast.makeText(requireContext(), "Harap masukkan 4 digit angka untuk waktu (HHmm)", Toast.LENGTH_SHORT).show()
                    false
                }
            }
        }
    }

    companion object {
        const val PREFS_NAME = "settings_prefs"
        const val NOTIFICATION_ENABLED = "notification_enabled"
        const val NOTIFICATION_DAYS = "notification_days"
        const val NOTIFICATION_TIME = "notification_time"
    }
}