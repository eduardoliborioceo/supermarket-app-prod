import time
from flask import Flask, make_response
from werkzeug.middleware.proxy_fix import ProxyFix
from app.config import Config
from app.extensions import get_db, oauth
from app.routes.pages import pages_bp
from app.routes.api import api_bp
from app.routes.auth import auth_bp

STARTUP_VERSION = str(int(time.time()))


def init_db(app):
    with app.app_context():
        conn = get_db()
        cur = conn.cursor()

        cur.execute("""
            CREATE TABLE IF NOT EXISTS usuarios (
                id SERIAL PRIMARY KEY,
                google_id TEXT UNIQUE NOT NULL,
                email TEXT UNIQUE NOT NULL,
                nome TEXT,
                foto_url TEXT,
                criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """)

        cur.execute("""
            CREATE TABLE IF NOT EXISTS produtos (
                id SERIAL PRIMARY KEY,
                nome TEXT NOT NULL,
                setor TEXT NOT NULL,
                ultimo_preco NUMERIC DEFAULT 0,
                usuario_id INTEGER REFERENCES usuarios(id) ON DELETE CASCADE
            );
        """)

        cur.execute("""
            CREATE TABLE IF NOT EXISTS compras (
                id SERIAL PRIMARY KEY,
                produto_id INTEGER REFERENCES produtos(id) ON DELETE CASCADE,
                quantidade NUMERIC DEFAULT 1,
                preco NUMERIC DEFAULT 0,
                comprado BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """)

        # Migração: adiciona usuario_id se a tabela já existia sem ela
        cur.execute("""
            ALTER TABLE produtos
            ADD COLUMN IF NOT EXISTS usuario_id INTEGER
            REFERENCES usuarios(id) ON DELETE CASCADE;
        """)

        # Remove unique constraint antiga (só nome) se ainda existir
        cur.execute("""
            ALTER TABLE produtos
            DROP CONSTRAINT IF EXISTS produtos_nome_key;
        """)

        # Cria unique constraint composta (nome, usuario_id) se não existir
        cur.execute("""
            DO $$ BEGIN
                IF NOT EXISTS (
                    SELECT 1 FROM pg_constraint
                    WHERE conname = 'produtos_nome_usuario_id_key'
                ) THEN
                    ALTER TABLE produtos
                    ADD CONSTRAINT produtos_nome_usuario_id_key
                    UNIQUE (nome, usuario_id);
                END IF;
            END $$;
        """)

        cur.execute("""
            ALTER TABLE usuarios
            ADD COLUMN IF NOT EXISTS supermercado_nome TEXT;
        """)
        cur.execute("""
            ALTER TABLE usuarios
            ADD COLUMN IF NOT EXISTS supermercado_endereco TEXT;
        """)
        cur.execute("""
            ALTER TABLE usuarios
            ADD COLUMN IF NOT EXISTS supermercado_place_id TEXT;
        """)

        conn.commit()
        cur.close()
        conn.close()


def create_app():
    app = Flask(__name__)
    app.config.from_object(Config)

    # Necessário para gerar URLs https:// corretas atrás de proxy (Render, Railway, etc.)
    app.wsgi_app = ProxyFix(app.wsgi_app, x_proto=1, x_host=1)

    oauth.init_app(app)
    oauth.register(
        name="google",
        client_id=app.config["GOOGLE_CLIENT_ID"],
        client_secret=app.config["GOOGLE_CLIENT_SECRET"],
        server_metadata_url="https://accounts.google.com/.well-known/openid-configuration",
        client_kwargs={"scope": "openid email profile"},
    )

    app.register_blueprint(auth_bp)
    app.register_blueprint(pages_bp)
    app.register_blueprint(api_bp)

    @app.route("/sw.js")
    def service_worker():
        sw_content = f"""/* cache version: {STARTUP_VERSION} */
const CACHE_NAME = "supermercado-{STARTUP_VERSION}";

const STATIC_ASSETS = [
  "/static/css/style.css",
  "/static/js/main.js",
  "/static/manifest.webmanifest"
];

self.addEventListener("install", (event) => {{
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(STATIC_ASSETS))
  );
  self.skipWaiting();
}});

self.addEventListener("activate", (event) => {{
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.map((k) => (k !== CACHE_NAME ? caches.delete(k) : null)))
    )
  );
  self.clients.claim();
}});

self.addEventListener("fetch", (event) => {{
  const req = event.request;
  const url = new URL(req.url);

  if (url.origin !== self.location.origin) return;

  if (url.pathname.startsWith("/static/")) {{
    event.respondWith(
      caches.match(req).then((cached) => cached || fetch(req).then((res) => {{
        const copy = res.clone();
        caches.open(CACHE_NAME).then((c) => c.put(req, copy));
        return res;
      }}))
    );
    return;
  }}

  event.respondWith(
    fetch(req)
      .then((res) => {{
        const copy = res.clone();
        caches.open(CACHE_NAME).then((c) => c.put(req, copy));
        return res;
      }})
      .catch(() => caches.match(req).then((cached) => cached || caches.match("/")))
  );
}});
"""
        resp = make_response(sw_content, 200)
        resp.headers["Content-Type"] = "application/javascript"
        resp.headers["Cache-Control"] = "no-store"
        return resp

    try:
        init_db(app)
    except Exception as e:
        print("Erro ao inicializar banco:", e)

    return app


app = create_app()
