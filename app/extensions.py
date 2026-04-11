import psycopg
from psycopg.rows import dict_row
from flask import current_app
from contextlib import contextmanager
from authlib.integrations.flask_client import OAuth

oauth = OAuth()


def get_db():
    return psycopg.connect(
        current_app.config["DATABASE_URL"],
        sslmode="require",
        row_factory=dict_row
    )


@contextmanager
def db_cursor():
    conn = None
    cur = None
    try:
        conn = get_db()
        cur = conn.cursor()
        yield cur
        conn.commit()
    except Exception:
        if conn:
            conn.rollback()
        raise
    finally:
        if cur:
            cur.close()
        if conn:
            conn.close()
