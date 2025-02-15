package com.example.classescreator

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.classescreator.examples.TPDiamondShapeExample
import com.example.classescreator.examples.testFirebaseObjects
import com.example.classescreator.screens.TPFirebaseChatScreen
import com.example.classescreator.ui.DatabaseTestScreen
import com.example.classescreator.ui.FirebaseTestScreen
import com.example.classescreator.ui.FirebaseUploadScreen
import com.example.classescreator.ui.LanguageTestScreen
import com.example.classescreator.ui.MapTestScreen
import com.example.classescreator.ui.SavedPDFsScreen
import com.example.classescreator.ui.SavedTextsScreen
import com.example.classescreator.ui.UserAttentionTestScreen
import com.example.classescreator.ui.ViewExtTestScreen
import com.example.classescreator.ui.theme.ClassesCreatorTheme
import com.google.firebase.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import com.kotlinclasses.TPSQLDatabase
import com.kotlinclasses.TPViewsNavigation
import com.kotlinclasses.TPViewsNavigation.Companion.addScreen


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        testFirebaseObjects()
//        val delegate = TPAppLanguageImpl(this)
//        val appLanguage = TPAppLanguage(delegate)
//        val currentLanguageCode = appLanguage.getLazyLanguageCode()
//        Log.d(TAG, "currentLanguageCode:${currentLanguageCode}  ")
//        TPFirebaseServicesExample.demonstrateUsage()

//       TPDateExtExample().runAllExamples()
//         runExample()
//        TPSQLDatabaseExample(this).customIdTableExample()
//        TPFileManager.init(applicationContext)
//        TPNetManager.init(applicationContext)
//        TPSQLDatabase.init(applicationContext)
//        val examples = TPDateExtExamples().printAllExamples()
//
//        // Print Previous Time Examples
//        examples.getPreviousTimeExamples().forEach { (key, value) ->
//            Log.d("DateExamples", "$key: $value")
//        }
//
//        // Print Future Time Examples
//        examples.getFutureTimeExamples().forEach { (key, value) ->
//            Log.d("DateExamples", "$key: $value")
//        }
//
//        // Print Current Time Examples
//        examples.getCurrentTimeExamples().forEach { (key, value) ->
//            Log.d("DateExamples", "$key: $value")
//        }
//
//        TPNetManagerExamples.checkBasicConnectivity()
//        TPNetManagerExamples.checkSpecificNetworks()
//        TPNetManagerExamples.printAllNetworkInfo()
//        TPStringExtExamples.runAllExamples()
//        val examples1 = TPFirebaseChatExamples()
//        examples1.runAllExamples()

//        val example = TPFirebaseDatabaseExample()

//        example.completeExample(this)

//        val scope = CoroutineScope(Dispatchers.IO)
//
//
//        val db = TPSQLDatabase.getInstance()
//
//        scope.launch {
////            db.createTable("test", listOf("name", "age"))
//
//
//
////            db.deleteValue(102, 2)
//            db.deleteValue(102, valueId = 12)
//            db.getTableValues(102).collectLatest{ values ->
//                values.forEach {
//                    Log.d(TAG, "Tables: ${it.valueData} ")
//                }
//                Log.d(TAG, "Tables11111111: $values ")
//            }
//        }




        setContent {
            ClassesCreatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navigation = remember { TPViewsNavigation() }
                    TPViewsNavigation.CreateNavigation(
                        startDestination = "DatabaseManagementScreen", // Changed to show diamond example by default
                        navigation = navigation
                    ) {
                        addScreen("FirebaseTest") {

                            FirebaseTestScreen()
                        }
                        addScreen("diamond_example") {
                            TPDiamondShapeExample()
                        }
                        addScreen("DatabaseManagementScreen") {
                            TPSQLDatabase.init(this@MainActivity)
//                            val db = TPSQLDatabase.getInstance()
//                            DatabaseTestScreen()
//                            RelationshipTestScreen(db)
                            DatabaseTestScreen()
                        }
                        addScreen("ChatScreen") {

                            TPFirebaseChatScreen()
                        }


                        addScreen("language") { 
                            LanguageTestScreen() 
                        }
                        addScreen("map") { 
                            MapTestScreen() 
                        }
                        addScreen("saved_pdfs") { 
                            SavedPDFsScreen() 
                        }
                        addScreen("saved_texts") { 
                            SavedTextsScreen() 
                        }
                        addScreen("user_attention") { 
                            UserAttentionTestScreen() 
                        }
                        addScreen("view_ext") { 
                            ViewExtTestScreen() 
                        }
                        addScreen("firebase_upload") { 
                            FirebaseUploadScreen(this@MainActivity) 
                        }
                    }
                }
            }
        }
    }
}
fun getDatabaseReference(): DatabaseReference {
    return Firebase.database.reference
}