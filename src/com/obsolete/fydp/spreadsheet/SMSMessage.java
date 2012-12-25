package com.obsolete.fydp.spreadsheet;

/**
 *
 * @author bnkengsa
 */
public class SMSMessage {

    private String sheetName;
    private int seqNumber;
    private int smsNumber;
    private int numSMS;
    private int row;
    private int col;
    private String data;

    public SMSMessage(String name, int seqNum, int smsNum, int numSMS, int row, int col, String data) {
        this.sheetName = name;
        this.seqNumber = seqNum;
        this.smsNumber = smsNum;
        this.numSMS = numSMS;
        this.row = row;
        this.col = col;
        this.data = data;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getNumSMS() {
        return numSMS;
    }

    public void setNumSMS(int numSMS) {
        this.numSMS = numSMS;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getSeqNumber() {
        return seqNumber;
    }

    public void setSeqNumber(int seqNumber) {
        this.seqNumber = seqNumber;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getSmsNumber() {
        return smsNumber;
    }

    public void setSmsNumber(int smsNumber) {
        this.smsNumber = smsNumber;
    }

    public String toString() {
        return this.sheetName+":"+this.seqNumber+":"+this.smsNumber+"/"+
                this.numSMS+":"+this.row+","+this.col+":"+this.data;
    }

}
