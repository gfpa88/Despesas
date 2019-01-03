package pt.gon.despesas.ws;

import io.reactivex.Single;
import pt.gon.despesas.ws.model.Categorias;
import pt.gon.despesas.ws.model.Movimentos;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

   // @FormUrlEncoded
   // @POST("login.php")
   // Single<Login> getLogin(@Field("username") String username,                           @Field("password") String password);

    @GET("https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec")
    Single<Categorias> getCategorias(@Query("id") String action, @Query("sheet") String sheet);

    @GET("https://script.google.com/macros/s/AKfycbxOLElujQcy1-ZUer1KgEvK16gkTLUqYftApjNCM_IRTL3HSuDk/exec")
    Single<Movimentos> getMovimentos(@Query("id") String action, @Query("sheet") String sheet);
    

    @GET("{path}.php")
    Single<Categorias> getMovimentos(@Path("path") String path, @Query("action") String action, @Query("page") String page, @Query("qualidade") String qualidade, @Query("ano") String ano, @Query("categoria") String categoria);

}