package jdg.clustering;

import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;

/**
 * This class provides methods for computing the modularity of a partition and
 * for solving the community detection problem
 * 
 * @author Luca Castelli Aleardi (INF421, 2017)
 */
public abstract class CommunityDetection {

	/**
	 * This method returns a partition of a network of size 'n' into communities. <p>
	 * <p>
	 * Remarks:<p>
	 * -) the nodes of the networks are numbered 0..n-1
	 * -) the graph is partitioned into 'k' communities, which have numbers 0..k-1
	 * 
	 * @param graph  the input network (adjacency list representation)
	 * @return an array of size 'n' storing, for each vertex, the index of its community (a value between 0..,k-1)
	 */
	public abstract int[] computeClusters(AdjacencyListGraph graph);
	
	/**
	 * This method computes the modularity of a partition of the input graph whose nodes
	 * are regrouped into 'k' communities. <p>
	 * <p>
	 * Remarks:<p>
	 * -) the nodes of the networks are numbered 0..n-1
	 * -) the graph is partitioned into 'k' communities, which have numbers 0..k-1
	 * 
	 * @param graph  the input network (adjacency list representation)
	 * @param communities  an array of size 'n' storing, for each vertex, the index of its community (a value between 0..,k-1)
	 * @return  the modularity of the partition
	 */
	public double computeModularity(AdjacencyListGraph graph, int[] communities) {
		System.err.println("To be completed: question 3");
		return -2.; // modularity is not defined (this method must be implemented)
	}

	/**
	 * This method returns the community of a vertex <p>
	 * <p>
	 * Remarks:<p>
	 * -) the graph is assumed to be endowed with a partition of its nodes into 'k' communities
	 * -) the nodes of the networks are numbered 0..n-1
	 * 
	 * @param graph  the input network (adjacency list representation)
	 * @param communities  an array of size 'n' storing, for each vertex, the index of its community (a value between 0..,k-1)
	 * @param u  a node in the network (whose index is a number between 0 and n-1)
	 * @return  the community to which node 'u' belongs: the result is a value between 0 and k-1
	 */
	public int getCommunity(AdjacencyListGraph graph, int[] communities, Node u) {
		if(u.index<0 || u.index>=graph.sizeVertices()) {
			throw new Error("Error: wrong vertex number v"+u.index);
		}
		
		return communities[u.index];
	}

}
