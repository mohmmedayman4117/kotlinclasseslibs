package com.kotlinclasses


interface TPFirebaseInstanceFields {
    val ID: String
    var classStructName: String
}
class TPFirebaseServices {

    companion object {

        /// - PostCondition: Property that have nil value, it will be set to "nil" (String) in returned dictionary
        fun convertToDictionary(instance: TPFirebaseInstanceFields?): Map<String, Any>? {
            if (instance == null) return null
            
            return try {
                val dictionary = mutableMapOf<String, Any>()
                // Add the standard fields from TPFirebaseInstanceFields interface
                dictionary["ID"] = instance.ID
                dictionary["classStructName"] = instance.classStructName
                
                // Use reflection to get all declared properties
                instance::class.java.declaredFields.forEach { field ->
                    field.isAccessible = true
                    val value = field.get(instance)
                    
                    // Skip ID and classStructName as they're already added
                    if (field.name != "ID" && field.name != "classStructName") {
                        when (value) {
                            null -> dictionary[field.name] = "nil"
                            is TPFirebaseInstanceFields -> {
                                // Recursively convert nested TPFirebaseInstanceFields objects
                                convertToDictionary(value)?.let { 
                                    dictionary[field.name] = it 
                                }
                            }
                            is List<*> -> {
                                // Handle lists of TPFirebaseInstanceFields objects
                                val list = value.filterIsInstance<TPFirebaseInstanceFields>()
                                if (list.isNotEmpty()) {
                                    dictionary[field.name] = convertMultiInstancesToDictionaryList(list)
                                } else {
                                    // For other types of lists, store as is if serializable
                                    if (value.all { it == null || it is String || it is Number || it is Boolean }) {
                                        dictionary[field.name] = value
                                    }
                                }
                            }
                            // Handle primitive types and strings
                            is String, is Number, is Boolean -> dictionary[field.name] = value
                            else -> {
                                // For other types, convert to string representation
                                dictionary[field.name] = value.toString()
                            }
                        }
                    }
                }
                dictionary
            } catch (e: Exception) {
                null
            }
        }

        fun convertMultiInstancesToDictionaryList(instancesList: List<TPFirebaseInstanceFields>): List<Map<String, Any?>?> {
            return instancesList.map { instance ->
                convertToDictionary(instance)
            }
        }
    }
}