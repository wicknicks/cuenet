package esl.cuenet.mapper.tree;

import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.source.*;

import java.util.HashMap;
import java.util.Set;

public class SourceMapper {

    private HashMap<String, String> shorthandNamespaceMap = new HashMap<String, String>();
    private HashMap<String, ISource> sourceMap = new HashMap<String, ISource>();
    private AccessorFactory accessorFactory = AccessorFactory.getInstance();
    private MapperFactory mapperFactory = MapperFactory.getInstance();
    private OntModel model = null;

    protected SourceMapper() {

    }

    private static SourceMapper sourceMapper = new SourceMapper();

    public static SourceMapper constructSourceMapper() {
        return sourceMapper;
    }

    public void setOntologyModel(OntModel model) {
        this.model = model;
    }

    public void addNamespaceMapping(String uri, String shorthand) {
        if (shorthandNamespaceMap.containsKey(shorthand))
            throw new RuntimeException("");

        shorthandNamespaceMap.put(shorthand, uri);
    }
    
    public String getNamespaceShorthand(String uri) {
        Set<String> keys = shorthandNamespaceMap.keySet();
        for (String key: keys) {
            if (shorthandNamespaceMap.get(key).compareTo(uri) == 0) {
                return key;
            }
        }

        return null;
    }
    
    public ISource createSource (String name) {

        IAccessor accessor = accessorFactory.getAccessor(name);
        if (accessor == null) throw new SourceInitializationException("Accessor not available for " + name);
        
        IMapper mapper = mapperFactory.get(shorthandNamespaceMap);
        if (mapper == null) throw new SourceInitializationException("Mapper not available for " + name);
        mapper.setOntologyModel(model);

        ISource source = new Source(name, accessor, mapper);
        sourceMap.put(name, source);

        return source;

    }
    
    public ISource get (String sourceName) {
        return sourceMap.get(sourceName);
    }

    public ISource[] getSources() {
        int sz = sourceMap.values().size();
        ISource[] sourceNames = new ISource[sz];
        return sourceMap.values().toArray(sourceNames);
    }

    public String[] getSourceNames() {
        int sz = sourceMap.keySet().size();
        String[] sourceNames = new String[sz];
        return sourceMap.keySet().toArray(sourceNames);
    }
    
    public int count() {
       return sourceMap.size();
    }

}
