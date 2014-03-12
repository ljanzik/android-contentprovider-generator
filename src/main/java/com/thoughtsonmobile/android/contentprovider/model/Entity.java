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
package com.thoughtsonmobile.android.contentprovider.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.WordUtils;


public class Entity {
    private final List<Constraint> mConstraints = new ArrayList<Constraint>();
    private final List<Field> mFields = new ArrayList<Field>();
    private final String mName;

    public Entity(final String name) {
        mName = name.toLowerCase();
    }

    public void addConstraint(final Constraint constraint) {
        mConstraints.add(constraint);
    }

    public void addField(final Field field) {
        mFields.add(field);
    }

    public List<Constraint> getConstraints() {
        return Collections.unmodifiableList(mConstraints);
    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(mFields);
    }

    public String getNameCamelCase() {
        return WordUtils.capitalizeFully(mName, new char[] { '_' }).replaceAll("_", "");
    }

    public String getNameLowerCase() {
        return mName;
    }

    public String getNameUpperCase() {
        return mName.toUpperCase();
    }

    @Override
    public String toString() {
        return "Entity [mName=" + mName + ", mFields=" + mFields + ", mConstraints=" + mConstraints
            + "]";
    }
}
