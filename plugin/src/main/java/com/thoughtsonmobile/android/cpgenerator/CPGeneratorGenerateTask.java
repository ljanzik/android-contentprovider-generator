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
