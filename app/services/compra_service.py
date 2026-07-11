from app.repositories.compra_repository import CompraRepository
from app.repositories.usuario_repository import UsuarioRepository


class CompraService:
    @staticmethod
    def validar(produto_id, quantidade, preco):
        if not produto_id:
            raise ValueError("Produto obrigatório")

        if quantidade is None or float(quantidade) < 0:
            raise ValueError("Quantidade inválida")

        if preco is None or float(preco) < 0:
            raise ValueError("Preço inválido")

    @staticmethod
    def validar_gasto_previsto(valor):
        if valor is None or float(valor) < 0:
            raise ValueError("Valor inválido")

    @staticmethod
    def limpar(cur, usuario_id):
        CompraRepository.clear(cur, usuario_id)
        UsuarioRepository.set_gasto_previsto(cur, usuario_id, 0)
