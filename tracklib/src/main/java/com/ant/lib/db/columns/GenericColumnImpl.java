package com.ant.lib.db.columns;

import android.support.annotation.NonNull;

import com.ant.lib.db.type.RowType;

/**
 * Class that describes the database columns.
 */
public class GenericColumnImpl implements GenericColumn {

    private boolean uniqueField = false;

    private boolean primaryKey = false;

    private String rowName;
    private RowType dataRowType;

    public GenericColumnImpl(boolean primaryKey, boolean uniqueField, String fieldName, @NonNull RowType type) {
        this.uniqueField = uniqueField;
        this.primaryKey = primaryKey;
        this.dataRowType = type;
        this.rowName = fieldName;
    }

    public GenericColumnImpl(boolean primaryKey, String rowName, @NonNull RowType type) {
        this(primaryKey, false, rowName, type);
    }

    public GenericColumnImpl(String rowName, @NonNull RowType type) {
        this(false, false, rowName, type);
    }


    public GenericColumnImpl(boolean primaryKey, @NonNull RowType type) {
        this(primaryKey, false, null, type);
    }

    @Override
    public boolean isPrimaryKey() {
        return primaryKey;
    }

    @Override
    public String getFieldName() {
        return rowName;
    }

    @Override
    public boolean isUnique() {
        return uniqueField;
    }

    @Override
    public RowType getDataType() {
        return dataRowType;
    }


    @Override
    public String toString() {
        return this.rowName;
    }
}
