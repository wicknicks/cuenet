package esl.cuenet.mapper.tree;

import esl.cuenet.source.Source;

import java.util.HashMap;

public class SourceMapper {

    private HashMap<String, String> shorthandNamespaceMap = new HashMap<String, String>();
    private HashMap<String, Source> sourceMap = new HashMap<String, Source>();

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
    
    public Source createSource (String name) {
        Source source = new Source(name);
        sourceMap.put(name, source);
        return source;
    }

    public void addAxiomsToSource() {

    }

}
