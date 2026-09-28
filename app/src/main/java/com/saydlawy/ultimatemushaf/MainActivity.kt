package com.saydlawy.ultimatemushaf

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.saydlawy.ultimatemushaf.mushaf.QcfMushafCanvas

class MainActivity:ComponentActivity(){
 override fun onCreate(s:Bundle?){super.onCreate(s);setContent{UltimateMushafApp()}}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun UltimateMushafApp(){
 var page by rememberSaveable{mutableIntStateOf(1)};var dark by rememberSaveable{mutableStateOf(false)}
 MaterialTheme(colorScheme=if(dark)darkColorScheme()else lightColorScheme()){
  Column(Modifier.fillMaxSize()){
   TopAppBar(title={Text("المصحف",fontSize=18.sp)},actions={TextButton(onClick={dark=!dark}){Text(if(dark)"☀" else "☾")}})
   Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){
    AndroidView(Modifier.fillMaxSize(),factory={QcfMushafCanvas(it).apply{setPageChangedListener{page=it}}},update={it.setPage(page);it.setDark(dark)})
   }
   Row(Modifier.fillMaxWidth().padding(8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
    TextButton(enabled=page>1,onClick={page--}){Text("‹ السابق")};Text("صفحة $page / 604");TextButton(enabled=page<604,onClick={page++}){Text("التالي ›")}
   }
  }
 }
}
