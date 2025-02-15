package com.kotlinclasses

class TPUserAttentionDelegateImpl(private val appLanguage: TPAppLanguage) : TPUserAttentionDelegate {
    
    private val arMessages = mapOf(
        "wrong" to "خطأ",
        "unavailable" to "غير متوفر",
        "error" to "حدث خطأ ما",
        "agree" to "موافق",
        "cancel" to "إلغاء",
        "successful" to "تم بنجاح",
        "savedSuccessfully" to "تم الحفظ بنجاح",
        "incorrectPassword" to "كلمة المرور غير صحيحة",
        "alreadyExists" to "موجود بالفعل",
        "failedToSave" to "فشل في الحفظ",
        "sentSuccessfully" to "تم الإرسال بنجاح",
        "deletedSuccessfully" to "تم الحذف بنجاح",
        "passwordsNotMatched" to "كلمات المرور غير متطابقة",
        "failedToRetrieveData" to "فشل في استرجاع البيانات من الخادم",
        "checkInternetConnection" to "تأكد من اتصالك بالإنترنت",
        "imageSizeNotSuitable" to "حجم الصورة غير مناسب",
        "insufficientBalance" to "الرصيد غير كافي",
        "allowPhotoAccess" to "تحتاج للسماح بالوصول إلى مكتبة الصور",
        "attention" to "تنبيه",
        "enterName" to "أدخل اسمك",
        "name" to "الاسم",
        "confirmDelete" to "تأكيد الحذف",
        "deleteConfirmMessage" to "هل تريد حذف هذا العنصر؟",
        "enterText" to "أدخل النص",
        "text" to "النص",
        "save" to "حفظ",
        "delete" to "حذف"
    )

    private val enMessages = mapOf(
        "wrong" to "Error",
        "unavailable" to "Unavailable",
        "error" to "An error occurred",
        "agree" to "OK",
        "cancel" to "Cancel",
        "successful" to "Success",
        "savedSuccessfully" to "Saved successfully",
        "incorrectPassword" to "Password is incorrect",
        "alreadyExists" to "Already exists",
        "failedToSave" to "Failed to save",
        "sentSuccessfully" to "Sent successfully",
        "deletedSuccessfully" to "Deleted successfully",
        "passwordsNotMatched" to "Passwords do not match",
        "failedToRetrieveData" to "Failed to retrieve data from server",
        "checkInternetConnection" to "Make sure you are connected to the internet",
        "imageSizeNotSuitable" to "Image size is not suitable",
        "insufficientBalance" to "Balance is not enough",
        "allowPhotoAccess" to "You need to allow access to your photo library",
        "attention" to "Attention",
        "enterName" to "Enter your name",
        "name" to "Name",
        "confirmDelete" to "Confirm Delete",
        "deleteConfirmMessage" to "Do you want to delete this item?",
        "enterText" to "Enter text",
        "text" to "Text",
        "save" to "Save",
        "delete" to "Delete"
    )

    private fun getMessage(key: String): String {
        return when (appLanguage.getCurrentLanguageCode()) {
            "ar" -> arMessages[key] ?: key
            "en" -> enMessages[key] ?: key
            else -> enMessages[key] ?: key
        }
    }

    override fun getWrongVocab(): String = getMessage("wrong")
    override fun getItemUnAvailableVocab(): String = getMessage("unavailable")
    override fun getSomeErrorHappenedVocab(): String = getMessage("error")
    override fun getAgreeVocab(): String = getMessage("agree")
    override fun getCancelVocab(): String = getMessage("cancel")
    override fun getSuccessfulVocab(): String = getMessage("successful")
    override fun getSuccessfullySavedVocab(): String = getMessage("savedSuccessfully")
    override fun getPasswordIsNotCorrectVocab(): String = getMessage("incorrectPassword")
    override fun getAlreadyExistVocab(): String = getMessage("alreadyExists")
    override fun getFailToSaveVocab(): String = getMessage("failedToSave")
    override fun getSentSuccessfullyVocab(): String = getMessage("sentSuccessfully")
    override fun getDeletedSuccessfullyVocab(): String = getMessage("deletedSuccessfully")
    override fun getPasswordsAreNotMatchedVocab(): String = getMessage("passwordsNotMatched")
    override fun getFailToRetrieveDataFromServerVocab(): String = getMessage("failedToRetrieveData")
    override fun getMakeSureConnectedToNetVocab(): String = getMessage("checkInternetConnection")
    override fun getImageSizeIsNotSuitableVocab(): String = getMessage("imageSizeNotSuitable")
    override fun getBalanceIsNotEnoughVocab(): String = getMessage("insufficientBalance")
    override fun getNeedToAllowAccessingYourPhotoLibraryVocab(): String = getMessage("allowPhotoAccess")
    override fun getAttentionVocab(): String = getMessage("attention")

    fun getEnterNameVocab(): String = getMessage("enterName")
    fun getNameVocab(): String = getMessage("name")
    fun getConfirmDeleteVocab(): String = getMessage("confirmDelete")
    fun getDeleteConfirmMessageVocab(): String = getMessage("deleteConfirmMessage")
    fun getEnterTextVocab(): String = getMessage("enterText")
    fun getTextVocab(): String = getMessage("text")
    fun getSaveVocab(): String = getMessage("save")
    fun getDeleteVocab(): String = getMessage("delete")
}
