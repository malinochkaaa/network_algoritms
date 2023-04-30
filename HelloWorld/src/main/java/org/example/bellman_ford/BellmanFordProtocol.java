/*
 * Copyright (c) 2018,
 *     University of Reading, Computer science Department
 *     CS2CA17 module - lab sessions
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU,
 * Lesser General Public License version 2 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 */
package org.example.bellman_ford;

import org.example.CostInitialiser;
import org.example.Path;
import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;


/**
 * The class implements a network layer Distance-Vector protocol.
 * It is based on the original Link-State protocol class.
 *
 * The protocol broadcast its local graph to all nodes in the network.
 * Each instance of the protocol then computes the shortest path tree using the Bellman-Ford algorithm.
 */
public class BellmanFordProtocol implements CDProtocol {

    /* Enumerated states */
    private enum State {
        INITIALISE, BROADCAST, COMPUTE;
    }

    /* Network graph as a set of edges (Unvisited nodes) */
    private ArrayList<BFEdge> graph = new ArrayList<>();
    /* shortest path tree (visited nodes) */
    private TreeMap<Long, Path> paths;
    /* current phase of the protocol */
    private State phase;

    private ArrayList<BFNode> unavailableNodes = new ArrayList<>();

    /**
     * A constructor.
     *
     * @param prefix required by PeerSim to access protocol's alias in the configuration file.
     */
    public BellmanFordProtocol(@SuppressWarnings("unused") String prefix) {
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
        //System.out.println(host.getIndex());
        /* Reference local Linkable protocol */
        Linkable lnk = (Linkable) host.getProtocol(FastConfig.getLinkable(pid));
        /* Reference host's node ID */
        long nodeId = host.getID();
        for (int i = 0; i < unavailableNodes.size(); i++) {
            if (unavailableNodes.get(i).assosiateWith == nodeId) {
                unavailableNodes.get(i).availableIn--;
                if (unavailableNodes.get(i).availableIn == 0) {
                    if (unavailableNodes.get(i).nodeId == nodeId) {
                        System.out.println("Message from node " + unavailableNodes.get(i).nodeId + " successfully sent!");
                    } else {
                        System.out.println("Message to node " + unavailableNodes.get(i).nodeId + " successfully sent!");
                    }
                } else {
                    if (unavailableNodes.get(i).nodeId == nodeId) {
                        System.out.println("Message from node " + unavailableNodes.get(i).nodeId + " still in process, "
                                + unavailableNodes.get(i).availableIn + " cycles left.");
                    } else {
                        System.out.println("Message to node " + unavailableNodes.get(i).nodeId + " still in process, "
                                + unavailableNodes.get(i).availableIn + " cycles left.");
                    }
                }
            }
        }
        for (BFEdge edge: graph) {
            if (edge.nodeIdAssosiated == nodeId) {
                if (edge.availableIn > 0) {
                    edge.availableIn--;
                }
            }
        }
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
                compute(nodeId);
                trySendMessage(nodeId);
            }
        }
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
            graph.add(new BFEdge(nodeId, neighborId, cost));
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
            Node tempNode = Network.get(i);
            /* Access DV protocol in node i */
            BellmanFordProtocol tempProtocol = (BellmanFordProtocol) tempNode.getProtocol(pid);
            /* Copy local graph */
            ArrayList<BFEdge> tempGraph = new ArrayList<>(graph);
            /* Send the copy to node i */
            tempProtocol.receive(tempGraph);
        }
    }

    /**
     * Compute shortest path using Bellman-Ford algorithm.
     * @param nodeId Host Node ID.
     */
    private void compute(long nodeId) {
        /* Do it only once */
        /*if (done) {
            return;
        }*/
        /* Initialise graph */
        for (long i = 0; i < Network.size(); i++) {
            paths.put(i, new Path(i, i, Integer.MAX_VALUE));
        }
        /* Source node costs 0 */
        paths.get(nodeId).cost = 0;
        /* Relax edges repeatedly */
        for (int i = 0; i < (Network.size() - 1); i++) {
            /* For every edge */
            for (BFEdge Edge : graph) {
                if (Edge.availableIn > 0) {
                    continue;
                }
                /* Get indexes */
                long src = Edge.source;
                long dst = Edge.destination;
                /* Skip unreached (can't add to infinity) */
                if (paths.get(src).cost >= Integer.MAX_VALUE) {
                    continue;
                }
                /* Calculate costs */
                int cost = Edge.cost;
                int oldCost = paths.get(dst).cost;
                int newCost = paths.get(src).cost + cost;
                /* Update if cost has decreased */
                if (newCost < oldCost) {
                    paths.get(dst).cost = newCost;
                    /* If direct path */
                    if (Edge.source == nodeId) {
                        paths.get(dst).predecessor = Edge.destination;
                    } else {
                        paths.get(dst).predecessor = Edge.source;
                    }
                }
            }
        }
    }

    private void trySendMessage(long from) {
        int chance = CDState.r.nextInt(5);
        if (chance > 0) return;
        long to = CDState.r.nextLong(Network.size());
        if (unavailableNodes.stream().anyMatch(bfNode -> (bfNode.nodeId == from ||
                bfNode.nodeId == to) && bfNode.availableIn > 0) || from == to) {
            return;
        }
        int messageWeight = CDState.r.nextInt(3) + 1;
        unavailableNodes.add(new BFNode(from, from, messageWeight));
        unavailableNodes.add(new BFNode(to, from, messageWeight));
        findPath(from, to, messageWeight);
    }

    private void findPath(long from, long to, int weight) {
        System.out.println("Sending message from " + from + " to " + to + " for " + weight + " cycles... Computing path:");
        ArrayList<Long> path = new ArrayList<>();
        path.add(to);
        long pathLast = to;
        long prevInPath = paths.get(pathLast).predecessor;
        while (pathLast != from) {
            for (BFEdge edge: graph) {
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
        for (BFEdge edge: graph) {
            if (edge.source == from && edge.destination == prevInPath) {
                edge.availableIn = weight;
                edge.nodeIdAssosiated = from;
            }
        }
        Collections.reverse(path);
        for (int i = 0 ; i < path.size(); i++) {
            if (i == 0) {
                System.out.print("(" + path.get(i) + " ->");
            } else if (i == path.size() - 1) {
                System.out.println(" " + path.get(i) + ")");
            } else {
                System.out.print(" " + path.get(i) + " ->");
            }
        }
    }

    /**
     * Receives a graph form a neighbour and updates local graph
     * removes duplicate edges if any
     *
     * @param neighborGraph a copy of neighbour's local graph
     */
    private void receive(ArrayList<BFEdge> neighborGraph) {
        /* For each edge in the neighbour's graph */
        for (BFEdge aNeighborGraph : neighborGraph) {
            /* Ignore duplicate edges */
            boolean duplicate = false;
            for (BFEdge aGraph : graph) {
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
}
