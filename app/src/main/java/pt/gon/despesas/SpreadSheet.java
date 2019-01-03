package pt.gon.despesas;

public class SpreadSheet {
    String id;
    String name;

    public SpreadSheet(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "SpreadSheet{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
