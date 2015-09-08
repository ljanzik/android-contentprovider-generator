/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2012-2014 Benoit 'BoD' Lubek (BoD@JRAF.org)
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
package org.jraf.androidcontentprovidergenerator;

import com.thoughtsonmobile.android.cpgenerator.CPGeneratorPluginExtension;
import com.thoughtsonmobile.android.cpgenerator.FixedClassTemplateLoader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jraf.androidcontentprovidergenerator.model.Constraint;
import org.jraf.androidcontentprovidergenerator.model.Entity;
import org.jraf.androidcontentprovidergenerator.model.EnumValue;
import org.jraf.androidcontentprovidergenerator.model.Field;
import org.jraf.androidcontentprovidergenerator.model.Field.OnDeleteAction;
import org.jraf.androidcontentprovidergenerator.model.ForeignKey;
import org.jraf.androidcontentprovidergenerator.model.Model;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class Generator {
    public static final String TEMPLATE_FOLDER = "org/jraf/androidcontentprovidergenerator/";
    private static String TAG = Constants.TAG + Generator.class.getSimpleName();

    private Configuration mFreemarkerConfig;
    private final CPGeneratorPluginExtension mConfig;

    public Generator(CPGeneratorPluginExtension config) {
        mConfig = config;
    }

    private Configuration getFreeMarkerConfig() {
        if (mFreemarkerConfig == null) {
            mFreemarkerConfig = new Configuration();
            mFreemarkerConfig.setTemplateLoader(new FixedClassTemplateLoader(Generator.class, "/"));
            mFreemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
        }
        return mFreemarkerConfig;
    }

    private void loadModel(File inputDir) throws IOException, JSONException {
        File[] entityFiles = inputDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getName().startsWith("_") && pathname.getName().endsWith(".json");
            }
        });

        // Sort the entity files (lexicographically) so they are always processed in the same order
        Arrays.sort(entityFiles);

        for (File entityFile : entityFiles) {
            if (Config.LOGD) Log.d(TAG, entityFile.getCanonicalPath());
            String entityName = FilenameUtils.getBaseName(entityFile.getCanonicalPath());
            if (Config.LOGD) Log.d(TAG, "entityName=" + entityName);
            String fileContents = FileUtils.readFileToString(entityFile);
            JSONObject entityJson = new JSONObject(fileContents);

            // Documentation (optional)
            String entityDocumentation = entityJson.optString(Entity.Json.DOCUMENTATION);
            if (entityDocumentation.isEmpty()) entityDocumentation = null;

            Entity entity = new Entity(entityName, entityDocumentation);

            // Fields
            JSONArray fieldsJson = entityJson.getJSONArray(Entity.Json.FIELDS);
            int len = fieldsJson.length();
            for (int i = 0; i < len; i++) {
                JSONObject fieldJson = fieldsJson.getJSONObject(i);
                if (Config.LOGD) Log.d(TAG, "fieldJson=" + fieldJson);
                String name = fieldJson.getString(Field.Json.NAME);
                String fieldDocumentation = fieldJson.optString(Field.Json.DOCUMENTATION);
                if (fieldDocumentation.isEmpty()) fieldDocumentation = null;
                String type = fieldJson.getString(Field.Json.TYPE);
                boolean isIndex = fieldJson.optBoolean(Field.Json.INDEX, false);
                boolean isNullable = fieldJson.optBoolean(Field.Json.NULLABLE, true);
                String defaultValue = fieldJson.optString(Field.Json.DEFAULT_VALUE);
                String defaultValueLegacy = fieldJson.optString(Field.Json.DEFAULT_VALUE_LEGACY);
                String enumName = fieldJson.optString(Field.Json.ENUM_NAME);
                JSONArray enumValuesJson = fieldJson.optJSONArray(Field.Json.ENUM_VALUES);
                List<EnumValue> enumValues = new ArrayList<EnumValue>();
                if (enumValuesJson != null) {
                    int enumLen = enumValuesJson.length();
                    for (int j = 0; j < enumLen; j++) {
                        Object enumValue = enumValuesJson.get(j);
                        if (enumValue instanceof String) {
                            // Name only
                            enumValues.add(new EnumValue((String) enumValue, null));
                        } else {
                            // Name and documentation
                            JSONObject enumValueJson = (JSONObject) enumValue;
                            String enumValueName = (String) enumValueJson.keys().next();
                            String enumValueDocumentation = enumValueJson.getString(enumValueName);
                            enumValues.add(new EnumValue(enumValueName, enumValueDocumentation));
                        }
                    }
                }
                JSONObject foreignKeyJson = fieldJson.optJSONObject(Field.Json.FOREIGN_KEY);
                ForeignKey foreignKey = null;
                if (foreignKeyJson != null) {
                    String table = foreignKeyJson.getString(Field.Json.FOREIGN_KEY_TABLE);
                    OnDeleteAction onDeleteAction = OnDeleteAction.fromJsonName(foreignKeyJson.getString(Field.Json.FOREIGN_KEY_ON_DELETE_ACTION));
                    foreignKey = new ForeignKey(table, onDeleteAction);
                }
                Field field = new Field(entity, name, fieldDocumentation, type, false, isIndex, isNullable, false, defaultValue != null ? defaultValue
                        : defaultValueLegacy, enumName, enumValues, foreignKey);
                entity.addField(field);
            }

            // ID Field
            JSONArray idFields = entityJson.optJSONArray(Entity.Json.ID_FIELD);
            String idFieldName;
            if (idFields == null) {
                // Implicit id field
                idFieldName = "_id";
            } else {
                if (idFields.length() != 1) {
                    throw new IllegalArgumentException("Invalid number of idField '" + idFields + "' value in " + entityFile.getCanonicalPath() + ".");
                }
                idFieldName = idFields.getString(0);
            }
            Field idField;
            if ("_id".equals(idFieldName)) {
                // Implicit id field: create a Field named "_id"
                idField = new Field(entity, "_id", "Primary key.", "Long", true, false, false, true, null, null, null, null);
                entity.addField(0, idField);
            } else {
                // Explicit id field (reference)
                idField = entity.getFieldByName(idFieldName);
                if (idField == null) {
                    // Referenced field not found
                    throw new IllegalArgumentException("Invalid idField: '" + idFieldName + "' not found " + entityFile.getCanonicalPath() + ".");
                }
                if (idField.getType() != Field.Type.INTEGER && idField.getType() != Field.Type.LONG && idField.getType() != Field.Type.DATE
                        && idField.getType() != Field.Type.ENUM) {
                    // Invalid type
                    throw new IllegalArgumentException("Invalid idField type " + idField.getType() + " in " + entityFile.getCanonicalPath() + "."
                            + "  It must be Integer, Long, Date or Enum.");
                }
                if (idField.getIsNullable()) {
                    // Referenced field is nullable
                    throw new IllegalArgumentException("Invalid idField: '" + idFieldName + "' must not be nullable in " + entityFile.getCanonicalPath() + ".");
                }
                if (!idField.getIsIndex()) {
                    // Referenced field is not an index
                    throw new IllegalArgumentException("Invalid idField: '" + idFieldName + "' must be an index in " + entityFile.getCanonicalPath() + ".");
                }
                idField.setIsId(true);
            }

            // Constraints (optional)
            JSONArray constraintsJson = entityJson.optJSONArray(Entity.Json.CONSTRAINTS);
            if (constraintsJson != null) {
                len = constraintsJson.length();
                for (int i = 0; i < len; i++) {
                    JSONObject constraintJson = constraintsJson.getJSONObject(i);
                    if (Config.LOGD) Log.d(TAG, "constraintJson=" + constraintJson);
                    String name = constraintJson.getString(Constraint.Json.NAME);
                    String definition = constraintJson.getString(Constraint.Json.DEFINITION);
                    Constraint constraint = new Constraint(name, definition);
                    entity.addConstraint(constraint);
                }
            }

            Model.get().addEntity(entity);
        }
        // Header (optional)
        File headerFile = new File(inputDir, "header.txt");
        if (headerFile.exists()) {
            String header = FileUtils.readFileToString(headerFile).trim();
            Model.get().setHeader(header);
        }
        if (Config.LOGD) Log.d(TAG, Model.get().toString());
    }

//    private void validateConfig() {
//        // Ensure the input files are compatible with this version of the tool
//        int syntaxVersion;
//        try {
//            syntaxVersion = mConfig.getInt(Json.SYNTAX_VERSION);
//        } catch (JSONException e) {
//            try {
//                // For legacy reasons we also allow this attribute to be a String
//                syntaxVersion = Integer.parseInt(mConfig.getString(Json.SYNTAX_VERSION));
//            } catch (Exception e2) {
//                try {
//                    // For legacy reasons we also allow a different name for this attribute
//                    syntaxVersion = Integer.parseInt(mConfig.getString(Json.SYNTAX_VERSION_LEGACY));
//                } catch (Exception e3) {
//                    throw new IllegalArgumentException("Could not find '" + Json.SYNTAX_VERSION
//                            + "' field in _config.json, which is mandatory and must be equal to " + Constants.SYNTAX_VERSION + ".");
//                }
//            }
//        }
//        if (syntaxVersion != Constants.SYNTAX_VERSION) {
//            throw new IllegalArgumentException("Invalid '" + Json.SYNTAX_VERSION + "' value in _config.json: found '" + syntaxVersion + "' but expected '"
//                    + Constants.SYNTAX_VERSION + "'.");
//        }
//
//        // Ensure mandatory fields are present
//        ensureString(Json.PROJECT_PACKAGE_ID);
//        ensureString(Json.PROVIDER_JAVA_PACKAGE);
//        ensureString(Json.PROVIDER_CLASS_NAME);
//        ensureString(Json.SQLITE_OPEN_HELPER_CLASS_NAME);
//        ensureString(Json.SQLITE_OPEN_HELPER_CALLBACKS_CLASS_NAME);
//        ensureString(Json.AUTHORITY);
//        ensureString(Json.DATABASE_FILE_NAME);
//        ensureInt(Json.DATABASE_VERSION);
//        ensureBoolean(Json.ENABLE_FOREIGN_KEY);
//        ensureBoolean(Json.USE_ANNOTATIONS);
//    }
//
//    private void ensureString(String field) {
//        try {
//            mConfig.getString(field);
//        } catch (JSONException e) {
//            throw new IllegalArgumentException("Could not find '" + field + "' field in _config.json, which is mandatory and must be a string.");
//        }
//    }
//
//    private void ensureBoolean(String field) {
//        try {
//            mConfig.getBoolean(field);
//        } catch (JSONException e) {
//            throw new IllegalArgumentException("Could not find '" + field + "' field in _config.json, which is mandatory and must be a boolean.");
//        }
//    }
//
//    private void ensureInt(String field) {
//        try {
//            mConfig.getInt(field);
//        } catch (JSONException e) {
//            throw new IllegalArgumentException("Could not find '" + field + "' field in _config.json, which is mandatory and must be an int.");
//        }
//    }

    private void generateColumns() throws IOException, JSONException, TemplateException {
        Template template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "columns.ftl");
        System.out.println("Generating Columns");

        File providerDir = new File(mConfig.getOutputDir(), mConfig.providerJavaPackage().replace('.', '/'));
        System.out.println(providerDir.getAbsolutePath());
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", mConfig.getConfig());
        root.put("header", Model.get().getHeader());
        root.put("model", Model.get());

        // Entities
        for (Entity entity : Model.get().getEntities()) {
            File outputDir = new File(providerDir, entity.getPackageName());
            outputDir.mkdirs();
            File outputFile = new File(outputDir, entity.getNameCamelCase() + "Columns.java");
            Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

            root.put("entity", entity);

            template.process(root, out);
            IOUtils.closeQuietly(out);
        }
    }

    private void generateModels() throws IOException, JSONException, TemplateException {
        Template template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "model.ftl");

        File providerDir = new File(mConfig.getOutputDir(), mConfig.providerJavaPackage().replace('.', '/'));
        Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", mConfig.getConfig());
        root.put("header", Model.get().getHeader());
        root.put("model", Model.get());

        // Entities
        for (Entity entity : Model.get().getEntities()) {
            File outputDir = new File(providerDir, entity.getPackageName());
            outputDir.mkdirs();
            File outputFile = new File(outputDir, entity.getNameCamelCase() + "Model.java");
            Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

            root.put("entity", entity);

            template.process(root, out);
            IOUtils.closeQuietly(out);
        }
    }

    private void generateWrappers() throws IOException, JSONException, TemplateException {
        File providerDir = new File(mConfig.getOutputDir(), mConfig.providerJavaPackage().replace('.', '/'));
        File baseClassesDir = new File(providerDir, "base");
        baseClassesDir.mkdirs();

        Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", mConfig.getConfig());
        root.put("header", Model.get().getHeader());
        root.put("model", Model.get());

        // AbstractCursor
        Template template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "abstractcursor.ftl");
        File outputFile = new File(baseClassesDir, "AbstractCursor.java");
        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // AbstractContentValuesWrapper
        template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "abstractcontentvalues.ftl");
        outputFile = new File(baseClassesDir, "AbstractContentValues.java");
        out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // AbstractSelection
        template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "abstractselection.ftl");
        outputFile = new File(baseClassesDir, "AbstractSelection.java");
        out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // BaseContentProvider
        template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "basecontentprovider.ftl");
        outputFile = new File(baseClassesDir, "BaseContentProvider.java");
        out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // BaseModel
        template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "basemodel.ftl");
        outputFile = new File(baseClassesDir, "BaseModel.java");
        out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // Entities
        for (Entity entity : Model.get().getEntities()) {
            File entityDir = new File(providerDir, entity.getPackageName());
            entityDir.mkdirs();

            // Cursor wrapper
            outputFile = new File(entityDir, entity.getNameCamelCase() + "Cursor.java");
            out = new OutputStreamWriter(new FileOutputStream(outputFile));
            root.put("entity", entity);
            template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "cursor.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // ContentValues wrapper
            outputFile = new File(entityDir, entity.getNameCamelCase() + "ContentValues.java");
            out = new OutputStreamWriter(new FileOutputStream(outputFile));
            root.put("entity", entity);
            template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "contentvalues.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // Selection builder
            outputFile = new File(entityDir, entity.getNameCamelCase() + "Selection.java");
            out = new OutputStreamWriter(new FileOutputStream(outputFile));
            root.put("entity", entity);
            template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "selection.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // Enums (if any)
            for (Field field : entity.getFields()) {
                if (field.isEnum()) {
                    outputFile = new File(entityDir, field.getEnumName() + ".java");
                    out = new OutputStreamWriter(new FileOutputStream(outputFile));
                    root.put("entity", entity);
                    root.put("field", field);
                    template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "enum.ftl");
                    template.process(root, out);
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    private void generateContentProvider() throws IOException, JSONException, TemplateException {
        Template template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "contentprovider.ftl");
        File providerDir = new File(mConfig.getOutputDir(), mConfig.providerJavaPackage().replace('.', '/'));
        providerDir.mkdirs();
        File outputFile = new File(providerDir, mConfig.getProviderClassName() + ".java");
        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

        Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", mConfig.getConfig());
        root.put("model", Model.get());
        root.put("header", Model.get().getHeader());

        template.process(root, out);
    }

    private void generateSqliteOpenHelper() throws IOException, JSONException, TemplateException {
        Template template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "sqliteopenhelper.ftl");
        File providerDir = new File(mConfig.getOutputDir(), mConfig.providerJavaPackage().replace('.', '/'));
        providerDir.mkdirs();
        File outputFile = new File(providerDir, mConfig.getSqliteOpenHelperClassName() + ".java");
        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

        Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", mConfig.getConfig());
        root.put("model", Model.get());
        root.put("header", Model.get().getHeader());

        template.process(root, out);
    }

    private void generateSqliteOpenHelperCallbacks() throws IOException, JSONException, TemplateException {
        Template template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "sqliteopenhelpercallbacks.ftl");
        File providerDir = new File(mConfig.getOutputDir(), mConfig.providerJavaPackage().replace('.', '/'));
        providerDir.mkdirs();
        File outputFile = new File(providerDir, mConfig.getSqliteOpenHelperCallbacksClassName() + ".java");
        if (outputFile.exists()) {
            if (Config.LOGD) Log.d(TAG, "generateSqliteOpenHelperCallbacks Open helper callbacks class already exists: skip");
            return;
        }
        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

        Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", mConfig.getConfig());
        root.put("model", Model.get());
        root.put("header", Model.get().getHeader());

        template.process(root, out);
    }

    private void printManifest() throws IOException, JSONException, TemplateException {
        Template template = getFreeMarkerConfig().getTemplate(TEMPLATE_FOLDER + "manifest.ftl");

        Writer out = new OutputStreamWriter(System.out);

        Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", mConfig.getConfig());
        root.put("model", Model.get());
        root.put("header", Model.get().getHeader());

        Log.i(TAG, "\nProvider declaration to paste in the AndroidManifest.xml file: ");
        template.process(root, out);
    }

    public static void go(CPGeneratorPluginExtension config) throws IOException, JSONException, TemplateException {
        Generator generator = new Generator(config);

        System.out.println(generator.getFreeMarkerConfig().getTemplateLoader().getClass().getName());

        generator.loadModel(new File(config.getInputDir()));

        Model.get().flagAmbiguousFields();

          generator.generateColumns();
        generator.generateWrappers();
        generator.generateModels();
        generator.generateContentProvider();
        generator.generateSqliteOpenHelper();
        generator.generateSqliteOpenHelperCallbacks();

        generator.printManifest();
    }
}
