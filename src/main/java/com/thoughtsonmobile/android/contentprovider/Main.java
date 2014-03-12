/*
 * This source is part of the     _____  ___   ____ __ / / _ \/ _ | / __/___  _______ _ /
 * // / , _/ __ |/ _/_/ _ \/ __/ _ `/ \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *            /___/ repository.
 *
 * Copyright (C) 2012-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
 *
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thoughtsonmobile.android.contentprovider;

import com.beust.jcommander.JCommander;
import com.thoughtsonmobile.android.contentprovider.model.Constraint;
import com.thoughtsonmobile.android.contentprovider.model.Entity;
import com.thoughtsonmobile.android.contentprovider.model.Field;
import com.thoughtsonmobile.android.contentprovider.model.Model;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class Main {
    private static String TAG = Constants.TAG + Main.class.getSimpleName();

    private static String FILE_CONFIG = "_config.json";
    private JSONObject mConfig;

    private Configuration mFreemarkerConfig;

    public static void main(final String[] args) throws Exception {
        new Main().go(args);
    }

    private void ensureBoolean(final String field) {

        try {
            mConfig.getBoolean(field);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not find '" + field
                + "' field in _config.json, which is mandatory and must be a boolean.");
        }
    }

    private void ensureString(final String field) {

        try {
            mConfig.getString(field);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Could not find '" + field
                + "' field in _config.json, which is mandatory and must be a string.");
        }
    }

    private void generateColumns(final Arguments arguments) throws IOException, JSONException,
        TemplateException {
        final Template template = getFreeMarkerConfig().getTemplate("columns.ftl");
        final JSONObject config = getConfig(arguments.inputDir);
        final String providerJavaPackage = config.getString(Json.PROVIDER_JAVA_PACKAGE);

        final File providerDir = new File(arguments.outputDir,
                providerJavaPackage.replace('.', '/'));
        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", getConfig(arguments.inputDir));
        root.put("header", Model.get().getHeader());

        // Entities
        for (final Entity entity:Model.get().getEntities()) {
            final File outputDir = new File(providerDir, entity.getNameLowerCase());
            outputDir.mkdirs();

            final File outputFile = new File(outputDir, entity.getNameCamelCase() + "Columns.java");
            final Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

            root.put("entity", entity);

            template.process(root, out);
            IOUtils.closeQuietly(out);
        }
    }

    private void generateContentProvider(final Arguments arguments) throws IOException,
        JSONException, TemplateException {
        final Template template = getFreeMarkerConfig().getTemplate("contentprovider.ftl");
        final JSONObject config = getConfig(arguments.inputDir);
        final String providerJavaPackage = config.getString(Json.PROVIDER_JAVA_PACKAGE);
        final File providerDir = new File(arguments.outputDir,
                providerJavaPackage.replace('.', '/'));
        providerDir.mkdirs();

        final File outputFile = new File(providerDir,
                config.getString(Json.PROVIDER_CLASS_NAME) + ".java");
        final Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", config);
        root.put("model", Model.get());
        root.put("header", Model.get().getHeader());

        template.process(root, out);
    }

    private void generateSqliteHelper(final Arguments arguments) throws IOException, JSONException,
        TemplateException {
        final Template template = getFreeMarkerConfig().getTemplate("sqlitehelper.ftl");
        final JSONObject config = getConfig(arguments.inputDir);
        final String providerJavaPackage = config.getString(Json.PROVIDER_JAVA_PACKAGE);
        final File providerDir = new File(arguments.outputDir,
                providerJavaPackage.replace('.', '/'));
        providerDir.mkdirs();

        final File outputFile = new File(providerDir,
                config.getString(Json.SQLITE_HELPER_CLASS_NAME) + ".java");
        final Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", config);
        root.put("model", Model.get());
        root.put("header", Model.get().getHeader());

        template.process(root, out);
    }

    private void generateWrappers(final Arguments arguments) throws IOException, JSONException,
        TemplateException {
        final JSONObject config = getConfig(arguments.inputDir);
        final String providerJavaPackage = config.getString(Json.PROVIDER_JAVA_PACKAGE);
        final File providerDir = new File(arguments.outputDir,
                providerJavaPackage.replace('.', '/'));
        final File baseClassesDir = new File(providerDir, "base");
        baseClassesDir.mkdirs();

        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", getConfig(arguments.inputDir));
        root.put("header", Model.get().getHeader());

        // AbstractCursor
        Template template = getFreeMarkerConfig().getTemplate("abstractcursor.ftl");
        File outputFile = new File(baseClassesDir, "AbstractCursor.java");
        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // AbstractContentValuesWrapper
        template = getFreeMarkerConfig().getTemplate("abstractcontentvalues.ftl");
        outputFile = new File(baseClassesDir, "AbstractContentValues.java");
        out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // AbstractSelection
        template = getFreeMarkerConfig().getTemplate("abstractselection.ftl");
        outputFile = new File(baseClassesDir, "AbstractSelection.java");
        out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // Entities
        for (final Entity entity:Model.get().getEntities()) {
            final File entityDir = new File(providerDir, entity.getNameLowerCase());
            entityDir.mkdirs();

            // Cursor wrapper
            outputFile = new File(entityDir, entity.getNameCamelCase() + "Cursor.java");
            out = new OutputStreamWriter(new FileOutputStream(outputFile));
            root.put("entity", entity);
            template = getFreeMarkerConfig().getTemplate("cursor.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // ContentValues wrapper
            outputFile = new File(entityDir, entity.getNameCamelCase() + "ContentValues.java");
            out = new OutputStreamWriter(new FileOutputStream(outputFile));
            root.put("entity", entity);
            template = getFreeMarkerConfig().getTemplate("contentvalues.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // Selection builder
            outputFile = new File(entityDir, entity.getNameCamelCase() + "Selection.java");
            out = new OutputStreamWriter(new FileOutputStream(outputFile));
            root.put("entity", entity);
            template = getFreeMarkerConfig().getTemplate("selection.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // Enums (if any)
            for (final Field field:entity.getFields()) {

                if (field.isEnum()) {
                    outputFile = new File(entityDir, field.getEnumName() + ".java");
                    out = new OutputStreamWriter(new FileOutputStream(outputFile));
                    root.put("entity", entity);
                    root.put("field", field);
                    template = getFreeMarkerConfig().getTemplate("enum.ftl");
                    template.process(root, out);
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    private JSONObject getConfig(final File inputDir) throws IOException, JSONException {

        if (mConfig == null) {
            final File configFile = new File(inputDir, FILE_CONFIG);
            final String fileContents = FileUtils.readFileToString(configFile);
            mConfig = new JSONObject(fileContents);
        }

        validateConfig();

        return mConfig;
    }

    private Configuration getFreeMarkerConfig() {

        if (mFreemarkerConfig == null) {
            mFreemarkerConfig = new Configuration();
            mFreemarkerConfig.setClassForTemplateLoading(getClass(), "");
            mFreemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
        }

        return mFreemarkerConfig;
    }

    private void go(final String[] args) throws IOException, JSONException, TemplateException {
        final Arguments arguments = new Arguments();
        final JCommander jCommander = new JCommander(arguments, args);
        jCommander.setProgramName("GenerateAndroidProvider");

        if (arguments.help) {
            jCommander.usage();

            return;
        }

        getConfig(arguments.inputDir);

        loadModel(arguments.inputDir);
        generateColumns(arguments);
        generateWrappers(arguments);
        generateContentProvider(arguments);
        generateSqliteHelper(arguments);
    }

    private void loadModel(final File inputDir) throws IOException, JSONException {
        final File[] entityFiles = inputDir.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(final File pathname) {
                        return !pathname.getName().startsWith("_")
                            && pathname.getName().endsWith(".json");
                    }
                });

        for (final File entityFile:entityFiles) {

            if (Config.LOGD)
                Log.d(TAG, entityFile.getCanonicalPath());

            final String entityName = FilenameUtils.getBaseName(entityFile.getCanonicalPath());

            if (Config.LOGD)
                Log.d(TAG, "entityName=" + entityName);

            final Entity entity = new Entity(entityName);
            final String fileContents = FileUtils.readFileToString(entityFile);
            final JSONObject entityJson = new JSONObject(fileContents);

            // Fields
            final JSONArray fieldsJson = entityJson.getJSONArray("fields");
            int len = fieldsJson.length();

            for (int i = 0; i < len; i++) {
                final JSONObject fieldJson = fieldsJson.getJSONObject(i);

                if (Config.LOGD)
                    Log.d(TAG, "fieldJson=" + fieldJson);

                final String name = fieldJson.getString(Field.Json.NAME);
                final String type = fieldJson.getString(Field.Json.TYPE);
                final boolean isIndex = fieldJson.optBoolean(Field.Json.INDEX, false);
                final boolean isNullable = fieldJson.optBoolean(Field.Json.NULLABLE, true);
                final String defaultValue = fieldJson.optString(Field.Json.DEFAULT_VALUE);
                final String enumName = fieldJson.optString(Field.Json.ENUM_NAME);
                final JSONArray enumValuesJson = fieldJson.optJSONArray(Field.Json.ENUM_VALUES);
                final List<String> enumValues = new ArrayList<String>();

                if (enumValuesJson != null) {
                    final int enumLen = enumValuesJson.length();

                    for (int j = 0; j < enumLen; j++) {
                        final String valueName = enumValuesJson.getString(j);
                        enumValues.add(valueName);
                    }
                }

                final Field field = new Field(name, type, isIndex, isNullable, defaultValue,
                        enumName, enumValues);
                entity.addField(field);
            }

            // Constraints (optional)
            final JSONArray constraintsJson = entityJson.optJSONArray("constraints");

            if (constraintsJson != null) {
                len = constraintsJson.length();

                for (int i = 0; i < len; i++) {
                    final JSONObject constraintJson = constraintsJson.getJSONObject(i);

                    if (Config.LOGD)
                        Log.d(TAG, "constraintJson=" + constraintJson);

                    final String name = constraintJson.getString(Constraint.Json.NAME);
                    final String definition = constraintJson.getString(Constraint.Json.DEFINITION);
                    final Constraint constraint = new Constraint(name, definition);
                    entity.addConstraint(constraint);
                }
            }

            Model.get().addEntity(entity);
        }

        // Header (optional)
        final File headerFile = new File(inputDir, "header.txt");

        if (headerFile.exists()) {
            final String header = FileUtils.readFileToString(headerFile).trim();
            Model.get().setHeader(header);
        }

        if (Config.LOGD)
            Log.d(TAG, Model.get().toString());
    }

    private void validateConfig() {

        // Ensure the input files are compatible with this version of the tool
        final String configVersion;

        try {
            configVersion = mConfig.getString(Json.TOOL_VERSION);
        } catch (JSONException e) {
            throw new IllegalArgumentException(
                "Could not find 'toolVersion' field in _config.json, which is mandatory and must be equals to '"
                + Constants.VERSION + "'.");
        }

        if (!configVersion.equals(Constants.VERSION)) {
            throw new IllegalArgumentException(
                "Invalid 'toolVersion' value in _config.json: found '" + configVersion
                + "' but expected '" + Constants.VERSION
                + "'.");
        }

        // Ensure mandatory fields are present
        ensureString(Json.PROJECT_PACKAGE_ID);
        ensureString(Json.PROVIDER_JAVA_PACKAGE);
        ensureString(Json.PROVIDER_CLASS_NAME);
        ensureString(Json.SQLITE_HELPER_CLASS_NAME);
        ensureString(Json.AUTHORITY);
        ensureString(Json.DATABASE_FILE_NAME);
        ensureBoolean(Json.ENABLE_FOREIGN_KEY);
    }

    public static class Json {
        public static final String TOOL_VERSION = "toolVersion";
        public static final String PROJECT_PACKAGE_ID = "projectPackageId";
        public static final String PROVIDER_JAVA_PACKAGE = "providerJavaPackage";
        public static final String PROVIDER_CLASS_NAME = "providerClassName";
        public static final String SQLITE_HELPER_CLASS_NAME = "sqliteHelperClassName";
        public static final String AUTHORITY = "authority";
        public static final String DATABASE_FILE_NAME = "databaseFileName";
        public static final String ENABLE_FOREIGN_KEY = "enableForeignKeys";
    }
}
