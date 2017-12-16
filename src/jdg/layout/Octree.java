package jdg.layout;

import java.util.*;
import Jcg.geometry.Point_3;
import Jcg.geometry.Vector_3;
import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;

public class Octree {
	public int max_tree_level =8;
	public int current_level = 1;
	public final double theta = 1.2;
	public ArrayList<Node> vertices;
	public Octree[] children = new Octree[8];
	public int num_vertice;
	public Point_3 barycenter = new Point_3();
	public Point_3 cubecenter;
	
	public Octree(){
		this.vertices = null;
	}

	public Octree(Node u, ArrayList<Node> vertices, int lelvel, Point_3 cubecenter){
		this.vertices = vertices;
		this.num_vertice = this.vertices.size();
		this.barycenter.barycenter();
	}
	
	
	public Octree(Node u, AdjacencyListGraph g){
		this.vertices = g.vertices;
		ArrayList<Node> vertice_child1 = new ArrayList<Node>();
		ArrayList<Node> vertice_child2 = new ArrayList<Node>();
		ArrayList<Node> vertice_child3 = new ArrayList<Node>();
		ArrayList<Node> vertice_child4 = new ArrayList<Node>();
		ArrayList<Node> vertice_child5 = new ArrayList<Node>();
		ArrayList<Node> vertice_child6 = new ArrayList<Node>();
		ArrayList<Node> vertice_child7 = new ArrayList<Node>();
		ArrayList<Node> vertice_child8 = new ArrayList<Node>();
		
		Point_3[] boudingbox = g.compute3DBoundingBox();
		Vector_3 width3 = (Vector_3)boudingbox[0].minus(boudingbox[1]);
		double max_width = Math.max(width3.x,width3.y);
		max_width = Math.max(max_width, width3.z);
		this.cubecenter = new Point_3(boudingbox[0].x+max_width/2,boudingbox[0].y+max_width/2,boudingbox[0].z+max_width/2 );
		Point_3[] positions = g.getPositions();
		this.barycenter.barycenter(positions);
		Vector_3 delta = (Vector_3) u.p.minus(this.barycenter);
		double norm = Math.sqrt(delta.squaredLength().doubleValue());
		
		if(this.current_level< this.max_tree_level && max_width/norm > theta ){
			for(Node v: g.vertices){
				if(this.cubecenter.compareCartesian(v.p, 1)< 0){
					if(this.cubecenter.compareCartesian(v.p, 2)< 0){
						if(this.cubecenter.compareCartesian(v.p, 3)< 0){
							vertice_child1.add(v);
						}
						else{
							vertice_child2.add(v);
						}
					}
					else{
						if(this.cubecenter.compareCartesian(v.p, 3)< 0){
							vertice_child3.add(v);
						}
						else{
							vertice_child4.add(v);
						}
					}
				}
				else{
					if(this.cubecenter.compareCartesian(v.p, 2)< 0){
						if(this.cubecenter.compareCartesian(v.p, 3)< 0){
							vertice_child5.add(v);
						}
						else{
							vertice_child6.add(v);
						}
					}
					else{
						if(this.cubecenter.compareCartesian(v.p, 3)< 0){
							vertice_child7.add(v);
						}
						else{
							vertice_child8.add(v);
						}
					}
				}
			}
			this.children[0] = new Octree(u,vertice_child1,this.current_level+1);
			this.children[1] = new Octree(u,vertice_child2,this.current_level+1);
			this.children[2] = new Octree(u,vertice_child3,this.current_level+1);
			this.children[3] = new Octree(u,vertice_child4,this.current_level+1);
			this.children[4] = new Octree(u,vertice_child5,this.current_level+1);
			this.children[5] = new Octree(u,vertice_child6,this.current_level+1);
			this.children[6] = new Octree(u,vertice_child7,this.current_level+1);
			this.children[7] = new Octree(u,vertice_child8,this.current_level+1);

			
		}
		else{
			
		}
	}
	
}
