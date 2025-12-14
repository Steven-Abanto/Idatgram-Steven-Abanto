package pe.edu.idat.dsi.dami.idatgram.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.edu.idat.dsi.dami.idatgram.data.remote.IdatgramApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * ⚠️ REEMPLAZA este BASE_URL con el de tu My JSON Server.
     * Debe terminar con "/"
     *
     * Ejemplo típico:
     * https://my-json-server.typicode.com/USUARIO/REPO/
     */
    private const val BASE_URL = "https://my-json-server.typicode.com/Steven-Abanto/Idatgram-database/"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // BODY para ver requests/responses en Logcat durante desarrollo
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): IdatgramApiService {
        return retrofit.create(IdatgramApiService::class.java)
    }
}
