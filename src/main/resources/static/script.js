document.addEventListener("DOMContentLoaded", () => {

// ================= ELEMENTS =================
const generateBtn = document.getElementById("generateBtn");
const copyBtn = document.getElementById("copyBtn");
const output = document.getElementById("output");
const inputBox = document.getElementById("inputText");
const loading = document.getElementById("loading");
const historyContainer = document.querySelector(".history");

const menuBtn = document.getElementById("menuBtn");
const mobileSearch = document.getElementById("mobileSearch");
const desktopSearch = document.getElementById("desktopSearch");
const sidebar = document.querySelector(".sidebar");

// ================= INIT =================
loadHistory();

// ================= SIDEBAR =================
if (menuBtn) {
  menuBtn.addEventListener("click", () => {
    sidebar.classList.toggle("active");
  });
}

// ================= SEARCH =================

// Mobile search
if (mobileSearch) {
  mobileSearch.addEventListener("input", (e) => {
    const value = e.target.value.trim();

    if (value.length > 0) {
      sidebar.classList.add("active");
      loadHistory(value);
    } else {
      sidebar.classList.remove("active");
      loadHistory();
    }
  });
}

// Desktop search
if (desktopSearch) {
  desktopSearch.addEventListener("input", (e) => {
    loadHistory(e.target.value);
  });
}

// Close sidebar when clicking outside
document.addEventListener("click", (e) => {
  if (
    sidebar &&
    !sidebar.contains(e.target) &&
    e.target !== menuBtn &&
    e.target !== mobileSearch
  ) {
    sidebar.classList.remove("active");
  }
});

// ================= BUTTONS =================

// Generate
if (generateBtn) {
  generateBtn.addEventListener("click", generateNotes);
}

// Copy
if (copyBtn) {
  copyBtn.addEventListener("click", () => {
    if (!output.innerText.trim()) {
      alert("Nothing to copy!");
      return;
    }
    navigator.clipboard.writeText(output.innerText);
    alert("Copied!");
  });
}

// Shortcut Ctrl + Enter
if (inputBox) {
  inputBox.addEventListener("keydown", (e) => {
    if (e.ctrlKey && e.key === "Enter") {
      generateNotes();
    }
  });
}

// ================= GENERATE =================

async function generateNotes() {
  const input = inputBox.value.trim();

  if (!input) {
    alert("Enter something first!");
    return;
  }

  output.innerText = "";
  loading.style.display = "block";

  try {
    const res = await fetch("https://ai-project-production-6ec9.up.railway.app/api/notes", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify({ input })
    });

    const data = await res.json();

    if (data && data.content) {
      typeText(output, data.content);
      saveToHistory(input, data.content);
    } else {
      output.innerText = "No notes returned.";
    }

  } catch (err) {
    output.innerText = "Error: " + err.message;
  } finally {
    loading.style.display = "none";
  }
}

// ================= TYPE EFFECT =================

function typeText(el, text) {
  let i = 0;
  el.innerText = "";

  const interval = setInterval(() => {
    el.innerText += text.charAt(i);
    i++;
    if (i >= text.length) clearInterval(interval);
  }, 5);
}

// ================= HISTORY =================

// SAVE
function saveToHistory(input, outputText) {
  let history = JSON.parse(localStorage.getItem("notesHistory")) || [];

  history.unshift({
    id: Date.now(),
    input,
    output: outputText,
    pinned: false
  });

  history = history.slice(0, 20);

  localStorage.setItem("notesHistory", JSON.stringify(history));
  loadHistory();
}

// DELETE
function deleteItem(id) {
  let history = JSON.parse(localStorage.getItem("notesHistory")) || [];
  history = history.filter(item => item.id !== id);
  localStorage.setItem("notesHistory", JSON.stringify(history));
  loadHistory();
}

// PIN
function togglePin(id) {
  let history = JSON.parse(localStorage.getItem("notesHistory")) || [];

  history = history.map(item => {
    if (item.id === id) item.pinned = !item.pinned;
    return item;
  });

  localStorage.setItem("notesHistory", JSON.stringify(history));
  loadHistory();
}

// LOAD + SEARCH + PIN FIRST
function loadHistory(search = "") {
  let history = JSON.parse(localStorage.getItem("notesHistory")) || [];

  const query = search.toLowerCase().trim();

  let filtered = history.filter(item =>
    item.input && item.input.toLowerCase().includes(query)
  );

  // pinned first
  filtered.sort((a, b) => b.pinned - a.pinned);

  historyContainer.innerHTML = "";

  if (filtered.length === 0) {
    historyContainer.innerHTML =
      "<p style='color:#94a3b8'>No results found</p>";
    return;
  }

  filtered.forEach(item => {
    const div = document.createElement("div");
    div.className = "item";

    div.innerHTML = `
      <span class="text">📄 ${item.input.substring(0, 25)}</span>
      <div class="item-actions">
        <button class="pin">${item.pinned ? "📌" : "📍"}</button>
        <button class="delete">🗑</button>
      </div>
    `;

    // LOAD NOTE
    div.querySelector(".text").addEventListener("click", () => {
      inputBox.value = item.input;
      output.innerText = item.output;
      sidebar.classList.remove("active");
    });

    // PIN
    div.querySelector(".pin").addEventListener("click", (e) => {
      e.stopPropagation();
      togglePin(item.id);
    });

    // DELETE
    div.querySelector(".delete").addEventListener("click", (e) => {
      e.stopPropagation();
      deleteItem(item.id);
    });

    historyContainer.appendChild(div);
  });
}

});
