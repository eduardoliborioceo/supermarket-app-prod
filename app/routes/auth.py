from functools import wraps
from flask import Blueprint, redirect, url_for, session, render_template, current_app
from app.extensions import oauth, db_cursor
from app.repositories.usuario_repository import UsuarioRepository

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

    session["user_id"] = user["id"]
    session["user_nome"] = user["nome"]
    session["user_foto"] = user["foto_url"]

    return redirect(url_for("pages.home"))


@auth_bp.route("/logout")
def logout():
    session.clear()
    return redirect(url_for("auth.login"))
