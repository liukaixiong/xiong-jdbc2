package com.x.jdbc.template.common.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;

public class ExceptionUtil {
    public ExceptionUtil() {
    }

    public static Throwable unwrapThrowable(Throwable wrapped) {
        Throwable unwrapped = wrapped;

        while(true) {
            while(!(unwrapped instanceof InvocationTargetException)) {
                if(!(unwrapped instanceof UndeclaredThrowableException)) {
                    return unwrapped;
                }

                unwrapped = ((UndeclaredThrowableException)unwrapped).getUndeclaredThrowable();
            }

            unwrapped = ((InvocationTargetException)unwrapped).getTargetException();
        }
    }
}
