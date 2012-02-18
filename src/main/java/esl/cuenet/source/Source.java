package esl.cuenet.source;

public class Source {

    private IO io = null;
    private TYPE type = null;
    private String name;

    public Source(String name) {
        this.name = name;
        associateAccessor();
    }

    private void associateAccessor() {
        Class t = AccessorFactory.INSTANCE.getClassForSource(name);
        if (t==null) throw new RuntimeException("Unknown source type");

        try {
            t.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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
    
    public TYPE getType() {
        return this.type;
    }
    
    public IO getIo() {
        return this.io;
    }
    
}
