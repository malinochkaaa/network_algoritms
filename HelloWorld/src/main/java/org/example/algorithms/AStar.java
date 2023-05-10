package org.example.algorithms;

import org.example.BaseProtocol;
import org.example.CustomNode;
import org.example.Edge;
import org.example.Path;
import peersim.core.Network;

import java.util.*;

public class AStar extends BaseProtocol {

    List<CustomNode> allNodes = new ArrayList<CustomNode>();

    public AStar(String prefix) {
        super(prefix);
    }
    @Override
    protected boolean compute(long nodeId, long to) {
        bfs(to, 0);
        CustomNode start = getById(nodeId);
        CustomNode end = getById(to);
        return aStar(start, end) != null;
    }

    private void bfs(long to, int h) {
        Queue<Long> queue = new ArrayDeque<Long>();
        allNodes.add(new CustomNode(h, to));
        for (Edge edge: graph) {
            if (edge.source != to || edge.availableIn > 0) continue;
            queue.add(edge.destination);
        }
        while (!queue.isEmpty()) {
            long current = queue.remove();
            if (allNodes.stream().anyMatch(customNode -> customNode.nodeId == current)) continue;
            bfs(current, h + 1);
        }
    }

    private CustomNode aStar(CustomNode start, CustomNode target){
        for (long i = 0; i < Network.size(); i++) {
            paths.put(i, new Path(i, i, Integer.MAX_VALUE));
        }
        PriorityQueue<CustomNode> closedList = new PriorityQueue<>();
        PriorityQueue<CustomNode> openList = new PriorityQueue<>();

        start.g = 0;
        start.cost = start.g + start.h;
        openList.add(start);

        while (!openList.isEmpty()){
            CustomNode n = openList.peek();
            if(n == target){
                return n;
            }

            for(Edge edge : graph){
                if (edge.source != n.nodeId || edge.availableIn > 0) {
                    continue;
                }

                int totalWeight = n.g + edge.cost;

                CustomNode m = getById(edge.destination);

                if (!openList.contains(m) && !closedList.contains(m)) {
                    paths.get(m.nodeId).cost = totalWeight;
                    paths.get(m.nodeId).predecessor = n.nodeId;
                    m.g = totalWeight;
                    m.cost = m.g + m.h;
                    openList.add(m);
                } else {
                    if(totalWeight < m.g){
                        paths.get(m.nodeId).cost = totalWeight;
                        paths.get(m.nodeId).predecessor = n.nodeId;
                        m.g = totalWeight;
                        m.cost = m.g + m.h;

                        if(closedList.contains(m)){
                            closedList.remove(m);
                            openList.add(m);
                        }
                    }
                }
            }

            openList.remove(n);
            closedList.add(n);
        }
        return null;
    }

    private CustomNode getById(long id) {
        return allNodes.stream().filter(customNode -> customNode.nodeId == id).findFirst().get();
    }
}
