(function () {
    const SpeechRec = window.SpeechRecognition || window.webkitSpeechRecognition;

    let recognition = null;
    let escutando = false;
    let estado = 'aguardando-produto';
    let produtoSelecionado = null;
    let produtosSugeridos = [];
    let qtdVoz = 0;
    let precoVoz = 0;

    /* ============================================================
       VOZ: OPEN / CLOSE
    ============================================================ */
    window.iniciarVoz = function () {
        if (typeof fecharMenuAcao === 'function') fecharMenuAcao();
        resetarEstado();
        document.getElementById('modalVoz').style.display = 'flex';

        if (!SpeechRec) {
            setStatus('Reconhecimento de voz não suportado neste navegador.');
            return;
        }

        falar('Diga o nome do produto. Para cadastrar um novo, diga registrar.');
        setTimeout(iniciarEscuta, 1800);
    };

    window.fecharVoz = function () {
        pararEscuta();
        window.speechSynthesis && window.speechSynthesis.cancel();
        document.getElementById('modalVoz').style.display = 'none';
        resetarEstado();
    };

    window.confirmarVozBtn = function () {
        aplicarProdutoVoz();
    };

    window.selecionarVozOpcao = function (idx) {
        selecionarProduto(produtosSugeridos[idx]);
    };

    /* ============================================================
       STATE
    ============================================================ */
    function resetarEstado() {
        pararEscuta();
        estado = 'aguardando-produto';
        produtoSelecionado = null;
        produtosSugeridos = [];
        qtdVoz = 0;
        precoVoz = 0;

        setStatus('Diga o nome do produto');
        const transcriptEl = document.getElementById('voz-transcript');
        if (transcriptEl) transcriptEl.textContent = '';
        const infoEl = document.getElementById('voz-produto-info');
        if (infoEl) { infoEl.style.display = 'none'; infoEl.innerHTML = ''; }
        const confirmarBtn = document.getElementById('btn-voz-confirmar');
        if (confirmarBtn) confirmarBtn.style.display = 'none';
        setMicAtivo(false);
    }

    function setMicAtivo(ativo) {
        const wrap = document.getElementById('voz-mic-wrap');
        const btn = document.getElementById('voz-mic-btn');
        if (wrap) wrap.classList.toggle('voz-escutando', ativo);
        if (btn) btn.classList.toggle('voz-mic-off', !ativo);
    }

    function setStatus(texto) {
        const el = document.getElementById('voz-status');
        if (el) el.textContent = texto;
    }

    /* ============================================================
       SPEECH RECOGNITION
    ============================================================ */
    function iniciarEscuta() {
        if (!SpeechRec || escutando) return;

        recognition = new SpeechRec();
        recognition.lang = 'pt-BR';
        recognition.continuous = false;
        recognition.interimResults = true;
        recognition.maxAlternatives = 3;

        recognition.onstart = () => {
            escutando = true;
            setMicAtivo(true);
        };

        recognition.onend = () => {
            escutando = false;
            setMicAtivo(false);
            const estadosAtivos = ['aguardando-produto', 'aguardando-quantidade', 'aguardando-preco', 'aguardando-selecao'];
            if (estadosAtivos.includes(estado)) {
                setTimeout(() => {
                    if (!escutando && estadosAtivos.includes(estado)) iniciarEscuta();
                }, 700);
            }
        };

        recognition.onresult = (event) => {
            const ultimo = event.results[event.results.length - 1];
            const texto = ultimo[0].transcript.trim();
            const transcriptEl = document.getElementById('voz-transcript');
            if (transcriptEl) transcriptEl.textContent = '“' + texto + '”';

            if (ultimo.isFinal) {
                const normalizado = texto.toLowerCase()
                    .normalize('NFD')
                    .replace(/[̀-ͯ]/g, '');
                processarComando(normalizado);
            }
        };

        recognition.onerror = (ev) => {
            if (ev.error === 'no-speech' || ev.error === 'aborted') return;
            escutando = false;
            setMicAtivo(false);
        };

        try { recognition.start(); } catch {}
    }

    function pararEscuta() {
        if (recognition) {
            try { recognition.abort(); } catch {}
            recognition = null;
        }
        escutando = false;
        setMicAtivo(false);
    }

    /* ============================================================
       COMMAND PROCESSING
    ============================================================ */
    async function processarComando(texto) {
        if (/\b(cancelar|fechar|sair|voltar)\b/.test(texto)) {
            fecharVoz();
            return;
        }

        if (estado === 'aguardando-produto') {
            await processarNomeProduto(texto);
        } else if (estado === 'aguardando-selecao') {
            processarSelecao(texto);
        } else if (estado === 'aguardando-quantidade') {
            processarQuantidade(texto);
        } else if (estado === 'aguardando-preco') {
            processarPreco(texto);
        } else if (estado === 'confirmando') {
            if (/\b(sim|confirmar|ok|isso|correto|certo|pode|confirma)\b/.test(texto)) {
                aplicarProdutoVoz();
            } else if (/\b(nao|cancelar|errado|diferente|mudar|refazer)\b/.test(texto)) {
                fecharVoz();
            }
        }
    }

    async function processarNomeProduto(texto) {
        const matchRegistrar = texto.match(/^(registrar|cadastrar|adicionar novo|novo produto|adicionar)\s*(.*)/);
        if (matchRegistrar) {
            const nomeExtraido = matchRegistrar[2].trim();
            pararEscuta();
            fecharVoz();
            if (typeof abrirModalProduto === 'function') {
                abrirModalProduto();
                if (nomeExtraido) {
                    setTimeout(() => {
                        const nomeInput = document.querySelector('#formNovoProduto input[name="nome"]');
                        if (nomeInput) {
                            nomeInput.value = capitalizarPrimeira(nomeExtraido);
                            nomeInput.focus();
                        }
                    }, 150);
                }
            }
            return;
        }

        estado = 'buscando';
        setStatus('Buscando...');
        pararEscuta();

        try {
            const res = await fetch('/api/produto/buscar?q=' + encodeURIComponent(texto));
            const data = await res.json();
            const produtos = data.status === 'ok' ? data.produtos : [];

            if (produtos.length === 0) {
                setStatus('Não encontrado. Diga o nome novamente ou "registrar".');
                falar('Produto não encontrado. Tente novamente ou diga registrar para cadastrar.');
                estado = 'aguardando-produto';
                setTimeout(iniciarEscuta, 2800);
            } else if (produtos.length === 1) {
                selecionarProduto(produtos[0]);
            } else {
                produtosSugeridos = produtos.slice(0, 4);
                renderizarListaProdutos(produtosSugeridos);
                estado = 'aguardando-selecao';
                const nomes = produtosSugeridos.map((p, i) => (i + 1) + ': ' + p.nome).join('. ');
                falar(produtos.length + ' produtos encontrados. ' + nomes + '. Diga o número.');
                setStatus(produtos.length + ' produtos encontrados — diga o número ou toque');
                setTimeout(iniciarEscuta, 1800 + produtosSugeridos.length * 600);
            }
        } catch {
            setStatus('Erro ao buscar. Tente novamente.');
            falar('Erro ao buscar o produto. Tente novamente.');
            estado = 'aguardando-produto';
            setTimeout(iniciarEscuta, 2000);
        }
    }

    function processarSelecao(texto) {
        const num = extrairNumeroInteiro(texto);
        if (num !== null && num >= 1 && num <= produtosSugeridos.length) {
            selecionarProduto(produtosSugeridos[num - 1]);
            return;
        }
        const match = produtosSugeridos.find(p => {
            const nomeLower = p.nome.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
            return nomeLower.includes(texto) || texto.includes(nomeLower.split(' ')[0]);
        });
        if (match) {
            selecionarProduto(match);
        } else {
            falar('Não entendi. Diga o número do produto.');
        }
    }

    function selecionarProduto(produto) {
        produtoSelecionado = produto;
        const card = encontrarCard(produto.nome);
        qtdVoz = card ? (Number(card.querySelector('.qtd-valor')?.innerText) || 0) : 0;
        precoVoz = card ? (Number(card.dataset.preco) || produto.preco) : produto.preco;

        renderizarInfoProduto(produto);
        estado = 'aguardando-quantidade';
        setStatus('Quantas unidades?');
        falar(produto.nome + ' encontrado. Quantas unidades?');
        setTimeout(iniciarEscuta, 1800);
    }

    function processarQuantidade(texto) {
        const num = extrairNumeroInteiro(texto);
        if (num !== null && num >= 0) {
            qtdVoz = num;
            atualizarCampos();
            estado = 'aguardando-preco';
            setStatus('Qual o preço? (Ex: "doze e noventa")');
            falar(num + ' unidade' + (num !== 1 ? 's' : '') + '. Qual o preço?');
            setTimeout(iniciarEscuta, 1600);
        } else {
            falar('Não entendi a quantidade. Diga um número, por exemplo: dois ou três.');
            setTimeout(iniciarEscuta, 2000);
        }
    }

    function processarPreco(texto) {
        const preco = extrairPreco(texto);
        if (preco !== null && preco >= 0) {
            precoVoz = preco;
            atualizarCampos();
            estado = 'confirmando';
            document.getElementById('btn-voz-confirmar').style.display = 'flex';
            setStatus('Confirmar? Diga "sim" ou toque em Confirmar');
            falar('Preço ' + formatarFala(preco) + '. ' + qtdVoz + ' ' + produtoSelecionado.nome + '. Confirmar?');
        } else {
            falar('Não entendi o preço. Diga, por exemplo: cinco reais ou doze e noventa.');
            setTimeout(iniciarEscuta, 2200);
        }
    }

    function aplicarProdutoVoz() {
        if (!produtoSelecionado) return;

        const precoInput = document.getElementById('voz-input-preco');
        const qtdInput = document.getElementById('voz-input-qtd');
        if (precoInput) precoVoz = parseFloat(precoInput.value) || precoVoz;
        if (qtdInput) qtdVoz = parseInt(qtdInput.value) || qtdVoz;

        const card = encontrarCard(produtoSelecionado.nome);
        if (card) {
            card.dataset.preco = precoVoz;
            card.querySelector('.preco-input').value = precoVoz;
            card.querySelector('.preco-texto').innerText = formatarMoeda(precoVoz);
            card.querySelector('.qtd-valor').innerText = qtdVoz;
            card.querySelector('.total-produto').innerText = formatarMoeda(precoVoz * qtdVoz);
            if (typeof calcularTotais === 'function') calcularTotais();
            if (typeof salvarEstado === 'function') salvarEstado();
            realcarCard(card);
        }

        mostrarToast(produtoSelecionado.nome + ' atualizado', 'ok');
        fecharVoz();
    }

    /* ============================================================
       RENDER
    ============================================================ */
    function renderizarInfoProduto(produto) {
        const el = document.getElementById('voz-produto-info');
        const card = encontrarCard(produto.nome);
        const qtdAtual = card ? (Number(card.querySelector('.qtd-valor')?.innerText) || 0) : qtdVoz;
        const precoAtual = (card ? (Number(card.dataset.preco) || produto.preco) : produto.preco);

        el.innerHTML =
            '<div class="voz-produto-nome">' + esc(produto.nome) + '</div>' +
            '<div class="voz-produto-setor">' + esc(produto.setor) + '</div>' +
            '<div class="voz-campo-row">' +
                '<div class="voz-campo">' +
                    '<label>Quantidade</label>' +
                    '<input id="voz-input-qtd" type="number" min="0" step="1" value="' + qtdAtual + '" inputmode="numeric">' +
                '</div>' +
                '<div class="voz-campo">' +
                    '<label>Preço (R$)</label>' +
                    '<input id="voz-input-preco" type="number" min="0" step="0.01" value="' + precoAtual.toFixed(2) + '" inputmode="decimal">' +
                '</div>' +
            '</div>';

        el.style.display = 'block';
    }

    function renderizarListaProdutos(produtos) {
        const el = document.getElementById('voz-produto-info');
        el.innerHTML = '<div class="voz-opcoes-lista">' +
            produtos.map(function (p, i) {
                return '<button class="voz-opcao-btn" onclick="selecionarVozOpcao(' + i + ')">' +
                    '<span class="voz-opcao-num">' + (i + 1) + '</span>' +
                    '<div>' +
                        '<div class="voz-opcao-nome">' + esc(p.nome) + '</div>' +
                        '<div class="voz-opcao-setor">' + esc(p.setor) + '</div>' +
                    '</div>' +
                    '</button>';
            }).join('') +
        '</div>';
        el.style.display = 'block';
    }

    function atualizarCampos() {
        const qtdInput = document.getElementById('voz-input-qtd');
        const precoInput = document.getElementById('voz-input-preco');
        if (qtdInput) qtdInput.value = qtdVoz;
        if (precoInput) precoInput.value = precoVoz.toFixed(2);
    }

    /* ============================================================
       UTILITIES
    ============================================================ */
    function falar(texto) {
        if (!window.speechSynthesis) return;
        window.speechSynthesis.cancel();
        const utt = new SpeechSynthesisUtterance(texto);
        utt.lang = 'pt-BR';
        utt.rate = 1.05;
        window.speechSynthesis.speak(utt);
    }

    function extrairNumeroInteiro(texto) {
        const map = {
            'zero': 0, 'um': 1, 'uma': 1, 'dois': 2, 'duas': 2, 'tres': 3, 'quatro': 4,
            'cinco': 5, 'seis': 6, 'sete': 7, 'oito': 8, 'nove': 9, 'dez': 10,
            'onze': 11, 'doze': 12, 'treze': 13, 'quatorze': 14, 'quinze': 15,
            'dezesseis': 16, 'dezessete': 17, 'dezoito': 18, 'dezenove': 19, 'vinte': 20,
            'primeiro': 1, 'primeira': 1, 'segundo': 2, 'segunda': 2,
            'terceiro': 3, 'terceira': 3, 'quarto': 4, 'quarta': 4, 'quinto': 5, 'quinta': 5
        };
        const palavras = texto.replace(/[,\.]/g, ' ').split(/\s+/);
        for (const p of palavras) {
            if (map[p] !== undefined) return map[p];
        }
        const match = texto.match(/\d+/);
        return match ? parseInt(match[0]) : null;
    }

    function extrairPreco(texto) {
        const map = {
            'zero': 0, 'um': 1, 'uma': 1, 'dois': 2, 'duas': 2, 'tres': 3, 'quatro': 4,
            'cinco': 5, 'seis': 6, 'sete': 7, 'oito': 8, 'nove': 9, 'dez': 10,
            'onze': 11, 'doze': 12, 'treze': 13, 'quatorze': 14, 'quinze': 15,
            'dezesseis': 16, 'dezessete': 17, 'dezoito': 18, 'dezenove': 19, 'vinte': 20,
            'trinta': 30, 'quarenta': 40, 'cinquenta': 50, 'sessenta': 60,
            'setenta': 70, 'oitenta': 80, 'noventa': 90, 'cem': 100, 'cento': 100
        };

        let match = texto.match(/(\d+)[,\.](\d{1,2})/);
        if (match) return parseFloat(match[1] + '.' + match[2].padEnd(2, '0'));

        match = texto.match(/\b(\d+)\b/);
        if (match) return parseFloat(match[1]);

        const semStop = texto.replace(/\breais?\b|\bcentavos?\b|\be\b/g, ' ').trim().split(/\s+/);
        const partes = semStop.map(function (p) { return map[p]; }).filter(function (n) { return n !== undefined; });
        if (partes.length === 2) return parseFloat(partes[0] + '.' + String(partes[1]).padEnd(2, '0'));
        if (partes.length === 1) return partes[0];

        return null;
    }

    function formatarMoeda(v) {
        return 'R$ ' + Number(v).toFixed(2).replace('.', ',');
    }

    function formatarFala(v) {
        const partes = Number(v).toFixed(2).split('.');
        const reais = partes[0];
        const cents = partes[1];
        if (cents === '00') return reais + ' reais';
        return reais + ' reais e ' + cents + ' centavos';
    }

    function capitalizarPrimeira(str) {
        return str.charAt(0).toUpperCase() + str.slice(1);
    }

    function esc(str) {
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
    }

    function encontrarCard(nome) {
        return Array.from(document.querySelectorAll('.produto-card'))
            .find(function (c) { return c.querySelector('.nome')?.innerText.trim() === nome.trim(); }) || null;
    }

    function realcarCard(card) {
        card.scrollIntoView({ behavior: 'smooth', block: 'center' });
        const prev = card.style.transition;
        card.style.transition = 'box-shadow 0.25s ease';
        card.style.boxShadow = '0 0 0 3px rgba(124,58,237,0.45)';
        setTimeout(function () {
            card.style.boxShadow = '';
            card.style.transition = prev;
        }, 2000);
    }

    function mostrarToast(msg, tipo) {
        const container = document.getElementById('toast-container');
        if (!container) return;
        const t = document.createElement('div');
        t.className = 'toast toast-' + tipo;
        t.textContent = msg;
        container.appendChild(t);
        requestAnimationFrame(function () { t.classList.add('show'); });
        setTimeout(function () {
            t.classList.remove('show');
            setTimeout(function () { t.remove(); }, 300);
        }, 3000);
    }
})();
