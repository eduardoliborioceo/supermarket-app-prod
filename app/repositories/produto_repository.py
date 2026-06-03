class ProdutoRepository:
    @staticmethod
    def list_all(cur, usuario_id):
        cur.execute("""
            SELECT id, nome, setor, ultimo_preco
            FROM produtos
            WHERE usuario_id = %s
            ORDER BY setor, nome
        """, (usuario_id,))
        return cur.fetchall()

    @staticmethod
    def upsert_by_name(cur, nome, setor, preco, usuario_id):
        cur.execute("""
            INSERT INTO produtos (nome, setor, ultimo_preco, usuario_id)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (nome, usuario_id) DO UPDATE SET
                setor = EXCLUDED.setor,
                ultimo_preco = EXCLUDED.ultimo_preco
        """, (nome, setor, preco, usuario_id))

    @staticmethod
    def update(cur, produto_id, nome, setor, preco, usuario_id):
        cur.execute("""
            UPDATE produtos
            SET nome = %s,
                setor = %s,
                ultimo_preco = %s
            WHERE id = %s AND usuario_id = %s
        """, (nome, setor, preco, produto_id, usuario_id))

    @staticmethod
    def delete(cur, produto_id, usuario_id):
        cur.execute(
            "DELETE FROM produtos WHERE id = %s AND usuario_id = %s",
            (produto_id, usuario_id)
        )

    @staticmethod
    def count_by_usuario(cur, usuario_id):
        cur.execute(
            "SELECT COUNT(*) AS total FROM produtos WHERE usuario_id = %s",
            (usuario_id,)
        )
        row = cur.fetchone()
        return row["total"] if row else 0

    @staticmethod
    def bulk_insert_defaults(cur, usuario_id, itens):
        cur.executemany("""
            INSERT INTO produtos (nome, setor, ultimo_preco, usuario_id)
            VALUES (%s, %s, 0, %s)
            ON CONFLICT (nome, usuario_id) DO NOTHING
        """, [(nome, setor, usuario_id) for nome, setor in itens])
