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
     * checks if the column has auto increment or not. This condition is together
     * with teh primary key condition.
     * @return true if auto - increment is set to true, false otherwise.
     *
     */
    boolean isAutoIncremented();

    /**
     * gets the field name.
     */
    String getFieldName();

    /**
     * returns true if it is foreign key false, otherwise
     * @return true if it is a foreign key;
     */
    boolean isForeignKey();

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
