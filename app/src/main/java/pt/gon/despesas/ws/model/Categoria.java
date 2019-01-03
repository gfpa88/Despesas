package pt.gon.despesas.ws.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Categoria {

    @SerializedName("Tipo")
    @Expose
    private String tipo;
    @SerializedName("Ano")
    @Expose
    private String ano;
    @SerializedName("Pessoa")
    @Expose
    private String pessoa;

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getPessoa() {
        return pessoa;
    }

    public void setPessoa(String pessoa) {
        this.pessoa = pessoa;
    }

    @Override
    public String toString() {
        return "Categoria{" +
                "tipo='" + tipo + '\'' +
                ", ano='" + ano + '\'' +
                ", pessoa='" + pessoa + '\'' +
                '}';
    }
}