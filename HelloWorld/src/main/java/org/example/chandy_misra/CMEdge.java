package org.example.chandy_misra;

import org.example.Edge;

public class CMEdge extends Edge {

    public int initiator;
    /**
     * A constructor.
     *
     * @param source
     * @param destination
     * @param cost
     */
    public CMEdge(long source, long destination, int cost, int initiator) {
        super(source, destination, cost);
        this.initiator=initiator;
    }
}
