package jdg.layout;

import java.util.*;
import Jcg.geometry.Point_3;
import jdg.graph.Node;

public class Octree {
	public int max_tree_level =8;
	public int current_level;
	public ArrayList<Node> vertices;
	public Octree[] children = new Octree[8];
	public int num_vertice;
	public Point_3 barycenter = new Point_3();
	public Point_3 cubecenter = new Point_3();
	public double width;
	
	public Octree(ArrayList<Node> vertices, int level, Point_3[] boundingbox){
		this.current_level = level;
		this.vertices = vertices;
		this.num_vertice = vertices.size();
		ArrayList<Node> vertice_child1;
		ArrayList<Node> vertice_child2;
		ArrayList<Node> vertice_child3;
		ArrayList<Node> vertice_child4;
		ArrayList<Node> vertice_child5;
		ArrayList<Node> vertice_child6;
		ArrayList<Node> vertice_child7;
		ArrayList<Node> vertice_child8;
		this.width = Math.max((boundingbox[1].getX().doubleValue()-boundingbox[0].getX().doubleValue())/2., (boundingbox[1].getY().doubleValue()-boundingbox[0].getY().doubleValue())/2.);
		this.width = Math.max(this.width, (boundingbox[1].getZ().doubleValue()-boundingbox[0].getZ().doubleValue())/2.);
		this.cubecenter = new Point_3((boundingbox[1].getX().doubleValue()+boundingbox[0].getX().doubleValue())/2.,(boundingbox[1].getY().doubleValue()+boundingbox[0].getY().doubleValue())/2.,
				(boundingbox[1].getZ().doubleValue()+boundingbox[0].getZ().doubleValue())/2. );
		Point_3[] positions = new Point_3[vertices.size()];
		int i=0;
		double x = this.cubecenter.getX().doubleValue();
		double y = this.cubecenter.getY().doubleValue();
		double z = this.cubecenter.getZ().doubleValue();
		//System.out.println("level:"+ this.current_level);
		if(this.current_level< this.max_tree_level){
			vertice_child1 = new ArrayList<Node>();
			vertice_child2 = new ArrayList<Node>();
			vertice_child3 = new ArrayList<Node>();
			vertice_child4 = new ArrayList<Node>();
			vertice_child5 = new ArrayList<Node>();
			vertice_child6 = new ArrayList<Node>();
			vertice_child7 = new ArrayList<Node>();
			vertice_child8 = new ArrayList<Node>();
			Point_3[] boundingbox1 = new Point_3[2];
			boundingbox1[0] = new Point_3(x,y,z);
			boundingbox1[1] = new Point_3(x+this.width,y+this.width,z+this.width);
			Point_3[] boundingbox2 = new Point_3[2];
			boundingbox2[0] = new Point_3(x,y,z-width);
			boundingbox2[1] = new Point_3(x+width,y+width,z);
			Point_3[] boundingbox3 = new Point_3[2];
			boundingbox3[0] = new Point_3(x,y-width,z);
			boundingbox3[1] = new Point_3(x+width,y,z+width);
			Point_3[] boundingbox4 = new Point_3[2];
			boundingbox4[0] = new Point_3(x,y-width,z-width);
			boundingbox4[1] = new Point_3(x+width,y,z);
			Point_3[] boundingbox5 = new Point_3[2];
			boundingbox5[0] = new Point_3(x-width,y,z);
			boundingbox5[1] = new Point_3(x,y+width,z+width);
			Point_3[] boundingbox6 = new Point_3[2];
			boundingbox6[0] = new Point_3(x-width,y,z-width);
			boundingbox6[1] = new Point_3(x,y,z);
			Point_3[] boundingbox7 = new Point_3[2];
			boundingbox7[0] = new Point_3(x-width,y-width,z);
			boundingbox7[1] = new Point_3(x,y,z+width);
			Point_3[] boundingbox8 = new Point_3[2];
			boundingbox8[0] = new Point_3(x-width,y-width,z-width);
			boundingbox8[1] = new Point_3(x,y,z);
			for(Node v: vertices){
				positions[i++] = v.getPoint();
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
			if(vertice_child1.size()!=0) this.children[0] = new Octree(vertice_child1,this.current_level+1,boundingbox1);
			if(vertice_child2.size()!=0) this.children[1] = new Octree(vertice_child2,this.current_level+1,boundingbox2);
			if(vertice_child3.size()!=0) this.children[2] = new Octree(vertice_child3,this.current_level+1,boundingbox3);
			if(vertice_child4.size()!=0) this.children[3] = new Octree(vertice_child4,this.current_level+1,boundingbox4);
			if(vertice_child5.size()!=0) this.children[4] = new Octree(vertice_child5,this.current_level+1,boundingbox5);
			if(vertice_child6.size()!=0) this.children[5] = new Octree(vertice_child6,this.current_level+1,boundingbox6);
			if(vertice_child7.size()!=0) this.children[6] = new Octree(vertice_child7,this.current_level+1,boundingbox7);
			if(vertice_child8.size()!=0) this.children[7] = new Octree(vertice_child8,this.current_level+1,boundingbox8);
			
		}
		else{
			for(Node v: vertices){
				positions[i++] = v.getPoint();
			}
		}
		this.barycenter.barycenter(positions);
		
	}
	
}
