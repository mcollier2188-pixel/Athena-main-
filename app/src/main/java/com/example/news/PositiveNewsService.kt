package com.example.news

import android.content.Context
import com.example.ai.CreativeHubAiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Positive news article model capturing uplifting tech, comedy, entertainment, space, and culture stories.
 */
data class PositiveArticle(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val category: PositiveCategory,
    val summary: String,
    val fullContent: String,
    val whyItMatters: String,
    val upliftScore: Int, // e.g. 98%
    val readTimeMinutes: Int,
    val timestamp: String,
    val sourceName: String,
    val emoji: String,
    val tags: List<String>
)

enum class PositiveCategory(val displayName: String, val icon: String, val badgeColorHex: Long) {
    ALL("All Horizons", "✨", 0xFF00E5FF),
    TECH_ENGINEERING("Tech & Engineering", "⚡", 0xFF00E5FF),
    COMEDY_ENTERTAINMENT("Comedy & Entertainment", "🎭", 0xFFFFD700),
    SPACE_SCIENCE("Space & Science", "🚀", 0xFF7C4DFF),
    CREATIVE_CULTURE("Creative & Animation", "🎨", 0xFFFF4081)
}

/**
 * Service providing curated positive news stories and live Gemini expansion.
 */
object PositiveNewsService {

    val curatedArticles: List<PositiveArticle> = listOf(
        PositiveArticle(
            id = "tech-fusion-breakthrough",
            title = "Next-Gen Fusion Reactor Achieves Sustained Net Energy Gain in Milestone Demonstration",
            category = PositiveCategory.TECH_ENGINEERING,
            summary = "Engineers at the international magnetic confinement consortium have sustained stable plasma confinement for over 22 minutes, marking an unprecedented leap toward abundant clean baseload energy.",
            fullContent = """
                In a monumental triumph for clean energy, the high-temperature superconducting tokamak consortium confirmed today that their sustained plasma chamber maintained Q-factor net positive energy output for twenty-two continuous minutes.
                
                Utilizing high-temperature superconductor (HTS) tape magnets operating at liquid nitrogen temperatures, the team contained 150-million-degree Celsius ionized hydrogen without standard magnet degradation.
                
                The project leads emphasized that this demonstration proves high-field compact magnetic fusion is economically and practically viable within this decade, with zero long-lived radioactive waste and inexhaustible seawater-derived fuel.
            """.trimIndent(),
            whyItMatters = "Offers a clear, non-carbon alternative capable of powering entire cities with zero grid intermittency or fossil fuel dependence.",
            upliftScore = 99,
            readTimeMinutes = 3,
            timestamp = "Today • 2 hours ago",
            sourceName = "Global Clean Energy Vanguard",
            emoji = "⚡",
            tags = listOf("Clean Energy", "Fusion", "Superconductors", "Milestone")
        ),
        PositiveArticle(
            id = "tech-solar-perovskite",
            title = "Tandem Perovskite Solar Cells Top 34% Efficiency Record with Recyclable Glass Encasement",
            category = PositiveCategory.TECH_ENGINEERING,
            summary = "University researchers have developed self-healing tandem perovskite-silicon solar cells that achieve over 34% commercial sunlight conversion while remaining 100% recyclable.",
            fullContent = """
                A multinational research coalition has shattered the commercial solar efficiency ceiling. By pairing silicon sub-layers with organic-inorganic halide perovskites, the new cell captures both high-energy blue photons and low-energy infrared rays simultaneously.
                
                Crucially, the novel molecular binding agent neutralizes historical moisture degradation, offering a 30-year operational lifespan comparable to traditional silicon panels at a fraction of manufacturing energy cost.
            """.trimIndent(),
            whyItMatters = "Cuts the required rooftop and land footprint for solar installations by nearly half, radically accelerating decentralized green grid adoption.",
            upliftScore = 96,
            readTimeMinutes = 2,
            timestamp = "Today • 4 hours ago",
            sourceName = "Applied Photonics Journal",
            emoji = "☀️",
            tags = listOf("Perovskite", "Solar Power", "Efficiency", "Engineering")
        ),
        PositiveArticle(
            id = "comedy-cozy-gaming-festival",
            title = "Beloved Cozy Game 'Meadow Harmony' Surpasses 10 Million Players, Donates All Soundtrack Royalties to Wildlife Sanctuaries",
            category = PositiveCategory.COMEDY_ENTERTAINMENT,
            summary = "The wholesome community game made by a tiny 3-person indie studio has captured global hearts with peaceful farming, gentle dialogue, and real-world conservation impact.",
            fullContent = """
                In an era dominated by high-stress competitive games, 'Meadow Harmony'—an indie gem focused entirely on gardening with woodland creatures and solving whimsical neighbor puzzles—has officially surpassed 10 million active players.
                
                The developers announced this morning that 100% of the game's acoustic soundtrack album proceeds (over \$3.8 million to date) are being permanently endowed to local rescue wildlife sanctuaries and national park preservation initiatives.
                
                Players worldwide celebrated with an in-game digital lantern parade, praising the title for its heartwarming humor, zero microtransactions, and comforting charm.
            """.trimIndent(),
            whyItMatters = "Proves that kindness, gentle storytelling, and low-stress gaming resonate deeply with global audiences while creating real-world tangible good.",
            upliftScore = 98,
            readTimeMinutes = 2,
            timestamp = "Today • 5 hours ago",
            sourceName = "Indie Play Chronicle",
            emoji = "🎮",
            tags = listOf("Cozy Gaming", "Indie Dev", "Wholesome", "Conservation")
        ),
        PositiveArticle(
            id = "comedy-improv-hospital-tour",
            title = "World-Famous Comedy Troupe Launches Nationwide 'Laughter Prescription' Tour in Children's Hospitals",
            category = PositiveCategory.COMEDY_ENTERTAINMENT,
            summary = "Top improvisers and voice actors bring customized interactive puppet shows and spontaneous sketch comedy directly to pediatric wards, brightening thousands of recovery journeys.",
            fullContent = """
                Renowned comedic performers from top animated series and sketch comedy programs have kicked off the 'Laughter Prescription' tour across forty pediatric healthcare centers.
                
                Equipped with interactive puppet avatars and participatory voice improvisation, the troupe invents spontaneous superhero origin stories starring the young patients themselves as brave captains and wizards.
                
                Clinical staff report measurable drops in patient pre-operative anxiety and elevated mood biomarkers, proving the timeless restorative power of shared laughter.
            """.trimIndent(),
            whyItMatters = "Reminds us that entertainment and humor are powerful medicine, transforming sterile hospital rooms into arenas of joy and wonder.",
            upliftScore = 97,
            readTimeMinutes = 3,
            timestamp = "Yesterday",
            sourceName = "Heartland Arts Review",
            emoji = "🎭",
            tags = listOf("Comedy", "Pediatric Care", "Joy", "Community")
        ),
        PositiveArticle(
            id = "space-james-webb-biosignatures",
            title = "James Webb Space Telescope Unveils Water Vapor & Atmospheric Stability on Habitable-Zone Exoplanet",
            category = PositiveCategory.SPACE_SCIENCE,
            summary = "Spectroscopic analysis of K2-18b and TRAPPIST-1e confirms temperate atmospheres with balanced carbon cycles and rich water clouds, offering our closest look yet at serene extrasolar worlds.",
            fullContent = """
                Astronomers analyzing transmission spectra captured by the James Webb Space Telescope's NIRSpec and MIRI instruments announced pristine detection of water vapor, methane equilibrium, and absence of harsh toxic sulfur plumes on candidate temperate exoplanets.
                
                The planetary system exhibits orbit stability spanning several billion years, providing calm, sustained environments that mirror early Earth conditions.
                
                The telescope's ultra-sensitive infrared coronagraphs will next map cloud cover dynamics across planetary day-night terminators in an unprecedented observational campaign.
            """.trimIndent(),
            whyItMatters = "Brings humanity closer to answering our oldest existential question: whether life and habitable sanctuary exist beyond the boundaries of our solar cradle.",
            upliftScore = 99,
            readTimeMinutes = 4,
            timestamp = "Today • 1 hour ago",
            sourceName = "Astrophysics Deep Sky Dispatch",
            emoji = "🚀",
            tags = listOf("JWST", "Exoplanets", "Astronomy", "Deep Space")
        ),
        PositiveArticle(
            id = "space-lunar-botany",
            title = "Lunar Greenhouse Experiment Sprouts Flowering Micro-Crops Using Simulated Moon Regolith & Recycled Moisture",
            category = PositiveCategory.SPACE_SCIENCE,
            summary = "Agronomists celebrate the first fully self-contained closed-loop flowering cycle of nutrient-dense greens in simulated lunar soil, unlocking sustained extraterrestrial exploration.",
            fullContent = """
                In a breakthrough for deep space settlement, bio-engineers have successfully cultivated multi-generation dwarf tomatoes, leafy greens, and edible clover utilizing simulated lunar basalt regolith enriched with symbiotic mycorrhizal fungi.
                
                The closed-loop chamber recycled 99.4% of moisture while producing crisp, vitamin-rich harvests on a 28-day accelerated cycle.
                
                The technology is already being adapted on Earth to regenerate degraded soil in drought-affected agricultural communities.
            """.trimIndent(),
            whyItMatters = "Solves space nutrition while simultaneously delivering water-efficient soil restoration breakthroughs for terrestrial drylands.",
            upliftScore = 95,
            readTimeMinutes = 3,
            timestamp = "2 days ago",
            sourceName = "BioSpace Agronomy",
            emoji = "🌱",
            tags = listOf("Lunar Botany", "Closed Loop", "Regolith", "Food Security")
        ),
        PositiveArticle(
            id = "culture-indie-animation-short",
            title = "Hand-Painted Watercolor Animated Short Wins Global Festival Grand Prix with Universally Uplifting Father-Daughter Tale",
            category = PositiveCategory.CREATIVE_CULTURE,
            summary = "Created over five years using traditional gouache frames and paper-cut stop motion, 'The Lighthouse Lantern' mesmerizes audiences with its message of resilience and unconditional warmth.",
            fullContent = """
                An independent animation team working from a seaside cabin in Brittany has claimed the highest honors at the Annecy International Animation Festival for 'The Lighthouse Lantern'.
                
                Consisting of more than 14,000 individually hand-painted gouache paper frames, the short film follows an aging lighthouse keeper and his inventor daughter who construct a beacon powered by bioluminescent coastal plankton to guide migrating birds safely around storm cliffs.
                
                The film received an unprecedented 12-minute standing ovation, with festival jurors citing its radiant color palette, zero cynicism, and celebration of family inventiveness.
            """.trimIndent(),
            whyItMatters = "Celebrates painstaking human craftsmanship, artistic soul, and gentle storytelling in an age of automated formulaic entertainment.",
            upliftScore = 98,
            readTimeMinutes = 3,
            timestamp = "Today • 6 hours ago",
            sourceName = "CineAnimation Quarterly",
            emoji = "🎨",
            tags = listOf("Animation", "Watercolor", "Storytelling", "Film Festival")
        ),
        PositiveArticle(
            id = "culture-open-source-orchestra",
            title = "Musicians in 84 Countries Collaborate in Open-Source Global Symphony, Releasing Free High-Res Audio Stems for Creators",
            category = PositiveCategory.CREATIVE_CULTURE,
            summary = "Over 1,200 traditional folk and orchestral instrumentalists recorded a breathtaking 45-minute harmonic suite, granting universal creative commons rights for independent filmmakers.",
            fullContent = """
                The 'Earth Harmonic Symphony'—a grassroots open-source music collective—has released their magnum opus: a sweeping multi-movement symphony recorded by musicians spanning every continent, from Mongolian morin khuur players to Andean pan-flutists and European cellists.
                
                The entire 24-bit 96kHz multitrack recording session has been published under a permissive Creative Commons Zero license, giving indie game developers, student animators, and short film creators free access to cinematic orchestra stems without legal barriers.
            """.trimIndent(),
            whyItMatters = "Democratizes world-class cinematic music for independent storytellers everywhere while uniting global artists in pure collaborative harmony.",
            upliftScore = 96,
            readTimeMinutes = 3,
            timestamp = "3 days ago",
            sourceName = "Acoustic Commons",
            emoji = "🎻",
            tags = listOf("Open Source", "Music", "Symphony", "Global Collaboration")
        )
    )

    /**
     * Expands an article with Gemini 2.5 Flash live deep dive, explaining the positive ripple effect and technical breakthroughs.
     */
    suspend fun expandArticleWithGemini(
        context: Context,
        article: PositiveArticle
    ): String = withContext(Dispatchers.IO) {
        val prompt = """
            You are ATHENA's Optimistic Horizon News Correspondent.
            
            Write an uplifting, rigorous, deeply inspiring deep-dive analysis on this breakthrough news story:
            Headline: "${article.title}"
            Category: ${article.category.displayName}
            Summary: ${article.summary}
            
            Please provide:
            1. 🌟 The Human Context & Why This Matters Today
            2. 🔬 The Technical / Creative Breakthrough (explain how it works simply and beautifully)
            3. 🚀 The Optimistic 5-Year Horizon (what positive ripple effects will this unlock for society)
            4. 💡 One inspiring takeaway quote or thought for the reader's day
            
            Keep the tone intelligent, joyful, and grounded in real human achievement—free of cynical doomism.
        """.trimIndent()

        val creativeMsg = CreativeHubAiService.askAnything(
            context = context,
            prompt = prompt,
            model = "gemini-3.5-flash",
            creativeTone = "Radiant, scientifically rigorous, deeply uplifting technology and arts journalism"
        )

        creativeMsg.text
    }
}
