package com.prevengos.plug.android.di;

import android.content.Context;

import com.prevengos.plug.android.BuildConfig;
import com.prevengos.plug.android.data.local.PrevengosDatabase;
import com.prevengos.plug.android.data.remote.api.PrevengosSyncApi;
import com.prevengos.plug.android.data.repository.CuestionarioRepository;
import com.prevengos.plug.android.data.repository.PacienteRepository;
import com.prevengos.plug.android.data.repository.SyncRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class AppContainer {
    private static final String BASE_URL = "https://api.prevengos.test/";

    private final PrevengosDatabase database;
    private final OkHttpClient okHttpClient;
    private final Retrofit retrofit;
    private final PrevengosSyncApi syncApi;
    private final ExecutorService ioExecutor;

    private final PacienteRepository pacienteRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final SyncRepository syncRepository;

    public AppContainer(Context context) {
        database = PrevengosDatabase.instance(context);
        ioExecutor = Executors.newSingleThreadExecutor();
        okHttpClient = buildHttpClient();
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(MoshiConverterFactory.create())
                .client(okHttpClient)
                .build();
        syncApi = retrofit.create(PrevengosSyncApi.class);
        pacienteRepository = new PacienteRepository(database.pacienteDao());
        cuestionarioRepository = new CuestionarioRepository(database.cuestionarioDao());
        syncRepository = new SyncRepository(
                database.pacienteDao(),
                database.cuestionarioDao(),
                database.syncMetadataDao(),
                syncApi,
                BuildConfig.APPLICATION_ID);
    }

    private OkHttpClient buildHttpClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .build();
    }

    public PacienteRepository getPacienteRepository() {
        return pacienteRepository;
    }

    public CuestionarioRepository getCuestionarioRepository() {
        return cuestionarioRepository;
    }

    public SyncRepository getSyncRepository() {
        return syncRepository;
    }

    public ExecutorService getIoExecutor() {
        return ioExecutor;
    }
}
