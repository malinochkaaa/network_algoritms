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
package org.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import com.sun.source.tree.NewArrayTree;
import org.example.algorithms.AStar;
import org.example.algorithms.BellmanFord;
import org.example.algorithms.Dijkstra;
import org.example.algorithms.Greedy;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.reports.GraphObserver;

/**
 * The control class extends the functionality of peersim.reports.GraphObserver to DistanceVectorProtocol protocol.
 * The class prints nodes and edge costs into out file using DOT language.
 * PREREQUISITE, must be used for DistanceVectorProtocol protocol in PeerSim.
 */
public class GraphPrinter extends GraphObserver {
	
	private static final String PAR_PROT = "protocol";	
    private static final String PAR_FILEPATH = "outf";

	
	private final int pid;
    private String outf;

    private long end_time;
	/**
	 * A constructor
	 * @param prefix a string provided by PeerSim and used to access parameters from the configuration file.
	 */
	public GraphPrinter(String prefix) {
		super(prefix);
		pid  = Configuration.getPid(prefix + "." + PAR_PROT);
		outf = Configuration.getString(prefix + "." + PAR_FILEPATH);
	}
	
	/**
	 * Implementation of the common method. This method is called in each cycle. 
	 */
	@Override
	public boolean execute() {
		final int n = Network.size();
        int l=0, m = 0;
        System.out.println( "[" + CommonState.getTime() + "] drawing ... ");
        try {
            if (CommonState.getTime() > 1) {
                int[] drawn = new int[n];
                Date date = new Date(System.currentTimeMillis());
                BaseProtocol baseProtocol = (BaseProtocol) Network.get(0).getProtocol(pid);
                String protocolStr = "";
                if (baseProtocol instanceof BellmanFord) {
                    protocolStr = "bellman";
                } else if (baseProtocol instanceof Dijkstra) {
                    protocolStr = "dijkstra";
                }else if (baseProtocol instanceof Greedy) {
                    protocolStr = "greedy";
                }else if (baseProtocol instanceof AStar) {
                    protocolStr = "a*";
                }
                String filename = String.format("%s%s%03d.graph", outf, protocolStr, CommonState.getTime());
                File f = new File(filename);
                f.createNewFile();
                Formatter file = new Formatter(f);
                file.format("// %s \ngraph random { ratio=\"fill\"; margin=0; \n", date.toString());

                for (int i = 0; i < n; i++) {

                    BaseProtocol protocol = (BaseProtocol) Network.get(i).getProtocol(pid);

                    Linkable link = (Linkable) Network.get(i).getProtocol(FastConfig.getLinkable(pid));

                    for (int j = 0; j < link.degree(); j++) {
                        long peerId = link.getNeighbor(j).getID();
                        int cost = -1;
                        if (protocol.getPaths() != null && protocol.getPaths().containsKey(peerId))
                            cost = protocol.getPaths().get(peerId).cost;

                        if (cost != -1 && cost != Integer.MAX_VALUE) {
                            if (undir) {
                                m = 0;
                                while (m < l && peerId != drawn[m]) m++;
                                if (m < l) continue;
                                file.format("   %d -- %d [dir=both, cost=%d];\n", i, peerId, cost);
                            } else {
                                file.format("   %d -> %d;\n", i, peerId);
                            }
                        }
                    }
                    drawn[l++] = (int) Network.get(i).getID();
                }
                file.format(" }");
                file.close();
                String edgeFile = String.format("%s%s%03d_edges.dat", outf, protocolStr, CommonState.getTime());
                f = new File(edgeFile);
                f.createNewFile();
                file = new Formatter(f);
                List<String> list = null;
                try {
                    java.nio.file.Path tmp = java.nio.file.Path.of("tmp_coords.txt");
                    list = Files.readAllLines(tmp);
                } catch (IOException e) {}
                List<String[]> newList = list.stream().map(s -> s.split(":")).toList();
                for (int i = 0; i < n; i++) {
                    BaseProtocol protocol = (BaseProtocol) Network.get(i).getProtocol(pid);
                    for (Edge e: protocol.getGraph()) {
                        if (e.source != i) continue;
                        Files.write(Path.of(edgeFile), String.format("%s %s %d\n", newList.get((int)e.source)[1], newList.get((int)e.source)[2], e.source).getBytes(), StandardOpenOption.APPEND);
                        Files.write(Path.of(edgeFile), String.format("%s %s %d\n", newList.get((int)e.destination)[1], newList.get((int)e.destination)[2], e.destination).getBytes(), StandardOpenOption.APPEND);
                        Files.write(Path.of(edgeFile), ("\n").getBytes(), StandardOpenOption.APPEND);
                    }
                }
                file.close();
            }
        } catch (Exception ex) { ex.printStackTrace(); }
        System.out.println("done.");
		
		return false;
	}
}
