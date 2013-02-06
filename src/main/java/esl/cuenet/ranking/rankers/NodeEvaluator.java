package esl.cuenet.ranking.rankers;

import esl.cuenet.model.Constants;
import esl.cuenet.ranking.EntityBase;
import esl.cuenet.ranking.URINode;
import esl.cuenet.ranking.network.OntProperties;

public class NodeEvaluator {

    protected final String participatesInPropertyURI;
    protected final String emailExchangeEventURI;
    protected final String photoCaptureEventURI;
    protected final String personURI;
    protected final String subeventURI;

    protected final double _DAMPNER = 0.75;

    public NodeEvaluator() {
        participatesInPropertyURI = Constants.DOLCE_Lite_Namespace + Constants.ParticipantIn;
        emailExchangeEventURI = Constants.CuenetNamespace + Constants.EmailExchangeEvent;
        photoCaptureEventURI = Constants.CuenetNamespace + Constants.PhotoCaptureEvent;
        personURI = Constants.CuenetNamespace + Constants.Person;
        subeventURI = Constants.CuenetNamespace + Constants.SubEvent;
    }


    protected boolean isEvent(URINode node) {
        if ( !node.hasProperty(OntProperties.ONT_URI) ) return false;
        String prop = (String) node.getProperty(OntProperties.ONT_URI);
        return (prop.contains(emailExchangeEventURI) || prop.contains(photoCaptureEventURI));
    }

    protected boolean isEntity(URINode node) {
        if ( node.hasProperty(OntProperties.ONT_URI) ) {
            String prop = (String) node.getProperty(OntProperties.ONT_URI);
            return prop.contains(personURI);
        }

        if (node.hasProperty(EntityBase.TYPE)) {
            String prop = (String) node.getProperty(EntityBase.TYPE);
            return prop.equals(EntityBase.ENTITY);
        }

        return false;
    }

}
