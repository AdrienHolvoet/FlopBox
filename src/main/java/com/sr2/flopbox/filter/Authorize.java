package com.sr2.flopbox.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.ws.rs.NameBinding;

/**
 * A annotation that binds to a filter. The endpoints that will have this
 * annotation can be invoked only if the caller is authenticated. This
 * annotation will be only use for authentication to the flopBox platform and
 * protect more sensitive operations such as adding, updating and deleting a
 * Server
 * 
 * @author Adrien Holvoet
 */
@NameBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.TYPE })
public @interface Authorize {

}
