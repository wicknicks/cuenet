package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import esl.datastructures.graph.relationgraph.RelationGraph;

import java.util.List;

public class EventGraph extends RelationGraph {

    public enum NodeType {
        EVENT,
        ENTITY
    }

    public List<Event> getEvents() {
        return null;
    }

    public List<Entity> getEntities() {
        return null;
    }

    public void addIndividual(Individual individual, NodeType type) {

    }

    public boolean addSubevent(Event event, Event subevent) {
        return false;
    }

    public void removeEvent(Event event) {

    }

    public void removeSubevent(Event event, Event subevent) {

    }

    public void addParticipant(Event event, Entity entity) {

    }

    public void addParticipant(Event event, Entity entity, String roleURI) {

    }

    public void removeParticipant(Event event, Entity entity) {

    }

    public void updateRole(Event event, Entity entity, String roleURI) {

    }

    public List<Event> getRelatedEvents(Entity entity) {
        return null;
    }

    public List<Entity> getRelatedEntities(Event event) {
        return null;
    }

}
