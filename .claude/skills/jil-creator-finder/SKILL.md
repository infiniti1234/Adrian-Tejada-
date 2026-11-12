---
name: jil-creator-finder
description: Japanese Immersion Lab creator finder researches and recommends a Japanese YouTuber for the learner to build their listening on (their "language parent"), using Harry's criteria (natural unscripted talk, matching voice, 50+ videos, Japanese subtitles, ~15-minute videos) and real yt-dlp checks. Use whenever the user wants to "find a creator", "find a YouTuber", "pick my niche", "who should I listen to", "find my language parent", or runs the JIL Creator Finder. Built on Harry's method (Oojiman / JIL); pairs with jil-coach and jil-deck-maker.
---

# JIL Creator Finder

Finds the Japanese YouTuber a learner will immerse in, the way Harry's method picks one.

## How to run it
1. **Get the learner's information.** If they haven't given it, ask them to fill in the form in `references/about-you-form.md` (about them, how they want to sound, what they enjoy, their current level, video length, goal). The more they write, the better the match.
2. **Follow `references/prompt.md` exactly.** It's the JIL Creator Finder prompt: Harry's criteria, the research steps with yt-dlp (candidates, count and screen, read subtitles and measure speed, rank) and the output format. Treat the learner's information as the "MY INFORMATION" section.
3. **Every number must come from something you ran.** Never from memory or a guess. Only download metadata and subtitles, never video or audio. Work in `~/JIL-decks/creator-search/` (or `$JIL_DECKS_DIR/creator-search/`).
4. **No yt-dlp / no network to YouTube:** say so plainly, give candidates from web search, and tell the learner to check each channel's video count (at least 50, ideally 100+) and that its videos have Japanese subtitles.
5. **Finish** with the table, 3 starter videos of ~15 minutes for the top pick, the ruled-out list, and a reminder to post their pick in the JIL Skool.
