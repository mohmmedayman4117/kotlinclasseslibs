package com.example.classescreator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.kotlinclasses.TPGoogleMap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTestScreen() {
    var selectedLocation by remember { mutableStateOf<LatLng?>(null) }
    var countryName by remember { mutableStateOf<String?>(null) }
    var cityName by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var googleMap by remember { mutableStateOf<TPGoogleMap?>(null) }
    var hasCircle by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var showSuggestions by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }
    var placePredictions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var circleResults by remember { mutableStateOf<Map<String, TPGoogleMap.CircleSearchResult>>(emptyMap()) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val cairo = LatLng(30.0444, 31.2357)

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions.entries.all { it.value }
        if (locationGranted) {
            googleMap?.moveToCurrentLocation()
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("السماح بالوصول للموقع") },
            text = { Text("نحتاج إذنك للوصول إلى موقعك الحالي لعرضه على الخريطة.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                ) {
                    Text("السماح")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // شريط البحث والأزرار
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // حقل البحث
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { query ->
                            searchQuery = query
                            if (query.length >= 2) {
                                coroutineScope.launch {
                                    isSearching = true
                                    googleMap?.getPlacePredictions(query) { predictions, error ->
                                        placePredictions = predictions
                                        showSuggestions = predictions.isNotEmpty()
                                        isSearching = false
                                        if (error != null) {
                                            errorMessage = error.message
                                        }
                                    }
                                }
                            } else {
                                showSuggestions = false
                                placePredictions = emptyList()
                            }
                        },
                        label = { Text("ابحث عن مكان") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { 
                            Icon(Icons.Default.Search, contentDescription = "بحث")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    showSuggestions = false
                                    placePredictions = emptyList()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        }
                    )

                    // تلميحات البحث
                    AnimatedVisibility(
                        visible = showSuggestions,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            items(placePredictions) { prediction ->
                                SuggestionItem(prediction) {
                                    searchQuery = prediction
                                    showSuggestions = false
                                    coroutineScope.launch {
                                        isSearching = true
                                        googleMap?.getCountryCoordinate(prediction) { location, error ->
                                            if (location != null) {
                                                selectedLocation = location
                                                googleMap?.navigateToLocation(location)
                                            } else {
                                                errorMessage = error?.message
                                            }
                                            isSearching = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // أزرار التحكم
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // الصف الأول
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MapControlButton(
                        icon = Icons.Default.Info,
                        text = "تكبير",
                        onClick = {
                            selectedLocation?.let { location ->
                                googleMap?.zoomIn(18f, location)
                            }
                        }
                    )

                    MapControlButton(
                        icon = Icons.Default.Info,
                        text = "تصغير",
                        onClick = {
                            selectedLocation?.let { location ->
                                googleMap?.zoomOut(10f, location)
                            }
                        }
                    )

                    MapControlButton(
                        icon = if (hasCircle) Icons.Default.Info else Icons.Default.AddCircle,
                        text = if (hasCircle) "إزالة" else "دائرة",
                        onClick = {
                            selectedLocation?.let { location ->
                                if (hasCircle) {
                                    googleMap?.clearCirclesByIds(listOf("test_circle"))
                                    hasCircle = false
                                } else {
                                    googleMap?.createCircle(
                                        "test_circle",
                                        location
                                    )
                                    hasCircle = true
                                    googleMap?.let { gMap ->
                                        circleResults = gMap.GetMarkersInsideCircles()
                                    }
                                }
                            }
                        }
                    )
                }

                // الصف الثاني
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MapControlButton(
                        icon = Icons.Default.Add,
                        text = "علامة",
                        onClick = {
                            selectedLocation?.let { location ->
                                googleMap?.let { gMap ->
                                    val markerId = "marker_${System.currentTimeMillis()}"
                                    gMap.createMarkers(
                                        coordinates = listOf(location),
                                        IDs = listOf(markerId),
                                        iconViews = listOf(null)
                                    )
                                    gMap.displayMarkersOnMap()
                                    if (hasCircle) {
                                        circleResults = gMap.GetMarkersInsideCircles()
                                    }
                                }
                            }
                        }
                    )
                }
            }

            // خريطة Google والزر العائم
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val mapView = rememberMapViewWithLifecycle()
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { mapView },
                    update = { mapView ->
                        if (googleMap == null) {
                            googleMap = TPGoogleMap(cairo, mapView, GoogleMap.OnCameraIdleListener {})
                            selectedLocation = cairo
                            
                            mapView.getMapAsync { map ->
                                map.setOnMapClickListener { latLng ->
                                    selectedLocation = latLng
                                    googleMap?.let { gMap ->
                                        gMap.getCountryName(latLng) { country, error ->
                                            countryName = country
                                            errorMessage = error?.message
                                        }
                                        gMap.getCityName(latLng) { city, error ->
                                            cityName = city
                                            if (error != null) {
                                                errorMessage = error.message
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                )

                // زر الموقع العائم
                FloatingActionButton(
                    onClick = { 
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                googleMap?.moveToCurrentLocation()
                            }
                            else -> {
                                showPermissionDialog = true
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(56.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "موقعي",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // معلومات الموقع والدوائر
            AnimatedVisibility(
                visible = selectedLocation != null,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    LocationInfoCard(
                        selectedLocation = selectedLocation,
                        countryName = countryName,
                        cityName = cityName,
                        errorMessage = errorMessage
                    )

                    // عرض نتائج البحث في الدوائر
                    if (hasCircle && circleResults.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "العلامات داخل الدائرة",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                circleResults.forEach { (circleId, result) ->
                                    Text("عدد العلامات: ${result.count}")
                                    if (result.titles.isNotEmpty()) {
                                        Text("العناوين: ${result.titles.joinToString(", ")}")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isSearching) {

        }
    }
}

@Composable
fun MapControlButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.padding(4.dp)
    ) {
        Icon(icon, contentDescription = text)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionItem(suggestion: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = suggestion,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun LocationInfoCard(
    selectedLocation: LatLng?,
    countryName: String?,
    cityName: String?,
    errorMessage: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "معلومات الموقع",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            selectedLocation?.let {
                Text("خط العرض: ${String.format("%.6f", it.latitude)}")
                Text("خط الطول: ${String.format("%.6f", it.longitude)}")
            }
            
            countryName?.let {
                Text("الدولة: $it")
            }
            
            cityName?.let {
                Text("المدينة: $it")
            }
            
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun rememberMapViewWithLifecycle(): MapView {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            id = androidx.compose.ui.R.id.compose_view_saveable_id_tag
        }
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, mapView) {
        val lifecycleObserver = getMapLifecycleObserver(mapView)
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            lifecycle.removeObserver(lifecycleObserver)
        }
    }

    return mapView
}

private fun getMapLifecycleObserver(mapView: MapView): LifecycleEventObserver =
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_CREATE -> mapView.onCreate(Bundle())
            Lifecycle.Event.ON_START -> mapView.onStart()
            Lifecycle.Event.ON_RESUME -> mapView.onResume()
            Lifecycle.Event.ON_PAUSE -> mapView.onPause()
            Lifecycle.Event.ON_STOP -> mapView.onStop()
            Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
            else -> throw IllegalStateException()
        }
    }
