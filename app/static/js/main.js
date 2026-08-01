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

  function escapeHtml(str) {
    return String(str || "")
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;");
  }

  function formatarMoeda(v) {
    return "R$ " + Number(v).toFixed(2).replace(".", ",");
  }

  const searchResults = document.getElementById("search-results");

  function renderSearchResults(qNorm, cards) {
    if (!searchResults) return;

    if (!qNorm) {
      searchResults.style.display = "none";
      searchResults.innerHTML = "";
      return;
    }

    const matches = Array.from(cards).filter(card => card.style.display !== "none");

    if (!matches.length) {
      searchResults.innerHTML = '<div class="search-results-empty">Nenhum produto encontrado.</div>';
      searchResults.style.display = "block";
      return;
    }

    searchResults.innerHTML = matches.map(card => {
      const categoryCard = card.closest(".category-card");
      const nome = card.querySelector(".nome")?.innerText || "";
      const setor = categoryCard?.dataset.categoria || "";
      const preco = Number(card.dataset.preco) || 0;
      const corCategoria = categoryCard
        ? getComputedStyle(categoryCard).getPropertyValue("--cat-color").trim()
        : "";
      const setorStyle = corCategoria ? ` style="color:${corCategoria}"` : "";
      return `<button type="button" class="search-result-item" data-id="${escapeHtml(card.dataset.id)}">
        <div class="search-result-info">
          <div class="search-result-nome">${escapeHtml(nome)}</div>
          <div class="search-result-setor"${setorStyle}>${escapeHtml(setor)}</div>
        </div>
        <div class="search-result-preco">${formatarMoeda(preco)}</div>
      </button>`;
    }).join("");
    searchResults.style.display = "block";
  }

  function irParaProdutoCard(id) {
    const card = document.querySelector(`.produto-card[data-id="${id}"]`);
    if (!card) return;

    closeSearch();
    if (searchInput) searchInput.blur();

    const header = document.querySelector(".app-header");
    const kpiBar = document.getElementById("kpi-sticky");
    const headerH = (header && header.offsetHeight) || 61;
    const kpiH = (kpiBar && kpiBar.offsetHeight) || 0;

    setTimeout(() => {
      const top = card.getBoundingClientRect().top + window.scrollY - headerH - kpiH - 8;
      window.scrollTo({ top, behavior: "smooth" });

      const prevTransition = card.style.transition;
      card.style.transition = "box-shadow 0.25s ease";
      card.style.boxShadow = "0 0 0 3px rgba(13,110,253,0.45)";
      setTimeout(() => {
        card.style.boxShadow = "";
        card.style.transition = prevTransition;
      }, 2000);
    }, 50);
  }

  if (searchResults) {
    searchResults.addEventListener("click", (e) => {
      const item = e.target.closest(".search-result-item");
      if (item) irParaProdutoCard(item.dataset.id);
    });
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

      renderSearchResults(qNorm, cards);
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
