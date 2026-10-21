package com.example.conectaalimentos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.conectaalimentos.ui.theme.*

data class Reservation(val id:String = "",val title:String,val store:String,val price:String,val code:String,val date:String,val pickup:String,val status:String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsScreen(reservations:List<Reservation>,onBack:()->Unit,onDetails:(Reservation)->Unit,onCancel:(Reservation)->Unit) {
    Scaffold(containerColor=Cream,topBar={TopAppBar(title={Text("Mis reservas")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Outlined.ArrowBack,"Regresar",tint=Orange)}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Cream))}) { p ->
        LazyColumn(Modifier.padding(p).fillMaxSize().padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)) {
            items(reservations) { r -> Card(colors=CardDefaults.cardColors(containerColor=FieldBg),border=BorderStroke(1.dp,FieldBorder),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)) {
                Row(horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){Text(r.title,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f));AssistChip(onClick={},label={Text(r.status)})}
                Text(r.store,color=MutedText); Text("$${r.price}",color=Orange,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)
                Text("Reserva # ${r.code}"); Text("Fecha: ${r.date}"); Text("Recogida: ${r.pickup}")
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) { OutlinedButton(onClick={onDetails(r)},modifier=Modifier.weight(1f)){Text("Ver detalles")}; if(r.status!="Completada") OutlinedButton(onClick={onCancel(r)},modifier=Modifier.weight(1f),colors=ButtonDefaults.outlinedButtonColors(contentColor=Orange)){Text("Cancelar reserva")} }
            } } }
        }
    }
}
