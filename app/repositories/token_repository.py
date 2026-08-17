class TokenRepository:
    @staticmethod
    def save_refresh_token(cur, usuario_id, token_hash, expira_em):
        cur.execute("""
            INSERT INTO refresh_tokens (usuario_id, token_hash, expira_em)
            VALUES (%s, %s, %s)
        """, (usuario_id, token_hash, expira_em))

    @staticmethod
    def find_active_refresh_token(cur, token_hash):
        cur.execute("""
            SELECT id, usuario_id, expira_em
            FROM refresh_tokens
            WHERE token_hash = %s AND revogado_em IS NULL AND expira_em > NOW()
        """, (token_hash,))
        return cur.fetchone()

    @staticmethod
    def revoke_refresh_token(cur, token_hash):
        cur.execute("""
            UPDATE refresh_tokens SET revogado_em = NOW()
            WHERE token_hash = %s AND revogado_em IS NULL
        """, (token_hash,))

    @staticmethod
    def save_exchange_code(cur, usuario_id, code_hash, expira_em):
        cur.execute("""
            INSERT INTO oauth_exchange_codes (usuario_id, code_hash, expira_em)
            VALUES (%s, %s, %s)
        """, (usuario_id, code_hash, expira_em))

    @staticmethod
    def consume_exchange_code(cur, code_hash):
        cur.execute("""
            UPDATE oauth_exchange_codes SET usado_em = NOW()
            WHERE code_hash = %s AND usado_em IS NULL AND expira_em > NOW()
            RETURNING usuario_id
        """, (code_hash,))
        row = cur.fetchone()
        return row["usuario_id"] if row else None
