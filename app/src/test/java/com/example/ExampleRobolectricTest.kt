package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PlayerDatabase
import com.example.model.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FootAuction FC", appName)
    }

    @Test
    fun `verify default players database and starter squad`() {
        val starter = PlayerDatabase.starterSquad
        assertEquals(11, starter.size)
        starter.forEach { player ->
            assertTrue("Starter player ${player.name} (${player.overall}) should be between 60 and 70 OVR", player.overall in 60..70)
        }
    }

    @Test
    fun `verify division progression from 0 trophies in division 9`() {
        assertEquals(DivisionTier.DIV_9, DivisionTier.fromTrophies(0))
        assertEquals(DivisionTier.DIV_9, DivisionTier.fromTrophies(99))
        assertEquals(DivisionTier.DIV_8, DivisionTier.fromTrophies(100))
        assertEquals(DivisionTier.DIV_7, DivisionTier.fromTrophies(200))
        assertEquals(DivisionTier.DIV_6, DivisionTier.fromTrophies(300))
        assertEquals(DivisionTier.DIV_5, DivisionTier.fromTrophies(400))
        assertEquals(DivisionTier.DIV_4, DivisionTier.fromTrophies(500))
        assertEquals(DivisionTier.DIV_3, DivisionTier.fromTrophies(600))
        assertEquals(DivisionTier.DIV_2, DivisionTier.fromTrophies(700))
        assertEquals(DivisionTier.DIV_1, DivisionTier.fromTrophies(850))
    }

    @Test
    fun `test match simulation scores and stats`() {
        val teamA = Team(
            id = "t_a",
            name = "Team Alpha",
            squad = PlayerDatabase.defaultPlayers.take(11)
        )
        val teamB = Team(
            id = "t_b",
            name = "Team Beta",
            squad = PlayerDatabase.defaultPlayers.drop(11).take(11)
        )

        val result = MatchSimulator.simulate(teamA, teamB)
        assertNotNull(result)
        assertTrue(result.homeScore >= 0)
        assertTrue(result.awayScore >= 0)
        assertEquals(100, result.homePossession + result.awayPossession)
        assertTrue(result.events.isNotEmpty())
    }
}
