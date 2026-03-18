package com.example.webserialread.data.remote

data class KnownSerial(
    val title: String,
    val author: String,
    val genre: String,
    val description: String,
    val siteUrl: String,
    val tocUrl: String? = null,
    /** If set, only WP posts tagged with this category slug are imported (for multi-series blogs). */
    val wpCategorySlug: String? = null
)

val KNOWN_SERIALS = listOf(
    KnownSerial(
        title = "Pale",
        author = "Wildbow",
        genre = "Urban Fantasy",
        description = "Three young practitioners navigate a dangerous magical world in a small Canadian town.",
        siteUrl = "https://palewebserial.wordpress.com"
    ),
    KnownSerial(
        title = "Pale Lights",
        author = "ErraticErrata",
        genre = "Fantasy",
        description = "A story of scheming, survival and secrets in a world of dying gods and living cities.",
        siteUrl = "https://palelights.com",
        tocUrl = "https://palelights.com/table-of-contents/"
    ),
    KnownSerial(
        title = "Katalepsis",
        author = "Hungry Ghost",
        genre = "Horror / Fantasy",
        description = "A young woman with a fractured mind investigates the eldritch horrors lurking beneath reality.",
        siteUrl = "https://katalepsis.net",
        tocUrl = "https://katalepsis.net/table-of-contents/"
    ),
    KnownSerial(
        title = "Worm",
        author = "Wildbow",
        genre = "Superhero",
        description = "A teenage girl who can control bugs becomes a superhero in a dark, gritty world.",
        siteUrl = "https://parahumans.wordpress.com"
    ),
    KnownSerial(
        title = "Ward",
        author = "Wildbow",
        genre = "Superhero",
        description = "The sequel to Worm. The world rebuilds after a catastrophic event, but old wounds run deep.",
        siteUrl = "https://www.parahumans.net",
        tocUrl = "https://www.parahumans.net/table-of-contents/"
    ),
    KnownSerial(
        title = "Pact",
        author = "Wildbow",
        genre = "Urban Fantasy",
        description = "A disinherited young man is drawn into a brutal world of practitioners and demons.",
        siteUrl = "https://pactwebserial.wordpress.com"
    ),
    KnownSerial(
        title = "Twig",
        author = "Wildbow",
        genre = "Biopunk",
        description = "A group of enhanced children serve as agents in a world where biology has replaced technology.",
        siteUrl = "https://twigserial.wordpress.com"
    ),
    KnownSerial(
        title = "A Practical Guide to Evil",
        author = "ErraticErrata",
        genre = "Fantasy",
        description = "A young girl chooses to apprentice under the evil Squire, aiming to reform villainy from within.",
        siteUrl = "https://practicalguidetoevil.wordpress.com"
    ),
    KnownSerial(
        title = "The Wandering Inn",
        author = "pirateaba",
        genre = "LitRPG / Fantasy",
        description = "A young woman falls into a fantasy world and opens an inn, meeting monsters, adventurers and more.",
        siteUrl = "https://wanderinginn.com",
        tocUrl = "https://wanderinginn.com/table-of-contents/"
    ),
    KnownSerial(
        title = "Heretical Edge",
        author = "Cerulean",
        genre = "Urban Fantasy",
        description = "A girl discovers her school is training the next generation of monster hunters.",
        siteUrl = "https://ceruleanscrawling.wordpress.com",
        wpCategorySlug = "heretical-edge"
    ),
    KnownSerial(
        title = "Summus Proelium",
        author = "Cerulean",
        genre = "Superhero",
        description = "A teenager discovers her parents are supervillains and secretly becomes a hero.",
        siteUrl = "https://ceruleanscrawling.wordpress.com",
        wpCategorySlug = "summus-proelium"
    ),
    KnownSerial(
        title = "Void Domain",
        author = "TowerCurator",
        genre = "Urban Fantasy",
        description = "A girl moves to a new city and discovers magic, demons, and the school that teaches students to fight them.",
        siteUrl = "https://www.towercurator.com",
        tocUrl = "https://www.towercurator.com/story/void-domain/"
    ),
    KnownSerial(
        title = "The Iron Teeth",
        author = "ClearMadness",
        genre = "Dark Fantasy",
        description = "A goblin runt struggles to survive and grow strong in a brutal, monster-filled world.",
        siteUrl = "https://theironteeth.com",
        tocUrl = "https://theironteeth.com/table-of-contents/"
    ),
)
