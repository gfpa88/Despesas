package pt.gon.expensivessheet.ui.analytics.model;

import java.util.ArrayList;
import java.util.List;

import pt.gon.expensivessheet.ws.model.Movimento;

public class Report {
    List<Movimento> movimentos = new ArrayList<>();
    List<ReportElement> elements = new ArrayList<>();

    public List<Movimento> getMovimentos() {
        return movimentos;
    }

    public void setMovimentos(List<Movimento> movimentos) {
        this.movimentos = movimentos;
    }

    public List<ReportElement> getElements() {
        return elements;
    }

    public void setElements(List<ReportElement> elements) {
        this.elements = elements;
    }

}
