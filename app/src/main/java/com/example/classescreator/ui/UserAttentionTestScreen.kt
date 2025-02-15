package com.example.classescreator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kotlinclasses.TPAppLanguage
import com.kotlinclasses.TPAppLanguageImpl
import com.kotlinclasses.TPUserAttention

import com.kotlinclasses.TPUserAttentionDelegateImpl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAttentionTestScreen() {
    var showSomeErrorHappened by remember { mutableStateOf(false) }
    var showUnAvailable by remember { mutableStateOf(false) }
    var showSuccessfullySaved by remember { mutableStateOf(false) }
    var showPasswordIsNotCorrect by remember { mutableStateOf(false) }
    var showThisIsAlreadyExist by remember { mutableStateOf(false) }
    var showFailToSave by remember { mutableStateOf(false) }
    var showSentSuccessfully by remember { mutableStateOf(false) }
    var showDeletedSuccessfully by remember { mutableStateOf(false) }
    var showPasswordsAreNotMatched by remember { mutableStateOf(false) }
    var showFailToRetrieveDataFromServer by remember { mutableStateOf(false) }
    var showMakeSureNetIsConnected by remember { mutableStateOf(false) }
    var showImageSizeIsNotSuitable by remember { mutableStateOf(false) }
    var showBalanceIsNotEnough by remember { mutableStateOf(false) }
    var showNeedToAllowAccessToPhotoLibrary by remember { mutableStateOf(false) }

    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val appLanguageDelegate = remember { TPAppLanguageImpl(context) }
    val appLanguage = remember { TPAppLanguage(appLanguageDelegate) }
    val userAttentionDelegate = remember { TPUserAttentionDelegateImpl(appLanguage) }
    var selectedLanguage by remember { mutableStateOf(appLanguage.getAllAvailableLanguages().first()) }
    var updateTrigger by remember { mutableStateOf(0) }

    val userAttention = remember { 
        TPUserAttention(
            delegate = userAttentionDelegate,
            appLanguage = appLanguage
        )
    }

    userAttention.Dialog()

    if (showSomeErrorHappened) {
        userAttention.displaySomeErrorHappened()
        showSomeErrorHappened = false
    }
    if (showUnAvailable) {
        userAttention.displayUnAvailable()
        showUnAvailable = false
    }
    if (showSuccessfullySaved) {
        userAttention.displaySuccessfullySaved()
        showSuccessfullySaved = false
    }
    if (showPasswordIsNotCorrect) {
        userAttention.displayPasswordIsNotCorrect()
        showPasswordIsNotCorrect = false
    }
    if (showThisIsAlreadyExist) {
        userAttention.displayThisIsAlreadyExist()
        showThisIsAlreadyExist = false
    }
    if (showFailToSave) {
        userAttention.displayFailToSave()
        showFailToSave = false
    }
    if (showSentSuccessfully) {
        userAttention.displaySentSuccessfully()
        showSentSuccessfully = false
    }
    if (showDeletedSuccessfully) {
        userAttention.displayDeletedSuccessfully()
        showDeletedSuccessfully = false
    }
    if (showPasswordsAreNotMatched) {
        userAttention.displayPasswordsAreNotMatched()
        showPasswordsAreNotMatched = false
    }
    if (showFailToRetrieveDataFromServer) {
        userAttention.displayFailToRetrieveDataFromServer()
        showFailToRetrieveDataFromServer = false
    }
    if (showMakeSureNetIsConnected) {
        userAttention.displayMakeSureNetIsConnected()
        showMakeSureNetIsConnected = false
    }
    if (showImageSizeIsNotSuitable) {
        userAttention.displayImageSizeIsNotSuitable()
        showImageSizeIsNotSuitable = false
    }
    if (showBalanceIsNotEnough) {
        userAttention.displayBalanceIsNotEnough()
        showBalanceIsNotEnough = false
    }
    if (showNeedToAllowAccessToPhotoLibrary) {
        userAttention.displayYouNeedToAllowAccessToPhotoLibrary {}
        showNeedToAllowAccessToPhotoLibrary = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(userAttentionDelegate.getAttentionVocab()) },
                actions = {
                    Box {
                        IconButton(onClick = { expanded = true }) {
                            Text(selectedLanguage)
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            appLanguage.getAllAvailableLanguages().forEach { language ->
                                DropdownMenuItem(
                                    text = { Text(language) },
                                    onClick = {
                                        selectedLanguage = language
                                        appLanguage.updateLanguageCode(language)
                                        expanded = false
                                        updateTrigger++ // تحديث الواجهة
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            key(updateTrigger) {
                Button(
                    onClick = { showSomeErrorHappened = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getSomeErrorHappenedVocab())
                }

                Button(
                    onClick = { showUnAvailable = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getItemUnAvailableVocab())
                }

                Button(
                    onClick = { showSuccessfullySaved = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getSuccessfullySavedVocab())
                }

                Button(
                    onClick = { showPasswordIsNotCorrect = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getPasswordIsNotCorrectVocab())
                }

                Button(
                    onClick = { showThisIsAlreadyExist = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getAlreadyExistVocab())
                }

                Button(
                    onClick = { showFailToSave = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getFailToSaveVocab())
                }

                Button(
                    onClick = { showSentSuccessfully = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getSentSuccessfullyVocab())
                }

                Button(
                    onClick = { showDeletedSuccessfully = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getDeletedSuccessfullyVocab())
                }

                Button(
                    onClick = { showPasswordsAreNotMatched = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getPasswordsAreNotMatchedVocab())
                }

                Button(
                    onClick = { showFailToRetrieveDataFromServer = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getFailToRetrieveDataFromServerVocab())
                }

                Button(
                    onClick = { showMakeSureNetIsConnected = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getMakeSureConnectedToNetVocab())
                }

                Button(
                    onClick = { showImageSizeIsNotSuitable = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getImageSizeIsNotSuitableVocab())
                }

                Button(
                    onClick = { showBalanceIsNotEnough = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getBalanceIsNotEnoughVocab())
                }

                Button(
                    onClick = { showNeedToAllowAccessToPhotoLibrary = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(userAttentionDelegate.getNeedToAllowAccessingYourPhotoLibraryVocab())
                }

                Button(
                    onClick = {
                        userAttention.displayInputDialog(
                            title = (userAttentionDelegate as TPUserAttentionDelegateImpl).getEnterNameVocab(),
                            message = (userAttentionDelegate as TPUserAttentionDelegateImpl).getNameVocab(),
                            onConfirm = { name ->
                                userAttention.displaySuccessfullySaved()
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text((userAttentionDelegate as TPUserAttentionDelegateImpl).getEnterNameVocab())
                }

                Button(
                    onClick = {
                        userAttention.displayConfirmDialog(
                            title = (userAttentionDelegate as TPUserAttentionDelegateImpl).getConfirmDeleteVocab(),
                            message = (userAttentionDelegate as TPUserAttentionDelegateImpl).getDeleteConfirmMessageVocab(),
                            onAgree = {
                                userAttention.displayDeletedSuccessfully()
                            },
                            onCancel = {}
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text((userAttentionDelegate as TPUserAttentionDelegateImpl).getDeleteVocab())
                }

                Button(
                    onClick = {
                        userAttention.displayWarningWithCancel(
                            message = (userAttentionDelegate as TPUserAttentionDelegateImpl).getDeleteConfirmMessageVocab(),
                            onAgree = {
                                userAttention.displayDeletedSuccessfully()
                            },
                            onCancel = {}
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text((userAttentionDelegate as TPUserAttentionDelegateImpl).getAttentionVocab())
                }

                Button(
                    onClick = {
                        userAttention.displayWarningWithCancel(
                            message = (userAttentionDelegate as TPUserAttentionDelegateImpl).getDeleteConfirmMessageVocab(),
                            onAgree = {
                                userAttention.displayDeletedSuccessfully()
                            },
                            onCancel = {}
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text((userAttentionDelegate as TPUserAttentionDelegateImpl).getConfirmDeleteVocab())
                }

                Button(
                    onClick = {
                        userAttention.showDialog(
                            title = (userAttentionDelegate as TPUserAttentionDelegateImpl).getAttentionVocab(),
                            message = (userAttentionDelegate as TPUserAttentionDelegateImpl).getMakeSureConnectedToNetVocab()
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text((userAttentionDelegate as TPUserAttentionDelegateImpl).getMakeSureConnectedToNetVocab())
                }

                Button(
                    onClick = {
                        userAttention.showDialog(
                            title = (userAttentionDelegate as TPUserAttentionDelegateImpl).getAttentionVocab(),
                            message = (userAttentionDelegate as TPUserAttentionDelegateImpl).getNeedToAllowAccessingYourPhotoLibraryVocab()
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text((userAttentionDelegate as TPUserAttentionDelegateImpl).getNeedToAllowAccessingYourPhotoLibraryVocab())
                }
            }
        }
    }
}
