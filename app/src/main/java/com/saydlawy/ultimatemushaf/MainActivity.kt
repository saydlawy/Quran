package com.saydlawy.ultimatemushaf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.saydlawy.ultimatemushaf.core.Bookmark
import com.saydlawy.ultimatemushaf.core.UltimateDatabase
import com.saydlawy.ultimatemushaf.mushaf.QcfMushafCanvas

enum class MushafMode { READING, CONTINUOUS, TEXT, STUDY, HIFZ }

class MainActivity : ComponentActivity() {
    private lateinit var db: UltimateDatabase

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        db = UltimateDatabase(this)
        setContent { UltimateMushafApp(db) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UltimateMushafApp(db: UltimateDatabase) {
    val saved = remember { db.loadPosition() }
    var page by rememberSaveable { mutableIntStateOf(saved.page.coerceIn(1, 604)) }
    var dark by rememberSaveable { mutableStateOf(false) }
    var drawer by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableStateOf(MushafMode.READING) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }

    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text("Ultimate Mushaf", fontSize = 17.sp) },
                    navigationIcon = {
                        IconButton(onClick = { drawer = !drawer }) { Text("☰", fontSize = 23.sp) }
                    },
                    actions = {
                        TextButton(onClick = { searchOpen = true }) { Text("بحث") }
                        TextButton(onClick = { dark = !dark }) { Text(if (dark) "☀" else "☾") }
                    }
                )

                Box(
                    Modifier.weight(1f).fillMaxWidth().background(if (dark) Color(12, 12, 11) else Color(225, 219, 204)),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = {
                            QcfMushafCanvas(it).apply {
                                setPageChangedListener { newPage ->
                                    page = newPage
                                    db.savePosition(com.saydlawy.ultimatemushaf.core.ReadingPosition(page = newPage))
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize(),
                        update = {
                            it.setPage(page)
                            it.setDark(dark)
                        }
                    )
                }

                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(enabled = page > 1, onClick = { page-- }) { Text("‹ السابق") }
                    Text("صفحة $page / 604")
                    TextButton(enabled = page < 604, onClick = { page++ }) { Text("التالي ›") }
                }
            }

            if (drawer) {
                Surface(
                    Modifier.fillMaxHeight().width(300.dp).align(Alignment.TopStart),
                    tonalElevation = 8.dp,
                    shadowElevation = 12.dp
                ) {
                    Column(Modifier.fillMaxSize().padding(top = 54.dp, start = 18.dp, end = 18.dp)) {
                        Text("الفهرس والتنقل", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { page = 1; drawer = false }, Modifier.fillMaxWidth()) { Text("أول المصحف") }
                        Button(onClick = { page = 604; drawer = false }, Modifier.fillMaxWidth()) { Text("آخر المصحف") }
                        OutlinedButton(onClick = { page = db.loadPosition().page; drawer = false }, Modifier.fillMaxWidth()) { Text("متابعة آخر موضع") }
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        Text("وضع العرض", style = MaterialTheme.typography.titleMedium)
                        MushafMode.entries.forEach { m ->
                            TextButton(onClick = { mode = m; drawer = false }, Modifier.fillMaxWidth()) {
                                Text(if (mode == m) "✓ " else "   " + m.name)
                            }
                        }
                        HorizontalDivider(Modifier.padding(vertical = 10.dp))
                        Button(onClick = {
                            db.addBookmark(Bookmark(page = page, title = "علامة القراءة", permanent = true))
                            drawer = false
                        }, Modifier.fillMaxWidth()) { Text("إضافة علامة مرجعية") }
                    }
                }
            }

            if (searchOpen) {
                AlertDialog(
                    onDismissRequest = { searchOpen = false },
                    title = { Text("البحث في القرآن") },
                    text = {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = { searchText = it },
                            singleLine = true,
                            label = { Text("النص أو الآية") }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { searchOpen = false }) { Text("بحث") }
                    },
                    dismissButton = {
                        TextButton(onClick = { searchText = ""; searchOpen = false }) { Text("إلغاء") }
                    }
                )
            }
        }
    }
}
