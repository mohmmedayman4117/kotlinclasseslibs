package com.example.classescreator.examples

import com.kotlinclasses.TPFirebaseDatabase
import com.kotlinclasses.TPFirebaseInstanceDataInterface


// Data class for Address information
data class Address(
    val street: String,
    val city: String,
    val country: String,
    val postalCode: String
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "street" to street,
        "city" to city,
        "country" to country,
        "postalCode" to postalCode
    )
}

// Data class for Contact information
data class ContactInfo(
    val email: String,
    val phone: String,
    val address: Address
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "email" to email,
        "phone" to phone,
        "address" to address.toMap()
    )
}

// Main class that implements TPFirebaseInstanceDataInterface
data class Company(
    override val ID: String,
    val name: String,
    val foundedYear: Int,
    val contactInfo: ContactInfo,
    val employees: List<String>,
    override var classStructName: String = "m/m/Companies"
) : TPFirebaseInstanceDataInterface {
    
    override fun toMap(): Map<String, Any?> = mapOf(
        "ID" to ID,
        "name" to name,
        "foundedYear" to foundedYear,
        "contactInfo" to contactInfo.toMap(),
        "employees" to employees
    )

//    override fun convertToOriginalInstance(dictionary: Map<String, Any>): Company {
//        val contactData = dictionary["contactInfo"] as Map<String, Any>
//        val addressData = contactData["address"] as Map<String, Any>
//
//        val address = Address(
//            street = addressData["street"] as String,
//            city = addressData["city"] as String,
//            country = addressData["country"] as String,
//            postalCode = addressData["postalCode"] as String
//        )
//
//        val contactInfo = ContactInfo(
//            email = contactData["email"] as String,
//            phone = contactData["phone"] as String,
//            address = address
//        )
//
//        @Suppress("UNCHECKED_CAST")
//        return Company(
//            ID = dictionary["ID"] as String,
//            name = dictionary["name"] as String,
//            foundedYear = (dictionary["foundedYear"] as Long).toInt(),
//            contactInfo = contactInfo,
//            employees = dictionary["employees"] as List<String>
//        )
//    }
}

// Example usage
fun testFirebaseObjects() {
    // Create nested objects
    val address = Address(
        street = "123 Tech Street",
        city = "Silicon Valley",
        country = "USA",
        postalCode = "94025"
    )
    
    val contactInfo = ContactInfo(
        email = "contact@techcompany.com",
        phone = "+1-555-123-4567",
        address = address
    )
    
    // Create main company object with custom path
    val company = Company(
        ID = "tech_123",
        name = "Tech Innovation Co",
        foundedYear = 2020,
        contactInfo = contactInfo,
        employees = listOf("John Doe", "Jane Smith", "Bob Johnson"),
        classStructName = "m/m/Companies" // Specify custom path here
    )
    
    // Save to Firebase
    TPFirebaseDatabase.getInstance().addObject(company) { id ->
        if (id.isNotEmpty()) {
            println("Company added successfully at path: ${company.classStructName}/\$id")
            
            // Example of reading the company back
            TPFirebaseDatabase.getInstance().read(company) { result ->
                if (result != null && result is Company) {
                    println("Company read successfully from path: ${company.classStructName}/${result.ID}")
                    println("Name: ${result.name}")
                    println("Founded: ${result.foundedYear}")
                    println("Email: ${result.contactInfo.email}")
                    println("Address: ${result.contactInfo.address.street}, ${result.contactInfo.address.city}")
                    println("Employees: ${result.employees.joinToString(", ")}")
                } else {
                    println("Failed to read company data")
                }
            }
        } else {
            println("Failed to add company")
        }
    }
}
