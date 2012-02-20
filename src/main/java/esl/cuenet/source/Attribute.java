package esl.cuenet.source;

public class Attribute implements Comparable<Attribute> {

    public String name;

    @Override
    public int compareTo(Attribute o) {
        return this.name.compareTo(o.name);
    }

}
