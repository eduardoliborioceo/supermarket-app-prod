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
