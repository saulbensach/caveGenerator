package com.bensach.saul;

import java.util.Random;

/**
 * Created by saul- on 09/02/2016.
 */
public class Edge implements Comparable<Edge> {

    private Point p1, p2;
    int src, dest, weight;

    public Edge(Point p1, Point p2) {
        Random random = new Random();
        this.p1 = p1;
        this.p2 = p2;
        weight = (int) Math.sqrt(Math.pow((p1.getX()-p2.getX()), 2) + Math.pow((p1.getY()-p2.getY()), 2));
        //weight = random.nextInt(50)+10;
    }
    public Edge() {

    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    @Override
    public int compareTo(Edge compareEdge)
    {
        return this.weight-compareEdge.weight;
    }

    public float getWeight() {
        return weight;
    }

    @Override
    public int hashCode() {
        return (p1.hashCode() + p2.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Edge){
            Edge e = (Edge) obj;
            if(this.p1 == e.p1 && this.p2 == e.p2){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Edge{" +
                "p1=" + p1 +
                ", p2=" + p2 +
                ", weight=" + weight +
                '}';
    }
}
