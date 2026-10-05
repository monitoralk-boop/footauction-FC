package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Formation
import com.example.model.PlayerCard
import com.example.model.PositionCategory
import com.example.ui.theme.EmeraldPitch
import com.example.ui.theme.GoldPrimary

@Composable
fun PitchFormationView(
    squad: List<PlayerCard>,
    formation: Formation = Formation.F_4_3_3,
    modifier: Modifier = Modifier,
    onPlayerSelected: ((PlayerCard) -> Unit)? = null,
    onEmptySlotClicked: ((String) -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(290.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F3822),
                            Color(0xFF092917),
                            Color(0xFF061B0F)
                        )
                    )
                )
                .drawBehind {
                    val w = size.width
                    val h = size.height
                    val lineColor = Color(0x33FFFFFF)
                    val stroke = Stroke(width = 1.5.dp.toPx())

                    // Field outline
                    drawRect(color = lineColor, style = stroke, size = Size(w - 20f, h - 20f), topLeft = Offset(10f, 10f))
                    // Halfway line
                    drawLine(color = lineColor, start = Offset(10f, h / 2), end = Offset(w - 10f, h / 2), strokeWidth = 1.5.dp.toPx())
                    // Center circle
                    drawCircle(color = lineColor, radius = 45.dp.toPx(), center = Offset(w / 2, h / 2), style = stroke)
                    // Penalty box top
                    drawRect(color = lineColor, style = stroke, size = Size(w * 0.5f, 50.dp.toPx()), topLeft = Offset(w * 0.25f, 10f))
                    // Penalty box bottom
                    drawRect(color = lineColor, style = stroke, size = Size(w * 0.5f, 50.dp.toPx()), topLeft = Offset(w * 0.25f, h - 10f - 50.dp.toPx()))
                }
                .padding(12.dp)
        ) {
            val attackers = squad.filter { it.position.category == PositionCategory.ATTACKER }
            val midfielders = squad.filter { it.position.category == PositionCategory.MIDFIELDER }
            val defenders = squad.filter { it.position.category == PositionCategory.DEFENDER }
            val goalkeepers = squad.filter { it.position.category == PositionCategory.GOALKEEPER }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ATTACK ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reqAtt = formation.attCount
                    for (i in 0 until reqAtt) {
                        val player = attackers.getOrNull(i)
                        PitchPlayerNode(
                            player = player,
                            defaultRole = if (reqAtt == 3) (if (i == 0) "LW" else if (i == 1) "ST" else "RW") else "ST",
                            onClick = onPlayerSelected,
                            onEmptySlot = { onEmptySlotClicked?.invoke(if (reqAtt == 3) (if (i == 0) "LW" else if (i == 1) "ST" else "RW") else "ST") }
                        )
                    }
                }

                // MIDFIELD ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reqMid = formation.midCount
                    for (i in 0 until reqMid) {
                        val player = midfielders.getOrNull(i)
                        PitchPlayerNode(
                            player = player,
                            defaultRole = "MID",
                            onClick = onPlayerSelected,
                            onEmptySlot = { onEmptySlotClicked?.invoke("MID") }
                        )
                    }
                }

                // DEFENSE ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reqDef = formation.defCount
                    for (i in 0 until reqDef) {
                        val player = defenders.getOrNull(i)
                        PitchPlayerNode(
                            player = player,
                            defaultRole = if (i == 0) "LB" else if (i == reqDef - 1) "RB" else "CB",
                            onClick = onPlayerSelected,
                            onEmptySlot = { onEmptySlotClicked?.invoke(if (i == 0) "LB" else if (i == reqDef - 1) "RB" else "CB") }
                        )
                    }
                }

                // GOALKEEPER ROW
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val player = goalkeepers.firstOrNull()
                    PitchPlayerNode(
                        player = player,
                        defaultRole = "GK",
                        onClick = onPlayerSelected,
                        onEmptySlot = { onEmptySlotClicked?.invoke("GK") }
                    )
                }
            }
        }
    }
}

@Composable
private fun PitchPlayerNode(
    player: PlayerCard?,
    defaultRole: String,
    onClick: ((PlayerCard) -> Unit)? = null,
    onEmptySlot: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                if (player != null) {
                    onClick?.invoke(player)
                } else {
                    onEmptySlot?.invoke()
                }
            }
    ) {
        if (player != null) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.dp, GoldPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${player.overall}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldPrimary
                )
            }
            Text(
                text = player.name.split(" ").lastOrNull() ?: player.name,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = "${player.flagEmoji} ${player.position.code}",
                fontSize = 8.sp,
                color = EmeraldPitch,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
                    .border(1.dp, Color(0x66FFFFFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 14.sp,
                    color = Color(0x99FFFFFF),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = defaultRole,
                fontSize = 9.sp,
                color = Color(0x88FFFFFF),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
