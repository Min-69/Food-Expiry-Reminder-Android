package com.example.foodexpiredreminder

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.reflect.TypeToken
import java.io.IOException

class FileHelper(private val context: Context) {

    private val gson: Gson = GsonBuilder().create()
    private val fileName = "products.json"

    fun saveData(products: List<Product>) {
        try {
            val jsonString = gson.toJson(products)
            context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                it.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun loadData(): MutableList<Product> {
        try {
            val file = context.getFileStreamPath(fileName)
            if (!file.exists() || file.length() == 0L) {
                return mutableListOf()
            }

            val jsonString = file.bufferedReader().use { it.readText() }
            val type = object : TypeToken<MutableList<Product>>() {}.type
            return gson.fromJson(jsonString, type) ?: mutableListOf()
        } catch (e: IOException) {
            e.printStackTrace()
            // If reading fails, you might want to start with a fresh list or notify the user.
            return mutableListOf()
        } catch (e: JsonParseException) {
            e.printStackTrace()
            // If parsing fails, the file is likely corrupt. Start fresh.
            context.deleteFile(fileName)
            return mutableListOf()
        }
    }
}