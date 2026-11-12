You're finding me a Japanese YouTuber to build my listening on, using Harry's method from the Japanese Immersion Lab. My information is at the bottom. Do the research for real on YouTube with yt-dlp: every number you give me must come from something you ran, never from memory or a guess.
WHAT HARRY LOOKS FOR IN A CREATOR
1. Natural, unscripted Japanese: people talking, vlogs, challenges, gaming with commentary, podcasts, cooking while chatting. Not anime, dramas, skits or anything scripted. Not videos made for learners of Japanese. Not AI voices or AI narration.
2. They talk the way I want to sound: the gender, rough age, standard Japanese or a Kansai mix, casual or polite, from my information. You become what you listen to. If nobody fits everything, only compromise on the topic or the speaker's age. Age matters least unless the gap is huge (a 20-year-old copying a 55-year-old sounds odd).
3. I'd enjoy their videos, so I'll actually watch hours of them.
4. A big back catalogue: at least 50 videos, ideally over 100. Anything under 50 is out. Flag anything between 50 and 100. Count regular videos only (the channel's Videos tab), not Shorts.
5. Videos are public (not members-only), and Japanese subtitles exist on most of them (auto-generated is fine). My deck tools need the subtitles.
6. Plenty of videos around 10 to 20 minutes, because the one-video method uses a video of about 15 minutes.
HOW TO RESEARCH
Setup:
- Work in a folder called creator-search inside my JIL-decks folder (create it if needed).
- Check yt-dlp works with: yt-dlp --version. If it's missing, install it with: python -m pip install -U yt-dlp
- Only download metadata and subtitles. Never download videos or audio.
- Add --encoding utf-8 --extractor-args "youtube:lang=ja" to every yt-dlp command, so titles come back in their original Japanese. On Windows, if a Python script reading yt-dlp output crashes on Japanese text, set PYTHONUTF8=1.
- Go gently: run subtitle downloads one at a time with --sleep-subtitles 2. If YouTube answers HTTP 429 (too many requests), wait a minute and retry. If it asks you to confirm you're not a bot, don't try to get round it: mark that video unchecked.
Step 1. Candidates. From my information, list 25 to 40 Japanese creators who could fit, across all my interests. Write search terms in Japanese. Find each real channel with a YouTube search, for example:
yt-dlp --flat-playlist --print "%(channel)s | %(channel_id)s | %(title)s" "ytsearch10:<Japanese search terms>"
Never guess a channel's @handle (most guesses don't exist). Once you have a channel ID, use https://www.youtube.com/channel/<channel_id>/videos from then on.
Step 2. Count and screen. For each candidate:
yt-dlp --flat-playlist --print "%(id)s | %(duration)s | %(title)s" "https://www.youtube.com/channel/<channel_id>/videos"
Count the videos and how many are 10 minutes or longer. Drop anything under 50 videos. Mark 50 to 100 as flagged. Drop anything clearly scripted, made for learners, or AI-voiced.
Step 3. Listen through the subtitles. For the 8 to 12 best candidates left, take 2 recent, typical videos of 10 minutes or more and get their Japanese subtitles without the video:
yt-dlp --skip-download --write-auto-subs --write-subs --sub-langs "ja" --sub-format vtt --sleep-subtitles 2 -o "%(id)s" "<video URL>"
- If most of a channel's videos have no Japanese subtitles, drop it or flag it.
- Auto-subtitles repeat each line as it scrolls, so collapse repeated lines before reading or counting, and leave out tags like [音楽].
- Read them and note: natural talk or scripted? One speaker or a group? Standard Japanese or Kansai (ほんま, やん, ちゃう, めっちゃ, へん)? Casual or polite (です/ます)?
- Measure speaking speed: Japanese characters (no punctuation or spaces) divided by the minutes between the first and last line. Under 300 a minute is on the easier side, 300 to 400 is middle, over 400 is fast. Groups and Kansai play harder than their number. Under about 150 a minute usually means the video is mostly music or on-screen text, not talking: drop it, because I need someone who talks.
- If a video fails with "この動画はご覧いただけません" or a format error (common on made-for-kids videos), retry with --extractor-args "youtube:lang=ja;player_client=web_embedded" --ignore-no-formats-error (in place of the usual --extractor-args)
- Members-only videos show up in the Videos list but fail when you fetch them. Skip them, and if a lot of a channel's recent videos are members-only, flag it: the video count overstates what I can watch.
Step 4. Rank the survivors against my information and Harry's rules above.
WHAT TO GIVE ME
1. Your top pick and 2 to 4 alternatives, in a table: channel | link | videos (and how many are 10 min+) | typical length | speaker (gender, rough age, standard or Kansai) | casual or polite | speed (characters a minute) | Japanese subtitles | why it fits me | anything flagged (50 to 100 videos, few subtitles, mostly groups, stopped uploading).
2. For your top pick: 3 starter videos of about 15 minutes, each with Japanese subtitles, for the one-video method (title and link).
3. The ones you ruled out, one line each with the reason.
4. Remind me to post my pick in the JIL Skool.
If anything failed or you couldn't check something, say so plainly rather than filling the gap.
MY INFORMATION
>>> PASTE YOUR INFORMATION HERE <<<
