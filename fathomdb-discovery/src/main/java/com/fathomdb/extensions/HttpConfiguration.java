package com.fathomdb.extensions;

import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.servlet.ServletModule.FilterKeyBindingBuilder;
import com.google.inject.servlet.ServletModule.ServletKeyBindingBuilder;

/**
 * Interface that allows extension of ServletModule.
 * 
 * Because those methods are protected, sadly each class needs a little
 * boilerplate to expose the inner methods.
 * 
 */
public interface HttpConfiguration {
	FilterKeyBindingBuilder filter(String urlPattern);

	ServletKeyBindingBuilder serve(String urlPattern);

	<T> AnnotatedBindingBuilder<T> bind(Class<T> clazz);
}
