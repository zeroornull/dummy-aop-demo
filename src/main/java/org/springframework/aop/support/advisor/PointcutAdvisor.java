package org.springframework.aop.support.advisor;

import org.springframework.aop.aspectj.support.ExpressionPointcut;
import org.springframework.aop.support.pointcut.Pointcut;

/**
 * Advisor = Advice + Pointcut
 *
 * Advice代表实际的增强逻辑。一般来说，我们编写一个Advice是为了某个方法或者某些方法服务的。
 * 那么，如果表示"某个方法/某些方法"呢？
 * Pointcut就是干这个的。
 * Pointcut代表一个切入点，通俗地说就是一个匹配规则。只有符合Pointcut的Method才有资格被增强。
 * Pointcut只是一个接口，具体实现可以多种多样，比如：
 * - {@link Pointcut#TRUE} 默认匹配任意方法
 * - {@link ExpressionPointcut} 使用表达式定义匹配规则，比如使用AspectJ表达式的AspectJExpressionPointcut
 */
public interface PointcutAdvisor extends Advisor{
    
    Pointcut getPointcut();
}
