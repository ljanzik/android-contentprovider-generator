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

/**
 * Created by janzik on 03.09.15.
 */
public class CPGeneratorPluginExtension {
    public CPGeneratorPluginExtension() {
        mConfig = new Config();
    }

    public String getAuthority() {
        return mConfig.getAuthority();
    }

    public void authority(String authority) {
        mConfig.setAuthority(authority);
    }

    public String getDatabaseFileName() {
        return mConfig.getDatabaseFileName();
    }

    public void databaseFileName(String databaseFileName) {
        mConfig.getDatabaseFileName();
    }

    public int getDatabaseVersion() {
        return mConfig.getDatabaseVersion();
    }

    public void databaseVersion(int databaseVersion) {
        mConfig.setDatabaseVersion(databaseVersion);
    }

    public boolean isEnableForeignKeys() {
        return mConfig.isEnableForeignKeys();
    }

    public void enableForeignKeys(boolean enableForeignKeys) {
        mConfig.setEnableForeignKeys(enableForeignKeys);
    }

    public String getInputDir() {
        return mConfig.getInputDir();
    }

    public void inputDir(String inputDir) {
        mConfig.setInputDir(inputDir);
    }

    public String getOutputDir() {
        return mConfig.getOutputDir();
    }

    public void outputDir(String outputDir) {
        mConfig.setOutputDir(outputDir);
    }

    public String getPackageId() {
        return mConfig.getProjectPackageId();
    }

    public void packageId(String packageId) {
        mConfig.setProjectPackageId(packageId);
    }

    public String getProviderClassName() {
        return mConfig.getProviderClassName();
    }

    public void providerClassName(String providerClassName) {
        mConfig.setProviderClassName(providerClassName);
    }

    public String providerJavaPackage() {
        return mConfig.getProviderJavaPackage();
    }

    public void providerJavaPackage(String providerJavaPackage) {
        mConfig.setProviderJavaPackage(providerJavaPackage);
    }

    public String getSqliteOpenHelperCallbacksClassName() {
        return mConfig.getSqliteOpenHelperCallbacksClassName();
    }

    public void sqliteOpenHelperCallbacksClassName(String sqliteOpenHelperCallbacksClassName) {
        mConfig.setSqliteOpenHelperCallbacksClassName(sqliteOpenHelperCallbacksClassName);
    }

    public String getSqliteOpenHelperClassName() {
        return mConfig.getSqliteOpenHelperClassName();
    }

    public void sqliteOpenHelperClassName(String sqliteOpenHelperClassName) {
        mConfig.setSqliteOpenHelperClassName(sqliteOpenHelperClassName);
    }

    public boolean isUseAnnotations() {
        return mConfig.isUseAnnotations();
    }

    public void useAnnotations(boolean useAnnotations) {
        mConfig.setUseAnnotations(useAnnotations);
    }

    private final Config mConfig;


    public Config getConfig() {
        return mConfig;
    }
}
