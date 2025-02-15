package com.example.classescreator.examples

import android.os.Build
import androidx.annotation.RequiresApi
import com.kotlinclasses.TPDateExt
import java.time.LocalDate

class TPDateExtExample {
    @RequiresApi(Build.VERSION_CODES.O)
    private val dateExt = TPDateExt()

    @RequiresApi(Build.VERSION_CODES.O)
    fun demonstrateGetPrevious() {
        // Example 1: Compare two specific dates
        val date1 = LocalDate.of(2024, 1, 1)
        val date2 = LocalDate.of(2024, 2, 1)
        val monthsBetween = dateExt.getPrevious(date1, date2).Months()
        println("Months between $date1 and $date2: $monthsBetween")

        // Example 2: Compare with current date
        val someDate = LocalDate.of(2024, 1, 1)
        val monthsToNow = dateExt.getPrevious(someDate).Months()
        println("Months from $someDate to now: $monthsToNow")

        // Example 3: Get all time units from start of year
        val year = 2024
        println("Time passed in $year:")
        println("Months: ${dateExt.getPrevious(year).Months()}")
        println("Weeks: ${dateExt.getPrevious(year).Weeks()}")
        println("Days: ${dateExt.getPrevious(year).Days()}")
        println("Hours: ${dateExt.getPrevious(year).Hours()}")
        println("Minutes: ${dateExt.getPrevious(year).Minutes()}")
        println("Seconds: ${dateExt.getPrevious(year).Seconds()}")
        println("Years: ${dateExt.getPrevious(year).Years()}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun demonstrateGetAhead() {
        // Example 1: Add time to current date
        println("\nFuture dates from now:")
        println("5 months ahead: ${dateExt.getAhead(5).Months()}")
        println("2 weeks ahead: ${dateExt.getAhead(2).Weeks()}")
        println("7 days ahead: ${dateExt.getAhead(7).Days()}")
        println("24 hours ahead: ${dateExt.getAhead(24).Hours()}")
        println("60 minutes ahead: ${dateExt.getAhead(60).Minutes()}")
        println("3600 seconds ahead: ${dateExt.getAhead(3600).Seconds()}")
        println("5 years ahead: ${dateExt.getAhead(5).Years()}")

        // Example 2: Add time to specific date
        val specificDate = LocalDate.of(2024, 1, 1)
        println("\nFuture dates from $specificDate:")
        println("3 months ahead: ${dateExt.getAhead(3, specificDate).Months()}")
        println("4 weeks ahead: ${dateExt.getAhead(4, specificDate).Weeks()}")
        println("5 years ahead: ${dateExt.getAhead(5, specificDate).Years()}")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun demonstrateGetCurrent() {
        // Example 1: Get current date information
        println("\nCurrent date information:")
        println("Year: ${dateExt.getCurrent().Year()}")
        println("Quarter: ${dateExt.getCurrent().Quarter()}")
        println("Month: ${dateExt.getCurrent().Month()}")
        println("Week: ${dateExt.getCurrent().Week()}")
        println("Day of Year: ${dateExt.getCurrent().Day()}")
        println("Years since 1970: ${dateExt.getCurrent().Years()}")
        
        // Example 2: Get formatted dates
        println("\nFormatted dates:")
        println("Default format: ${dateExt.getCurrent().Date()}")
        println("Custom format (dd/MM/yyyy): ${dateExt.getCurrent().Date("dd/MM/yyyy")}")
        println("Custom format (MMMM dd, yyyy): ${dateExt.getCurrent().Date("MMMM dd, yyyy")}")

        // Example 3: Get information for specific date
        val specificDate = LocalDate.of(2024, 1, 1)
        println("\nInformation for $specificDate:")
        println("Quarter: ${dateExt.getCurrent(specificDate).Quarter()}")
        println("Week of Year: ${dateExt.getCurrent(specificDate).Week()}")
        println("Day of Year: ${dateExt.getCurrent(specificDate).Day()}")
        println("Years since 1970: ${dateExt.getCurrent(specificDate).Years()}")
    }
    @RequiresApi(Build.VERSION_CODES.O)
    // Main function to run all examples
    fun runAllExamples() {
        println("=== TPDateExt Examples ===\n")
        println("1. Previous Time Calculations:")
        demonstrateGetPrevious()
        
        println("\n2. Ahead Time Calculations:")
        demonstrateGetAhead()
        
        println("\n3. Current Time Information:")
        demonstrateGetCurrent()
    }
}
@RequiresApi(Build.VERSION_CODES.O)
// Example usage:
fun main() {
    val example = TPDateExtExample()
    example.runAllExamples()
}
