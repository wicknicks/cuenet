package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import esl.datastructures.graph.relationgraph.RelationGraph;
import org.apache.xerces.util.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventGraph extends RelationGraph {

    private OntModel model = null;
    private List<EventGraphNode> graphNodes = new ArrayList<EventGraphNode>();
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
        List<Event> events = new ArrayList<Event>();
        for (EventGraphNode n: graphNodes) {
            if (n.getType() == NodeType.EVENT) events.add((Event) n);
        }
        return events;
    }

    public Event createEvent(String eventType) throws EventGraphException {
        if (URI.isWellFormedAddress(eventType)) {
            return (Event) createInstanceWithURI(eventType);
        }

        for (String nsPrefixKey: model.getNsPrefixMap().keySet()) {
            String nsPrefix = model.getNsPrefixMap().get(nsPrefixKey);
            Event event = (Event) createInstanceWithURI(nsPrefix + eventType);
            if (event == null) continue;
            return event;
        }

        throw new EventGraphException("Invalid event type: " + eventType);
    }

    private EventGraphNode createInstanceWithURI(String uri) {
        OntClass ontClass = model.getOntClass(uri);
        if (ontClass == null) return null;
        Individual eventInd = model.createIndividual(ontClass);
        return addIndividual(eventInd, NodeType.EVENT);
    }

    public List<Entity> getEntities() {
        List<Entity> entities = new ArrayList<Entity>();
        for (EventGraphNode n: graphNodes) {
            if (n.getType() == NodeType.ENTITY) entities.add((Entity) n);
        }
        return entities;
    }

    public EventGraphNode addIndividual(Individual individual, NodeType type) {
        if (type == NodeType.EVENT) {
            EventGraphNode event = new Event(individual);
            this.graphNodes.add(event);
            edgeMap.put(event, new ArrayList<EventGraphEdge>());
            return event;
        }

        else if (type == NodeType.ENTITY) {
            EventGraphNode entity = new Entity(individual);
            this.graphNodes.add(entity);
            edgeMap.put(entity, new ArrayList<EventGraphEdge>());
            return entity;
        }

        return null;
    }

    public void addSubevent(Event event, Event subevent) throws EventGraphException {
        if (event == null) throw new RuntimeException("Parent event is null");
        if (subevent == null) throw new RuntimeException("Parent event is null");

        EventGraphEdge sube = EventGraphEdgeFactory.createSubeventEdge(model);
        edges.add(sube);
        createEdge(subevent, sube, event);
    }

    public void removeEvent(Event event) {
        List<EventGraphEdge> removedEdges = new ArrayList<EventGraphEdge>();

        for (EventGraphEdge edge: edges) {
            /* if the origin or destination of the edge is event */
            if (edgeOriginMap.get(edge).equals(event) || edgeDestinationMap.get(edge).equals(event)) {
                removedEdges.add(edge);
            }

        }

        nodes.remove(event);
        edges.removeAll(removedEdges);
    }

    public void addParticipant(Event event, Entity entity) throws EventGraphException {
        addParticipant(event, entity, "participates-in");
    }

    public void addParticipant(Event event, Entity entity, String roleURI) throws EventGraphException {
        if (entity == null) throw new RuntimeException("Entity is null");
        if (event == null) throw new RuntimeException("Event is null");

        EventGraphEdge partsIn = EventGraphEdgeFactory.createObjectPropertyEdge(model, roleURI);
        edges.add(partsIn);
        createEdge(entity, partsIn, event);
    }

    public void removeParticipant(Event event, Entity entity) {
        //todo:
    }

    public void removeSubevent(Event event, Event subevent) {
        //todo:
    }

    public void updateRole(Event event, Entity entity, String roleURI) {
        throw new UnsupportedOperationException("updateRole() Not implemented");
    }

    public List<Event> getRelatedEvents(Entity entity) {
        List<Event> events = new ArrayList<Event>();
        for (EventGraphEdge edge: edges) {
            if (edgeOriginMap.get(edge).equals(entity)) {
                events.add((Event) edgeDestinationMap.get(edge));
            }
        }

        for (EventGraphEdge edge: edges) {
            if (edgeDestinationMap.get(edge).equals(entity)) {
                events.add((Event) edgeOriginMap.get(edge));
            }
        }

        return events;
    }

    public List<Entity> getRelatedEntities(Event event) {
        List<Entity> entities = new ArrayList<Entity>();
        for (EventGraphEdge edge: edges) {
            if (edgeOriginMap.get(edge).equals(event)) {
                entities.add((Entity) edgeDestinationMap.get(edge));
            }
        }

        for (EventGraphEdge edge: edges) {
            if (edgeDestinationMap.get(edge).equals(event)) {
                entities.add((Entity) edgeOriginMap.get(edge));
            }
        }

        return entities;
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
