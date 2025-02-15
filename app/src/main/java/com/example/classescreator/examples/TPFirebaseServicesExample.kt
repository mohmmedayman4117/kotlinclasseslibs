package com.example.classescreator.examples

import com.kotlinclasses.TPFirebaseInstanceDataInterface
import com.kotlinclasses.TPFirebaseInstanceFields
import com.kotlinclasses.TPFirebaseServices


// Example of a simple user class implementing TPFirebaseInstanceFields
data class UserProfile(
    override val ID: String,
    override var classStructName: String = "UserProfile",
    val name: String,
    val email: String,
    val age: Int,
    val isActive: Boolean = true
) : TPFirebaseInstanceFields

// Example of a more complex class implementing TPFirebaseInstanceDataInterface
data class ChatMessage(
    override val ID: String,
    override var classStructName: String = "ChatMessage",
    val message: String,
    val timestamp: Long,
    val sender: UserProfile,  // Nested TPFirebaseInstanceFields object
    val recipients: List<UserProfile>, // List of TPFirebaseInstanceFields objects
    val attachments: List<String>? = null // Nullable field
) : TPFirebaseInstanceDataInterface {
    override fun convertToOriginalInstance(dictionary: Map<String, Any>): Any {
        // Implementation of conversion from dictionary to ChatMessage
        return this
    }

    override fun convertToMultiOriginalInstances(dictionaries: List<Map<String, Any>>): List<Any> {
        // Implementation of conversion from list of dictionaries to list of ChatMessages
        return listOf(this)
    }

    override fun toMap(): Map<String, Any?> {
        return mapOf(
            "ID" to ID,
            "classStructName" to classStructName,
            "message" to message,
            "timestamp" to timestamp,
            "sender" to sender,
            "recipients" to recipients,
            "attachments" to attachments
        )
    }
}

object TPFirebaseServicesExample {
    fun demonstrateUsage() {
        // Create sample user profiles
        val user1 = UserProfile(
            ID = "user1",
            name = "John Doe",
            email = "john@example.com",
            age = 30
        )

        val user2 = UserProfile(
            ID = "user2",
            name = "Jane Smith",
            email = "jane@example.com",
            age = 28
        )

        // Create a sample chat message
        val chatMessage = ChatMessage(
            ID = "msg1",
            message = "Hello everyone!",
            timestamp = System.currentTimeMillis(),
            sender = user1,
            recipients = listOf(user2),
            attachments = listOf("image1.jpg", "document.pdf")
        )

        // Demonstrate converting single instances to dictionary
        val user1Dict = TPFirebaseServices.convertToDictionary(user1)
        println("User 1 Dictionary:")
        user1Dict?.forEach { (key, value) -> println("$key: $value") }

        val messageDict = TPFirebaseServices.convertToDictionary(chatMessage)
        println("\nChat Message Dictionary:")
        messageDict?.forEach { (key, value) -> println("$key: $value") }

        // Demonstrate converting list of instances to dictionary list
        val usersList = listOf(user1, user2)
        val usersListDict = TPFirebaseServices.convertMultiInstancesToDictionaryList(usersList)
        println("\nUsers List Dictionary:")
        usersListDict.forEachIndexed { index, dict ->
            println("User $index:")
            dict?.forEach { (key, value) -> println("  $key: $value") }
        }
    }

    // Example of how to use the converted data with Firebase
    fun saveToFirebase() {
        val user = UserProfile(
            ID = "user123",
            name = "Alice Johnson",
            email = "alice@example.com",
            age = 25
        )

        // Convert to dictionary
        val userDict = TPFirebaseServices.convertToDictionary(user)

        // Here you would typically save to Firebase
        // Example (commented out as it requires Firebase setup):
        /*
        val database = FirebaseDatabase.getInstance()
        val usersRef = database.getReference("users")
        userDict?.let { dict ->
            usersRef.child(user.ID).setValue(dict)
        }
        */
    }
}

// Example of how to run the demonstrations
fun main() {
    TPFirebaseServicesExample.demonstrateUsage()
    // TPFirebaseServicesExample.saveToFirebase() // Uncomment when Firebase is set up
}
