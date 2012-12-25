package com.obsolete.fydp.spreadsheet;

/**
 *
 * @author bnkengsa
 */
public class MessageParser {

    public static SMSHeader parse(String msg) {
        int index1 = 0;
        int index2 = msg.indexOf(":");
        String sheetName = msg.substring(index1, index2);

        index1 = index2 + 1;
        index2 = msg.indexOf(":", index1);
        String permission = msg.substring(index1, index2);

        index1 = index2 + 1;
        index2 = msg.indexOf(":", index1);
        int seqNum = Integer.parseInt(msg.substring(index1, index2));

        index1 = index2 + 1;
        index2 = msg.indexOf("/", index1);
        int smsNum = Integer.parseInt(msg.substring(index1, index2));

        index1 = index2 + 1;
        index2 = msg.indexOf(":", index1);
        int numSMS = Integer.parseInt(msg.substring(index1, index2));

        index1 = index2 + 1;
        index2 = msg.indexOf(",", index1);
        int row = Integer.parseInt(msg.substring(index1, index2));

        index1 = index2 + 1;
        index2 = msg.indexOf(":", index1);
        int col = Integer.parseInt(msg.substring(index1, index2));

        String data = msg.substring(index2 + 1);

        return new SMSHeader(sheetName, permission, seqNum, smsNum, numSMS, row, col, data);
    }

}
