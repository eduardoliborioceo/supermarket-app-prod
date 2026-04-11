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
      filterProducts(searchInput.value.trim().toLowerCase());
    });
  }

  function filterProducts(q) {
    // Home page: filtra .produto-card pelo nome
    const cards = document.querySelectorAll(".produto-card");
    if (cards.length) {
      cards.forEach(card => {
        const nomeEl = card.querySelector(".nome");
        const match = !q || (nomeEl && nomeEl.innerText.toLowerCase().includes(q));
        card.style.display = match ? "" : "none";
      });

      document.querySelectorAll(".category-card").forEach(cat => {
        const allCards = cat.querySelectorAll(".produto-card");
        if (!allCards.length) { cat.style.display = ""; return; }
        const hasVisible = Array.from(allCards).some(c => c.style.display !== "none");
        cat.style.display = hasVisible || !q ? "" : "none";
      });
    }

    // Produtos page: filtra .admin-card pelo input nome
    document.querySelectorAll(".admin-card").forEach(card => {
      const nomeInput = card.querySelector(".produto-nome");
      const match = !q || (nomeInput && nomeInput.value.toLowerCase().includes(q));
      card.style.display = match ? "" : "none";
    });
  }

  // Register Service Worker
  if ("serviceWorker" in navigator) {
    window.addEventListener("load", () => {
      navigator.serviceWorker.register("/static/sw.js").catch(() => {});
    });
  }
})();
