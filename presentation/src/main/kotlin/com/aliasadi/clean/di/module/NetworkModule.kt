package com.aliasadi.clean.di.module

import android.content.SharedPreferences
import android.util.Log
import com.aliasadi.core.di.AppSettingsSharedPreference
import com.aliasadi.data.BuildConfig
import com.aliasadi.data.api.MovieApi
import com.aliasadi.data.auth.ITokenProvider
import com.aliasadi.data.auth.SharedPrefsTokenProvider
import com.aliasadi.data.remote.http.AuthInterceptor
import com.aliasadi.iam.client.api.IamServiceApi
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/**
 * Created by Ali Asadi on 15/05/2020
 **/
@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl(BuildConfig.BASE_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideTokenProvider(
        @AppSettingsSharedPreference prefs: SharedPreferences
    ): ITokenProvider {
        return SharedPrefsTokenProvider(prefs)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenProvider: ITokenProvider): OkHttpClient {

        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Log.d("HTTP", message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideMovieApi(retrofit: Retrofit): MovieApi {
        return retrofit.create(MovieApi::class.java)
    }

    @Provides
    @Singleton
    fun provideIamHttpApi(
        retrofit: Retrofit
    ): IamServiceApi {
        return retrofit.create(IamServiceApi::class.java)
    }

}