package com.thoughtsonmobile.android.cpgenerator;

/**
 * Created by janzik on 08.09.15.
 */
public class Config {

    private String projectPackageId = null; //"com.demo"
    private String authority = "com.example.app.provider";
    private String providerJavaPackage = "com.example.app.provider";
    private String providerClassName = "ExampleProvider";
    private String sqliteOpenHelperClassName = "ExampleSQLiteOpenHelper";
    private String sqliteOpenHelperCallbacksClassName = "ExampleSQLiteOpenHelperCallbacks";
    private String databaseFileName = "example.db";
    private int databaseVersion = 1;
    private boolean enableForeignKeys = true;
    private boolean useAnnotations = true;

    private String inputDir = "src/persistence";
    private String outputDir = null;


    public String getProjectPackageId() {
        return projectPackageId;
    }

    public void setProjectPackageId(String packageId) {
        this.projectPackageId = packageId;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getProviderJavaPackage() {
        return providerJavaPackage;
    }

    public void setProviderJavaPackage(String providerJavaPackage) {
        this.providerJavaPackage = providerJavaPackage;
    }

    public String getProviderClassName() {
        return providerClassName;
    }

    public void setProviderClassName(String providerClassName) {
        this.providerClassName = providerClassName;
    }

    public String getSqliteOpenHelperClassName() {
        return sqliteOpenHelperClassName;
    }

    public void setSqliteOpenHelperClassName(String sqliteOpenHelperClassName) {
        this.sqliteOpenHelperClassName = sqliteOpenHelperClassName;
    }

    public String getSqliteOpenHelperCallbacksClassName() {
        return sqliteOpenHelperCallbacksClassName;
    }

    public void setSqliteOpenHelperCallbacksClassName(String sqliteOpenHelperCallbacksClassName) {
        this.sqliteOpenHelperCallbacksClassName = sqliteOpenHelperCallbacksClassName;
    }

    public String getDatabaseFileName() {
        return databaseFileName;
    }

    public void setDatabaseFileName(String databaseFileName) {
        this.databaseFileName = databaseFileName;
    }

    public int getDatabaseVersion() {
        return databaseVersion;
    }

    public void setDatabaseVersion(int databaseVersion) {
        this.databaseVersion = databaseVersion;
    }

    public boolean isEnableForeignKeys() {
        return enableForeignKeys;
    }

    public void setEnableForeignKeys(boolean enableForeignKeys) {
        this.enableForeignKeys = enableForeignKeys;
    }

    public boolean isUseAnnotations() {
        return useAnnotations;
    }

    public void setUseAnnotations(boolean useAnnotations) {
        this.useAnnotations = useAnnotations;
    }

    public String getInputDir() {
        return inputDir;
    }

    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }
}
