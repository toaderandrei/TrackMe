package com.ant.track.lib.db.type;

/**
 * Enum for all sqlite data types.
 */
public enum SqliteDataType implements RowType {

    Boolean("BOOLEAN"),
    Integer("INTEGER"),
    Double("REAL"),
    String("TEXT"),
    Float("FLOAT");

    private String dataType;

    private SqliteDataType(String dataType) {
        this.dataType = dataType;
    }


    @Override
    public String getRowType() {
        return dataType;
    }

}
