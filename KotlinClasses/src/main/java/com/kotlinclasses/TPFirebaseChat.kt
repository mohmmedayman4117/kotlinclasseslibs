package com.kotlinclasses

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

interface TPFirebaseChatInterface {
    val chatter1ID: String
    val chatter2ID: String
    var texts: List<TPFirebaseChatTextInterface>
    var isChatter1Online: Boolean
    var isChatter2Online: Boolean
    fun onMessagesUpdated(messages: List<TPFirebaseChatTextInterface>)
    fun onError(error: String)
}

interface TPFirebaseChatTextInterface : TPFirebaseInstanceDataInterface {
    val text: String
    val sender: String
    val timestamp: String
}

data class TPFirebaseChatTextImpl(
    val messageId: String = "",
    override val text: String = "",
    override val sender: String = "",
    override val timestamp: String = "",
    override var classStructName: String = "chat_messages"
) : TPFirebaseChatTextInterface {
    override val ID: String
        get() = messageId.ifEmpty {
            "${timestamp}_${sender}_${UUID.randomUUID()}"
        }

    // Default constructor for Firebase
    constructor() : this("", "", "", "", "chat_messages")
}

data class TPFirebaseChatImpl(
    override val chatter1ID: String,
    override val chatter2ID: String,
    override var texts: List<TPFirebaseChatTextInterface>,
    override var isChatter1Online: Boolean,
    override var isChatter2Online: Boolean
) : TPFirebaseChatInterface {
    override fun onMessagesUpdated(messages: List<TPFirebaseChatTextInterface>) {
        texts = messages
    }

    override fun onError(error: String) {
        // Handle error in implementation
    }

    // Default constructor for Firebase
    constructor() : this("", "", emptyList(), false, false)
}

class TPFirebaseChat(
    appName: String,
    databaseReference: DatabaseReference,
    private val cht_LiveChat: TPFirebaseChatInterface,
    private val classStructName: String = "Chatting",
    private val chatId: String = "default_chat" // إضافة معرف المحادثة
) : TPFirebaseDatabase(appName, databaseReference, null) {

    private lateinit var chatListener: ValueEventListener
    private lateinit var databaseRef: DatabaseReference

    init {
        databaseRef = databaseReference.child(appName).child(classStructName)
    }

    fun startListening() {
        val chatId = "${cht_LiveChat.chatter1ID}${cht_LiveChat.chatter2ID}"
        chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val messages = mutableListOf<TPFirebaseChatTextInterface>()
                    val chatSnapshot = snapshot.child(chatId)
                    
                    // تحديث حالة الاتصال
                    cht_LiveChat.isChatter1Online = chatSnapshot.child("isChatter1Online").getValue(Boolean::class.java) ?: false
                    cht_LiveChat.isChatter2Online = chatSnapshot.child("isChatter2Online").getValue(Boolean::class.java) ?: false

                    // معالجة الرسائل
                    val textsSnapshot = chatSnapshot.child("texts")
                    if (textsSnapshot.value is List<*>) {
                        // إذا كانت الرسائل في شكل مصفوفة
                        textsSnapshot.children.forEach { messageSnapshot ->
                            createMessageFromSnapshot(messageSnapshot)?.let { messages.add(it) }
                        }
                    } else {
                        // إذا كانت الرسائل في شكل map
                        textsSnapshot.children.forEach { messageSnapshot ->
                            createMessageFromSnapshot(messageSnapshot)?.let { messages.add(it) }
                        }
                    }
                    
                    cht_LiveChat.onMessagesUpdated(messages)
                } catch (e: Exception) {
                    cht_LiveChat.onError("Error loading messages: ${e.message}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                cht_LiveChat.onError(error.message)
            }
        }
        databaseRef.addValueEventListener(chatListener)
    }

    private fun createMessageFromSnapshot(snapshot: DataSnapshot): TPFirebaseChatTextInterface? {
        return try {
            val id = snapshot.child("ID").getValue(String::class.java) ?: ""
            val text = snapshot.child("text").getValue(String::class.java) ?: ""
            val sender = snapshot.child("chatterID").getValue(String::class.java) ?: ""
            val time = snapshot.child("time").getValue(String::class.java) ?: ""
            val classStructName = snapshot.child("classStructName").getValue(String::class.java) ?: "Texture"

            TPFirebaseChatTextImpl(
                messageId = id,
                text = text,
                sender = sender,
                timestamp = time,
                classStructName = classStructName
            )
        } catch (e: Exception) {
            null
        }
    }

    fun stopListening() {
        if (::chatListener.isInitialized) {
            databaseRef.removeEventListener(chatListener)
        }
    }

    fun sendMessage(text: String, sender: String,classStructNameMessage :String = classStructName) {
        val chatId = "${cht_LiveChat.chatter1ID}${cht_LiveChat.chatter2ID}"
        val timestamp = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(Date())
        val messageId = UUID.randomUUID().toString()

        val message = mapOf(
            "ID" to messageId, 
            "chatId" to chatId,
            "chatterID" to sender,
            "classStructName" to classStructNameMessage ,
            "text" to text,
            "time" to timestamp
        )

        val chatRef = databaseRef.child(chatId)
        val newMessageRef = chatRef.child("texts").push()
        newMessageRef.setValue(message)

        // تحديث معلومات المحادثة
        chatRef.updateChildren(mapOf(
            "ID" to chatId,
            "chatter1ID" to cht_LiveChat.chatter1ID,
            "chatter2ID" to cht_LiveChat.chatter2ID,
            "classStructName" to classStructName,
            "isChatter1Online" to cht_LiveChat.isChatter1Online,
            "isChatter2Online" to cht_LiveChat.isChatter2Online
        ))
    }
//fun sendMessage(text: String, sender: String) {
//    val chatId = "${cht_LiveChat.chatter1ID}${cht_LiveChat.chatter2ID}"
//    val timestamp = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(Date())
//
//    val message = mapOf(
//        "ID" to chatId,
//        "chatterID" to sender,
//        "classStructName" to "Texture",
//        "text" to text,
//        "time" to timestamp
//    )
//
//    val chatRef = databaseRef.child(chatId).child("texts")
//
//    chatRef.get().addOnSuccessListener { snapshot ->
//        val messagesList = snapshot.value as? MutableList<Map<String, Any>> ?: mutableListOf()
//        messagesList.add(message)
//        chatRef.setValue(messagesList)
//        val chatInfoRef = databaseRef.child(chatId)
//        chatInfoRef.updateChildren(mapOf(
//            "ID" to chatId,
//            "chatter1ID" to cht_LiveChat.chatter1ID,
//            "chatter2ID" to cht_LiveChat.chatter2ID,
//            "classStructName" to "Chatting",
//            "isChatter1Online" to cht_LiveChat.isChatter1Online,
//            "isChatter2Online" to cht_LiveChat.isChatter2Online
//        ))
//    }
//}




    /**
     * Example usage of sendChat:
     * ```
     * // Create a message object
     * val message = TPFirebaseChatTextImpl(
     *     text = "مرحباً!",
     *     sender = "user123",
     *     timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date()),
     *     classStructName = "ChatMessage"
     * )
     * 
     * // Send the message
     * chat.sendChat(message) { success ->
     *     if (success) {
     *         println("تم إرسال الرسالة بنجاح")
     *     } else {
     *         println("فشل في إرسال الرسالة")
     *     }
     * }
     * ```
     */
    fun sendChat(text: TPFirebaseChatTextInterface, status: (Boolean) -> Unit) {
        try {
            // إنشاء مرجع للمحادثة
            val chatRef = databaseRef.child("${cht_LiveChat.chatter1ID}_${cht_LiveChat.chatter2ID}")

            // إضافة الرسالة الجديدة
            val messageRef = chatRef.child("messages").push()
            messageRef.setValue(mapOf(
                "chatterID" to text.sender,
                "time" to text.timestamp,
                "text" to text.text
            )).addOnCompleteListener { task ->
                status(task.isSuccessful)
            }
        } catch (e: Exception) {
            status(false)
        }
    }

    fun observeChat(getText: (TPFirebaseChatTextInterface?) -> Unit) {
        val chatRef = databaseRef.child("${cht_LiveChat.chatter1ID}_${cht_LiveChat.chatter2ID}")
            .child("messages")

        chatListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.lastOrNull()?.let { messageSnapshot ->
                    val message = messageSnapshot.getValue(TPFirebaseChatTextImpl::class.java)
                    getText(message)
                } ?: getText(null)
            }

            override fun onCancelled(error: DatabaseError) {
                getText(null)
            }
        }

        chatRef.addValueEventListener(chatListener)
    }

    fun getLiveChat(): TPFirebaseChatInterface {
        return cht_LiveChat
    }

    fun getPreviousChats(IDs: List<String>, getResults: (List<TPFirebaseChatInterface?>) -> Unit) {
        val results = mutableListOf<TPFirebaseChatInterface?>()
        var completedQueries = 0

        IDs.forEach { chatId ->
            databaseRef.child(chatId).get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatData = task.result.getValue(TPFirebaseChatImpl::class.java)
                    results.add(chatData)
                } else {
                    results.add(null)
                }

                completedQueries++
                if (completedQueries == IDs.size) {
                    getResults(results)
                }
            }
        }
    }

    private fun updateOnlineStatus(isOnline: Boolean) {
        val currentUserId = cht_LiveChat.chatter1ID
        val chatRef = databaseRef.child("${cht_LiveChat.chatter1ID}_${cht_LiveChat.chatter2ID}")

        if (currentUserId == cht_LiveChat.chatter1ID) {
            chatRef.child("isChatter1Online").setValue(isOnline)
        } else {
            chatRef.child("isChatter2Online").setValue(isOnline)
        }
    }

    fun deleteMessage(messageId: String, callback: (Boolean) -> Unit) {
        try {
            val chatId = "${cht_LiveChat.chatter1ID}${cht_LiveChat.chatter2ID}"
            val chatRef = databaseRef.child(chatId).child("texts")
            
            chatRef.get().addOnSuccessListener { snapshot ->
                var messageKey: String? = null
                snapshot.children.forEach { child ->
                    val id = child.child("ID").getValue(String::class.java)
                    if (id == messageId) {
                        messageKey = child.key
                        return@forEach
                    }
                }
                
                if (messageKey != null) {
                    chatRef.child(messageKey!!).removeValue().addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentMessages = cht_LiveChat.texts.toMutableList()
                            val updatedMessages = currentMessages.filterNot { it.ID == messageId }
                            cht_LiveChat.onMessagesUpdated(updatedMessages)
                            callback(true)
                        } else {
                            callback(false)
                            cht_LiveChat.onError("Failed to delete message")
                        }
                    }
                } else {
                    callback(false)
                    cht_LiveChat.onError("Message not found")
                }
            }.addOnFailureListener { e ->
                callback(false)
                cht_LiveChat.onError("Error finding message: ${e.message}")
            }
        } catch (e: Exception) {
            callback(false)
            cht_LiveChat.onError("Error deleting message: ${e.message}")
        }
    }

    fun updateMessage(messageId: String, newText: String, callback: (Boolean) -> Unit) {
        try {
            val chatId = "${cht_LiveChat.chatter1ID}${cht_LiveChat.chatter2ID}"
            val chatRef = databaseRef.child(chatId).child("texts")
            
            chatRef.get().addOnSuccessListener { snapshot ->
                var messageKey: String? = null
                var oldMessage: Map<String, Any>? = null
                
                snapshot.children.forEach { child ->
                    val id = child.child("ID").getValue(String::class.java)
                    if (id == messageId) {
                        messageKey = child.key
                        oldMessage = child.value as? Map<String, Any>
                        return@forEach
                    }
                }
                
                if (messageKey != null && oldMessage != null) {
                    val updatedMessage = oldMessage!!.toMutableMap().apply {
                        put("text", newText)
                        // put("time", timestamp)
                    }
                    
                    chatRef.child(messageKey!!).updateChildren(updatedMessage).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val currentMessages = cht_LiveChat.texts.toMutableList()
                            val messageIndex = currentMessages.indexOfFirst { it.ID == messageId }
                            if (messageIndex != -1) {
                                val oldMessageObj = currentMessages[messageIndex]
                                val updatedMessageObj = TPFirebaseChatTextImpl(
                                    messageId = messageId,
                                    text = newText,
                                    sender = oldMessageObj.sender,
                                    timestamp = oldMessageObj.timestamp, // نستخدم نفس الوقت القديم
                                    classStructName = oldMessageObj.classStructName
                                )
                                currentMessages[messageIndex] = updatedMessageObj
                                cht_LiveChat.onMessagesUpdated(currentMessages)
                            }
                            callback(true)
                        } else {
                            callback(false)
                            cht_LiveChat.onError("Failed to update message")
                        }
                    }
                } else {
                    callback(false)
                    cht_LiveChat.onError("Message not found")
                }
            }.addOnFailureListener { e ->
                callback(false)
                cht_LiveChat.onError("Error finding message: ${e.message}")
            }
        } catch (e: Exception) {
            callback(false)
            cht_LiveChat.onError("Error updating message: ${e.message}")
        }
    }

    /**
     * Deletes the entire chat conversation between two users
     * @param callback Callback function that returns true if deletion was successful, false otherwise
     */
    fun deleteEntireChat(callback: (Boolean) -> Unit) {
        try {
            val chatId = "${cht_LiveChat.chatter1ID}${cht_LiveChat.chatter2ID}"
            val chatRef = databaseRef.child(chatId)
            
            chatRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Clear local messages
                    cht_LiveChat.onMessagesUpdated(emptyList())
                    callback(true)
                } else {
                    callback(false)
                    cht_LiveChat.onError("Failed to delete chat")
                }
            }
        } catch (e: Exception) {
            callback(false)
            cht_LiveChat.onError("Error deleting chat: ${e.message}")
        }
    }
}