package com.prevengos.plug.android.di;

import android.content.Context;

import com.prevengos.plug.android.data.local.PrevengosDatabase;
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi;
import com.prevengos.plug.android.data.repository.CuestionarioRepository;
import com.prevengos.plug.android.data.repository.PacienteRepository;
import com.prevengos.plug.android.data.repository.SyncRepository;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class AppContainer {
    private static final String BASE_URL = "https://api.prevengos.test/";

    private final PrevengosDatabase database;
    private OkHttpClient okHttpClient;
    private Retrofit retrofit;
    private PrevengosSyncApi syncApi;
    private PacienteRepository pacienteRepository;
    private CuestionarioRepository cuestionarioRepository;
    private SyncRepository syncRepository;

    public AppContainer(Context context) {
        this.database = PrevengosDatabase.instance(context);
    }

    private OkHttpClient getOkHttpClient() {
        if (okHttpClient == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
            okHttpClient = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();
        }
        return okHttpClient;
    }

    private Retrofit getRetrofit() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(MoshiConverterFactory.create())
                    .client(getOkHttpClient())
                    .build();
        }
        return retrofit;
    }

    private PrevengosSyncApi getSyncApi() {
        if (syncApi == null) {
            syncApi = getRetrofit().create(PrevengosSyncApi.class);
        }
        return syncApi;
    }

    public PacienteRepository getPacienteRepository() {
        if (pacienteRepository == null) {
            pacienteRepository = new PacienteRepository(database.pacienteDao());
        }
        return pacienteRepository;
    }

    public CuestionarioRepository getCuestionarioRepository() {
        if (cuestionarioRepository == null) {
            cuestionarioRepository = new CuestionarioRepository(database.cuestionarioDao());
        }
        return cuestionarioRepository;
    }

    public SyncRepository getSyncRepository() {
        if (syncRepository == null) {
            syncRepository = new SyncRepository(
                    database.pacienteDao(),
                    database.cuestionarioDao(),
                    database.syncMetadataDao(),
                    getSyncApi()
            );
        }
        return syncRepository;
    }
}
