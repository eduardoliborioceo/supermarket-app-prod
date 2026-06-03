from functools import wraps
from flask import Blueprint, redirect, url_for, session, render_template, current_app
from app.extensions import oauth, db_cursor
from app.repositories.usuario_repository import UsuarioRepository
from app.services.itens_padrao_service import ItensPadraoService

auth_bp = Blueprint("auth", __name__)


def login_required(f):
    @wraps(f)
    def decorated(*args, **kwargs):
        if "user_id" not in session:
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
    return oauth.google.authorize_redirect(redirect_uri)


@auth_bp.route("/auth/google/callback")
def google_callback():
    token = oauth.google.authorize_access_token()
    user_info = token.get("userinfo")

    if not user_info:
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

    return redirect(url_for("pages.selecionar_supermercado"))


@auth_bp.route("/logout")
def logout():
    session.clear()
    return redirect(url_for("auth.login"))
