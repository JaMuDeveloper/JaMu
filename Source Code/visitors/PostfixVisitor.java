package visitors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PostfixExpression;

public class PostfixVisitor extends CustomVisitor{
	
	private String symbolToFind;
	private String symbolToReplace;
	
	public PostfixVisitor(CompilationUnit cu, String type, String fsymbol, String rsymbol, String label) {
		super(cu);
		setVisitorType(type);
		symbolToFind = fsymbol;
		symbolToReplace = rsymbol;
		setVisitorLabel(label);
	}

	public PostfixVisitor(CompilationUnit cu, String type, String fsymbol, String rsymbol) {
		super(cu);
		setVisitorType(type);
		symbolToFind = fsymbol;
		symbolToReplace = rsymbol;
		setVisitorLabel(fsymbol + " to " + rsymbol);
	}
	
	public void endVisit(PostfixExpression node){
		if (node.getOperator().toString().equals(symbolToFind)){
			String newLine = symbolToReplace + " " + node.getOperand();
			String regex = stringToRegex(node.getOperator() + node.getOperand().toString());
			matches.add(new Triple(node.toString(),newLine,cu.getLineNumber(node.getStartPosition())-1,regex));
		}
	}
	
}