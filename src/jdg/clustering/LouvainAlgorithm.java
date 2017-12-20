package jdg.clustering;

import java.util.ArrayList;
import java.util.HashMap;

import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;

/**
 * This class provides an implementatino of the Louvain algorithm for Community detection
 * 
 * @author Luca Castelli Aleardi (INF421, 2017)
 */
public class LouvainAlgorithm extends CommunityDetection {

	/**
	 * Initialize the parameters of the Louvain algorithm
	 */
	public LouvainAlgorithm() {
		// TO DO
	}
	
	/**
	 * This method returns a partition of a network of size 'n' into communities,
	 * computed by the Louvain algorithm <p>
	 * <p>
	 * Remarks:<p>
	 * -) the nodes of the networks are numbered 0..n-1
	 * -) the graph is partitioned into 'k' communities, which have numbers 0..k-1
	 * 
	 * @param graph  the input network (adjacency list representation)
	 * @return an array of size 'n' storing, for each vertex, the index of its community (a value between 0..,k-1)
	 */
	public int[] computeClusters(AdjacencyListGraph graph) {
		int n=graph.sizeVertices();
		int m = graph.sizeEdges();
		int[] result=new int[n];
		int[] communities=new int[n];
		HashMap<Integer,ArrayList<int[]>> newGraph = new HashMap<Integer,ArrayList<int[]>>();
		int[] sum_in = new int[n];
		int[] sum_tot = new int[n];
		int[][] E = new int[n][n];
		for(Node node: graph.vertices){
			int k = node.index;
			if(newGraph.get(k) ==null) newGraph.put(k, new ArrayList<int[]>());
			for(Node neighbour: node.neighborsList()){
				newGraph.get(k).add(new int[]{neighbour.index,1});
				E[k][neighbour.index] = 1;
				E[neighbour.index][k] = 1;
			}
			sum_in[k] = 0;
			sum_tot[k] =  node.degree();
			communities[k] = k;
			result[k] = k;
		}
		
		
		boolean flag = true;
		boolean change = false;
		while(flag){
			change = false;
			while(flag){
				flag = false;
				int k =-1 ,l = -1;
		
				for(int node: newGraph.keySet()){
					for(int[] neighbour: newGraph.get(node)){
						int index_nbr = neighbour[0];
						if(find(communities,node) == find(communities,index_nbr)) continue;
						HashMap<Integer,Integer> connected_community = new HashMap<Integer,Integer>();
						connected_community.put(find(communities,node), 0);
						connected_community.put(find(communities,index_nbr), 0);
						int k_i = 0;
						for(int[] nbr: newGraph.get(node)){
							int index = nbr[0];
							k_i += nbr[1];
							if(connected_community.get(find(communities,index)) == null) connected_community.put(find(communities,index),1);
							else connected_community.put(find(communities,index),connected_community.get(find(communities,index))+1);
							
						}
					
						double deltaQ =  connected_community.get(find(communities,index_nbr))/2.0/m - 2.0* k_i* sum_tot[find(communities,index_nbr)]/(2.0*m)/(2.0*m);
						deltaQ += -connected_community.get(find(communities,node))/2.0/m - 2*k_i*(k_i-sum_tot[find(communities,node)])/(2.0*m)/(2.0*m);
						
						if(deltaQ > 0){
							flag = true;
							change = true;
							k = find(communities,index_nbr);
							l = find(communities,node);
						
							sum_tot[k] = sum_tot[k] + newGraph.get(node).size();
							sum_tot[l] = sum_tot[l] - newGraph.get(node).size();
							for(int nbr_community: connected_community.keySet()){
								E[k][nbr_community] = E[k][nbr_community] + connected_community.get(nbr_community);
								E[nbr_community][k] = E[k][nbr_community];
								E[l][nbr_community] = E[l][nbr_community] - connected_community.get(nbr_community);
								E[nbr_community][l] = E[l][nbr_community];
							}
							communities[node] = k;
							
							break;
						}
					}
				}
			}
			
			if(change == false) break;
			newGraph = new HashMap<Integer,ArrayList<int[]>>();
			for(Node node: graph.vertices){
				if(newGraph.get(find(communities,node.index)) == null) newGraph.put(find(communities,node.index), new ArrayList<int[]>());
			}
		
			for(int i: newGraph.keySet()){
				for(int j: newGraph.keySet()){
					if(i==j && E[i][i]>0) newGraph.get(i).add(new int[]{i,E[i][i]*2}); // to be determined
					else{
						if(E[i][j]> 0 && E[i][j]==E[j][i]){
							newGraph.get(i).add(new int[]{j,E[i][j]});
							newGraph.get(j).add(new int[]{i,E[i][j]});
						}
					}
				}
			}
			flag = true;
		}

		
		for(int i=0; i<communities.length;i++){
			find(communities,i);
		}
		HashMap<Integer,Integer> change_communitry_number = new HashMap<Integer,Integer>();
		int num_community=0;
		for(int i=0; i<communities.length;i++){
			if(change_communitry_number.get(communities[i]) == null) change_communitry_number.put(communities[i], num_community++);
			communities[i] = change_communitry_number.get(communities[i]);
		}
		
		return communities;
	}
	
	public int find(int[] communities, int u){
		if(communities[u] == u) return u;
		else{
			int v = find(communities, communities[u]);
			communities[u] = v;
			return v;
		}
	}
	
	public void union(int[] communities, int u, int v){
		int fu = find(communities, u);
		int fv = find(communities, v);
		if(fu < fv){
			communities[fv] = fu;
		}
		else if(fu > fv){
			communities[fu] = fv;
		}
	}

}
