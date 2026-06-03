from flask import Blueprint, render_template, request, redirect, url_for, flash, session
from app.extensions import db_cursor
from app.repositories.produto_repository import ProdutoRepository
from app.repositories.supermercado_repository import SupermercadoRepository
from app.services.produto_service import ProdutoService
from app.routes.auth import login_required

pages_bp = Blueprint("pages", __name__)

CATEGORIAS_PADRAO = [
    "Alimentos Principais",
    "Complementos",
    "Temperos",
    "Higiene e Limpeza",
]


@pages_bp.route("/")
@login_required
def home():
    with db_cursor() as cur:
        produtos = ProdutoRepository.list_all(cur, session["user_id"])

    setores_db = set(p["setor"] for p in produtos)
    extra = sorted(s for s in setores_db if s not in CATEGORIAS_PADRAO)
    categorias = CATEGORIAS_PADRAO + extra

    return render_template(
        "home.html",
        produtos=produtos,
        categorias=categorias,
        page="home"
    )


@pages_bp.route("/produtos")
@login_required
def produtos():
    with db_cursor() as cur:
        produtos = ProdutoRepository.list_all(cur, session["user_id"])

    setores_db = set(p["setor"] for p in produtos)
    extra = sorted(s for s in setores_db if s not in CATEGORIAS_PADRAO)
    categorias = CATEGORIAS_PADRAO + extra

    return render_template(
        "produtos.html",
        produtos=produtos,
        categorias=categorias,
        page="produtos"
    )


@pages_bp.route("/selecionar-supermercado")
@login_required
def selecionar_supermercado():
    from flask import current_app
    with db_cursor() as cur:
        supermercado = SupermercadoRepository.get_by_usuario(cur, session["user_id"])
    has_api = bool(current_app.config.get("GOOGLE_MAPS_API_KEY"))
    return render_template(
        "selecionar_supermercado.html",
        supermercado=supermercado,
        has_api=has_api,
    )


@pages_bp.route("/add", methods=["POST"])
@login_required
def add_item():
    nome = ProdutoService.normalizar_nome(request.form.get("nome", ""))
    setor = ProdutoService.normalizar_setor(request.form.get("setor", ""))
    preco_raw = request.form.get("preco", "0")

    try:
        preco = float(preco_raw)
        ProdutoService.validar(nome, preco, setor=setor)

        with db_cursor() as cur:
            ProdutoRepository.upsert_by_name(cur, nome, setor, preco, session["user_id"])

    except ValueError as e:
        flash(str(e))
    except Exception:
        flash("Erro ao salvar produto.")

    return redirect(url_for("pages.home"))
