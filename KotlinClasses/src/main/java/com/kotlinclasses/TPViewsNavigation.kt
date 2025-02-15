package com.kotlinclasses

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * فئة للتحكم في التنقل بين الشاشات في التطبيق
 * Navigation controller class for handling screen navigation in the app
 *
 * طريقة الاستخدام / Usage:
 * ```kotlin
 * // في الشاشة الرئيسية / In MainActivity
 * val navigation = remember { TPViewsNavigation() }
 * 
 * TPViewsNavigation.CreateNavigation(
 *     startDestination = "screen1",
 *     navigation = navigation
 * ) {
 *     addScreen("screen1") { Screen1() }
 *     addScreen("screen2") { Screen2() }
 * }
 * 
 * // في أي شاشة / In any screen
 * navigation.navigateTo("screen2")        // الانتقال إلى شاشة / Navigate to screen
 * navigation.navigateBack()               // العودة للخلف / Go back
 * navigation.navigateAndClear("screen1")  // العودة مع مسح المكدس / Clear stack and navigate
 * navigation.navigateToActivity(SecondActivity::class.java) // الانتقال إلى نشاط / Navigate to activity
 * ```
 */
class TPViewsNavigation {
    private var navController: NavHostController? = null
    private var context: Context? = null

    /**
     * تعيين متحكم التنقل والسياق
     * Set the navigation controller and context
     */
    fun setNavController(controller: NavHostController, activityContext: Context) {
        navController = controller
        context = activityContext
    }
    
    /**
     * الانتقال إلى شاشة محددة
     * Navigate to a specific screen
     * 
     * @param route مسار الشاشة / Screen route
     * @param args معاملات التنقل (اختياري) / Navigation arguments (optional)
     * 
     * مثال / Example:
     * ```kotlin
     * // تنقل بسيط / Simple navigation
     * navigateTo("profile")
     * 
     * // تنقل مع معاملات / Navigation with arguments
     * navigateTo("profile/{id}", listOf("id" to "123"))
     * ```
     */
    fun navigateTo(route: String, args: List<Pair<String, String>> = emptyList()) {
        var finalRoute = route
        args.forEach { (key, value) ->
            finalRoute = finalRoute.replace("{$key}", value)
        }
        navController?.navigate(finalRoute)
    }

    /**
     * العودة إلى الشاشة السابقة
     * Navigate back to previous screen
     */
    fun navigateBack() {
        navController?.popBackStack()
    }

    /**
     * الانتقال إلى شاشة مع مسح كل المكدس
     * Navigate to a screen and clear the entire back stack
     * 
     * @param route مسار الشاشة / Screen route
     * 
     * مثال / Example:
     * ```kotlin
     * // العودة إلى الشاشة الرئيسية / Return to main screen
     * navigateAndClear("main")
     * 
     * // الخروج وتسجيل الدخول / Logout and login
     * navigateAndClear("login")
     * ```
     */
    fun navigateAndClear(route: String) {
        navController?.navigate(route) {
            popUpTo(0) { inclusive = true }
        }
    }

    /**
     * الانتقال إلى نشاط (Activity) جديد
     * Navigate to a new Activity
     * 
     * @param activityClass النشاط المراد الانتقال إليه / Target Activity class
     * @param finishCurrent إنهاء النشاط الحالي (اختياري) / Finish current activity (optional)
     * @param flags خيارات التنقل (اختياري) / Navigation flags (optional)
     * 
     * مثال / Example:
     * ```kotlin
     * // انتقال بسيط / Simple navigation
     * navigateToActivity(SecondActivity::class.java)
     * 
     * // انتقال مع إنهاء النشاط الحالي / Navigate and finish current
     * navigateToActivity(SecondActivity::class.java, finishCurrent = true)
     * 
     * // انتقال مع خيارات / Navigate with flags
     * navigateToActivity(
     *     SecondActivity::class.java,
     *     flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
     * )
     * ```
     */
    fun navigateToActivity(
        activityClass: Class<out Activity>,
        finishCurrent: Boolean = false,
        flags: Int? = null
    ) {
        context?.let { ctx ->
            val intent = Intent(ctx, activityClass)
            flags?.let { intent.addFlags(it) }
            ctx.startActivity(intent)
            if (finishCurrent && ctx is Activity) {
                ctx.finish()
            }
        }
    }

    companion object {
        /**
         * إنشاء هيكل التنقل
         * Create navigation structure
         * 
         * @param startDestination الشاشة الابتدائية / Start destination
         * @param navigation كائن التنقل / Navigation instance
         * @param builder منشئ التنقل / Navigation builder
         */
        @Composable
        fun CreateNavigation(
            startDestination: String,
            navigation: TPViewsNavigation,
            builder: NavGraphBuilder.() -> Unit
        ) {
            val navController = rememberNavController()
            val context = androidx.compose.ui.platform.LocalContext.current
            navigation.setNavController(navController, context)
            
            NavHost(
                navController = navController,
                startDestination = startDestination,
                builder = builder
            )
        }

        /**
         * إضافة شاشة إلى هيكل التنقل
         * Add a screen to navigation structure
         * 
         * @param route مسار الشاشة / Screen route
         * @param content محتوى الشاشة / Screen content
         */
        fun NavGraphBuilder.addScreen(
            route: String,
            content: @Composable () -> Unit
        ) {
            composable(route) { content() }
        }
    }
}