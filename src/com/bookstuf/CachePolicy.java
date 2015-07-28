package com.bookstuf;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specify non-coherent cache policies using this annotation on data objects
 * stored in the datastore or served up via an RpcServlet.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CachePolicy {
	public int cacheDuration() default 0;
}
