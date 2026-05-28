package kyung.kung_android.data.chatbot.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kyung.kung_android.BuildConfig
import kyung.kung_android.data.chatbot.api.ChatBotApi
import kyung.kung_android.data.network.di.AuthClient
import okhttp3.OkHttpClient
import retrofit2.Converter
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ChatBotApiModule {

    // LLM 응답은 ApiResponse 래퍼가 아닌 {reply} 직접 형식이라,
    // ApiResponse unwrap(callAdapter) 없이 LlmChatResponse를 그대로 역직렬화한다.
    @Provides
    @Singleton
    fun provideChatBotApi(
        @AuthClient client: OkHttpClient,
        converterFactory: Converter.Factory,
    ): ChatBotApi = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(converterFactory)
        .build()
        .create(ChatBotApi::class.java)
}
