import os

class Config:
    DATABASE_URL = (
        os.getenv("DATABASE_URL")
        or os.getenv("DATABASE_PUBLIC_URL")
    )
    SECRET_KEY = os.getenv("SECRET_KEY", "dev-secret-change-in-prod")
    GOOGLE_CLIENT_ID = os.getenv("GOOGLE_CLIENT_ID")
    GOOGLE_CLIENT_SECRET = os.getenv("GOOGLE_CLIENT_SECRET")
