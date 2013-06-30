package esl.cuenet.generative.structs;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class ContextNetwork {

    private List<IndexedSubeventTree> eventTrees = new ArrayList<IndexedSubeventTree>();

    public void addAtomic(Instance inst) {
        IndexedSubeventTree tree = new IndexedSubeventTree();
        tree.root = inst;
        tree.typeIndex.put(inst.id.eventId, new HashSet<Instance>());
        eventTrees.add(tree);
    }

    public void addSubeventEdge(Instance root, Instance current, Instance subevent) {
        //find root in eventTrees list
        IndexedSubeventTree subtree = null;
        for (IndexedSubeventTree i: eventTrees)
            if (i.root.equals(root)) subtree = i;

        if (subtree == null) throw new RuntimeException("No corresponding subtree for event " + root);

        if (subtree.typeIndex.containsKey(current.id.eventId)) {
            HashSet<Instance> instances = subtree.typeIndex.get(current.id.eventId);
            instances.add(current);

            Instance temp = null;
            for (Instance i: instances) {
                if (i.equals(current)) temp = i;
            }

            if (temp == null) throw new RuntimeException("Could not find instance " + current);

            temp.immediateSubevents.add(subevent.id);

        } else {
            HashSet<Instance> instances = new HashSet<Instance>();
            subtree.typeIndex.put(current.id.eventId, instances);
            instances.add(current);

            current.immediateSubevents.add(subevent.id);
        }

        HashSet<Instance> subeventInstances;
        if (subtree.typeIndex.containsKey(subevent.id.eventId))
            subeventInstances = subtree.typeIndex.get(subevent.id.eventId);
        else {
            subeventInstances = new HashSet<Instance>();
            subtree.typeIndex.put(subevent.id.eventId, subeventInstances);
        }

        if (subeventInstances.contains(subevent))
            System.out.println("subevent already exists: " + current + " " + subevent);
        else
            subeventInstances.add(subevent);
    }

    public int count() {
        return eventTrees.size();
    }

    public void updateTimeIntervals(Instance root) {
        IndexedSubeventTree temp = null;
        for (int i=eventTrees.size()-1; i>=0; i--) {
            if (eventTrees.get(i).root.equals(root)) {
                temp = eventTrees.get(i);
                break;
            }
        }

        if (temp == null) throw new RuntimeException("Invalid root: " + root);

        updateTimeIntervals(temp, temp.root);

        count();
    }

    private Instance lookup(IndexedSubeventTree root, InstanceId id) {
        HashSet<Instance> instances = root.typeIndex.get(id.eventId);
        if (instances == null) throw new NullPointerException(id.toString());
        for (Instance i: instances) {
            if (i.id.equals(id)) return i;
        }
        throw new NoSuchElementException(root.toString() + " " + id.toString());
    }

    private void updateTimeIntervals(IndexedSubeventTree root, Instance current) {
        int count = current.immediateSubevents.size();
        if (count == 0) return;

        int span = (current.intervalEnd - current.intervalStart) / count;

        int i=0;
        for (InstanceId instanceid: current.immediateSubevents) {
            Instance subevent = lookup(root, instanceid);
            subevent.location = current.location;
            subevent.intervalStart = current.intervalStart + (span * i);
            subevent.intervalEnd = current.intervalStart + (span * (i+1));
            i++;
            updateTimeIntervals(root, subevent);
        }
    }

    public void merge (ContextNetwork other) {
        for (IndexedSubeventTree thisSub: this.eventTrees)
            for (IndexedSubeventTree otherTree: other.eventTrees)
                descendAndMerge(thisSub, otherTree);

//        for (IndexedSubeventTree otherTree: other.eventTrees) merge(otherTree);
    }

    private void descendAndMerge(IndexedSubeventTree subtree, IndexedSubeventTree other) {
        if (!STHelper.lequals(subtree.root, other.root)) return;

        if (STHelper.tequals(subtree.root, other.root)) {
            mergeInformation(subtree.root, other.root);
            for (InstanceId oSubeventId: other.root.immediateSubevents) {
                Instance s = lookup(other, oSubeventId);
                recursiveMerge(subtree, subtree.root, other, s);
            }
        }
        else if (STHelper.contains(subtree.root, other.root)) {
            recursiveMerge(subtree, subtree.root, other, other.root);
        } else if (STHelper.contains(other.root, subtree.root)) {
            Instance new_root = other.root.attributeClone();
            Instance old_root = subtree.root;
            subtree.root = new_root;
            addSubeventEdge(subtree.root, subtree.root, old_root);
            System.out.println("new root " + other.root + " " + subtree.root);
            for (InstanceId oSubeventId: other.root.immediateSubevents) {
                Instance s = lookup(other, oSubeventId);
                recursiveMerge(subtree, subtree.root, other, s);
            }
        }
    }

    //the parent is guranteed to contain instance. RM will decide to either
    // (a) add as a subevent to parent
    // (b) push instance down to one of its immediate subevents.
    // (c) insert between parent and one of its immediate subevents.
    private void recursiveMerge(IndexedSubeventTree subtree, Instance parent,
                                IndexedSubeventTree other, Instance instance) {
        boolean addAsSubevent = true;

        if ( !STHelper.lequals(parent, instance) ) return;

        //check if any of the immediate subevents accepts instance
        for (InstanceId subs: parent.immediateSubevents) {
            Instance subevent = lookup(subtree, subs);
            if (STHelper.tequals(subevent, instance)) {
                addAsSubevent = false;
                mergeInformation(subevent, instance);
                for (InstanceId osubs: instance.immediateSubevents) {
                    Instance s = lookup(other, osubs);
                    recursiveMerge(subtree, subevent, other, s);
                }
            }
            else if (STHelper.contains(subevent, instance)) {
                addAsSubevent = false;
                recursiveMerge(subtree, subevent, other, instance);
            }
            else if (STHelper.contains(instance, subevent)) {
                addAsSubevent = false;

                Instance new_i = instance.attributeClone();

                parent.immediateSubevents.remove(subevent.id);

                addSubeventEdge(subtree.root, parent, new_i);
                addSubeventEdge(subtree.root, new_i, subevent);

                for (InstanceId osubs: instance.immediateSubevents) {
                    Instance s = lookup(other, osubs);
                    recursiveMerge(subtree, new_i, other, s);
                }
                break;
            }
        }

        if (addAsSubevent) {
            Instance new_i = instance.attributeClone();
            addSubeventEdge(subtree.root, parent, new_i);
            for (InstanceId osubs: instance.immediateSubevents) {
                Instance s = lookup(other, osubs);
                recursiveMerge(subtree, new_i, other, s);
            }
        }
    }

    private void mergeInformation(Instance thisSubevent, Instance otherInstance) {
        System.out.println("[merge_info] " + thisSubevent + " " + otherInstance);
    }

    public void printTree() {
        for (IndexedSubeventTree etree: eventTrees)
            etree.print(System.out);
    }

    public void printTree(int root_eventid, int root_instanceid) {
        for (IndexedSubeventTree etree: eventTrees)
            if (etree.root.id.eventId == root_eventid && etree.root.id.instanceId == root_instanceid)
                etree.print(System.out);
    }

    public boolean compareNetwork(ContextNetwork other) {
        for (IndexedSubeventTree tree: eventTrees) {
            boolean flag = false;
            for (IndexedSubeventTree otherTree: other.eventTrees) {
                if (otherTree.compareTree(tree)) {
                    flag = true;
                    break;
                }
            }
            if ( !flag ) return false;
        }
        return true;
    }

    private class IndexedSubeventTree {
        Instance root;
        HashMap<Integer, HashSet<Instance>> typeIndex = new HashMap<Integer, HashSet<Instance>>();

        @Override
        public String toString() {
            return root.toString();
        }

        public void print(PrintStream out) {
            try {
                out.write(("ROOT " + root + "\n").getBytes());
                Stack<InstanceId> stack = new Stack<InstanceId>();
                stack.add(root.id);
                while (!stack.isEmpty()) {
                    InstanceId i = stack.pop();
                    for (InstanceId subids: lookup(this, i).immediateSubevents) {
                        out.write(i.toString().getBytes());
                        out.write(" -> ".getBytes());
                        out.write(subids.toString().getBytes());
                        out.write("\n".getBytes());
                        stack.add(subids);
                    }
                }
                out.write(".\n".getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean compareTree(IndexedSubeventTree other) {
            if ( !this.root.equals(other.root) ) return false;
            if ( this.typeIndex.size() != other.typeIndex.size()) return false;
            if ( !this.typeIndex.keySet().containsAll(other.typeIndex.keySet()) ) return false;

            for (Integer thiskey: this.typeIndex.keySet()) {
                HashSet<Instance> thisvalues = this.typeIndex.get(thiskey);
                HashSet<Instance> othervalues = other.typeIndex.get(thiskey);
                if ( !thisvalues.containsAll(othervalues) )  return false;

                for (Instance thisinstance: thisvalues) {
                    boolean flag = true;
                    for (Instance thatinstance: othervalues) {
                        if (thatinstance.equals(thisinstance)) {
                            flag = false;
                            if ( !thatinstance.immediateSubevents.containsAll(thisinstance.immediateSubevents) )
                                return false;
                            break;
                        }
                    }
                    if ( flag ) return false;
                }
            }
            return true;
        }

    }

    public static class Instance {
        InstanceId id;
        List<InstanceId> immediateSubevents;
        int intervalStart, intervalEnd;
        String location;

        public Instance(int eventId, int instanceId) {
            this.id = new InstanceId(eventId, instanceId);
            this.immediateSubevents = new ArrayList<InstanceId>();
        }

        public void setLocation(String location) {
            this.location = location;
        }

        public void setInterval(int start, int end) {
            this.intervalStart = start;
            this.intervalEnd = end;
        }

        // create a clone of this instance without the immediate subevents.
        public Instance attributeClone() {
            Instance instance = this;
            Instance new_i = new Instance(instance.id.eventId, instance.id.instanceId);
            new_i.setInterval(instance.intervalStart, instance.intervalEnd);
            new_i.setLocation(instance.location);
            return new_i;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Instance that = (Instance)o;
            return this.id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public String toString() {
            return id.toString();
        }
    }

    public static class InstanceId {
        int eventId;
        int instanceId;

        public InstanceId(int eventId, int instanceId) {
            this.instanceId = instanceId;
            this.eventId = eventId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InstanceId that = (InstanceId) o;

            if (eventId != that.eventId) return false;
            if (instanceId != that.instanceId) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = eventId;
            result = 31 * result + instanceId;
            return result;
        }

        @Override
        public String toString() {
            return eventId + "_" + instanceId;
        }
    }
}
