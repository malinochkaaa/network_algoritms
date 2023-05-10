package org.example.algorithms;

import org.example.BaseProtocol;
import org.example.Edge;
import org.example.Path;
import peersim.core.Network;

import java.util.Optional;

public class FloydWarshall extends BaseProtocol {
    public FloydWarshall(String prefix) {
        super(prefix);
    }
    @Override
    protected boolean compute(long nodeId, long to) {
        for (long i = 0; i < Network.size(); i++) {
            paths.put(i, new Path(i, i, Integer.MAX_VALUE));
        }
        /* Source node costs 0 */
        paths.get(nodeId).cost = 0;
        /* Relax edges repeatedly */

        for (long k = 0; k < Network.size(); k++) {
            // Pick all vertices as source one by one
            for (long i = 0; i < Network.size(); i++) {
                // Pick all vertices as destination for the
                // above picked source
                for (long j = 0; j < Network.size(); j++) {
                    // If vertex k is on the shortest path
                    // from i to j, then update the value of
                    // dist[i][j]
                    /*if (dist[i][k] + dist[k][j]
                            < dist[i][j])
                        dist[i][j]
                                = dist[i][k] + dist[k][j];*/
                }
            }
        }
        for (int i = 0; i < (Network.size() - 1); i++) {
            /* For every edge */
            for (org.example.Edge Edge : graph) {
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
        return true;
    }
}
