package com.thoughtsonmobile.android.cpgenerator;

import java.net.URL;

import freemarker.cache.ClassTemplateLoader;

/**
 * Created by janzik on 04.09.15.
 */
public class FixedClassTemplateLoader extends ClassTemplateLoader {

    private ClassLoader classLoader;
    public FixedClassTemplateLoader(Class clazz, String string) {
        super(clazz, string);
        classLoader = clazz.getClassLoader();
    }
    @Override
    protected URL getURL(String name) {
        // this now loads the template file from the jar
        return classLoader.getResource(name);
    }
}
