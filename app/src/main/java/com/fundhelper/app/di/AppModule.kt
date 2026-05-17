package com.fundhelper.app.di

import android.content.Context
import androidx.room.Room
import com.fundhelper.app.data.api.FundApi
import com.fundhelper.app.data.db.AppDatabase
import com.fundhelper.app.data.db.FundDao
import com.fundhelper.app.data.db.GroupDao
import com.fundhelper.app.data.db.IndexDao
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(Double::class.java, object : JsonAdapter<Double>() {
            override fun fromJson(reader: JsonReader): Double? {
                return when (reader.peek()) {
                    JsonReader.Token.NUMBER -> reader.nextDouble()
                    JsonReader.Token.STRING -> reader.nextString().toDoubleOrNull()
                    JsonReader.Token.NULL -> { reader.nextNull(); null }
                    else -> throw JsonDataException("Expected double but got ${reader.peek()}")
                }
            }
            override fun toJson(writer: JsonWriter, value: Double?) {
                value?.let { writer.value(it) } ?: writer.nullValue()
            }
        }.nullSafe())
        .addLast(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl("https://fundmobapi.eastmoney.com/")
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun provideFundApi(retrofit: Retrofit): FundApi = retrofit.create(FundApi::class.java)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "fund_helper.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideFundDao(db: AppDatabase): FundDao = db.fundDao()

    @Provides
    fun provideIndexDao(db: AppDatabase): IndexDao = db.indexDao()

    @Provides
    fun provideGroupDao(db: AppDatabase): GroupDao = db.groupDao()
}
