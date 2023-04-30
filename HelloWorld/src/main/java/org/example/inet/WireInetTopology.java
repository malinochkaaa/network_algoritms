package org.example.inet;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.WireGraph;
import peersim.graph.Graph;

public class WireInetTopology extends WireGraph {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------
    private static final String PAR_ALPHA = "alpha";
    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    // --------------------------------------------------------------------------
    // Fields
    // --------------------------------------------------------------------------
    /* A parameter that affects the distance importance. */
    private final double alpha;
    /** Coordinate protocol pid. */
    private final int coordPid;
    // -------------------------------
    // Initialization
// --------------------------------------------------------------------------
    public WireInetTopology(String prefix) {
        super(prefix);
        alpha = Configuration.getDouble(prefix + "." + PAR_ALPHA, 0.5);
        coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
    }
    /**
     * Performs the actual wiring.
     * @param g
     *            a {@link peersim.graph.Graph} interface object to work on.
     */
    public void wire(Graph g) {
        /** Contains the distance in hops from the root node for each node. */
        int[] hops = new int[Network.size()];
        // connect all the nodes other than roots
        for (int i = 1; i < Network.size(); ++i) {
            Node n = (Node) g.getNode(i);
            // Look for a suitable parent node between those allready part of
            // the overlay topology: alias FIND THE MINIMUM!
            // Node candidate = null;
            int candidate_index = 0;
            double min = Double.POSITIVE_INFINITY;
            for (int j = 0; j < i; j++) {
                Node parent = (Node) g.getNode(j);
                double jHopDistance = hops[j];
                double value = jHopDistance
                        + (alpha * distance(n, parent, coordPid));
                if (value < min) {
                    // candidate = parent; // best parent node to connect to
                    min = value;
                    candidate_index = j;
                } }
            hops[i] = hops[candidate_index] + 1;
            g.setEdge(i, candidate_index);
        }
    }
    private static double distance(Node new_node, Node old_node, int coordPid) {
        double x1 = ((InetCoordinates) new_node.getProtocol(coordPid))
                .getX();
        double x2 = ((InetCoordinates) old_node.getProtocol(coordPid))
                .getX();
        double y1 = ((InetCoordinates) new_node.getProtocol(coordPid))
                .getY();
        double y2 = ((InetCoordinates) old_node.getProtocol(coordPid))
                .getY();
        if (x1 == -1 || x2 == -1 || y1 == -1 || y2 == -1)
            // NOTE: in release 1.0 the line above incorrectly contains
            // |-s instead of ||. Use latest CVS version, or fix it by hand.
            throw new RuntimeException(
                    "Found un-initialized coordinate. Use e.g., InetInitializer class in the config file.");
        return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
    }
}
