package com.example.classescreator.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import com.kotlinclasses.TPFirebaseChat
import com.kotlinclasses.TPFirebaseChatInterface
import com.kotlinclasses.TPFirebaseChatTextInterface
import kotlinx.coroutines.launch

@Composable
fun TPFirebaseChatScreen(
    modifier: Modifier = Modifier
) {
    var currentUser by remember { mutableStateOf("User1") }
    var messages by remember { mutableStateOf<List<TPFirebaseChatTextInterface>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var isChatter1Online by remember { mutableStateOf(true) }
    var isChatter2Online by remember { mutableStateOf(false) }


    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMessage by remember { mutableStateOf<TPFirebaseChatTextInterface?>(null) }
    var editedText by remember { mutableStateOf("") }
    var showDeleteChatDialog by remember { mutableStateOf(false) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val chatInterface = remember(currentUser) {
        object : TPFirebaseChatInterface {
            override val chatter1ID: String = "User1"
            override val chatter2ID: String = "User2"
            override var texts: List<TPFirebaseChatTextInterface> = emptyList()
            override var isChatter1Online: Boolean
                get() = isChatter1Online
                set(value) {
                    isChatter1Online = value
                }
            override var isChatter2Online: Boolean
                get() = isChatter2Online
                set(value) {
                    isChatter2Online = value
                }

            override fun onMessagesUpdated(newMessages: List<TPFirebaseChatTextInterface>) {
                val validMessages = newMessages.filter { it.ID.isNotEmpty() }
                    .distinctBy { it.ID } // تأكد من عدم تكرار الرسائل
                messages = validMessages.sortedBy { it.timestamp }
                texts = messages
                if (messages.isNotEmpty()) {
                    coroutineScope.launch {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            }

            override fun onError(errorMessage: String) {
                error = errorMessage
            }
        }
    }

    val database = FirebaseDatabase.getInstance()
    val chat = remember(currentUser) {
        TPFirebaseChat(
            appName = "NewTester",
            databaseReference = database.reference,
            cht_LiveChat = chatInterface,
            classStructName = "Chatting"
        )
    }

    DisposableEffect(currentUser) {
        chat.startListening()
        onDispose {
            chat.stopListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // User Selection and Delete Chat Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { currentUser = "User1" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentUser == "User1") 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("User 1")
                }
                Button(
                    onClick = { currentUser = "User2" },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentUser == "User2") 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("User 2")
                }
            }
            
            IconButton(
                onClick = { showDeleteChatDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف المحادثة",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }

        // Current User Display
        Text(
            text = "Current User: $currentUser",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = messages,
                key = { message -> 
                    // إنشاء مفتاح فريد لكل رسالة باستخدام معرف الرسالة والطابع الزمني
                    "${message.ID}_${message.timestamp}"
                }
            ) { message ->
                ChatMessageItem(
                    message = message,
                    isCurrentUser = message.sender == currentUser,
                    onDeleteClick = { 
                        if (message.sender == currentUser) {
                            chat.deleteMessage(message.ID) { success ->
                                if (!success) {
                                    error = "فشل في حذف الرسالة"
                                }
                            }
                        }
                    },
                    onEditClick = {
                        if (message.sender == currentUser) {
                            selectedMessage = message
                            editedText = message.text
                            showEditDialog = true
                        }
                    }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newMessage,
                onValueChange = { newMessage = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )

            Button(
                onClick = {
                    if (newMessage.isNotBlank()) {
                        chat.sendMessage(newMessage, currentUser,"Texture")
                        newMessage = ""
                    }
                }
            ) {
                Text("Send")
            }
        }
    }

    if (showEditDialog && selectedMessage != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("تعديل الرسالة") },
            text = {
                OutlinedTextField(
                    value = editedText,
                    onValueChange = { editedText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        chat.updateMessage(selectedMessage!!.ID, editedText) { success ->
                            if (!success) {
                                error = "فشل في تحديث الرسالة"
                            }
                            showEditDialog = false
                        }
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                Button(onClick = { showEditDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    if (showDeleteChatDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteChatDialog = false },
            title = { Text("حذف المحادثة") },
            text = { Text("هل أنت متأكد من حذف كل المحادثة؟") },
            confirmButton = {
                Button(
                    onClick = {
                        chat.deleteEntireChat { success ->
                            if (!success) {
                                error = "فشل في حذف المحادثة"
                            }
                            showDeleteChatDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("حذف")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteChatDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
fun ChatMessageItem(
    message: TPFirebaseChatTextInterface,
    isCurrentUser: Boolean,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = if (isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.widthIn(max = 340.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${message.sender} • ${message.timestamp}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentUser) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                
                if (isCurrentUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onEditClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "حذف",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}
