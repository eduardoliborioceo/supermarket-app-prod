import re

from flask import url_for

from app.repositories.compra_repository import CompraRepository
from app.repositories.produto_repository import ProdutoRepository
from app.repositories.usuario_repository import UsuarioRepository

CATEGORIAS_PADRAO = [
    "Principais",
    "Complementos",
    "Temperos",
    "Higiene e Limpeza",
    "Bebidas",
]

IMAGEM_PASTA = "images/icons/produtos"


class ProdutoService:
    @staticmethod
    def validar(nome, preco, setor=None):
        if not nome or not nome.strip():
            raise ValueError("Nome obrigatório")

        if preco is None:
            raise ValueError("Preço obrigatório")

        if float(preco) < 0:
            raise ValueError("Preço inválido")

        if setor is not None and (not setor or not str(setor).strip()):
            raise ValueError("Setor obrigatório")

    @staticmethod
    def normalizar_nome(nome: str) -> str:
        return (nome or "").strip().title()

    @staticmethod
    def normalizar_setor(setor: str) -> str:
        return (setor or "").strip()

    @staticmethod
    def extrair_tokens_busca(texto: str) -> list:
        tokens = re.split(r"[^a-zA-ZÀ-ÿ0-9]+", texto)
        return [t for t in tokens if len(t) >= 3][:15]

    @staticmethod
    def montar_categorias(produtos: list) -> list:
        setores_db = set(p["setor"] for p in produtos)
        extra = sorted(s for s in setores_db if s not in CATEGORIAS_PADRAO)
        return CATEGORIAS_PADRAO + extra

    @staticmethod
    def montar_imagem_url(imagem: str) -> str:
        if not imagem:
            return None
        return url_for("static", filename=f"{IMAGEM_PASTA}/{imagem}")

    @staticmethod
    def anexar_imagens(produtos: list) -> list:
        for p in produtos:
            p["imagem_url"] = ProdutoService.montar_imagem_url(p.get("imagem"))
        return produtos

    @staticmethod
    def montar_home(cur, usuario_id: int) -> dict:
        """Produtos + carrinho combinados, mesma composição usada pelo
        home.html (web) e pelo endpoint JSON /api/home (app nativo) — a
        regra de negócio vive só aqui, nunca duplicada entre as duas UIs."""
        produtos = ProdutoRepository.list_all(cur, usuario_id)
        carrinho = CompraRepository.get_carrinho(cur, usuario_id)
        gasto_previsto = UsuarioRepository.get_gasto_previsto(cur, usuario_id)

        carrinho_map = {c["produto_id"]: c for c in carrinho}
        for p in produtos:
            item = carrinho_map.get(p["id"])
            p["qtd_carrinho"] = int(item["quantidade"]) if item else 0
            p["preco_carrinho"] = float(item["preco"]) if item else float(p["ultimo_preco"])
        ProdutoService.anexar_imagens(produtos)

        return {
            "produtos": produtos,
            "categorias": ProdutoService.montar_categorias(produtos),
            "gasto_previsto": float(gasto_previsto or 0),
        }
