class UsuarioRepository:
    @staticmethod
    def find_or_create(cur, google_id, email, nome, foto_url):
        cur.execute("""
            INSERT INTO usuarios (google_id, email, nome, foto_url)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (google_id) DO UPDATE SET
                email = EXCLUDED.email,
                nome = EXCLUDED.nome,
                foto_url = EXCLUDED.foto_url
            RETURNING id, nome, foto_url
        """, (google_id, email, nome, foto_url))
        return cur.fetchone()

    @staticmethod
    def get_gasto_previsto(cur, usuario_id):
        cur.execute(
            "SELECT gasto_previsto FROM usuarios WHERE id = %s",
            (usuario_id,)
        )
        row = cur.fetchone()
        return row["gasto_previsto"] if row else 0

    @staticmethod
    def set_gasto_previsto(cur, usuario_id, valor):
        cur.execute(
            "UPDATE usuarios SET gasto_previsto = %s WHERE id = %s",
            (valor, usuario_id)
        )
