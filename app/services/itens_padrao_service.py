from app.repositories.produto_repository import ProdutoRepository

ITENS_PADRAO = {
    "Alimentos Principais": [
        # Cafés
        "Café Torrado e Moído Santa Clara Tradicional 250g",
        "Café Torrado e Moído Santa Clara Extra Forte 250g",
        "Café Torrado e Moído Santa Clara Premium 250g",
        "Café Torrado e Moído Pilão Tradicional 250g",
        "Café Torrado e Moído Pilão Extra Forte 250g",
        "Café Torrado e Moído 3 Corações Tradicional 250g",
        "Café Torrado e Moído Melitta Tradicional 250g",
        "Café Solúvel Nescafé Tradição 100g",
        # Arroz
        "Arroz Branco Tipo 1 Tio João 1kg",
        "Arroz Branco Tipo 1 Tio João 5kg",
        "Arroz Branco Tipo 1 Camil 1kg",
        "Arroz Branco Tipo 1 Camil 5kg",
        "Arroz Branco Tipo 1 Prato Fino 1kg",
        "Arroz Integral Camil 1kg",
        "Arroz Parboilizado Tio João 1kg",
        # Feijão
        "Feijão Carioca Kicaldo 1kg",
        "Feijão Carioca Camil 1kg",
        "Feijão Preto Kicaldo 1kg",
        "Feijão Preto Camil 1kg",
        "Feijão Fradinho Kicaldo 500g",
        "Feijão Branco Camil 500g",
        # Leites
        "Leite UHT Integral Piracanjuba 1L",
        "Leite UHT Integral Italac 1L",
        "Leite UHT Integral Parmalat 1L",
        "Leite UHT Desnatado Piracanjuba 1L",
        "Leite UHT Semidesnatado Italac 1L",
        "Leite em Pó Integral Ninho 750g",
        "Leite em Pó Integral Itambé 400g",
        # Massas
        "Macarrão Espaguete Renata 500g",
        "Macarrão Espaguete Adria 500g",
        "Macarrão Parafuso Adria 500g",
        "Macarrão Penne Renata 500g",
        "Macarrão Instantâneo Nissin Lámen Galinha Caipira 85g",
        "Macarrão Instantâneo Nissin Lámen Carne 85g",
        # Produtos regionais (Amazonas)
        "Farinha de Mandioca Uarini 1kg",
        "Farinha de Tapioca Granulada 500g",
        "Tucupi Amarelo 500ml",
        "Goma de Tapioca Hidratada 1kg",
        "Polpa de Açaí Congelada 1kg",
        "Polpa de Cupuaçu Congelada 1kg",
        "Polpa de Graviola Congelada 1kg",
    ],
    "Complementos": [
        # Refrigerantes
        "Refrigerante Coca-Cola Original 350ml",
        "Refrigerante Coca-Cola Original 2L",
        "Refrigerante Coca-Cola Zero Açúcar 2L",
        "Refrigerante Guaraná Antarctica 2L",
        "Refrigerante Guaraná Antarctica Zero 2L",
        "Refrigerante Fanta Laranja 2L",
        "Refrigerante Sprite Lemon Fresh 2L",
        # Óleos e Azeites
        "Óleo de Soja Liza 900ml",
        "Óleo de Soja Soya 900ml",
        "Óleo de Soja Concórdia 900ml",
        "Azeite de Oliva Extra Virgem Gallo 500ml",
        "Azeite de Oliva Extra Virgem Andorinha 500ml",
        # Biscoitos
        "Biscoito Recheado Oreo Original 90g",
        "Biscoito Recheado Trakinas Morango 126g",
        "Biscoito Cream Cracker Marilan 350g",
        "Biscoito Água e Sal Vitarella 350g",
        "Wafer Bauducco Chocolate 140g",
        # Chocolates
        "Chocolate ao Leite Lacta 90g",
        "Chocolate ao Leite Nestlé Classic 80g",
        "Chocolate ao Leite Garoto 80g",
        "Bombom Ferrero Rocher 100g",
        "Caixa de Bombons Lacta Favoritos 250g",
    ],
    "Temperos": [
        "Sal Refinado",
        "Sal Grosso",
        "Pimenta-do-Reino Preta Moída",
        "Alho",
        "Cebola",
        "Colorau",
        "Orégano",
        "Cominho",
        "Louro",
        "Coentro",
        "Cheiro-Verde",
        "Tempero Completo",
        "Caldo de Frango Knorr",
        "Caldo de Carne Knorr",
    ],
    "Higiene e Limpeza": [
        # Higiene Pessoal
        "Sabonete Dove Original 90g",
        "Sabonete Lux Buquê de Jasmim 85g",
        "Sabonete Francis Clássico Rosa Branca 90g",
        "Shampoo Seda Ceramidas 325ml",
        "Shampoo Pantene Brilho Extremo 400ml",
        "Creme Dental Colgate Total 12 Clean Mint 90g",
        "Creme Dental Oral-B 123 Menta 70g",
        # Limpeza
        "Detergente Líquido Ypê Neutro 500ml",
        "Detergente Líquido Limpol Neutro 500ml",
        "Água Sanitária Qboa 1L",
        "Desinfetante Pinho Sol Original 500ml",
        "Sabão em Pó OMO Lavagem Perfeita 1,6kg",
        "Sabão em Pó Brilhante Limpeza Total 1,6kg",
        "Amaciante Downy Brisa Suave 1L",
    ],
}


class ItensPadraoService:
    @staticmethod
    def popular_se_novo(cur, usuario_id):
        total = ProdutoRepository.count_by_usuario(cur, usuario_id)
        if total > 0:
            return
        ItensPadraoService._inserir(cur, usuario_id)

    @staticmethod
    def popular_todos(cur, usuario_id):
        ItensPadraoService._inserir(cur, usuario_id)

    @staticmethod
    def _inserir(cur, usuario_id):
        itens = [
            (nome, setor)
            for setor, nomes in ITENS_PADRAO.items()
            for nome in nomes
        ]
        ProdutoRepository.bulk_insert_defaults(cur, usuario_id, itens)
