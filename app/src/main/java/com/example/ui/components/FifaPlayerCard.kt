package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.model.CardTier
import com.example.model.PlayerCard
import com.example.ui.theme.*

enum class CardSize {
    LARGE,
    MEDIUM,
    MINI
}

@Composable
fun FifaPlayerCard(
    player: PlayerCard,
    modifier: Modifier = Modifier,
    size: CardSize = CardSize.MEDIUM,
    isHighlighted: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    // Dynamic Holographic Shimmer animation
    val infiniteTransition = rememberInfiniteTransition(label = "holo_sheen")
    val holoOffset by infiniteTransition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "holo_offset"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    // Visual Palette & Borders based on the 5 exact tiers:
    // • Common (45-64) — Bronze
    // • Unknown (65-74) — Silver / Shimmering Teal
    // • Rare (75-81) — Gold
    // • Epic (82-89) — Neon Purple / Amethyst
    // • Legendary (90-99) — Dynamic Holographic / Diamond
    val (bgBrush, borderColor, headerColor, badgeBg) = when (player.tier) {
        CardTier.COMMON -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF3E2818), Color(0xFF26180E), Color(0xFF130C07))),
            Color(0xFFCD7F32).copy(alpha = if (isHighlighted) glowAlpha else 0.8f),
            Color(0xFFE89E58),
            Color(0xFF5A3820)
        )
        CardTier.UNKNOWN -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF0F3B3E), Color(0xFF082426), Color(0xFF041213))),
            Color(0xFF14B8A6).copy(alpha = if (isHighlighted) glowAlpha else 0.85f),
            Color(0xFF2DD4BF),
            Color(0xFF115E59)
        )
        CardTier.RARE -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF3E3113), Color(0xFF231B0A), Color(0xFF0F0B03))),
            GoldPrimary.copy(alpha = if (isHighlighted) glowAlpha else 0.85f),
            GoldLight,
            Color(0xFF6B4E10)
        )
        CardTier.EPIC -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF3B0F3F), Color(0xFF200723), Color(0xFF0D020E))),
            Color(0xFFD946EF).copy(alpha = if (isHighlighted) glowAlpha else 0.9f),
            Color(0xFFF0ABFC),
            Color(0xFF701A75)
        )
        CardTier.LEGENDARY -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF0C2B42), Color(0xFF061826), Color(0xFF02090F))),
            Color(0xFF38BDF8).copy(alpha = if (isHighlighted) glowAlpha else 0.95f),
            Color(0xFFBAE6FD),
            Color(0xFF0369A1)
        )
    }

    val cardShape = RoundedCornerShape(16.dp)

    Card(
        modifier = modifier
            .testTag("player_card_${player.id}")
            .shadow(
                elevation = if (player.tier == CardTier.LEGENDARY || isHighlighted) 14.dp else 6.dp,
                shape = cardShape,
                spotColor = borderColor
            )
            .border(
                width = if (isHighlighted || player.tier == CardTier.LEGENDARY) 2.5.dp else 1.5.dp,
                color = borderColor,
                shape = cardShape
            ),
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Box(
            modifier = Modifier
                .background(bgBrush)
                .fillMaxWidth()
                .drawBehind {
                    // For Legendary cards, render an animated holographic light refraction beam
                    if (player.tier == CardTier.LEGENDARY) {
                        val brush = Brush.linearGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x3338BDF8),
                                Color(0x66FFFFFF),
                                Color(0x33E0F2FE),
                                Color.Transparent
                            ),
                            start = Offset(holoOffset, 0f),
                            end = Offset(holoOffset + 180f, this.size.height)
                        )
                        drawRect(brush = brush)
                    }
                }
        ) {
            when (size) {
                CardSize.LARGE -> LargeCardContent(player, headerColor, borderColor, badgeBg)
                CardSize.MEDIUM -> MediumCardContent(player, headerColor, borderColor, badgeBg)
                CardSize.MINI -> MiniCardContent(player, headerColor, badgeBg)
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
private fun LargeCardContent(player: PlayerCard, headerColor: Color, borderColor: Color, badgeBg: Color) {
    Column(
        modifier = Modifier
            .padding(14.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Rating, Position, Flag on Left; Photo in Center; Tier & Club on Right
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(60.dp)
            ) {
                Text(
                    text = "${player.overall}",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = headerColor
                )
                Text(
                    text = player.position.code,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = player.flagEmoji, fontSize = 24.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "#${player.jerseyNumber}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = headerColor
                )
            }

            // Big, High-Detail Dynamic Player Portrait with real photo or canvas athlete art
            PlayerPortraitVisual(
                player = player,
                accentColor = headerColor,
                badgeBg = badgeBg,
                sizeDp = 115.dp,
                isLarge = true
            )

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.width(70.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeBg.copy(alpha = 0.85f),
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Text(
                        text = player.tier.tagLabel,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = headerColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = player.league,
                    fontSize = 10.sp,
                    color = TextSecondaryDark,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = player.club,
                    fontSize = 11.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Name Banner
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.55f),
            border = BorderStroke(1.dp, Color(0x33FFFFFF))
        ) {
            Column(
                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = player.name.uppercase(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Age: ${player.age}", fontSize = 11.sp, color = TextSecondaryDark)
                    Text(text = "•", color = TextSecondaryDark, fontSize = 10.sp)
                    Text(text = player.nationality, fontSize = 11.sp, color = TextSecondaryDark)
                    Text(text = "•", color = TextSecondaryDark, fontSize = 10.sp)
                    Text(text = player.wageFormatted, fontSize = 11.sp, color = EmeraldPitch, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = headerColor.copy(alpha = 0.25f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

        // 6 FIFA Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(label = "PAC", value = player.pac, accent = headerColor)
            StatItem(label = "SHO", value = player.sho, accent = headerColor)
            StatItem(label = "PAS", value = player.pas, accent = headerColor)
            StatItem(label = "DRI", value = player.dri, accent = headerColor)
            StatItem(label = "DEF", value = player.def, accent = headerColor)
            StatItem(label = "PHY", value = player.phy, accent = headerColor)
        }
    }
}

@Composable
private fun MediumCardContent(player: PlayerCard, headerColor: Color, borderColor: Color, badgeBg: Color) {
    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Player portrait headshot
            PlayerPortraitVisual(
                player = player,
                accentColor = headerColor,
                badgeBg = badgeBg,
                sizeDp = 46.dp,
                isLarge = false
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "${player.overall}", fontSize = 17.sp, fontWeight = FontWeight.Black, color = headerColor)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = player.position.code, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = player.flagEmoji, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = player.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${player.club} • ${player.tier.displayName}",
                    fontSize = 10.sp,
                    color = TextSecondaryDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = player.marketValueFormatted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = headerColor
                )
                Text(
                    text = player.wageFormatted,
                    fontSize = 9.sp,
                    color = EmeraldPitch
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CompactStat(label = "PAC", value = player.pac)
            CompactStat(label = "SHO", value = player.sho)
            CompactStat(label = "PAS", value = player.pas)
            CompactStat(label = "DRI", value = player.dri)
            CompactStat(label = "DEF", value = player.def)
            CompactStat(label = "PHY", value = player.phy)
        }
    }
}

@Composable
private fun MiniCardContent(player: PlayerCard, headerColor: Color, badgeBg: Color) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerPortraitVisual(
            player = player,
            accentColor = headerColor,
            badgeBg = badgeBg,
            sizeDp = 28.dp,
            isLarge = false
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = "${player.overall}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = headerColor)
            Text(text = player.position.code, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Text(
            text = player.name.split(" ").lastOrNull() ?: player.name,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(text = player.flagEmoji, fontSize = 10.sp)
    }
}

/**
 * Player Portrait Visual:
 * 1) Loads real football headshot photo from player.imageUrl via Coil when online/available!
 * 2) Falls back gracefully to an illustrated custom footballer portrait (skin, hairstyle, kit, crest)
 *    rendered crisply with Canvas vector drawing!
 */
@Composable
fun PlayerPortraitVisual(
    player: PlayerCard,
    accentColor: Color,
    badgeBg: Color,
    sizeDp: androidx.compose.ui.unit.Dp,
    isLarge: Boolean = false
) {
    val portraitShape = if (isLarge) RoundedCornerShape(14.dp) else CircleShape

    Box(
        modifier = Modifier
            .size(sizeDp)
            .shadow(if (isLarge) 8.dp else 4.dp, portraitShape, spotColor = accentColor)
            .clip(portraitShape)
            .background(
                Brush.radialGradient(
                    listOf(
                        badgeBg,
                        Color(0xFF0B1218),
                        Color.Black
                    )
                )
            )
            .border(if (isLarge) 2.dp else 1.5.dp, accentColor, portraitShape),
        contentAlignment = Alignment.Center
    ) {
        if (!player.imageUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = player.imageUrl,
                contentDescription = player.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    AthleteCanvasPortrait(
                        player = player,
                        accentColor = accentColor,
                        isLarge = isLarge
                    )
                },
                error = {
                    AthleteCanvasPortrait(
                        player = player,
                        accentColor = accentColor,
                        isLarge = isLarge
                    )
                }
            )
        } else {
            AthleteCanvasPortrait(
                player = player,
                accentColor = accentColor,
                isLarge = isLarge
            )
        }
    }
}

/**
 * Detailed Athlete Canvas Portrait:
 * Draws dynamic stadium lighting, team kit collar, athletic shoulders, face oval with skin tone,
 * hairstyle (fade, curly, locks, buzz, slick, afro), eyebrows, eyes, and player jersey number.
 */
@Composable
private fun AthleteCanvasPortrait(
    player: PlayerCard,
    accentColor: Color,
    isLarge: Boolean
) {
    val skinColor = Color(player.skinToneHex)
    val hairColor = when (player.hairstyle) {
        "slick" -> Color(0xFF2C1E14)
        "buzz" -> Color(0xFF1E1611)
        "curly" -> Color(0xFF231812)
        "locks" -> Color(0xFF16110D)
        "afro" -> Color(0xFF1B140F)
        else -> Color(0xFF2B2019) // fade
    }
    val kitColor = when (player.position.category) {
        com.example.model.PositionCategory.GOALKEEPER -> Color(0xFF059669)
        com.example.model.PositionCategory.DEFENDER -> Color(0xFF1D4ED8)
        com.example.model.PositionCategory.MIDFIELDER -> Color(0xFF7C3AED)
        com.example.model.PositionCategory.ATTACKER -> Color(0xFFDC2626)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. Stadium Light Beams Background
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.4f), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.35f),
                radius = w * 0.6f
            )
        )

        // 2. Athlete Kit / Jersey & Shoulders
        val jerseyPath = Path().apply {
            moveTo(w * 0.15f, h)
            lineTo(w * 0.22f, h * 0.72f)
            quadraticTo(w * 0.35f, h * 0.65f, w * 0.42f, h * 0.66f)
            lineTo(w * 0.5f, h * 0.75f) // V-neck collar
            lineTo(w * 0.58f, h * 0.66f)
            quadraticTo(w * 0.65f, h * 0.65f, w * 0.78f, h * 0.72f)
            lineTo(w * 0.85f, h)
            close()
        }
        drawPath(path = jerseyPath, color = kitColor)

        // Kit Accent Trim (Sash or Collar)
        val collarPath = Path().apply {
            moveTo(w * 0.42f, h * 0.66f)
            lineTo(w * 0.5f, h * 0.75f)
            lineTo(w * 0.58f, h * 0.66f)
        }
        drawPath(path = collarPath, color = Color.White, style = Stroke(width = w * 0.04f, cap = StrokeCap.Round))

        // 3. Athletic Neck
        drawRoundRect(
            color = skinColor.copy(alpha = 0.95f),
            topLeft = Offset(w * 0.42f, h * 0.52f),
            size = Size(w * 0.16f, h * 0.20f),
            cornerRadius = CornerRadius(w * 0.04f, w * 0.04f)
        )

        // 4. Head & Face Contour
        val faceWidth = w * 0.38f
        val faceHeight = h * 0.42f
        val faceLeft = (w - faceWidth) / 2f
        val faceTop = h * 0.22f

        drawOval(
            color = skinColor,
            topLeft = Offset(faceLeft, faceTop),
            size = Size(faceWidth, faceHeight)
        )

        // 5. Hairstyle rendering
        when (player.hairstyle) {
            "afro" -> {
                drawCircle(
                    color = hairColor,
                    center = Offset(w * 0.5f, faceTop + faceHeight * 0.25f),
                    radius = faceWidth * 0.62f
                )
                // Redraw face front so afro surrounds head
                drawOval(
                    color = skinColor,
                    topLeft = Offset(faceLeft, faceTop),
                    size = Size(faceWidth, faceHeight)
                )
            }
            "locks" -> {
                drawRoundRect(
                    color = hairColor,
                    topLeft = Offset(faceLeft - w * 0.05f, faceTop - h * 0.06f),
                    size = Size(faceWidth + w * 0.1f, faceHeight * 0.65f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f)
                )
            }
            "curly" -> {
                drawRoundRect(
                    color = hairColor,
                    topLeft = Offset(faceLeft - w * 0.02f, faceTop - h * 0.05f),
                    size = Size(faceWidth + w * 0.04f, faceHeight * 0.52f),
                    cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
                )
            }
            "slick" -> {
                drawRoundRect(
                    color = hairColor,
                    topLeft = Offset(faceLeft, faceTop - h * 0.04f),
                    size = Size(faceWidth, faceHeight * 0.40f),
                    cornerRadius = CornerRadius(w * 0.06f, w * 0.06f)
                )
            }
            else -> { // Fade / Buzz
                drawRoundRect(
                    color = hairColor,
                    topLeft = Offset(faceLeft + w * 0.02f, faceTop - h * 0.03f),
                    size = Size(faceWidth - w * 0.04f, faceHeight * 0.38f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f)
                )
            }
        }

        // 6. Facial Features (Eyes & Brows) if size is sufficient
        if (w >= 40f) {
            val eyeY = faceTop + faceHeight * 0.50f
            val eyeRadius = w * 0.022f

            // Left & Right Eyes
            drawCircle(color = Color(0xFF1F2937), center = Offset(w * 0.43f, eyeY), radius = eyeRadius)
            drawCircle(color = Color(0xFF1F2937), center = Offset(w * 0.57f, eyeY), radius = eyeRadius)

            // Eyebrows
            drawLine(
                color = hairColor,
                start = Offset(w * 0.39f, eyeY - h * 0.04f),
                end = Offset(w * 0.46f, eyeY - h * 0.035f),
                strokeWidth = w * 0.025f,
                cap = StrokeCap.Round
            )
            drawLine(
                color = hairColor,
                start = Offset(w * 0.54f, eyeY - h * 0.035f),
                end = Offset(w * 0.61f, eyeY - h * 0.04f),
                strokeWidth = w * 0.025f,
                cap = StrokeCap.Round
            )
        }

        // 7. Team Jersey Number & Badge
        if (isLarge) {
            // Little crest badge on left chest
            drawCircle(
                color = accentColor,
                center = Offset(w * 0.28f, h * 0.82f),
                radius = w * 0.045f
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: Int, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "$value",
            fontSize = 15.sp,
            fontWeight = FontWeight.Black,
            color = if (value >= 85) GoldPrimary else if (value >= 75) EmeraldPitch else Color.White
        )
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondaryDark
        )
    }
}

@Composable
private fun CompactStat(label: String, value: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "$value",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (value >= 85) GoldPrimary else Color.White
        )
        Text(text = label, fontSize = 9.sp, color = TextSecondaryDark)
    }
}
