package org.example;

import org.example.algorithms.BellmanFord;
import org.example.algorithms.Dijkstra;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.TreeMap;

public abstract class BaseProtocol implements CDProtocol {

    private enum State {
        INITIALISE, BROADCAST, COMPUTE;
    }

    /* Network graph as a set of edges (Unvisited nodes) */
    protected ArrayList<Edge> graph = new ArrayList<>();
    /* shortest path tree (visited nodes) */
    protected TreeMap<Long, Path> paths;
    /* current phase of the protocol */
    protected State phase;

    protected ArrayList<CustomNode> unavailableNodes = new ArrayList<>();

    /**
     * A constructor.
     *
     * @param prefix required by PeerSim to access protocol's alias in the configuration file.
     */
    public BaseProtocol(@SuppressWarnings("unused") String prefix) {
        /* Start in INIT phase */
        this.phase = State.INITIALISE;
    }

    /**
     * PeerSim cyclic service. To be execute every cycle.
     *
     * @param host Reference to host node.
     * @param pid Global protocol's ID in this simulation.
     */
    @Override
    public void nextCycle(Node host, int pid) {
        /* Reference local Linkable protocol */
        Linkable lnk = (Linkable) host.getProtocol(FastConfig.getLinkable(pid));
        /* Reference host's node ID */
        long nodeId = host.getID();
        checkAvailability(nodeId);
        /* Current phase */
        switch (phase) {
            case INITIALISE -> {
                /* Initialise graph */
                init(lnk, nodeId);
                /* Transit to next phase */
                phase = State.BROADCAST;
            }
            case BROADCAST -> {
                /* Broadcast the local graph */
                broadcast(pid);
                /* Transit to next phase */
                phase = State.COMPUTE;
            }
            case COMPUTE -> {
                /* Compute shortest paths */
                long to = generateNodeTo(nodeId);
                if (to != -1) {
                    compute(nodeId, to);
                    sendMessage(nodeId, to);
                }
            }
        }
    }

    private long generateNodeTo(long from) {
        int chance = CDState.r.nextInt(5);
        if (chance > 0) return -1;
        long to = CDState.r.nextLong(Network.size());
        if (unavailableNodes.stream().anyMatch(bfNode -> (bfNode.nodeId == from ||
                bfNode.nodeId == to) && bfNode.availableIn > 0) || from == to) {
            return -1;
        }
        return to;
    }

    /**
     * Initialises local graph.
     *
     * @param lnk Reference local Linkable protocol.
     * @param nodeId Host Node ID.
     */
    private void init(Linkable lnk, long nodeId) {
        long neighborId;
        /* Create information containers */
        this.graph = new ArrayList<>();
        this.paths = new TreeMap<>();
        /* Add neighbours - access neighbours in the Linkable */
        for (int i = 0; i < lnk.degree(); i++) {
            /* Get neighbour's i ID */
            neighborId = lnk.getNeighbor(i).getID();
            /* Get cost of the link between this node and neighbour i */
            int cost = CostInitialiser.getCost(nodeId, neighborId);
            /* Add edge to local graph */
            graph.add(new Edge(nodeId, neighborId, cost));
        }
    }

    /**
     * Broadcasts local graph to peers.
     * @param pid Global protocol's ID in this simulation.
     */
    private void broadcast(int pid) {
        /* Get network size */
        int size = Network.size();
        /* Broadcast to all nodes */
        for (int i = 0; i < size; i++) {
            /* Access node i */
            peersim.core.Node tempNode = Network.get(i);
            /* Access DV protocol in node i */
            BaseProtocol tempProtocol = (BaseProtocol) tempNode.getProtocol(pid);
            /* Copy local graph */
            ArrayList<Edge> tempGraph = new ArrayList<>(graph);
            /* Send the copy to node i */
            tempProtocol.receive(tempGraph);
        }
    }

    /**
     * Compute shortest path using Bellman-Ford algorithm.
     * @param nodeId Host Node ID.
     */
    protected abstract void compute(long nodeId, long to);

    /**
     * Receives a graph form a neighbour and updates local graph
     * removes duplicate edges if any
     *
     * @param neighborGraph a copy of neighbour's local graph
     */
    private void receive(ArrayList<Edge> neighborGraph) {
        /* For each edge in the neighbour's graph */
        for (Edge aNeighborGraph : neighborGraph) {
            /* Ignore duplicate edges */
            boolean duplicate = false;
            for (Edge aGraph : graph) {
                if (aNeighborGraph.equals(aGraph)) {
                    duplicate = true;
                    break;
                }
            }
            /* Add new edge */
            if (!duplicate) {
                graph.add(aNeighborGraph);
            }
        }
    }

    /**
     * Access to local path tree. Used bye the observer.
     *
     * @return the local path tree
     */
    public TreeMap<Long, Path> getPaths() {
        return paths;
    }

    /**
     * used by PeerSim to clone this protocol at the start of the simulation
     */
    @Override
    public Object clone() {
        Object o = null;
        try {
            o = super.clone();
        } catch (Exception ignored) { }
        return o;
    }

    private void checkAvailability(long nodeId){
        for (CustomNode unavailableNode : unavailableNodes) {
            if (unavailableNode.assosiateWith == nodeId) {
                unavailableNode.availableIn--;
                if (unavailableNode.availableIn == 0) {
                    if (unavailableNode.nodeId == nodeId) {
                        messagePrint("Message from node " + unavailableNode.nodeId + " successfully sent!");
                    } else {
                        messagePrint("Message to node " + unavailableNode.nodeId + " successfully sent!");
                    }
                } else {
                    if (unavailableNode.nodeId == nodeId) {
                        messagePrint("Message from node " + unavailableNode.nodeId + " still in process, "
                                + unavailableNode.availableIn + " cycles left.");
                    } else {
                        messagePrint("Message to node " + unavailableNode.nodeId + " still in process, "
                                + unavailableNode.availableIn + " cycles left.");
                    }
                }
            }
        }
        unavailableNodes.removeIf(node -> node.availableIn == 0);
        for (Edge edge: graph) {
            if (edge.nodeIdAssosiated == nodeId) {
                if (edge.availableIn > 0) {
                    edge.availableIn--;
                }
            }
        }
    }

    private void sendMessage(long from, long to) {
        int messageWeight = CDState.r.nextInt(3) + 1;
        unavailableNodes.add(new CustomNode(from, from, messageWeight));
        unavailableNodes.add(new CustomNode(to, from, messageWeight));
        findPath(from, to, messageWeight);
    }

    private void findPath(long from, long to, int weight) {
        String msg = "Sending message from " + from + " to " + to + " for " + weight + " cycles... Computing path:";
        messagePrint(msg);
        ArrayList<Long> path = new ArrayList<>();
        path.add(to);
        long pathLast = to;
        long prevInPath = paths.get(pathLast).predecessor;
        while (pathLast != from) {
            for (Edge edge: graph) {
                if (edge.source == prevInPath && edge.destination == pathLast) {
                    edge.availableIn = weight;
                    edge.nodeIdAssosiated = from;
                }
            }
            pathLast = prevInPath;
            prevInPath = paths.get(pathLast).predecessor;
            path.add(pathLast);
            if (pathLast == prevInPath) {
                pathLast = from;
            }
        }
        path.add(from);
        for (Edge edge: graph) {
            if (edge.source == from && edge.destination == prevInPath) {
                edge.availableIn = weight;
                edge.nodeIdAssosiated = from;
            }
        }
        Collections.reverse(path);
        msg = "";
        for (int i = 0 ; i < path.size(); i++) {
            if (i == 0) {
                msg ="(" + path.get(i) + " ->";
            } else if (i == path.size() - 1) {
                msg +=" " + path.get(i) + ")";
                messagePrint(msg);
            } else {
                msg += " " + path.get(i) + " ->";
            }
        }
    }

    private void messagePrint(String msg) {
        String protocolStr = "";
        if (this instanceof BellmanFord) {
            protocolStr = "bellman";
        } else if (this instanceof Dijkstra) {
            protocolStr = "dijkstra";
        }
        String filename = String.format("messages/%s%03d.txt", protocolStr, CommonState.getTime());
        File f = new File(filename);
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            Files.write(Paths.get(filename), (msg + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
    }
}
