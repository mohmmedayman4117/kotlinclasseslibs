package com.example.classescreator

import android.content.Context
import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.kotlinclasses.TPSQLDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * أمثلة شاملة على استخدام TPSQLDatabase
 * يوضح كيفية استخدام جميع الوظائف المتاحة في المكتبة
 */
class TPSQLDatabaseExample(private val context: Context) {
    
    private val TAG = "TPSQLDatabaseExample"
    private lateinit var db: TPSQLDatabase

    /**
     * تهيئة قاعدة البيانات
     */
    fun initializeDatabase() {
        // تهيئة قاعدة البيانات
        TPSQLDatabase.init(context, "example_db.db", 1)
        db = TPSQLDatabase.getInstance()
    }

    /**
     * مثال على إنشاء وإدارة جدول الطلاب
     */
    fun studentsTableExample() {
        // إنشاء جدول الطلاب
        db.createTableWithId(
            id = "STD_001",
            name = "students",
            columns = listOf("name", "age", "grade", "email")
        )

        // إضافة طالب جديد
        val studentId1 = db.insertValue(
            "STD_001",
            mapOf(
                "name" to "Ahmed",
                "age" to "15",
                "grade" to "10th",
                "email" to "ahmed@example.com"
            )
        )

        // إضافة طالب آخر مع معرف مخصص
        val studentId2 = db.insertValue(
            "STD_001",
            mapOf(
                "name" to "Sarah",
                "age" to "16",
                "grade" to "11th",
                "email" to "sarah@example.com"
            ),
            "STD_1001"
        )

        // تحديث بيانات طالب
        if (studentId1 != null) {
            db.simpleUpdateValue(
                tableId = "STD_001",
                valueId = studentId1,
                columnName = "grade",
                value = "11th"
            )
        }

        // البحث عن طلاب
        val searchResults = db.searchTableValues(
            "STD_001",
            TPSQLDatabase.ValueSearchOptions(
                query = "11th",
                type = TPSQLDatabase.ValueSearchType.SPECIFIC_COLUMN,
                columnName = "grade"
            )
        )

        // عرض نتائج البحث
        searchResults.forEach { result ->
            Log.d(TAG, "Found student: ${result.allValues}")
        }
    }

    /**
     * مثال على إدارة جدول المعلمين
     */
    fun teachersTableExample() {
        // إنشاء جدول المعلمين مع معرف مخصص
        db.createTableWithId(
            id = "TCH_001",
            name = "teachers",
            columns = listOf("name", "subject", "salary")
        )

        // إضافة معلمين
        val teachers = listOf(
            mapOf(
                "name" to "Mohammed",
                "subject" to "Math",
                "salary" to "5000"
            ),
            mapOf(
                "name" to "Fatima",
                "subject" to "Science",
                "salary" to "5500"
            )
        )

        teachers.forEach { teacher ->
            db.insertValue("TCH_001", teacher)
        }

        // تحديث رواتب المعلمين
        db.searchTableValues(
            "TCH_001",
            TPSQLDatabase.ValueSearchOptions(
                query = "5000",
                type = TPSQLDatabase.ValueSearchType.SPECIFIC_COLUMN,
                columnName = "salary"
            )
        ).forEach { result ->
            db.updateValueById(
                "TCH_001",
                result.valueId,
                mapOf("salary" to "5200")
            )
        }
    }

    /**
     * مثال على استخدام الـ Flow للمراقبة المباشرة
     */
    fun flowExample() = runBlocking {
        val scope = CoroutineScope(Dispatchers.Main)
        
        // مراقبة التغييرات في جدول الطلاب
        scope.launch {
            db.getTableValues("STD_001")
                .collect { values ->
                    Log.d(TAG, "Students table updated: ${values.size} records")
                    values.forEach { value ->
                        Log.d(TAG, "Student: ${value.valueData}")
                    }
                }
        }

        // مراقبة نتائج البحث
        scope.launch {
            db.searchTableValues("STD_001", "Ahmed")
                .collect { values ->
                    Log.d(TAG, "Search results updated: ${values.size} matches")
                }
        }
    }

    /**
     * مثال على عمليات متقدمة
     */
    suspend fun advancedOperationsExample() {
        try {
            // تأكد من وجود بيانات للبحث فيها
            val tables = db.getTableValues("STD_001").first()
            if (tables.isEmpty()) {
                Log.d(TAG, "No data available for search")
                return
            }

            // البحث في كل الجداول
            val searchResults = db.searchTables(
                TPSQLDatabase.SearchOptions(
                    query = "math",
                    type = TPSQLDatabase.SearchType.VALUES,
                    caseSensitive = false,
                    limit = 10 // تحديد عدد النتائج
                )
            )

            // عرض نتائج البحث
            searchResults.forEach { result ->
                Log.d(TAG, "Search result: ${result.table.name}")
            }

            // تحديث متعدد للقيم
            db.simpleUpdateValue(
                tableId = "STD_001",
                valueId = "STD_1001",
                type = TPSQLDatabase.ValueUpdateType.MULTIPLE_VALUES,
                updates = mapOf(
                    "grade" to "12th",
                    "email" to "new.sarah@example.com"
                )
            )

            // حذف بناءً على شروط
            try {
                val deletedCount = db.deleteValueByCondition(
                    tableId = "STD_001",
                    conditions = mapOf(
                        "grade" to "12th",
                        "age" to "16"
                    ),
                    matchAll = true
                )
                Log.d(TAG, "Deleted $deletedCount records")
            } catch (e: Exception) {
                Log.e(TAG, "Error during deletion: ${e.message}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in advanced operations: ${e.message}")
        }
    }

    /**
     * مثال على إدارة الأعمدة
     */
    fun columnManagementExample() {
        // إضافة عمود جديد
        db.simpleUpdateTable(
            tableId = "STD_001",
            type = TPSQLDatabase.TableUpdateType.ADD_COLUMN,
            value = "phone"
        )

        // حذف عمود
        db.simpleUpdateTable(
            tableId = "STD_001",
            type = TPSQLDatabase.TableUpdateType.REMOVE_COLUMN,
            value = "email"
        )

        // تحديث قائمة الأعمدة كاملة
        db.simpleUpdateTable(
            tableId = "STD_001",
            type = TPSQLDatabase.TableUpdateType.COLUMNS,
            value = listOf("name", "age", "grade", "phone")
        )
    }

    /**
     * تشغيل كل الأمثلة
     */
    suspend fun runAllExamples() {
//        initializeDatabase()
//        studentsTableExample()
//        teachersTableExample()
//        flowExample()
//        advancedOperationsExample()
//        columnManagementExample()
    }
}
