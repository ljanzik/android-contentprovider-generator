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

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.jraf.androidcontentprovidergenerator.Generator;

import java.io.IOException;

import freemarker.template.TemplateException;

/**
 * Created by janzik on 03.09.15.
 */
public class CPGeneratorGenerateTask extends DefaultTask {


    @TaskAction
    public void generateContentprovider() {
        System.out.println("Hello World");


        try {
            Generator.go((CPGeneratorPluginExtension) getProject().getExtensions().findByName("contentprovider"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TemplateException e) {
            e.printStackTrace();
        }
    }

}
