package com.kotlinclasses

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Network connectivity manager that provides functions to check and monitor network status.
 * مدير الاتصال بالشبكة الذي يوفر وظائف للتحقق من حالة الشبكة ومراقبتها
 */
class TPNetManager {
    companion object {
        private const val TAG = "TPNetManager"
        private var context: Context? = null

        /**
         * Initializes the TPNetManager with an application context.
         * Must be called before using any other methods in this class.
         * 
         * Example usage:
         * ```kotlin
         * override fun onCreate() {
         *     super.onCreate()
         *     TPNetManager.init(applicationContext)
         * }
         * ```
         * 
         * @param appContext The application context
         * يجب تهيئة المدير قبل استخدام أي من الدوال الأخرى
         */
        fun init(appContext: Context) {
            context = appContext
        }

        /**
         * Checks if the device is currently connected to a network.
         * يتحقق مما إذا كان الجهاز متصلاً حالياً بالشبكة
         * 
         * Example usage:
         * ```kotlin
         * if (TPNetManager.isConnected()) {
         *     println("Device is connected to network")
         * } else {
         *     println("No network connection")
         * }
         * ```
         * 
         * @return true if connected to any network, false otherwise
         * @throws IllegalStateException if context is not initialized
         */
        fun isConnected(): Boolean {
            val connectivityManager = getConnectivityManager()
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        }

        /**
         * Checks if the device is connected to WiFi.
         * يتحقق مما إذا كان الجهاز متصلاً بشبكة WiFi
         * 
         * Example usage:
         * ```kotlin
         * if (TPNetManager.isWifiConnected()) {
         *     println("Connected to WiFi")
         * } else {
         *     println("Not connected to WiFi")
         * }
         * ```
         * 
         * @return true if connected to WiFi, false otherwise
         * @throws IllegalStateException if context is not initialized
         */
        fun isWifiConnected(): Boolean {
            val connectivityManager = getConnectivityManager()
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
        }

        /**
         * Checks if the device is connected to cellular network.
         * يتحقق مما إذا كان الجهاز متصلاً بشبكة الجوال
         * 
         * Example usage:
         * ```kotlin
         * if (TPNetManager.isCellularConnected()) {
         *     println("Connected to cellular network")
         * } else {
         *     println("Not connected to cellular network")
         * }
         * ```
         * 
         * @return true if connected to cellular network, false otherwise
         * @throws IllegalStateException if context is not initialized
         */
        fun isCellularConnected(): Boolean {
            val connectivityManager = getConnectivityManager()
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            
            return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true
        }

        /**
         * Provides a Flow that emits network connectivity status changes.
         * يوفر تدفقاً يرسل تغييرات حالة الاتصال بالشبكة
         * 
         * Example usage:
         * ```kotlin
         * // In a ViewModel or Composable
         * val networkStatus = TPNetManager.observeNetworkStatus()
         *     .collectAsState(initial = false)
         * 
         * Text(
         *     text = if (networkStatus.value) "Connected" else "Disconnected"
         * )
         * ```
         * 
         * @return Flow that emits true when connected, false when disconnected
         * @throws IllegalStateException if context is not initialized
         */
        fun observeNetworkStatus(): Flow<Boolean> = callbackFlow {
            val connectivityManager = getConnectivityManager()
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: android.net.Network) {
                    trySend(true)
                }

                override fun onLost(network: android.net.Network) {
                    trySend(false)
                }

                override fun onCapabilitiesChanged(
                    network: android.net.Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    val hasInternet = networkCapabilities.hasCapability(
                        NetworkCapabilities.NET_CAPABILITY_INTERNET
                    )
                    trySend(hasInternet)
                }
            }

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()

            connectivityManager.registerNetworkCallback(request, callback)

            // Send initial value
            trySend(isConnected())

            awaitClose {
                connectivityManager.unregisterNetworkCallback(callback)
            }
        }

        private fun getConnectivityManager(): ConnectivityManager {
            if (context == null) {
                Log.e(TAG, "Context is null. Did you forget to call TPNetManager.init()?")
                throw IllegalStateException("TPNetManager is not initialized")
            }
            return context!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        }
    }
}