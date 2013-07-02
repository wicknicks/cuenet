package esl.cuenet.generative;

import esl.cuenet.generative.structs.ContextNetwork;
import esl.cuenet.generative.structs.NetworkBuildingHelper;
import esl.cuenet.generative.structs.Ontology;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

public class DataReader {

    public ContextNetwork readInstanceGraphs(String filename) throws IOException {
        ContextNetwork network = new ContextNetwork();

        BufferedReader reader = new BufferedReader(new FileReader(filename));

        String line = reader.readLine();
        String roadNetEdge = "", roadNetNode = "";
        String location = "";
        while(true) {
            line = reader.readLine();
            if (line == null) break;
            if (line.indexOf("## ") == 0) {
                roadNetEdge = line;
            }
            else if (line.indexOf("$$ ") == 0) {
                roadNetNode = line;
                location = roadNetEdge + " " + roadNetNode;
            }
            else {
                String[] parts = line.split(",");
                if (parts.length == 3) addAtomicEvent(network, parts, location);
                else if (parts.length == 4) atomicCompositeEvent(reader, network, parts, location);
                else throw new RuntimeException("Line " + line);
            }

        }

        return network;
    }

    private void addAtomicEvent(ContextNetwork network, String[] parts, String location) {
        int[] instanceSpec = splitAndConvertToInts(parts[0], "_");
        int intervalStart = Integer.parseInt(parts[1]);
        int intervalEnd = Integer.parseInt(parts[2]);
        ContextNetwork.Instance inst = new ContextNetwork.Instance(instanceSpec[0], instanceSpec[1]);
        inst.setInterval(intervalStart, intervalEnd);
        inst.setLocation(location);
        network.addAtomic(inst);
    }

    private void atomicCompositeEvent(BufferedReader reader, ContextNetwork network, String[] parts, String location) throws IOException {
        int[] instanceSpec = splitAndConvertToInts(parts[0], "_");
        int intervalStart = Integer.parseInt(parts[1]);
        int intervalEnd = Integer.parseInt(parts[2]);

        //ContextNetwork tempNetwork = new ContextNetwork();
        ContextNetwork.Instance inst = new ContextNetwork.Instance(instanceSpec[0], instanceSpec[1]);
        inst.setInterval(intervalStart, intervalEnd);
        inst.setLocation(location);
        network.addAtomic(inst);

        String line = null;
        int[] superEvent;
        int[] subEvent;
        ContextNetwork.Instance superInstance;
        ContextNetwork.Instance subInstance;

        while (true) {
            line = reader.readLine();
            if (line.equals(".")) break;

            String[] eparts = line.split(" -> ");
            superEvent = splitAndConvertToInts(eparts[0], "_");
            subEvent = splitAndConvertToInts(eparts[1], "_");

            superInstance = new ContextNetwork.Instance(superEvent[0], superEvent[1]);
            if (superInstance.equals(inst)) {
                superInstance = inst;
            }

            subInstance = new ContextNetwork.Instance(subEvent[0], subEvent[1]);
            subInstance.setLocation(location);

            network.addSubeventEdge(inst, superInstance, subInstance);
        }

        NetworkBuildingHelper.updateTimeIntervals(network, inst);
    }

    public Ontology readOntology(String ontfilename) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(ontfilename));

        String line = reader.readLine();
        int[] ontconfig = splitAndConvertToInts(line, ",");
        int eventCount = ontconfig[0];

        Ontology ontology = new Ontology(eventCount);

        while(true) {
            line = reader.readLine();
            if (line == null) break;

            if (line.indexOf("###") == 0) {
                String[] parts = line.split(" ");
                ontology.startTree(Integer.parseInt(parts[2]));
            } else {
                String[] parts = line.split(" -> ");
                ontology.addEdges(Integer.parseInt(parts[0]), splitAndConvertToInts(parts[1], ","));
            }
        }

        return ontology;
    }

    public int[] splitAndConvertToInts(String line, String delim) {
        String[] parts = line.split(delim);
        int[] intparts = new int[parts.length];

        for (int i=0; i<parts.length; i++)
            intparts[i] = Integer.parseInt(parts[i]);

        return intparts;
    }

}
