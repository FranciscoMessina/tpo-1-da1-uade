package com.da_grupo9.ronda.di;

import com.da_grupo9.ronda.BuildConfig;
import com.da_grupo9.ronda.data.remote.PublicacionApi;
import com.da_grupo9.ronda.data.remote.AuthApi;
import com.da_grupo9.ronda.data.remote.ProfileApi;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public final class NetworkModule {

    private NetworkModule() {
    }

    @Provides
    @Singleton
    static OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    private final List<Cookie> cookies = new ArrayList<>();

                    @Override public synchronized void saveFromResponse(HttpUrl url, List<Cookie> received) {
                        cookies.removeIf(saved -> received.stream().anyMatch(next -> next.name().equals(saved.name())));
                        cookies.addAll(received);
                    }

                    @Override public synchronized List<Cookie> loadForRequest(HttpUrl url) {
                        cookies.removeIf(cookie -> cookie.expiresAt() < System.currentTimeMillis());
                        List<Cookie> matching = new ArrayList<>();
                        for (Cookie cookie : cookies) if (cookie.matches(url)) matching.add(cookie);
                        return matching;
                    }
                })
                .build();
    }

    @Provides
    @Singleton
    static Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Provides
    @Singleton
    static PublicacionApi providePublicacionApi(Retrofit retrofit) {
        return retrofit.create(PublicacionApi.class);
    }

    @Provides @Singleton static AuthApi provideAuthApi(Retrofit retrofit) {
        return retrofit.create(AuthApi.class);
    }

    @Provides @Singleton static ProfileApi provideProfileApi(Retrofit retrofit) {
        return retrofit.create(ProfileApi.class);
    }
}
