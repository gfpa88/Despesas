package pt.gon.expensivessheet.ws;

import androidx.annotation.NonNull;

public interface ApiCallBack {
    void onSuccess(@NonNull Object value);

    void onError(@NonNull Throwable throwable);
}
