package com.saydlawy.ultimatemushaf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UltimateMushafApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UltimateMushafApp() {
    var page by rememberSaveable { mutableIntStateOf(1) }
    var dark by rememberSaveable { mutableStateOf(false) }

    val paper = if (dark) Color(0xFF1B1915) else Color(0xFFF4EEDC)
    val ink = if (dark) Color(0xFFF4EEDC) else Color(0xFF211E19)

    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Column(Modifier.fillMaxSize().background(if (dark) Color(0xFF10100E) else Color(0xFFE7E0CE))) {
            TopAppBar(
                title = { Text("Ultimate Mushaf", fontSize = 18.sp) },
                actions = {
                    TextButton(onClick = { dark = !dark }) { Text(if (dark) "☀" else "☾") }
                }
            )

            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures(
                            onDragEnd = {},
                            onHorizontalDrag = { _, drag ->
                                if (abs(drag) > 12f) {
                                    page = (page + if (drag < 0) 1 else -1).coerceIn(1, 604)
                                }
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                MushafPage(page = page, paper = paper, ink = ink)
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = { page = (page - 1).coerceAtLeast(1) }) { Text("‹") }
                Text("صفحة $page / 604", fontSize = 16.sp)
                TextButton(onClick = { page = (page + 1).coerceAtMost(604) }) { Text("›") }
            }
        }
    }
}

@Composable
private fun MushafPage(page: Int, paper: Color, ink: Color) {
    Canvas(
        Modifier
            .fillMaxWidth(0.92f)
            .aspectRatio(0.70f)
            .background(paper)
    ) {
        val margin = size.width * 0.08f
        drawRect(Color.Black.copy(alpha = 0.05f), topLeft = Offset.Zero, size = size)
        drawRect(ink.copy(alpha = 0.28f), topLeft = Offset(margin, margin), size = androidx.compose.ui.geometry.Size(size.width - margin * 2, size.height - margin * 2), style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
    }
}
