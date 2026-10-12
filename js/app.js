"use strict";

/* ---------- Data ---------- */
const Q = window.QURAN;
const surahs = Q.surahs;

/* ---------- Progress (localStorage) ---------- */
const LS_KEY = "hifz-progress";
let progress = loadProgress();

function defaultProgress(){
  return { points: 0, streak: { count: 0, last: "" }, memorized: {}, activity: 0 };
}
function loadProgress(){
  try {
    const raw = localStorage.getItem(LS_KEY);
    if (raw) { return Object.assign(defaultProgress(), JSON.parse(raw)); }
  } catch (e) {}
  return defaultProgress();
}
function saveProgress(){
  localStorage.setItem(LS_KEY, JSON.stringify(progress));
}

/* ---------- Helpers ---------- */
const $ = (sel) => document.querySelector(sel);
const $$ = (sel) => Array.from(document.querySelectorAll(sel));

function key(surah, ayah){ return surah + ":" + ayah; }

function isMemorized(surah, ayah){ return !!progress.memorized[key(surah, ayah)]; }
function setMemorized(surah, ayah, val){
  const k = key(surah, ayah);
  if (val) progress.memorized[k] = true; else delete progress.memorized[k];
}
function addPoints(n){ progress.points += n; progress.activity++; touchStreak(); saveProgress(); updatePills(); }

function touchStreak(){
  const today = new Date().toISOString().slice(0,10);
  if (progress.streak.last !== today){
    const yesterday = new Date(Date.now()-86400000).toISOString().slice(0,10);
    progress.streak.count = (progress.streak.last === yesterday) ? progress.streak.count + 1 : 1;
    progress.streak.last = today;
  }
  saveProgress();
}

function shuffle(arr){
  const a = arr.slice();
  for (let i = a.length - 1; i > 0; i--){
    const j = Math.floor(Math.random() * (i + 1));
    [a[i], a[j]] = [a[j], a[i]];
  }
  return a;
}

function toast(msg){
  const t = $("#toast");
  t.textContent = msg;
  t.classList.remove("hidden");
  clearTimeout(t._timer);
  t._timer = setTimeout(() => t.classList.add("hidden"), 1800);
}

/* ---------- Navigation ---------- */
function show(screenId){
  $$(".screen").forEach(s => s.classList.remove("active"));
  const el = $("#screen-" + screenId);
  if (el) el.classList.add("active");
  $$(".tab").forEach(t => t.classList.toggle("active", t.dataset.go === screenId));
  window.scrollTo({ top: 0 });
}

function bindNavigation(){
  document.querySelectorAll("[data-go]").forEach(el => {
    el.addEventListener("click", () => {
      const target = el.dataset.go;
      if (target === "surah" && !state.currentSurah){
        // open first surah by default
        openSurah(1);
        return;
      }
      show(target);
    });
  });
}

/* ---------- State ---------- */
const state = {
  currentSurah: null,
  builderSurah: null,
};

/* ---------- Update pills ---------- */
function updatePills(){
  $("#pill-points").textContent = "⭐ " + progress.points;
  $("#pill-streak").textContent = "🔥 " + progress.streak.count;
}

/* ---------- Home ---------- */
function renderHome(){
  const grid = $("#surah-grid");
  grid.innerHTML = "";
  surahs.forEach(s => {
    const total = s.verses.length;
    let done = 0;
    for (let a = 1; a <= total; a++) if (isMemorized(s.num, a)) done++;
    const pct = total ? Math.round(done / total * 100) : 0;
    const card = document.createElement("button");
    card.className = "surah-card";
    card.innerHTML =
      `<span class="num">${s.num}</span>
       <div class="name">${s.name}</div>
       <div class="meaning">${s.meaning}</div>
       <div class="mini-bar"><i style="width:${pct}%"></i></div>
       <div class="count">${done}/${total}</div>`;
    card.addEventListener("click", () => openSurah(s.num));
    grid.appendChild(card);
  });

  renderContinue();
  updatePills();
}

function renderContinue(){
  const list = $("#continue-list");
  list.innerHTML = "";
  // find surahs with partial progress, else show a few early surahs
  let picks = surahs.filter(s => {
    const total = s.verses.length;
    let done = 0;
    for (let a = 1; a <= total; a++) if (isMemorized(s.num, a)) done++;
    return done > 0 && done < total;
  });
  if (picks.length === 0) picks = surahs.slice(0, 3);
  picks.slice(0, 4).forEach(s => {
    const total = s.verses.length;
    let done = 0;
    for (let a = 1; a <= total; a++) if (isMemorized(s.num, a)) done++;
    const item = document.createElement("button");
    item.className = "quick-item";
    item.innerHTML =
      `<div class="q-top">
         <span class="q-name">${s.num}. ${s.name}</span>
         <span style="font-size:12px;color:var(--muted)">${done}/${total}</span>
       </div>
       <div class="q-meaning">${s.meaning}</div>`;
    item.addEventListener("click", () => openSurah(s.num));
    list.appendChild(item);
  });
}

/* ---------- Surah view ---------- */
function openSurah(num){
  state.currentSurah = num;
  show("surah");
  const s = surahs[num - 1];
  $("#surah-header").innerHTML =
    `<h2>${s.num}. ${s.name}</h2><p>${s.meaning}</p><p style="color:#9aa1aa">${s.verses.length} verses</p>`;
  renderVerseList();
}

function renderVerseList(){
  const s = surahs[state.currentSurah - 1];
  const list = $("#verse-list");
  list.innerHTML = "";
  const showT = $("#toggle-translit").checked;
  const showTr = $("#toggle-translation").checked;

  s.verses.forEach(v => {
    const item = document.createElement("div");
    item.className = "verse-item";
    const mem = isMemorized(s.num, v.ayah);
    item.innerHTML =
      `<span class="ayah-badge">${v.ayah}</span>` +
      (showT ? `<div class="translit">${escapeHtml(v.translit)}</div>` : "") +
      (showTr ? `<div class="translation">${escapeHtml(v.translation)}</div>` : "") +
      `<div class="verse-foot">
         <button class="mem-btn ${mem ? "mem" : ""}">${mem ? "✓ Memorized" : "Mark memorized"}</button>
       </div>`;
    const btn = item.querySelector(".mem-btn");
    btn.addEventListener("click", () => {
      const now = isMemorized(s.num, v.ayah);
      setMemorized(s.num, v.ayah, !now);
      if (!now){ addPoints(5); toast("Memorized! +5 ⭐"); }
      else { saveProgress(); }
      renderVerseList();
      refreshSurahMini();
    });
    list.appendChild(item);
  });
}

function escapeHtml(str){
  return str.replace(/[&<>"']/g, c => ({ "&":"&amp;","<":"&lt;",">":"&gt;",'"':"&quot;","'":"&#39;" }[c]));
}

function refreshSurahMini(){
  renderHome(); // quick refresh of grid bars
}

/* ---------- Build surah <select> options ---------- */
function fillSurahSelect(el, withAllOption){
  el.innerHTML = "";
  if (withAllOption){ const o = document.createElement("option"); o.value = "0"; o.textContent = "All (random)"; el.appendChild(o); }
  surahs.forEach(s => {
    const o = document.createElement("option");
    o.value = s.num;
    o.textContent = `${s.num}. ${s.name}`;
    el.appendChild(o);
  });
}

/* ---------- Verse Builder ---------- */
const builder = { surah: null, ayah: 0, words: [], revealed: 0 };

function startBuilder(){
  const num = parseInt($("#builder-surah").value, 10) || 1;
  const ayahInput = parseInt($("#builder-ayah").value, 10) || 0;
  const s = surahs[num - 1];
  const ayah = (ayahInput >= 1 && ayahInput <= s.verses.length)
    ? ayahInput : Math.floor(Math.random() * s.verses.length) + 1;
  const v = s.verses[ayah - 1];
  builder.surah = num;
  builder.ayah = ayah;
  builder.words = v.translit.split(/\s+/).filter(Boolean);
  builder.revealed = 0;

  $("#builder-setup").classList.add("hidden");
  $("#builder-play").classList.remove("hidden");
  $("#builder-translation").textContent = v.translation;
  $("#builder-grade").classList.add("hidden");

  renderBuilderWords();
}

function renderBuilderWords(){
  const wrap = $("#builder-words");
  wrap.innerHTML = "";
  builder.words.forEach((w, i) => {
    const chip = document.createElement("span");
    chip.className = "word-chip";
    if (i < builder.revealed){ chip.textContent = w; }
    else { chip.classList.add("hidden"); chip.textContent = "\u00A0".repeat(3); }
    wrap.appendChild(chip);
  });
}

function revealNext(){
  if (builder.revealed < builder.words.length){
    builder.revealed++;
    renderBuilderWords();
  }
  if (builder.revealed >= builder.words.length){
    $("#builder-grade").classList.remove("hidden");
  }
}

function revealAll(){
  builder.revealed = builder.words.length;
  renderBuilderWords();
  $("#builder-grade").classList.remove("hidden");
}

/* ---------- Matching ---------- */
const matching = { deck: [], chosenLeft: null, chosenRight: null, matches: 0, moves: 0, pairs: [] };

function startMatching(){
  const num = parseInt($("#matching-surah").value, 10) || 1;
  const s = surahs[num - 1];
  const pool = shuffle(s.verses).slice(0, 6);
  matching.pairs = pool;
  matching.matches = 0;
  matching.moves = 0;
  matching.chosenLeft = null;
  matching.chosenRight = null;

  $("#matching-setup").classList.add("hidden");
  $("#matching-board").classList.remove("hidden");
  $("#matching-result").classList.add("hidden");
  renderMatching();
}

function renderMatching(){
  const left = $("#col-left");
  const right = $("#col-right");
  left.innerHTML = "";
  right.innerHTML = "";

  const leftOrder = shuffle(matching.pairs.map((_, i) => i));
  const rightOrder = shuffle(matching.pairs.map((_, i) => i));

  leftOrder.forEach(i => {
    const c = document.createElement("button");
    c.className = "match-card";
    c.textContent = matching.pairs[i].translit;
    c.dataset.side = "L";
    c.dataset.idx = i;
    c.addEventListener("click", () => onMatchPick(c, "L", i));
    left.appendChild(c);
  });

  rightOrder.forEach(i => {
    const c = document.createElement("button");
    c.className = "match-card";
    c.textContent = matching.pairs[i].translation;
    c.dataset.side = "R";
    c.dataset.idx = i;
    c.addEventListener("click", () => onMatchPick(c, "R", i));
    right.appendChild(c);
  });

  $("#match-count").textContent = "Matches: " + matching.matches + "/" + matching.pairs.length;
  $("#match-moves").textContent = "Moves: " + matching.moves;
}

function onMatchPick(el, side, idx){
  if (el.classList.contains("done")) return;

  if (side === "L"){
    if (matching.chosenLeft) matching.chosenLeft.classList.remove("sel");
    matching.chosenLeft = el;
    el.classList.add("sel");
  } else {
    if (matching.chosenRight) matching.chosenRight.classList.remove("sel");
    matching.chosenRight = el;
    el.classList.add("sel");
  }

  if (matching.chosenLeft && matching.chosenRight){
    matching.moves++;
    const li = parseInt(matching.chosenLeft.dataset.idx, 10);
    const ri = parseInt(matching.chosenRight.dataset.idx, 10);
    if (li === ri){
      matching.chosenLeft.classList.remove("sel");
      matching.chosenRight.classList.remove("sel");
      matching.chosenLeft.classList.add("done");
      matching.chosenRight.classList.add("done");
      matching.matches++;
      matching.chosenLeft = null;
      matching.chosenRight = null;
      if (matching.matches === matching.pairs.length){
        finishMatching();
      }
    } else {
      const l = matching.chosenLeft, r = matching.chosenRight;
      l.classList.add("wrong");
      r.classList.add("wrong");
      setTimeout(() => {
        l.classList.remove("sel","wrong");
        r.classList.remove("sel","wrong");
      }, 380);
      matching.chosenLeft = null;
      matching.chosenRight = null;
    }
    $("#match-count").textContent = "Matches: " + matching.matches + "/" + matching.pairs.length;
    $("#match-moves").textContent = "Moves: " + matching.moves;
  }
}

function finishMatching(){
  const pts = 10 + Math.max(0, 5 - matching.moves) * 2;
  addPoints(pts);
  $("#matching-board").classList.add("hidden");
  const res = $("#matching-result");
  res.classList.remove("hidden");
  res.innerHTML =
    `<div class="big">🎉</div>
     <p>Solved in ${matching.moves} moves!</p>
     <p class="big" style="font-size:30px">+${pts} ⭐</p>
     <button class="btn primary" id="matching-again">Play again</button>`;
  $("#matching-again").addEventListener("click", () => {
    $("#matching-result").classList.add("hidden");
    $("#matching-setup").classList.remove("hidden");
  });
}

/* ---------- Quiz ---------- */
const quiz = { questions: [], i: 0, score: 0, direction: "pick-translit" };

function startQuiz(){
  const num = parseInt($("#quiz-surah").value, 10) || 0;
  let pool;
  if (num >= 1) pool = surahs[num-1].verses.map(v => ({ s: num, a: v.ayah, t: v.translit, tr: v.translation }));
  else {
    pool = [];
    surahs.forEach(s => s.verses.forEach(v => pool.push({ s: s.num, a: v.ayah, t: v.translit, tr: v.translation })));
  }
  const chosen = shuffle(pool).slice(0, 10);
  quiz.questions = chosen;
  quiz.i = 0;
  quiz.score = 0;
  quiz.direction = Math.random() < 0.5 ? "pick-translit" : "pick-translation";

  $("#quiz-setup").classList.add("hidden");
  $("#quiz-result").classList.add("hidden");
  $("#quiz-play").classList.remove("hidden");
  renderQuestion();
}

function renderQuestion(){
  const q = quiz.questions[quiz.i];
  $("#quiz-num").textContent = `Question ${quiz.i + 1} of ${quiz.questions.length}  •  Score ${quiz.score}`;

  // build options: 1 correct + 3 distractors (same direction)
  let promptText, correctText, distractorFn;
  if (quiz.direction === "pick-translit"){
    promptText = q.tr;      // given the meaning...
    correctText = q.t;      // ...pick the transliteration
    distractorFn = o => o.t;
  } else {
    promptText = q.t;       // given the transliteration...
    correctText = q.tr;     // ...pick the meaning
    distractorFn = o => o.tr;
  }
  $("#quiz-prompt").textContent = promptText;
  const distractors = shuffle(quiz.questions)
    .filter(o => o !== q)
    .slice(0, 3)
    .map(distractorFn);
  const opts = shuffle(distractors.concat([correctText]));

  const wrap = $("#quiz-options");
  wrap.innerHTML = "";
  opts.forEach(opt => {
    const b = document.createElement("button");
    b.className = "quiz-opt";
    b.textContent = opt;
    b.addEventListener("click", () => {
      const isCorrect = (opt === correctText);
      $$(".quiz-opt").forEach(x => x.disabled = true);
      if (isCorrect){
        b.classList.add("correct");
        quiz.score++;
        addPoints(3);
      } else {
        b.classList.add("wrong");
        // highlight the correct one
        $$(".quiz-opt").forEach(x => { if (x.textContent === correctText) x.classList.add("correct"); });
      }
      setTimeout(nextQuestion, 900);
    });
    wrap.appendChild(b);
  });
}

function nextQuestion(){
  quiz.i++;
  if (quiz.i < quiz.questions.length){
    renderQuestion();
  } else {
    finishQuiz();
  }
}

function finishQuiz(){
  $("#quiz-play").classList.add("hidden");
  const res = $("#quiz-result");
  res.classList.remove("hidden");
  const pct = Math.round(quiz.score / quiz.questions.length * 100);
  let msg = (pct >= 80) ? "Masha'Allah, excellent!" : (pct >= 50) ? "Good effort, keep going!" : "Keep practicing — you'll get there!";
  res.innerHTML =
    `<div class="big">${pct}%</div>
     <p>${quiz.score} / ${quiz.questions.length} correct</p>
     <p>${msg}</p>
     <button class="btn primary" id="quiz-again">Try again</button>`;
  $("#quiz-again").addEventListener("click", () => {
    $("#quiz-result").classList.add("hidden");
    $("#quiz-setup").classList.remove("hidden");
  });
}

/* ---------- Progress screen ---------- */
function renderProgress(){
  const total = surahs.reduce((n,s) => n + s.verses.length, 0);
  const memorized = Object.keys(progress.memorized).length;
  const pct = Math.round(memorized / total * 100);
  let bestSurah = surahs.map(s => {
    let done = 0;
    for (let a = 1; a <= s.verses.length; a++) if (isMemorized(s.num, a)) done++;
    return { s, done };
  }).sort((a,b) => b.done - a.done)[0];

  $("#stats-cards").innerHTML =
    `<div class="stat-card"><div class="val">${memorized}</div><div class="lbl">Verses memorized</div></div>
     <div class="stat-card"><div class="val">${pct}%</div><div class="lbl">Of Quran</div></div>
     <div class="stat-card"><div class="val">${progress.points}</div><div class="lbl">Points</div></div>
     <div class="stat-card"><div class="val">${progress.streak.count}</div><div class="lbl">Day streak</div></div>
     <div class="stat-card"><div class="val">${bestSurah ? bestSurah.s.num : "—"}</div><div class="lbl">Best surah</div></div>
     <div class="stat-card"><div class="val">${progress.activity}</div><div class="lbl">Actions</div></div>`;

  const list = $("#memorized-list");
  list.innerHTML = "";
  const keys = Object.keys(progress.memorized);
  if (keys.length === 0){
    list.innerHTML = `<p style="color:var(--muted)">No verses memorized yet. Start with the Verse Builder or Quiz!</p>`;
  } else {
    // show most recent 50
    keys.slice(-50).reverse().forEach(k => {
      const [sn, ay] = k.split(":").map(Number);
      const s = surahs[sn-1];
      const v = s.verses[ay-1];
      const row = document.createElement("div");
      row.className = "mem-row";
      row.innerHTML = `<span class="ref">${sn}:${ay}</span><span class="tx">${escapeHtml(v.translit)}</span>`;
      list.appendChild(row);
    });
  }
}

/* ---------- Init ---------- */
function init(){
  fillSurahSelect($("#builder-surah"), false);
  fillSurahSelect($("#matching-surah"), false);
  fillSurahSelect($("#quiz-surah"), true);

  bindNavigation();

  $("#practice-surah").addEventListener("click", () => {
    if (state.currentSurah){
      $("#builder-surah").value = state.currentSurah;
      show("builder");
    }
  });

  $("#toggle-translit").addEventListener("change", renderVerseList);
  $("#toggle-translation").addEventListener("change", renderVerseList);

  $("#builder-start").addEventListener("click", startBuilder);
  $("#builder-reveal").addEventListener("click", revealNext);
  $("#builder-reveal-all").addEventListener("click", revealAll);
  $("#grade-knew").addEventListener("click", () => {
    setMemorized(builder.surah, builder.ayah, true);
    addPoints(10);
    toast("Great! +10 ⭐");
    nextBuilder();
  });
  $("#grade-struggle").addEventListener("click", () => {
    addPoints(2);
    toast("Keep going! +2 ⭐");
    nextBuilder();
  });

  $("#matching-start").addEventListener("click", startMatching);
  $("#quiz-start").addEventListener("click", startQuiz);

  $("#reset-progress").addEventListener("click", () => {
    if (confirm("Reset all progress and points?")){
      progress = defaultProgress();
      saveProgress();
      renderProgress();
      renderHome();
      toast("Progress reset");
    }
  });

  // refresh progress screen whenever it's shown
  document.querySelectorAll('[data-go="progress"]').forEach(el => {
    el.addEventListener("click", () => renderProgress());
  });

  renderHome();
}

function nextBuilder(){
  // jump to next ayah in same surah (or wrap)
  const s = surahs[builder.surah - 1];
  const next = (builder.ayah < s.verses.length) ? builder.ayah + 1 : 1;
  $("#builder-ayah").value = next;
  startBuilder();
}

init();

// Register service worker (PWA install / offline) — only over http(s)
if ("serviceWorker" in navigator && (location.protocol === "https:" || location.hostname === "localhost")) {
  window.addEventListener("load", () => {
    navigator.serviceWorker.register("sw.js").catch(() => {});
  });
}
