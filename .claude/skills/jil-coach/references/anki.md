# Anki, cards and words

## Why Anki is required
- **Why:** you can't immerse your way to words you've never pinned down. Anki gets a word to "I recognise this".
- **Then immersion takes over.** About 10 times hearing it in context and understanding it makes a word really stick. Knowing a word is a spectrum, not yes/no.

## Setting it up
1. Install Anki on your computer and phone, and sync them with a free AnkiWeb account.
2. In every deck's options, change the two values set to 20 (including "new cards per day") to 9999, so Anki doesn't throttle you.
3. Suspend your big batch of new cards and unsuspend today's set.
4. Do your reviews first every day, then new cards in sets of 10.

**Getting through Anki faster:**
- **The speed-run (new cards only):** the first time a new card shows up, show the answer straight away, read the breakdown, picture the word and press Good, then let the next showing decide. On reviews, grade properly: a wrong reading is always Again.
- **Swap out cards that keep failing.** A few stubborn cards eat a lot of time.
- **Keep new cards to what your retention can hold.** If retention drops well under 75-80%, your reviews snowball.
- **Do reviews in gaps** (commute, a work break) so Anki doesn't eat your listening time.

Keep **one master deck** for your own cards (from the deck-maker or the Claude chat), not split by video or topic. The **Hikaru 500 stays its own deck** alongside it. The kanji deck is separate too, and you delete it about a week after you've started your word cards.

## The starter decks
- **The Hikaru 500.** Everyone does it after kana. It comes in two looks with the same cards: dark (black and gold) or light (like the JIL Immersion Tracker). Import one; you can switch later with Anki's Change Note Type and keep your progress.
- **The 300-kanji deck.** Optional (see `foundations.md`).
- **The JLPT deck** (7,160 words). Only if you're sitting the JLPT.

Everything else, you make yourself from what you watch.

## Premade decks: no
- Someone else decided what's "common" for you. A boxing fan, a baseball fan, a maths student and a konbini worker all need completely different words.
- Words from your own videos stick better, because you already know the context.

## The Hikaru 500
- **What:** the 500 most common words in the YouTuber Hikaru's videos, as **sentence cards from card 1.** Each sentence only uses words from earlier cards plus the one new word, so the deck builds on itself.
- **Brute force:** a few of the very first cards have two new words. You just grind them until they stick; that's normal early on.
- **Front:** the sentence.
- **Back:**
  - the sentence with furigana
  - audio of the sentence
  - the new word with its standard pitch accent drawn on it
  - the meaning and the English
  - a short note
- **Why pitch on these 500:** so you know what to listen for from day one. Your own sentence cards later don't need pitch.
- **Language:** standard Japanese, mostly casual like Hikaru, with some polite sentences.

## How many new cards a day
- **There's no floor and no ceiling.**
  - Start at 10 or more a day.
  - Then do as many as you can while your retention for words stays around **75-80%**.
  - **Retention above 80%:** add more.
  - **75-80%:** hold.
  - **Below 75%:** add fewer (10 a day is a fine place to sit).
  - **Below 50%:** stop adding until it recovers.
- **About 20 a day is normal.** 50 a day is fine if your retention holds; Harry aimed for 50-60 a day for his N1 (some days only 20-30).
- **Red flag:** if you're struggling at 20 a day (retention around 50-70%), something's wrong with your cards or your setup. Post a few of your cards in the Skool so Harry can see what's going on.
- **The Anki push:** go hard to about 4,000-5,000 words in your first 3-6 months, counted from day one, without dropping your daily listening. About 4,000 words in 6 months is about 22 new cards a day, so the usual 20 a day already gets you there; faster than that is fine if your retention holds. Then slow right down and put the time into listening. About 10,000 words is the long-run target, and after the push, Anki comes back in short bursts when you start a new topic.
- **The Anki trap:** don't let cards eat the time you should be listening.

## How to grade a card
- **The reading must be right.** Wrong reading = Again.
- **If the audio is on the front:** if you didn't really know the reading before the audio played, it's wrong. If you weren't sure but the audio confirmed it and you got the meaning, press Good.
- **The meaning: grade on whether the Japanese makes sense in your head,** even if it's vague, **not on whether you can translate it into English.**
  - Translating back and forth is a roundabout habit that slows down real-time understanding.
  - The goal is Japanese straight to meaning. If it means something to you, Good; if it doesn't, Again.
  - If you currently understand by translating in your head, that's normal early on. Just stop grading on it, and lean into "feel the meaning".
- **Pitch accent is never something you grade.**
- **A card that keeps failing:** check the sentence is clear and that you fully understand it, then swap it for a clearer one. Hikaru 500 cards are the exception: don't edit them (each one builds on the cards before), post the ones you keep failing in the Skool. Adding furigana is a fix for a failing card, not a habit.

## Breaks and review pile-ups
- **Back from a break** (holiday, broken laptop): stop adding new cards and clear the backlog over a few days, not in one sitting. Start new cards again once your daily reviews are back to normal and retention is around 75-80%.
- **A busy stretch coming up:** add more new cards before it starts. During it, just do reviews and keep listening, which needs no Anki.
- **Away with only your phone?** Keep doing reviews on the phone app (it syncs through AnkiWeb). Even short sessions stop the pile-up.
- **Adding extra new cards first is for busy stretches** where you'll still do reviews, not for a full break, where it would only make the backlog bigger.
- **Long breaks from Anki are fine** as long as the listening keeps going. Harry's own Anki history is a big push followed by breaks and short bursts.

## Making your own cards (after the Hikaru 500)
- **The easy way:** the **JIL deck-maker** skill (in Claude Code) does all of this from a YouTube link. It finds every word and chunk you don't know, writes the cards, checks them, builds the deck and keeps your known-words list up to date. The classroom's Toolkit lesson shows how to install it. It needs Claude Code, which needs a paid Claude plan.
- **On the free Claude plan:** use the free-Claude kit from the classroom (the JIL card prompt, your known-words file and the JIL card types). Download the video's subtitles as a plain .txt file from any YouTube subtitle download site, attach it and your known-words file to a Claude chat, paste in the card prompt, and import the cards it gives you into Anki (File > Import). Add the words from its last block to your known-words file. The Toolkit lesson walks through it.
- **Main source:** the video you're working through with the one-video method (see `listening.md`). Without the deck-maker, use the free-Claude kit (above), or ask Claude for the cards yourself. Give Claude the video's transcript and your list of words you already know. Ask it to pull out every word and chunk you don't know, and write a sentence card for each:
  - **one new word per card**; two only if a natural sentence with one would feel forced
  - only words you already know otherwise
  - clear, self-contained context you'd fully understand
  - sentences in the same style as the video (the same kind of Japanese you'd hear in it), varied in length and type so the cards don't all feel the same
- **Second source:** words that **pop out** at you during immersion. Write them in one phone note and turn the list into cards in batches.
- **What goes on cards:** content words (nouns, verbs, adjectives…) and **chunks**. The tiny bits (particles, endings, the little in-between sounds) come from listening. Don't try to look up sounds you can't break down.
- **Use Claude** for card-making and grammar questions. The free plan is enough to make cards in the chat; the full method (the deck-maker, the breakdown page, the passive-repeat audio, the speech comparison) needs Claude Pro, which Harry calls the best investment you'll ever make.
- **An old premade deck** (Core 6k etc.): no new cards from it. Keep reviewing it while its retention stays above 75%; below that, suspend it, or bad sentences keep your reviews in the hundreds forever. Give the Hikaru 500 its own options preset so its 9999 setting doesn't switch on the old deck's new cards.
- **Studied before?** Go through the Hikaru 500 and delete every card where you know every word and understand the whole sentence, then carry on with what's left.
- **Your known-words list:** keep a list of every word in your Anki (the deck-maker starts it with every Hikaru 500 word and reads the decks already in your Anki). Give it to Claude whenever you make cards, so it never makes you a card for a word you already know.
- **Card look:** your own cards from the deck-maker come in the same two looks as the Hikaru 500, dark or light. Pick the one that matches.
- **Card format** (Harry's style):
  - **Front:** one clean sentence with the target word.
  - **Back:** the word with its reading, its meaning, a short breakdown of the word and of the sentence, and the sentence's English.

## Japanese-Japanese definitions (going monolingual)
- **When:** switch to Japanese definitions at about **4,000 words.** Not too early, not too late to make a difference.
- **How:** ask Claude for a short, plain Japanese definition of the word as it's used in your sentence. Go one layer deep: if a word in that definition is unknown, get its English meaning (its own card), never another Japanese definition. Don't fall into an endless chain of definitions.
- **Your own cards:** the deck-maker has a Japanese-definitions setting for the switch; words with a plain English equivalent (most nouns) keep English.

## Audio on cards
- **What's on every sentence card's back** (the Hikaru 500, deck-maker cards and Claude-chat cards all share it): the sentence with furigana and audio, then **Target word** (the word and its reading, pitch on the Hikaru 500, then Meaning, Breakdown of its kanji or parts, and English equivalent) and **Sentence breakdown** (Meaning, the sentence chunk by chunk, and three natural English versions). From about 4,000 words, new cards switch to Japanese explanations (意味（日本語で）, 分解（日本語）), with the English equivalents kept.
- **By default, sentence audio goes on the back.** This suits people who want to read properly (books and so on), since the reading stays the main thing you grade.
- **Audio on the front is a legitimate choice** if you mainly want listening and speaking as fast as possible. You'll still see kanji constantly, so you won't end up unable to read; you'll just forget readings a bit more.
- **Audio is optional.** Your cards work fine without it.
- **How:** an ElevenLabs API key plus Claude Code (Claude running on your computer), following the classroom's card-audio Toolkit lesson.
  - **Keep the key in a file on your computer and never paste it into a chat.**
  - For Japanese, use the eleven_v3 model. The older multilingual model misreads kanji on quite a few sentences (店 as てん, for example).
  - Listen to a few clips before doing a whole deck.
- **Audio is nice, not essential.** Harry never had audio on his cards. With enough immersion you're hearing the words all day anyway.
