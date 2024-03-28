package org.springframework.aop.aspectj.support;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.PerClauseKind;

import java.io.Serializable;

/**
 * Aspect元数据，主要封装诸如class、name等信息，主要用于后期实例化Aspect对象。
 */
public class AspectMetadata implements Serializable {
    private final String aspectName;
    private final Class<?> aspectClass;
    private final AjType<?> ajType;

    public AspectMetadata(Class<?> aspectClass,String aspectName) {
        this.aspectName = aspectName;
        Class<?> currClass = aspectClass;
        AjType<?> ajType = null;
        while (currClass != Object.class) {
            AjType<?> ajTypeToCheck = AjTypeSystem.getAjType(currClass);
            if (ajTypeToCheck.isAspect()) {
                ajType = ajTypeToCheck;
                break;
            }
            currClass = currClass.getSuperclass();
        }
        if (ajType == null) {
            throw new IllegalArgumentException("Class '" + aspectClass.getName() + "' is not an @AspectJ aspect");
        }
        if (ajType.getDeclarePrecedence().length > 0) {
            throw new IllegalArgumentException("DeclarePrecedence not presently supported in Spring AOP");
        }
        this.aspectClass = ajType.getJavaClass();
        this.ajType = ajType;
    }

    public String getAspectName() {
        return aspectName;
    }

    public AjType<?> getAjType() {
        return this.ajType;
    }

    public Class<?> getAspectClass() {
        return this.aspectClass;
    }

    public boolean isPerThisOrPerTarget() {
        return getAjType().getPerClause().getKind() == PerClauseKind.PERTARGET
            || getAjType().getPerClause().getKind() == PerClauseKind.PERTHIS;
    }

    public boolean isPerTypeWithin() {
        PerClauseKind kind = getAjType().getPerClause().getKind();
        return (kind == PerClauseKind.PERTYPEWITHIN);
    }

    public boolean isLazilyInstantiated() {
        return (isPerThisOrPerTarget() || isPerTypeWithin());
    }

}
