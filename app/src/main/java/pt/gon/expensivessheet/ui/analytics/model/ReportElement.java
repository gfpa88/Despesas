package pt.gon.expensivessheet.ui.analytics.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReportElement {
    Double value;
    Map<String, Double> childs = new HashMap<>();

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Map<String, Double> getChilds() {
        return childs;
    }

    public void setChilds(Map<String, Double> childs) {
        this.childs = childs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReportElement that = (ReportElement) o;
        return Objects.equals(getValue(), that.getValue()) && Objects.equals(getChilds(), that.getChilds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue(), getChilds());
    }
}
