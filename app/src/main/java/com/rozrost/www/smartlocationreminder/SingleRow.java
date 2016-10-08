package com.rozrost.www.smartlocationreminder;

/**
 * Created by Yateen Kedare on 5/13/2016.
 */
public class SingleRow {
    String name;
    Boolean status;
    String PrimaryKey;
    String time;
    SingleRow(String Name, Boolean Status, String primaryKey, String ti){
        this.name = Name;
        this.status = Status;
        this.PrimaryKey = primaryKey;
        this.time = ti;
    }
}
