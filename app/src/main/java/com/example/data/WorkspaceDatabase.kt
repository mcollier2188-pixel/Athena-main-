package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

import androidx.room.migration.Migration

@Database(
    entities = [
        WorkspaceItem::class,
        FrequencyHistoryEntry::class,
        FrequencyPreset::class,
        ResonanceIntervalCalculation::class,
        ProjectEntity::class,
        MultimediaProjectEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class WorkspaceDatabase : RoomDatabase() {
    abstract fun workspaceDao(): WorkspaceDao
    abstract fun frequencyHistoryDao(): FrequencyHistoryDao
    abstract fun frequencyPresetDao(): FrequencyPresetDao
    abstract fun resonanceIntervalDao(): ResonanceIntervalDao
    abstract fun projectDao(): ProjectDao
    abstract fun multimediaProjectDao(): MultimediaProjectDao

    companion object {
        @Volatile
        private var INSTANCE: WorkspaceDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `frequency_history` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `frequencyHz` REAL NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `durationSeconds` INTEGER NOT NULL,
                        `isResonanceLock` INTEGER NOT NULL,
                        `sessionNote` TEXT
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `frequency_presets` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `frequencyHz` REAL NOT NULL,
                        `label` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `isDefault` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("INSERT INTO `frequency_presets` (`frequencyHz`, `label`, `description`, `isDefault`, `orderIndex`) VALUES (42.000, 'Sub-Band Floor', '42.000 Hz baseline threshold', 1, 0)")
                db.execSQL("INSERT INTO `frequency_presets` (`frequencyHz`, `label`, `description`, `isDefault`, `orderIndex`) VALUES (42.598, 'Hilsea 1986 Lab', '42.598 Hz chalk layer alignment', 1, 1)")
                db.execSQL("INSERT INTO `frequency_presets` (`frequencyHz`, `label`, `description`, `isDefault`, `orderIndex`) VALUES (42.600, 'Fundamental Peak', '42.600 Hz bedrock standing wave', 1, 2)")
                db.execSQL("INSERT INTO `frequency_presets` (`frequencyHz`, `label`, `description`, `isDefault`, `orderIndex`) VALUES (43.000, 'Tesla 1899 Peak', '43.000 Hz terrestrial wave peak', 1, 3)")
            }
        }

        val MIGRATION_1_3 = object : Migration(1, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                MIGRATION_1_2.migrate(db)
                MIGRATION_2_3.migrate(db)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `resonance_intervals` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `intervalLabel` TEXT NOT NULL,
                        `multiplier` REAL NOT NULL,
                        `baseFrequencyHz` REAL NOT NULL,
                        `calculatedFrequencyHz` REAL NOT NULL,
                        `wavelengthMeters` REAL NOT NULL,
                        `periodSeconds` REAL NOT NULL,
                        `acousticVelocityMps` REAL NOT NULL,
                        `couplingEfficiencyPercent` REAL NOT NULL,
                        `note` TEXT NOT NULL,
                        `timestampEpochMs` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `projects` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `modifiedAt` INTEGER NOT NULL,
                        `colorTagHex` TEXT NOT NULL,
                        `isArchived` INTEGER NOT NULL,
                        `orderIndex` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `multimedia_projects` (
                        `id` TEXT NOT NULL PRIMARY KEY,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `mediaType` TEXT NOT NULL,
                        `filePath` TEXT NOT NULL,
                        `thumbnailPath` TEXT,
                        `fileSizeBytes` INTEGER NOT NULL,
                        `durationSeconds` REAL NOT NULL,
                        `mimeType` TEXT NOT NULL,
                        `resolution` TEXT,
                        `aspectRatio` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `modifiedAt` INTEGER NOT NULL,
                        `isFavorite` INTEGER NOT NULL,
                        `tags` TEXT NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun getDatabase(context: Context): WorkspaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WorkspaceDatabase::class.java,
                    "echo_workspace_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_1_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(true)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(
                                    getDatabase(context).workspaceDao(),
                                    getDatabase(context).frequencyHistoryDao(),
                                    getDatabase(context).frequencyPresetDao(),
                                    getDatabase(context).resonanceIntervalDao(),
                                    getDatabase(context).projectDao(),
                                    getDatabase(context).multimediaProjectDao()
                                )
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(
            dao: WorkspaceDao,
            historyDao: FrequencyHistoryDao,
            presetDao: FrequencyPresetDao,
            intervalDao: ResonanceIntervalDao,
            projectDao: ProjectDao,
            multimediaProjectDao: MultimediaProjectDao
        ) {
            val echoComicsFolderId = UUID.randomUUID().toString()
            val myProjectsFolderId = UUID.randomUUID().toString()
            val fieldNotesFolderId = UUID.randomUUID().toString()

            val initialItems = listOf(
                WorkspaceItem(
                    id = echoComicsFolderId,
                    parentId = null,
                    kind = "folder",
                    name = "Echo Comics",
                    tags = "comics, visual, archive"
                ),
                WorkspaceItem(
                    id = myProjectsFolderId,
                    parentId = null,
                    kind = "folder",
                    name = "My Projects",
                    tags = "projects, active"
                ),
                WorkspaceItem(
                    id = fieldNotesFolderId,
                    parentId = null,
                    kind = "folder",
                    name = "Field Docs & Surveys",
                    tags = "nodes, echo-interface, telemetry"
                ),
                WorkspaceItem(
                    id = UUID.randomUUID().toString(),
                    parentId = echoComicsFolderId,
                    kind = "note",
                    name = "Issue 1 — The First Frequency",
                    text = """# ECHO COMICS: Issue 1 — The First Frequency
Setting: Portsmouth Harbour, Hilsea bastion, 1986.
Characters: David & Jean (Acoustic Researchers).

Page 1 (Splash):
Rain beating against reinforced subterranean bunker glass. An oscilloscope trace stabilizes into a perfect standing sinusoidal tone: 42.598 Hz.

David: "It's not ambient industrial noise, Jean. Look at the phase alignment across the Solent chalk layer."
Jean: "That's bedrock resonance. The entire island is humming."
""",
                    tags = "comic, script, issue1",
                    isPinned = true
                ),
                WorkspaceItem(
                    id = UUID.randomUUID().toString(),
                    parentId = myProjectsFolderId,
                    kind = "note",
                    name = "42 Geniuses Narrative Outline",
                    text = """The chronicle of 42 historical and contemporary observers who tuned into the Earth's low-frequency acoustic waveguide.

From the megalithic sarsen stones of Stonehenge (~2500 BCE) to Lord Rayleigh's mathematical formulation of surface waves in 1887, Nikola Tesla's 43 Hz terrestrial resonance note in 1899, and the modern 11-node ECHO-INTERFACE commissioning in 2026.

Key Themes:
1. Resonant harmonic ratios (Base: 42.6 Hz).
2. The 11.74-second tidal envelope cycle.
3. Network coherence during tectonic shifts.
""",
                    tags = "novel, lore, canon"
                ),
                WorkspaceItem(
                    id = UUID.randomUUID().toString(),
                    parentId = fieldNotesFolderId,
                    kind = "note",
                    name = "N1 Portsmouth Commissioning Telemetry",
                    text = """Site: Fort Widley / Portsdown Hill chalk alignment.
Transmitter: Piezo-acoustic array coupled to lower chalk strata.
Carrier Frequency: 85.200 Hz (Master 2x Harmonic).
Status: LOCKED (All 10 slave nodes synchronized).
Daily Energy Budget: 21.9 Wh.
Battery Buffer: 24V 15Ah LiFePO4.
Autonomy Margin: 16.4 days winter baseline.
""",
                    tags = "n1, telemetry, master",
                    isPinned = true
                )
            )
            dao.insertItems(initialItems)

            // Seed initial sample frequency resonance history entries
            val now = System.currentTimeMillis()
            val sampleHistory = listOf(
                FrequencyHistoryEntry(
                    frequencyHz = 42.600,
                    timestamp = now - (1000 * 60 * 12),
                    durationSeconds = 180,
                    isResonanceLock = true,
                    sessionNote = "Fundamental 42.6 Hz Bedrock lock. Max standing wave resonance."
                ),
                FrequencyHistoryEntry(
                    frequencyHz = 42.598,
                    timestamp = now - (1000 * 60 * 45),
                    durationSeconds = 95,
                    isResonanceLock = true,
                    sessionNote = "Hilsea 1986 chalk alignment verification session."
                ),
                FrequencyHistoryEntry(
                    frequencyHz = 42.000,
                    timestamp = now - (1000 * 60 * 120),
                    durationSeconds = 60,
                    isResonanceLock = false,
                    sessionNote = "Sub-band sweep baseline calibration."
                ),
                FrequencyHistoryEntry(
                    frequencyHz = 43.000,
                    timestamp = now - (1000 * 60 * 240),
                    durationSeconds = 110,
                    isResonanceLock = false,
                    sessionNote = "Tesla 1899 Colorado terrestrial resonance comparative note."
                )
            )
            sampleHistory.forEach { historyDao.insertEntry(it) }

            // Seed default frequency presets
            if (presetDao.getCount() == 0) {
                presetDao.insertPresets(FrequencyPresetRepository.DEFAULT_PRESETS)
            }

            // Seed default user projects and workspaces
            if (projectDao.getProjectCount() == 0) {
                val defaultProjects = listOf(
                    ProjectEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Acoustic Resonance Lab (42.6 Hz)",
                        description = "Circular Chladni eigenmodes, nodal ring physics, and bedrock standing wave experiments.",
                        createdAt = now - (1000L * 60 * 60 * 24 * 7),
                        colorTagHex = "#00F0FF",
                        orderIndex = 0
                    ),
                    ProjectEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Hilsea 1986 Chalk Strata Survey",
                        description = "Subterranean acoustic waveguide telemetry archive and field notes across the Solent chalk layer.",
                        createdAt = now - (1000L * 60 * 60 * 24 * 3),
                        colorTagHex = "#FFB347",
                        orderIndex = 1
                    ),
                    ProjectEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Living Tapestry Creative Engine",
                        description = "The Four Guardians — The Visionary, Weaver, Kindle, and Echo — awakening across the light.",
                        createdAt = now - (1000L * 60 * 60 * 12),
                        colorTagHex = "#9370DB",
                        orderIndex = 2
                    )
                )
                projectDao.insertProjects(defaultProjects)
            }

            // Seed initial multimedia projects
            if (multimediaProjectDao.getProjectCount() == 0) {
                val defaultMediaProjects = listOf(
                    MultimediaProjectEntity(
                        id = UUID.randomUUID().toString(),
                        title = "ATHENA Loom-Weave Launch Asset",
                        description = "16:9 cinematic launch asset: golden shuttle stitching the ATHENA logomark across dark obsidian horizon.",
                        mediaType = "VIDEO",
                        filePath = "/storage/emulated/0/Movies/ATHENA/athena_loom_16x9.mp4",
                        fileSizeBytes = 48_234_496L,
                        durationSeconds = 12.0f,
                        mimeType = "video/mp4",
                        resolution = "1920x1080",
                        aspectRatio = "16:9",
                        createdAt = now - (1000L * 60 * 60 * 2),
                        isFavorite = true,
                        tags = "video, 16:9, loom-weave, launch, 42hz"
                    ),
                    MultimediaProjectEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Living Tapestry Teaser (Four Guardians)",
                        description = "9:16 vertical teaser featuring The Visionary, Weaver, Kindle, and Echo converging into full pullback.",
                        mediaType = "VIDEO",
                        filePath = "/storage/emulated/0/Movies/ATHENA/living_tapestry_9x16.mp4",
                        fileSizeBytes = 72_412_890L,
                        durationSeconds = 38.0f,
                        mimeType = "video/mp4",
                        resolution = "1080x1920",
                        aspectRatio = "9:16",
                        createdAt = now - (1000L * 60 * 60 * 24),
                        isFavorite = true,
                        tags = "video, 9:16, living-tapestry, youtube-short"
                    ),
                    MultimediaProjectEntity(
                        id = UUID.randomUUID().toString(),
                        title = "42.6 Hz Bedrock Standing Wave Master",
                        description = "Direct 44.1 kHz 16-bit PCM master acoustic recording of Portsmouth chalk layer resonance.",
                        mediaType = "AUDIO",
                        filePath = "/storage/emulated/0/Music/ATHENA/bedrock_standing_wave_42_6hz.wav",
                        fileSizeBytes = 18_920_000L,
                        durationSeconds = 180.0f,
                        mimeType = "audio/wav",
                        resolution = "44.1kHz / 16-bit PCM",
                        aspectRatio = "1:1",
                        createdAt = now - (1000L * 60 * 60 * 48),
                        isFavorite = false,
                        tags = "audio, wav, 42.6hz, bedrock, cymatics"
                    ),
                    MultimediaProjectEntity(
                        id = UUID.randomUUID().toString(),
                        title = "Hilsea 1986 Chladni Plate Mode (m=4, n=2)",
                        description = "Ultra high-resolution nodal dispersion diagram captured from resonant brass acoustic plate.",
                        mediaType = "IMAGE",
                        filePath = "/storage/emulated/0/Pictures/ATHENA/chladni_m4_n2_hilsea.png",
                        fileSizeBytes = 6_410_200L,
                        durationSeconds = 0.0f,
                        mimeType = "image/png",
                        resolution = "2048x2048",
                        aspectRatio = "1:1",
                        createdAt = now - (1000L * 60 * 60 * 72),
                        isFavorite = false,
                        tags = "image, png, chladni, nodal-geometry"
                    )
                )
                multimediaProjectDao.insertProjects(defaultMediaProjects)
            }
        }
    }
}
