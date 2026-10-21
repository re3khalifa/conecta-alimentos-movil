package com.example.conectaalimentos.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.conectaalimentos.ui.theme.*


data class MerchantOffer(
    val id: String = "",
    val title: String,
    val price: String,
    val quantity: String,
    val expiry: String,
    val status: String,
    val category: String = "Panadería",
    val address: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOffersScreen(offers: List<MerchantOffer>, onBack:()->Unit, onNewOffer:()->Unit, onDelete:(MerchantOffer)->Unit, onEdit:(MerchantOffer)->Unit) {
    Scaffold(containerColor=Cream, topBar={ TopAppBar(title={Text("Mis ofertas")},navigationIcon={IconButton(onClick=onBack){Icon(Icons.Outlined.ArrowBack,"Regresar",tint=Orange)}},actions={Button(onClick=onNewOffer,colors=ButtonDefaults.buttonColors(containerColor=Orange)){Text("Nueva oferta")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Cream)) }) { p ->
        if(offers.isEmpty()) Box(Modifier.padding(p).fillMaxSize(),contentAlignment=Alignment.Center){Text("Aún no has publicado ofertas",color=MutedText)} else LazyColumn(Modifier.padding(p).fillMaxSize().padding(horizontal=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(vertical=12.dp)) {
            items(offers) { offer -> Card(colors=CardDefaults.cardColors(containerColor=FieldBg),border=BorderStroke(1.dp,FieldBorder),shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) { Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween,modifier=Modifier.fillMaxWidth()){ Text(offer.title,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleMedium,modifier=Modifier.weight(1f)); AssistChip(onClick={},label={Text(offer.status)}) }
                Text("$ ${offer.price}",color=Orange,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge)
                Text("Categoría: ${offer.category}",color=MutedText); Text("Cantidad disponible: ${offer.quantity}"); Text("Fecha de caducidad: ${offer.expiry}")
                Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()) { OutlinedButton(onClick={onEdit(offer)},modifier=Modifier.weight(1f)){Icon(Icons.Outlined.Edit,null);Spacer(Modifier.width(4.dp));Text("Editar")}; OutlinedButton(onClick={onDelete(offer)},modifier=Modifier.weight(1f),colors=ButtonDefaults.outlinedButtonColors(contentColor=Orange)){Icon(Icons.Outlined.DeleteOutline,null);Spacer(Modifier.width(4.dp));Text("Eliminar")} }
            } } }
        }
    }
}
