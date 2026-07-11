from flask import Blueprint, request, jsonify, session
from app.extensions import db_cursor
from app.repositories.produto_repository import ProdutoRepository
from app.repositories.supermercado_repository import SupermercadoRepository
from app.repositories.compra_repository import CompraRepository
from app.repositories.usuario_repository import UsuarioRepository
from app.services.produto_service import ProdutoService
from app.services.supermercado_service import SupermercadoService
from app.services.itens_padrao_service import ItensPadraoService
from app.services.compra_service import CompraService
from app.routes.auth import login_required

api_bp = Blueprint("api", __name__, url_prefix="/api")


def _bad_request(msg, status=400):
    return jsonify({"status": "error", "message": msg}), status


@api_bp.route("/seed-defaults", methods=["POST"])
@login_required
def seed_defaults():
    with db_cursor() as cur:
        ItensPadraoService.popular_todos(cur, session["user_id"])
    return jsonify({"status": "ok"})


@api_bp.route("/supermercados/buscar")
@login_required
def buscar_supermercados():
    q = request.args.get("q", "").strip()
    lat = request.args.get("lat")
    lng = request.args.get("lng")

    if lat and lng and not q:
        results, google_status = SupermercadoService.buscar_por_localizacao(lat, lng)
    elif q:
        results, google_status = SupermercadoService.buscar_por_texto(q, lat, lng)
    else:
        return jsonify({"results": [], "google_status": "no_query"})

    return jsonify({"results": results, "google_status": google_status})


@api_bp.route("/supermercado/selecionar", methods=["POST"])
@login_required
def selecionar_supermercado():
    data = request.get_json(silent=True) or {}
    nome = (data.get("nome") or "").strip()
    endereco = (data.get("endereco") or "").strip()
    place_id = (data.get("place_id") or "").strip()

    if not nome:
        return _bad_request("Nome do supermercado é obrigatório.")

    with db_cursor() as cur:
        SupermercadoRepository.save(cur, session["user_id"], nome, endereco, place_id)

    session["supermercado_nome"] = nome
    session["supermercado_endereco"] = endereco
    return jsonify({"status": "ok"})


@api_bp.route("/produto/buscar")
@login_required
def buscar_produto():
    q = (request.args.get("q") or "").strip()
    if len(q) < 2:
        return jsonify({"status": "ok", "produtos": []})
    tokens = ProdutoService.extrair_tokens_busca(q)
    if not tokens:
        return jsonify({"status": "ok", "produtos": []})
    with db_cursor() as cur:
        rows = ProdutoRepository.search_by_tokens(cur, session["user_id"], tokens)
    return jsonify({
        "status": "ok",
        "produtos": [
            {"id": p["id"], "nome": p["nome"], "setor": p["setor"], "preco": float(p["ultimo_preco"])}
            for p in rows
        ]
    })


@api_bp.route("/produto/adicionar", methods=["POST"])
@login_required
def adicionar_produto():
    data = request.get_json(silent=True) or {}
    nome = ProdutoService.normalizar_nome(data.get("nome", ""))
    setor = (data.get("setor") or "").strip()
    preco = data.get("preco")

    try:
        preco = float(preco)
        ProdutoService.validar(nome, preco, setor=setor)
        with db_cursor() as cur:
            produto_id = ProdutoRepository.upsert_by_name(cur, nome, setor, preco, session["user_id"])
        return jsonify({"status": "ok", "id": produto_id, "nome": nome, "preco": preco, "setor": setor})
    except ValueError as e:
        return _bad_request(str(e))
    except Exception:
        return _bad_request("Erro ao salvar produto.", status=500)


@api_bp.route("/produto/excluir", methods=["POST"])
@login_required
def excluir_produto():
    data = request.get_json(silent=True) or {}
    produto_id = data.get("id")

    if not produto_id:
        return _bad_request("ID obrigatório.")

    try:
        with db_cursor() as cur:
            ProdutoRepository.delete(cur, produto_id, session["user_id"])
        return jsonify({"status": "ok"})
    except Exception:
        return _bad_request("Erro ao excluir produto.", status=500)


@api_bp.route("/produto/atualizar", methods=["POST"])
@login_required
def atualizar_produto():
    data = request.get_json(silent=True) or {}

    produto_id = data.get("id")
    nome = ProdutoService.normalizar_nome(data.get("nome", ""))
    setor = (data.get("setor") or "").strip()
    preco = data.get("preco")

    if not produto_id:
        return _bad_request("ID obrigatório.")

    try:
        preco = float(preco)
        ProdutoService.validar(nome, preco, setor=setor)

        with db_cursor() as cur:
            ProdutoRepository.update(cur, produto_id, nome, setor, preco, session["user_id"])

        return jsonify({"status": "ok"})
    except ValueError as e:
        return _bad_request(str(e))
    except Exception:
        return _bad_request("Erro ao salvar produto.", status=500)


@api_bp.route("/carrinho/atualizar", methods=["POST"])
@login_required
def atualizar_carrinho():
    data = request.get_json(silent=True) or {}
    produto_id = data.get("produto_id")

    try:
        quantidade = float(data.get("quantidade"))
        preco = float(data.get("preco"))
        CompraService.validar(produto_id, quantidade, preco)

        with db_cursor() as cur:
            linhas = CompraRepository.upsert_item(cur, produto_id, session["user_id"], quantidade, preco)

        if linhas == 0:
            return _bad_request("Produto não encontrado.", status=404)
        return jsonify({"status": "ok"})
    except (TypeError, ValueError) as e:
        return _bad_request(str(e) or "Dados inválidos.")
    except Exception:
        return _bad_request("Erro ao salvar item do carrinho.", status=500)


@api_bp.route("/carrinho/limpar", methods=["POST"])
@login_required
def limpar_carrinho():
    with db_cursor() as cur:
        CompraService.limpar(cur, session["user_id"])
    return jsonify({"status": "ok"})


@api_bp.route("/carrinho/gasto-previsto", methods=["POST"])
@login_required
def salvar_gasto_previsto():
    data = request.get_json(silent=True) or {}

    try:
        valor = float(data.get("valor"))
        CompraService.validar_gasto_previsto(valor)

        with db_cursor() as cur:
            UsuarioRepository.set_gasto_previsto(cur, session["user_id"], valor)

        return jsonify({"status": "ok"})
    except (TypeError, ValueError):
        return _bad_request("Valor inválido.")
    except Exception:
        return _bad_request("Erro ao salvar gasto previsto.", status=500)
