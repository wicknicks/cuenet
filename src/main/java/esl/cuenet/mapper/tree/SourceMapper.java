package esl.cuenet.mapper.tree;

import esl.cuenet.source.ISource;

import java.util.HashMap;

public class SourceMapper {

    private HashMap<String, String> shorthandNamespaceMap = new HashMap<String, String>();
    private HashMap<String, ISource> sourceMap = new HashMap<String, ISource>();

    protected SourceMapper() {

    }

    private static SourceMapper sourceMapper = new SourceMapper();

    public static SourceMapper constructSourceMapper() {
        return sourceMapper;
    }

    public void addNamespaceMapping(String uri, String shorthand) {
        if (shorthandNamespaceMap.containsKey(shorthand))
            throw new RuntimeException("");

        shorthandNamespaceMap.put(shorthand, uri);
    }
    
    public ISource createSource (String name) {
        ISource source = null;
        return source;
    }

    public void addAxiomsToSource() {

    }

}
