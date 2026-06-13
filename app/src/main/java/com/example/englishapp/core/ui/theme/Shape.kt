package com.example.englishapp.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),   // Dùng cho TextField, Chip
    medium = RoundedCornerShape(12.dp),  // Dùng cho Button, Card nhỏ
    large = RoundedCornerShape(20.dp)    // Dùng cho Dialog, BottomSheet, Story Card
)