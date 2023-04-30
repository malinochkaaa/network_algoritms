package org.example.bellman_ford;

public class BFNode {

    public long nodeId;

    public int availableIn = 0;
    public long assosiateWith = 0;

    public BFNode(long nodeId, long assosiateWith, int availableIn) {
        this.nodeId = nodeId;
        this.availableIn = availableIn;
        this.assosiateWith = assosiateWith;
    }
}
