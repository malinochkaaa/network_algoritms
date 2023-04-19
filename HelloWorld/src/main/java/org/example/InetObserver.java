package org.example;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.graph.Graph;
import peersim.reports.GraphObserver;
import peersim.util.FileNameGenerator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class InetObserver extends GraphObserver {
    // ------------------------------------------------------------------------
    // Parameters
    // ------------------------------------------------------------------------
    private static final String PAR_FILENAME_BASE = "file_base";
    private static final String PAR_COORDINATES_PROT = "coord_protocol";
    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------
    private final String graph_filename;

    private final FileNameGenerator fng;
    private final int coordPid;

    // ------------------------------------------------------------------------
// Constructor
// ------------------------------------------------------------------------
    public InetObserver(String prefix) {
        super(prefix);
        coordPid = Configuration.getPid(prefix + "." + PAR_COORDINATES_PROT);
        graph_filename = Configuration.getString(prefix + "."
                + PAR_FILENAME_BASE, "graph_dump");
        fng = new FileNameGenerator(graph_filename, ".dat");
    }

    // Control interface method.
    public boolean execute() {
        try {
            updateGraph();
            System.out.print(name + ": ");
            // initialize output streams
            String fname = fng.nextCounterName();
            FileOutputStream fos = new FileOutputStream(fname);
            System.out.println("Writing to file " + fname);
            PrintStream pstr = new PrintStream(fos);
            // dump topology:
            graphToFile(g, pstr, coordPid);
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    private static void graphToFile(Graph g, PrintStream ps, int coordPid) {
        for (int i = 1; i < g.size(); i++) {
            Node current = (Node) g.getNode(i);
            double x_to = ((InetCoordinates) current
                    .getProtocol(coordPid)).getX();
            double y_to = ((InetCoordinates) current
                    .getProtocol(coordPid)).getY();
            for (int index : g.getNeighbours(i)) {
                Node n = (Node) g.getNode(index);
                double x_from = ((InetCoordinates) n
                        .getProtocol(coordPid)).getX();
                double y_from = ((InetCoordinates) n
                        .getProtocol(coordPid)).getY();
                ps.println(x_from + " " + y_from);
                ps.println(x_to + " " + y_to);
                ps.println();
            }
        }
    }
}
