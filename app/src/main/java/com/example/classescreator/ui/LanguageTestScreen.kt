package com.example.classescreator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kotlinclasses.TPAppLanguage
import com.kotlinclasses.TPAppLanguageImpl
import com.kotlinclasses.TPViewExt


@Composable
fun LanguageTestScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewExt = remember { TPViewExt(scope) }
    val delegate = remember { TPAppLanguageImpl(context) }
    val appLanguage = remember { TPAppLanguage(delegate) }
    val currentLanguageCode by rememberUpdatedState(appLanguage.getCurrentLanguageCode())
    var key by remember { mutableStateOf(0) }

    LaunchedEffect(key) {
        // No-op
    }

    viewExt.keepConstraintsAlignedWithLanguageDirection(appLanguage) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // زر تبديل اللغة
            Button(
                onClick = {
                    val nextLanguage = if (appLanguage.isRTL()) "English" else "العربية"
                    appLanguage.updateLanguageCode(nextLanguage)
                    key += 1  // Trigger recomposition
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (appLanguage.isRTL()) "Switch to English" else "تغيير إلى العربية"
                )
            }

            Divider()

            // عرض الاتجاه الحالي
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (appLanguage.isRTL()) "معلومات اللغة الحالية" else "Current Language Info",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Text(
                        text = if (appLanguage.isRTL())
                            "اتجاه اللغة: من اليمين إلى اليسار"
                        else
                            "Language Direction: Left to Right"
                    )

                    Text(
                        text = if (appLanguage.isRTL())
                            "رمز اللغة: ${appLanguage.getCurrentLanguageCode()}"
                        else
                            "Language Code: ${appLanguage.getCurrentLanguageCode()}"
                    )
                }
            }

            // أزرار للاختبار
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (appLanguage.isRTL()) "أزرار الاختبار" else "Test Buttons",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { /* عمل شيء */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = if (appLanguage.isRTL()) "زر 1" else "Button 1")
                        }

                        Button(
                            onClick = { /* عمل شيء آخر */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = if (appLanguage.isRTL()) "زر 2" else "Button 2")
                        }
                    }
                }
            }

            // قائمة اللغات المتاحة
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (appLanguage.isRTL()) "اللغات المتاحة" else "Available Languages",
                        style = MaterialTheme.typography.titleLarge
                    )

                    appLanguage.getAllAvailableLanguages().forEach { language ->
                        ListItem(
                            headlineContent = { Text(language) },
                            leadingContent = {
                                RadioButton(
                                    selected = when (language) {
                                        "العربية" -> appLanguage.isRTL()
                                        "English" -> !appLanguage.isRTL()
                                        else -> false
                                    },
                                    onClick = {
                                        appLanguage.updateLanguageCode(language)
                                        key += 1  // Trigger recomposition
                                    }
                                )
                            }
                        )
                    }
                }
            }


            Divider()
        }
    }
}
