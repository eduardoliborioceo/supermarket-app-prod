class CompraRepository:
    @staticmethod
    def get_carrinho(cur, usuario_id):
        cur.execute("""
            SELECT produto_id, quantidade, preco
            FROM compras
            WHERE usuario_id = %s
        """, (usuario_id,))
        return cur.fetchall()

    @staticmethod
    def upsert_item(cur, produto_id, usuario_id, quantidade, preco):
        cur.execute("""
            INSERT INTO compras (produto_id, usuario_id, quantidade, preco)
            SELECT id, usuario_id, %s, %s
            FROM produtos
            WHERE id = %s AND usuario_id = %s
            ON CONFLICT (produto_id, usuario_id) DO UPDATE SET
                quantidade = EXCLUDED.quantidade,
                preco = EXCLUDED.preco
        """, (quantidade, preco, produto_id, usuario_id))
        return cur.rowcount

    @staticmethod
    def clear(cur, usuario_id):
        cur.execute("DELETE FROM compras WHERE usuario_id = %s", (usuario_id,))
