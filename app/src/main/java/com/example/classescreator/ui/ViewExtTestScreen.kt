package com.example.classescreator.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.kotlinclasses.TPViewExt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewExtTestScreen() {
    val scope = rememberCoroutineScope()
    val viewExt = remember { TPViewExt(scope) }
    var effectsEnabled by remember { mutableStateOf(true) }

    Box(modifier = Modifier.fillMaxSize()) {
        // مربع الاختبار الثابت في الأعلى
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
                .zIndex(1f)
                .size(100.dp)
                .background(Color.Blue)
                .let { if (effectsEnabled) viewExt.applyEffects(it) else it },
            contentAlignment = Alignment.Center
        ) {
            Text("مربع الاختبار", color = Color.White)
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("اختبار التأثيرات") }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .padding(top = 100.dp) // إضافة مسافة للمربع
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // أزرار التأثيرات الأساسية
                Button(
                    onClick = { viewExt.hide() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("إخفاء")
                }

                Button(
                    onClick = { viewExt.unHide() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("إظهار")
                }

                Button(
                    onClick = { viewExt.dim() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("تعتيم")
                }

                Button(
                    onClick = { viewExt.unDim() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("إلغاء التعتيم")
                }

                // أزرار الحركات
                Button(
                    onClick = { viewExt.shakeIt() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("هز")
                }

                Button(
                    onClick = { viewExt.rotateClockWise360Degrees() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("دوران مع عقارب الساعة")
                }

                Button(
                    onClick = { viewExt.rotateAntiClockWise360Degrees() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("دوران عكس عقارب الساعة")
                }

                Button(
                    onClick = { viewExt.flash() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("وميض")
                }

                Button(
                    onClick = { viewExt.scaleInOutQuickly() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("تكبير وتصغير سريع")
                }

                Button(
                    onClick = { viewExt.scaleOutInOneTime() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("تكبير وتصغير مرة واحدة")
                }

                // أزرار الظهور والاختفاء
                Button(
                    onClick = { viewExt.showFromBottomToTop() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("ظهور من الأسفل")
                }

                Button(
                    onClick = { viewExt.showFromTopToBottom() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("ظهور من الأعلى")
                }

                Button(
                    onClick = { viewExt.hideSlowlyFromTopToBottom() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("اختفاء من الأعلى")
                }

                Button(
                    onClick = { viewExt.hideSlowlyFromBottomToTop() },
                    modifier = if (effectsEnabled) {
                        Modifier
                            .fillMaxWidth()
                    } else {
                        Modifier.fillMaxWidth()
                    }
                ) {
                    Text("اختفاء من الأسفل")
                }

                // أزرار التحكم
                Button(
                    onClick = { effectsEnabled = !effectsEnabled },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (effectsEnabled) "إيقاف التأثيرات" else "تشغيل التأثيرات")
                }

                Button(
                    onClick = { viewExt.stopAllAnimations() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إيقاف جميع الحركات")
                }
            }
        }
    }
}
