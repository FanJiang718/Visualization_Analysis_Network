package jdg.layout;

import java.util.ArrayList;

import Jcg.geometry.Point_3;
import Jcg.geometry.Vector_3;
import jdg.graph.AdjacencyListGraph;
import jdg.graph.Node;

/**
 * A class implementing the Fruchterman and Reingold method with fast approximatino of repulsive forces (using octrees)
 * 
 * @author Luca Castelli Aleardi, Ecole Polytechnique
 * @version fev 2017
 */
public class FastFR91Layout extends Layout {
	// parameters of the algorithm by Fruchterman and Reingold
	public double k; // natural spring length
	public double area; // area of the drawing (width times height)
	public double C; // step
	public double temperature; // initial temperature
	public double minTemperature; // minimal temperature (strictly positive)
	public double coolingConstant; // constant term: the temperature decreases linearly at each iteration
	public boolean useCooling; // say whether performing simulated annealing
	public int iterationCount=0; // count the number of performed iterations
	private int countRepulsive=0; // count the number of computed repulsive forces (to measure time performances)
	
	public Point_3[] boundingbox;
	public double theta = 1.2;
	/**
	 * Initialize the parameters of the force-directed layout
	 * 
	 *  @param g  input graph to draw
	 *  @param w  width of the drawing area
	 *  @param h  height of the drawing area
	 *  @param C  step length
	 */
	public FastFR91Layout(AdjacencyListGraph g, double w, double h) {
		System.out.print("Initializing force-directed method: fast Fruchterman-Reingold 91...");
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
		this.temperature=w/5.; // the temperature is a fraction of the width of the drawing area
		this.minTemperature=0.05;
		this.coolingConstant=0.98;
				
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

    ////////
    //computing the 3D bouding box
    ////////
    public static Point_3[] compute3DBoundingBox(ArrayList<Node> vertices) {
    	double 	xmin=Double.MAX_VALUE, xmax=Double.MIN_VALUE, 
    			ymin=Double.MAX_VALUE, ymax=Double.MIN_VALUE,
    			zmin=Double.MAX_VALUE, zmax=Double.MIN_VALUE;
    	
    	double x, y,z;
    	for(Node u: vertices) {
    		x=u.getPoint().getX().doubleValue();
    		y=u.getPoint().getY().doubleValue();
    		z=u.getPoint().getY().doubleValue();
    		if (x<xmin)
    			xmin = x;
    		if (x>xmax)
    			xmax = x;
    		if (y<ymin)
    			ymin = y;
    		if (y>ymax)
    			ymax = y;
    		if (z<zmin)
    			zmin = z;
    		if (z>zmax)
    			zmax = z;
    	}
    	Point_3 p=new Point_3(xmin, ymin, zmin);
    	Point_3 q=new Point_3(xmax, ymax, zmax);
    	//System.out.println("\nBounding box: "+p+" - "+q);
    	return new Point_3[]{p, q};
    }
	
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
	
	private Vector_3[] computeAllAttractiveForces() {
		Vector_3[] displacements = new Vector_3[g.sizeVertices()];
		int count = 0;
		for(Node u: g.vertices){
			displacements[count++] = computeAttractiveForce(u);
		}
		return displacements;
	}
	
	private Vector_3 computeRepulsiveForce(Node u, Octree octree) {
		Vector_3 displacement = new Vector_3(0.,0.,0.);
		double norm;
		Vector_3 delta = (Vector_3) u.p.minus(octree.barycenter);
		norm = Math.sqrt(delta.squaredLength().doubleValue())+0.0000001;
		if(octree.width/norm <= this.theta || octree.current_level>= (octree.max_tree_level-1)){
			displacement = displacement.sum(delta.multiplyByScalar(-C*octree.num_vertice /norm*repulsiveForce(norm)));
		}
		else{
			for(Octree children: octree.children ){
				if(children == null) continue;
				displacement = displacement.sum(computeRepulsiveForce(u, children));
			}
		}
		return displacement;
	}
	
	private Vector_3[] computeAllRepulsiveForces(Octree octree) {
		Vector_3[] displacements = new Vector_3[g.sizeVertices()];
		int count = 0;
		for(Node u: g.vertices){
			displacements[count++] = computeRepulsiveForce(u,octree);
		}
		return displacements;
	}
    
	/**
	 * Perform one iteration of the Force-Directed algorithm.
	 * Positions of vertices are updated according to their mutual attractive and repulsive forces.
	 */	
	public void computeLayout() {
		System.out.print("Performing iteration (fast FR91): "+this.iterationCount);
		long startTime=System.nanoTime(), endTime; // for evaluating time performances
		
		// first step: for each vertex compute the displacements due to attractive and repulsive forces
		Octree octree = new Octree(this.g.vertices,0,FastFR91Layout.compute3DBoundingBox(g.vertices));
		Vector_3[] displacement_replusive = computeAllRepulsiveForces(octree);
		Vector_3[] displacement_attractive = computeAllAttractiveForces();
		// second step: compute the total displacements and move all nodes to their new locations
		int count = 0;
        for(Node u: g.vertices){
        	Vector_3 displacement_tot = displacement_replusive[count].sum(displacement_attractive[count]);
        	double norm = Math.sqrt(displacement_tot.squaredLength().doubleValue());
        	u.p.translateOf(displacement_tot.multiplyByScalar(Math.min(temperature, norm)/norm));
        	count++;
        }
		
		// evaluate time performances
    	endTime=System.nanoTime();
        double duration=(double)(endTime-startTime)/1000000000.;
        System.out.println("iteration "+this.iterationCount+" done ("+duration+" seconds)");

		this.cooling(); // update temperature
		
		this.iterationCount++; // increase counter (to count the number of performed iterations)
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
		String result="fast implementation of the force-directed algorihm: Fruchterman Reingold\n";
		result=result+"\t area= "+w+" x "+h+"\n";
		result=result+"\t k= "+this.k+"\n";
		result=result+"\t C= "+this.C+"\n";
		result=result+"\t initial temperature= "+this.temperature+"\n";
		result=result+"\t minimal temperature= "+this.minTemperature+"\n";
		result=result+"\t cooling constant= "+this.coolingConstant+"\n";
		
		return result;
	}


}
