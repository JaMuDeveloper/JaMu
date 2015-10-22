package visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

public class VisitorList {

	private InfixVisitor additionVisitor;
	private InfixVisitor subtractionVisitor;
	private InfixVisitor divisionVisitor;
	private InfixVisitor multiplicationVisitor;
	private InfixVisitor remainderVisitor;
	private InfixVisitor greaterThanReversedVisitor;
	private InfixVisitor greaterThanBoundaryVisitor;
	private InfixVisitor greaterOrEqualReversedVisitor;
	private InfixVisitor greaterOrEqualBoundaryVisitor;
	private InfixVisitor lessThanReversedVisitor;
	private InfixVisitor lessThanBoundaryVisitor;
	private InfixVisitor lessOrEqualReversed;
	private InfixVisitor lessOrEqualBoundary;
	private InfixVisitor equalsVisitor;
	private InfixVisitor notEqualsVisitor;
	private InfixVisitor andVisitor;
	private InfixVisitor orVisitor;
	private PostfixVisitor plusPlusVisitor;
	private PostfixVisitor minusMinusVisitor;
	private PrefixVisitor notVisitor;
	private IfVisitor ifToTrueVisitor;
	private IfVisitor ifToFalseVisitor;
	private CustomVisitor[] visitors;
	
	public VisitorList(CompilationUnit cu){
		additionVisitor = new InfixVisitor(cu, "Addition","+","-");
		subtractionVisitor = new InfixVisitor(cu, "Subtraction","-","+");
		divisionVisitor = new InfixVisitor(cu, "Division","/","*");
		multiplicationVisitor = new InfixVisitor(cu, "Multiplication","*","/");
		remainderVisitor = new InfixVisitor(cu, "Remainder", "%","*", "% to *");
		greaterThanReversedVisitor = new InfixVisitor(cu, "GreaterThanReversed", ">", "<=");
		greaterThanBoundaryVisitor = new InfixVisitor(cu, "GreaterThanBoundary", ">", ">=");
		greaterOrEqualReversedVisitor = new InfixVisitor(cu, "GreaterOrEqualReversed", ">=", "<");
		greaterOrEqualBoundaryVisitor = new InfixVisitor(cu, "GreaterOrEqualBoundary", ">=", ">");
		lessThanReversedVisitor = new InfixVisitor(cu, "LessThanReversed", "<", ">=");
		lessThanBoundaryVisitor = new InfixVisitor(cu, "LessThanBoundary", "<", "<=");
		lessOrEqualReversed = new InfixVisitor(cu, "LessOrEqualReversed", "<=", ">");
		lessOrEqualBoundary = new InfixVisitor(cu, "LessOrEqualBoundary", "<=", "<");
		equalsVisitor = new InfixVisitor(cu, "Equal", "==", "!=");
		notEqualsVisitor = new InfixVisitor(cu, "NotEqual", "!=", "==");
		andVisitor = new InfixVisitor(cu, "And", "&&", "||");
		orVisitor = new InfixVisitor(cu, "Or", "||", "&&");
		plusPlusVisitor = new PostfixVisitor(cu, "PlusPlus", "++", "--");
		minusMinusVisitor = new PostfixVisitor(cu, "MinusMinus","--","++");
		notVisitor = new PrefixVisitor(cu, "Not", "!","","!x to x");
		ifToTrueVisitor = new IfVisitor(cu,"IfToTrue","true");
		ifToFalseVisitor = new IfVisitor(cu,"IfToFalse","false");
		
		CustomVisitor[] temp = {additionVisitor,subtractionVisitor,divisionVisitor,multiplicationVisitor,remainderVisitor,
				greaterThanReversedVisitor,greaterThanBoundaryVisitor,greaterOrEqualReversedVisitor,greaterOrEqualBoundaryVisitor,
				lessThanReversedVisitor,lessThanBoundaryVisitor,lessOrEqualReversed,lessOrEqualBoundary,equalsVisitor,
				notEqualsVisitor,andVisitor,orVisitor,plusPlusVisitor,minusMinusVisitor,notVisitor,ifToTrueVisitor,ifToFalseVisitor};
		visitors = temp;
	}

	public InfixVisitor getAdditionVisitor() {
		return additionVisitor;
	}

	public InfixVisitor getSubtractionVisitor() {
		return subtractionVisitor;
	}

	public InfixVisitor getDivisionVisitor() {
		return divisionVisitor;
	}

	public InfixVisitor getMultiplicationVisitor() {
		return multiplicationVisitor;
	}

	public InfixVisitor getRemainderVisitor() {
		return remainderVisitor;
	}

	public InfixVisitor getGreaterThanReversedVisitor() {
		return greaterThanReversedVisitor;
	}

	public InfixVisitor getGreaterThanBoundaryVisitor() {
		return greaterThanBoundaryVisitor;
	}

	public InfixVisitor getGreaterOrEqualReversedVisitor() {
		return greaterOrEqualReversedVisitor;
	}

	public InfixVisitor getGreaterOrEqualBoundaryVisitor() {
		return greaterOrEqualBoundaryVisitor;
	}

	public InfixVisitor getLessThanReversedVisitor() {
		return lessThanReversedVisitor;
	}

	public InfixVisitor getLessThanBoundaryVisitor() {
		return lessThanBoundaryVisitor;
	}

	public InfixVisitor getEqualsVisitor() {
		return equalsVisitor;
	}

	public CustomVisitor[] getVisitors() {
		return visitors;
	}

	public CustomVisitor get(int i) {
		return visitors[i];
	}
	
	public CustomVisitor get(String label){
		for (CustomVisitor v : visitors){
			if (v.getVisitorLabel().equals(label)){
				return v;
			}
		}
		return null;
	}
	
	public static List<String> getLabels(){
		VisitorList DO_NOT_USE = new VisitorList(null);
		List<String> labelList = new ArrayList<String>();
		for (CustomVisitor v : DO_NOT_USE.getVisitors()){
			labelList.add(v.getVisitorLabel());
		}
		return labelList;
	}
	
}