package main;

import java.io.Serializable;

public class Sunflower implements Serializable{

	private int year;
	private double production;
	private double cost;
	
	public Sunflower() {
		
	}

	public Sunflower(int year,double production,double cost) {
		this.year=year;
		this.production=production;
		this.cost=cost;
	}
	
	public int getYear() {
		return year;
	}
	
	public double getProduction() {
		return production;
	}
	
	public double getCost() {
		return cost;
	}
}
