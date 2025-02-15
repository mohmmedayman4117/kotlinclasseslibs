package com.example.classescreator.examples

import com.example.classescreator.*
import com.google.firebase.database.FirebaseDatabase
import com.kotlinclasses.TPFirebaseChat
import com.kotlinclasses.TPFirebaseChatInterface
import com.kotlinclasses.TPFirebaseChatTextImpl
import com.kotlinclasses.TPFirebaseChatTextInterface
import com.kotlinclasses.TPLogger
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class TPFirebaseChatExamples {
    private lateinit var firebaseChat: TPFirebaseChat
    private val user1Id = "Hu1"
    private val user2Id = "Ma2"

    private fun initializeChat() {
        val chatImpl = object : TPFirebaseChatInterface {
            override val chatter1ID: String = user1Id
            override val chatter2ID: String = user2Id
            override var texts: List<TPFirebaseChatTextInterface> = emptyList()
            override var isChatter1Online: Boolean = true
            override var isChatter2Online: Boolean = true

            override fun onMessagesUpdated(messages: List<TPFirebaseChatTextInterface>) {
                println("\n=== تحديث الرسائل ===")
                TPLogger.i("\n=== تحديث الرسائل ===")
                if (messages.isEmpty()) {
                    println("لا توجد رسائل")
                    TPLogger.i("لا توجد رسائل")
                } else {
                    messages.forEach { message ->
                        val alignment = if (message.sender == user1Id) "◀" else "▶"
                        println("$alignment [${message.sender}]: ${message.text}")
                        println("   الوقت: ${message.timestamp}")
                        println("   المعرف: ${message.ID}")
                        println("--------------------------------")
                        TPLogger.i("$alignment [${message.sender}]: ${message.text}")
                        TPLogger.d("   الوقت: ${message.timestamp}")
                        TPLogger.d("   المعرف: ${message.ID}")
                        TPLogger.d("--------------------------------")
                    }
                }
                texts = messages
            }

            override fun onError(error: String) {
                println("حدث خطأ: $error")
                TPLogger.e("حدث خطأ: $error")
            }
        }

        firebaseChat = TPFirebaseChat(
            appName = "NewTester",
            databaseReference = FirebaseDatabase.getInstance().reference,
            cht_LiveChat = chatImpl,
            classStructName = "Chatting"
        )
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.getDefault()).format(Date())
    }

    fun Chat() {
        println("\n=== بدء عرض سجل المحادثة الكامل ===")
        TPLogger.i("\n=== بدء عرض سجل المحادثة الكامل ===")

        try {
            println("تهيئة المحادثة...")
            TPLogger.d("تهيئة المحادثة...")
            initializeChat()

            firebaseChat.startListening()

            println("\nإرسال رسائل المحادثة...")
            TPLogger.i("\nإرسال رسائل المحادثة...")

            val currentTime = getCurrentTime()
            val messages = listOf(
                TPFirebaseChatTextImpl(
                    messageId = "0",
                    text = "I will come to you",
                    sender = user1Id,
                    timestamp = currentTime,
                    classStructName = "Texture"
                ),
                TPFirebaseChatTextImpl(
                    messageId = "1",
                    text = "Ok go ahead",
                    sender = user2Id,
                    timestamp = currentTime,
                    classStructName = "Texture"
                ),
                TPFirebaseChatTextImpl(
                    messageId = "2",
                    text = "Ok baby let's do it",
                    sender = user1Id,
                    timestamp = currentTime,
                    classStructName = "Texture"
                ),
                TPFirebaseChatTextImpl(
                    messageId = "3",
                    text = "I am ready",
                    sender = user2Id,
                    timestamp = currentTime,
                    classStructName = "Texture"
                )
            )

            messages.forEach { message ->
                firebaseChat.sendMessage(message.text, message.sender,"Texture")
                println("تم إرسال الرسالة: ${message.text}")
                TPLogger.d("تم إرسال الرسالة: ${message.text}")
                Thread.sleep(500)
            }

            Thread.sleep(3000)

            println("\nجاري استرجاع المحادثات...")
            TPLogger.d("\nجاري استرجاع المحادثات...")
            firebaseChat.getLiveChat()

            Thread.sleep(5000)

            firebaseChat.stopListening()

        } catch (e: Exception) {
            println("حدث خطأ أثناء تنفيذ العملية: ${e.message}")
            TPLogger.e("حدث خطأ أثناء تنفيذ العملية: ${e.message}")
            e.printStackTrace()
        }

        println("=== انتهى عرض سجل المحادثة ===\n")
        TPLogger.i("=== انتهى عرض سجل المحادثة ===\n")
    }

    fun runAllExamples() {
        println("=== بدء أمثلة الدردشة ===")
        TPLogger.i("=== بدء أمثلة الدردشة ===")
        Chat()
        println("=== اكتملت جميع الأمثلة ===")
        TPLogger.i("=== اكتملت جميع الأمثلة ===")
    }
}
