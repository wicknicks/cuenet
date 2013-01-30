package esl.cuenet.ranking.sources;

import esl.cuenet.ranking.EventEntityNetwork;
import esl.cuenet.ranking.URINode;
import esl.cuenet.ranking.network.OntProperties;

class SourceHelper {

    public static URINode createInstance(EventEntityNetwork network, String uri) {
        URINode node = network.createNode();
        node.setProperty(OntProperties.ONT_URI, uri);
        node.setProperty(OntProperties.TYPE, OntProperties.INSTANCE);
        return node;
    }

    public static URINode createLiteral(EventEntityNetwork network, Object obj) {
        URINode node = network.createNode();
        node.setProperty(OntProperties.TYPE, OntProperties.LITERAL);
        node.setProperty(OntProperties.LVALUE, obj);
        return node;
    }

}
