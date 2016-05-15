package com.ant.lib.db.columns;

import com.ant.lib.db.type.RowType;

/**
 * Created by toaderandrei on 15/05/16.
 */
public interface GenericColumn {
    /**
     * checks if the column is the primary key.
     *
     * @return true if it is.
     */
    boolean isPrimaryKey();

    /**
     * gets the field name.
     */
    String getFieldName();

    /**
     * checks if the column is unique.
     *
     * @return true if it is, false otherwise.
     */
    boolean isUnique();

    /**
     * retrieves the current row type.
     */
    RowType getDataType();
}
