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
package org.example.algorithms;

import org.example.BaseProtocol;
import org.example.CustomNode;
import org.example.Edge;
import org.example.Path;
import peersim.core.Network;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * The class implements a network layer Distance-Vector protocol.
 * It is based on the original Link-State protocol class.
 *
 * The protocol broadcast its local graph to all nodes in the network.
 * Each instance of the protocol then computes the shortest path tree using the Dijkstra algorithm.
 */
public class Dijkstra extends BaseProtocol {
    private PriorityQueue<CustomNode> pq;
    private Set<Long> settled;

    /**
     * A constructor.
     *
     * @param prefix required by PeerSim to access protocol's alias in the configuration file.
     */
    public Dijkstra(String prefix) {
        super(prefix);
    }

    @Override
    protected boolean compute(long nodeId, long to) {
        pq = new PriorityQueue<>();
        settled = new HashSet<>();
        for (long i = 0; i < Network.size(); i++) {
            paths.put(i, new Path(i, i, Integer.MAX_VALUE));
        }
        // Add source node to the priority queue
        pq.add(new CustomNode(nodeId, 0));
        /* Source node costs 0 */
        paths.get(nodeId).cost = 0;

        while (settled.size() != Network.size()) {

            // Terminating condition check when
            // the priority queue is empty, return
            if (pq.isEmpty())
                return false;

            // Removing the minimum distance node
            // from the priority queue
            long u = pq.remove().nodeId;

            // Adding the node whose distance is
            // finalized
            if (settled.contains(u))

                // Continue keyword skips execution for
                // following check
                continue;

            // We don't have to call e_Neighbors(u)
            // if u is already present in the settled set.
            settled.add(u);

            e_Neighbours(u);
        }
        return true;
    }

    /**
     * Compute shortest path using Dijkstra algorithm.
     *
     */
    // Dijkstra's Algorithm
    // Method 2
    // To process all the neighbours
    // of the passed node
    private void e_Neighbours(long u) {

        int edgeDistance = -1;
        int newDistance = -1;

        // All the neighbors of v
        for (int i = 0; i < graph.size(); i++) {
            Edge v = graph.get(i);
            if (v.availableIn > 0) {
                continue;
            }
            if (v.source != u)
                continue;
            // If current node hasn't already been processed
            if (!settled.contains(v.destination)) {
                edgeDistance = v.cost;
                newDistance = paths.get(v.source).cost + edgeDistance;

                // If new distance is cheaper in cost
                if (newDistance < paths.get(v.destination).cost) {
                    paths.get(v.destination).cost = newDistance;
                    paths.get(v.destination).predecessor = v.source;
                }

                // Add the current node to the queue
                pq.add(new CustomNode(v.destination, paths.get(v.destination).cost));
            }
        }
    }
}