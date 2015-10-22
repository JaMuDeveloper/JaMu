package visitors;

public class Triple{
		
	public String oldCode;
	public String newCode;
	public int lineNumber;
	public String regex;
	
	public Triple(String o, String n, int i, String r){
		oldCode = o;
		newCode = n;
		lineNumber = i;
		regex = r;
	}
}