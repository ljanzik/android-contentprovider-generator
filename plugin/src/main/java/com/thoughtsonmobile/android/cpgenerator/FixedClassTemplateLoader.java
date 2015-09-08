/*
 *
 * Copyright (C) 2015 Leif Janzik (leif.janzik@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
