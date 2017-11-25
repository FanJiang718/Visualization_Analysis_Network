package jdg.layout;

import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;

import java.util.ArrayList;

import Jcg.geometry.Point_3;
import Jcg.geometry.Vector_3;

/**
 * A class implementing the force directed algorithm by Fruchterman and Reingold (1991)
 * 
 * @author Luca Castelli Aleardi, Ecole Polytechnique
 * @version fev 2017
 */
public class FR91Layout extends Layout {
	// parameters of the algorithm by Fruchterman and Reingold
	public double k; // natural spring length
	public double area; // area of the drawing (width times height)
	public double C; // step
	public double temperature; // initial temperature
	public double minTemperature; // minimal temperature (strictly positive)
	public double coolingConstant; // constant term: the temperature decreases linearly at each iteration
	
	public int iterationCount=0; // count the number of performed iterations
	private int countRepulsive=0; // count the number of computed repulsive forces (to measure time performances)
	
	/**
	 * Initialize the parameters of the force-directed layout
	 * 
	 *  @param g  input graph to draw
	 *  @param w  width of the drawing area
	 *  @param h  height of the drawing area
	 *  @param C  step length
	 */
	public FR91Layout(AdjacencyListGraph g, double w, double h) {
		System.out.print("Initializing force-directed method: Fruchterman-Reingold 91...");
		if(g==null) {
			System.out.println("Input graph not defined");
			System.exit(0);
		}
		this.g=g;
		int N=g.sizeVertices();
		
		// set the parameters of the algorithm FR91
		this.C=1.;
		this.w=w;
		this.h=h;
		this.area=w*h;
		this.k=C*Math.sqrt(area/N);
		this.temperature=w/2.; // the temperature is a fraction of the width of the drawing area
		this.minTemperature=0.05;
		this.coolingConstant=0.99;
		
		System.out.println("done ("+N+" nodes)");
		//System.out.println("k="+k+" - temperature="+temperature);
		System.out.println(this.toString());
	}
	
	/**
	 * Compute the (intensity of the) attractive force between two nodes at a given distance
	 * 
	 * @param distance  distance between two nodes
	 */	
	public double attractiveForce(double distance) {
		return (distance*distance)/k;
	}
	
	/**
	 * Compute the (intensity of the) repulsive force between two nodes at a given distance
	 * 
	 * @param distance  distance between two nodes
	 */	
	public double repulsiveForce(double distance) {
		countRepulsive++;
		return (k*k)/distance;
	}

	/**
	 * Perform one iteration of the Force-Directed algorithm.
	 * Positions of vertices are updated according to their mutual attractive and repulsive forces.
	 */	
	public void computeLayout() {
		System.out.print("Performing iteration (FR91): "+this.iterationCount);
		long startTime=System.nanoTime(), endTime; // for evaluating time performances

		//System.err.println("\nWarning: the class FR91Layout must be completed (question 1) ");
		//System.exit(0);

		// first step: for each vertex compute the displacements due to attractive and repulsive forces
		Vector_3[] displacement_replusive = computeAllRepulsiveForces();
		Vector_3[] displacement_attractive = computeAllAttractiveForces();
		// second step: compute the total displacements and move all nodes to their new locations
		int count = 0;
        for(Node u: g.vertices){
        	Vector_3 displacement_tot = displacement_replusive[count].sum(displacement_attractive[count]);
        	double norm = Math.sqrt(displacement_tot.squaredLength().doubleValue());
        	u.p.translateOf(displacement_tot.multiplyByScalar(Math.min(temperature, norm)/norm));
        	count++;
        }
		
		
        this.cooling(); // update temperature
		
		// evaluate time performances
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
        System.out.println("iteration "+this.iterationCount+" done ("+duration+" seconds)");
		
		this.iterationCount++; // increase counter (to count the number of performed iterations)
	}
	
	/**
	 * Compute the displacement of vertex 'u', due to repulsive forces (of all nodes)
	 * 
	 * @param u  the vertex to which repulsive forces are applied
	 * @return 'displacement' a 3d vector storing the displacement of vertex 'u'
	 */	
	private Vector_3 computeRepulsiveForce(Node u) {
		Vector_3 displacement = new Vector_3(0.,0.,0.);
		Point_3[] Positions = g.getPositions();
		double norm;
		for(Point_3 v: Positions ){
			Vector_3 delta = (Vector_3) u.p.minus(v);
			norm = Math.sqrt(delta.squaredLength().doubleValue());
			if(norm == 0.) continue;
			displacement = displacement.sum(delta.multiplyByScalar(-C /norm*repulsiveForce(norm)));
		}
		return displacement;

	}
	
	/**
	 * Compute, for each vertex, the displacement due to repulsive forces (between all nodes)
	 * 
	 * @return a vector v[]: v[i] stores the geometric displacement of the i-th node
	 */	
	private Vector_3[] computeAllRepulsiveForces() {
		Vector_3[] displacements = new Vector_3[g.sizeVertices()];
		int count = 0;
		for(Node u: g.vertices){
			displacements[count++] = computeRepulsiveForce(u);
		}
		return displacements;
	}
	
	/**
	 * Compute the displacement of vertex 'u', due to the attractive forces of its neighbors
	 * 
	 * @param u  the vertex to which attractive forces are applied
	 * @return 'disp' a 3d vector storing the displacement of vertex 'u'
	 */	
	private Vector_3 computeAttractiveForce(Node u) {
		Vector_3 displacement = new Vector_3(0.,0.,0.);
		ArrayList<Node> neighbors = u.neighbors;
		double norm;
		for(Node v: neighbors){
			Vector_3 delta = (Vector_3) u.p.minus(v.p);
			norm = Math.sqrt(delta.squaredLength().doubleValue());
			displacement = displacement.sum(delta.multiplyByScalar(attractiveForce(norm)/norm));
		}
		return displacement;
	}
	
	/**
	 * Compute, for each vertex, the displacement due to attractive forces (between neighboring nodes)
	 * 
	 * @return a vector v[]: v[i] stores the geometric displacement of the i-th node
	 */	
	private Vector_3[] computeAllAttractiveForces() {
		Vector_3[] displacements = new Vector_3[g.sizeVertices()];
		int count = 0;
		for(Node u: g.vertices){
			displacements[count++] = computeAttractiveForce(u);
		}
		return displacements;
	}
	
	/**
	 * Cooling system: the temperature decreases linearly at each iteration
	 * 
	 * Remark: the temperature is assumed to remain strictly positive (>=minTemperature)
	 */	
	protected void cooling() {
		this.temperature=Math.max(this.temperature*coolingConstant, minTemperature);
		//this.temperature=Math.max(this.temperature-coolingConstant, minTemperature); // variant
	}
	
	public String toString() {
		String result="force-directed algorihm: Fruchterman Reingold\n";
		result=result+"\t area= "+w+" x "+h+"\n";
		result=result+"\t k= "+this.k+"\n";
		result=result+"\t C= "+this.C+"\n";
		result=result+"\t initial temperature= "+this.temperature+"\n";
		result=result+"\t minimal temperature= "+this.minTemperature+"\n";
		result=result+"\t cooling constant= "+this.coolingConstant+"\n";
		
		return result;
	}
	
}
