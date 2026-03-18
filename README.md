# WebSerialRead

An Android app for reading free web serials from WordPress-based sites in one place, with offline support.

## Features

- **Library** — Grid view of all your added serials with unread chapter counts
- **Browse / Discover** — Curated list of popular web serials you can add with one tap
- **Offline reading** — Download chapters to read without an internet connection
- **Background downloads** — Downloads continue while you navigate the app
- **Reader** — Clean in-app WebView reader with customisable settings
- **Reader settings** — Adjust text size, font (Serif / Sans-serif / Monospace), and background colour (White / Sepia / Dark / Black)
- **Updates tab** — See which serials have unread chapters at a glance
- **History tab** — Recently read chapters across all serials
- **Sync / Refresh** — Pull new chapters from the source site

## Supported Sites

Works with any self-hosted or WordPress.com web serial. Includes built-in support for:

| Serial | Author | Genre |
|---|---|---|
| Pale | Wildbow | Urban Fantasy |
| Worm | Wildbow | Superhero |
| Ward | Wildbow | Superhero |
| Pact | Wildbow | Urban Fantasy |
| Twig | Wildbow | Biopunk |
| Katalepsis | Hungry Ghost | Horror / Fantasy |
| Pale Lights | ErraticErrata | Fantasy |
| A Practical Guide to Evil | ErraticErrata | Fantasy |
| Heretical Edge | Cerulean | Urban Fantasy |
| Summus Proelium | Cerulean | Superhero |
| The Wandering Inn | pirateaba | LitRPG / Fantasy |
| Void Domain | TowerCurator | Urban Fantasy |
| The Iron Teeth | ClearMadness | Dark Fantasy |

You can also add any WordPress serial manually by entering the site URL, with an optional Table of Contents URL for sites that block the WordPress REST API.

## How It Works

1. The app first tries the **WordPress REST API** (`/wp-json/wp/v2/posts`) to fetch chapters
2. If the API is blocked or unavailable, it falls back to **HTML scraping** (OkHttp + Jsoup) of the Table of Contents page
3. If scraping is also blocked (e.g. Cloudflare protection), it uses an **Android WebView** — a real Chromium browser — which handles JS challenges natively

## Tech Stack

- **UI** — Jetpack Compose + Material 3
- **Architecture** — MVVM (AndroidViewModel, no DI framework)
- **Database** — Room (SQLite) with KSP annotation processing
- **Networking** — Retrofit + OkHttp for the WordPress REST API
- **Scraping** — Jsoup for HTML parsing, Android WebView for JS-rendered pages
- **Persistence** — DataStore Preferences for reader settings
- **Navigation** — Jetpack Navigation Compose

## Building

1. Clone the repo
2. Open in Android Studio (Hedgehog or newer)
3. Sync Gradle and run on a device or emulator running Android 8.0+

## Permissions

- `INTERNET` — fetching chapters and cover images
