package com.kotlinclasses

import android.content.Context
import android.content.res.Resources
import android.os.Build
import androidx.compose.ui.unit.LayoutDirection
import java.util.*

class TPAppLanguage(private val delegate: TPAppLanguageDelegate) {

    private val english = "en"
    private val arabic = "ar"

    private val languagesCodes = listOf(arabic, english)
    private val languagesNames = listOf("العربية", "English")
    private val languageCodeTitle = "myLanguageCode"

    // (Lazy)
    private val lazyLanguageCode: Lazy<String> = lazy {
        val savedLanguageCode = delegate.readLanguageCodeFromUserPreferences(languageCodeTitle)
        if (savedLanguageCode != null) {
            savedLanguageCode
        } else {
            val deviceLanguageCode = getDeviceLanguageCode()
            if (languagesCodes.contains(deviceLanguageCode)) {
                deviceLanguageCode
            } else {
                english // اللغة الافتراضية
            }
        }
    }

    /**
     * الحصول على رمز اللغة باستخدام Lazy
     * القيمة سيتم حسابها مرة واحدة فقط عند أول استدعاء
     */
    fun getLazyLanguageCode(): String {
        return lazyLanguageCode.value
    }

    // Computed value
    private fun getLanguageCode(): String {
        val savedLanguageCode = delegate.readLanguageCodeFromUserPreferences(languageCodeTitle)
        if (savedLanguageCode != null) {
            return savedLanguageCode
        }
        val deviceLanguageCode = getDeviceLanguageCode()
        return if (languagesCodes.contains(deviceLanguageCode)) {
            deviceLanguageCode
        } else {
            english // Default language
        }
    }

    fun updateLanguageCode(languageName: String) {
        val index = languagesNames.indexOf(languageName)
        if (index != -1) {
            val newLanguageCode = languagesCodes[index]
            delegate.saveLanguageCodeToUserPreferences(languageCodeTitle, newLanguageCode)
            delegate.languageDidUpdate()
        }
    }

    fun getAllAvailableLanguages(): List<String> {
        return this.languagesNames
    }

    fun getCurrentLanguageCode(): String {
        return getLanguageCode()
    }

    fun isRTL(): Boolean {
        return getCurrentLanguageCode() == arabic
    }

    fun getLanguageDirection(): LayoutDirection {
        return if (isRTL()) LayoutDirection.Rtl else LayoutDirection.Ltr
    }

    private fun getDeviceLanguageCode(): String {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Resources.getSystem().configuration.locales[0]
        } else {
            Resources.getSystem().configuration.locale
        }
        return locale.language
    }
}

interface TPAppLanguageDelegate {
    fun readLanguageCodeFromUserPreferences(title: String): String?
    fun saveLanguageCodeToUserPreferences(title: String, languageCode: String)
    fun languageDidUpdate()
}