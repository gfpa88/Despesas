package pt.gon.despesas.ws;

import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.ResponseBody;
import pt.gon.despesas.ws.model.Categorias;
import pt.gon.despesas.ws.model.Movimentos;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

   // @FormUrlEncoded
   // @POST("login.php")
   // Single<Login> getLogin(@Field("username") String username,                           @Field("password") String password);

    @GET("https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec")
    Single<Categorias> getCategorias(@Query("id") String id, @Query("sheet") String sheet);

    @GET("https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec")
    Single<Movimentos> getMovimentos(@Query("id") String id, @Query("sheet") String sheet);

    @FormUrlEncoded
    @POST("https://script.google.com/macros/s/AKfycbwdV9vxmRzIwjUtcTPWxqrSc89UZsKW7NBPW4onCg/exec")
    Observable<Response<ResponseBody>> manageMovimento(@Field("id") String id, @Field("sheet") String sheet, @Field("operation") String operation, @Field("date") String date, @Field("description") String description, @Field("value") String value, @Field("category") String category, @Field("person") String person, @Field("rowIndex") String rowIndex);

}