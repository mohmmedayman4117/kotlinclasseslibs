package com.kotlinclasses

import android.content.Context
import android.content.SharedPreferences

class TPAppLanguageImpl(private val context: Context) : TPAppLanguageDelegate {
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("TPAppLanguage", Context.MODE_PRIVATE)
    }

    override fun readLanguageCodeFromUserPreferences(title: String): String? {
        return sharedPreferences.getString(title, null)
    }

    override fun saveLanguageCodeToUserPreferences(title: String, languageCode: String) {
        sharedPreferences.edit().putString(title, languageCode).apply()
    }

    override fun languageDidUpdate() {
        // Recreate the activity to apply language changes
        if (context is android.app.Activity) {
            context.recreate()
        }
    }
}
