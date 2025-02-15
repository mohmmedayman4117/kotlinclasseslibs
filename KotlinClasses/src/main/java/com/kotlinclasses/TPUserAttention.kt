package com.kotlinclasses

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

class TPUserAttention(
    private val delegate: TPUserAttentionDelegate,
    private val appLanguage: TPAppLanguage
) {

    private val showDialog = mutableStateOf(false)
    private val showInputDialog = mutableStateOf(false)
    private val showConfirmDialog = mutableStateOf(false)
    private val dialogTitle = mutableStateOf("")
    private val dialogMessage = mutableStateOf("")
    private val dialogAgreeButtonText = mutableStateOf("")
    private val dialogCancelButtonText = mutableStateOf("")
    private var onAgree: () -> Unit = {}
    private var onCancel: () -> Unit = {}
    private var onConfirm: (String) -> Unit = {}

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Dialog() {
        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showDialog.value = false
                    onCancel()
                },
                title = { Text(dialogTitle.value) },
                text = { Text(dialogMessage.value) },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog.value = false
                        onAgree()
                    }) {
                        Text(dialogAgreeButtonText.value)
                    }
                },
                dismissButton = if (dialogCancelButtonText.value.isNotEmpty()) {
                    {
                        TextButton(onClick = {
                            showDialog.value = false
                            onCancel()
                        }) {
                            Text(dialogCancelButtonText.value)
                        }
                    }
                } else null
            )
        }

        if (showInputDialog.value) {
            var inputText by rememberSaveable { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = {
                    showInputDialog.value = false
                    onCancel()
                },
                title = { Text(dialogTitle.value) },
                text = {
                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        label = { Text(dialogMessage.value) }
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showInputDialog.value = false
                        onConfirm(inputText)
                    }) {
                        Text(dialogAgreeButtonText.value)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showInputDialog.value = false
                        onCancel()
                    }) {
                        Text(dialogCancelButtonText.value)
                    }
                }
            )
        }

        if (showConfirmDialog.value) {
            AlertDialog(
                onDismissRequest = {
                    showConfirmDialog.value = false
                    onCancel()
                },
                title = { Text(dialogTitle.value) },
                text = { Text(dialogMessage.value) },
                confirmButton = {
                    TextButton(onClick = {
                        showConfirmDialog.value = false
                        onAgree()
                    }) {
                        Text(dialogAgreeButtonText.value)
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showConfirmDialog.value = false
                        onCancel()
                    }) {
                        Text(dialogCancelButtonText.value)
                    }
                }
            )
        }
    }

    fun showDialog(
        title: String,
        message: String,
        agreeButtonText: String = delegate.getAgreeVocab(),
        cancelButtonText: String = delegate.getCancelVocab(),
        onAgree: () -> Unit = {},
        onCancel: () -> Unit = {}
    ) {
        dialogTitle.value = title
        dialogMessage.value = message
        dialogAgreeButtonText.value = agreeButtonText
        dialogCancelButtonText.value = cancelButtonText
        this.onAgree = onAgree
        this.onCancel = onCancel
        showDialog.value = true
    }

    private fun showInputDialog(
        title: String,
        message: String,
        agreeButtonText: String = delegate.getAgreeVocab(),
        cancelButtonText: String = delegate.getCancelVocab(),
        onConfirm: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        dialogTitle.value = title
        dialogMessage.value = message
        dialogAgreeButtonText.value = agreeButtonText
        dialogCancelButtonText.value = cancelButtonText
        this.onConfirm = onConfirm
        this.onCancel = onCancel
        showInputDialog.value = true
    }

    private fun showConfirmDialog(
        title: String,
        message: String,
        agreeButtonText: String = delegate.getAgreeVocab(),
        cancelButtonText: String = delegate.getCancelVocab(),
        onAgree: () -> Unit,
        onCancel: () -> Unit
    ) {
        dialogTitle.value = title
        dialogMessage.value = message
        dialogAgreeButtonText.value = agreeButtonText
        dialogCancelButtonText.value = cancelButtonText
        this.onAgree = onAgree
        this.onCancel = onCancel
        showConfirmDialog.value = true
    }

    fun displaySomeErrorHappened() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getSomeErrorHappenedVocab()
        )
    }

    fun displayUnAvailable() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getItemUnAvailableVocab()
        )
    }

    fun displaySuccessfullySaved() {
        showDialog(
            title = delegate.getSuccessfulVocab(),
            message = delegate.getSuccessfullySavedVocab()
        )
    }

    fun displayPasswordIsNotCorrect() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getPasswordIsNotCorrectVocab()
        )
    }

    fun displayThisIsAlreadyExist() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getAlreadyExistVocab()
        )
    }

    fun displayFailToSave() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getFailToSaveVocab()
        )
    }

    fun displaySentSuccessfully() {
        showDialog(
            title = delegate.getSuccessfulVocab(),
            message = delegate.getSentSuccessfullyVocab()
        )
    }

    fun displayDeletedSuccessfully() {
        showDialog(
            title = delegate.getSuccessfulVocab(),
            message = delegate.getDeletedSuccessfullyVocab()
        )
    }

    fun displayPasswordsAreNotMatched() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getPasswordsAreNotMatchedVocab()
        )
    }

    fun displayFailToRetrieveDataFromServer() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getFailToRetrieveDataFromServerVocab()
        )
    }

    fun displayMakeSureNetIsConnected() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getMakeSureConnectedToNetVocab()
        )
    }

    fun displayImageSizeIsNotSuitable() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getImageSizeIsNotSuitableVocab()
        )
    }

    fun displayBalanceIsNotEnough() {
        showDialog(
            title = delegate.getWrongVocab(),
            message = delegate.getBalanceIsNotEnoughVocab()
        )
    }

    fun displayYouNeedToAllowAccessToPhotoLibrary(onAgree: () -> Unit) {
        showDialog(
            title = delegate.getAttentionVocab(),
            message = delegate.getNeedToAllowAccessingYourPhotoLibraryVocab(),
            onAgree = onAgree
        )
    }

    fun displayInputDialog(
        title: String,
        message: String,
        onConfirm: (String) -> Unit
    ) {
        showInputDialog(
            title = title,
            message = message,
            onConfirm = onConfirm
        )
    }

    fun displayConfirmDialog(
        title: String,
        message: String,
        onAgree: () -> Unit,
        onCancel: () -> Unit
    ) {
        showConfirmDialog(
            title = title,
            message = message,
            onAgree = onAgree,
            onCancel = onCancel
        )
    }

    fun displayWarningWithCancel(
        message: String,
        onAgree: () -> Unit = {},
        onCancel: () -> Unit = {}
    ) {
        showDialog(
            title = delegate.getAttentionVocab(),
            message = message,
            agreeButtonText = delegate.getAgreeVocab(),
            cancelButtonText = delegate.getCancelVocab(),
            onAgree = onAgree,
            onCancel = onCancel
        )
    }

}

interface TPUserAttentionDelegate {
    fun getWrongVocab(): String
    fun getItemUnAvailableVocab(): String
    fun getSomeErrorHappenedVocab(): String
    fun getAgreeVocab(): String
    fun getCancelVocab(): String
    fun getSuccessfulVocab(): String
    fun getSuccessfullySavedVocab(): String
    fun getPasswordIsNotCorrectVocab(): String
    fun getAlreadyExistVocab(): String
    fun getFailToSaveVocab(): String
    fun getSentSuccessfullyVocab(): String
    fun getDeletedSuccessfullyVocab(): String
    fun getPasswordsAreNotMatchedVocab(): String
    fun getFailToRetrieveDataFromServerVocab(): String
    fun getMakeSureConnectedToNetVocab(): String
    fun getImageSizeIsNotSuitableVocab(): String
    fun getBalanceIsNotEnoughVocab(): String
    fun getNeedToAllowAccessingYourPhotoLibraryVocab(): String
    fun getAttentionVocab(): String
}
