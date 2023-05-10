package org.example;


public class CustomNode implements Comparable {

    public long nodeId;

    public int availableIn = 0;
    public long assosiateWith = 0;
    public int cost = Integer.MAX_VALUE;
    public int g = Integer.MAX_VALUE;
    public int h;

    public CustomNode(long nodeId, long assosiateWith, int availableIn) {
        this.nodeId = nodeId;
        this.availableIn = availableIn;
        this.assosiateWith = assosiateWith;
    }
    public CustomNode(long nodeId, int cost) {
        this.nodeId = nodeId;
        this.cost = cost;
    }
    public CustomNode(int h, long nodeId) {
        this.nodeId = nodeId;
        this.h = h;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof CustomNode)) return 0;
        if (this.cost < ((CustomNode) o).cost)
            return -1;

        if (this.cost > ((CustomNode) o).cost)
            return 1;
        return 0;
    }
}
