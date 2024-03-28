package org.springframework.aop.support.pointcut;

/**
 * @author: xxp
 * @date: 2024/3/28 6:59
 * @description: TODO
 **/
public interface ClassFilter {
    boolean matches(Class<?> clazz);
    
    ClassFilter TRUE = new ClassFilter() {
        @Override
        public boolean matches(Class<?> clazz) {
            return true;
        }
    };
}
