from functools import wraps
from flask import Blueprint, redirect, url_for, session, render_template, current_app, request, jsonify
from app.extensions import oauth, db_cursor
from app.repositories.usuario_repository import UsuarioRepository
from app.services.itens_padrao_service import ItensPadraoService
from app.services.auth_token_service import AuthTokenService

auth_bp = Blueprint("auth", __name__)


def login_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if "user_id" not in session:
            auth_header = request.headers.get("Authorization", "")
            if auth_header.startswith("Bearer "):
                usuario_id = AuthTokenService.verificar_access_token(auth_header[7:].strip())
                if usuario_id is not None:
                    # Sessão assinada por requisição só pra reaproveitar o
                    # session["user_id"] já usado em toda rota existente —
                    # não representa um cookie de longa duração: o app nativo
                    # nunca guarda cookies, só o Authorization: Bearer.
                    session["user_id"] = usuario_id
                    return f(*args, **kwargs)
            if request.path.startswith("/api/"):
                return jsonify({"status": "error", "message": "Não autenticado."}), 401
            return redirect(url_for("auth.login"))
        return f(*args, **kwargs)
    return decorated


@auth_bp.route("/login")
def login():
    if "user_id" in session:
        return redirect(url_for("pages.home"))
    return render_template("login.html")


@auth_bp.route("/auth/google")
def google_login():
    # GOOGLE_REDIRECT_URI pode ser definida como variável de ambiente para
    # garantir a URI exata registrada no Google Console (ex: em produção)
    import os
    redirect_uri = (
        os.getenv("GOOGLE_REDIRECT_URI")
        or url_for("auth.google_callback", _external=True)
    )
    # Marca a sessão (mesmo cookie que sobrevive ao round-trip até o Google e
    # volta no callback) quando a Custom Tab do app nativo iniciou o fluxo,
    # pra callback saber se deve devolver um deep link em vez do redirect web.
    if request.args.get("client") == "android":
        session["oauth_client"] = "android"
    return oauth.google.authorize_redirect(redirect_uri)


@auth_bp.route("/auth/google/callback")
def google_callback():
    token = oauth.google.authorize_access_token()
    user_info = token.get("userinfo")

    if not user_info:
        if session.pop("oauth_client", None) == "android":
            return redirect("app.supermercado.mobile://oauth-callback?error=login_failed")
        return redirect(url_for("auth.login"))

    with db_cursor() as cur:
        user = UsuarioRepository.find_or_create(
            cur,
            google_id=user_info["sub"],
            email=user_info["email"],
            nome=user_info.get("name", ""),
            foto_url=user_info.get("picture", ""),
        )
        ItensPadraoService.popular_se_novo(cur, user["id"])

    session["user_id"] = user["id"]
    session["user_nome"] = user["nome"]
    session["user_foto"] = user["foto_url"]

    with db_cursor() as cur:
        from app.repositories.supermercado_repository import SupermercadoRepository
        supermercado = SupermercadoRepository.get_by_usuario(cur, user["id"])

    if supermercado and supermercado.get("supermercado_nome"):
        session["supermercado_nome"] = supermercado["supermercado_nome"]
        session["supermercado_endereco"] = supermercado.get("supermercado_endereco", "")

    if session.pop("oauth_client", None) == "android":
        with db_cursor() as cur:
            codigo = AuthTokenService.gerar_codigo_troca(cur, user["id"])
        return redirect(f"app.supermercado.mobile://oauth-callback?code={codigo}")

    return redirect(url_for("pages.selecionar_supermercado"))


@auth_bp.route("/logout")
def logout():
    session.clear()
    return redirect(url_for("auth.login"))
