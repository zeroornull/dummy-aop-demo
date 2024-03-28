package org.springframework.aop.aspectj.advice;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInvocation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.weaver.tools.JoinPointMatch;
import org.aspectj.weaver.tools.PointcutParameter;
import org.springframework.aop.aspectj.aspect.AspectInstanceFactory;
import org.springframework.aop.aspectj.joinpoint.MethodInvocationProceedingJoinPoint;
import org.springframework.aop.aspectj.pointcut.AspectJExpressionPointcut;
import org.springframework.aop.aspectj.support.AspectJAdviceParameterNameDiscoverer;
import org.springframework.aop.aspectj.support.ExposeInvocationInterceptor;
import org.springframework.aop.framework.ProxyMethodInvocation;
import org.springframework.core.common.Nullable;
import org.springframework.core.exception.AopInvocationException;
import org.springframework.core.parametername.DefaultParameterNameDiscoverer;
import org.springframework.core.parametername.ParameterNameDiscoverer;
import org.springframework.core.util.AspectJProxyUtils;
import org.springframework.core.util.ClassUtils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;

/**
 * 抽象AspectJAdvice
 * - before：{@link AspectJMethodBeforeAdvice}
 * - after：
 * - {@link AspectJAfterAdvice} 一定执行
 * - {@link AspectJAfterThrowingAdvice} 异常时执行
 * - {@link AspectJAfterReturningAdvice} 正常返回才执行
 */
public abstract class AbstractAspectJAdvice implements Advice, Serializable {

    /**
     * Key used in ReflectiveMethodInvocation userAttributes map for the current joinPoint.
     */
    protected static final String JOIN_POINT_KEY = JoinPoint.class.getName();

    public static JoinPoint currentJoinPoint() {
        MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
        if (!(mi instanceof ProxyMethodInvocation)) {
            throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        ProxyMethodInvocation pmi = (ProxyMethodInvocation) mi;
        JoinPoint jp = (JoinPoint) pmi.getUserAttribute(JOIN_POINT_KEY);
        if (jp == null) {
            jp = new MethodInvocationProceedingJoinPoint(pmi);
            pmi.setUserAttribute(JOIN_POINT_KEY, jp);
        }
        return jp;
    }


    private final Class<?>[] parameterTypes;

    protected transient Method aspectJAdviceMethod;

    private final AspectJExpressionPointcut pointcut;

    private final AspectInstanceFactory aspectInstanceFactory;

    // 下面这些是对AspectJ注解参数的抽象，比如
    // @AfterReturning(returning = "res", value = "pointcut()")
    // @AfterThrowing(throwing = "ex", value = "pointcut()")
    @Nullable
    private String[] argumentNames;

    @Nullable
    private String throwingName;

    @Nullable
    private String returningName;

    private Class<?> discoveredReturningType = Object.class;

    private Class<?> discoveredThrowingType = Object.class;

    private int joinPointArgumentIndex = -1;

    private int joinPointStaticPartArgumentIndex = -1;

    private boolean argumentsIntrospected = false;

    @Nullable
    private Map<String, Integer> argumentBindings;

    public AbstractAspectJAdvice(Method aspectJAdviceMethod, AspectJExpressionPointcut pointcut, AspectInstanceFactory aspectInstanceFactory) {
        Assert.notNull(aspectJAdviceMethod, "Advice method must not be null");
        this.parameterTypes = aspectJAdviceMethod.getParameterTypes();
        this.aspectJAdviceMethod = aspectJAdviceMethod;
        this.pointcut = pointcut;
        this.aspectInstanceFactory = aspectInstanceFactory;
    }

    public void setReturningName(String name) {
        throw new UnsupportedOperationException("Only afterReturning advice can be used to bind a return value");
    }

    protected void setReturningNameNoCheck(String name) {
        // name could be a variable or a type...
        if (AspectJProxyUtils.isVariableName(name)) {
            this.returningName = name;
        } else {
            // assume a type
            try {
                this.discoveredReturningType = ClassUtils.forName(name, this.getClass().getClassLoader());
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Returning name '" + name +
                        "' is neither a valid argument name nor the fully-qualified " +
                        "name of a Java type on the classpath. Root cause: " + ex);
            }
        }
    }

    protected Class<?> getDiscoveredReturningType() {
        return this.discoveredReturningType;
    }

    public void setThrowingName(String name) {
        throw new UnsupportedOperationException("Only afterThrowing advice can be used to bind a thrown exception");
    }

    protected void setThrowingNameNoCheck(String name) {
        // name could be a variable or a type...
        if (AspectJProxyUtils.isVariableName(name)) {
            this.throwingName = name;
        } else {
            // assume a type
            try {
                this.discoveredThrowingType = ClassUtils.forName(name, this.getClass().getClassLoader());
            } catch (Throwable ex) {
                throw new IllegalArgumentException("Throwing name '" + name +
                        "' is neither a valid argument name nor the fully-qualified " +
                        "name of a Java type on the classpath. Root cause: " + ex);
            }
        }
    }

    protected Class<?> getDiscoveredThrowingType() {
        return this.discoveredThrowingType;
    }

    public final void calculateArgumentBindings() {
        if (this.argumentsIntrospected || this.parameterTypes.length == 0) {
            return;
        }

        int numUnboundArgs = this.parameterTypes.length;
        Class<?>[] parameterTypes = this.aspectJAdviceMethod.getParameterTypes();
        if (maybeBindJoinPoint(parameterTypes[0]) || maybeBindProceedingJoinPoint(parameterTypes[0]) ||
                maybeBindJoinPointStaticPart(parameterTypes[0])) {
            numUnboundArgs--;
        }

        if (numUnboundArgs > 0) {
            // need to bind arguments by name as returned from the pointcut match
            bindArgumentsByName(numUnboundArgs);
        }

        this.argumentsIntrospected = true;
    }

    private boolean maybeBindJoinPoint(Class<?> candidateParameterType) {
        if (JoinPoint.class == candidateParameterType) {
            this.joinPointArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    private boolean maybeBindProceedingJoinPoint(Class<?> candidateParameterType) {
        if (ProceedingJoinPoint.class == candidateParameterType) {
            if (!supportsProceedingJoinPoint()) {
                throw new IllegalArgumentException("ProceedingJoinPoint is only supported for around advice");
            }
            this.joinPointArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    private boolean maybeBindJoinPointStaticPart(Class<?> candidateParameterType) {
        if (JoinPoint.StaticPart.class == candidateParameterType) {
            this.joinPointStaticPartArgumentIndex = 0;
            return true;
        } else {
            return false;
        }
    }

    private void bindArgumentsByName(int numArgumentsExpectingToBind) {
        if (this.argumentNames == null) {
            this.argumentNames = createParameterNameDiscoverer().getParameterNames(this.aspectJAdviceMethod);
        }
        if (this.argumentNames != null) {
            // We have been able to determine the arg names.
            bindExplicitArguments(numArgumentsExpectingToBind);
        } else {
            throw new IllegalStateException("Advice method [" + this.aspectJAdviceMethod.getName() + "] " +
                    "requires " + numArgumentsExpectingToBind + " arguments to be bound by name, but " +
                    "the argument names were not specified and could not be discovered.");
        }
    }

    protected ParameterNameDiscoverer createParameterNameDiscoverer() {
        // We need to discover them, or if that fails, guess,
        // and if we can't guess with 100% accuracy, fail.
        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        AspectJAdviceParameterNameDiscoverer adviceParameterNameDiscoverer =
                new AspectJAdviceParameterNameDiscoverer(this.pointcut.getExpression());
        adviceParameterNameDiscoverer.setReturningName(this.returningName);
        adviceParameterNameDiscoverer.setThrowingName(this.throwingName);
        // Last in chain, so if we're called and we fail, that's bad...
        adviceParameterNameDiscoverer.setRaiseExceptions(true);
        discoverer.addDiscoverer(adviceParameterNameDiscoverer);
        return discoverer;
    }

    private void bindExplicitArguments(int numArgumentsLeftToBind) {
        Assert.state(this.argumentNames != null, "No argument names available");
        this.argumentBindings = new HashMap<>();

        int numExpectedArgumentNames = this.aspectJAdviceMethod.getParameterCount();
        if (this.argumentNames.length != numExpectedArgumentNames) {
            throw new IllegalStateException("Expecting to find " + numExpectedArgumentNames +
                    " arguments to bind by name in advice, but actually found " +
                    this.argumentNames.length + " arguments.");
        }

        // So we match in number...
        int argumentIndexOffset = this.parameterTypes.length - numArgumentsLeftToBind;
        for (int i = argumentIndexOffset; i < this.argumentNames.length; i++) {
            this.argumentBindings.put(this.argumentNames[i], i);
        }

        // Check that returning and throwing were in the argument names list if
        // specified, and find the discovered argument types.
        if (this.returningName != null) {
            if (!this.argumentBindings.containsKey(this.returningName)) {
                throw new IllegalStateException("Returning argument name '" + this.returningName +
                        "' was not bound in advice arguments");
            } else {
                Integer index = this.argumentBindings.get(this.returningName);
                this.discoveredReturningType = this.aspectJAdviceMethod.getParameterTypes()[index];
            }
        }
        if (this.throwingName != null) {
            if (!this.argumentBindings.containsKey(this.throwingName)) {
                throw new IllegalStateException("Throwing argument name '" + this.throwingName +
                        "' was not bound in advice arguments");
            } else {
                Integer index = this.argumentBindings.get(this.throwingName);
                this.discoveredThrowingType = this.aspectJAdviceMethod.getParameterTypes()[index];
            }
        }

        // configure the pointcut expression accordingly.
        configurePointcutParameters(this.argumentNames, argumentIndexOffset);
    }

    private void configurePointcutParameters(String[] argumentNames, int argumentIndexOffset) {
        int numParametersToRemove = argumentIndexOffset;
        if (this.returningName != null) {
            numParametersToRemove++;
        }
        if (this.throwingName != null) {
            numParametersToRemove++;
        }
        String[] pointcutParameterNames = new String[argumentNames.length - numParametersToRemove];
        Class<?>[] pointcutParameterTypes = new Class<?>[pointcutParameterNames.length];
        Class<?>[] methodParameterTypes = this.aspectJAdviceMethod.getParameterTypes();

        int index = 0;
        for (int i = 0; i < argumentNames.length; i++) {
            if (i < argumentIndexOffset) {
                continue;
            }
            if (argumentNames[i].equals(this.returningName) ||
                    argumentNames[i].equals(this.throwingName)) {
                continue;
            }
            pointcutParameterNames[index] = argumentNames[i];
            pointcutParameterTypes[index] = methodParameterTypes[i];
            index++;
        }

        this.pointcut.setParameterNames(pointcutParameterNames);
        this.pointcut.setParameterTypes(pointcutParameterTypes);
    }

    protected boolean supportsProceedingJoinPoint() {
        return false;
    }

    protected Object[] argBinding(JoinPoint jp, @Nullable JoinPointMatch jpMatch,
                                  @Nullable Object returnValue, @Nullable Throwable ex) {

        calculateArgumentBindings();

        // AMC start
        Object[] adviceInvocationArgs = new Object[this.parameterTypes.length];
        int numBound = 0;

        if (this.joinPointArgumentIndex != -1) {
            adviceInvocationArgs[this.joinPointArgumentIndex] = jp;
            numBound++;
        } else if (this.joinPointStaticPartArgumentIndex != -1) {
            adviceInvocationArgs[this.joinPointStaticPartArgumentIndex] = jp.getStaticPart();
            numBound++;
        }

        if (!CollUtil.isEmpty(this.argumentBindings)) {
            // binding from pointcut match
            if (jpMatch != null) {
                PointcutParameter[] parameterBindings = jpMatch.getParameterBindings();
                for (PointcutParameter parameter : parameterBindings) {
                    String name = parameter.getName();
                    Integer index = this.argumentBindings.get(name);
                    adviceInvocationArgs[index] = parameter.getBinding();
                    numBound++;
                }
            }
            // binding from returning clause
            if (this.returningName != null) {
                Integer index = this.argumentBindings.get(this.returningName);
                adviceInvocationArgs[index] = returnValue;
                numBound++;
            }
            // binding from thrown exception
            if (this.throwingName != null) {
                Integer index = this.argumentBindings.get(this.throwingName);
                adviceInvocationArgs[index] = ex;
                numBound++;
            }
        }

        if (numBound != this.parameterTypes.length) {
            throw new IllegalStateException("Required to bind " + this.parameterTypes.length +
                    " arguments, but only bound " + numBound + " (JoinPointMatch " +
                    (jpMatch == null ? "was NOT" : "WAS") + " bound in invocation)");
        }

        return adviceInvocationArgs;
    }


    protected Object invokeAdviceMethod(
            @Nullable JoinPointMatch jpMatch, @Nullable Object returnValue, @Nullable Throwable ex)
            throws Throwable {

        return invokeAdviceMethodWithGivenArgs(argBinding(getJoinPoint(), jpMatch, returnValue, ex));
    }

    protected Object invokeAdviceMethod(JoinPoint jp, @Nullable JoinPointMatch jpMatch,
                                        @Nullable Object returnValue, @Nullable Throwable t) throws Throwable {

        return invokeAdviceMethodWithGivenArgs(argBinding(jp, jpMatch, returnValue, t));
    }

    protected Object invokeAdviceMethodWithGivenArgs(Object[] args) throws Throwable {
        Object[] actualArgs = args;
        if (this.aspectJAdviceMethod.getParameterCount() == 0) {
            actualArgs = null;
        }
        try {
            this.aspectJAdviceMethod.setAccessible(true);
            return this.aspectJAdviceMethod.invoke(this.aspectInstanceFactory.getAspectInstance(), actualArgs);
        } catch (IllegalArgumentException ex) {
            throw new AopInvocationException("Mismatch on arguments to advice method [" +
                    this.aspectJAdviceMethod + "]; pointcut expression [" +
                    this.pointcut.getPointcutExpression() + "]", ex);
        } catch (InvocationTargetException ex) {
            throw ex.getTargetException();
        }
    }

    /**
     * Around通知需要重写此方法，返回ProceedingJoinPoint
     */
    protected JoinPoint getJoinPoint() {
        return currentJoinPoint();
    }

    /**
     * Get the current join point match at the join point we are being dispatched on.
     */
    @Nullable
    protected JoinPointMatch getJoinPointMatch() {
        MethodInvocation mi = ExposeInvocationInterceptor.currentInvocation();
        if (!(mi instanceof ProxyMethodInvocation)) {
            throw new IllegalStateException("MethodInvocation is not a Spring ProxyMethodInvocation: " + mi);
        }
        return getJoinPointMatch((ProxyMethodInvocation) mi);
    }


    @Nullable
    protected JoinPointMatch getJoinPointMatch(ProxyMethodInvocation pmi) {
        String expression = this.pointcut.getExpression();
        return (expression != null ? (JoinPointMatch) pmi.getUserAttribute(expression) : null);
    }


    @Override
    public String toString() {
        return getClass().getName() + ": advice method [" + this.aspectJAdviceMethod + "]; ";
    }

}
