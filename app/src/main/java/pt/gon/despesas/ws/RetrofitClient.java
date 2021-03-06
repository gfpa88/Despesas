package pt.gon.despesas.ws;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.util.Date;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import pt.gon.despesas.adapter.Preferences;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance;

    private Retrofit client;
    private Retrofit client2;

    public synchronized static RetrofitClient getInstance() {
        if(instance == null) {
            synchronized (RetrofitClient.class ){
                if (instance == null){
                    instance = new RetrofitClient();
                }
            }
        }
        return instance;
    }

    public RetrofitClient() {
        if(client == null) {
            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            // add logging as last interceptor
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            // set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);

            client = new Retrofit.Builder()
                    .baseUrl("http://mpapi.ml/apinew/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();

            client2 = new Retrofit.Builder()
                    .baseUrl("http://mpapi.ml/apinew/")
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
    }
    @SuppressLint("CheckResult")
    public  void getMovimentos(String id, final ApiCallBack callback) {
        client.create(ApiService.class).getMovimentos(id,"Movimentos")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(callObserver(callback));
    }

    @SuppressLint("CheckResult")
    public  void getCategorias(String id, final ApiCallBack callback) {
        client.create(ApiService.class).getCategorias(id,"Categorias")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(callObserver(callback));
    }

    @SuppressLint("CheckResult")
    public  void deleteMovimento(String id, int index, final ApiCallBack callback) {
        client2.create(ApiService.class).manageMovimento(id,"Movimentos","delete","","","","","",(index+2)+"")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ResponseBody> responseBodyResponse) {
                        callback.onSuccess(responseBodyResponse.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @SuppressLint("CheckResult")
    public  void inserirMovimento(String id, String descricao, String valor, String categoria, String pessoa, Date date, final ApiCallBack callback) {
        client2.create(ApiService.class).manageMovimento(id,"Movimentos","insert",Preferences.convertFromDate(date),descricao,valor,categoria,pessoa,"")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ResponseBody> responseBodyResponse) {
                        callback.onSuccess(responseBodyResponse.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @SuppressLint("CheckResult")
    public  void insertMovimento(String id, int index, final ApiCallBack callback) {
        client2.create(ApiService.class).manageMovimento(id,"Movimentos","delete","","","","","",(index+2)+"")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ResponseBody> responseBodyResponse) {
                        callback.onSuccess(responseBodyResponse.toString());
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    @SuppressLint("CheckResult")
    public void getVersion(final ApiCallBack callback) {
        client2.create(ApiService.class).getAppVersion()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new Observer<Response<ResponseBody>>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Response<ResponseBody> responseBodyResponse) {
                        String resp = null ;
                        try {
                            resp = responseBodyResponse.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        callback.onSuccess(resp);
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onError(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    @NonNull
    private DisposableSingleObserver<Object> callObserver(final ApiCallBack callback) {
        return new DisposableSingleObserver<Object>() {
            @Override
            public void onSuccess(Object resp) {
                callback.onSuccess(resp);
            }

            @Override
            public void onError(Throwable e) {
                Log.e("aa", "", e);
                callback.onError(e);
            }
        };
    }
}
