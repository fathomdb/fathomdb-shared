package com.fathomdb.extensions;

import java.util.List;

import com.fathomdb.Configuration;
import com.google.inject.Module;

public interface ExtensionModule {
	void addEntities(List<Class<?>> entities);

	void addHttpExtensions(HttpConfiguration webConfiguration);

	Module getOverrideModule();

	List<Module> getExtraModules(Configuration configuration);
}
