package com.kotlinclasses

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * A library for easy SQLite database management
 * Enables creating and managing dynamic tables and their values
 *
 * This class provides a flexible way to create and manage dynamic tables in SQLite.
 * It supports operations like creating tables with custom columns, inserting values,
 * searching, and real-time updates using Kotlin Flows.
 *
 * @property context Application context used to create the database
 * @property databaseName Name of the database file
 * @property version Database version number
 */
class TPSQLDatabase private constructor(
    private val context: Context,
    private val databaseName: String,
    private val version: Int
) : SQLiteOpenHelper(context, databaseName, null, version) {

    companion object {
        private var instance: TPSQLDatabase? = null

        // Increment version to force database recreation
        private const val DATABASE_VERSION = 2

        fun init(context: Context, databaseName: String = "dynamic_tables.db", version: Int = DATABASE_VERSION) {
            if (instance == null) {
                instance = TPSQLDatabase(context.applicationContext, databaseName, version)
            }
        }

        fun getInstance(): TPSQLDatabase {
            return instance ?: throw IllegalStateException("TPSQLDatabase must be initialized first")
        }
    }


    private val _tablesFlow = MutableStateFlow<List<TableInfo>>(emptyList())
    private val _valuesFlow = MutableStateFlow<List<ValueInfo>>(emptyList())
    private val _relationshipsFlow = MutableStateFlow<List<TableRelationship>>(emptyList())

    init {
        // Tables will be updated after onCreate
        _tablesFlow.value = emptyList()
    }

    /**
     * أنواع العلاقات بين الجداول
     */
    enum class TableRelationType {
        /** علاقة واحد لواحد */
        ONE_TO_ONE,
        /** علاقة واحد لمتعدد */
        ONE_TO_MANY,
        /** علاقة متعدد لمتعدد */
        MANY_TO_MANY
    }

    /**
     * Create database tables when first created
     * @param db Database to create tables in
     */
    override fun onCreate(db: SQLiteDatabase) {
        // Enable foreign key support first
        db.execSQL("PRAGMA foreign_keys=ON")

        // Create dynamic tables table first since it's referenced by others
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS dynamic_tables (
                name TEXT NOT NULL,
                id TEXT PRIMARY KEY,
                columns TEXT NOT NULL
            )
        """)

        // Create values table with foreign key reference
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS dynamic_table_values (
                id TEXT PRIMARY KEY,
                tableId TEXT NOT NULL,
                value_data TEXT NOT NULL,
                FOREIGN KEY(tableId) REFERENCES dynamic_tables(id) ON DELETE CASCADE
            )
        """)

        // Create relationships table with foreign key references
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS table_relationships (
                id TEXT PRIMARY KEY,
                source_table_id TEXT NOT NULL,
                target_table_id TEXT NOT NULL,
                relationship_type TEXT NOT NULL,
                source_column TEXT NOT NULL,
                target_column TEXT NOT NULL,
                FOREIGN KEY(source_table_id) REFERENCES dynamic_tables(id) ON DELETE CASCADE,
                FOREIGN KEY(target_table_id) REFERENCES dynamic_tables(id) ON DELETE CASCADE
            )
        """)

    }

    /**
     * Upgrade database when version changes
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Drop tables in reverse order of dependencies
        db.execSQL("DROP TABLE IF EXISTS table_relationships")
        db.execSQL("DROP TABLE IF EXISTS dynamic_table_values")
        db.execSQL("DROP TABLE IF EXISTS dynamic_tables")
        
        // Recreate tables
        onCreate(db)
    }

    /**
     * Update tables list in memory
     * Internal function used to update [_tablesFlow]
     */
    private fun updateTables() {
        val tables = mutableListOf<TableInfo>()
        val cursor = readableDatabase.query(
            "dynamic_tables",
            arrayOf("id", "name", "columns"),
            null, null, null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                tables.add(TableInfo(
                    it.getString(0),
                    it.getString(1),
                    it.getString(2)
                ))
            }
        }
        _tablesFlow.value = tables
    }

    /**
     * Update values list in memory for a specific table
     * Internal function used to update [_valuesFlow]
     *
     * @param tableId ID of the table to update values for
     */
    private fun updateValues(tableId: String) {
        val values = mutableListOf<ValueInfo>()
        val cursor = readableDatabase.query(
            "dynamic_table_values",
            arrayOf("id", "tableId", "value_data"),
            "tableId = ?",
            arrayOf(tableId),
            null, null, null
        )
        
        cursor.use {
            while (it.moveToNext()) {
                values.add(ValueInfo(
                    it.getString(0),
                    it.getString(1),
                    it.getString(2)
                ))
            }
        }
        _valuesFlow.value = values
    }

    /**
     * Get all tables in the database
     * @return Flow of tables list that updates automatically
     *
     * Example usage:
     * ```kotlin
     * val tables = db.getAllTables().collectAsState(initial = emptyList())
     * ```
     * 
     * Example output:
     * ```kotlin
     * [
     *   TableInfo(
     *     id = "STD_001",
     *     name = "students",
     *     columns = "[\"name\",\"age\",\"grade\"]"
     *   ),
     *   TableInfo(
     *     id = "TCH_001",
     *     name = "teachers",
     *     columns = "[\"name\",\"subject\"]"
     *   )
     * ]
     * ```
     */
    fun getAllTables(): Flow<List<TableInfo>> = _tablesFlow

    /**
     * Search tables using a text query
     * @param query Search text
     * @return Flow of tables that match the search query
     *
     * Example usage:
     * ```kotlin
     * val searchResults = db.searchTables("student").collectAsState(initial = emptyList())
     * ```
     * 
     * Example output:
     * ```kotlin
     * [
     *   TableInfo(
     *     id = "STD_001",
     *     name = "students",
     *     columns = "[\"name\",\"age\",\"grade\"]"
     *   )
     * ]
     * ```
     */
    fun searchTables(query: String): Flow<List<TableInfo>> = _tablesFlow
        .map { tables -> 
            tables.filter { it.name.contains(query, ignoreCase = true) }
        }

    /**
     * أنواع البحث المتاحة في الجداول
     */
    enum class SearchType {
        /** البحث في اسم الجدول */
        TABLE_NAME,
        /** البحث في معرف الجدول */
        TABLE_ID,
        /** البحث في أسماء الأعمدة */
        COLUMN_NAMES,
        /** البحث في قيم الجدول */
        VALUES,
        /** البحث في كل شيء */
        ALL
    }

    /**
     * خيارات البحث المتقدم
     * @property query نص البحث
     * @property type نوع البحث
     * @property caseSensitive حساسية الأحرف الكبيرة والصغيرة
     * @property exactMatch البحث عن تطابق تام
     * @property limit عدد النتائج المطلوبة (0 يعني بدون حد)
     * @property offset البداية من أي نتيجة
     * @property sortBy ترتيب النتائج حسب عمود معين
     * @property sortDescending ترتيب تنازلي
     */
    data class SearchOptions(
        val query: String,
        val type: SearchType = SearchType.ALL,
        val caseSensitive: Boolean = false,
        val exactMatch: Boolean = false,
        val limit: Int = 0,
        val offset: Int = 0,
        val sortBy: String? = null,
        val sortDescending: Boolean = false
    )

    /**
     * نتيجة البحث
     * @property table معلومات الجدول
     * @property matchType نوع التطابق
     * @property matchValue القيمة المتطابقة
     */
    data class SearchResult(
        val table: TableInfo,
        val matchType: SearchType,
        val matchValue: String
    )

    /**
     * البحث المتقدم في الجداول
     * @param options خيارات البحث
     * @return قائمة بنتائج البحث
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // البحث في كل شيء
     * val results1 = db.searchTables(
     *     SearchOptions(
     *         query = "student"
     *     )
     * )
     *
     * // البحث في أسماء الجداول فقط
     * val results2 = db.searchTables(
     *     SearchOptions(
     *         query = "users",
     *         type = SearchType.TABLE_NAME
     *     )
     * )
     *
     * // البحث عن تطابق تام مع حساسية الأحرف
     * val results3 = db.searchTables(
     *     SearchOptions(
     *         query = "Ahmed",
     *         type = SearchType.VALUES,
     *         caseSensitive = true,
     *         exactMatch = true
     *     )
     * )
     *
     * // البحث مع تحديد عدد النتائج
     * val results4 = db.searchTables(
     *     SearchOptions(
     *         query = "grade",
     *         limit = 10,
     *         offset = 20
     *     )
     * )
     * ```
     */
    fun searchTables(options: SearchOptions): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val query = if (options.caseSensitive) options.query else options.query.lowercase()

        // دالة مساعدة للتحقق من التطابق
        fun checkMatch(value: String): Boolean {
            val testValue = if (options.caseSensitive) value else value.lowercase()
            return when {
                options.exactMatch -> testValue == query
                else -> testValue.contains(query)
            }
        }

        // البحث في الجداول
        val cursor = readableDatabase.query(
            "dynamic_tables",
            arrayOf("id", "name", "columns"),
            null, null,
            null, null, null
        )

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val tableId = cursor.getString(0)
                val tableName = cursor.getString(1)
                val columnsJson = cursor.getString(2)
                val columns = JSONArray(columnsJson).let { array ->
                    List(array.length()) { array.getString(it) }
                }

                val table = TableInfo(tableId, tableName, columnsJson)

                // البحث في معرف الجدول
                if (options.type in listOf(SearchType.TABLE_ID, SearchType.ALL)) {
                    if (checkMatch(tableId)) {
                        results.add(SearchResult(table, SearchType.TABLE_ID, tableId))
                        if (options.limit > 0 && results.size >= options.limit) return results
                    }
                }

                // البحث في اسم الجدول
                if (options.type in listOf(SearchType.TABLE_NAME, SearchType.ALL)) {
                    if (checkMatch(tableName)) {
                        results.add(SearchResult(table, SearchType.TABLE_NAME, tableName))
                        if (options.limit > 0 && results.size >= options.limit) return results
                    }
                }

                // البحث في أسماء الأعمدة
                if (options.type in listOf(SearchType.COLUMN_NAMES, SearchType.ALL)) {
                    for (column in columns) {
                        if (checkMatch(column)) {
                            results.add(SearchResult(table, SearchType.COLUMN_NAMES, column))
                            if (options.limit > 0 && results.size >= options.limit) return results
                        }
                    }
                }

                // البحث في القيم
                if (options.type in listOf(SearchType.VALUES, SearchType.ALL)) {
                    val valuesCursor = readableDatabase.query(
                        "dynamic_table_values",
                        arrayOf("value_data"),
                        "tableId = ?",
                        arrayOf(tableId),
                        null, null, null
                    )

                    valuesCursor.use { values ->
                        while (values.moveToNext()) {
                            val valueData = values.getString(0)
                            val valueJson = JSONObject(valueData)
                            valueJson.keys().forEach { key ->
                                val value = valueJson.get(key).toString()
                                if (checkMatch(value)) {
                                    results.add(SearchResult(table, SearchType.VALUES, value))
                                    if (options.limit > 0 && results.size >= options.limit) return results
                                }
                            }
                        }
                    }
                }
            }
        }

        // تطبيق الـ offset
        return if (options.offset > 0) {
            results.drop(options.offset)
        } else {
            results
        }
    }

    /**
     * Create a new table
     * @param tableName Name of the table
     * @param columns List of column names
     *
     * Example usage:
     * ```kotlin
     * db.createTable("students", listOf("name", "age", "grade"))
     * ```
     */
    fun createTable(tableName: String, columns: List<String>) {
        val values = ContentValues().apply {
            put("name", tableName)
            put("columns", JSONArray(columns).toString())
        }
        writableDatabase.insert("dynamic_tables", null, values)
        updateTables()
    }

    /**
     * Delete a table
     * @param tableId ID of the table to delete
     *
     * Example usage:
     * ```kotlin
     * db.deleteTable("STD_001")
     * ```
     */
    fun deleteTable(tableId: String) {
        writableDatabase.delete("dynamic_tables", "id = ?", arrayOf(tableId))
        updateTables()
        _valuesFlow.value = emptyList()
    }

    /**
     * Get values for a specific table
     * @param tableId Table ID
     * @return Flow of values list that updates automatically
     *
     * Example usage:
     * ```kotlin
     * val values = db.getTableValues("STD_001").collectAsState(initial = emptyList())
     * ```
     * 
     * Example output for students table:
     * ```kotlin
     * [
     *   ValueInfo(
     *     id = 1,
     *     tableId = "STD_001",
     *     valueData = "{\"name\":\"John\",\"age\":\"15\",\"grade\":\"10th\"}"
     *   ),
     *   ValueInfo(
     *     id = 2,
     *     tableId = "STD_001",
     *     valueData = "{\"name\":\"Sarah\",\"age\":\"16\",\"grade\":\"11th\"}"
     *   )
     * ]
     * ```
     */
    fun getTableValues(tableId: String): Flow<List<ValueInfo>> {
        updateValues(tableId)
        return _valuesFlow
    }

    /**
     * Search values in a specific table
     * @param tableId Table ID
     * @param query Search text
     * @return Flow of values that match the search query
     *
     * Example usage:
     * ```kotlin
     * val searchResults = db.searchTableValues("STD_001", "John").collectAsState(initial = emptyList())
     * ```
     * 
     * Example output:
     * ```kotlin
     * [
     *   ValueInfo(
     *     id = 1,
     *     tableId = "STD_001",
     *     valueData = "{\"name\":\"John\",\"age\":\"15\",\"grade\":\"10th\"}"
     *   )
     * ]
     * ```
     */
    fun searchTableValues(tableId: String, query: String): Flow<List<ValueInfo>> = _valuesFlow
        .map { values ->
            values.filter { value ->
                val json = JSONObject(value.valueData)
                json.keys().asSequence().any { key ->
                    json.optString(key).contains(query, ignoreCase = true)
                }
            }
        }

    /**
     * أنواع البحث في قيم الجدول
     */
    enum class ValueSearchType {
        /** البحث في معرف القيمة */
        VALUE_ID,
        /** البحث في عمود محدد */
        SPECIFIC_COLUMN,
        /** البحث في كل الأعمدة */
        ALL_COLUMNS
    }

    /**
     * خيارات البحث في قيم الجدول
     * @property query نص البحث
     * @property type نوع البحث
     * @property columnName اسم العمود (مطلوب فقط مع SPECIFIC_COLUMN)
     * @property caseSensitive حساسية الأحرف الكبيرة والصغيرة
     * @property exactMatch البحث عن تطابق تام
     * @property limit عدد النتائج المطلوبة (0 يعني بدون حد)
     * @property offset البداية من أي نتيجة
     * @property sortBy ترتيب النتائج حسب عمود معين
     * @property sortDescending ترتيب تنازلي
     */
    data class ValueSearchOptions(
        val query: String,
        val type: ValueSearchType = ValueSearchType.ALL_COLUMNS,
        val columnName: String? = null,
        val caseSensitive: Boolean = false,
        val exactMatch: Boolean = false,
        val limit: Int = 0,
        val offset: Int = 0,
        val sortBy: String? = null,
        val sortDescending: Boolean = false
    )

    /**
     * نتيجة البحث في قيم الجدول
     * @property valueId معرف القيمة
     * @property columnName اسم العمود
     * @property value القيمة المتطابقة
     * @property allValues كل قيم الصف
     */
    data class ValueSearchResult(
        val valueId: String,
        val columnName: String?,
        val value: String,
        val allValues: Map<String, Any>
    )

    /**
     * البحث في قيم الجدول
     * @param tableId معرف الجدول
     * @param options خيارات البحث
     * @return قائمة بنتائج البحث
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // البحث في كل الأعمدة
     * val results1 = db.searchTableValues(
     *     tableId = "STD_001",
     *     options = ValueSearchOptions(
     *         query = "Ahmed"
     *     )
     * )
     *
     * // البحث في عمود محدد
     * val results2 = db.searchTableValues(
     *     tableId = "STD_001",
     *     options = ValueSearchOptions(
     *         query = "12th",
     *         type = ValueSearchType.SPECIFIC_COLUMN,
     *         columnName = "grade"
     *     )
     * )
     *
     * // البحث في معرفات القيم
     * val results3 = db.searchTableValues(
     *     tableId = "STD_001",
     *     options = ValueSearchOptions(
     *         query = "STD_",
     *         type = ValueSearchType.VALUE_ID
     *     )
     * )
     *
     * // البحث مع الترتيب
     * val results4 = db.searchTableValues(
     *     tableId = "STD_001",
     *     options = ValueSearchOptions(
     *         query = "grade",
     *         sortBy = "name",
     *         sortDescending = true
     *     )
     * )
     * ```
     */
    fun searchTableValues(
        tableId: String,
        options: ValueSearchOptions
    ): List<ValueSearchResult> {
        val results = mutableListOf<ValueSearchResult>()
        val query = if (options.caseSensitive) options.query else options.query.lowercase()

        // دالة مساعدة للتحقق من التطابق
        fun checkMatch(value: String): Boolean {
            val testValue = if (options.caseSensitive) value else value.lowercase()
            return when {
                options.exactMatch -> testValue == query
                else -> testValue.contains(query)
            }
        }

        // التحقق من وجود العمود المطلوب للبحث أو الترتيب
        val tableCursor = readableDatabase.query(
            "dynamic_tables",
            arrayOf("columns"),
            "id = ?",
            arrayOf(tableId),
            null, null, null
        )

        val columns = tableCursor.use { cursor ->
            if (cursor.moveToFirst()) {
                JSONArray(cursor.getString(0)).let { array ->
                    List(array.length()) { array.getString(it) }
                }
            } else {
                return emptyList()
            }
        }

        if (options.type == ValueSearchType.SPECIFIC_COLUMN && 
            options.columnName != null && 
            !columns.contains(options.columnName)) {
            return emptyList()
        }

        if (options.sortBy != null && !columns.contains(options.sortBy)) {
            return emptyList()
        }

        // البحث في القيم
        val valuesCursor = readableDatabase.query(
            "dynamic_table_values",
            arrayOf("id", "value_data"),
            "tableId = ?",
            arrayOf(tableId),
            null, null, null
        )

        valuesCursor.use { cursor ->
            while (cursor.moveToNext()) {
                val valueId = cursor.getString(0)
                val valueData = cursor.getString(1)
                val valueJson = JSONObject(valueData)
                val values = mutableMapOf<String, Any>()

                // تحويل القيم إلى Map
                valueJson.keys().forEach { key ->
                    values[key] = valueJson.get(key)
                }

                when (options.type) {
                    ValueSearchType.VALUE_ID -> {
                        if (checkMatch(valueId)) {
                            results.add(
                                ValueSearchResult(
                                    valueId = valueId,
                                    columnName = null,
                                    value = valueId,
                                    allValues = values
                                )
                            )
                        }
                    }
                    ValueSearchType.SPECIFIC_COLUMN -> {
                        val columnName = options.columnName!!
                        if (valueJson.has(columnName)) {
                            val value = valueJson.get(columnName).toString()
                            if (checkMatch(value)) {
                                results.add(
                                    ValueSearchResult(
                                        valueId = valueId,
                                        columnName = columnName,
                                        value = value,
                                        allValues = values
                                    )
                                )
                            }
                        }
                    }
                    ValueSearchType.ALL_COLUMNS -> {
                        valueJson.keys().forEach { key ->
                            val value = valueJson.get(key).toString()
                            if (checkMatch(value)) {
                                results.add(
                                    ValueSearchResult(
                                        valueId = valueId,
                                        columnName = key,
                                        value = value,
                                        allValues = values
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // ترتيب النتائج إذا كان مطلوباً
        if (options.sortBy != null) {
            results.sortBy { result ->
                result.allValues[options.sortBy]?.toString() ?: ""
            }
            if (options.sortDescending) {
                results.reverse()
            }
        }

        // تطبيق الـ offset و limit
        return results
            .drop(options.offset)
            .let { if (options.limit > 0) it.take(options.limit) else it }
    }

    /**
     * إضافة قيمة جديدة للجدول
     * @param tableId معرف الجدول
     * @param values القيم المراد إضافتها على شكل Map
     * @param customId معرف مخصص للقيمة (اختياري)
     * @param stringId معرف نصي للقيمة (اختياري)
     * @return معرف القيمة الجديدة أو null إذا فشلت العملية
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // إضافة قيمة مع توليد معرف تلقائي
     * val newId1 = db.insertValue(
     *     tableId = "STD_001",
     *     values = mapOf(
     *         "name" to "Ahmed",
     *         "age" to "15",
     *         "grade" to "10th"
     *     )
     * )
     *
     * // إضافة قيمة مع تحديد معرف مخصص (رقم)
     * val newId2 = db.insertValue(
     *     tableId = "STD_001",
     *     values = mapOf(
     *         "name" to "Sarah",
     *         "age" to "16",
     *         "grade" to "11th"
     *     ),
     *     customId = 1001
     * )
     *
     * // إضافة قيمة مع تحديد معرف مخصص (نص)
     * val newId3 = db.insertValue(
     *     tableId = "STD_001",
     *     values = mapOf(
     *         "name" to "Mohammed",
     *         "age" to "17",
     *         "grade" to "12th"
     *     ),
     *     customId = "STD_1001"
     * )
     *
     * // إضافة قيمة مع تحديد معرف نصي
     * val newId4 = db.insertValue(
     *     tableId = "STD_001",
     *     values = mapOf(
     *         "name" to "Ali",
     *         "age" to "18",
     *         "grade" to "12th"
     *     ),
     *     stringId = "STD_1002"
     * )
     * ```
     */
    fun insertValue(
        tableId: String,
        values: Map<String, Any>,
        customId: Any? = null,
        stringId: String? = null
    ): String? {
        // Check if the table exists
        val cursor = readableDatabase.query(
            "dynamic_tables",
            arrayOf("columns"),
            "id = ?",
            arrayOf(tableId),
            null, null, null
        )
        
        return cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                // Prepare the value identifier
                val valueId = when {
                    stringId != null -> stringId
                    customId != null -> customId.toString()
                    else -> UUID.randomUUID().toString()
                }

                // Check for existing value with the same identifier
                if (customId != null) {
                    val existingCursor = readableDatabase.query(
                        "dynamic_table_values",
                        arrayOf("id"),
                        "tableId = ? AND id = ?",
                        arrayOf(tableId, valueId),
                        null, null, null
                    )

                    existingCursor.use { existing ->
                        if (existing.moveToFirst()) {
                            return@use null
                        } else {
                            return@use ""
                        }
                    }
                }

                // Insert the new value
                val contentValues = ContentValues().apply {
                    put("id", valueId)
                    put("tableId", tableId)
                    put("value_data", JSONObject(values.toMap()).toString())
                }

                val id = writableDatabase.insert("dynamic_table_values", null, contentValues)
                if (id != -1L) {
                    updateValues(tableId)
                    valueId
                } else {
                    null
                }
            } else {
                null
            }
        }
    }

    /**
     * Delete a column from a table and update all values
     * @param tableId Table ID
     * @param columnName Name of the column to delete
     *
     * Example usage:
     * ```kotlin
     * db.deleteColumn("STD_001", "age")
     * ```
     */
    fun deleteColumn(tableId: String, columnName: String) {
        val cursor = readableDatabase.query(
            "dynamic_tables",
            arrayOf("columns"),
            "id = ?",
            arrayOf(tableId),
            null, null, null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val columnsJson = it.getString(0)
                val columnsArray = JSONArray(columnsJson)
                val newColumns = mutableListOf<String>()
                
                for (i in 0 until columnsArray.length()) {
                    val column = columnsArray.getString(i)
                    if (column != columnName) {
                        newColumns.add(column)
                    }
                }

                val values = ContentValues().apply {
                    put("columns", JSONArray(newColumns).toString())
                }
                writableDatabase.update("dynamic_tables", values, "id = ?", arrayOf(tableId))

                val valuesCursor = readableDatabase.query(
                    "dynamic_table_values",
                    arrayOf("id", "value_data"),
                    "tableId = ?",
                    arrayOf(tableId),
                    null, null, null
                )

                valuesCursor.use { valueCursor ->
                    while (valueCursor.moveToNext()) {
                        val valueId = valueCursor.getString(0)
                        val valueData = valueCursor.getString(1)
                        val valueJson = JSONObject(valueData)
                        valueJson.remove(columnName)
                        
                        val updateValues = ContentValues().apply {
                            put("value_data", valueJson.toString())
                        }
                        writableDatabase.update(
                            "dynamic_table_values",
                            updateValues,
                            "id = ?",
                            arrayOf(valueId)
                        )
                    }
                }
            }
        }
    }

    /**
     * أنواع التحديثات المتاحة للجدول والقيم
     */
    enum class TableUpdateType {
        /** تحديث اسم الجدول */
        NAME,
        /** إضافة عمود جديد */
        ADD_COLUMN,
        /** حذف عمود موجود */
        REMOVE_COLUMN,
        /** تحديث قائمة الأعمدة كاملة */
        COLUMNS,
        /** تحديث قيمة في صف محدد */
        UPDATE_VALUE,
        /** تحديث عدة قيم في صف محدد */
        UPDATE_MULTIPLE_VALUES
    }

    /**
     * تحديث الجدول أو قيمة محددة بطريقة سهلة
     * @param tableId معرف الجدول
     * @param type نوع التحديث
     * @param value القيمة الجديدة
     * @param valueId معرف القيمة (مطلوب فقط عند تحديث قيمة محددة)
     * @param columnName اسم العمود (مطلوب فقط عند تحديث قيمة واحدة)
     * @return true إذا تم التحديث بنجاح
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // تحديث اسم الجدول
     * db.simpleUpdateTable(
     *     tableId = "STD_001",
     *     type = TableUpdateType.NAME,
     *     value = "students_2024"
     * )
     *
     * // إضافة عمود جديد
     * db.simpleUpdateTable(
     *     tableId = "STD_001",
     *     type = TableUpdateType.ADD_COLUMN,
     *     value = "email"
     * )
     *
     * // حذف عمود
     * db.simpleUpdateTable(
     *     tableId = "STD_001",
     *     type = TableUpdateType.REMOVE_COLUMN,
     *     value = "age"
     * )
     *
     * // تحديث قائمة الأعمدة كاملة
     * db.simpleUpdateTable(
     *     tableId = "STD_001",
     *     type = TableUpdateType.COLUMNS,
     *     value = listOf("name", "grade", "email")
     * )
     *
     * // تحديث قيمة واحدة في صف محدد
     * db.simpleUpdateTable(
     *     tableId = "STD_001",
     *     type = TableUpdateType.UPDATE_VALUE,
     *     valueId = "1",
     *     columnName = "grade",
     *     value = "12th"
     * )
     *
     * // تحديث عدة قيم في صف محدد
     * db.simpleUpdateTable(
     *     tableId = "STD_001",
     *     type = TableUpdateType.UPDATE_MULTIPLE_VALUES,
     *     valueId = "1",
     *     value = mapOf(
     *         "grade" to "12th",
     *         "name" to "Ahmed"
     *     )
     * )
     * ```
     */
    @JvmOverloads
    fun simpleUpdateTable(
        tableId: String,
        type: TableUpdateType,
        value: Any? = null,
        valueId: String? = null,
        columnName: String? = null
    ): Boolean {
        return when (type) {
            TableUpdateType.NAME -> {
                writableDatabase.update(
                    "dynamic_tables",
                    ContentValues().apply { put("name", value!!.toString()) },
                    "id = ?",
                    arrayOf(tableId)
                ) > 0
            }
            TableUpdateType.ADD_COLUMN -> {
                val cursor = readableDatabase.query(
                    "dynamic_tables",
                    arrayOf("columns"),
                    "id = ?",
                    arrayOf(tableId),
                    null, null, null
                )

                cursor.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val currentColumns = cursor.getString(0)
                        val columnsList = JSONArray(currentColumns).let { array ->
                            List(array.length()) { array.getString(it) }
                        }.toMutableList()

                        val newColumn = value!!.toString()
                        if (!columnsList.contains(newColumn)) {
                            columnsList.add(newColumn)
                            
                            // تحديث قائمة الأعمدة
                            val updateColumns = writableDatabase.update(
                                "dynamic_tables",
                                ContentValues().apply {
                                    put("columns", JSONArray(columnsList).toString())
                                },
                                "id = ?",
                                arrayOf(tableId)
                            ) > 0
                            
                            // تحديث القيم الموجودة بإضافة العمود الجديد
                            if (updateColumns) {
                                val valuesCursor = readableDatabase.query(
                                    "dynamic_table_values",
                                    arrayOf("id", "value_data"),
                                    "tableId = ?",
                                    arrayOf(tableId),
                                    null, null, null
                                )
                                
                                valuesCursor.use { valueCursor ->
                                    while (valueCursor.moveToNext()) {
                                        val valueId = valueCursor.getString(0)
                                        val valueData = valueCursor.getString(1)
                                        val valueJson = JSONObject(valueData)
                                        
                                        // إضافة العمود الجديد بقيمة فارغة
                                        valueJson.put(newColumn, "")
                                        
                                        writableDatabase.update(
                                            "dynamic_table_values",
                                            ContentValues().apply {
                                                put("value_data", valueJson.toString())
                                            },
                                            "id = ?",
                                            arrayOf(valueId)
                                        )
                                    }
                                }
                            }
                            updateColumns
                        } else true
                    } else false
                }
            }
            TableUpdateType.REMOVE_COLUMN -> {
                val columnName = value!!.toString()
                val cursor = readableDatabase.query(
                    "dynamic_tables",
                    arrayOf("columns"),
                    "id = ?",
                    arrayOf(tableId),
                    null, null, null
                )

                cursor.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val currentColumns = cursor.getString(0)
                        val columnsList = JSONArray(currentColumns).let { array ->
                            List(array.length()) { array.getString(it) }
                        }.toMutableList()

                        if (columnsList.remove(columnName)) {
                            // تحديث الأعمدة في الجدول
                            writableDatabase.update(
                                "dynamic_tables",
                                ContentValues().apply {
                                    put("columns", JSONArray(columnsList).toString())
                                },
                                "id = ?",
                                arrayOf(tableId)
                            )

                            // حذف القيم المرتبطة بهذا العمود
                            val valuesCursor = readableDatabase.query(
                                "dynamic_table_values",
                                arrayOf("id", "value_data"),
                                "tableId = ?",
                                arrayOf(tableId),
                                null, null, null
                            )

                            valuesCursor.use { valueCursor ->
                                while (valueCursor.moveToNext()) {
                                    val valueId = valueCursor.getString(0)
                                    val valueData = valueCursor.getString(1)
                                    val valueJson = JSONObject(valueData)
                                    valueJson.remove(columnName)

                                    writableDatabase.update(
                                        "dynamic_table_values",
                                        ContentValues().apply {
                                            put("value_data", valueJson.toString())
                                        },
                                        "id = ?",
                                        arrayOf(valueId)
                                    )
                                }
                            }
                            true
                        } else true
                    } else false
                }
            }
            TableUpdateType.COLUMNS -> {
                @Suppress("UNCHECKED_CAST")
                val newColumns = value as List<String>
                writableDatabase.update(
                    "dynamic_tables",
                    ContentValues().apply {
                        put("columns", JSONArray(newColumns).toString())
                    },
                    "id = ?",
                    arrayOf(tableId)
                ) > 0
            }
            TableUpdateType.UPDATE_VALUE -> {
                if (valueId == null || columnName == null || value == null) {
                    throw IllegalArgumentException("valueId, columnName and value are required for UPDATE_VALUE")
                }

                val cursor = readableDatabase.query(
                    "dynamic_table_values",
                    arrayOf("value_data"),
                    "tableId = ? AND id = ?",
                    arrayOf(tableId, valueId),
                    null, null, null
                )

                cursor.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val currentData = cursor.getString(0)
                        val currentJson = JSONObject(currentData)
                        currentJson.put(columnName, value)

                        writableDatabase.update(
                            "dynamic_table_values",
                            ContentValues().apply {
                                put("value_data", currentJson.toString())
                            },
                            "tableId = ? AND id = ?",
                            arrayOf(tableId, valueId)
                        ) > 0
                    } else false
                }
            }
            TableUpdateType.UPDATE_MULTIPLE_VALUES -> {
                if (valueId == null || value !is Map<*, *>) {
                    throw IllegalArgumentException("valueId and value (as Map) are required for UPDATE_MULTIPLE_VALUES")
                }

                val cursor = readableDatabase.query(
                    "dynamic_table_values",
                    arrayOf("value_data"),
                    "tableId = ? AND id = ?",
                    arrayOf(tableId, valueId),
                    null, null, null
                )

                cursor.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val currentData = cursor.getString(0)
                        val currentJson = JSONObject(currentData)

                        @Suppress("UNCHECKED_CAST")
                        (value as Map<String, Any>).forEach { (key, v) ->
                            currentJson.put(key, v)
                        }

                        writableDatabase.update(
                            "dynamic_table_values",
                            ContentValues().apply {
                                put("value_data", currentJson.toString())
                            },
                            "tableId = ? AND id = ?",
                            arrayOf(tableId, valueId)
                        ) > 0
                    } else false
                }
            }
        }.also {
            if (it) {
                updateTables()
                updateValues(tableId)
            }
        }
    }

    /**
     * حذف قيمة من الجدول باستخدام ترتيبها في القائمة أو معرفها
     * @param tableId معرف الجدول
     * @param position ترتيب العنصر في القائمة (يبدأ من 1)
     * @param valueId معرف القيمة (اختياري)
     * @param deleteTable إذا كانت true سيتم حذف الجدول نفسه مع كل البيانات
     * @return true إذا تم الحذف بنجاح، false إذا لم يتم العثور على القيمة أو الجدول
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // حذف باستخدام position
     * val success1 = db.deleteValue(
     *     tableId = "STD_001",
     *     position = 1  // حذف العنصر الأول في القائمة
     * )
     *
     * // حذف باستخدام valueId
     * val success2 = db.deleteValue(
     *     tableId = "STD_001",
     *     valueId = "STD_VAL_001"  // حذف العنصر بمعرف نصي
     * )
     *
     * // حذف الجدول بالكامل مع كل البيانات
     * val success3 = db.deleteValue(
     *     tableId = "STD_001",
     *     deleteTable = true  // حذف الجدول نفسه
     * )
     * ```
     */
    fun deleteValue(
        tableId: String,
        position: Int? = null,
        valueId: String? = null,
        deleteTable: Boolean = false
    ): Boolean {
        // إذا كان المطلوب حذف الجدول بالكامل
        if (deleteTable) {
            // حذف كل البيانات أولاً
            writableDatabase.delete(
                "dynamic_table_values",
                "tableId = ?",
                arrayOf(tableId)
            )

            // ثم حذف الجدول نفسه
            val rowsAffected = writableDatabase.delete(
                "dynamic_tables",
                "id = ?",
                arrayOf(tableId)
            )

            updateTables()
            updateValues(tableId)
            return rowsAffected > 0
        }

        // التحقق من المدخلات
        if (position == null && valueId == null) return false
        if (position != null && position < 1) return false

        // البحث عن القيمة المراد حذفها
        val cursor = readableDatabase.query(
            "dynamic_table_values",
            arrayOf("id"),
            if (valueId != null) "id = ? AND tableId = ?" else "tableId = ?",
            if (valueId != null) arrayOf(valueId, tableId) else arrayOf(tableId),
            null, null, null
        )

        return cursor.use { cursor ->
            var currentPosition = 1
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)

                if ((valueId != null && id == valueId) ||
                    (position != null && currentPosition == position)) {

                    val rowsAffected = writableDatabase.delete(
                        "dynamic_table_values",
                        "id = ? AND tableId = ?",
                        arrayOf(id, tableId)
                    )

                    if (rowsAffected > 0) {
                        updateValues(tableId)
                        return@use true
                    }
                }
                currentPosition++
            }
            false
        }
    }

    /**
     * إنشاء جدول جديد مع ID معين (رقمي أو نصي)
     * @param T نوع الـ ID (Int أو String)
     * @param id الـ ID المطلوب للجدول (رقم أو نص)
     * @param name اسم الجدول
     * @param columns قائمة بأسماء الأعمدة
     * @return true إذا تم إنشاء الجدول بنجاح، false إذا كان الـ ID موجود مسبقاً
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // استخدام ID رقمي
     * db.createTableWithId(
     *     id = 1,
     *     name = "students",
     *     columns = listOf("name", "age")
     * )
     *
     * // استخدام ID نصي
     * db.createTableWithId(
     *     id = "STD_001",
     *     name = "students",
     *     columns = listOf("name", "age")
     * )
     * ```
     */
    fun <T> createTableWithId(id: T, name: String, columns: List<String>): Boolean where T : Any {
        val db = writableDatabase

        // تحويل الـ ID إلى نص للتخزين
        val idString = id.toString()

        // التحقق من عدم وجود جدول بنفس الـ ID
        val cursor = db.query(
            "dynamic_tables",
            arrayOf("id"),
            "id = ?",
            arrayOf(idString),
            null, null, null
        )

        val exists = cursor.use { it.moveToFirst() }
        if (exists) {
            return false
        }

        // إنشاء الجدول الجديد
        val values = ContentValues().apply {
            put("id", idString)
            put("name", name)
            put("columns", JSONArray(columns).toString())
        }

        return try {
            db.beginTransaction()
            val result = db.insert("dynamic_tables", null, values)
            if (result != -1L) {
                db.setTransactionSuccessful()
                true
            } else {
                false
            }
        } finally {
            db.endTransaction()
        }
    }

    /**
     * تحديث معلومات الجدول (الاسم و/أو الأعمدة)
     * يمكنك تحديث قيمة واحدة أو أكثر حسب الحاجة
     * @param tableId معرف الجدول المراد تحديثه
     * @param updates التحديثات المطلوبة على شكل Map
     * @return true إذا تم التحديث بنجاح، false إذا لم يتم العثور على الجدول
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // تحديث اسم الجدول فقط
     * db.updateTable(
     *     tableId = "STD_001",
     *     updates = mapOf(
     *         "name" to "students_2024"
     *     )
     * )
     *
     * // تحديث عمود واحد فقط
     * db.updateTable(
     *     tableId = "STD_001",
     *     updates = mapOf(
     *         "addColumn" to "email"
     *     )
     * )
     *
     * // حذف عمود
     * db.updateTable(
     *     tableId = "STD_001",
     *     updates = mapOf(
     *         "removeColumn" to "age"
     *     )
     * )
     *
     * // تحديثات متعددة في نفس الوقت
     * db.updateTable(
     *     tableId = "STD_001",
     *     updates = mapOf(
     *         "name" to "students_2024",
     *         "addColumn" to "email",
     *         "removeColumn" to "age"
     *     )
     * )
     *
     * // تحديث قائمة الأعمدة كاملة
     * db.updateTable(
     *     tableId = "STD_001",
     *     updates = mapOf(
     *         "columns" to listOf("name", "grade", "email")
     *     )
     * )
     * ```
     */
    fun updateTable(
        tableId: String,
        updates: Map<String, Any>
    ): Boolean {
        // التحقق من وجود الجدول وجلب البيانات الحالية
        val cursor = readableDatabase.query(
            "dynamic_tables",
            arrayOf("name", "columns"),
            "id = ?",
            arrayOf(tableId),
            null, null, null
        )

        return cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val currentName = cursor.getString(0)
                val currentColumns = cursor.getString(1)
                val currentColumnsList = JSONArray(currentColumns).let { array ->
                    List(array.length()) { array.getString(it) }
                }.toMutableList()

                // تحضير القيم الجديدة
                val values = ContentValues()
                var columnsChanged = false

                // معالجة التحديثات
                updates.forEach { (key, value) ->
                    when (key) {
                        "name" -> values.put("name", value.toString())
                        "addColumn" -> {
                            val columnName = value.toString()
                            if (!currentColumnsList.contains(columnName)) {
                                currentColumnsList.add(columnName)
                                columnsChanged = true
                            }
                        }
                        "removeColumn" -> {
                            val columnName = value.toString()
                            if (currentColumnsList.remove(columnName)) {
                                columnsChanged = true
                            }
                        }
                        "columns" -> {
                            @Suppress("UNCHECKED_CAST")
                            currentColumnsList.clear()
                            currentColumnsList.addAll((value as List<String>))
                            columnsChanged = true
                        }
                    }
                }

                // تحديث الأعمدة إذا تم تغييرها
                if (columnsChanged) {
                    values.put("columns", JSONArray(currentColumnsList).toString())

                    // تحديث قيم الجدول لتتناسب مع الأعمدة الجديدة
                    val valuesCursor = readableDatabase.query(
                        "dynamic_table_values",
                        arrayOf("id", "value_data"),
                        "tableId = ?",
                        arrayOf(tableId),
                        null, null, null
                    )

                    valuesCursor.use { valCursor ->
                        while (valCursor.moveToNext()) {
                            val valueId = valCursor.getString(0)
                            val valueData = valCursor.getString(1)
                            val valueJson = JSONObject(valueData)

                            // حذف القيم غير الموجودة في الأعمدة الجديدة
                            for (key in valueJson.keys()) {
                                if (!currentColumnsList.contains(key)) {
                                    valueJson.remove(key)
                                }
                            }

                            // تحديث قيم الصف
                            val valueUpdateValues = ContentValues().apply {
                                put("value_data", valueJson.toString())
                            }
                            writableDatabase.update(
                                "dynamic_table_values",
                                valueUpdateValues,
                                "id = ?",
                                arrayOf(valueId)
                            )
                        }
                    }
                }

                // تنفيذ التحديث إذا كانت هناك تغييرات
                if (values.size() > 0) {
                    val rowsAffected = writableDatabase.update(
                        "dynamic_tables",
                        values,
                        "id = ?",
                        arrayOf(tableId)
                    )

                    if (rowsAffected > 0) {
                        updateTables()
                        updateValues(tableId)
                        return@use true
                    }
                }

                // إذا لم تكن هناك تحديثات أو تم التحديث بنجاح
                true
            } else {
                false
            }
        }
    }

    /**
     * حذف قيم من الجدول بناءً على شروط معينة
     * @param tableId معرف الجدول
     * @param conditions شروط الحذف على شكل Map
     * @param matchAll إذا كانت true يجب تحقق كل الشروط، إذا كانت false يكفي تحقق شرط واحد
     * @return عدد العناصر التي تم حذفها
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // حذف كل الطلاب في الصف العاشر
     * val deletedCount1 = db.deleteValueByCondition(
     *     tableId = "STD_001",
     *     conditions = mapOf(
     *         "grade" to "10th"
     *     )
     * )
     *
     * // حذف الطلاب الذين عمرهم 15 وفي الصف العاشر (يجب تحقق كل الشروط)
     * val deletedCount2 = db.deleteValueByCondition(
     *     tableId = "STD_001",
     *     conditions = mapOf(
     *         "age" to "15",
     *         "grade" to "10th"
     *     ),
     *     matchAll = true
     * )
     *
     * // حذف الطلاب الذين عمرهم 15 أو في الصف العاشر (يكفي تحقق شرط واحد)
     * val deletedCount3 = db.deleteValueByCondition(
     *     tableId = "STD_001",
     *     conditions = mapOf(
     *         "age" to "15",
     *         "grade" to "10th"
     *     ),
     *     matchAll = false
     * )
     * ```
     */
    fun deleteValueByCondition(
        tableId: String,
        conditions: Map<String, Any>,
        matchAll: Boolean = true
    ): Int {
        val cursor = readableDatabase.query(
            "dynamic_table_values",
            arrayOf("id", "value_data"),
            "tableId = ?",
            arrayOf(tableId),
            null, null, null
        )

        val idsToDelete = mutableListOf<String>()

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val valueData = cursor.getString(1)
                val valueJson = JSONObject(valueData)

                var matchesConditions = matchAll
                for ((key, value) in conditions) {
                    val hasMatch = when (val jsonValue = valueJson.opt(key)) {
                        null -> false
                        else -> jsonValue.toString() == value.toString()
                    }

                    if (matchAll && !hasMatch) {
                        // إذا كان مطلوب تحقق كل الشروط وهذا الشرط غير متحقق
                        matchesConditions = false
                        break
                    } else if (!matchAll && hasMatch) {
                        // إذا كان يكفي تحقق شرط واحد وهذا الشرط متحقق
                        matchesConditions = true
                        break
                    }
                }

                if (matchesConditions) {
                    idsToDelete.add(id)
                }
            }
        }

        if (idsToDelete.isEmpty()) {
            return 0
        }

        // بناء عبارة الحذف
        val whereClause = StringBuilder("id IN (")
        val whereArgs = Array(idsToDelete.size) { "?" }
        whereClause.append(whereArgs.joinToString(","))
        whereClause.append(") AND tableId = ?")

        // تجهيز قائمة المعاملات
        val args = idsToDelete.toTypedArray() + tableId

        // تنفيذ عملية الحذف
        val deletedCount = writableDatabase.delete(
            "dynamic_table_values",
            whereClause.toString(),
            args
        )

        if (deletedCount > 0) {
            updateValues(tableId)
        }

        return deletedCount
    }

    /**
     * تحديث قيمة محددة في الجدول باستخدام معرف القيمة
     * @param tableId معرف الجدول
     * @param valueId معرف القيمة المراد تحديثها
     * @param updates التحديثات المطلوبة على شكل Map
     * @return true إذا تم التحديث بنجاح
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // تحديث قيمة واحدة فقط
     * db.updateValueById(
     *     tableId = "STD_001",
     *     valueId = "1",
     *     updates = mapOf(
     *         "name" to "Ahmed",
     *         "grade" to "11th"
     *     )
     * )
     * ```
     */
    fun updateValueById(
        tableId: String,
        valueId: String,
        updates: Map<String, Any>
    ): Boolean {
        // التحقق من وجود القيمة
        val cursor = readableDatabase.query(
            "dynamic_table_values",
            arrayOf("value_data"),
            "tableId = ? AND id = ?",
            arrayOf(tableId, valueId),
            null, null, null
        )

        return cursor.use { cursor ->
            if (cursor.moveToFirst()) {
                val currentData = cursor.getString(0)
                val currentJson = JSONObject(currentData)

                // تحديث القيم
                updates.forEach { (key, value) ->
                    currentJson.put(key, value)
                }

                // حفظ التحديثات
                val values = ContentValues().apply {
                    put("value_data", currentJson.toString())
                }

                val rowsAffected = writableDatabase.update(
                    "dynamic_table_values",
                    values,
                    "tableId = ? AND id = ?",
                    arrayOf(tableId, valueId)
                )

                if (rowsAffected > 0) {
                    updateValues(tableId)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    /**
     * أنواع تحديثات القيم
     */
    enum class ValueUpdateType {
        /** تحديث قيمة واحدة */
        SINGLE_VALUE,
        /** تحديث عدة قيم */
        MULTIPLE_VALUES
    }

    /**
     * تحديث قيمة محددة بطريقة سهلة
     * @param tableId معرف الجدول
     * @param valueId معرف القيمة
     * @param columnName اسم العمود
     * @param value القيمة الجديدة
     * @param type نوع التحديث
     * @param updates التحديثات المطلوبة على شكل Map
     * @return true إذا تم التحديث بنجاح
     *
     * مثال على الاستخدام:
     * ```kotlin
     * // تحديث قيمة واحدة
     * db.simpleUpdateValue(
     *     tableId = "STD_001",
     *     valueId = "1",
     *     columnName = "grade",
     *     value = "12th"
     * )
     *
     * // تحديث عدة قيم
     * db.simpleUpdateValue(
     *     tableId = "STD_001",
     *     valueId = "1",
     *     type = ValueUpdateType.MULTIPLE_VALUES,
     *     updates = mapOf(
     *         "grade" to "12th",
     *         "name" to "Ahmed"
     *     )
     * )
     * ```
     */
    @JvmOverloads
    fun simpleUpdateValue(
        tableId: String,
        valueId: String,
        columnName: String? = null,
        value: Any? = null,
        type: ValueUpdateType = ValueUpdateType.SINGLE_VALUE,
        updates: Map<String, Any>? = null
    ): Boolean {
        return when (type) {
            ValueUpdateType.SINGLE_VALUE -> {
                if (columnName == null || value == null) {
                    throw IllegalArgumentException("columnName and value must not be null for SINGLE_VALUE update")
                }
                updateValueById(tableId, valueId, mapOf(columnName to value))
            }
            ValueUpdateType.MULTIPLE_VALUES -> {
                if (updates == null) {
                    throw IllegalArgumentException("updates must not be null for MULTIPLE_VALUES update")
                }
                updateValueById(tableId, valueId, updates)
            }
        }
    }

    /**
     * معلومات العلاقة بين الجداول
     */
    data class TableRelationship(
        val id: String,
        val sourceTableId: String,
        val targetTableId: String,
        val type: TableRelationType,
        val sourceColumn: String,
        val targetColumn: String
    )

    /**
     * دالة لإنشاء علاقة بين جدولين
     */
    fun createTableRelationship(
        id: String,
        sourceTableId: String,
        targetTableId: String,
        type: TableRelationType,
        sourceColumn: String,
        targetColumn: String
    ): String {
        val contentValues = ContentValues().apply {
            put("id", id)
            put("source_table_id", sourceTableId)
            put("target_table_id", targetTableId)
            put("relationship_type", type.name)
            put("source_column", sourceColumn)
            put("target_column", targetColumn)
        }

        writableDatabase.insert("table_relationships", null, contentValues)
        updateRelationships()
        return id
    }

    /**
     * تحديث قائمة العلاقات في الذاكرة
     */
    private fun updateRelationships() {
        val relationships = mutableListOf<TableRelationship>()
        val cursor = readableDatabase.query(
            "table_relationships",
            arrayOf("id", "source_table_id", "target_table_id", "relationship_type", "source_column", "target_column"),
            null, null, null, null, null
        )

        cursor.use {
            while (it.moveToNext()) {
                relationships.add(TableRelationship(
                    id = it.getString(0),
                    sourceTableId = it.getString(1),
                    targetTableId = it.getString(2),
                    type = TableRelationType.valueOf(it.getString(3)),
                    sourceColumn = it.getString(4),
                    targetColumn = it.getString(5)
                ))
            }
        }
        _relationshipsFlow.value = relationships
    }

    /**
     * الحصول على كل العلاقات في قاعدة البيانات
     * @return تدفق من قائمة العلاقات
     */
    fun getAllRelationships(): Flow<List<TableRelationship>> = _relationshipsFlow

    /**
     * الحصول على العلاقات لجدول معين
     * @param tableId معرف الجدول
     * @return تدفق من قائمة العلاقات المرتبطة بالجدول
     */
    fun getTableRelationships(tableId: String): Flow<List<TableRelationship>> = _relationshipsFlow
        .map { relationships ->
            relationships.filter { 
                it.sourceTableId == tableId || it.targetTableId == tableId 
            }
        }

    /**
     * الحصول على قيم جدول معين
     */
    private fun getValuesForTable(tableId: String): List<ValueInfo> {
        val values = mutableListOf<ValueInfo>()
        val cursor = readableDatabase.query(
            "dynamic_table_values",
            arrayOf("id", "tableId", "value_data"),
            "tableId = ?",
            arrayOf(tableId),
            null, null, null
        )
        
        cursor.use {
            while (it.moveToNext()) {
                values.add(ValueInfo(
                    it.getString(0),
                    it.getString(1),
                    it.getString(2)
                ))
            }
        }
        return values
    }

    /**
     * الحصول على القيم المرتبطة في جدول آخر
     * @param sourceTableId معرف الجدول المصدر
     * @param sourceValueId معرف القيمة في الجدول المصدر
     * @param relationshipId معرف العلاقة
     * @return قائمة بالقيم المرتبطة
     */
    fun getRelatedValues(
        sourceTableId: String,
        sourceValueId: String,
        relationshipId: String
    ): List<ValueInfo> {
        val relationship = _relationshipsFlow.value.find { it.id == relationshipId } ?: return emptyList()
        
        val sourceCursor = readableDatabase.query(
            "dynamic_table_values",
            arrayOf("value_data"),
            "tableId = ? AND id = ?",
            arrayOf(sourceTableId, sourceValueId),
            null, null, null
        )

        return sourceCursor.use { source ->
            if (!source.moveToFirst()) return emptyList()
            
            val sourceData = JSONObject(source.getString(0))
            val sourceValue = sourceData.opt(relationship.sourceColumn)?.toString() ?: return emptyList()

            val targetCursor = readableDatabase.query(
                "dynamic_table_values",
                arrayOf("id", "tableId", "value_data"),
                "tableId = ?",
                arrayOf(relationship.targetTableId),
                null, null, null
            )

            val relatedValues = mutableListOf<ValueInfo>()
            targetCursor.use { target ->
                while (target.moveToNext()) {
                    val valueData = target.getString(2)
                    val valueJson = JSONObject(valueData)
                    
                    if (valueJson.opt(relationship.targetColumn)?.toString() == sourceValue) {
                        relatedValues.add(ValueInfo(
                            target.getString(0),
                            target.getString(1),
                            valueData
                        ))
                    }
                }
            }
            relatedValues
        }
    }

    /**
     * الحصول على قيم الجدول والقيم المرتبطة في نفس الوقت
     * @param sourceTableId معرف الجدول المصدر
     * @param sourceValueId معرف القيمة في الجدول المصدر
     * @param relationshipId معرف العلاقة
     * @return Pair من قيمة الجدول المصدر والقيم المرتبطة
     */
    fun getTableAndRelatedValues(
        sourceTableId: String,
        sourceValueId: String,
        relationshipId: String
    ): Pair<List<ValueInfo>, List<ValueInfo>> {
        // Check if relationship exists first
        val relationship = _relationshipsFlow.value.find { it.id == relationshipId }
        if (relationship == null) {
            println("Relationship not found: $relationshipId")
            return Pair(emptyList(), emptyList())
        }

        // Get all source table values
        val sourceValues = mutableListOf<ValueInfo>()
        readableDatabase.query(
            "dynamic_table_values",
            arrayOf("id", "tableId", "value_data"),
            "tableId = ? AND id = ?",
            arrayOf(sourceTableId, sourceValueId),
            null, null, null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                sourceValues.add(
                    ValueInfo(
                        cursor.getString(0),
                        cursor.getString(1),
                        cursor.getString(2)
                    ).also {
                        println("Source value found: ${it.valueData}")
                    }
                )
            }
        }

        if (sourceValues.isEmpty()) {
            println("No source values found for: $sourceValueId")
            return Pair(emptyList(), emptyList())
        }

        // Get related values for all source values
        val relatedValues = getRelatedValues(sourceTableId, sourceValueId, relationshipId).also {
            println("Found ${it.size} related values")
        }

        return Pair(sourceValues, relatedValues)
    }

    /**
     * دالة مساعدة لإنشاء علاقة بين جدولين بشكل سريع
     * @param sourceTableName اسم الجدول المصدر
     * @param targetTableName اسم الجدول الهدف
     * @param type نوع العلاقة
     * @param sourceColumn عمود الربط في الجدول المصدر (افتراضياً 'id')
     * @param targetColumn عمود الربط في الجدول الهدف
     * @return معرف العلاقة الجديدة
     */
    fun quickRelation(
        sourceTableName: String,
        targetTableName: String,
        type: TableRelationType,
        sourceColumn: String = "id",
        targetColumn: String
    ): String {
        val sourceTable = _tablesFlow.value.find { it.name == sourceTableName }
            ?: throw IllegalArgumentException("Source table $sourceTableName not found")
        
        val targetTable = _tablesFlow.value.find { it.name == targetTableName }
            ?: throw IllegalArgumentException("Target table $targetTableName not found")

        val relationshipId = UUID.randomUUID().toString()
        createTableRelationship(
            id = relationshipId,
            sourceTableId = sourceTable.id,
            targetTableId = targetTable.id,
            type = type,
            sourceColumn = sourceColumn,
            targetColumn = targetColumn
        )
        return relationshipId
    }

    /**
     * دالة للحصول على كل القيم المرتبطة بقيمة معينة
     * @param tableName اسم الجدول
     * @param valueId معرف القيمة
     * @param relatedTableName اسم الجدول المرتبط
     * @return قائمة بالقيم المرتبطة
     */
    fun getLinkedValues(
        tableName: String,
        valueId: String,
        relatedTableName: String
    ): List<ValueInfo> {
        val table = _tablesFlow.value.find { it.name == tableName }
            ?: throw IllegalArgumentException("Table $tableName not found")
        
        val relatedTable = _tablesFlow.value.find { it.name == relatedTableName }
            ?: throw IllegalArgumentException("Related table $relatedTableName not found")

        val relationship = _relationshipsFlow.value.find { 
            (it.sourceTableId == table.id && it.targetTableId == relatedTable.id) ||
            (it.sourceTableId == relatedTable.id && it.targetTableId == table.id)
        } ?: throw IllegalArgumentException("No relationship found between $tableName and $relatedTableName")

        return when {
            relationship.sourceTableId == table.id -> {
                getValuesForTable(relationship.targetTableId)
                    .filter { it.tableId == relationship.targetTableId }
            }
            else -> {
                getValuesForTable(relationship.sourceTableId)
                    .filter { it.tableId == relationship.sourceTableId }
            }
        }
    }

    /**
     * دالة للربط السريع بين قيمتين في جدولين مختلفين
     * @param sourceTableName اسم الجدول المصدر
     * @param sourceValueId معرف القيمة في الجدول المصدر
     * @param targetTableName اسم الجدول الهدف
     * @param targetValueId معرف القيمة في الجدول الهدف
     */
    fun linkValues(
        sourceTableName: String,
        sourceValueId: String,
        targetTableName: String,
        targetValueId: String
    ) {
        val relationship = _relationshipsFlow.value.find { rel ->
            val sourceTable = _tablesFlow.value.find { it.name == sourceTableName }
            val targetTable = _tablesFlow.value.find { it.name == targetTableName }
            (rel.sourceTableId == sourceTable?.id && rel.targetTableId == targetTable?.id) ||
            (rel.sourceTableId == targetTable?.id && rel.targetTableId == sourceTable?.id)
        } ?: throw IllegalArgumentException("No relationship found between $sourceTableName and $targetTableName")

        when (relationship.type) {
            TableRelationType.ONE_TO_ONE, TableRelationType.ONE_TO_MANY -> {
                val values = mapOf(relationship.targetColumn to sourceValueId)
                val contentValues = ContentValues().apply {
                    values.forEach { (key, value) ->
                        put(key, value.toString())
                    }
                }
                writableDatabase.update(
                    "dynamic_table_values",
                    contentValues,
                    "id = ?",
                    arrayOf(targetValueId)
                )
                updateValues(relationship.targetTableId)
            }
            TableRelationType.MANY_TO_MANY -> {
                val junctionTableId = "${relationship.sourceTableId}_${relationship.targetTableId}"
                val values = mapOf(
                    relationship.sourceColumn to sourceValueId,
                    relationship.targetColumn to targetValueId
                )
                insertValue(junctionTableId, values)
            }
        }
    }

    /**
     * Class representing table information
     * @property id Table ID (can be numeric or string)
     * @property name Table name
     * @property columns JSON string containing column names
     * 
     * Example:
     * ```kotlin
     * TableInfo(
     *   id = "STD_001", // يمكن أن يكون رقم أو نص
     *   name = "students",
     *   columns = "[\"name\",\"age\",\"grade\"]"
     * )
     * ```
     */
    data class TableInfo(
        val id: String, // تم تغيير النوع إلى String ليدعم الأرقام والنصوص
        val name: String,
        val columns: String // تم تغيير النوع من List<String> إلى String
    )

    /**
     * Class representing a value in a table
     * @property id Value ID (can be numeric or string)
     * @property tableId ID of the table this value belongs to
     * @property valueData JSON string containing the values
     * 
     * Example:
     * ```kotlin
     * ValueInfo(
     *   id = "VAL_001", // يمكن أن يكون رقم أو نص
     *   tableId = "STD_001",
     *   valueData = "{\"name\":\"John\",\"age\":\"15\",\"grade\":\"10th\"}"
     * )
     * ```
     */
    data class ValueInfo(
        val id: String, // تم تغيير النوع إلى String ليدعم الأرقام والنصوص
        val tableId: String,
        val valueData: String
    )
}