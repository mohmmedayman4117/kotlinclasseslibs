package com.example.classescreator.examples

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kotlinclasses.tpViewExt

@Composable
fun TPDiamondShapeExample() {
    val tpView = Modifier.tpViewExt()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Error Diamond
        Box(
            modifier = Modifier
                .size(150.dp)
                .then(tpView.addDiamondMaskWithCornersShaped(cornerRadius = 25.0))
                .background(Color(0xFF9C27B0)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "حوف ثبته",
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        // Success Diamond
        Box(
            modifier = Modifier
                .size(150.dp)
                .then(tpView.addDiamondMaskWithCornersShaped(  cornerRadiusTop = 16.0,
                    cornerRadiusRight = 8.0,
                    cornerRadiusBottom = 16.0,
                    cornerRadiusLeft = 8.0))
                .background(Color(0xFFFFEB3B)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "مختلف ",
                color = Color.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}
