package com.obsolete.fydp.spreadsheet;

/**
 *
 * @author bnkengsa
 */
public class SMSHeader {

    private String sheetName;
    private String permission;
    private int seqNumber;
    private int smsNumber;
    private int numSMS;
    private int row;
    private int col;
    private String data;
    private final String ACK = "ACK";


    public SMSHeader(String name, String perm, int seqNum, int smsNum, int numSMS, int row, int col, String data) {
        this.sheetName = name;
        this.permission = perm;
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

    public String getPermission() {
        return this.permission;
    }

    public void setPermission(String perm) {
        this.permission = perm;
    }

    public String toString() {
        return this.sheetName+":"+this.permission+":"+this.seqNumber+":"+this.smsNumber+"/"+
                this.numSMS+":"+this.row+","+this.col+":"+this.data;
    }

    public String appendAck(int row, int col) {
        String msg = this.toString();
        String ack = ":" + this.ACK + ":" + row + "," + col;
        if (msg.length() <= 160 - ack.length())
            return msg + ack;
        else
            return null;
    }

}
