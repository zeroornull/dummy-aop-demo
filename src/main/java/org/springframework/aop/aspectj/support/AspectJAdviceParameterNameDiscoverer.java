package org.springframework.aop.aspectj.support;

import cn.hutool.core.util.StrUtil;
import org.springframework.core.common.Nullable;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.core.parametername.ParameterNameDiscoverer;
import org.springframework.core.util.AspectJProxyUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AspectJAdviceParameterNameDiscoverer implements ParameterNameDiscoverer {

    private static final String THIS_JOIN_POINT = "thisJoinPoint";
    private static final String THIS_JOIN_POINT_STATIC_PART = "thisJoinPointStaticPart";

    // Steps in the binding algorithm...
    private static final int STEP_JOIN_POINT_BINDING = 1;
    private static final int STEP_THROWING_BINDING = 2;
    private static final int STEP_ANNOTATION_BINDING = 3;
    private static final int STEP_RETURNING_BINDING = 4;
    private static final int STEP_FINISHED = 8;

    private static final Set<String> singleValuedAnnotationPcds = new HashSet<>();

    static {
        singleValuedAnnotationPcds.add("@this");
        singleValuedAnnotationPcds.add("@target");
        singleValuedAnnotationPcds.add("@within");
        singleValuedAnnotationPcds.add("@annotation");
    }


    /**
     * The pointcut expression associated with the advice, as a simple String.
     */
    @Nullable
    private final String pointcutExpression;

    private boolean raiseExceptions;

    /**
     * If the advice is afterReturning, and binds the return value, this is the parameter name used.
     */
    @Nullable
    private String returningName;

    /**
     * If the advice is afterThrowing, and binds the thrown value, this is the parameter name used.
     */
    @Nullable
    private String throwingName;

    private Class<?>[] argumentTypes = new Class<?>[0];

    private String[] parameterNameBindings = new String[0];

    private int numberOfRemainingUnboundArguments;


    /**
     * Create a new discoverer that attempts to discover parameter names.
     * from the given pointcut expression.
     */
    public AspectJAdviceParameterNameDiscoverer(@Nullable String pointcutExpression) {
        this.pointcutExpression = pointcutExpression;
    }


    /**
     * Indicate whether {@link IllegalArgumentException} and {@link AmbiguousBindingException}
     * must be thrown as appropriate in the case of failing to deduce advice parameter names.
     *
     * @param raiseExceptions {@code true} if exceptions are to be thrown
     */
    public void setRaiseExceptions(boolean raiseExceptions) {
        this.raiseExceptions = raiseExceptions;
    }

    public void setReturningName(@Nullable String returningName) {
        this.returningName = returningName;
    }

    public void setThrowingName(@Nullable String throwingName) {
        this.throwingName = throwingName;
    }

    @Override
    @Nullable
    public String[] getParameterNames(Method method) {
        this.argumentTypes = method.getParameterTypes();
        this.numberOfRemainingUnboundArguments = this.argumentTypes.length;
        this.parameterNameBindings = new String[this.numberOfRemainingUnboundArguments];

        int minimumNumberUnboundArgs = 0;
        if (this.returningName != null) {
            minimumNumberUnboundArgs++;
        }
        if (this.throwingName != null) {
            minimumNumberUnboundArgs++;
        }
        if (this.numberOfRemainingUnboundArguments < minimumNumberUnboundArgs) {
            throw new IllegalStateException(
                    "Not enough arguments in method to satisfy binding of returning and throwing variables");
        }

        try {
            int algorithmicStep = STEP_JOIN_POINT_BINDING;
            while ((this.numberOfRemainingUnboundArguments > 0) && algorithmicStep < STEP_FINISHED) {
                switch (algorithmicStep++) {
                    case STEP_JOIN_POINT_BINDING:
                        if (!maybeBindThisJoinPoint()) {
                            maybeBindThisJoinPointStaticPart();
                        }
                        break;
                    case STEP_THROWING_BINDING:
                        maybeBindThrowingVariable();
                        break;
                    case STEP_ANNOTATION_BINDING:
                        maybeBindAnnotationsFromPointcutExpression();
                        break;
                    case STEP_RETURNING_BINDING:
                        maybeBindReturningVariable();
                        break;
                    default:
                        throw new IllegalStateException("Unknown algorithmic step: " + (algorithmicStep - 1));
                }
            }
        } catch (AmbiguousBindingException | IllegalArgumentException ex) {
            if (this.raiseExceptions) {
                throw ex;
            } else {
                return null;
            }
        }

        if (this.numberOfRemainingUnboundArguments == 0) {
            return this.parameterNameBindings;
        } else {
            if (this.raiseExceptions) {
                throw new IllegalStateException("Failed to bind all argument names: " +
                        this.numberOfRemainingUnboundArguments + " argument(s) could not be bound");
            } else {
                // convention for failing is to return null, allowing participation in a chain of responsibility
                return null;
            }
        }
    }

    @Override
    @Nullable
    public String[] getParameterNames(Constructor<?> ctor) {
        if (this.raiseExceptions) {
            throw new UnsupportedOperationException("An advice method can never be a constructor");
        } else {
            // we return null rather than throw an exception so that we behave well
            // in a chain-of-responsibility.
            return null;
        }
    }


    private void bindParameterName(int index, @Nullable String name) {
        this.parameterNameBindings[index] = name;
        this.numberOfRemainingUnboundArguments--;
    }

    /**
     * If the first parameter is of type JoinPoint or ProceedingJoinPoint, bind "thisJoinPoint" as
     * parameter name and return true, else return false.
     */
    private boolean maybeBindThisJoinPoint() {
        if ((this.argumentTypes[0] == JoinPoint.class) || (this.argumentTypes[0] == ProceedingJoinPoint.class)) {
            bindParameterName(0, THIS_JOIN_POINT);
            return true;
        } else {
            return false;
        }
    }

    private void maybeBindThisJoinPointStaticPart() {
        if (this.argumentTypes[0] == JoinPoint.StaticPart.class) {
            bindParameterName(0, THIS_JOIN_POINT_STATIC_PART);
        }
    }

    /**
     * If a throwing name was specified and there is exactly one choice remaining
     * (argument that is a subtype of Throwable) then bind it.
     */
    private void maybeBindThrowingVariable() {
        if (this.throwingName == null) {
            return;
        }

        // So there is binding work to do...
        int throwableIndex = -1;
        for (int i = 0; i < this.argumentTypes.length; i++) {
            if (isUnbound(i) && isSubtypeOf(Throwable.class, i)) {
                if (throwableIndex == -1) {
                    throwableIndex = i;
                } else {
                    // Second candidate we've found - ambiguous binding
                    throw new AmbiguousBindingException("Binding of throwing parameter '" +
                            this.throwingName + "' is ambiguous: could be bound to argument " +
                            throwableIndex + " or argument " + i);
                }
            }
        }

        if (throwableIndex == -1) {
            throw new IllegalStateException("Binding of throwing parameter '" + this.throwingName +
                    "' could not be completed as no available arguments are a subtype of Throwable");
        } else {
            bindParameterName(throwableIndex, this.throwingName);
        }
    }

    /**
     * If a returning variable was specified and there is only one choice remaining, bind it.
     */
    private void maybeBindReturningVariable() {
        if (this.numberOfRemainingUnboundArguments == 0) {
            throw new IllegalStateException(
                    "Algorithm assumes that there must be at least one unbound parameter on entry to this method");
        }

        if (this.returningName != null) {
            if (this.numberOfRemainingUnboundArguments > 1) {
                throw new AmbiguousBindingException("Binding of returning parameter '" + this.returningName +
                        "' is ambiguous, there are " + this.numberOfRemainingUnboundArguments + " candidates.");
            }

            // We're all set... find the unbound parameter, and bind it.
            for (int i = 0; i < this.parameterNameBindings.length; i++) {
                if (this.parameterNameBindings[i] == null) {
                    bindParameterName(i, this.returningName);
                    break;
                }
            }
        }
    }

    /**
     * Parse the string pointcut expression looking for:
     * &#64;this, &#64;target, &#64;args, &#64;within, &#64;withincode, &#64;annotation.
     * If we find one of these pointcut expressions, try and extract a candidate variable
     * name (or variable names, in the case of args).
     * <p>Some more support from AspectJ in doing this exercise would be nice... :)
     */
    private void maybeBindAnnotationsFromPointcutExpression() {
        List<String> varNames = new ArrayList<>();
        String[] tokens = StrUtil.split(this.pointcutExpression, " ").toArray(new String[0]);
        for (int i = 0; i < tokens.length; i++) {
            String toMatch = tokens[i];
            int firstParenIndex = toMatch.indexOf('(');
            if (firstParenIndex != -1) {
                toMatch = toMatch.substring(0, firstParenIndex);
            }
            if (singleValuedAnnotationPcds.contains(toMatch)) {
                PointcutBody body = getPointcutBody(tokens, i);
                i += body.numTokensConsumed;
                String varName = maybeExtractVariableName(body.text);
                if (varName != null) {
                    varNames.add(varName);
                }
            } else if (tokens[i].startsWith("@args(") || tokens[i].equals("@args")) {
                PointcutBody body = getPointcutBody(tokens, i);
                i += body.numTokensConsumed;
                maybeExtractVariableNamesFromArgs(body.text, varNames);
            }
        }

        bindAnnotationsFromVarNames(varNames);
    }

    /**
     * Match the given list of extracted variable names to argument slots.
     */
    private void bindAnnotationsFromVarNames(List<String> varNames) {
        if (!varNames.isEmpty()) {
            // we have work to do...
            int numAnnotationSlots = countNumberOfUnboundAnnotationArguments();
            if (numAnnotationSlots > 1) {
                throw new AmbiguousBindingException("Found " + varNames.size() +
                        " potential annotation variable(s), and " +
                        numAnnotationSlots + " potential argument slots");
            } else if (numAnnotationSlots == 1) {
                if (varNames.size() == 1) {
                    // it's a match
                    findAndBind(Annotation.class, varNames.get(0));
                } else {
                    // multiple candidate vars, but only one slot
                    throw new IllegalArgumentException("Found " + varNames.size() +
                            " candidate annotation binding variables" +
                            " but only one potential argument binding slot");
                }
            } else {
                // no slots so presume those candidate vars were actually type names
            }
        }
    }

    /**
     * If the token starts meets Java identifier conventions, it's in.
     */
    @Nullable
    private String maybeExtractVariableName(@Nullable String candidateToken) {
        if (AspectJProxyUtils.isVariableName(candidateToken)) {
            return candidateToken;
        }
        return null;
    }

    /**
     * Given an args pointcut body (could be {@code args} or {@code at_args}),
     * add any candidate variable names to the given list.
     */
    private void maybeExtractVariableNamesFromArgs(@Nullable String argsSpec, List<String> varNames) {
        if (argsSpec == null) {
            return;
        }
        String[] tokens = StrUtil.split(argsSpec, ",").toArray(new String[0]);
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = StrUtil.trim(tokens[i]);
            String varName = maybeExtractVariableName(tokens[i]);
            if (varName != null) {
                varNames.add(varName);
            }
        }
    }

    /**
     * We've found the start of a binding pointcut at the given index into the
     * token array. Now we need to extract the pointcut body and return it.
     */
    private PointcutBody getPointcutBody(String[] tokens, int startIndex) {
        int numTokensConsumed = 0;
        String currentToken = tokens[startIndex];
        int bodyStart = currentToken.indexOf('(');
        if (currentToken.charAt(currentToken.length() - 1) == ')') {
            // It's an all in one... get the text between the first (and the last)
            return new PointcutBody(0, currentToken.substring(bodyStart + 1, currentToken.length() - 1));
        } else {
            StringBuilder sb = new StringBuilder();
            if (bodyStart >= 0 && bodyStart != (currentToken.length() - 1)) {
                sb.append(currentToken.substring(bodyStart + 1));
                sb.append(' ');
            }
            numTokensConsumed++;
            int currentIndex = startIndex + numTokensConsumed;
            while (currentIndex < tokens.length) {
                if (tokens[currentIndex].equals("(")) {
                    currentIndex++;
                    continue;
                }

                if (tokens[currentIndex].endsWith(")")) {
                    sb.append(tokens[currentIndex], 0, tokens[currentIndex].length() - 1);
                    return new PointcutBody(numTokensConsumed, sb.toString().trim());
                }

                String toAppend = tokens[currentIndex];
                if (toAppend.startsWith("(")) {
                    toAppend = toAppend.substring(1);
                }
                sb.append(toAppend);
                sb.append(' ');
                currentIndex++;
                numTokensConsumed++;
            }

        }

        // We looked and failed...
        return new PointcutBody(numTokensConsumed, null);
    }

    /*
     * Return true if the parameter name binding for the given parameter
     * index has not yet been assigned.
     */
    private boolean isUnbound(int i) {
        return this.parameterNameBindings[i] == null;
    }

    /**
     * Return {@code true} if the given argument type is a subclass
     * of the given supertype.
     */
    private boolean isSubtypeOf(Class<?> supertype, int argumentNumber) {
        return supertype.isAssignableFrom(this.argumentTypes[argumentNumber]);
    }

    private int countNumberOfUnboundAnnotationArguments() {
        int count = 0;
        for (int i = 0; i < this.argumentTypes.length; i++) {
            if (isUnbound(i) && isSubtypeOf(Annotation.class, i)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Find the argument index with the given type, and bind the given
     * {@code varName} in that position.
     */
    private void findAndBind(Class<?> argumentType, String varName) {
        for (int i = 0; i < this.argumentTypes.length; i++) {
            if (isUnbound(i) && isSubtypeOf(argumentType, i)) {
                bindParameterName(i, varName);
                return;
            }
        }
        throw new IllegalStateException("Expected to find an unbound argument of type '" +
                argumentType.getName() + "'");
    }


    /**
     * Simple struct to hold the extracted text from a pointcut body, together
     * with the number of tokens consumed in extracting it.
     */
    private static class PointcutBody {

        private final int numTokensConsumed;

        @Nullable
        private final String text;

        public PointcutBody(int tokens, @Nullable String text) {
            this.numTokensConsumed = tokens;
            this.text = text;
        }
    }


    /**
     * Thrown in response to an ambiguous binding being detected when
     * trying to resolve a method's parameter names.
     */
    public static class AmbiguousBindingException extends RuntimeException {

        /**
         * Construct a new AmbiguousBindingException with the specified message.
         *
         * @param msg the detail message
         */
        public AmbiguousBindingException(String msg) {
            super(msg);
        }
    }

}
