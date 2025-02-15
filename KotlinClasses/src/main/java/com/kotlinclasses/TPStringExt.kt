package com.kotlinclasses

import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

/**
 * Extension functions for String operations.
 * توفر دوال مساعدة للتعامل مع النصوص
 */


data class DateTimeResult(
    val formattedDateTime: String? = null,
    val error: String? = null
) {
    /**
     * Returns true if the parsing was successful (formattedDateTime is not null and error is null)
     */
    val isSuccess: Boolean
        get() = formattedDateTime != null && error == null

    /**
     * Returns true if there was an error during parsing (error is not null)
     */
    val isError: Boolean
        get() = error != null

    /**
     * Returns the formatted date/time or a default value if parsing failed
     * 
     * @param defaultValue The value to return if parsing failed
     * @return The formatted date/time or the default value
     */
    fun getOrDefault(defaultValue: String): String {
        return formattedDateTime ?: defaultValue
    }

    /**
     * Returns the formatted date/time or throws an exception if parsing failed
     * 
     * @throws IllegalStateException if parsing failed
     * @return The formatted date/time
     */
    fun getOrThrow(): String {
        return formattedDateTime ?: throw IllegalStateException(error ?: "Unknown error occurred during parsing")
    }

    /**
     * Executes the given block if parsing was successful
     * 
     * @param block The block to execute with the formatted date/time
     */
    inline fun onSuccess(block: (String) -> Unit) {
        if (isSuccess) {
            formattedDateTime?.let(block)
        }
    }

    /**
     * Executes the given block if parsing failed
     * 
     * @param block The block to execute with the error message
     */
    inline fun onError(block: (String) -> Unit) {
        if (isError) {
            error?.let(block)
        }
    }

    override fun toString(): String {
        return when {
            isSuccess -> formattedDateTime!!
            isError -> error!!
            else -> "Invalid state: Both formattedDateTime and error are null"
        }
    }

    companion object {
        /**
         * Creates a successful result
         * 
         * @param dateTime The formatted date/time string
         * @return A successful DateTimeResult
         */
        fun success(dateTime: String): DateTimeResult {
            return DateTimeResult(formattedDateTime = dateTime)
        }

        /**
         * Creates an error result
         * 
         * @param message The error message
         * @return A DateTimeResult with an error
         */
        fun error(message: String): DateTimeResult {
            return DateTimeResult(error = message)
        }
    }
}

/**
 * Available date formats:
 * - DEFAULT:        MM/dd/yyyy    (01/22/2025)
 * - DASHES:         MM-dd-yyyy    (01-22-2025)
 * - DOTS:           MM.dd.yyyy    (01.22.2025)
 * - REVERSE:        yyyy/MM/dd    (2025/01/22)
 * - REVERSE_DASHES: yyyy-MM-dd    (2025-01-22)
 * - REVERSE_DOTS:   yyyy.MM.dd    (2025.01.22)
 * - TIME:           HH:mm         (14:30)
 * - DATE_TIME:      MM/dd/yyyy HH:mm  (01/22/2025 14:30)
 * 
 * Example usage:
 * ```
 * // Date formats
 * "2018 10 12".getCurrentDateFromFormat()                    // -> "10/12/2018"
 * "2018 10 12".getCurrentDateFromFormat(DateFormat.DASHES)   // -> "10-12-2018"
 * "2018 10 12".getCurrentDateFromFormat(DateFormat.DOTS)     // -> "10.12.2018"
 * 
 * // Time formats (24-hour)
 * "1430".getCurrentDateFromFormat()         // -> "14:30"
 * "14:30".getCurrentDateFromFormat()        // -> "14:30"
 * "14-30".getCurrentDateFromFormat()        // -> "14:30"
 * "14 30".getCurrentDateFromFormat()        // -> "14:30"
 * 
 * // Time formats (12-hour)
 * "0230PM".getCurrentDateFromFormat()       // -> "14:30"
 * "02:30PM".getCurrentDateFromFormat()      // -> "14:30"
 * "02-30PM".getCurrentDateFromFormat()      // -> "14:30"
 * "02 30 PM".getCurrentDateFromFormat()     // -> "14:30"
 * 
 * // Combined date and time
 * "201810121430".getCurrentDateFromFormat()           // -> "10/12/2018 14:30"
 * "2018-10-12 14:30".getCurrentDateFromFormat()      // -> "10/12/2018 14:30"
 * "2018/10/12 02:30 PM".getCurrentDateFromFormat()   // -> "10/12/2018 14:30"
 * ```
 */
enum class DateFormat(val format: String) {
    DEFAULT("MM/dd/yyyy"),         // 01/22/2025
    DASHES("MM-dd-yyyy"),          // 01-22-2025
    DOTS("MM.dd.yyyy"),            // 01.22.2025
    REVERSE("yyyy/MM/dd"),         // 2025/01/22
    REVERSE_DASHES("yyyy-MM-dd"),  // 2025-01-22
    REVERSE_DOTS("yyyy.MM.dd"),    // 2025.01.22
    TIME("HH:mm"),                 // 14:30
    DATE_TIME("MM/dd/yyyy HH:mm")  // 01/22/2025 14:30
}

const val TAG = "DateTimeFormatter"

/**
 * Gets the current date/time in the specified format.
 * الحصول على التاريخ/الوقت الحالي بالتنسيق المحدد
 * 
 * @param format التنسيق المطلوب (اختياري)
 * @return DateTimeResult يحتوي على التاريخ/الوقت المنسق أو رسالة خطأ
 *
 * أمثلة للاستخدام:
 * ```kotlin
 * // استخدام النص كتنسيق مباشرة
 * "MM/dd/yyyy".getCurrentDateFromFormat()               // -> "03/20/2024"
 * "HH:mm".getCurrentDateFromFormat()                   // -> "14:30"
 * "dd/MM/yyyy HH:mm".getCurrentDateFromFormat()        // -> "20/03/2024 14:30"
 * "EEEE, MMMM dd, yyyy".getCurrentDateFromFormat()     // -> "Wednesday, March 20, 2024"
 * "hh:mm a".getCurrentDateFromFormat()                 // -> "02:30 PM"
 * 
 * // استخدام التنسيقات الجاهزة
 * DateTimePatterns.DATE_SIMPLE.getCurrentDateFromFormat()    // -> "20/03/2024"
 * DateTimePatterns.TIME_24.getCurrentDateFromFormat()        // -> "14:30"
 * DateTimePatterns.DATE_TIME_12.getCurrentDateFromFormat()   // -> "20/03/2024 02:30 PM"
 * ```
 */
fun String.getCurrentDateFromFormat(
    format: String? = null
): DateTimeResult {
    return try {
        val formatString = when {
            this.isNotEmpty() -> this
            !format.isNullOrEmpty() -> format
            else -> DateTimePatterns.DATE_SIMPLE
        }

        // Log the attempt
        Log.d(TAG, "Attempting to format current date with pattern: '$formatString'")

        // Validate format
        validateDateFormat(formatString)?.let { errorMessage ->
            Log.e(TAG, "Format validation failed: $errorMessage")
            return DateTimeResult.error(errorMessage)
        }
        
        val date = Calendar.getInstance().time
        val formattedDate = SimpleDateFormat(formatString, Locale.getDefault()).format(date)
        
        // Log success
        Log.d(TAG, "Successfully formatted date: '$formattedDate' using pattern: '$formatString'")
        
        DateTimeResult.success(formattedDate)
    } catch (e: Exception) {
        val errorMessage = when (e) {
            is IllegalArgumentException -> {
                when {
                    e.message?.contains("MMMM") == true -> {
                        Log.e(TAG, "Invalid month name format (MMMM)", e)
                        "Invalid month name format (MMMM)"
                    }
                    e.message?.contains("EEEE") == true -> {
                        Log.e(TAG, "Invalid day name format (EEEE)", e)
                        "Invalid day name format (EEEE)"
                    }
                    e.message?.contains("yyyy") == true -> {
                        Log.e(TAG, "Invalid year format (yyyy)", e)
                        "Invalid year format (yyyy)"
                    }
                    e.message?.contains("MM") == true -> {
                        Log.e(TAG, "Invalid month format (MM)", e)
                        "Invalid month format (MM)"
                    }
                    e.message?.contains("dd") == true -> {
                        Log.e(TAG, "Invalid day format (dd)", e)
                        "Invalid day format (dd)"
                    }
                    e.message?.contains("HH") == true -> {
                        Log.e(TAG, "Invalid 24-hour format (HH)", e)
                        "Invalid 24-hour format (HH)"
                    }
                    e.message?.contains("hh") == true -> {
                        Log.e(TAG, "Invalid 12-hour format (hh)", e)
                        "Invalid 12-hour format (hh)"
                    }
                    e.message?.contains("mm") == true -> {
                        Log.e(TAG, "Invalid minutes format (mm)", e)
                        "Invalid minutes format (mm)"
                    }
                    e.message?.contains("ss") == true -> {
                        Log.e(TAG, "Invalid seconds format (ss)", e)
                        "Invalid seconds format (ss)"
                    }
                    e.message?.contains("a") == true -> {
                        Log.e(TAG, "Invalid AM/PM marker format (a)", e)
                        "Invalid AM/PM marker format (a)"
                    }
                    else -> {
                        Log.e(TAG, "Invalid format: ${e.message}", e)
                        "Invalid format: ${e.message}"
                    }
                }
            }
            else -> {
                Log.e(TAG, "Unexpected error while formatting date", e)
                "Error occurred: ${e.localizedMessage}"
            }
        }
        DateTimeResult.error(errorMessage)
    }
}

/**
 * Validates the date format pattern
 */
private fun validateDateFormat(format: String): String? {
    return when {
        format.isEmpty() -> {
            Log.w(TAG, "Empty format pattern provided")
            "Format cannot be empty"
        }

        format.any { it !in "yMdHhmsSaEw-/.,: " } -> {
            val invalidChars = format.filter { it !in "yMdHhmsSaEw-/.,: " }
            Log.w(TAG, "Invalid characters found in format: $invalidChars")
            "Format contains invalid characters. Only use: y, M, d, H, h, m, s, S, a, E, w, and separators (-/.,: )"
        }

        !format.contains("y") && !format.contains("M") && !format.contains("d") 
            && !format.contains("H") && !format.contains("h") && !format.contains("m") -> {
            Log.w(TAG, "Format pattern missing required elements: $format")
            """
            Format must contain at least one of:
            • y (year)
            • M (month)
            • d (day)
            • H (24-hour)
            • h (12-hour)
            • m (minutes)
            """.trimIndent()
        }

        format.contains("YYYY") -> {
            Log.w(TAG, "Incorrect year format YYYY used instead of yyyy")
            "Use 'yyyy' instead of 'YYYY' for year"
        }
            
        format.contains("DD") -> {
            Log.w(TAG, "Incorrect day format DD used instead of dd")
            "Use 'dd' instead of 'DD' for day"
        }
            
        format.contains("hh") && !format.contains("a") -> {
            Log.w(TAG, "12-hour format used without AM/PM marker")
            "When using 12-hour format (hh), you must include 'a' for AM/PM"
        }

        format.contains("HH") && format.contains("a") -> {
            Log.w(TAG, "24-hour format used with AM/PM marker")
            "24-hour format (HH) should not include AM/PM marker (a)"
        }

        format.count { it == 'y' } > 4 -> {
            Log.w(TAG, "Too many year characters: ${format.count { it == 'y' }}")
            "Too many year characters (y). Maximum is 4"
        }

        format.count { it == 'M' } > 4 -> {
            Log.w(TAG, "Too many month characters: ${format.count { it == 'M' }}")
            "Too many month characters (M). Maximum is 4"
        }

        format.count { it == 'd' } > 2 -> {
            Log.w(TAG, "Too many day characters: ${format.count { it == 'd' }}")
            "Too many day characters (d). Maximum is 2"
        }

        format.count { it == 'H' } > 2 -> {
            Log.w(TAG, "Too many hour characters: ${format.count { it == 'H' }}")
            "Too many hour characters (H). Maximum is 2"
        }

        format.count { it == 'h' } > 2 -> {
            Log.w(TAG, "Too many hour characters: ${format.count { it == 'h' }}")
            "Too many hour characters (h). Maximum is 2"
        }

        format.count { it == 'm' } > 2 -> {
            Log.w(TAG, "Too many minute characters: ${format.count { it == 'm' }}")
            "Too many minute characters (m). Maximum is 2"
        }

        format.count { it == 's' } > 2 -> {
            Log.w(TAG, "Too many second characters: ${format.count { it == 's' }}")
            "Too many second characters (s). Maximum is 2"
        }

        format.count { it == 'a' } > 1 -> {
            Log.w(TAG, "Too many AM/PM markers: ${format.count { it == 'a' }}")
            "Too many AM/PM markers (a). Maximum is 1"
        }

        else -> null
    }
}

/**
 * Common date/time format patterns
 * أنماط شائعة لتنسيق التاريخ/الوقت
 */
object DateTimePatterns {
    // Date patterns (أنماط التاريخ)
    const val DATE_SIMPLE = "dd/MM/yyyy"           // 20/03/2024
    const val DATE_DASHES = "dd-MM-yyyy"           // 20-03-2024
    const val DATE_DOTS = "dd.MM.yyyy"             // 20.03.2024
    const val DATE_REVERSE = "yyyy/MM/dd"          // 2024/03/20
    const val DATE_REVERSE_DASHES = "yyyy-MM-dd"   // 2024-03-20
    const val DATE_REVERSE_DOTS = "yyyy.MM.dd"    // 2024.03.20
    const val DATE_FULL = "EEEE, MMMM dd, yyyy"    // Wednesday, March 20, 2024
    const val DATE_MEDIUM = "MMM dd, yyyy"         // Mar 20, 2024
    
    // Time patterns (أنماط الوقت)
    const val TIME_24 = "HH:mm"                    // 14:30
    const val TIME_24_SECONDS = "HH:mm:ss"         // 14:30:45
    const val TIME_12 = "hh:mm a"                  // 02:30 PM
    const val TIME_12_SECONDS = "hh:mm:ss a"       // 02:30:45 PM
    
    // Combined patterns (أنماط مشتركة)
    const val DATE_TIME_SIMPLE = "dd/MM/yyyy HH:mm"        // 20/03/2024 14:30
    const val DATE_TIME_SECONDS = "dd/MM/yyyy HH:mm:ss"    // 20/03/2024 14:30:45
    const val DATE_TIME_12 = "dd/MM/yyyy hh:mm a"         // 20/03/2024 02:30 PM
    const val DATE_TIME_FULL = "EEEE, MMMM dd, yyyy HH:mm" // Wednesday, March 20, 2024 14:30
}

/**
 * Removes all whitespace characters from a string.
 * يزيل جميع المسافات البيضاء من النص
 * 
 * Example usage:
 * مثال للاستخدام:
 * ```kotlin
 * val text = "  Hello   World  ".removeSpaces()  // returns "HelloWorld"
 * val numbers = "1 2 3".removeSpaces()           // returns "123"
 * ```
 * 
 * @return String with all spaces removed
 * @return النص بدون مسافات
 */
fun String.removeSpaces(): String {
    return replace("\\s+".toRegex(), "")
}

/**
 * Checks if the string contains only numeric characters.
 * Spaces, tabs, and line breaks will be removed before checking.
 * 
 * Examples:
 * ```kotlin
 * "123".isContainOnlyNumbers()      // returns true
 * "123 456".isContainOnlyNumbers()  // returns true
 * "12.34".isContainOnlyNumbers()    // returns false
 * "12a34".isContainOnlyNumbers()    // returns false
 * "   12  34   ".isContainOnlyNumbers() // returns true
 * ```
 * 
 * @return true if string contains only numbers after removing whitespace, false otherwise
 */
fun String.isContainOnlyNumbers(): Boolean {
    val cleaned = replace(Regex("\\s+"), "")
    if (cleaned.isEmpty()) {
        return false
    }
    return cleaned.all { it.isDigit() }
}

/**
 * Checks if the string contains only numeric characters and returns a detailed result.
 * Spaces, tabs, and line breaks will be removed before checking.
 * 
 * Examples:
 * ```kotlin
 * "123".validateNumbers()           // Valid
 * "123 456".validateNumbers()       // Valid
 * "12.34".validateNumbers()         // Invalid: Contains decimal point
 * "12a34".validateNumbers()         // Invalid: Contains letters
 * "   ".validateNumbers()           // Invalid: Empty after cleaning
 * ```
 * 
 * @return NumberValidationResult containing the validation status and any error message
 */
fun String.validateNumbers(): NumberValidationResult {
    val cleaned = replace(Regex("\\s+"), "")
    
    return when {
        cleaned.isEmpty() -> {
            NumberValidationResult(
                isValid = false,
                error = "String is empty or contains only whitespace",
                cleanedValue = null
            )
        }
        cleaned.all { it.isDigit() } -> {
            NumberValidationResult(
                isValid = true,
                error = null,
                cleanedValue = cleaned
            )
        }
        else -> {
            val invalidChars = cleaned.filter { !it.isDigit() }.toSet()
            NumberValidationResult(
                isValid = false,
                error = "Contains invalid characters: ${invalidChars.joinToString()}",
                cleanedValue = null
            )
        }
    }
}

/**
 * Represents the result of number validation.
 */
data class NumberValidationResult(
    val isValid: Boolean,
    val error: String?,
    val cleanedValue: String?
) {
    /**
     * Returns true if the string contains only numbers
     */
    fun isOnlyNumbers() = isValid

    /**
     * Returns the error message or null if valid
     */
    fun getErrorOrNull() = error

    /**
     * Returns the cleaned value (whitespace removed) or null if invalid
     */
    fun getCleanedValueOrNull() = cleanedValue

    /**
     * Executes the given block if the validation is successful
     */
    inline fun onValid(block: (String) -> Unit) {
        if (isValid && cleanedValue != null) {
            block(cleanedValue)
        }
    }

    /**
     * Executes the given block if the validation fails
     */
    inline fun onInvalid(block: (String) -> Unit) {
        if (!isValid && error != null) {
            block(error)
        }
    }

    override fun toString(): String {
        return when {
            isValid -> "Valid: $cleanedValue"
            else -> "Invalid: $error"
        }
    }
}

/**
 * Validates if the string is a properly formatted email address.
 * يتحقق مما إذا كان النص عنوان بريد إلكتروني صحيح
 * 
 * The email validation checks for:
 * يتحقق من:
 * - Presence of @ symbol (وجود علامة @)
 * - Valid local part before @ (صحة الجزء المحلي قبل @)
 * - Valid domain part after @ (صحة اسم النطاق بعد @)
 * - Proper TLD (صحة امتداد النطاق)
 * 
 * Example usage:
 * مثال للاستخدام:
 * ```kotlin
 * "user@example.com".isValidEmail()     // returns true
 * "user.name@domain.co.uk".isValidEmail() // returns true
 * "invalid.email".isValidEmail()        // returns false
 * "no@tld@domain".isValidEmail()        // returns false
 * ```
 * 
 * @param popularDomainsOnly If true, only allows popular email domains (Gmail, Yahoo, Outlook, etc.)
 * @return true if string is a valid email address, false otherwise
 * @return true إذا كان النص عنوان بريد إلكتروني صحيح، false في غير ذلك
 */
fun String.isValidEmail(popularDomainsOnly: Boolean = false): Boolean {
    // تعبير منتظم أكثر دقة للتحقق من صحة البريد الإلكتروني
    val emailRegex = """^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"""
    
    if (!matches(Regex(emailRegex))) {
        return false
    }

    // التحقق من عدم وجود نقطتين متتاليتين
    if (contains("..")) {
        return false
    }

    // التحقق من أن النطاق يحتوي على نقطة واحدة على الأقل
    val parts = split("@")
    if (parts.size != 2 || !parts[1].contains(".")) {
        return false
    }

    if (popularDomainsOnly) {
        val popularDomains = listOf(
            // Google
            "@gmail.com",
            "@googlemail.com",
            
            // Microsoft
            "@outlook.com",
            "@hotmail.com",
            "@live.com",
            "@msn.com",
            
            // Yahoo
            "@yahoo.com",
            "@yahoo.co.uk",
            "@yahoo.co.jp",
            "@yahoo.fr",
            "@yahoo.de",
            "@yahoo.it",
            "@yahoo.es",
            "@yahoo.ca",
            
            // Apple
            "@icloud.com",
            "@me.com",
            "@mac.com",
            
            // Other Popular
            "@aol.com",
            "@protonmail.com",
            "@proton.me",
            "@zoho.com",
            "@yandex.com",
            "@yandex.ru",
            
            // Business/Education
            "@edu",
            "@ac.uk",
            "@edu.com",
            "@org",
            "@gov"
        )

        return popularDomains.any { domain ->
            lowercase().endsWith(domain.lowercase())
        }
    }

    return true
}

/**
 * Validates if the string is a valid email address and returns a detailed result.
 * 
 * @param popularDomainsOnly If true, only allows popular email domains (Gmail, Yahoo, Outlook, etc.)
 * @return EmailValidationResult containing the validation status and any error message
 */
fun String.validateEmail(popularDomainsOnly: Boolean = false): EmailValidationResult {
    val emailRegex = """^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$"""
    
    if (isEmpty()) {
        return EmailValidationResult(
            isValid = false,
            error = "Email cannot be empty"
        )
    }

    if (!contains("@")) {
        return EmailValidationResult(
            isValid = false,
            error = "Email must contain '@' symbol"
        )
    }

    if (!matches(Regex(emailRegex))) {
        return EmailValidationResult(
            isValid = false,
            error = "Invalid email format"
        )
    }

    if (popularDomainsOnly) {
        val domain = substringAfter("@").lowercase()
        if (!isPopularEmailDomain(domain)) {
            return EmailValidationResult(
                isValid = false,
                error = "Only popular email domains are allowed (Gmail, Yahoo, Outlook, etc.)"
            )
        }
    }

    return EmailValidationResult(
        isValid = true,
        error = null
    )
}

/**
 * Represents the result of email validation.
 */
data class EmailValidationResult(
    val isValid: Boolean,
    val error: String?
) {
    /**
     * Returns true if the email is valid
     */
    fun isValidEmail() = isValid

    /**
     * Returns the error message or null if the email is valid
     */
    fun getErrorOrNull() = error

    /**
     * Executes the given block if the email is valid
     */
    inline fun onValid(block: () -> Unit) {
        if (isValid) block()
    }

    /**
     * Executes the given block if the email is invalid
     */
    inline fun onInvalid(block: (String) -> Unit) {
        if (!isValid && error != null) block(error)
    }
}

/**
 * Checks if the given domain is a popular email domain.
 */
private fun isPopularEmailDomain(domain: String): Boolean {
    val popularDomains = listOf(
        // Google
        "gmail.com",
        "googlemail.com",
        
        // Microsoft
        "outlook.com",
        "hotmail.com",
        "live.com",
        "msn.com",
        
        // Yahoo
        "yahoo.com",
        "yahoo.co.uk",
        "yahoo.co.jp",
        "yahoo.fr",
        "yahoo.de",
        "yahoo.it",
        "yahoo.es",
        "yahoo.ca",
        
        // Apple
        "icloud.com",
        "me.com",
        "mac.com",
        
        // Other Popular
        "aol.com",
        "protonmail.com",
        "proton.me",
        "zoho.com",
        "yandex.com",
        "yandex.ru",
        
        // Business/Education
        "edu",
        "ac.uk",
        "edu.com",
        "org",
        "gov"
    )

    return popularDomains.any { domain.endsWith(it) }
}

/**
 * Gets the current date/time in the specified custom format.
 * الحصول على التاريخ/الوقت الحالي بتنسيق مخصص
 * 
 * @param customFormat تنسيق مخصص (مثل "yyyy-MM-dd HH:mm:ss")
 * @return التاريخ/الوقت الحالي بالتنسيق المخصص
 *
 * أمثلة للاستخدام:
 * ```kotlin
 * // تنسيقات مخصصة
 * getCurrentDateTimeCustomFormat("yyyy-MM-dd")            // -> "2024-03-20"
 * getCurrentDateTimeCustomFormat("HH:mm:ss")             // -> "14:30:45"
 * getCurrentDateTimeCustomFormat("dd/MM/yyyy HH:mm:ss")  // -> "20/03/2024 14:30:45"
 * getCurrentDateTimeCustomFormat("EEEE, MMMM dd, yyyy")  // -> "Wednesday, March 20, 2024"
 * ```
 */
fun getCurrentDateTimeCustomFormat(customFormat: String): String {
    val date = Calendar.getInstance().time
    return SimpleDateFormat(customFormat, Locale.getDefault()).format(date)
}