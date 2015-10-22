package mutation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.JavaRuntime;

public class TestExecutor{
	
	private final static String JUNITCORE_PACKAGE_NAME = "org.junit.runner.JUnitCore";
	private final static String RESULT_PACKAGE_NAME = "org.junit.runner.Result";
	private final static String FAILURE_PACKAGE_NAME = "org.junit.runner.notification.Failure";
	private final static String LIST_PACKAGE_NAME = "java.util.List";
	private final static String RUN = "run";
	private final static String GET_FAILURE_COUNT = "getFailureCount";
	private final static String GET_FAILURES = "getFailures";
	private final static String GET_IGNORE_COUNT = "getIgnoreCount";
	private final static String GET_RUN_COUNT = "getRunCount";
	private final static String GET_RUN_TIME = "getRunTime";
	private final static String WAS_SUCCESFUL = "wasSuccessful";
	private final static String GET_EXCEPTION = "getException";
	private final static String GET_MESSAGE = "getMessage";
	private final static String GET_TEST_HEADER = "getTestHeader";
	private final static String GET_TRACE = "getTrace";
	private final static String TO_STRING = "toString";
	private final static String LIST_GET = "get";
	private URLClassLoader loader;
	private String projectName;
	private Class<?> junitcoreClass;
	private Class<?> resultClass;
	private Class<?> failureClass;
	private Class<?> listClass;
	private Object junitCoreInstance;
	private Method runTestMethod;
	private Method getFailureCountMethod;
	private Method getFailuresMethod;
	private Method getIgnoredCountMethod;
	private Method getRunCountMethod;
	private Method getRunTimeMethod;
	private Method wasSuccessfulMethod;
	private Method getExceptionMethod;
	private Method getMessageMethod;
	private Method getTestHeaderMethod;
	private Method getTraceMethod;
	private Method toStringMethod;
	private Method listGet;
	
	public TestExecutor(IJavaProject project) throws CoreException, MalformedURLException, ReflectiveOperationException{
		projectName = project.getElementName();
		String[] classPathEntries = JavaRuntime.computeDefaultRuntimeClassPath(project);
		List<URL> urlList = new ArrayList<URL>();
		for (int i = 0; i < classPathEntries.length; i++){
			String entry = classPathEntries[i];
			IPath path = new Path(entry);
			URL url = path.toFile().toURI().toURL();
			urlList.add(url);
		}
		ClassLoader parentClassLoader = project.getClass().getClassLoader();
		URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);
		loader = new URLClassLoader(urls, parentClassLoader);
		loadJUnitClasses();
	}
	
	private void loadJUnitClasses() throws ReflectiveOperationException{
		junitcoreClass = loader.loadClass(JUNITCORE_PACKAGE_NAME);
		resultClass = loader.loadClass(RESULT_PACKAGE_NAME);
		failureClass = loader.loadClass(FAILURE_PACKAGE_NAME);
		listClass = loader.loadClass(LIST_PACKAGE_NAME);
		junitCoreInstance = junitcoreClass.newInstance();
		runTestMethod = junitcoreClass.getDeclaredMethod(RUN, Class[].class);
		loadResultAndFailureMethods();
		listGet = listClass.getDeclaredMethod(LIST_GET, int.class);
	}
	
	@SuppressWarnings(value = {"all"})
	private void loadResultAndFailureMethods() throws ReflectiveOperationException{
		getFailureCountMethod = resultClass.getDeclaredMethod(GET_FAILURE_COUNT,null);
		getFailuresMethod = resultClass.getDeclaredMethod(GET_FAILURES, null);
		getIgnoredCountMethod = resultClass.getDeclaredMethod(GET_IGNORE_COUNT, null);
		getRunCountMethod = resultClass.getDeclaredMethod(GET_RUN_COUNT, null);
		getRunTimeMethod = resultClass.getDeclaredMethod(GET_RUN_TIME, null);
		wasSuccessfulMethod = resultClass.getDeclaredMethod(WAS_SUCCESFUL, null);
		getExceptionMethod = failureClass.getDeclaredMethod(GET_EXCEPTION, null);
		getMessageMethod = failureClass.getDeclaredMethod(GET_MESSAGE, null);
		getTestHeaderMethod = failureClass.getDeclaredMethod(GET_TEST_HEADER, null);
		getTraceMethod = failureClass.getDeclaredMethod(GET_TRACE, null);
		toStringMethod = failureClass.getDeclaredMethod(TO_STRING, null);
	}
	
	@SuppressWarnings(value = {"all"})
	public Result runAllTests(String fileName) throws IOException, ReflectiveOperationException {
		Class<?> clazz = loader.loadClass(fileName);
		Class<?>[] givenParameters = {clazz};
		Object res = runTestMethod.invoke(junitCoreInstance, (Object) givenParameters);
		Object failureList = getFailuresMethod.invoke(res, null);
		int failureCount = (int) getFailureCountMethod.invoke(res, null);
		List<Failure> failures = new ArrayList<Failure>();
		for (int i = 0; i < failureCount; i++){
			Object failure = listGet.invoke(failureList, i);
			Throwable exception = (Throwable) getExceptionMethod.invoke(failure, null);
			String message = (String) getMessageMethod.invoke(failure, null);
			String testHeader = (String) getTestHeaderMethod.invoke(failure, null);
			String trace = (String) getTraceMethod.invoke(failure, null);
			String toString = (String) toStringMethod.invoke(failure, null);
			Failure f = new Failure(exception, message, testHeader, trace, toString);
			failures.add(f);
		}
		int ignoredCount = (int) getIgnoredCountMethod.invoke(res, null);
		int runCount = (int) getRunCountMethod.invoke(res, null);
		long runTime = (long) getRunTimeMethod.invoke(res, null);
		boolean successful = (boolean) wasSuccessfulMethod.invoke(res, null);
		Result r = new Result(failureCount, failures, ignoredCount, runCount, runTime, successful);
		return r;
	}		

	public String getProjectName(){
		return projectName;
	}
	
}