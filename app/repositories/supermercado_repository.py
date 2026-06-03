class SupermercadoRepository:
    @staticmethod
    def save(cur, usuario_id, nome, endereco, place_id):
        cur.execute("""
            UPDATE usuarios
            SET supermercado_nome = %s,
                supermercado_endereco = %s,
                supermercado_place_id = %s
            WHERE id = %s
        """, (nome, endereco, place_id, usuario_id))

    @staticmethod
    def get_by_usuario(cur, usuario_id):
        cur.execute("""
            SELECT supermercado_nome, supermercado_endereco, supermercado_place_id
            FROM usuarios WHERE id = %s
        """, (usuario_id,))
        return cur.fetchone()
