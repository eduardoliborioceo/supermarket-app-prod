(function () {
  const btnSearch = document.querySelector(".btn-search");
  const searchBar = document.querySelector(".mobile-search");

  function closeSearch() {
    if (!searchBar) return;
    searchBar.style.display = "none";
  }

  function openSearch() {
    if (!searchBar) return;
    searchBar.style.display = "block";
    const input = searchBar.querySelector("input");
    if (input) input.focus();
  }

  if (btnSearch && searchBar) {
    btnSearch.addEventListener("click", () => {
      const isOpen = searchBar.style.display === "block";
      if (isOpen) closeSearch();
      else openSearch();
    });
  }

  const clearBtn = document.querySelector(".mobile-search-clear");
  if (clearBtn && searchBar) {
    clearBtn.addEventListener("click", () => {
      const input = searchBar.querySelector("input");
      if (input) input.value = "";
      filterProducts("");
      closeSearch();
    });
  }

  const searchInput = searchBar ? searchBar.querySelector("input") : null;
  if (searchInput) {
    searchInput.addEventListener("input", () => {
      filterProducts(searchInput.value.trim());
    });
  }

  function normalizar(str) {
    return (str || "").normalize("NFD").replace(/[̀-ͯ]/g, "").toLowerCase();
  }

  function filterProducts(q) {
    const qNorm = normalizar(q);

    // Home page: filtra .produto-card pelo nome
    const cards = document.querySelectorAll(".produto-card");
    if (cards.length) {
      cards.forEach(card => {
        const nomeEl = card.querySelector(".nome");
        const match = !qNorm || normalizar(nomeEl && nomeEl.innerText).includes(qNorm);
        card.style.display = match ? "" : "none";
      });

      document.querySelectorAll(".category-card").forEach(cat => {
        const allCards = cat.querySelectorAll(".produto-card");
        if (!allCards.length) { cat.style.display = ""; return; }
        const hasVisible = Array.from(allCards).some(c => c.style.display !== "none");
        cat.style.display = hasVisible || !qNorm ? "" : "none";
      });
    }

    // Produtos page: filtra .admin-card pelo input nome
    const adminCards = document.querySelectorAll(".admin-card");
    if (adminCards.length) {
      adminCards.forEach(card => {
        const nomeInput = card.querySelector(".produto-nome");
        const match = !qNorm || normalizar(nomeInput && nomeInput.value).includes(qNorm);
        card.style.display = match ? "" : "none";
      });

      document.querySelectorAll(".cat-section").forEach(section => {
        const visible = Array.from(section.querySelectorAll(".admin-card"))
          .filter(c => c.style.display !== "none");
        section.style.display = visible.length || !qNorm ? "" : "none";
        if (qNorm && visible.length) {
          const toggle = section.querySelector(".cat-toggle");
          const items = section.querySelector(".cat-items");
          if (toggle && toggle.classList.contains("collapsed")) {
            toggle.classList.remove("collapsed");
            if (items) items.classList.add("open");
          }
        }
        if (!qNorm) {
          const toggle = section.querySelector(".cat-toggle");
          const items = section.querySelector(".cat-items");
          if (toggle && items) {
            toggle.classList.add("collapsed");
            items.classList.remove("open");
          }
        }
      });
    }
  }

  // Register Service Worker
  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("/static/sw.js").catch(() => {});
    });
  }
})();
