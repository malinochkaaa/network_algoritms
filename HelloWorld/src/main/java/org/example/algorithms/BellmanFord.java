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

import org.example.*;
import peersim.core.Network;


/**
 * The class implements a network layer Distance-Vector protocol.
 * It is based on the original Link-State protocol class.
 *
 * The protocol broadcast its local graph to all nodes in the network.
 * Each instance of the protocol then computes the shortest path tree using the Bellman-Ford algorithm.
 */
public class BellmanFord extends BaseProtocol {

    /**
     * A constructor.
     *
     * @param prefix required by PeerSim to access protocol's alias in the configuration file.
     */
    public BellmanFord(String prefix) {
        super(prefix);
    }

    /**
     * Compute shortest path using Bellman-Ford algorithm.
     * @param nodeId Host Node ID.
     */
    @Override
    protected void compute(long nodeId, long to) {
        /* Initialise graph */
        for (long i = 0; i < Network.size(); i++) {
            paths.put(i, new Path(i, i, Integer.MAX_VALUE));
        }
        /* Source node costs 0 */
        paths.get(nodeId).cost = 0;
        /* Relax edges repeatedly */
        for (int i = 0; i < (Network.size() - 1); i++) {
            /* For every edge */
            for (Edge Edge : graph) {
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
}
