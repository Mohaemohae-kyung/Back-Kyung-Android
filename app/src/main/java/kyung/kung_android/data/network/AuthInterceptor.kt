package kyung.kung_android.data.network

import kyung.kung_android.data.auth.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val access = tokenStore.getAccessSync()
        val authorized = if (access.isNullOrBlank()) {
            request
        } else {
            request.newBuilder()
                .header("Authorization", "Bearer $access")
                .build()
        }
        return chain.proceed(authorized)
    }
}
