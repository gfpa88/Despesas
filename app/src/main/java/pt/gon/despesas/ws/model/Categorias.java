package pt.gon.despesas.ws.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Categorias {

    @SerializedName("Categorias")
    @Expose
    private List<Categoria> categorias = null;

    public List<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(List<Categoria> categorias) {
        this.categorias = categorias;
    }

    @Override
    public String toString() {
        return "Categorias{" +
                "categorias=" + categorias +
                '}';
    }
}
