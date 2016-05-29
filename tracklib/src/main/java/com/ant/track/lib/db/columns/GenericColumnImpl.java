package com.ant.track.lib.db.columns;

import android.support.annotation.NonNull;

import com.ant.track.lib.db.type.RowType;

/**
 * Class that describes the database columns.
 */
public class GenericColumnImpl implements GenericColumn {

    private boolean uniqueField = false;

    private boolean autoIncrement = false;

    private boolean isForeignKey = false;

    private boolean primaryKey = false;

    private String rowName;
    private RowType dataRowType;

    public GenericColumnImpl(boolean primaryKey, boolean autoIncrement, boolean uniqueField, boolean isForeignKey, String fieldName, @NonNull RowType type) {
        this.uniqueField = uniqueField;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
        this.dataRowType = type;
        this.rowName = fieldName;
        this.isForeignKey = isForeignKey;
    }

    public GenericColumnImpl(boolean primaryKey, boolean autoIncrement, String fieldName, @NonNull RowType type) {
        this(primaryKey, autoIncrement, false, false, fieldName, type);
    }

    public GenericColumnImpl(boolean primaryKey, String rowName, @NonNull RowType type) {
        this(primaryKey, false, false, false, rowName, type);
    }

    public GenericColumnImpl(String rowName, @NonNull RowType type) {
        this(false, false, false, false, rowName, type);
    }


    public GenericColumnImpl(boolean primaryKey, @NonNull RowType type) {
        this(primaryKey, false, false, false, null, type);
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
        return isForeignKey;
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
