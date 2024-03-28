package org.springframework.aop.aspectj.pointcut;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.aspectj.weaver.tools.PointcutExpression;
import org.aspectj.weaver.tools.PointcutParameter;
import org.aspectj.weaver.tools.PointcutParser;
import org.aspectj.weaver.tools.PointcutPrimitive;
import org.springframework.aop.aspectj.support.ExpressionPointcut;
import org.springframework.aop.support.pointcut.ClassFilter;
import org.springframework.aop.support.pointcut.MethodMatcher;
import org.springframework.core.common.Nullable;
import org.springframework.core.util.ClassUtils;

import cn.hutool.core.text.CharSequenceUtil;

/**
 * 支持AspectJ表达式的Pointcut
 */
public class AspectJExpressionPointcut implements ExpressionPointcut, ClassFilter, MethodMatcher {

    private static final Set<PointcutPrimitive> SUPPORTED_PRIMITIVES = new HashSet<>();

    static {
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.EXECUTION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.REFERENCE);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.THIS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.TARGET);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ANNOTATION);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_WITHIN);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_ARGS);
        SUPPORTED_PRIMITIVES.add(PointcutPrimitive.AT_TARGET);
    }

    private PointcutExpression pointcutExpression;
    private String expression;

    @Nullable
    private Class<?> pointcutDeclarationScope;
    private String[] pointcutParameterNames = new String[0];
    private Class<?>[] pointcutParameterTypes = new Class<?>[0];

    public AspectJExpressionPointcut() {}

    public AspectJExpressionPointcut(Class<?> declarationScope, String[] paramNames, Class<?>[] paramTypes) {
        this.pointcutDeclarationScope = declarationScope;
        if (paramNames.length != paramTypes.length) {
            throw new IllegalStateException(
                "Number of pointcut parameter names must match number of pointcut parameter types");
        }
        this.pointcutParameterNames = paramNames;
        this.pointcutParameterTypes = paramTypes;
    }

    @Override
    public boolean matches(Class<?> clazz) {
        PointcutExpression pointcutExpression = obtainPointcutExpression();
        return pointcutExpression.couldMatchJoinPointsInType(clazz);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        PointcutExpression pointcutExpression = obtainPointcutExpression();
        return pointcutExpression.matchesMethodExecution(method).alwaysMatches();
    }

    @Override
    public ClassFilter getClassFilter() {
        obtainPointcutExpression();
        return this;
    }

    @Override
    public MethodMatcher getMethodMatcher() {
        obtainPointcutExpression();
        return this;
    }

    private PointcutExpression obtainPointcutExpression() {
        if (getExpression() == null) {
            throw new IllegalStateException("Must set property 'expression' before attempting to match");
        }
        if (this.pointcutExpression == null) {
            this.pointcutExpression = buildPointcutExpression(ClassUtils.getDefaultClassLoader());
        }
        return this.pointcutExpression;
    }

    private PointcutExpression buildPointcutExpression(@Nullable ClassLoader classLoader) {
        PointcutParser parser = initializePointcutParser(classLoader);
        PointcutParameter[] pointcutParameters = new PointcutParameter[this.pointcutParameterNames.length];
        for (int i = 0; i < pointcutParameters.length; i++) {
            pointcutParameters[i] =
                parser.createPointcutParameter(this.pointcutParameterNames[i], this.pointcutParameterTypes[i]);
        }
        return parser.parsePointcutExpression(replaceBooleanOperators(getExpression()), this.pointcutDeclarationScope,
            pointcutParameters);
    }

    private PointcutParser initializePointcutParser(@Nullable ClassLoader classLoader) {
        return PointcutParser.getPointcutParserSupportingSpecifiedPrimitivesAndUsingSpecifiedClassLoaderForResolution(
            SUPPORTED_PRIMITIVES, classLoader);
    }

    private String replaceBooleanOperators(String pcExpr) {
        String result = CharSequenceUtil.replace(pcExpr, " and ", " && ");
        result = CharSequenceUtil.replace(result, " or ", " || ");
        result = CharSequenceUtil.replace(result, " not ", " ! ");
        return result;
    }

    @Nullable
    @Override
    public String getExpression() {
        return expression;
    }

    public void setExpression(@Nullable String expression) {
        this.expression = expression;
    }

    public PointcutExpression getPointcutExpression() {
        return obtainPointcutExpression();
    }

    public void setParameterNames(String... names) {
        this.pointcutParameterNames = names;
    }

    public void setParameterTypes(Class<?>... types) {
        this.pointcutParameterTypes = types;
    }
}
