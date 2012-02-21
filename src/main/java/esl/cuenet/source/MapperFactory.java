package esl.cuenet.source;

public class MapperFactory {

    private static MapperFactory factoryInstance = new MapperFactory();

    private MapperFactory() {

    }

    public static MapperFactory getInstance() {
        return factoryInstance;
    }

    public IMapper get() {
        return new TreeMapper();
    }
}
