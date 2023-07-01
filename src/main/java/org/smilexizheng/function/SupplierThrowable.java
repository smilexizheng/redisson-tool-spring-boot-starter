package org.smilexizheng.function;

import org.springframework.lang.Nullable;

/**
 *
 * @author BJWK
 */
@FunctionalInterface
public interface SupplierThrowable<T> {
    @Nullable
    T get() throws Throwable;
}
