package com.thoughtsonmobile.android.contentprovider;

import com.thoughtsonmobile.android.contentprovider.model.Constraint;
import com.thoughtsonmobile.android.contentprovider.model.Entity;
import com.thoughtsonmobile.android.contentprovider.model.Field;
import com.thoughtsonmobile.android.contentprovider.model.Model;

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
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;


/**
 * Goal which generates ContentProvider and supporting classes
 *
 * @author  Leif Janzik (leif.janzik@gmail.com)
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES)
public class GenerateContentProviderMojo extends AbstractMojo {

    private final Configuration freemarkerConfig;

    private static final FileFilter FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File pathname) {
            return !pathname.getName().startsWith("_")
                    && pathname.getName().endsWith(".json");
        }
    };

    @Parameter(required = true)
    private String authority;

    @Parameter(required = true)
    private String providerClassName;

    @Parameter(required = true)
    private String databaseFileName;

    @Parameter(required = true)
    private boolean enableForeignKeys;

    @Parameter(required = true)
    private File inputDir;

    private final Log log = getLog();

    @Parameter(required = true)
    private String packageId;

    @Parameter(required = true)
    private String sqliteHelperClassName;


    @Parameter(required = true)
    private String targetPackage;

    @Parameter(required = true)
    private File outputDir;

    public void execute() throws MojoExecutionException {
        try {
            loadModel(inputDir);
            generateColumns();
            generateWrappers();
            generateContentProvider();
            generateSqliteHelper();
        } catch (IOException e) {
            throw new MojoExecutionException("IOException", e);
        } catch (JSONException e) {
            throw new MojoExecutionException("JSONException", e);
        } catch (TemplateException e) {
            throw new MojoExecutionException("TemplateException", e);
        }
    }

    private void generateSqliteHelper() throws IOException, JSONException,
            TemplateException {
        final Template template = freemarkerConfig.getTemplate("templates/sqlitehelper.ftl");
        final File providerDir = new File(outputDir,
                targetPackage.replace('.', '/'));
        providerDir.mkdirs();

        final File outputFile = new File(providerDir,
                sqliteHelperClassName + ".java");
        final Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("authority", authority);
        root.put("providerClassName", providerClassName);
        root.put("sqliteHelperClassName", sqliteHelperClassName);
        root.put("projectPackageId", packageId);
        root.put("providerJavaPackage", targetPackage);
        root.put("model", Model.get());
        root.put("databaseFileName", databaseFileName);
        root.put("enableForeignKeys", enableForeignKeys);
        root.put("header", Model.get().getHeader());
        root.put("model", Model.get());
        root.put("header", Model.get().getHeader());

        template.process(root, out);
    }

    private void generateContentProvider() throws IOException,
            JSONException, TemplateException {
        final Template template = freemarkerConfig.getTemplate("templates/contentprovider.ftl");
        final File providerDir = new File(outputDir,
                targetPackage.replace('.', '/'));
        providerDir.mkdirs();

        final File outputFile = new File(providerDir,
                providerClassName + ".java");
        final Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));

        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("authority", authority);
        root.put("providerClassName", providerClassName);
        root.put("sqliteHelperClassName", sqliteHelperClassName);
        root.put("projectPackageId", packageId);
        root.put("providerJavaPackage", targetPackage);
        root.put("model", Model.get());
        root.put("header", Model.get().getHeader());

        template.process(root, out);
    }

    private void generateWrappers() throws IOException, JSONException,
            TemplateException {
        final File providerDir = new File(outputDir,
                targetPackage.replace('.', '/'));
        final File baseClassesDir = new File(providerDir, "base");
        baseClassesDir.mkdirs();

        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("providerJavaPackage", targetPackage);
        root.put("header", Model.get().getHeader());

        // AbstractCursor
        Template template = freemarkerConfig.getTemplate("templates/abstractcursor.ftl");
        File outputFile = new File(baseClassesDir, "AbstractCursor.java");
        Writer out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // AbstractContentValuesWrapper
        template = freemarkerConfig.getTemplate("templates/abstractcontentvalues.ftl");
        outputFile = new File(baseClassesDir, "AbstractContentValues.java");
        out = new OutputStreamWriter(new FileOutputStream(outputFile));
        template.process(root, out);
        IOUtils.closeQuietly(out);

        // AbstractSelection
        template = freemarkerConfig.getTemplate("templates/abstractselection.ftl");
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
            template = freemarkerConfig.getTemplate("templates/cursor.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // ContentValues wrapper
            outputFile = new File(entityDir, entity.getNameCamelCase() + "ContentValues.java");
            out = new OutputStreamWriter(new FileOutputStream(outputFile));
            root.put("entity", entity);
            template = freemarkerConfig.getTemplate("templates/contentvalues.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // Selection builder
            outputFile = new File(entityDir, entity.getNameCamelCase() + "Selection.java");
            out = new OutputStreamWriter(new FileOutputStream(outputFile));
            root.put("entity", entity);
            template = freemarkerConfig.getTemplate("templates/selection.ftl");
            template.process(root, out);
            IOUtils.closeQuietly(out);

            // Enums (if any)
            for (final Field field:entity.getFields()) {

                if (field.isEnum()) {
                    outputFile = new File(entityDir, field.getEnumName() + ".java");
                    out = new OutputStreamWriter(new FileOutputStream(outputFile));
                    root.put("entity", entity);
                    root.put("field", field);
                    template = freemarkerConfig.getTemplate("templates/enum.ftl");
                    template.process(root, out);
                    IOUtils.closeQuietly(out);
                }
            }
        }
    }

    public GenerateContentProviderMojo() {
            freemarkerConfig = new Configuration();
            freemarkerConfig.setClassForTemplateLoading(getClass(), "/");
            freemarkerConfig.setObjectWrapper(new DefaultObjectWrapper());
    }

    private void generateColumns() throws IOException, JSONException,
            TemplateException {
        final Template template = freemarkerConfig.getTemplate("templates/columns.ftl");

        final File providerDir = new File(outputDir,
                targetPackage.replace('.', '/'));
        final Map<String, Object> root = new HashMap<String, Object>();
        root.put("config", inputDir);
        root.put("providerJavaPackage", targetPackage);
        root.put("providerClassName", providerClassName);
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

    private void loadModel(final File inputDir) throws IOException, JSONException {
        final File[] entityFiles = inputDir.listFiles(FILE_FILTER);

        for (final File entityFile:entityFiles) {
            log.debug(entityFile.getCanonicalPath());
            final String entityName = FilenameUtils.getBaseName(entityFile.getCanonicalPath());
            log.debug("entityName=" + entityName);

            final Entity entity = new Entity(entityName);
            final String fileContents = FileUtils.readFileToString(entityFile);
            final JSONObject entityJson = new JSONObject(fileContents);

            // Fields
            final JSONArray fieldsJson = entityJson.getJSONArray("fields");
            int len = fieldsJson.length();

            for (int i = 0; i < len; i++) {
                final JSONObject fieldJson = fieldsJson.getJSONObject(i);

                log.debug("fieldJson=" + fieldJson);

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

                    log.debug("constraintJson=" + constraintJson);

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

        log.debug(Model.get().toString());
    }
}
