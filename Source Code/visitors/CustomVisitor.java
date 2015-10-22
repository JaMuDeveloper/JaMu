package visitors;

import java.util.ArrayList;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

public abstract class CustomVisitor extends ASTVisitor {

	protected ArrayList<Triple> matches;
	protected CompilationUnit cu;
	//Used on disk to keep track of mutations made
	protected String visitorType;
	//Used in the UI to indicate mutators
	protected String visitorLabel;
	
	protected CustomVisitor(CompilationUnit cu){
		this(cu,"NotSet");
	}
	
	protected CustomVisitor(CompilationUnit cu, String visitorLabel){
		matches = new ArrayList<Triple>();
		this.cu = cu;
		setVisitorType("NotSet");
		setVisitorLabel(visitorLabel);
	}
	
	protected String generateNewCode(String source, Triple occurence, int counter){
		String[] lines = source.split("\\n");
		lines[occurence.lineNumber] = lines[occurence.lineNumber].replaceAll(occurence.regex, occurence.newCode);
		lines = insertLine(lines, "//JaMu mutated the line below from " + occurence.oldCode + " to " + occurence.newCode + "\n", occurence.lineNumber);
		String newSource = "";
		for (String line : lines){
			newSource += line;
		}
		return newSource;
	}
	
	public String[] generateNewSources(String source){
		String[] newSources = new String[matches.size()];
		int counter = 0;
		for (Triple occurence : matches){
			String newCode = generateNewCode(source, occurence,counter+1);
			newSources[counter] = newCode;
			counter++;
		}
		return newSources;
	}
	
	public static String[] insertLine(String[] lines, String lineToInsert, int position){
		String[] newLines = new String[lines.length+1];
		for (int i =0; i < lines.length+1; i++){
			if (i < position){
				newLines[i] = lines[i];
			}
			else if (i == position){
				newLines[i] = lineToInsert;
			}
			else{
				newLines[i] = lines[i-1];
			}
		}
		return newLines;
	}
	
	protected String addSpacesRegex(String source){
		String returnString = "";
		for (int i = 0; i < source.length(); i++){
			if (source.charAt(i) == '\\'){
				returnString += source.charAt(i);
			}
			else{
				returnString += source.charAt(i) + " ";
			}
	}
		return returnString;
	}
	
	protected String stringToRegex(String base){
		Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");
		String pattern = SPECIAL_REGEX_CHARS.matcher(base).replaceAll("\\\\$0");
		pattern = addSpacesRegex(StringUtils.deleteWhitespace(pattern));
		pattern = pattern.replaceAll("\\s+", "\\\\s*");
		return pattern;
	}

	public String getVisitorLabel(){
		return visitorLabel;
	}
	public void setVisitorLabel(String visitorLabel){
		this.visitorLabel = visitorLabel;
	}
	
	public String getVisitorType() {
		return visitorType;
	}
	public void setVisitorType(String visitorType) {
		this.visitorType = visitorType;
	}
	
	public int getNumberOfMatches(){
		return matches.size();
	}
	
	public ArrayList<Triple> getMatches(){
		return matches;
	}
	
}