package visitors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PrefixExpression;

public class PrefixVisitor extends CustomVisitor {

	private String symbolToFind;
	private String symbolToReplace;
	
	public PrefixVisitor(CompilationUnit cu, String type, String fsymbol, String rsymbol, String label, String category) {
		super(cu);
		setVisitorType(type);
		symbolToFind = fsymbol;
		symbolToReplace = rsymbol;
		setVisitorLabel(label);
		setVisitorCategory(category);
	}

	public PrefixVisitor(CompilationUnit cu, String type, String fsymbol, String rsymbol, String category) {
		super(cu);
		setVisitorType(type);
		symbolToFind = fsymbol;
		symbolToReplace = rsymbol;
		setVisitorLabel(fsymbol + " to " + rsymbol);
		setVisitorCategory(category);
	}
	
	public void endVisit(PrefixExpression node){
		if (node.getOperator().toString().equals(symbolToFind)){
			String newLine = symbolToReplace + " " + node.getOperand();
			String regex = stringToRegex(node.getOperator() + node.getOperand().toString());
			matches.add(new Triple(node.toString(),newLine,cu.getLineNumber(node.getStartPosition())-1,regex));
		}
	}
	
}