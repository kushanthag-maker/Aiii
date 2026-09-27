package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.persona.AgentPersona
import com.example.data.persona.Personas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaSelectorSheet(
    selectedPersona: AgentPersona,
    onSelect: (AgentPersona) -> Unit,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141416),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = "Select Agent Persona",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Customize Nova AI's coding style and behavior",
                color = Color(0xFF9CA3AF),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Personas.all.forEach { persona ->
                    val isSelected = persona.id == selectedPersona.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0xFF1E283D) else Color(0xFF1B1B1E))
                            .border(
                                width = 1.dp,
                                color = if (isSelected) Color(0xFF38BDF8) else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable {
                                onSelect(persona)
                                onDismiss()
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF38BDF8) else Color(0xFF26262B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = persona.icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.Black else Color(0xFF38BDF8),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = persona.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFF38BDF8).copy(alpha = 0.15f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = persona.badge,
                                        color = Color(0xFF38BDF8),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = persona.shortDesc,
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.5.sp
                            )
                        }

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
