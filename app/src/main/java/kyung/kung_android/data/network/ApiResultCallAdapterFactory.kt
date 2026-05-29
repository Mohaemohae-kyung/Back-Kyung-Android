package kyung.kung_android.data.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Retrofit 인터페이스가 `suspend fun foo(): T`로 선언돼 있어도
 * 내부적으로 `ApiResponse<T>` 응답을 받아 `result`만 꺼내 반환하도록 처리한다.
 * 실패 응답(isSuccess=false 또는 HTTP non-2xx)은 [ApiException] 발생.
 */
class ApiResultCallAdapterFactory(private val json: Json) : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit,
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Call::class.java) return null
        if (returnType !is ParameterizedType) return null

        // 외부/프록시 응답처럼 ApiResponse 형식이 아닌 경우 wrap 우회
        if (annotations.any { it is RawResponse }) return null

        val responseType = getParameterUpperBound(0, returnType)
        // 이미 ApiResponse<T>로 선언된 경우는 건드리지 않음 (raw 접근 원하는 경우 대비)
        if (responseType is ParameterizedType && responseType.rawType == ApiResponse::class.java) {
            return null
        }
        if (responseType is Class<*> && responseType == ApiResponse::class.java) {
            return null
        }

        val wrappedType = parameterized(ApiResponse::class.java, responseType)
        return ApiResultCallAdapter<Any?>(wrappedType, json)
    }

    private fun parameterized(raw: Class<*>, vararg args: Type): ParameterizedType {
        return object : ParameterizedType {
            override fun getActualTypeArguments(): Array<Type> = arrayOf(*args)
            override fun getRawType(): Type = raw
            override fun getOwnerType(): Type? = null
        }
    }
}

private class ApiResultCallAdapter<T>(
    private val wrappedType: Type,
    private val json: Json,
) : CallAdapter<ApiResponse<T>, Call<T>> {

    override fun responseType(): Type = wrappedType

    override fun adapt(call: Call<ApiResponse<T>>): Call<T> = ApiResultCall(call, json)
}

private class ApiResultCall<T>(
    private val delegate: Call<ApiResponse<T>>,
    private val json: Json,
) : Call<T> {

    override fun enqueue(callback: Callback<T>) {
        delegate.enqueue(object : Callback<ApiResponse<T>> {
            override fun onResponse(call: Call<ApiResponse<T>>, response: Response<ApiResponse<T>>) {
                try {
                    callback.onResponse(this@ApiResultCall, unwrap(response))
                } catch (t: Throwable) {
                    callback.onFailure(this@ApiResultCall, t)
                }
            }

            override fun onFailure(call: Call<ApiResponse<T>>, t: Throwable) {
                callback.onFailure(this@ApiResultCall, t)
            }
        })
    }

    override fun execute(): Response<T> = unwrap(delegate.execute())

    private fun unwrap(response: Response<ApiResponse<T>>): Response<T> {
        if (response.isSuccessful) {
            val body = response.body()
                ?: throw ApiException(
                    ApiException.CODE_EMPTY_BODY,
                    "응답 본문이 비어있습니다.",
                    null,
                    response.code(),
                )
            if (body.isSuccess) {
                @Suppress("UNCHECKED_CAST")
                val safeResult: T = body.result ?: (Unit as T)
                return Response.success(safeResult, response.raw())
            }
            throw ApiException(body.code, body.message, null, response.code())
        } else {
            throw parseErrorBody(response.errorBody()?.string(), response.code())
        }
    }

    private fun parseErrorBody(text: String?, httpStatus: Int): ApiException {
        if (text.isNullOrBlank()) {
            return ApiException(ApiException.CODE_UNKNOWN, "HTTP $httpStatus", null, httpStatus)
        }
        return try {
            val root: JsonObject = json.parseToJsonElement(text).jsonObject
            val code = root["code"]?.jsonPrimitive?.contentOrNull ?: ApiException.CODE_UNKNOWN
            val message = root["message"]?.jsonPrimitive?.contentOrNull ?: "HTTP $httpStatus"
            val fieldErrors = if (code == "COMMON_400") parseFieldErrors(root) else null
            ApiException(code, message, fieldErrors, httpStatus)
        } catch (t: Throwable) {
            ApiException(ApiException.CODE_UNKNOWN, "HTTP $httpStatus", null, httpStatus)
        }
    }

    private fun parseFieldErrors(root: JsonObject): Map<String, String>? {
        val resultEl = root["result"] ?: return null
        if (resultEl !is JsonObject) return null
        return runCatching {
            resultEl.entries.associate { (k, v) -> k to (v.jsonPrimitive.contentOrNull ?: "") }
        }.getOrNull()
    }

    override fun isExecuted(): Boolean = delegate.isExecuted
    override fun cancel() = delegate.cancel()
    override fun isCanceled(): Boolean = delegate.isCanceled

    @Suppress("UNCHECKED_CAST")
    override fun clone(): Call<T> = ApiResultCall(delegate.clone(), json)

    override fun request(): Request = delegate.request()
    override fun timeout(): Timeout = delegate.timeout()
}

class NetworkIoException(cause: Throwable) : IOException(cause)
