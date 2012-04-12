package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import esl.datastructures.graph.relationgraph.RelationGraph;
import esl.datastructures.graph.relationgraph.RelationGraphNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventGraph extends RelationGraph {

    private OntModel model = null;
    private List<EventGraphNode> nodes = new ArrayList<EventGraphNode>();
    private List<EventGraphEdge> edges = new ArrayList<EventGraphEdge>();
    private HashMap<EventGraphNode, List<EventGraphEdge>> edgeMap = new HashMap<EventGraphNode, List<EventGraphEdge>>();
    private HashMap<EventGraphEdge, EventGraphNode> edgeDestinationMap = new HashMap<EventGraphEdge, EventGraphNode>();
    private HashMap<EventGraphEdge, EventGraphNode> edgeOriginMap = new HashMap<EventGraphEdge, EventGraphNode>();

    public EventGraph (OntModel model) {
        this.model = model;
    }

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
        if (type == NodeType.EVENT) {
            EventGraphNode event = new Event(individual);
            this.nodes.add(event);
            edgeMap.put(event, new ArrayList<EventGraphEdge>());
        }

        else if (type == NodeType.ENTITY) {
            EventGraphNode entity = new Entity(individual);
            this.nodes.add(entity);
            edgeMap.put(entity, new ArrayList<EventGraphEdge>());
        }
    }

    public void addSubevent(Event event, Event subevent) throws EventGraphException {
        if (event == null) throw new RuntimeException("Parent event is null");
        if (subevent == null) throw new RuntimeException("Parent event is null");

        EventGraphEdge sube = EventGraphEdgeFactory.createSubeventEdge(model);
        edges.add(sube);
        createEdge(subevent, sube, event);
    }

    public void removeEvent(Event event) {

    }

    public void removeSubevent(Event event, Event subevent) {

    }

    public void addParticipant(Event event, Entity entity) throws EventGraphException {
        if (entity == null) throw new RuntimeException("Entity is null");
        if (event == null) throw new RuntimeException("Event is null");

        EventGraphEdge partsIn = EventGraphEdgeFactory.createParticipatesInEdge(model);
        edges.add(partsIn);
        createEdge(entity, partsIn, event);
    }

    public void addParticipant(Event event, Entity entity, String roleURI) {

    }

    public void removeParticipant(Event event, Entity entity) {

    }

    public void updateRole(Event event, Entity entity, String roleURI) {

    }

    public List<Event> getRelatedEvents(Entity entity) {
        for (EventGraphEdge edge: edges) {
            if (edgeOriginMap.get(edge).equals(entity)) {
                //todo: process edge
            }
        }

        for (EventGraphEdge edge: edges) {
            if (edgeDestinationMap.get(edge).equals(entity)) {
                //todo: process edge
            }
        }

        return null;
    }

    public List<Entity> getRelatedEntities(Event event) {
        for (EventGraphEdge edge: edges) {
            if (edgeOriginMap.get(edge).equals(event)) {
                //todo: process edge
            }
        }

        for (EventGraphEdge edge: edges) {
            if (edgeDestinationMap.get(edge).equals(event)) {
                //todo: process edge
            }
        }

        return null;
    }

    private void createEdge(EventGraphNode n1,EventGraphEdge edge, EventGraphNode n2) {
        if (n1 == null) throw new RuntimeException("Null Node (n1)");
        if (n2 == null) throw new RuntimeException("Null Node (n2)");
        List<EventGraphEdge> edges = edgeMap.get(n1);
        edges.add(edge);
        edgeDestinationMap.put(edge, n2);
        edgeOriginMap.put(edge, n1);
    }

}
