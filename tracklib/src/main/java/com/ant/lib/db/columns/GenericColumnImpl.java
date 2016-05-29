package com.ant.lib.db.columns;

import android.support.annotation.NonNull;

import com.ant.lib.db.type.RowType;

/**
 * Class that describes the database columns.
 */
public class GenericColumnImpl implements GenericColumn {

    private boolean uniqueField = false;

    private boolean autoIncrement = false;

    private boolean primaryKey = false;

    private String rowName;
    private RowType dataRowType;

    public GenericColumnImpl(boolean primaryKey, boolean autoIncrement, boolean uniqueField, String fieldName, @NonNull RowType type) {
        this.uniqueField = uniqueField;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
        this.dataRowType = type;
        this.rowName = fieldName;
    }

    public GenericColumnImpl(boolean primaryKey, boolean autoIncrement, String fieldName, @NonNull RowType type) {
        this(primaryKey, autoIncrement, false, fieldName, type);
    }

    public GenericColumnImpl(boolean primaryKey, String rowName, @NonNull RowType type) {
        this(primaryKey, false, false, rowName, type);
    }

    public GenericColumnImpl(String rowName, @NonNull RowType type) {
        this(false, false, false, rowName, type);
    }


    public GenericColumnImpl(boolean primaryKey, @NonNull RowType type) {
        this(primaryKey, false, false, null, type);
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
    public boolean isForeignKey() {
        return false;
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
    public boolean isAutoIncremented() {
        return autoIncrement;
    }

    @Override
    public String toString() {
        return this.rowName;
    }
}
