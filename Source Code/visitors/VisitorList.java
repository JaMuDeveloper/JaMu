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
		additionVisitor = new InfixVisitor(cu, "Addition","+","-","AORB");
		subtractionVisitor = new InfixVisitor(cu, "Subtraction","-","+","AORB");
		divisionVisitor = new InfixVisitor(cu, "Division","/","*","AORB");
		multiplicationVisitor = new InfixVisitor(cu, "Multiplication","*","/","AORB");
		remainderVisitor = new InfixVisitor(cu, "Remainder", "%","*", "% to *","AORB");
		plusPlusVisitor = new PostfixVisitor(cu, "PlusPlus", "++", "--","AORS");
		minusMinusVisitor = new PostfixVisitor(cu, "MinusMinus","--","++","AORS");
		greaterThanReversedVisitor = new InfixVisitor(cu, "GreaterThanReversed", ">", "<=","ROR");
		greaterThanBoundaryVisitor = new InfixVisitor(cu, "GreaterThanBoundary", ">", ">=","ROR");
		greaterOrEqualReversedVisitor = new InfixVisitor(cu, "GreaterOrEqualReversed", ">=", "<","ROR");
		greaterOrEqualBoundaryVisitor = new InfixVisitor(cu, "GreaterOrEqualBoundary", ">=", ">","ROR");
		lessThanReversedVisitor = new InfixVisitor(cu, "LessThanReversed", "<", ">=","ROR");
		lessThanBoundaryVisitor = new InfixVisitor(cu, "LessThanBoundary", "<", "<=","ROR");
		lessOrEqualReversed = new InfixVisitor(cu, "LessOrEqualReversed", "<=", ">","ROR");
		lessOrEqualBoundary = new InfixVisitor(cu, "LessOrEqualBoundary", "<=", "<","ROR");
		equalsVisitor = new InfixVisitor(cu, "Equal", "==", "!=","ROR");
		notEqualsVisitor = new InfixVisitor(cu, "NotEqual", "!=", "==","ROR");
		andVisitor = new InfixVisitor(cu, "And", "&&", "||","COR");
		orVisitor = new InfixVisitor(cu, "Or", "||", "&&","COR");
		notVisitor = new PrefixVisitor(cu, "Not", "!","","!x to x","COR");
		ifToTrueVisitor = new IfVisitor(cu,"IfToTrue","true","BOR");
		ifToFalseVisitor = new IfVisitor(cu,"IfToFalse","false","BOR");
		
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
		List<String> labels = new ArrayList<String>();
		for (CustomVisitor v : DO_NOT_USE.getVisitors()){
			labels.add(v.getVisitorLabel());
		}
		return labels;
	}
	
	public static List<String> getCategories(){
		VisitorList DO_NOT_USE = new VisitorList(null);
		List<String> categories = new ArrayList<String>();
		for (CustomVisitor v : DO_NOT_USE.getVisitors()){
			String cat = v.getVisitorCategory();
			if (!categories.contains(cat)){
				categories.add(cat);
			}
		}
		return categories;
	}
	
	public static List<String> transformToLabels(List<String> mutationRules) {
		VisitorList DO_NOT_USE = new VisitorList(null);
		List<String> labels = new ArrayList<String>();
		for (String mutationRule : mutationRules){
			for (CustomVisitor v : DO_NOT_USE.getVisitors()){
				if (v.getVisitorCategory().equals(mutationRule)){
					labels.add(v.getVisitorLabel());
				}
			}
		}
		return labels;
	}

	public static String getCategoryForLabel(String mutatorLabel) {
		VisitorList DO_NOT_USE = new VisitorList(null);
		for (CustomVisitor v : DO_NOT_USE.getVisitors()){
			if (v.getVisitorLabel().equals(mutatorLabel)){
				return v.getVisitorCategory();
			}
		}
		return "UNCATEGORIZED";
	}
		
}