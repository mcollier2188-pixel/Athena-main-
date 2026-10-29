package com.example.ai

enum class PromptCategory(val displayName: String, val badge: String) {
    FILMMAKING("Cinema & Video", "🎬"),
    ACOUSTIC_MUSIC("Sound & Music", "🎵"),
    CREATIVE_WRITING("Fiction & Poetry", "✍️"),
    DESIGN_AESTHETICS("Art & Visuals", "🎨"),
    INVENTIONS_TECH("Inventions & Tech", "💡"),
    COSMIC_PHILOSOPHY("Cosmos & Philosophy", "🌌")
}

data class CreativePromptTemplate(
    val id: String,
    val category: PromptCategory,
    val title: String,
    val tagline: String,
    val detailedPrompt: String,
    val suggestedTone: String = "Vivid, Atmospheric, Precise"
)

object PromptTemplateLibrary {

    val templates: List<CreativePromptTemplate> = listOf(
        // 1. FILMMAKING & CINEMA
        CreativePromptTemplate(
            id = "film_hook",
            category = PromptCategory.FILMMAKING,
            title = "Cinematic Opening Hook & Scene",
            tagline = "High-tension visual opening with sensory audio cues",
            detailedPrompt = "Write a striking, atmospheric 2-page opening scene for a cinematic film. Establish tension purely through sensory sound cues (low hums, glass vibrations, wind resonance) and vivid visual cinematography without relying on heavy dialogue.",
            suggestedTone = "Cinematic Neo-Noir"
        ),
        CreativePromptTemplate(
            id = "film_essay_outline",
            category = PromptCategory.FILMMAKING,
            title = "YouTube Video Essay Blueprint",
            tagline = "Irresistible thesis, 3-beat rhythm, and B-roll shotlist",
            detailedPrompt = "Create a master script outline for an engaging 12-minute video essay analyzing why certain frequencies and sound design manipulate human emotional recall in film. Include an irresistible opening hook, 3 distinct thematic chapters, and specific visual B-roll directions.",
            suggestedTone = "Intellectual & Compelling"
        ),
        CreativePromptTemplate(
            id = "film_moodboard",
            category = PromptCategory.FILMMAKING,
            title = "Cinematic Color Palette & Moodboard",
            tagline = "Color grading, lighting ratios, and texture guide",
            detailedPrompt = "Design a visual director's lookbook for a speculative near-future thriller set along coastal industrial landscapes. Detail the exact 5-color hexadecimal palette, key lighting ratios (hard vs diffuse), lenses, and camera movement philosophy.",
            suggestedTone = "Visually Expressive"
        ),

        // 2. SOUND & MUSIC SYNTHESIS
        CreativePromptTemplate(
            id = "music_harmonic_soundscape",
            category = PromptCategory.ACOUSTIC_MUSIC,
            title = "42.6 Hz Bedrock Soundscape Blueprint",
            tagline = "Modular patch design & harmonic sub-carrier concept",
            detailedPrompt = "Draft an acoustic sound design blueprint for an ambient meditation suite centered around a 42.600 Hz sub-carrier. Specify complementary harmonic overtone ratios (85.2 Hz, 127.8 Hz, 213 Hz), slow LFO filter sweeps, and stereo spatial panning parameters.",
            suggestedTone = "Acoustic Engineering"
        ),
        CreativePromptTemplate(
            id = "music_lyrics_ballad",
            category = PromptCategory.ACOUSTIC_MUSIC,
            title = "Acoustic Folk Song Lyrics",
            tagline = "Poetic lyrics of memory, open roads, and Solent tides",
            detailedPrompt = "Compose deeply evocative song lyrics for an acoustic guitar ballad titled 'Signals on the Water'. Structure it with 3 verses, a soaring chorus, and an intimate bridge exploring memory, coastal radio beacons, and the passage of time.",
            suggestedTone = "Melancholic & Hopeful"
        ),
        CreativePromptTemplate(
            id = "music_solfeggio_intent",
            category = PromptCategory.ACOUSTIC_MUSIC,
            title = "Solfeggio Resonance Suite",
            tagline = "528 Hz transformation & 432 Hz natural frequency design",
            detailedPrompt = "Structure a guided sonic journey combining 432 Hz organic warmth with 528 Hz Solfeggio bell chimes. Describe the tempo changes, binaural beat delta-wave offsets for relaxation, and emotional arc across a 15-minute sound therapy session.",
            suggestedTone = "Calming & Meditative"
        ),

        // 3. FICTION & CREATIVE WRITING
        CreativePromptTemplate(
            id = "writing_scifi_nomad",
            category = PromptCategory.CREATIVE_WRITING,
            title = "Deep Space Nomad Flash Fiction",
            tagline = "A radio operator catching prehistoric galactic echoes",
            detailedPrompt = "Write an evocative sci-fi short story (approx. 500 words) about an isolated signal operator stationed on a moon outpost who decodes an acoustic frequency broadcast that predates the known solar system.",
            suggestedTone = "Literary Science Fiction"
        ),
        CreativePromptTemplate(
            id = "writing_poetry_resonance",
            category = PromptCategory.CREATIVE_WRITING,
            title = "Contemporary Free-Verse Poem",
            tagline = "Echoes, hardware oscillators, and quiet coastlines",
            detailedPrompt = "Write an original 24-line free-verse poem exploring human consciousness through the imagery of copper audio cables, ocean tides, low-frequency oscillators, and late-night contemplation.",
            suggestedTone = "Lyrical & Introspective"
        ),
        CreativePromptTemplate(
            id = "writing_worldbuilding_rules",
            category = PromptCategory.CREATIVE_WRITING,
            title = "Acoustic Worldbuilding System",
            tagline = "A world where frequency determines physical matter",
            detailedPrompt = "Invent a detailed fantasy worldbuilding system where architects and travelers shape physical bedrock using precise acoustic vocal frequencies. Outline 4 foundational harmonic laws, the societal castes of 'Tuners', and the dangerous consequences of dissonant chords.",
            suggestedTone = "Immersive & Structured"
        ),

        // 4. ART & VISUAL DESIGN
        CreativePromptTemplate(
            id = "design_cyber_interface",
            category = PromptCategory.DESIGN_AESTHETICS,
            title = "Cyber-Organic Mobile Interface",
            tagline = "Tactile dials, dark glass, and cyan luminescence",
            detailedPrompt = "Detail the UI/UX design language for a cyber-organic scientific mobile app: describe the typography pairing, micro-interactions for frequency knobs, glowing phosphor oscilloscope displays, and ergonomic haptic vibration cues.",
            suggestedTone = "Modern UI Architecture"
        ),
        CreativePromptTemplate(
            id = "design_brutalist_poster",
            category = PromptCategory.DESIGN_AESTHETICS,
            title = "Neo-Brutalist Poster Art Concept",
            tagline = "Bold Swiss typography, asymmetric grid, and stark contrast",
            detailedPrompt = "Describe a striking poster composition celebrating 'ATHENA 42.6 Hz Harmonic Acoustic Engine'. Detail the asymmetric grid layout, massive monospaced type hierarchy, high-contrast cyan on obsidian contrast, and geometric soundwave vectors.",
            suggestedTone = "Bold & Minimalist"
        ),

        // 5. INVENTIONS & TECH INNOVATION
        CreativePromptTemplate(
            id = "tech_innovations_5apps",
            category = PromptCategory.INVENTIONS_TECH,
            title = "5 Disruptive Creative App Concepts",
            tagline = "High-utility, delightful tools for makers & sound designers",
            detailedPrompt = "Brainstorm 5 innovative, delightfully unexpected mobile app concepts that merge real-time audio DSP, creative generative AI, and spatial awareness. For each, describe the core user hook, why existing apps fail at it, and its viral utility.",
            suggestedTone = "Visionary & Practical"
        ),
        CreativePromptTemplate(
            id = "tech_resilient_hub",
            category = PromptCategory.INVENTIONS_TECH,
            title = "Offline-First Knowledge Engine Architecture",
            tagline = "Local SQLite, zero-cloud dependency, and local AI",
            detailedPrompt = "Outline the system architecture for an ultra-resilient, offline-first personal creative vault capable of operating without internet connectivity using on-device Room SQLite caching and periodic differential cloud syncing.",
            suggestedTone = "Engineering Rigor"
        ),

        // 6. COSMOS & PHILOSOPHY
        CreativePromptTemplate(
            id = "phil_superposition",
            category = PromptCategory.COSMIC_PHILOSOPHY,
            title = "Quantum Superposition Metaphor",
            tagline = "Artistic dialogue on possibilities and observer reality",
            detailedPrompt = "Explain quantum superposition and wave-particle duality through an intuitive, poetic philosophical dialogue between an acoustic composer and a quantum physicist walking along a dark pebble beach.",
            suggestedTone = "Profound & Accessible"
        ),
        CreativePromptTemplate(
            id = "phil_tides_memory",
            category = PromptCategory.COSMIC_PHILOSOPHY,
            title = "The Epistemology of Echoes & Memory",
            tagline = "Why humans seek eternal resonance in fleeting moments",
            detailedPrompt = "Write an essay examining the human obsession with recording, archiving, and resonating with the past. Draw connections between acoustic echoes, digital journaling, geological strata, and the search for enduring meaning.",
            suggestedTone = "Philosophical & Reflective"
        )
    )

    fun getByCategory(category: PromptCategory): List<CreativePromptTemplate> {
        return templates.filter { it.category == category }
    }
}
