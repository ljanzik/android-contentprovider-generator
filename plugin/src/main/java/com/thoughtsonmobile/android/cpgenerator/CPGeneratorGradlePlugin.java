package com.thoughtsonmobile.android.cpgenerator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;


/**
 * Created by janzik on 03.09.15.
 */
public class CPGeneratorGradlePlugin implements Plugin<Project> {

    @Override
    public void apply(final Project project) {
        project.getTasks().create("generateContentprovider", CPGeneratorGenerateTask.class);
        project.getExtensions().add("contentprovider", new CPGeneratorPluginExtension());
    }
}
