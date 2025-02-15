package com.example.classescreator.examples

import com.example.classescreator.*
import android.util.Log
import com.kotlinclasses.DateTimePatterns
import com.kotlinclasses.getCurrentDateFromFormat
import com.kotlinclasses.isContainOnlyNumbers
import com.kotlinclasses.isValidEmail
import com.kotlinclasses.removeSpaces
import com.kotlinclasses.validateEmail
import com.kotlinclasses.validateNumbers

/**
 * Examples demonstrating the usage of TPStringExt functions
 * أمثلة توضح استخدام دوال TPStringExt
 */
object TPStringExtExamples {
    private const val TAG = "TPStringExtExamples"

    /**
     * Demonstrates date formatting functions
     * يوضح دوال تنسيق التاريخ
     */
    fun demonstrateDateFormatting() {
        println( TAG + "\n=== Date Formatting Examples ===")
        
        // Using getCurrentDateFromFormat with different patterns
        "yyyy-MM-dd".getCurrentDateFromFormat().onSuccess { date ->
            println(TAG + "Current date (yyyy-MM-dd): $date")
        }

        "HH:mm".getCurrentDateFromFormat().onSuccess { time ->
            println(TAG + "Current time (24-hour): $time")
        }

        "hh:mm a".getCurrentDateFromFormat().onSuccess { time ->
            println(TAG + "Current time (12-hour): $time")
        }

        // Using predefined DateTimePatterns
        DateTimePatterns.DATE_FULL.getCurrentDateFromFormat().onSuccess { date ->
            println(TAG + "Full date: $date")
        }

        // Handling errors
        "invalid format".getCurrentDateFromFormat().onError { error ->
            println(TAG + "Error with invalid format: $error")
        }
    }

    /**
     * Demonstrates string manipulation functions
     * يوضح دوال معالجة النصوص
     */
    fun demonstrateStringManipulation() {
        println(TAG + "\n=== String Manipulation Examples ===")
        
        // removeSpaces examples
        val text1 = "  Hello   World  "
        println(TAG + "Original text: '$text1'")
        println(TAG + "After removing spaces: '${text1.removeSpaces()}'")

        val text2 = "1 2 3 4 5"
        println(TAG + "\nOriginal numbers: '$text2'")
        println(TAG + "After removing spaces: '${text2.removeSpaces()}'")
    }

    /**
     * Demonstrates number validation functions
     * يوضح دوال التحقق من الأرقام
     */
    fun demonstrateNumberValidation() {
        println(TAG + "\n=== Number Validation Examples ===")
        
        // Basic number validation
        val numbers = listOf("123", "123 456", "12.34", "12a34", "   12  34   ", "")
        
        println("Testing isContainOnlyNumbers:")
        numbers.forEach { number ->
            println(TAG + "'$number' contains only numbers: ${number.isContainOnlyNumbers()}")
        }

        println("\nTesting validateNumbers:")
        numbers.forEach { number ->
            val result = number.validateNumbers()
            println(TAG + "'$number' validation result: $result")
        }

        // Using validation callbacks
        "123456".validateNumbers().onValid { value ->
            println(TAG + "\nValid number: $value")
        }

        "12.34".validateNumbers().onInvalid { error ->
            println(TAG + "Invalid number: $error")
        }
    }

    /**
     * Demonstrates email validation functions
     * يوضح دوال التحقق من البريد الإلكتروني
     */
    fun demonstrateEmailValidation() {
        println(TAG + "\n=== Email Validation Examples ===")
        
        // Basic email validation
        val emails = listOf(
            "user@example.com",
            "user.name@domain.co.uk",
            "invalid.email",
            "no@tld@domain",
            "user@gmail.com"
        )

        println(TAG + "Testing isValidEmail:")
        emails.forEach { email ->
            println("'$email' is valid: ${email.isValidEmail()}")
        }

        println(TAG + "\nTesting isValidEmail with popular domains only:")
        emails.forEach { email ->
            println(TAG + "'$email' is valid (popular domains): ${email.isValidEmail(popularDomainsOnly = true)}")
        }

        println(TAG + "\nTesting validateEmail:")
        emails.forEach { email ->
            val result = email.validateEmail()
            println(TAG + "'$email' validation result: $result")
        }
    }

    /**
     * Run all examples
     * تشغيل جميع الأمثلة
     */
    fun runAllExamples() {
        println(TAG + "Running TPStringExt Examples...")
        demonstrateDateFormatting()
        demonstrateStringManipulation()
        demonstrateNumberValidation()
        demonstrateEmailValidation()
        println(TAG + "\nTPStringExt Examples completed.")
    }
}

