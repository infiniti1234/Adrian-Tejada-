# Short meanings for grammar pieces and common contracted / Kansai endings, pre-filled into the breakdown so Claude only
# has to check them. Only pieces with ONE usual meaning are here: に, で, と, の, か, な, って, や and the like change meaning
# with context, so Claude fills those itself. Keyed by the piece as written (surface), then by dictionary form.
GRAMMAR = {
    "は": "topic marker: 'as for…'", "が": "subject marker (or 'but' between clauses)", "を": "object marker",
    "も": "also / even", "へ": "towards", "から": "from / because", "まで": "until / up to",
    "より": "than / from", "ね": "right? / isn't it", "よ": "I'm telling you (emphasis)",
    "けど": "but / although",
    "けれど": "but / although", "ながら": "while …ing", "たり": "doing things like…",
    "ば": "if", "たら": "if / when (after)", "なら": "if it's the case that", "ので": "because", "のに": "even though",
    "だけ": "only / just", "しか": "nothing but (with a negative)", "ほど": "to the extent of", "くらい": "about, roughly",
    "ぐらい": "about, roughly", "など": "and so on", "とか": "and things like / or something", "ばかり": "nothing but / just did",
    "じゃん": "casual: isn't it? / you know", "ぞ": "casual emphatic ending", "わ": "soft emphasis ending", "さ": "casual filler / emphasis",
    "だ": "is (plain)", "です": "is (polite)", "ます": "polite verb ending", "た": "past tense", "ない": "not (negative)",
    "たい": "want to", "れる": "passive / can / polite", "られる": "passive / can / polite", "せる": "make / let someone do",
    "させる": "make / let someone do", "う": "let's / I'll (volitional)", "よう": "let's / I'll (volitional)",
    "て": "te-form: links actions ('and then')", "ちゃう": "end up doing (casual てしまう)", "じゃう": "end up doing (casual でしまう)",
    "てる": "is …ing (casual ている)", "でる": "is …ing (casual でいる)", "とく": "do in advance (casual ておく)",
    "下さい": "please (ください)", "ください": "please", "御座い": "polite 'is / have' (ございます)", "ござい": "polite 'is / have' (ございます)",
    # Kansai
    "やん": "isn't it? / you know (Kansai for じゃん)", "やで": "it's…, I'm telling you (Kansai)",
    "ねん": "the thing is… (Kansai for んだ)", "へん": "not (Kansai negative)", "ひん": "not (Kansai negative)",
    "とる": "is …ing (Kansai for ている)", "とん": "is …ing (Kansai for ているの)", "とっ": "was …ing (Kansai, from とる)",
    "はる": "polite verb ending (Kansai)", "あかん": "no good, mustn't (Kansai for だめ)", "ほんま": "really, truly (Kansai for 本当)",
    "めっちゃ": "really, super", "やろ": "right? / probably (Kansai for だろう)", "やから": "so, because (Kansai for だから)",
    "けど": "but", "せや": "that's right (Kansai for そうだ)",
}


def gloss_for(surface, base=""):
    return GRAMMAR.get(surface) or GRAMMAR.get(base) or ""
