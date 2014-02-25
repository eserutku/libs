package uk.co.kayratech.aop.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;

@Order(value=10)
@Aspect
public class LoggingAspect {
	
	private static final String METHOD_PARAM_DELIM_START = "{";
	private static final String METHOD_PARAM_DELIM_END = "}";
	
	@Pointcut("execution(* uk.co.kayratech..*(..))"
			+ "&& !execution(* uk.co.kayratech.aop..*(..))"
			+ "&& !execution(* main(..))"
			+ "&& !execution(* get*(..))"
			+ "&& !execution(* set*(..))"
			+ "&& !execution(* is*(..))"
			+ "&& !@annotation(uk.co.kayratech.aop.annotations.NoAutoLogging)"
			+ "&& !@within(uk.co.kayratech.aop.annotations.NoAutoLogging)"
			)
	public void loggingPointcut() {}
	
	@Around("loggingPointcut()")
	public Object annotationExcludedAdvice(ProceedingJoinPoint joinpoint) throws Throwable {
		Object retval;
		Signature sig = joinpoint.getSignature();
		Object[] paramValues = joinpoint.getArgs();
		String[] paramNames = ((CodeSignature)sig).getParameterNames();
		
		logMethodEntry(paramValues, paramNames, sig);
		
		try {
			retval = joinpoint.proceed();
		} catch(Exception e) {
			System.err.println("Exception received: " + e.getMessage());
			throw(e);
		}
		
		logMethodExit(joinpoint, retval, sig);

		return retval;
	}
	
	private void logMethodEntry(Object[] methodParamVals, String[] methodParamNames, Signature sig) {
		
		StringBuffer logString = new StringBuffer("Entering ");
		
		logString.append("[" + sig.getDeclaringType().getName() + "." + sig.getName() + "]");
		logString.append(" with ");
		appendMethodParameterNamesAndValues(methodParamVals, methodParamNames, logString);
		
		System.out.println(logString.toString());
	}
	
	private void appendMethodParameterNamesAndValues(Object[] methodParamVals, String[] methodParamNames, 
			StringBuffer logString) {
		
		logString.append(METHOD_PARAM_DELIM_START);
		for (int i=0; i<methodParamVals.length; i++) {
			logString.append(methodParamNames[i] + " = " + paramValInString(methodParamVals[i] + 
					" - "));
		}
		logString.append(METHOD_PARAM_DELIM_END);
		
		// Replace last method param separator with log end separator
		if (methodParamVals.length > 0) {
			logString.replace(logString.indexOf(" - " + METHOD_PARAM_DELIM_END), logString.length(), 
					METHOD_PARAM_DELIM_END);
		}
	}
	
	private String paramValInString(Object parameter) {
		if (parameter == null) {
			return "NULL";
		} else if (parameter instanceof Long) {
			return "" + ((Long)parameter).longValue();
		} else if (parameter instanceof Integer) {
			return "" + ((Integer)parameter).intValue();
		} else if (parameter instanceof Float) {
			return "" + ((Float)parameter).floatValue();
		} else if (parameter instanceof StringBuffer) {
			return new String((StringBuffer)parameter);
		} else {
			return parameter.toString();
		}
	}
	
	private void logMethodExit(ProceedingJoinPoint joinpoint, Object retval, Signature sig) {
		StringBuffer logString = new StringBuffer("Leaving ");
		
		logString.append("[" + sig.getDeclaringType().getName() + "." + sig.getName() + "]");
		logString.append(" with ");
		logString.append(METHOD_PARAM_DELIM_START);
		
		MethodSignature methodSignature = (MethodSignature)(joinpoint.getSignature());
		if (retval == null) {
			if (methodSignature.getReturnType() == void.class) {
				logString.append("void");
			} else {
				logString.append("null");
			}
		} else {
			logString.append(paramValInString(retval));
		}
		logString.append(METHOD_PARAM_DELIM_END);
		
		System.out.println(logString.toString());
	}
}
