package visitors;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;

public class IfVisitor extends CustomVisitor {

	private String newSymbol;
	
	public IfVisitor(CompilationUnit cu, String type, String newSymbol, String label, String category) {
		super(cu);
		setVisitorType(type);
		this.newSymbol = newSymbol;
		setVisitorLabel(label);
		setVisitorCategory(category);
	}
	
	public IfVisitor(CompilationUnit cu, String type, String newSymbol, String category) {
		super(cu);
		setVisitorType(type);
		this.newSymbol = newSymbol;
		setVisitorLabel("if(x) to if(" + newSymbol + ")");
		setVisitorCategory(category);
	}	

	public void endVisit(IfStatement node){
		String regex = stringToRegex(node.getExpression().toString());
		matches.add(new Triple(node.getExpression().toString(),newSymbol,cu.getLineNumber(node.getStartPosition())-1,regex));
	}
	
	
}
