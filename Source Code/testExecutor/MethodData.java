package testExecutor;

public class MethodData {

	private String methodName;
	private Object args;
	
	public MethodData(String name, Object args){
		methodName = name;
		this.args = args;
	}
	
	public String getMethodName(){
		return methodName;
	}
	
	public Object getArgs(){
		return args;
	}
	
	public boolean nameEquals(String name){
		return methodName.equals(name);
	}
	
}