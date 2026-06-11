(function () {
    let stream = null;
    let produtosScan = [];
    let produtoAtual = null;
    let statusEl = null;

    window.abrirCamera = async function () {
        statusEl = document.getElementById("camera-status");
        const btn = document.getElementById("btn-capturar");

        document.getElementById("modalCamera").style.display = "flex";
        btn.disabled = false;
        definirStatus("Iniciando câmera...", "");

        if (!navigator.mediaDevices?.getUserMedia) {
            definirStatus("Câmera não suportada neste navegador.", "error");
            btn.disabled = true;
            return;
        }

        try {
            stream = await navigator.mediaDevices.getUserMedia({
                video: { facingMode: { ideal: "environment" }, width: { ideal: 1280 } }
            });
            document.getElementById("camera-video").srcObject = stream;
            definirStatus("Aponte para a embalagem do produto e toque em Capturar.", "");
        } catch (e) {
            const msg = e.name === "NotAllowedError"
                ? "Permissão negada. Habilite a câmera nas configurações do navegador."
                : "Câmera não disponível neste dispositivo.";
            definirStatus(msg, "error");
            document.getElementById("btn-capturar").disabled = true;
        }
    };

    window.fecharCamera = function () {
        pararStream();
        document.getElementById("modalCamera").style.display = "none";
    };

    function pararStream() {
        if (stream) {
            stream.getTracks().forEach(t => t.stop());
            stream = null;
        }
    }

    window.capturarImagem = async function () {
        const video = document.getElementById("camera-video");
        const canvas = document.getElementById("camera-canvas");
        const btn = document.getElementById("btn-capturar");

        if (!video.videoWidth) {
            definirStatus("Câmera ainda inicializando. Aguarde.", "");
            return;
        }

        btn.disabled = true;
        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        canvas.getContext("2d").drawImage(video, 0, 0);

        definirStatus("Lendo texto da embalagem...", "");

        try {
            const texto = await extrairTexto(canvas);

            if (!texto || texto.trim().length < 2) {
                definirStatus("Nenhum texto detectado. Aproxime mais e tente novamente.", "warn");
                btn.disabled = false;
                return;
            }

            definirStatus("Buscando na sua lista...", "");
            const res = await fetch(`/api/produto/buscar?q=${encodeURIComponent(texto)}`);
            const data = await res.json();
            const produtos = data.status === "ok" ? data.produtos : [];

            if (!produtos.length) {
                definirStatus("Produto não encontrado na sua lista. Tente outro ângulo.", "warn");
                btn.disabled = false;
                return;
            }

            pararStream();
            document.getElementById("modalCamera").style.display = "none";
            abrirScanResultado(produtos);
        } catch {
            definirStatus("Erro ao processar imagem. Tente novamente.", "error");
            btn.disabled = false;
        }
    };

    async function extrairTexto(canvas) {
        if ("TextDetector" in window) {
            const detector = new TextDetector();
            const blocos = await detector.detect(canvas);
            return blocos.map(b => b.rawValue).join(" ");
        }
        return extrairComTesseract(canvas);
    }

    async function extrairComTesseract(canvas) {
        if (!window.Tesseract) {
            definirStatus("Carregando leitor de texto (1x, pode demorar)...", "");
            await carregarScript("https://cdn.jsdelivr.net/npm/tesseract.js@4.1.4/dist/tesseract.min.js");
        }
        const { data: { text } } = await Tesseract.recognize(canvas, "por+eng", {
            logger: () => {}
        });
        return text;
    }

    function carregarScript(src) {
        return new Promise((resolve, reject) => {
            if (document.querySelector(`script[src="${src}"]`)) { resolve(); return; }
            const s = document.createElement("script");
            s.src = src;
            s.onload = resolve;
            s.onerror = reject;
            document.head.appendChild(s);
        });
    }

    function abrirScanResultado(produtos) {
        produtosScan = produtos;
        const titulo = document.getElementById("scan-titulo");
        const corpo = document.getElementById("scan-corpo");

        if (produtos.length === 1) {
            produtoAtual = produtos[0];
            titulo.textContent = "Produto encontrado";
            renderizarEditorProduto(corpo, produtos[0]);
        } else {
            produtoAtual = null;
            titulo.textContent = `${produtos.length} produtos encontrados`;
            corpo.innerHTML = `<p class="scan-hint">Selecione o produto encontrado na embalagem:</p>` +
                produtos.map((p, i) => `
                    <button class="scan-item" onclick="selecionarItemScan(${i})">
                        <span class="scan-item-nome">${esc(p.nome)}</span>
                        <span class="scan-item-setor">${esc(p.setor)}</span>
                    </button>`
                ).join("");
        }

        document.getElementById("modalScanResultado").style.display = "flex";
    }

    window.selecionarItemScan = function (idx) {
        produtoAtual = produtosScan[idx];
        document.getElementById("scan-titulo").textContent = "Produto encontrado";
        renderizarEditorProduto(document.getElementById("scan-corpo"), produtoAtual);
    };

    function renderizarEditorProduto(container, produto) {
        const card = encontrarCard(produto.nome);
        const qtdAtual = card ? (Number(card.querySelector(".qtd-valor").innerText) || 0) : 0;
        const precoAtual = card ? (Number(card.dataset.preco) || produto.preco) : produto.preco;

        container.innerHTML = `
            <div class="scan-produto-info">
                <div class="scan-nome">${esc(produto.nome)}</div>
                <div class="scan-setor">${esc(produto.setor)}</div>
            </div>
            <label class="field">
                <span>Preço (R$)</span>
                <input id="scan-preco" type="number" step="0.01" min="0"
                       value="${precoAtual.toFixed(2)}" inputmode="decimal">
            </label>
            <div class="scan-qtd-row">
                <span class="scan-qtd-label">Quantidade no carrinho</span>
                <div class="qtd">
                    <button type="button" class="btn-menos" onclick="ajustarQtdScan(-1)">−</button>
                    <span class="qtd-valor" id="scan-qtd">${qtdAtual}</span>
                    <button type="button" class="btn-mais" onclick="ajustarQtdScan(1)">+</button>
                </div>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn-ghost" onclick="fecharModalScan()">Cancelar</button>
                <button type="button" class="btn-primary" onclick="aplicarScan()">
                    <i class="fa-solid fa-check"></i>
                    Atualizar lista
                </button>
            </div>`;
    }

    window.ajustarQtdScan = function (delta) {
        const el = document.getElementById("scan-qtd");
        el.innerText = Math.max(0, Number(el.innerText) + delta);
    };

    window.fecharModalScan = function () {
        document.getElementById("modalScanResultado").style.display = "none";
        produtoAtual = null;
        produtosScan = [];
    };

    window.aplicarScan = function () {
        if (!produtoAtual) return;

        const novoPreco = parseFloat(document.getElementById("scan-preco").value) || 0;
        const novaQtd = Number(document.getElementById("scan-qtd").innerText) || 0;
        const card = encontrarCard(produtoAtual.nome);

        if (card) {
            card.dataset.preco = novoPreco;
            card.querySelector(".preco-input").value = novoPreco;
            card.querySelector(".preco-texto").innerText = formatar(novoPreco);
            card.querySelector(".qtd-valor").innerText = novaQtd;
            card.querySelector(".total-produto").innerText = formatar(novoPreco * novaQtd);
            calcularTotais();
            salvarEstado();
            realcarCard(card);
        }

        fecharModalScan();
    };

    function encontrarCard(nome) {
        return [...document.querySelectorAll(".produto-card")]
            .find(c => c.querySelector(".nome")?.innerText.trim() === nome.trim()) || null;
    }

    function realcarCard(card) {
        card.scrollIntoView({ behavior: "smooth", block: "center" });
        const prevTransition = card.style.transition;
        card.style.transition = "box-shadow 0.25s ease";
        card.style.boxShadow = "0 0 0 3px rgba(13,110,253,0.45)";
        setTimeout(() => {
            card.style.boxShadow = "";
            card.style.transition = prevTransition;
        }, 2000);
    }

    function definirStatus(msg, tipo) {
        if (!statusEl) return;
        statusEl.textContent = msg;
        statusEl.className = "camera-status" + (tipo ? ` camera-status--${tipo}` : "");
    }

    function esc(str) {
        return String(str)
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;");
    }
})();
