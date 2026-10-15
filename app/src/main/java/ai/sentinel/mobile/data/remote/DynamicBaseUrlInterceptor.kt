package ai.sentinel.mobile.data.remote

import ai.sentinel.mobile.data.local.SessionManager
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response

class DynamicBaseUrlInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val configuredUrlStr = sessionManager.serverBaseUrl
        val configuredHttpUrl = configuredUrlStr.toHttpUrlOrNull()

        if (configuredHttpUrl != null) {
            val newUrl = request.url.newBuilder()
                .scheme(configuredHttpUrl.scheme)
                .host(configuredHttpUrl.host)
                .port(configuredHttpUrl.port)
                .build()
            request = request.newBuilder().url(newUrl).build()
        }

        return chain.proceed(request)
    }
}
