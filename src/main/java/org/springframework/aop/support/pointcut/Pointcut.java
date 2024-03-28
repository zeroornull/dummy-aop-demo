package org.springframework.aop.support.pointcut;

/**
 * 切点：Advice的匹配规则。对符合规则的方法进行拦截。
 */
public interface Pointcut {
    
    ClassFilter getClassFilter();
    
    MethodMatcher getMethodMatcher();
    
    // 静态常量，表示匹配所有类和所有方法。
    Pointcut TRUE = new Pointcut() {
        @Override
        public ClassFilter getClassFilter() {
            return ClassFilter.TRUE;
        }

        @Override
        public MethodMatcher getMethodMatcher() {
            return MethodMatcher.TRUE;
        }
    };
    
}
