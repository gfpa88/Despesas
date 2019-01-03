package pt.gon.despesas.ws.model;

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
    private Double valor;
    @SerializedName("Tipo")
    @Expose
    private String tipo;
    @SerializedName("Pessoa")
    @Expose
    private String pessoa;
    @SerializedName("Mes")
    @Expose
    private Integer mes;
    @SerializedName("Ano")
    @Expose
    private Integer ano;

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

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
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


    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getMes() {
        return mes;
    }

    public void setMes(Integer mes) {
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