package jdg.clustering;

import java.util.HashMap;

import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;

/**
 * This class provides an implementation of the Greedy algorithm for Community detection
 * 
 * @author Luca Castelli Aleardi (INF421, 2017)
 */
public class GreedyAlgorithm extends CommunityDetection {

	/**
	 * Initialize the parameters of the Louvain algorithm
	 */
	public GreedyAlgorithm() {
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
		double[] M = new double[n]; // modularities
		int[] result=new int[n];
		int[] communities=new int[n];
		 // recording community configuration at each step
		HashMap<Integer,int[]> configurations = new HashMap<Integer,int[]>(); // recording the merge of clusters at each step
		double[][] E = new double[n][n]; // corresponding to e_ij in the paper
		double[] a = new double[n];
		for(Node node: graph.vertices){
			int k = node.index;
			for(Node neighbour: node.neighbors){
				E[k][neighbour.index] =1.0/m;
				E[neighbour.index][k] =1.0/m;
			}
		}
		M[0] = 0.;
		for(int i=0; i<n;i++){
			for(int j=0;j<n;j++){
				a[i] += E[i][j]; 
			}
			M[0]+= E[i][i] - a[i]*a[i];
			communities[i] = i;
			result[i] = i;
		}
		int count =1;
		while(count <n){
			double deltaQ = Double.NEGATIVE_INFINITY;
			int k =-1 ,l = -1;
		
			for(Node node: graph.vertices){
				int index = node.index;
				for(Node neighbour: node.neighbors){
					int index_nbr = neighbour.index;
					if(find(communities,index) == find(communities,index_nbr)) continue;
					double tmp = 2*(E[find(communities,index)][find(communities,index_nbr)] - a[find(communities,index)]*a[find(communities,index_nbr)]);
					if(tmp > deltaQ){
						deltaQ = tmp;
						k = Math.min(find(communities,index),find(communities,index_nbr));
						l = Math.max(find(communities,index),find(communities,index_nbr));
					}
				}
			}
			if(k==-1){
				count++;
				continue;
			}
			M[count] = M[count-1] + deltaQ;
			int[] tmp = new int[2];
			tmp[0] = k; tmp[1] = l;
			configurations.put(count, tmp);
			E[k][k] = E[k][k]+E[k][l]+E[l][l];
			E[k][l]=0;E[l][k] =0;E[l][l] =0;
			for(int i = 0; i<n;i++){
				E[k][i] = E[k][i] + E[i][l];
				E[i][k] = E[k][i];
				E[i][l] =0;
				E[l][i] =0;
			}
			a[k] += a[l];
			a[l] = 0;
			union(communities,k,l);
			count++;
		}
		
		double max_M = Double.NEGATIVE_INFINITY; int loc_maxM = -1;
		for(int i=0;i < M.length;i++){
			if(M[i] > max_M){
				max_M = M[i];
				loc_maxM = i;
			}
		}
		if(loc_maxM==0) return result;
		for(int i =1; i<= loc_maxM;i++){
			int[] tmp = configurations.get(i);
			union(result, tmp[0],tmp[1]);
		}
		for(int i=0; i<result.length;i++){
			find(result,i);
		}
		HashMap<Integer,Integer> change_communitry_number = new HashMap<Integer,Integer>();
		int num_community=0;
		for(int i=0; i<result.length;i++){
			if(change_communitry_number.get(result[i]) == null) change_communitry_number.put(result[i], num_community++);
			result[i] = change_communitry_number.get(result[i]);
		}
		
		return result;
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
