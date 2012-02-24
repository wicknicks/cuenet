package esl.cuenet.source;

import java.util.HashMap;

public class MapperFactory {

    private static MapperFactory factoryInstance = new MapperFactory();

    private MapperFactory() {

    }

    public static MapperFactory getInstance() {
        return factoryInstance;
    }

    public IMapper get(HashMap<String, String> namespaceMap) {
        return new TreeMapper(namespaceMap);
    }
}
