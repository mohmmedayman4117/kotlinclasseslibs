package com.example.classescreator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kotlinclasses.TPSQLDatabase

@Composable
fun TPSQLDatabaseExample(
    db: TPSQLDatabase,
    modifier: Modifier = Modifier
) {
    var selectedExample by remember { mutableStateOf(0) }
    val examples = listOf("طلاب ومواد", "مدرسين وفصول", "طلاب ومكتبة")

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        // اختيار المثال
        TabRow(selectedTabIndex = selectedExample) {
            examples.forEachIndexed { index, title ->
                Tab(
                    selected = selectedExample == index,
                    onClick = { selectedExample = index },
                    text = { Text(title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // عرض المثال المختار
        when (selectedExample) {
            0 -> StudentsAndCoursesExample(db)
            1 -> TeachersAndClassesExample(db)
            2 -> StudentsAndLibraryExample(db)
        }
    }
}

@Composable
private fun StudentsAndCoursesExample(db: TPSQLDatabase) {
    var studentsState by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var coursesState by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var selectedStudentId by remember { mutableStateOf<String?>(null) }
    var selectedCourseId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            // إنشاء جداول المثال
            db.createTableWithId(
                id = "STD_001",
                name = "students",
                columns = listOf("name", "grade")
            )

            db.createTableWithId(
                id = "CRS_001",
                name = "courses",
                columns = listOf("name", "code")
            )

            // إنشاء العلاقة متعدد لمتعدد
            val relationshipId = db.quickRelation(
                sourceTableName = "students",
                targetTableName = "courses",
                type = TPSQLDatabase.TableRelationType.MANY_TO_MANY,
                targetColumn = "course_id"
            )

            // إضافة بعض البيانات
            db.insertValue("STD_001", mapOf(
                "name" to "أحمد",
                "grade" to "الأول"
            ))?.let { studentId ->
                db.insertValue("CRS_001", mapOf(
                    "name" to "رياضيات",
                    "code" to "MATH101"
                ))?.let { courseId ->
                    // ربط الطالب بالمادة
                    if (studentId.isNotEmpty() && courseId.isNotEmpty()) {
                        db.linkValues(
                            sourceTableName = "students",
                            sourceValueId = studentId,
                            targetTableName = "courses",
                            targetValueId = courseId
                        )
                    }
                }
            }
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("مثال العلاقة بين الطلاب والمواد (متعدد لمتعدد)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // قائمة الطلاب
            Column(modifier = Modifier.weight(1f)) {
                Text("الطلاب")
                LazyColumn {
                    items(studentsState) { student ->
                        StudentItem(
                            student = student,
                            isSelected = student["id"] as String == selectedStudentId,
                            onClick = { selectedStudentId = student["id"] as String }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // قائمة المواد
            Column(modifier = Modifier.weight(1f)) {
                Text("المواد")
                LazyColumn {
                    items(coursesState) { course ->
                        CourseItem(
                            course = course,
                            isSelected = course["id"] as String == selectedCourseId,
                            onClick = { selectedCourseId = course["id"] as String }
                        )
                    }
                }
            }
        }

        // زر الربط
        if (selectedStudentId != null && selectedCourseId != null) {
            Button(
                onClick = {
                    db.linkValues(
                        sourceTableName = "students",
                        sourceValueId = selectedStudentId!!,
                        targetTableName = "courses",
                        targetValueId = selectedCourseId!!
                    )
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("ربط الطالب بالمادة")
            }
        }
    }
}

@Composable
private fun TeachersAndClassesExample(db: TPSQLDatabase) {
    var teachersState by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var classesState by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    
    LaunchedEffect(Unit) {
        try {
            // إنشاء جداول المثال
            db.createTableWithId(
                id = "TCH_001",
                name = "teachers",
                columns = listOf("name", "subject")
            )

            db.createTableWithId(
                id = "CLS_001",
                name = "classes",
                columns = listOf("name", "room")
            )

            // إنشاء العلاقة واحد لمتعدد
            db.quickRelation(
                sourceTableName = "teachers",
                targetTableName = "classes",
                type = TPSQLDatabase.TableRelationType.ONE_TO_MANY,
                targetColumn = "teacher_id"
            )
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("مثال العلاقة بين المدرسين والفصول (واحد لمتعدد)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("المدرسين")
                LazyColumn {
                    items(teachersState) { teacher ->
                        TeacherItem(teacher = teacher)
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("الفصول")
                LazyColumn {
                    items(classesState) { classItem ->
                        ClassItem(classItem = classItem)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentsAndLibraryExample(db: TPSQLDatabase) {
    var studentsState by remember { mutableStateOf(emptyList<Map<String, Any>>()) }
    var cardsState by remember { mutableStateOf(emptyList<Map<String, Any>>()) }

    LaunchedEffect(Unit) {
        try {
            // إنشاء جداول المثال
            db.createTableWithId(
                id = "STD_001",
                name = "students",
                columns = listOf("name", "grade")
            )

            db.createTableWithId(
                id = "LIB_001",
                name = "library_cards",
                columns = listOf("card_number", "expiry_date")
            )

            // إنشاء العلاقة واحد لواحد
            db.quickRelation(
                sourceTableName = "students",
                targetTableName = "library_cards",
                type = TPSQLDatabase.TableRelationType.ONE_TO_ONE,
                targetColumn = "student_id"
            )
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("مثال العلاقة بين الطلاب وبطاقات المكتبة (واحد لواحد)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text("الطلاب")
                LazyColumn {
                    items(studentsState) { student ->
                        StudentItem(
                            student = student,
                            isSelected = false,
                            onClick = {}
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text("بطاقات المكتبة")
                LazyColumn {
                    items(cardsState) { card ->
                        LibraryCardItem(card = card)
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentItem(
    student: Map<String, Any>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        tonalElevation = if (isSelected) 8.dp else 0.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = student["name"] as String)
            Text(
                text = "الصف: ${student["grade"]}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CourseItem(
    course: Map<String, Any>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        tonalElevation = if (isSelected) 8.dp else 0.dp,
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = course["name"] as String)
            Text(
                text = "الرمز: ${course["code"]}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun TeacherItem(teacher: Map<String, Any>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = teacher["name"] as String)
            Text(
                text = "المادة: ${teacher["subject"]}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ClassItem(classItem: Map<String, Any>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = classItem["name"] as String)
            Text(
                text = "الغرفة: ${classItem["room"]}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun LibraryCardItem(card: Map<String, Any>) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "بطاقة رقم: ${card["card_number"]}")
            Text(
                text = "تاريخ الانتهاء: ${card["expiry_date"]}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
