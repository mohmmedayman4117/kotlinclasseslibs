package com.kotlinclasses

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

object FirebaseConfig {
    private var initialized = false
    
    fun initialize(context: Context) {
        if (!initialized) {
            FirebaseApp.initializeApp(context)
            initialized = true
        }
    }
    
    fun getDatabaseReference(): DatabaseReference {
        return Firebase.database.reference
    }
    

}
