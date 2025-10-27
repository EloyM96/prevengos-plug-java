package com.prevengos.plug.android.di

import android.content.Context
import com.prevengos.plug.android.data.local.PrevengosDatabase
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi
import com.prevengos.plug.android.data.repository.CuestionarioRepository
import com.prevengos.plug.android.data.repository.PacienteRepository
import com.prevengos.plug.android.data.repository.SyncRepository
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class AppContainer(context: Context) {
    private val database: PrevengosDatabase = PrevengosDatabase.instance(context)

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(MoshiConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    private val syncApi: PrevengosSyncApi by lazy {
        retrofit.create(PrevengosSyncApi::class.java)
    }

    val pacienteRepository: PacienteRepository by lazy {
        PacienteRepository(database.pacienteDao())
    }

    val cuestionarioRepository: CuestionarioRepository by lazy {
        CuestionarioRepository(database.cuestionarioDao())
    }

    val syncRepository: SyncRepository by lazy {
        SyncRepository(
            pacienteDao = database.pacienteDao(),
            cuestionarioDao = database.cuestionarioDao(),
            syncMetadataDao = database.syncMetadataDao(),
            syncApi = syncApi,
            ioDispatcher = Dispatchers.IO
        )
    }

    companion object {
        private const val BASE_URL = "https://api.prevengos.test/"
    }
}
