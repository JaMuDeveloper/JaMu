package mutationEngine;

public class Mutant {

	private String oldSource;
	private String newSource;
	private String location;
	private String name;
	
	public Mutant(String oldSource, String newSource, String location, String name){
		this.oldSource = oldSource;
		this.newSource = newSource;
		this.location = location;
		this.name = name;
	}

	public String getOldSource(){
		return oldSource;
	}
	
	public String getNewSource(){
		return newSource;
	}
	
	public String getLocation(){
		return location;
	}
	
	public String getName(){
		return name;
	}
	
}