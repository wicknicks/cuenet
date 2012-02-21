package esl.cuenet.source;

public class Attribute implements Comparable<Attribute> {

    private String name;

    public Attribute(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public int compareTo(Attribute o) {
        return this.name.compareTo(o.name);
    }

}
