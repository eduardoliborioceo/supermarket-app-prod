from app.repositories.usuario_repository import UsuarioRepository
from app.services.itens_padrao_service import ItensPadraoService


class UsuarioService:
    @staticmethod
    def autenticar_google(cur, google_id, email, nome, foto_url):
        user = UsuarioRepository.find_or_create(
            cur,
            google_id=google_id,
            email=email,
            nome=nome,
            foto_url=foto_url,
        )
        ItensPadraoService.popular_se_novo(cur, user["id"])
        return user
