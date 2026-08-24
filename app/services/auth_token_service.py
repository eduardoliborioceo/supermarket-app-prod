import hashlib
import secrets
from datetime import datetime, timedelta, timezone

import jwt
from flask import current_app
from google.auth.transport import requests as google_requests
from google.oauth2 import id_token as google_id_token

from app.repositories.token_repository import TokenRepository

ACCESS_TOKEN_TTL = timedelta(minutes=15)
REFRESH_TOKEN_TTL = timedelta(days=60)
EXCHANGE_CODE_TTL = timedelta(minutes=2)


class AuthTokenService:
    @staticmethod
    def _hash(token):
        return hashlib.sha256(token.encode("utf-8")).hexdigest()

    @staticmethod
    def gerar_access_token(usuario_id):
        agora = datetime.now(timezone.utc)
        payload = {
            "sub": str(usuario_id),
            "iat": agora,
            "exp": agora + ACCESS_TOKEN_TTL,
        }
        return jwt.encode(payload, current_app.config["SECRET_KEY"], algorithm="HS256")

    @staticmethod
    def verificar_access_token(token):
        try:
            payload = jwt.decode(token, current_app.config["SECRET_KEY"], algorithms=["HS256"])
            return int(payload["sub"])
        except (jwt.PyJWTError, KeyError, ValueError, TypeError):
            return None

    @staticmethod
    def emitir_par_tokens(cur, usuario_id):
        refresh_token = secrets.token_urlsafe(48)
        expira_em = datetime.now(timezone.utc) + REFRESH_TOKEN_TTL
        TokenRepository.save_refresh_token(cur, usuario_id, AuthTokenService._hash(refresh_token), expira_em)

        access_token = AuthTokenService.gerar_access_token(usuario_id)
        return {
            "access_token": access_token,
            "refresh_token": refresh_token,
            "expires_in": int(ACCESS_TOKEN_TTL.total_seconds()),
        }

    @staticmethod
    def renovar_par_tokens(cur, refresh_token):
        token_hash = AuthTokenService._hash(refresh_token)
        row = TokenRepository.find_active_refresh_token(cur, token_hash)
        if not row:
            return None

        # Rotaciona o refresh token a cada uso: o antigo é revogado e um novo
        # é emitido, para limitar o dano caso um refresh token vaze.
        TokenRepository.revoke_refresh_token(cur, token_hash)
        return AuthTokenService.emitir_par_tokens(cur, row["usuario_id"])

    @staticmethod
    def revogar_refresh_token(cur, refresh_token):
        TokenRepository.revoke_refresh_token(cur, AuthTokenService._hash(refresh_token))

    @staticmethod
    def verificar_id_token_google(id_token_str):
        """Valida um ID token do Credential Manager (login nativo Android) —
        confere assinatura, expiração e que o audience bate com o client OAuth
        Web (GOOGLE_CLIENT_ID), o mesmo que o fluxo Authlib/browser já usa.
        Retorna as claims (sub/email/name/picture) ou None se inválido."""
        try:
            return google_id_token.verify_oauth2_token(
                id_token_str,
                google_requests.Request(),
                current_app.config["GOOGLE_CLIENT_ID"],
            )
        except ValueError:
            return None

    @staticmethod
    def gerar_codigo_troca(cur, usuario_id):
        codigo = secrets.token_urlsafe(32)
        expira_em = datetime.now(timezone.utc) + EXCHANGE_CODE_TTL
        TokenRepository.save_exchange_code(cur, usuario_id, AuthTokenService._hash(codigo), expira_em)
        return codigo

    @staticmethod
    def trocar_codigo(cur, codigo):
        return TokenRepository.consume_exchange_code(cur, AuthTokenService._hash(codigo))
