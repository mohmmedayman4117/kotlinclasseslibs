package com.example.classescreator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

import com.google.firebase.database.DatabaseReference
import com.kotlinclasses.TPFirebaseChat
import com.kotlinclasses.TPFirebaseChatImpl
import com.kotlinclasses.TPFirebaseChatTextInterface
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    appName: String,
    databaseReference: DatabaseReference,
    chatter1ID: String,
    chatter2ID: String,
    chatter1Name: String = "المستخدم 1",
    chatter2Name: String = "المستخدم 2"
) {
    var messages by remember { mutableStateOf(listOf<TPFirebaseChatTextInterface>()) }
    var messageText by remember { mutableStateOf("") }
    var isChatter1Online by remember { mutableStateOf(false) }
    var isChatter2Online by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // إنشاء كائن المحادثة
    val chat = remember {
        val chatImpl = TPFirebaseChatImpl(
            chatter1ID = chatter1ID,
            chatter2ID = chatter2ID,
            texts = emptyList(),
            isChatter1Online = false,
            isChatter2Online = false
        )
        TPFirebaseChat(appName, databaseReference, chatImpl)
    }

    // بدء الاستماع للرسائل عند بدء الشاشة
//    DisposableEffect(chat) {
//        chat.startListening()
//        chat.updateOnlineStatus(true)
//        onDispose {
//            chat.updateOnlineStatus(false)
//            chat.stopListening()
//        }
//    }

    // مراقبة الرسائل الجديدة
    LaunchedEffect(chat) {
        chat.observeChat { message ->
            message?.let {
                messages = messages + it
                coroutineScope.launch {
                    listState.animateScrollToItem(messages.size - 1)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // شريط العنوان
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = if (chatter1ID == chat.getLiveChat().chatter1ID) chatter2Name else chatter1Name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (chatter1ID == chat.getLiveChat().chatter1ID) {
                            if (isChatter2Online) "متصل" else "غير متصل"
                        } else {
                            if (isChatter1Online) "متصل" else "غير متصل"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isChatter2Online) Color.Green else Color.Gray
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        // قائمة الرسائل
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            state = listState,
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages) { message ->
                val isCurrentUser = message.sender == chatter1ID
                MessageBubble(
                    message = message,
                    isCurrentUser = isCurrentUser,
                    senderName = if (isCurrentUser) chatter1Name else chatter2Name
                )
            }
        }

        // حقل إدخال الرسالة
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                placeholder = { Text("اكتب رسالة...") },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            IconButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        chat.sendMessage(messageText, chatter1ID)
                        messageText = ""
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    Icons.Default.Send,
                    contentDescription = "إرسال",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun MessageBubble(
    message: TPFirebaseChatTextInterface,
    isCurrentUser: Boolean,
    senderName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = senderName,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Surface(
            shape = RoundedCornerShape(
                topStart = if (isCurrentUser) 16.dp else 0.dp,
                topEnd = if (isCurrentUser) 0.dp else 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            color = if (isCurrentUser) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .widthIn(max = 280.dp)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.text,
                    color = if (isCurrentUser) 
                        MaterialTheme.colorScheme.onPrimaryContainer 
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = message.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isCurrentUser) 
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    else 
                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
