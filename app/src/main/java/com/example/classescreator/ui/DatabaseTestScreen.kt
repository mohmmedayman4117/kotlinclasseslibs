package com.example.classescreator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotlinclasses.TPSQLDatabase
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseTestScreen() {
    var currentOperation by remember { mutableStateOf("") }
    var operationResult by remember { mutableStateOf("") }
    var tableValues by remember { mutableStateOf<List<TPSQLDatabase.ValueInfo>>(emptyList()) }
    var dbState by remember { mutableStateOf("") }

    val db = TPSQLDatabase.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "اختبار قاعدة البيانات",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Database State Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "حالة قاعدة البيانات:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = dbState,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Operations Section
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            item {
                // Create Table Operation
                OperationCard(
                    title = "1. إنشاء جدول جديد",
                    onClick = {
                        currentOperation = "إنشاء جدول"
                        val tableId = "STUDENTS_001"

                        val success = db.createTableWithId(
                            id = tableId,
                            name = "students",
                            columns = listOf("name", "age", "grade", "email")
                        )
                        operationResult = if (success) {
                            "✅ تم إنشاء الجدول بنجاح"
                        } else {
                            "❌ فشل إنشاء الجدول"
                        }
                        dbState = "تم إنشاء جدول: $tableId"
                        
                        // تحديث القيم المعروضة
                        CoroutineScope(Dispatchers.IO).launch {
                            refreshTableValues(db) { values ->
                                tableValues = values
                            }
                        }
                    }
                )

                // Insert First Student
                OperationCard(
                    title = "2. إضافة الطالب الأول",
                    onClick = {
                        currentOperation = "إضافة طالب"
                        val tableId = "STUDENTS_001"
                        val studentId = "STUDENT_001"

                        val id1 = db.insertValue(
                            tableId = tableId,
                            values = mapOf(
                                "name" to "أحمد",
                                "age" to "15",
                                "grade" to "10",
                                "email" to "ahmed@example.com"
                            ),
                            stringId = studentId
                        )

                        operationResult = if (id1 != null) {
                            "✅ تم إضافة الطالب (معرف: $id1)"
                        } else {
                            "❌ فشل إضافة الطالب"
                        }

                        CoroutineScope(Dispatchers.IO).launch {
                            refreshTableValues(db) { values ->
                                tableValues = values
                                val addedStudent = values.find { it.id == studentId }
                                if (addedStudent != null) {
                                    operationResult += "\n\nبيانات الطالب المضاف:\n${addedStudent.valueData}"
                                }
                            }
                        }

                        dbState = "عدد الطلاب: ${tableValues.size}"
                    }
                )

                // Insert Second Student
                OperationCard(
                    title = "3. إضافة الطالب الثاني",
                    onClick = {
                        currentOperation = "إضافة طالب"
                        val tableId = "STUDENTS_001"
                        val studentId = "STUDENT_002"

                        val id2 = db.insertValue(
                            tableId = tableId,
                            values = mapOf(
                                "name" to "سارة",
                                "age" to "16",
                                "grade" to "11",
                                "email" to "sara@example.com"
                            ),
                            stringId = studentId
                        )
                        operationResult = if (id2 != null) {
                            "✅ تم إضافة الطالب (معرف: $id2)"
                        } else {
                            "❌ فشل إضافة الطالب"
                        }
                        CoroutineScope(Dispatchers.IO).launch {
                            refreshTableValues(db) { values ->
                                tableValues = values
                            }
                        }
                        dbState = "عدد الطلاب: ${tableValues.size}"
                    }
                )

                // Update First Student
                OperationCard(
                    title = "4. تحديث بيانات الطالب الأول",
                    onClick = {
                        currentOperation = "تحديث بيانات"
                        val tableId = "STUDENTS_001"
                        val studentId = "STUDENT_001"

                        // التحقق من وجود الطالب
                        val student = tableValues.find { it.id == studentId }
                        if (student != null) {
                            operationResult = "القيم الحالية:\n${student.valueData}\n\n"

                            val results = mutableListOf<String>()

                            // تحديث الدرجة
                            val updateGrade = db.simpleUpdateTable(
                                tableId = tableId,
                                type = TPSQLDatabase.TableUpdateType.UPDATE_VALUE,
                                valueId = studentId,
                                columnName = "grade",
                                value = "55"
                            )
                            results.add("تحديث الدرجة: ${if (updateGrade) "✅" else "❌"}")

                            // تحديث البريد الإلكتروني
                            val updateEmail = db.simpleUpdateTable(
                                tableId = tableId,
                                type = TPSQLDatabase.TableUpdateType.UPDATE_VALUE,
                                valueId = studentId,
                                columnName = "email",
                                value = "ahmed.new@example.com"
                            )
                            results.add("تحديث البريد: ${if (updateEmail) "✅" else "❌"}")

                            operationResult += results.joinToString("\n")

                            // تحديث وعرض القيم الجديدة
                            CoroutineScope(Dispatchers.IO).launch {
                                refreshTableValues(db) { values ->
                                    tableValues = values
                                    val updatedStudent = values.find { it.id == studentId }
                                    if (updatedStudent != null) {
                                        operationResult += "\n\nالقيم بعد التحديث:\n${updatedStudent.valueData}"
                                    }
                                }
                            }
                        } else {
                            operationResult = "❌ لم يتم العثور على الطالب"
                        }
                    }
                )

                // Simple Update Table Operation
                OperationCard(
                    title = "5. تعديل هيكل الجدول",
                    onClick = {
                        currentOperation = "تعديل هيكل الجدول"

                        val results = mutableListOf<String>()

                        // إضافة عمود جديد للعنوان
                        val addAddressColumn = db.simpleUpdateTable(
                            tableId = "STUDENTS_001",
                            type = TPSQLDatabase.TableUpdateType.ADD_COLUMN,
                            value = "address"
                        )
                        results.add("إضافة عمود 'address': ${if (addAddressColumn) "✅" else "❌"}")

                        // إضافة عمود للهاتف
                        val addPhoneColumn = db.simpleUpdateTable(
                            tableId = "STUDENTS_001",
                            type = TPSQLDatabase.TableUpdateType.ADD_COLUMN,
                            value = "phone"
                        )
                        results.add("إضافة عمود 'phone': ${if (addPhoneColumn) "✅" else "❌"}")

                        // تغيير اسم الجدول
//                        val updateName = db.simpleUpdateTable(
//                            tableId = "STUDENTS_001",
//                            type = TPSQLDatabase.TableUpdateType.NAME,
//                            value = "students_2024"
//                        )
//                        results.add("تغيير اسم الجدول: ${if (updateName) "✅" else "❌"}")
//
                        // تحديث قائمة الأعمدة
                        val updateColumns = db.simpleUpdateTable(
                            tableId = "STUDENTS_001",
                            type = TPSQLDatabase.TableUpdateType.COLUMNS,
                            value = listOf("name", "age", "grade", "email", "address", "phone")
                        )
                        results.add("تحديث قائمة الأعمدة: ${if (updateColumns) "✅" else "❌"}")

                        // حذف عمود الهاتف (كمثال)
                        val removePhone = db.simpleUpdateTable(
                            tableId = "STUDENTS_001",
                            type = TPSQLDatabase.TableUpdateType.REMOVE_COLUMN,
                            value = "phone"
                        )
                        results.add("حذف عمود 'phone': ${if (removePhone) "✅" else "❌"}")

                        operationResult = results.joinToString("\n")
                        dbState = "تم تحديث هيكل الجدول"
                        CoroutineScope(Dispatchers.IO).launch {
                            refreshTableValues(db) { values ->
                                tableValues = values
                            }
                        }
                    }
                )

                // Search Operations
                OperationCard(
                    title = "6. عمليات البحث",
                    onClick = {
                        currentOperation = "عمليات البحث"
                        val results = mutableListOf<String>()

                        // البحث في عمود محدد (العمر)
                        val ageResults = db.searchTableValues(
                            tableId = "STUDENTS_001",
                            options = TPSQLDatabase.ValueSearchOptions(
                                query = "15",
                                type = TPSQLDatabase.ValueSearchType.SPECIFIC_COLUMN,
                                columnName = "id",
                                exactMatch = true
                            )
                        )
                        results.add("البحث في العمر (15 سنة):")
                        ageResults.forEach { result ->
                            results.add("- الاسم: ${result.allValues["name"]}, العمر: ${result.value}")
                        }

                        // البحث في الاسم
                        val nameResults = db.searchTableValues(
                            tableId = "STUDENTS_001",
                            options = TPSQLDatabase.ValueSearchOptions(
                                query = "سارة",
                                type = TPSQLDatabase.ValueSearchType.SPECIFIC_COLUMN,
                                columnName = "id"
                            )
                        )
                        results.add("\nالبحث في الاسم (يحتوي 'سارة'):")
                        nameResults.forEach { result ->
                            results.add("- الاسم: ${result.value}, الدرجة: ${result.allValues["grade"]}")
                        }

                        // البحث في كل الأعمدة
                        val allResults = db.searchTableValues(
                            tableId = "STUDENTS_001",
                            options = TPSQLDatabase.ValueSearchOptions(
                                query = "متفوق",
                                type = TPSQLDatabase.ValueSearchType.ALL_COLUMNS
                            )
                        )
                        results.add("\nالبحث في كل الأعمدة (يحتوي 'متفوق'):")
                        allResults.forEach { result ->
                            results.add("- الاسم: ${result.allValues["name"]}, القيمة: ${result.value}")
                        }

                        operationResult = results.joinToString("\n")
                        dbState = "تم العثور على ${ageResults.size + nameResults.size + allResults.size} نتيجة"
                    }
                )

                // Delete Operations
                OperationCard(
                    title = "7. عمليات الحذف",
                    onClick = {
                        currentOperation = "عمليات الحذف"

                        db.deleteValue("STUDENTS_001",valueId = "STUDENT_002", deleteTable = false)

//                        val deleteCount = db.deleteValueByCondition(
//                            tableId = "STUDENTS_001",
//                            conditions = mapOf("grade" to "55"),
//                            matchAll = true
//                        )

//                        operationResult = "تم حذف $deleteCount سجل/سجلات بنجاح"
                        CoroutineScope(Dispatchers.IO).launch {
                            refreshTableValues(db) { values ->
                                tableValues = values
                            }
                        }

//                        dbState = "تم حذف $deleteCount سجل/سجلات"
                    }
                )
            }

            // Display Current Values
            item {
                Text(
                    text = "البيانات الحالية في الجدول:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            items(tableValues) { value ->
                ValueCard(value = value)
            }
        }

        // Operation Result
        if (operationResult.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (operationResult.startsWith("✅"))
                        Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else
                        Color(0xFFE57373).copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "نتيجة العملية: $currentOperation",
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = operationResult)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationCard(
    title: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun ValueCard(value: TPSQLDatabase.ValueInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "معرف: ${value.id}",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "معرف الجدول: ${value.tableId}",
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "البيانات:",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            val valueData = JSONObject(value.valueData)

            // الحصول على قائمة الأعمدة من الجدول
            val db = TPSQLDatabase.getInstance()
            val cursor = db.readableDatabase.query(
                "dynamic_tables",
                arrayOf("columns"),
                "id = ?",
                arrayOf(value.tableId),
                null, null, null
            )

            cursor.use { cursor ->
                if (cursor.moveToFirst()) {
                    val columnsJson = cursor.getString(0)
                    val columnsList = JSONArray(columnsJson).let { array ->
                        List(array.length()) { array.getString(it) }
                    }

                    // عرض كل الأعمدة، حتى الفارغة
                    columnsList.forEach { columnName ->
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = columnName,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = valueData.optString(columnName, ""),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(2f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private suspend fun refreshTableValues(
    db: TPSQLDatabase,
    onUpdate: (List<TPSQLDatabase.ValueInfo>) -> Unit
) {
    db.getTableValues("STUDENTS_001")
        ?.collect { values ->
            onUpdate(values)
        }
}
