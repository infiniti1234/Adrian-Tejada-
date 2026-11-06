// Simple spaced repetition. Card state lives in data/srs.json keyed by word.
import { getSrs, saveSrs } from "./store.js";

const DAY = 86400000;

export function intervals(card) {
  const iv = card?.interval || 0;
  return {
    again: 0,
    hard: Math.max(1, Math.round(iv * 1.2)),
    good: iv ? Math.round(iv * (card.ease || 2.5)) : 1,
    easy: iv ? Math.round(iv * (card.ease || 2.5) * 1.3) : 4,
    month: 30,
  };
}

export function grade(word, rating, now = Date.now()) {
  const srs = getSrs();
  const card = (srs[word] ??= { interval: 0, ease: 2.5, reps: 0, lapses: 0, firstSeen: new Date(now).toISOString() });
  const iv = intervals(card);
  card.reps++;
  card.last = new Date(now).toISOString();
  if (rating === "mastered") {
    card.mastered = true;
    card.due = null;
  } else if (rating === "again") {
    card.lapses++;
    card.ease = Math.max(1.3, card.ease - 0.2);
    card.interval = 0;
    card.due = new Date(now + 10 * 60000).toISOString();
  } else {
    if (rating === "hard") card.ease = Math.max(1.3, card.ease - 0.15);
    if (rating === "easy") card.ease += 0.15;
    card.interval = iv[rating];
    card.mastered = false;
    card.due = new Date(now + card.interval * DAY).toISOString();
  }
  saveSrs();
  return card;
}

export function stats(now = Date.now()) {
  const today = new Date(now).toISOString().slice(0, 10);
  let newToday = 0, reviewsToday = 0;
  for (const c of Object.values(getSrs())) {
    if (c.firstSeen?.slice(0, 10) === today) newToday++;
    else if (c.last?.slice(0, 10) === today) reviewsToday++;
  }
  return { newToday, reviewsToday };
}
