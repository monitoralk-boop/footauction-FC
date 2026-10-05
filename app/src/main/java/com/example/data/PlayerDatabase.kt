package com.example.data

import com.example.model.CardTier
import com.example.model.PlayerCard
import com.example.model.Position

object PlayerDatabase {

    // --- STARTER SQUAD (Rating between 60 and 70) ---
    val starterSquad: List<PlayerCard> = listOf(
        PlayerCard("st_gk_1", "Lucas Morales", 66, Position.GK, "Chile", "🇨🇱", "Santiago FC", "National League", CardTier.UNKNOWN, 65, 62, 64, 66, 42, 68, 22, 12, 1_500_000L, jerseyNumber = 1),
        PlayerCard("st_lb_1", "Yuki Tanaka", 64, Position.LB, "Japan", "🇯🇵", "Yokohama City", "Asian League", CardTier.COMMON, 74, 52, 62, 65, 63, 62, 21, 10, 1_200_000L, jerseyNumber = 3),
        PlayerCard("st_cb_1", "Mateo Bianchi", 68, Position.CB, "Italy", "🇮🇹", "Monza Calcio", "Serie B", CardTier.UNKNOWN, 62, 40, 58, 59, 70, 72, 24, 15, 2_000_000L, jerseyNumber = 4),
        PlayerCard("st_cb_2", "Ibrahim Diallo", 67, Position.CB, "Senegal", "🇸🇳", "Dakar United", "African Cup", CardTier.UNKNOWN, 66, 38, 55, 57, 69, 74, 23, 14, 1_800_000L, jerseyNumber = 5),
        PlayerCard("st_rb_1", "Carlos Mendoza", 64, Position.RB, "Mexico", "🇲🇽", "Guadalajara CF", "Americas Cup", CardTier.COMMON, 72, 50, 60, 63, 64, 65, 22, 11, 1_400_000L, jerseyNumber = 2),
        PlayerCard("st_cm_1", "Liam O'Connor", 67, Position.CM, "Ireland", "🇮🇪", "Dublin Rovers", "National League", CardTier.UNKNOWN, 68, 64, 69, 66, 62, 65, 23, 15, 1_900_000L, jerseyNumber = 8),
        PlayerCard("st_cdm_1", "Sofiane Belkacem", 68, Position.CDM, "Morocco", "🇲🇦", "Rabat Athletic", "African Cup", CardTier.UNKNOWN, 64, 58, 67, 66, 70, 71, 24, 16, 2_100_000L, jerseyNumber = 6),
        PlayerCard("st_cam_1", "Thiago Ribeiro", 69, Position.CAM, "Brazil", "🇧🇷", "Santos Youth", "Americas Cup", CardTier.UNKNOWN, 74, 68, 71, 72, 45, 60, 20, 18, 2_500_000L, jerseyNumber = 10),
        PlayerCard("st_lw_1", "Felix Nygård", 66, Position.LW, "Sweden", "🇸🇪", "Stockholm BK", "Nordic League", CardTier.UNKNOWN, 78, 64, 63, 68, 38, 61, 21, 13, 1_700_000L, jerseyNumber = 11),
        PlayerCard("st_st_1", "Tunde Adeleke", 69, Position.ST, "Nigeria", "🇳🇬", "Lagos Stars", "African Cup", CardTier.UNKNOWN, 80, 71, 58, 67, 35, 70, 22, 19, 2_600_000L, jerseyNumber = 9),
        PlayerCard("st_rw_1", "Ethan Walker", 63, Position.RW, "USA", "🇺🇸", "Austin United", "MLS", CardTier.COMMON, 77, 63, 61, 66, 36, 62, 21, 12, 1_500_000L, jerseyNumber = 7)
    )

    // --- COMMON (45 - 64) BASE PLAYERS ---
    val commonGrassrootsPool: List<PlayerCard> = listOf(
        PlayerCard("cm_1", "Piotr Kowalski", 62, Position.CB, "Poland", "🇵🇱", "Warsaw Legion", "European Tier", CardTier.COMMON, 60, 36, 56, 58, 67, 71, 25, 12, 1_400_000L, jerseyNumber = 15),
        PlayerCard("cm_2", "Min-jun Park", 64, Position.CM, "South Korea", "🇰🇷", "Busan City", "Asian League", CardTier.COMMON, 70, 62, 68, 67, 60, 62, 23, 14, 1_600_000L, jerseyNumber = 16),
        PlayerCard("cm_3", "Antoine Mercier", 63, Position.LB, "France", "🇫🇷", "Toulouse FC", "Ligue 2", CardTier.COMMON, 75, 54, 65, 67, 66, 64, 22, 16, 1_500_000L, jerseyNumber = 17),
        PlayerCard("cm_4", "Kwame Mensah", 62, Position.ST, "Ghana", "🇬🇭", "Accra Sporting", "African Cup", CardTier.COMMON, 79, 68, 55, 66, 32, 69, 21, 15, 1_400_000L, jerseyNumber = 18),
        PlayerCard("cm_5", "Luka Varga", 61, Position.GK, "Croatia", "🇭🇷", "Split United", "Balkan League", CardTier.COMMON, 66, 64, 62, 67, 40, 67, 26, 13, 1_300_000L, jerseyNumber = 12),
        PlayerCard("cm_9", "Omar Mansour", 64, Position.RB, "Egypt", "🇪🇬", "Alexandria SC", "African Cup", CardTier.COMMON, 74, 52, 62, 65, 65, 66, 23, 14, 1_400_000L, jerseyNumber = 22),
        PlayerCard("cm_11", "Darius Vance", 58, Position.CM, "Canada", "🇨🇦", "Toronto Stars", "MLS", CardTier.COMMON, 66, 58, 62, 60, 56, 61, 20, 8, 900_000L, jerseyNumber = 24),
        PlayerCard("cm_12", "Bjorn Lind", 59, Position.CB, "Norway", "🇳🇴", "Bergen SK", "Nordic League", CardTier.COMMON, 58, 30, 50, 52, 64, 66, 22, 9, 950_000L, jerseyNumber = 25)
    )

    // --- 70 TO 80 POOL (FOR FREE AD PACK & SCOUTING) ---
    val adPackPlayers70To80: List<PlayerCard> = listOf(
        PlayerCard("ad_guler", "Arda Güler", 78, Position.CAM, "Turkey", "🇹🇷", "Real Madrid", "La Liga", CardTier.RARE, 74, 76, 82, 84, 52, 60, 19, 60, 25_000_000L, jerseyNumber = 15),
        PlayerCard("ad_endrick", "Endrick", 77, Position.ST, "Brazil", "🇧🇷", "Real Madrid", "La Liga", CardTier.RARE, 86, 79, 68, 79, 36, 78, 18, 55, 28_000_000L, jerseyNumber = 16),
        PlayerCard("ad_mainoo", "Kobbie Mainoo", 79, Position.CM, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Manchester United", "Premier League", CardTier.RARE, 75, 71, 79, 82, 76, 75, 19, 70, 32_000_000L, jerseyNumber = 37),
        PlayerCard("ad_cubarsi", "Pau Cubarsí", 77, Position.CB, "Spain", "🇪🇸", "Barcelona", "La Liga", CardTier.RARE, 70, 38, 76, 74, 80, 74, 17, 50, 24_000_000L, jerseyNumber = 2),
        PlayerCard("ad_fermin", "Fermin López", 78, Position.CM, "Spain", "🇪🇸", "Barcelona", "La Liga", CardTier.RARE, 78, 78, 77, 80, 64, 72, 21, 55, 26_000_000L, jerseyNumber = 16),
        PlayerCard("ad_lewis", "Rico Lewis", 76, Position.RB, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Manchester City", "Premier League", CardTier.RARE, 79, 58, 76, 80, 75, 68, 20, 60, 22_000_000L, jerseyNumber = 82),
        PlayerCard("ad_barcola", "Bradley Barcola", 79, Position.LW, "France", "🇫🇷", "Paris Saint-Germain", "Ligue 1", CardTier.RARE, 89, 75, 75, 83, 38, 66, 22, 75, 35_000_000L, jerseyNumber = 29),
        PlayerCard("ad_neves", "João Neves", 80, Position.CM, "Portugal", "🇵🇹", "Paris Saint-Germain", "Ligue 1", CardTier.RARE, 78, 70, 80, 82, 79, 78, 20, 85, 40_000_000L, jerseyNumber = 87),
        PlayerCard("ad_zaire", "Warren Zaïre-Emery", 80, Position.CM, "France", "🇫🇷", "Paris Saint-Germain", "Ligue 1", CardTier.RARE, 79, 72, 79, 81, 78, 80, 18, 80, 38_000_000L, jerseyNumber = 33),
        PlayerCard("ad_tel", "Mathys Tel", 77, Position.ST, "France", "🇫🇷", "Bayern Munich", "Bundesliga", CardTier.RARE, 87, 78, 70, 80, 32, 74, 19, 60, 24_000_000L, jerseyNumber = 39),
        PlayerCard("ad_sesko", "Benjamin Šeško", 79, Position.ST, "Slovenia", "🇸🇮", "RB Leipzig", "Bundesliga", CardTier.RARE, 86, 80, 67, 76, 40, 82, 21, 70, 30_000_000L, jerseyNumber = 30),
        PlayerCard("ad_yildiz", "Kenan Yildiz", 77, Position.LW, "Turkey", "🇹🇷", "Juventus", "Serie A", CardTier.RARE, 84, 76, 75, 81, 35, 68, 19, 50, 22_000_000L, jerseyNumber = 10),
        PlayerCard("ad_ferguson", "Evan Ferguson", 75, Position.ST, "Ireland", "🇮🇪", "Brighton", "Premier League", CardTier.RARE, 77, 78, 64, 73, 38, 80, 20, 45, 18_000_000L, jerseyNumber = 28),
        PlayerCard("ad_elliott", "Harvey Elliott", 78, Position.CAM, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Liverpool", "Premier League", CardTier.RARE, 77, 75, 81, 82, 54, 62, 21, 65, 25_000_000L, jerseyNumber = 19),
        PlayerCard("ad_bobb", "Oscar Bobb", 76, Position.RW, "Norway", "🇳🇴", "Manchester City", "Premier League", CardTier.RARE, 82, 72, 76, 82, 42, 60, 21, 55, 20_000_000L, jerseyNumber = 52),
        PlayerCard("ad_udogie", "Destiny Udogie", 79, Position.LB, "Italy", "🇮🇹", "Tottenham", "Premier League", CardTier.RARE, 88, 64, 74, 80, 77, 80, 22, 75, 30_000_000L, jerseyNumber = 38),
        PlayerCard("ad_gusto", "Malo Gusto", 77, Position.RB, "France", "🇫🇷", "Chelsea", "Premier League", CardTier.RARE, 86, 52, 75, 78, 75, 74, 21, 65, 22_000_000L, jerseyNumber = 27),
        PlayerCard("ad_pino", "Yeremy Pino", 79, Position.RW, "Spain", "🇪🇸", "Villarreal", "La Liga", CardTier.RARE, 82, 77, 76, 81, 44, 66, 22, 70, 28_000_000L, jerseyNumber = 21),
        PlayerCard("ad_inacio", "Gonçalo Inácio", 80, Position.CB, "Portugal", "🇵🇹", "Sporting CP", "Champions League", CardTier.RARE, 74, 45, 74, 72, 82, 79, 23, 75, 30_000_000L, jerseyNumber = 25),
        PlayerCard("ad_lukeba", "Castello Lukeba", 78, Position.CB, "France", "🇫🇷", "RB Leipzig", "Bundesliga", CardTier.RARE, 76, 40, 68, 70, 80, 78, 22, 65, 25_000_000L, jerseyNumber = 23),
        PlayerCard("ad_paz", "Nico Paz", 74, Position.CAM, "Argentina", "🇦🇷", "Como 1907", "Serie A", CardTier.UNKNOWN, 76, 73, 76, 78, 48, 68, 20, 35, 14_000_000L, jerseyNumber = 79),
        PlayerCard("ad_jobe", "Jobe Bellingham", 73, Position.CM, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Sunderland", "Championship", CardTier.UNKNOWN, 74, 70, 72, 74, 65, 76, 19, 30, 12_000_000L, jerseyNumber = 7),
        PlayerCard("ad_nusa", "Antonio Nusa", 75, Position.LW, "Norway", "🇳🇴", "RB Leipzig", "Bundesliga", CardTier.RARE, 88, 70, 71, 82, 34, 62, 19, 40, 16_000_000L, jerseyNumber = 7),
        PlayerCard("ad_samu", "Samu Omorodion", 76, Position.ST, "Spain", "🇪🇸", "Porto", "Champions League", CardTier.RARE, 85, 77, 58, 72, 38, 86, 20, 50, 22_000_000L, jerseyNumber = 9)
    )

    // --- RARE GOLD POOL (75 - 81) ---
    val rareGoldPool: List<PlayerCard> = listOf(
        PlayerCard("r_1", "Takefusa Kubo", 80, Position.RW, "Japan", "🇯🇵", "Real Sociedad", "La Liga", CardTier.RARE, 86, 76, 79, 85, 40, 58, 23, 85, 30_000_000L, jerseyNumber = 14),
        PlayerCard("r_2", "Sofyan Amrabat", 79, Position.CDM, "Morocco", "🇲🇦", "Fenerbahçe", "Super Lig", CardTier.RARE, 68, 65, 75, 74, 80, 84, 28, 70, 22_000_000L, jerseyNumber = 34),
        PlayerCard("r_3", "Darwin Núñez", 81, Position.ST, "Uruguay", "🇺🇾", "Liverpool", "Premier League", CardTier.RARE, 89, 81, 71, 76, 42, 85, 25, 140, 45_000_000L, jerseyNumber = 9),
        PlayerCard("r_4", "Lisandro Martínez", 81, Position.CB, "Argentina", "🇦🇷", "Manchester United", "Premier League", CardTier.RARE, 74, 50, 77, 75, 83, 80, 26, 120, 40_000_000L, jerseyNumber = 6),
        PlayerCard("r_5", "Alejandro Garnacho", 79, Position.LW, "Argentina", "🇦🇷", "Manchester United", "Premier League", CardTier.RARE, 87, 76, 74, 82, 35, 60, 20, 65, 35_000_000L, jerseyNumber = 17),
        PlayerCard("r_6", "Yassine Bounou", 81, Position.GK, "Morocco", "🇲🇦", "Al Hilal", "Saudi Pro League", CardTier.RARE, 82, 84, 76, 85, 48, 83, 33, 110, 25_000_000L, jerseyNumber = 37),
        PlayerCard("r_7", "Brahim Díaz", 80, Position.CAM, "Morocco", "🇲🇦", "Real Madrid", "La Liga", CardTier.RARE, 83, 76, 78, 85, 35, 55, 25, 90, 32_000_000L, jerseyNumber = 21),
        PlayerCard("r_8", "Pedro Neto", 79, Position.RW, "Portugal", "🇵🇹", "Chelsea", "Premier League", CardTier.RARE, 88, 74, 77, 83, 38, 62, 24, 85, 34_000_000L, jerseyNumber = 19)
    )

    // --- EPIC AMETHYST POOL (82 - 89) ---
    val epicAmethystPool: List<PlayerCard> = listOf(
        PlayerCard("p_yamal", "Lamine Yamal", 85, Position.RW, "Spain", "🇪🇸", "Barcelona", "La Liga", CardTier.EPIC, 91, 80, 83, 89, 30, 56, 17, 100, 120_000_000L, jerseyNumber = 19),
        PlayerCard("p_son", "Son Heung-min", 87, Position.LW, "South Korea", "🇰🇷", "Tottenham", "Premier League", CardTier.EPIC, 87, 89, 82, 84, 42, 70, 32, 220, 50_000_000L, jerseyNumber = 7),
        PlayerCard("p_saka", "Bukayo Saka", 88, Position.RW, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Arsenal", "Premier League", CardTier.EPIC, 86, 83, 83, 88, 65, 76, 23, 280, 110_000_000L, jerseyNumber = 7),
        PlayerCard("p_osimhen", "Victor Osimhen", 87, Position.ST, "Nigeria", "🇳🇬", "Galatasaray", "Champions League", CardTier.EPIC, 90, 85, 65, 81, 42, 82, 25, 250, 75_000_000L, jerseyNumber = 45),
        PlayerCard("p_lautaro", "Lautaro Martínez", 89, Position.ST, "Argentina", "🇦🇷", "Inter Milan", "Serie A", CardTier.EPIC, 81, 88, 75, 86, 48, 84, 27, 270, 100_000_000L, jerseyNumber = 10),
        PlayerCard("p_musiala", "Jamal Musiala", 88, Position.CAM, "Germany", "🇩🇪", "Bayern Munich", "Bundesliga", CardTier.EPIC, 85, 81, 82, 91, 63, 65, 21, 230, 120_000_000L, jerseyNumber = 42),
        PlayerCard("p_wirtz", "Florian Wirtz", 89, Position.CAM, "Germany", "🇩🇪", "Bayer Leverkusen", "Bundesliga", CardTier.EPIC, 82, 82, 87, 89, 55, 68, 21, 240, 130_000_000L, jerseyNumber = 10),
        PlayerCard("p_odegaard", "Martin Ødegaard", 89, Position.CAM, "Norway", "🇳🇴", "Arsenal", "Premier League", CardTier.EPIC, 78, 82, 89, 88, 68, 67, 25, 260, 100_000_000L, jerseyNumber = 8),
        PlayerCard("p_rice", "Declan Rice", 87, Position.CDM, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Arsenal", "Premier League", CardTier.EPIC, 74, 72, 81, 79, 86, 86, 25, 250, 95_000_000L, jerseyNumber = 41),
        PlayerCard("p_valverde", "Federico Valverde", 89, Position.CM, "Uruguay", "🇺🇾", "Real Madrid", "La Liga", CardTier.EPIC, 88, 82, 84, 84, 80, 84, 26, 290, 115_000_000L, jerseyNumber = 8),
        PlayerCard("p_hakimi", "Achraf Hakimi", 86, Position.RB, "Morocco", "🇲🇦", "Paris Saint-Germain", "Ligue 1", CardTier.EPIC, 92, 76, 80, 82, 76, 78, 26, 260, 70_000_000L, jerseyNumber = 2),
        PlayerCard("p_davies", "Alphonso Davies", 84, Position.LB, "Canada", "🇨🇦", "Bayern Munich", "Bundesliga", CardTier.EPIC, 95, 68, 78, 84, 75, 77, 24, 180, 60_000_000L, jerseyNumber = 19),
        PlayerCard("p_ronaldo", "Cristiano Ronaldo", 88, Position.ST, "Portugal", "🇵🇹", "Al Nassr", "Champions League", CardTier.EPIC, 82, 91, 79, 84, 34, 77, 39, 400, 35_000_000L, jerseyNumber = 7),
        PlayerCard("p_saliba", "William Saliba", 88, Position.CB, "France", "🇫🇷", "Arsenal", "Premier League", CardTier.EPIC, 83, 40, 70, 73, 88, 83, 23, 230, 85_000_000L, jerseyNumber = 2),
        PlayerCard("p_dias", "Rúben Dias", 88, Position.CB, "Portugal", "🇵🇹", "Manchester City", "Premier League", CardTier.EPIC, 68, 39, 70, 69, 89, 87, 27, 250, 80_000_000L, jerseyNumber = 3)
    )

    // --- LEGENDARY DIAMOND HOLOGRAPHIC POOL (90 - 99) ---
    val legendaryHoloPool: List<PlayerCard> = listOf(
        PlayerCard("p_messi", "Lionel Messi", 90, Position.RW, "Argentina", "🇦🇷", "Inter Miami", "World Cup All-Stars", CardTier.LEGENDARY, 81, 89, 90, 94, 35, 64, 37, 350, 45_000_000L, jerseyNumber = 10),
        PlayerCard("p_mbappe", "Kylian Mbappé", 92, Position.ST, "France", "🇫🇷", "Real Madrid", "La Liga", CardTier.LEGENDARY, 97, 90, 80, 92, 36, 78, 26, 500, 180_000_000L, jerseyNumber = 9),
        PlayerCard("p_haaland", "Erling Haaland", 92, Position.ST, "Norway", "🇳🇴", "Manchester City", "Premier League", CardTier.LEGENDARY, 89, 93, 66, 80, 45, 88, 24, 450, 175_000_000L, jerseyNumber = 9),
        PlayerCard("p_vinicius", "Vinícius Júnior", 91, Position.LW, "Brazil", "🇧🇷", "Real Madrid", "Champions League", CardTier.LEGENDARY, 96, 84, 81, 93, 29, 69, 24, 380, 150_000_000L, jerseyNumber = 7),
        PlayerCard("p_salah", "Mohamed Salah", 90, Position.RW, "Egypt", "🇪🇬", "Liverpool", "Premier League", CardTier.LEGENDARY, 89, 88, 82, 88, 45, 76, 32, 350, 75_000_000L, jerseyNumber = 11),
        PlayerCard("p_kane", "Harry Kane", 90, Position.ST, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Bayern Munich", "Bundesliga", CardTier.LEGENDARY, 69, 93, 84, 83, 49, 83, 31, 360, 90_000_000L, jerseyNumber = 9),
        PlayerCard("p_bellingham", "Jude Bellingham", 91, Position.CAM, "England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Real Madrid", "La Liga", CardTier.LEGENDARY, 81, 87, 84, 88, 78, 83, 21, 350, 160_000_000L, jerseyNumber = 5),
        PlayerCard("p_rodri", "Rodri", 92, Position.CDM, "Spain", "🇪🇸", "Manchester City", "Champions League", CardTier.LEGENDARY, 66, 80, 86, 84, 87, 85, 28, 340, 130_000_000L, jerseyNumber = 16),
        PlayerCard("p_debruyne", "Kevin De Bruyne", 91, Position.CM, "Belgium", "🇧🇪", "Manchester City", "Premier League", CardTier.LEGENDARY, 72, 87, 94, 87, 65, 78, 33, 420, 65_000_000L, jerseyNumber = 17),
        PlayerCard("p_vandijk", "Virgil van Dijk", 90, Position.CB, "Netherlands", "🇳🇱", "Liverpool", "Premier League", CardTier.LEGENDARY, 78, 60, 71, 72, 91, 86, 33, 320, 55_000_000L, jerseyNumber = 4),
        PlayerCard("p_courtois", "Thibaut Courtois", 90, Position.GK, "Belgium", "🇧🇪", "Real Madrid", "La Liga", CardTier.LEGENDARY, 85, 89, 76, 88, 46, 90, 32, 280, 45_000_000L, jerseyNumber = 1),
        PlayerCard("p_zidane", "Zinédine Zidane", 95, Position.CAM, "France", "🇫🇷", "Icon Legends", "World Cup All-Stars", CardTier.LEGENDARY, 84, 92, 96, 95, 75, 86, 34, 500, 210_000_000L, jerseyNumber = 10),
        PlayerCard("p_ronaldinho", "Ronaldinho", 94, Position.LW, "Brazil", "🇧🇷", "Icon Legends", "World Cup All-Stars", CardTier.LEGENDARY, 92, 90, 91, 97, 42, 81, 26, 480, 200_000_000L, jerseyNumber = 10),
        PlayerCard("p_maldini", "Paolo Maldini", 94, Position.CB, "Italy", "🇮🇹", "Icon Legends", "Champions League", CardTier.LEGENDARY, 86, 56, 75, 70, 96, 83, 32, 420, 180_000_000L, jerseyNumber = 3)
    )

    // Complete Database of all players
    val defaultPlayers: List<PlayerCard> = (
        starterSquad + commonGrassrootsPool + adPackPlayers70To80 + rareGoldPool + epicAmethystPool + legendaryHoloPool
    ).distinctBy { it.id }

    val leagues: List<String> = listOf(
        "All Leagues",
        "Champions League",
        "World Cup All-Stars",
        "Premier League",
        "La Liga",
        "Bundesliga",
        "Serie A",
        "Ligue 1",
        "Americas Cup",
        "African Cup",
        "Asian League"
    )

    private val firstNames = listOf(
        "Mateo", "Lucas", "Alex", "Julian", "Karim", "Adam", "Youssef", "David", "Carlos", "Thiago",
        "Marco", "Noah", "Kenji", "Omar", "Liam", "Leo", "Victor", "Enzo", "Samuel", "Diego",
        "Gabriel", "Rafael", "Arthur", "Tariq", "Hichem", "Mehdi", "Tatsuya", "Hiroshi", "Daniel", "Patrick"
    )

    private val lastNames = listOf(
        "Santos", "Silva", "Fernandez", "Rossi", "Martin", "Dubois", "Müller", "Costa", "Pereira",
        "Alami", "Benali", "Mansouri", "Traoré", "Diallo", "Mensah", "Tanaka", "Sato", "Kim", "Park",
        "O'Neill", "Johansson", "Larsen", "Kovacic", "Petrovic", "Walker", "Davis", "Moreno", "Romero"
    )

    private val nations = listOf(
        Triple("Brazil", "🇧🇷", "Americas Cup"),
        Triple("Argentina", "🇦🇷", "Americas Cup"),
        Triple("France", "🇫🇷", "Ligue 1"),
        Triple("Spain", "🇪🇸", "La Liga"),
        Triple("Germany", "🇩🇪", "Bundesliga"),
        Triple("Italy", "🇮🇹", "Serie A"),
        Triple("England", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "Premier League"),
        Triple("Portugal", "🇵🇹", "Champions League"),
        Triple("Morocco", "🇲🇦", "African Cup"),
        Triple("Tunisia", "🇹🇳", "African Cup"),
        Triple("Algeria", "🇩🇿", "African Cup"),
        Triple("Nigeria", "🇳🇬", "African Cup"),
        Triple("Senegal", "🇸🇳", "African Cup"),
        Triple("Japan", "🇯🇵", "Asian League"),
        Triple("South Korea", "🇰🇷", "Asian League"),
        Triple("Mexico", "🇲🇽", "Americas Cup"),
        Triple("Colombia", "🇨🇴", "Americas Cup"),
        Triple("Uruguay", "🇺🇾", "Americas Cup"),
        Triple("Netherlands", "🇳🇱", "Champions League"),
        Triple("Norway", "🇳🇴", "Bundesliga"),
        Triple("Sweden", "🇸🇪", "Champions League")
    )

    private val clubs = listOf(
        "Sevilla FC", "Real Betis", "Torino FC", "Genoa CFC", "FC Nantes", "Montpellier HSC",
        "Everton FC", "Wolverhampton", "FC Augsburg", "FSV Mainz 05", "Boca Juniors", "River Plate",
        "Santos FC", "Flamengo", "Raja Casablanca", "Esperance Tunis", "Al Ahly SC", "Urawa Reds",
        "Celtic FC", "Feyenoord", "Sporting Gijón", "Espanyol", "Sampdoria", "Parma Calcio"
    )

    fun generateRandomPlayer(pos: Position, ratingMin: Int = 60, ratingMax: Int = 70): PlayerCard {
        val ovr = kotlin.random.Random.nextInt(ratingMin, ratingMax + 1)
        val firstName = firstNames.random()
        val lastName = lastNames.random()
        val (country, flag, league) = nations.random()
        val club = clubs.random()
        val id = "rnd_${pos.code.lowercase()}_${System.nanoTime() % 1000000}_${kotlin.random.Random.nextInt(100, 999)}"

        val pace = when (pos) {
            Position.ST, Position.LW, Position.RW -> (ovr + kotlin.random.Random.nextInt(4, 12)).coerceIn(55, 88)
            Position.LB, Position.RB -> (ovr + kotlin.random.Random.nextInt(3, 10)).coerceIn(55, 84)
            Position.CB -> (ovr - kotlin.random.Random.nextInt(2, 8)).coerceIn(50, 72)
            else -> ovr + kotlin.random.Random.nextInt(-3, 5)
        }

        val shooting = when (pos) {
            Position.ST -> (ovr + kotlin.random.Random.nextInt(2, 6)).coerceIn(58, 76)
            Position.LW, Position.RW, Position.CAM -> (ovr - kotlin.random.Random.nextInt(0, 4)).coerceIn(55, 74)
            Position.CB, Position.LB, Position.RB -> (ovr - kotlin.random.Random.nextInt(15, 25)).coerceIn(30, 52)
            Position.GK -> (ovr - kotlin.random.Random.nextInt(30, 40)).coerceIn(20, 35)
            else -> (ovr - kotlin.random.Random.nextInt(3, 8)).coerceIn(50, 70)
        }

        val passing = when (pos) {
            Position.CM, Position.CAM -> (ovr + kotlin.random.Random.nextInt(1, 5)).coerceIn(60, 75)
            Position.CDM -> (ovr - kotlin.random.Random.nextInt(0, 3)).coerceIn(58, 72)
            Position.CB -> (ovr - kotlin.random.Random.nextInt(5, 12)).coerceIn(48, 65)
            Position.GK -> (ovr - kotlin.random.Random.nextInt(5, 10)).coerceIn(55, 68)
            else -> (ovr - kotlin.random.Random.nextInt(1, 6)).coerceIn(54, 72)
        }

        val dribbling = when (pos) {
            Position.LW, Position.RW, Position.CAM -> (ovr + kotlin.random.Random.nextInt(2, 7)).coerceIn(62, 78)
            Position.ST, Position.CM -> (ovr + kotlin.random.Random.nextInt(-1, 4)).coerceIn(58, 74)
            Position.CB -> (ovr - kotlin.random.Random.nextInt(6, 12)).coerceIn(50, 65)
            else -> ovr + kotlin.random.Random.nextInt(-3, 3)
        }

        val defending = when (pos) {
            Position.CB -> (ovr + kotlin.random.Random.nextInt(2, 6)).coerceIn(62, 75)
            Position.CDM, Position.LB, Position.RB -> (ovr + kotlin.random.Random.nextInt(0, 4)).coerceIn(60, 73)
            Position.ST, Position.LW, Position.RW -> (ovr - kotlin.random.Random.nextInt(20, 30)).coerceIn(28, 45)
            else -> (ovr - kotlin.random.Random.nextInt(5, 12)).coerceIn(48, 65)
        }

        val physical = when (pos) {
            Position.CB, Position.CDM, Position.ST -> (ovr + kotlin.random.Random.nextInt(2, 7)).coerceIn(62, 77)
            Position.GK -> (ovr + kotlin.random.Random.nextInt(1, 5)).coerceIn(60, 74)
            else -> (ovr + kotlin.random.Random.nextInt(-2, 4)).coerceIn(56, 72)
        }

        val age = kotlin.random.Random.nextInt(19, 29)
        val wage = kotlin.random.Random.nextInt(8, 20)
        val value = (ovr * 25_000L) + kotlin.random.Random.nextInt(200_000, 600_000)

        val jersey = when (pos) {
            Position.GK -> 1
            Position.RB -> 2
            Position.LB -> 3
            Position.CB -> if (kotlin.random.Random.nextBoolean()) 4 else 5
            Position.CDM -> 6
            Position.RW -> 7
            Position.CM -> 8
            Position.ST -> 9
            Position.CAM -> 10
            Position.LW -> 11
        }

        return PlayerCard(
            id = id,
            name = "$firstName $lastName",
            overall = ovr,
            position = pos,
            nationality = country,
            flagEmoji = flag,
            club = club,
            league = league,
            tier = CardTier.fromRating(ovr),
            pac = pace,
            sho = shooting,
            pas = passing,
            dri = dribbling,
            def = defending,
            phy = physical,
            age = age,
            wage = wage,
            marketValue = value,
            jerseyNumber = jersey
        )
    }

    fun generateRandomStarterSquad(): Pair<List<PlayerCard>, List<PlayerCard>> {
        val starting11 = listOf(
            generateRandomPlayer(Position.GK, 62, 69),
            generateRandomPlayer(Position.LB, 62, 68),
            generateRandomPlayer(Position.CB, 63, 70),
            generateRandomPlayer(Position.CB, 62, 69),
            generateRandomPlayer(Position.RB, 61, 68),
            generateRandomPlayer(Position.CDM, 63, 69),
            generateRandomPlayer(Position.CM, 64, 70),
            generateRandomPlayer(Position.CAM, 65, 70),
            generateRandomPlayer(Position.LW, 63, 70),
            generateRandomPlayer(Position.ST, 65, 70),
            generateRandomPlayer(Position.RW, 63, 69)
        )

        val reserves = listOf(
            generateRandomPlayer(Position.GK, 60, 65),
            generateRandomPlayer(Position.CB, 60, 66),
            generateRandomPlayer(Position.CM, 61, 67),
            generateRandomPlayer(Position.ST, 61, 68)
        )

        return Pair(starting11, reserves)
    }
}

