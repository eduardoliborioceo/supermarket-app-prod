from app.repositories.produto_repository import ProdutoRepository

ITENS_PADRAO = {
    "Principais": [
        "Bife de Carne em Kg",
        "Carne Moída (Picadinho) em Kg",
        "Carne para Hambúrguer a Und",
        "Cartela de Ovos (30 Und)",
        "Coxa e Sobrecoxa em Kg",
        "Coxão Mole (Isca) em Kg",
        "Filé de Frango em Kg",
        "Frango Empanado a Und",
        "Hambúrguer Bovino a Und",
        "Linguiça Toscana em Kg",
        "Nuggets Pct 300g",
        "Ovos Brancos (Dúzia)",
        "Ovos Vermelhos (Dúzia)",
        "Peito de Frango em Kg",
        "Peixe Filé em Kg",
        "Salsicha em Kg",
    ],
    "Complementos": [
        "Achocolatado Pct 400g",
        "Açúcar Cristal Pct 1Kg",
        "Açúcar Refinado Pct 1Kg",
        "Açúcar Pct 1Kg",
        "Arroz Branco Pct 1Kg",
        "Arroz Branco Pct 5Kg",
        "Arroz Parboilizado Pct 1Kg",
        "Azeite de Oliva 500ml",
        "Café Solúvel Pct 120g",
        "Café Tradicional Pct 500g",
        "Café Tradicional Pct 1Kg",
        "Farinha Branca Pct 1Kg",
        "Farinha Pct 1Kg",
        "Feijão Carioca Pct 1Kg",
        "Feijão Preto Pct 1Kg",
        "Lasanha Massa Pct 500g",
        "Leite Desnatado 1L",
        "Leite em Pó 400g",
        "Leite Integral 1L",
        "Lentilha Pct 500g",
        "Macarrão Espaguete Pct 1Kg",
        "Macarrão Instantâneo Pct 80g",
        "Macarrão Parafuso Pct 500g",
        "Macarrão Penne Pct 500g",
        "Margarina 500g",
        "Manteiga 200g",
        "Milho para Pipoca Pct 500g",
        "Óleo de Soja 900ml",
        "Queijo Ralado 50g",
    ],
    "Temperos": [
        "Açafrão (Cúrcuma) 100g",
        "Alho Descascado Pct 200g",
        "Alho Kg",
        "Caldo de Carne Caixa",
        "Caldo de Galinha Caixa",
        "Cebola Kg",
        "Chimichurri 50g",
        "Colorau 100g",
        "Cominho 100g",
        "Extrato de Tomate 340g",
        "Ketchup 400g",
        "Maionese 500g",
        "Molho de Tomate Sachê 300g",
        "Mostarda 200g",
        "Orégano 50g",
        "Páprica Defumada 100g",
        "Páprica Doce 100g",
        "Pimenta-do-Reino 50g",
        "Sal Grosso Pct 1Kg",
        "Sal Refinado Pct 1Kg",
        "Shoyu 150ml",
        "Tempero Baiano 100g",
        "Vinagre 750ml",
    ],
    "Saúde e Higiene": [
        "Algodão Pct",
        "Álcool em Gel 500ml",
        "Amaciante 2L",
        "Água Sanitária 1L",
        "Bombril Pct",
        "Condicionador 350ml",
        "Cotonetes Pct",
        "Creme Dental 90g",
        "Desinfetante 2L",
        "Desodorante Aerosol",
        "Desodorante Roll-on",
        "Detergente 500ml",
        "Escova Dental Und",
        "Esponja de Louça Und",
        "Fio Dental Und",
        "Limpador Multiuso 500ml",
        "Pano de Chão Und",
        "Papel Higiênico Pct 4 Rolos",
        "Papel Higiênico Pct 12 Rolos",
        "Papel Toalha Pct",
        "Sabão em Pó 1Kg",
        "Sabão Líquido 1L",
        "Sabonete 90g",
        "Saco de Lixo 30L",
        "Saco de Lixo 50L",
        "Saco de Lixo 100L",
        "Shampoo 350ml",
    ],
    "Bebidas": [
        "Água de Coco 1L",
        "Água Mineral 500ml",
        "Água Mineral 1,5L",
        "Água Mineral 20L",
        "Chá Gelado 1,5L",
        "Coca-Cola 350ml",
        "Coca-Cola 2L",
        "Energético 250ml",
        "Fanta Laranja 2L",
        "Guaraná 2L",
        "Iogurte 170g",
        "Iogurte 1L",
        "Isotônico 500ml",
        "Sprite 2L",
        "Suco de Caixinha 200ml",
        "Suco de Laranja 1L",
        "Suco de Maracujá 1L",
        "Suco de Uva 1L",
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
