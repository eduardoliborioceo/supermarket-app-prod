import requests
from flask import current_app

PLACES_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json"
PLACES_NEARBY_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json"


class SupermercadoService:
    @staticmethod
    def buscar_por_texto(query, lat=None, lng=None):
        api_key = current_app.config.get("GOOGLE_MAPS_API_KEY")
        if not api_key:
            return [], "sem_chave"

        params = {
            "query": f"supermercado {query}".strip(),
            "language": "pt-BR",
            "key": api_key,
        }
        if lat and lng:
            params["location"] = f"{lat},{lng}"
            params["radius"] = 10000

        try:
            resp = requests.get(PLACES_TEXT_SEARCH_URL, params=params, timeout=6)
            resp.raise_for_status()
            data = resp.json()
        except Exception as e:
            return [], str(e)

        status = data.get("status", "UNKNOWN")
        if status not in ("OK", "ZERO_RESULTS"):
            return [], status

        return SupermercadoService._normalizar(data.get("results", [])), status

    @staticmethod
    def buscar_por_localizacao(lat, lng):
        api_key = current_app.config.get("GOOGLE_MAPS_API_KEY")
        if not api_key:
            return [], "sem_chave"

        params = {
            "location": f"{lat},{lng}",
            "radius": 5000,
            "type": "supermarket",
            "language": "pt-BR",
            "key": api_key,
        }

        try:
            resp = requests.get(PLACES_NEARBY_URL, params=params, timeout=6)
            resp.raise_for_status()
            data = resp.json()
        except Exception as e:
            return [], str(e)

        status = data.get("status", "UNKNOWN")
        if status not in ("OK", "ZERO_RESULTS"):
            return [], status

        return SupermercadoService._normalizar(data.get("results", [])), status

    @staticmethod
    def _normalizar(places):
        results = []
        for p in places[:12]:
            results.append({
                "place_id": p.get("place_id", ""),
                "nome": p.get("name", ""),
                "endereco": p.get("formatted_address") or p.get("vicinity", ""),
                "rating": p.get("rating"),
                "aberto": (
                    p.get("opening_hours", {}).get("open_now")
                    if p.get("opening_hours") else None
                ),
            })
        return results
