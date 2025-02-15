package com.example.classescreator.examples

import com.kotlinclasses.TPNetManager
import kotlinx.coroutines.flow.collectLatest

class TPNetManagerExamples {
    companion object {
        /**
         * Example of checking basic network connectivity
         * مثال للتحقق من الاتصال الأساسي بالشبكة
         */
        fun checkBasicConnectivity() {
            println("=== Basic Connectivity Check ===")
            if (TPNetManager.isConnected()) {
                println("✅ Device is connected to network")
            } else {
                println("❌ No network connection")
            }
        }

        /**
         * Example of checking specific network types
         * مثال للتحقق من أنواع محددة من الشبكات
         */
        fun checkSpecificNetworks() {
            println("\n=== Specific Network Checks ===")
            // Check WiFi connection
            if (TPNetManager.isWifiConnected()) {
                println("📶 Connected to WiFi")
            } else {
                println("❌ Not connected to WiFi")
            }

            // Check Cellular connection
            if (TPNetManager.isCellularConnected()) {
                println("📱 Connected to cellular network")
            } else {
                println("❌ Not connected to cellular network")
            }
        }

        /**
         * Example of monitoring network status changes using Flow
         * مثال لمراقبة تغييرات حالة الشبكة باستخدام Flow
         */
        suspend fun monitorNetworkChanges() {
            println("\n=== Network Status Monitor ===")
            println("Monitoring network changes... (Press Ctrl+C to stop)")
            
            TPNetManager.observeNetworkStatus().collectLatest { isConnected ->
                if (isConnected) {
                    println("🌐 Network connection established")
                } else {
                    println("❌ Network connection lost")
                }
            }
        }

        /**
         * Prints all network information at once
         * طباعة جميع معلومات الشبكة دفعة واحدة
         */
        fun printAllNetworkInfo() {
            println("\n====== Network Information ======")
            println("General Connection: ${if (TPNetManager.isConnected()) "✅ Connected" else "❌ Disconnected"}")
            println("WiFi Status: ${if (TPNetManager.isWifiConnected()) "📶 Connected" else "❌ Disconnected"}")
            println("Cellular Status: ${if (TPNetManager.isCellularConnected()) "📱 Connected" else "❌ Disconnected"}")
            println("==============================")
        }
    }
}

