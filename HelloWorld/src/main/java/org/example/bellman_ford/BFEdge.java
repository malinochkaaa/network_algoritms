package org.example.bellman_ford;

import org.example.Edge;

public class BFEdge extends Edge {

    public int availableIn = 0;
    public long nodeIdAssosiated;
    /**
     * A constructor.
     *
     * @param source
     * @param destination
     * @param cost
     */
    public BFEdge(long source, long destination, int cost) {
        super(source, destination, cost);
    }
}
