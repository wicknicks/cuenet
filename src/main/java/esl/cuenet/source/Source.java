package esl.cuenet.source;

public class Source {

    private IO io = null;
    private TYPE type = null;
    private String name;

    public Source(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public enum IO {
        DISK,
        NETWORK
    }

    public enum TYPE {
        PUBLIC,
        SOCIAL,
        PERSONAL
    }

    public void setIO(IO io) {
        this.io = io;
    }

    public void setType (TYPE type) {
        this.type = type;
    }
    
}
