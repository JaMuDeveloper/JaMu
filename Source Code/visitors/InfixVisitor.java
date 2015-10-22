package visitors;

import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

public class InfixVisitor extends CustomVisitor{
	
	private String symbolToFind;
	private String symbolToReplace;
	
	public InfixVisitor(CompilationUnit cu, String type, String fsymbol, String rsymbol, String label, String category) {
		super(cu);
		setVisitorType(type);
		symbolToFind = fsymbol;
		symbolToReplace = rsymbol;
		setVisitorLabel(label);
		setVisitorCategory(category);
	}

	public InfixVisitor(CompilationUnit cu, String type, String fsymbol, String rsymbol, String category) {
		super(cu);
		setVisitorType(type);
		symbolToFind = fsymbol;
		symbolToReplace = rsymbol;
		setVisitorLabel(fsymbol + " to " + rsymbol);
		setVisitorCategory(category);
	}
	
	public void endVisit(InfixExpression node){
		if (node.getLeftOperand() instanceof MethodInvocation){
			MethodInvocation mi = (MethodInvocation) node.getLeftOperand();
			if (mi.getExpression() instanceof StringLiteral){
				return;
			}
		}
		else if (node.getRightOperand() instanceof MethodInvocation) {
			MethodInvocation mi = (MethodInvocation) node.getRightOperand();
			if (mi.getExpression() instanceof StringLiteral){
				return;
			}
		} else if (node.hasExtendedOperands()){
			List<?> operatorlist =  node.extendedOperands();
			for (Object e : operatorlist){
				if (e instanceof StringLiteral){
					return;
				}
			}
		} else if (!(node.getLeftOperand() instanceof StringLiteral) && !(node.getRightOperand() instanceof StringLiteral)
				&& node.getOperator().toString().equals(symbolToFind)){
			handleMatch(node);
		}
		
	}
	
	private void handleMatch(InfixExpression node){
		String newLine = node.getLeftOperand().toString() + " " + symbolToReplace + " " + node.getRightOperand().toString();
		String regex = stringToRegex(node.getLeftOperand().toString() + node.getOperator() + node.getRightOperand().toString());
		matches.add(new Triple(node.toString(),newLine,cu.getLineNumber(node.getStartPosition())-1,regex));
		if (node.hasExtendedOperands()){
			String base = node.getLeftOperand().toString() + node.getOperator().toString() + node.getRightOperand().toString();
			for (int i = 0; i < node.extendedOperands().size(); i++){
				newLine = base + " " + symbolToReplace + " " + node.extendedOperands().get(i).toString();
				base += node.getOperator().toString() + " " + node.extendedOperands().get(i).toString();
				regex = stringToRegex(base);
				matches.add(new Triple(base,newLine,cu.getLineNumber(node.getStartPosition())-1, regex));
			}
		}
	}
	
}