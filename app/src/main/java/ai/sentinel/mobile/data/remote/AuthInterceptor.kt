package ai.sentinel.mobile.data.remote

import ai.sentinel.mobile.data.local.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()

        // 1. Attach Bearer token if present
        sessionManager.accessToken?.let { token ->
            if (token.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
        }

        // 2. Attach organization scope header if tenant selected
        sessionManager.selectedOrganizationId?.let { orgId ->
            if (orgId > 0) {
                requestBuilder.header("X-Organization-ID", orgId.toString())
            }
        }

        // 3. Attach client user agent
        requestBuilder.header("User-Agent", "SentinelMobile/1.0.0 (Android)")
        requestBuilder.header("Accept", "application/json")

        val response = chain.proceed(requestBuilder.build())

        // 4. Handle 401 Unauthorized: invalidate session if server rejects token
        if (response.code == 401 && !originalRequest.url.encodedPath.contains("auth/login")) {
            sessionManager.clearSession()
        }

        return response
    }
}
