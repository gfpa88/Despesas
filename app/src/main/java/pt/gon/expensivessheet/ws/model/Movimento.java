package pt.gon.expensivessheet.ws.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Movimento {

    @SerializedName("Data")
    @Expose
    private String data;
    @SerializedName("Descricao")
    @Expose
    private String descricao;
    @SerializedName("Valor")
    @Expose
    private String valor;
    @SerializedName("Tipo")
    @Expose
    private String tipo;
    @SerializedName("Pessoa")
    @Expose
    private String pessoa;
    @SerializedName("Mes")
    @Expose
    private String mes;
    @SerializedName("Ano")
    @Expose
    private String ano;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getPessoa() {
        return pessoa;
    }

    public void setPessoa(String pessoa) {
        this.pessoa = pessoa;
    }


    public String getAno() {
        return ano;
    }

    public void setAno(String ano) {
        this.ano = ano;
    }

    public String getMes() {
        return mes;
    }

    public void setMes(String mes) {
        this.mes = mes;
    }

    @Override
    public String toString() {
        return "Movimento{" +
                "data='" + data + '\'' +
                ", descricao='" + descricao + '\'' +
                ", valor=" + valor +
                ", tipo='" + tipo + '\'' +
                ", pessoa='" + pessoa + '\'' +
                ", mes=" + mes +
                ", ano=" + ano +
                '}';
    }
}