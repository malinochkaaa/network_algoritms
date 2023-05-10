package org.example.algorithms;

import org.example.BaseProtocol;
import org.example.Edge;
import org.example.Path;
import peersim.core.Network;

import java.util.ArrayList;
import java.util.List;


public class Greedy extends BaseProtocol {

    private List<Long> visited = new ArrayList<Long>();

    public Greedy(String prefix) {
        super(prefix);
    }
    @Override
    protected boolean compute(long nodeId, long to) {
        visited.add(nodeId);
        for (long i = 0; i < Network.size(); i++) {
            paths.put(i, new Path(i, i, Integer.MAX_VALUE));
        }
        return findPath(nodeId, to) != Integer.MAX_VALUE;
    }

    private long findPath(long from, long to) {
        long nodeTo = -1;
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < graph.size(); i++) {
            Edge v = graph.get(i);
            if (v.source != from || v.availableIn > 0 || visited.contains(v.destination)) {
                continue;
            }
            int weight = v.cost;
            if (weight < min) {
                nodeTo = v.destination;
                min = weight;
            }
        }

        if (nodeTo != -1) {
            paths.get(nodeTo).predecessor = from;
            visited.add(nodeTo);
        }

        if (nodeTo != to && nodeTo != -1) {
            min += findPath(nodeTo, to);
            paths.get(nodeTo).cost = min;
        }
        return min;
    }
}
