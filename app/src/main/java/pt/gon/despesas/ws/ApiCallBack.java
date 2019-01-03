package pt.gon.despesas.ws;

import android.support.annotation.NonNull;

public interface ApiCallBack {
    void onSuccess(@NonNull Object value);

    void onError(@NonNull Throwable throwable);
}
