package pt.gon.expensivessheet.ws.model;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Movimentos {

    @SerializedName("Movimentos")
    @Expose
    private List<Movimento> movimentos = null;

    public List<Movimento> getMovimentos() {
        return movimentos;
    }

    public void setMovimentos(List<Movimento> movimentos) {
        this.movimentos = movimentos;
    }

    @Override
    public String toString() {
        return "Movimentos{" +
                "movimentos=" + movimentos +
                '}';
    }
}