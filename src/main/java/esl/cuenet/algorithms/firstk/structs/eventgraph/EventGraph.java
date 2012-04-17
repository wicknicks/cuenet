package esl.cuenet.algorithms.firstk.structs.eventgraph;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import esl.cuenet.algorithms.firstk.exceptions.EventGraphException;
import esl.datastructures.graph.relationgraph.RelationGraph;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class EventGraph extends RelationGraph {

    private OntModel model = null;
    private List<EventGraphNode> graphNodes = new ArrayList<EventGraphNode>();
    private List<EventGraphEdge> edges = new ArrayList<EventGraphEdge>();
    private HashMap<EventGraphNode, List<EventGraphEdge>> edgeMap = new HashMap<EventGraphNode, List<EventGraphEdge>>();
    private HashMap<EventGraphEdge, EventGraphNode> edgeDestinationMap = new HashMap<EventGraphEdge, EventGraphNode>();
    private HashMap<EventGraphEdge, EventGraphNode> edgeOriginMap = new HashMap<EventGraphEdge, EventGraphNode>();
    private Logger logger = Logger.getLogger(EventGraph.class);

    public EventGraph (OntModel model) {
        this.model = model;
    }

    public EventGraphNode getDestination(EventGraphEdge edge) {
        return edgeDestinationMap.get(edge);
    }

    /**
     * Return a list of EventGraphNodes which do not have an incoming edge
     * @return nodes
     */
    public List<EventGraphNode> getStartNodes() {
        List<EventGraphNode> startNodes = new ArrayList<EventGraphNode>();

        for (EventGraphNode node: graphNodes) {
            if (edgeDestinationMap.values().contains(node)) continue;
            startNodes.add(node);
        }

        return startNodes;
    }

    public enum NodeType {
        EVENT,
        ENTITY
    }

    public Event createEvent(String eventType) throws EventGraphException {
        for (String nsPrefixKey: model.getNsPrefixMap().keySet()) {
            String nsPrefix = model.getNsPrefixMap().get(nsPrefixKey);
            Event event = (Event) createInstanceWithURI(nsPrefix + eventType, NodeType.EVENT);
            if (event == null) continue;
            return event;
        }

        throw new EventGraphException("Invalid event type: " + eventType);
    }

    public Entity createPerson() throws EventGraphException {
        String entityType = "person";
        for (String nsPrefixKey: model.getNsPrefixMap().keySet()) {
            String nsPrefix = model.getNsPrefixMap().get(nsPrefixKey);
            Entity entity = (Entity) createInstanceWithURI(nsPrefix + entityType, NodeType.ENTITY);
            if (entity == null) continue;
            return entity;
        }

        throw new EventGraphException("Invalid event type: " + entityType);
    }

    public List<EventGraphEdge> getEdges(EventGraphNode node) {
        return edgeMap.get(node);
    }

    private EventGraphNode createInstanceWithURI(String uri, NodeType type) {
        OntClass ontClass = model.getOntClass(uri);
        if (ontClass == null) return null;
        Individual eventInd = model.createIndividual(uri + "_" + UUID.randomUUID().toString(), ontClass);
        return addIndividual(eventInd, type);
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
        createEdge(subevent, sube, event);
    }

    public void addParticipant(Event event, Entity entity) throws EventGraphException {
        if (event == null) throw new RuntimeException("Event is null");
        if (entity == null) throw new RuntimeException("Entity is null");

        EventGraphEdge partsIn = EventGraphEdgeFactory.createParticipatesInEdge(model);
        createEdge(entity, partsIn, event);
    }

    public void updateParticipantRole(Event event, Entity entity, String roleURI) throws EventGraphException {
        updateEdge(event, entity, roleURI);
    }

    public void removeEvent(Event event) {
        removeNode(event);
    }

    public void removeEntity(Entity entity) {
        removeNode(entity);
    }

    public void dropParticipantEdge(Event event, Entity entity) {
        EventGraphEdge targetEdge = null;
        for (EventGraphEdge edge: edgeMap.get(entity)) {
            if (edgeDestinationMap.get(edge).equals(event) && EventGraphEdgeFactory.isParticipantInEdge(edge))
                targetEdge = edge;
        }

        if (targetEdge != null) dropEdge(targetEdge);
    }

    public void dropSubeventEdge(Event event, Event subevent) {
        EventGraphEdge subeventEdge = null;
        for (EventGraphEdge edge: edgeMap.get(subevent)) {
            if (edgeDestinationMap.get(edge).equals(event) && EventGraphEdgeFactory.isSubeventOfEdge(edge))
                subeventEdge = edge;
        }
        if (subeventEdge != null) dropEdge(subeventEdge);
    }

    public void updateEdge(Event event, Entity entity, String roleURI) throws EventGraphException {
        if (entity == null) throw new RuntimeException("Entity is null");
        if (event == null) throw new RuntimeException("Event is null");

        dropParticipantEdge(event, entity);
        createEdge(entity, EventGraphEdgeFactory.createObjectPropertyEdge(model, roleURI), event);
    }

    public List<Event> getSubevents(Event event) {
        List<Event> events = new ArrayList<Event>();
        for (EventGraphEdge edge: edgeDestinationMap.keySet()) {
            if (edgeDestinationMap.get(edge).equals(event)) {
                if (EventGraphEdgeFactory.isSubeventOfEdge(edge))
                    events.add((Event) edgeOriginMap.get(edge));
            }
        }

        return events;
    }

    public List<Entity> getParticipants(Event event) {
        List<Entity> entities = new ArrayList<Entity>();
        for (EventGraphEdge edge: edgeDestinationMap.keySet()) {
            if (edgeDestinationMap.get(edge).equals(event)) {
                if (EventGraphEdgeFactory.isParticipantInEdge(edge))
                    entities.add((Entity) edgeOriginMap.get(edge));
            }
        }

        return entities;
    }

    private void removeNode(EventGraphNode targetNode) {
        List<EventGraphEdge> removedEdges = new ArrayList<EventGraphEdge>();

        /* if the origin or destination of the edge is event */
        for (EventGraphEdge edge: edges) {
            if (edgeOriginMap.get(edge).equals(targetNode) || edgeDestinationMap.get(edge).equals(targetNode)) {
                logger.info("To be removed: " + edge.label());
                removedEdges.add(edge);
            }
        }

        for (EventGraphEdge edge: removedEdges) {
            if (edgeDestinationMap.containsKey(edge)) edgeDestinationMap.remove(edge);
            if (edgeOriginMap.containsKey(edge)) edgeOriginMap.remove(edge);

            for (EventGraphNode node: graphNodes) {
                if (edgeMap.get(node).contains(edge)) edgeMap.get(node).remove(edge);
            }

        }

        edgeMap.remove(targetNode);
        graphNodes.remove(targetNode);
        edges.removeAll(removedEdges);
    }

    private void dropEdge(EventGraphEdge edge) {
        edgeOriginMap.remove(edge);
        edgeDestinationMap.remove(edge);
        for (EventGraphNode node: graphNodes) {
            List<EventGraphEdge> edges = edgeMap.get(node);
            if (edges.contains(edge)) edges.remove(edge);
        }
        edges.remove(edge);
    }

    private void createEdge(EventGraphNode n1,EventGraphEdge edge, EventGraphNode n2) {
        if (n1 == null) throw new RuntimeException("Null Node (n1)");
        if (n2 == null) throw new RuntimeException("Null Node (n2)");
        List<EventGraphEdge> nodeEdges = edgeMap.get(n1);
        nodeEdges.add(edge);
        edges.add(edge);
        edgeDestinationMap.put(edge, n2);
        edgeOriginMap.put(edge, n1);
    }

}
