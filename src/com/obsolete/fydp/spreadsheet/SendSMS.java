package com.obsolete.fydp.spreadsheet;

/**
 *
 * @author bnkengsa
 */
import java.io.IOException;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

public class SendSMS implements Runnable {

    private String phone,text,port;
    private SpreadSheet sheet;

    public SendSMS(SpreadSheet sheet, String tfPhoneNo, String tfText, String port) {
        phone = tfPhoneNo;
        text = tfText;
        this.port = port;
        this.sheet = sheet;

    }

    public void start() {
        Thread t = new Thread(this);
        t.start();
    }

    public void run() {
        // construct sms URI with the phone number
        String addr = "sms://" + this.phone + ":" + this.port;
        try {

          //open message connection with Connector class
          MessageConnection conn = sheet.getConnection();

          //construct TextMessage object
          TextMessage msg = (TextMessage)conn.newMessage(MessageConnection.TEXT_MESSAGE);

          msg.setAddress(addr);

          //set the message
          msg.setPayloadText(text);

          //sends the message
          conn.send(msg);
          System.out.println(addr + " " +text);


      //IllegalArgumentException happens when illegal phone number entered
      } catch (IllegalArgumentException e) {
          //do something
          e.printStackTrace();

      //catch IException
      } catch (IOException e) {
          e.printStackTrace();

      // catch remaining exceptions
      } catch (Exception e) {
          e.printStackTrace();
      }

    }
}
