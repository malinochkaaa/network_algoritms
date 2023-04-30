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
package org.example.bellman_ford;

import java.util.TreeMap;

import org.example.Path;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

/**
 * The class observes DistanceVectorProtocol protocol and provide readable printout.
 * The observer prints to output screen the shortest path tree form each source node and in each cycle.
 * PREREQUISITE, must be used for DistanceVectorProtocol protocol in PeerSim.
 */
public class BFObserver implements Control{

	private final int pid;											//DistanceVectorProtocol protocol ID
	/**
	 * A constructor
	 * @param prefix a string provided by PeerSim and used to access parameters from the configuration file.
	 */
	public BFObserver(String prefix) {
		this.pid  = Configuration.getPid(prefix + ".protocol");
	}
	
	/**
	 * Implementation of the common method. This method is called in each cycle. 
	 */
	@Override
	public boolean execute() {
		for(int i=0; i < Network.size(); i++ ) {										//for each node in network
			
			Node node = Network.get(i);													//reference node i
			BellmanFordProtocol protocol = (BellmanFordProtocol) node.getProtocol(pid);	//get DV protocol
			TreeMap<Long, Path> paths = protocol.getPaths();							//get path tree
						
			if(paths==null) continue;											
																						//print tree
			System.out.printf("ND#%4d->| ", Network.get(i).getID());
			for(Path p : paths.values()) System.out.printf("%4d,", p.destination );
			System.out.printf(" |%s\n    cost| ", "");
			for(Path p : paths.values())
				if(p.cost == Integer.MAX_VALUE)
					System.out.printf(" X ," );
				else
					System.out.printf("%4d,", p.cost );
			System.out.printf(" |\n     via| ");
			for(Path p : paths.values()) System.out.printf("%4d,", p.predecessor );
			System.out.printf(" |\n----------------------------------------------------------------------------\n");
		}
		return false;
	}
}
