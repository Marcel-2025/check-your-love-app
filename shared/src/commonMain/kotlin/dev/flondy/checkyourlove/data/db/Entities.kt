package dev.flondy.checkyourlove.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Ein gespeichertes Testergebnis. Ein Namenspaar existiert genau einmal (pair_key). */
@Entity(
    tableName = "love_results",
    indices = [Index(value = ["pair_key"], unique = true), Index(value = ["created_at"])],
)
data class LoveResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name_a") val nameA: String,
    @ColumnInfo(name = "name_b") val nameB: String,
    @ColumnInfo(name = "pair_key") val pairKey: String,
    @ColumnInfo(name = "percent") val percent: Int,
    @ColumnInfo(name = "variant") val variant: Int,
    @ColumnInfo(name = "algorithm_version") val algorithmVersion: Int,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "favorite") val favorite: Boolean = false,
)

/**
 * Unveränderte Übernahme der Daten aus der alten Tabelle „NameValuePairs“ (DB-Version 1).
 * Die alte App speicherte pro Name einen Zufallswert 0–49; ein Paarergebnis war die Summe.
 * Die Werte werden nur aufbewahrt, nicht mehr für neue Berechnungen verwendet.
 */
@Entity(tableName = "legacy_name_values")
data class LegacyNameValueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "name") val name: String?,
    @ColumnInfo(name = "value") val value: Int?,
)
