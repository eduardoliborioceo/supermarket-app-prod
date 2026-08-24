package app.supermercado.mobile.core.util

import app.supermercado.mobile.BuildConfig

/** O backend devolve caminhos relativos (ex: "/static/images/icons/produtos/x.jpg"),
 * já que o mesmo campo alimenta o Jinja (home.html/produtos.html) via url_for. */
fun urlImagemProduto(imagemUrl: String?): String? {
    if (imagemUrl.isNullOrBlank()) return null
    return BuildConfig.API_BASE_URL.trimEnd('/') + imagemUrl
}
