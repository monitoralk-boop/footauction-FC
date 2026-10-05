package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PlayerDatabase
import com.example.model.CardTier
import com.example.model.PlayerCard
import com.example.model.Position
import com.example.ui.components.CardSize
import com.example.ui.components.FifaPlayerCard
import com.example.ui.theme.*
import com.example.viewmodel.AuctionGameViewModel
import java.util.UUID

@Composable
fun CardCreatorScreen(
    viewModel: AuctionGameViewModel,
    modifier: Modifier = Modifier
) {
    val countriesWithFlags = listOf(
        "Brazil" to "🇧🇷", "Argentina" to "🇦🇷", "France" to "🇫🇷", "England" to "🏴󠁧󠁢󠁥󠁮󠁧󠁿",
        "Spain" to "🇪🇸", "Germany" to "🇩🇪", "Portugal" to "🇵🇹", "Netherlands" to "🇳🇱",
        "Italy" to "🇮🇹", "Morocco" to "🇲🇦", "Norway" to "🇳🇴", "South Korea" to "🇰🇷",
        "Japan" to "🇯🇵", "Nigeria" to "🇳🇬", "Egypt" to "🇪🇬", "Canada" to "🇨🇦", "USA" to "🇺🇸"
    )

    var name by remember { mutableStateOf("Custom Wonderkid") }
    var selectedPosition by remember { mutableStateOf(Position.ST) }
    var selectedTier by remember { mutableStateOf(CardTier.RARE) }
    var selectedNation by remember { mutableStateOf("Brazil" to "🇧🇷") }
    var club by remember { mutableStateOf("Real Madrid") }
    var league by remember { mutableStateOf("Champions League") }

    var overall by remember { mutableFloatStateOf(88f) }
    var pac by remember { mutableFloatStateOf(92f) }
    var sho by remember { mutableFloatStateOf(87f) }
    var pas by remember { mutableFloatStateOf(83f) }
    var dri by remember { mutableFloatStateOf(89f) }
    var def by remember { mutableFloatStateOf(45f) }
    var phy by remember { mutableFloatStateOf(78f) }
    var age by remember { mutableFloatStateOf(21f) }
    var wage by remember { mutableFloatStateOf(180f) }

    val livePreviewCard = remember(
        name, selectedPosition, selectedTier, selectedNation, club, league,
        overall, pac, sho, pas, dri, def, phy, age, wage
    ) {
        PlayerCard(
            id = "custom_preview",
            name = name.ifBlank { "Custom Player" },
            overall = overall.toInt(),
            position = selectedPosition,
            nationality = selectedNation.first,
            flagEmoji = selectedNation.second,
            club = club.ifBlank { "Custom FC" },
            league = league,
            tier = selectedTier,
            pac = pac.toInt(),
            sho = sho.toInt(),
            pas = pas.toInt(),
            dri = dri.toInt(),
            def = def.toInt(),
            phy = phy.toInt(),
            age = age.toInt(),
            wage = wage.toInt(),
            marketValue = (overall.toInt() * 1_200_000L).coerceAtLeast(10_000_000L),
            isCustom = true
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PitchBlack)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🎨 FIFA CARD CREATOR STUDIO",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = GoldPrimary,
            letterSpacing = 1.sp
        )
        Text(
            text = "Create custom cards with custom nationalities, stats & FIFA tiers",
            fontSize = 12.sp,
            color = TextSecondaryDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Live Dynamic Preview of the FIFA Card
        FifaPlayerCard(
            player = livePreviewCard,
            size = CardSize.LARGE,
            isHighlighted = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Card Details Form
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardSurfaceDark),
            border = BorderStroke(1.dp, CardBorderGold)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "PLAYER PROFILE & IDENTITY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Player Full Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_card_name_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Position Selector Chips
                Text("POSITION", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(Position.entries) { pos ->
                        FilterChip(
                            selected = selectedPosition == pos,
                            onClick = { selectedPosition = pos },
                            label = { Text(pos.code, fontWeight = FontWeight.Bold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = PitchBlack
                            )
                        )
                    }
                }

                // Nationality Selector
                Text("NATIONALITY & FLAG", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(countriesWithFlags) { (country, flag) ->
                        FilterChip(
                            selected = selectedNation.first == country,
                            onClick = { selectedNation = country to flag },
                            label = { Text("$flag $country") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPitch,
                                selectedLabelColor = PitchBlack
                            )
                        )
                    }
                }

                // Tier Selector
                Text("CARD TIER STYLE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondaryDark)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(CardTier.entries) { tier ->
                        FilterChip(
                            selected = selectedTier == tier,
                            onClick = { selectedTier = tier },
                            label = { Text(tier.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = PitchBlack
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = club,
                        onValueChange = { club = it },
                        label = { Text("Club Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = league,
                        onValueChange = { league = it },
                        label = { Text("League / Tournament") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                HorizontalDivider(color = Color(0x33FFFFFF))

                Text(
                    text = "FIFA ATTRIBUTE STATS (40 - 99)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )

                StatSlider("OVERALL RATING", overall, 70f..99f, GoldPrimary) { overall = it }
                StatSlider("PAC (Pace)", pac, 40f..99f, Color.White) { pac = it }
                StatSlider("SHO (Shooting)", sho, 40f..99f, Color.White) { sho = it }
                StatSlider("PAS (Passing)", pas, 40f..99f, Color.White) { pas = it }
                StatSlider("DRI (Dribbling)", dri, 40f..99f, Color.White) { dri = it }
                StatSlider("DEF (Defending)", def, 40f..99f, Color.White) { def = it }
                StatSlider("PHY (Physical)", phy, 40f..99f, Color.White) { phy = it }
                StatSlider("Age", age, 17f..40f, TextSecondaryDark) { age = it }
                StatSlider("Weekly Wage (€K/wk)", wage, 20f..500f, EmeraldPitch) { wage = it }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val newCard = livePreviewCard.copy(id = "custom_${UUID.randomUUID()}")
                viewModel.createCustomCard(newCard)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("save_custom_card_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = GoldPrimary,
                contentColor = PitchBlack
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(imageVector = Icons.Default.AddCard, contentDescription = "Save Card")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SAVE & ADD TO CARD POOL",
                fontSize = 14.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun StatSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 11.sp, color = TextSecondaryDark, fontWeight = FontWeight.SemiBold)
            Text("${value.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Black, color = accentColor)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor
            )
        )
    }
}
