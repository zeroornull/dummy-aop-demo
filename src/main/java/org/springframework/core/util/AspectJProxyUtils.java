package org.springframework.core.util;


import org.springframework.core.common.Nullable;

import cn.hutool.core.util.StrUtil;

public abstract class AspectJProxyUtils {

    public static boolean isVariableName(@Nullable String name) {
        if (StrUtil.isBlank(name)) {
            return false;
        }
        if (!Character.isJavaIdentifierStart(name.charAt(0))) {
            return false;
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                return false;
            }
        }
        return true;
    }

}
