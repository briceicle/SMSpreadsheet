package com.obsolete.fydp.spreadsheet;

import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.PushRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;
import javax.microedition.lcdui.List;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.MessageListener;
import javax.wireless.messaging.TextMessage;

public class SpreadSheet extends MIDlet
  implements CommandListener, ItemStateListener, MessageListener
{

  private Display display;
  private SpreadSheetCanvas canvas;
  private Table table;
  private RecordsForm recordsForm;
  private String port;
  private MessageConnection smsconn;
  private String[] connections;
  private boolean done;
  private MessageReader reader;
  private String[] options = {"Load Sheet", "New Sheet", "About", "Exit"};
  private List mainMenu;

  public SpreadSheet(){
      this.display = Display.getDisplay(this);
      this.canvas = new SpreadSheetCanvas();
      this.mainMenu = new List("Menu", 3, options, null);
      this.port = getAppProperty ("SMS-Port");
  }

  protected void startApp()
  {

    this.display.setCurrent(this.mainMenu);
    this.mainMenu.setCommandListener(this);

     // SMS connection to be read.
     String smsConnection = "sms://:" + port;

     // Open the message connection.
     if (smsconn == null) {
         try {
             smsconn = (MessageConnection) Connector.open (smsConnection);
             smsconn.setMessageListener (this);
         }
          catch (IOException ioe) {
              ioe.printStackTrace ();
          }
      }

      // Initialize the text if we were started manually.
      connections = PushRegistry.listConnections (true);

      done = false;
      reader = new MessageReader();
      new Thread(reader).start();
  
  }

  public void commandAction(Command paramCommand, Displayable paramDisplayable)
  {
     if (paramDisplayable == this.mainMenu) {
        int index = this.mainMenu.getSelectedIndex();
        if (index == 0) {
            this.createRecordForm();
        } else if (index == 1) {
            this.createSheet();
        } else if (index == 2) {
            aboutScreen();

        } else if (index == 3) {
            destroyApp(false);
            notifyDestroyed();
        }
    }
  }

  public void itemStateChanged(Item paramItem) {
  }

  protected void destroyApp(boolean paramBoolean) {
      done = true;

      if (smsconn != null) {
          try {
              smsconn.setMessageListener(null);
              smsconn.close ();
          }
          catch (IOException e) {
              // Ignore any errors on shutdown
          }
      }
  }

  protected void pauseApp() {
      done = true;
      try{
          smsconn.close();
      }catch(Exception e){
          e.printStackTrace();
      }
  }

  public void notifyIncomingMessage(MessageConnection conn) {
      System.out.println("Got new message");
      if(conn == smsconn){
          reader.handleMessage();
      }else {
          smsconn = conn;
          reader.handleMessage();
      }
  }

  private void createRecordForm() {
      this.recordsForm = new RecordsForm(this.display, this);
      if (this.recordsForm.getNumRecords() > 0) {
        this.display.setCurrent(this.recordsForm.getRecordList());
        this.recordsForm.getRecordList().setCommandListener(this.recordsForm);
      } else {
          Alert alert = new Alert("Alert");
          alert.setString("No Records");
          this.display.setCurrent(alert, this.mainMenu);
      }
  }

  private void aboutScreen() {
      AboutForm aboutForm = new AboutForm(this.display, this);
      this.display.setCurrent(aboutForm);
  }

  private void createSheet() {
      this.table = new Table("Untitled SpreadSheet", Display.getDisplay(this), this, this.canvas.getWidth(), this.canvas.getHeight());
      this.table.setIsOwner(true);
      this.table.setPermission("W");
  }

  public void openSheet(String record) {
      this.table = new Table(record, Display.getDisplay(this), this, this.canvas.getWidth(), this.canvas.getHeight());
      this.table.openRecordStore(record);
  }

  public String getPort() {
      return this.port;
  }

  public MessageConnection getConnection() {
      return this.smsconn;
  }

  public List getMainMenu() {
      return this.mainMenu;
  }

  public void reset() {
      this.table = null;
  }

  private RecordStore getRecord(String sheetName) throws Exception{
      RecordStore store= null;
      try {
            store = RecordStore.openRecordStore(sheetName, false);
        }catch (Exception e) {
            RecordStore rs = RecordStore.openRecordStore("SpreadSheet", true);
            byte[] arr = sheetName.getBytes();
            rs.addRecord(arr, 0, arr.length);
            rs.closeRecordStore();

            store = RecordStore.openRecordStore(sheetName, true);
        }
        return store;
  }

  private void addDataToRecord(String sheetName, int row, int col, String data) throws Exception{
    RecordStore store = getRecord(sheetName);
    byte[] arrayOfBytes = null;
    String str = "" + row + "," + col + "," + data;
    arrayOfBytes = str.getBytes();
    store.addRecord(arrayOfBytes, 0, arrayOfBytes.length);
    store.closeRecordStore();
  }

  private void addContactToRecord(String sheetName, String phoneNo) throws Exception{
    String sharedContacts = sheetName + "/" + "Contacts";
    RecordStore store = RecordStore.openRecordStore(sharedContacts, true);
    byte[] arrayOfByte = phoneNo.getBytes();
    store.addRecord(arrayOfByte, 0, arrayOfByte.length);
    if (store.getNumRecords() == 1)
        addContactPermissionToRecord(sheetName, "W");
    store.closeRecordStore();
  }

  private void addContactPermissionToRecord(String sheetName, String permission) throws Exception{
      String permissions = sheetName + "/" + "Permissions";
      RecordStore store = RecordStore.openRecordStore(permissions, true);
      byte[] arrayOfByte = permission.getBytes();
      store.addRecord(arrayOfByte, 0, arrayOfByte.length);
      store.closeRecordStore();
  }

  private void addMyPermissionToRecord(String sheetName, String permission) throws Exception {
      String myPermission = sheetName + "/" + "MyPermission";
      RecordStore store = RecordStore.openRecordStore(myPermission, true);
      byte[] arrayOfByte = permission.getBytes();
      store.addRecord(arrayOfByte, 0, arrayOfByte.length);
      store.closeRecordStore();
  }

  private void deleteContactFromRecord(String sheetName, String phoneNo) throws Exception{
      Vector contacts = new Vector();
      Vector perm = new Vector();
      String sharedContacts = sheetName + "/" + "Contacts";
      String permissions = sheetName + "/" + "Permissions";

      RecordStore store = RecordStore.openRecordStore(sharedContacts, true);
      RecordStore rs = RecordStore.openRecordStore(permissions, true);

      for (int i = 1; i <= store.getNumRecords(); i++) {
          byte[] arrayOfByte = store.getRecord(i);
          String str = new String(arrayOfByte);

          byte[] arrayOfBytes = rs.getRecord(i);
          String str1 = new String(arrayOfBytes);
          if(!str.equals(phoneNo)) {
            contacts.addElement(str);
            perm.addElement(str1);
          }
      }
      store.closeRecordStore();
      rs.closeRecordStore();

      RecordStore.deleteRecordStore(sharedContacts);
      RecordStore.deleteRecordStore(permissions);

      for (int i = 0; i < contacts.size(); i++) {
          addContactToRecord(sheetName, (String)contacts.elementAt(i));
      }

      for (int i = 0; i < perm.size(); i++) {
          addContactPermissionToRecord(sheetName, (String)perm.elementAt(i));
      }
  }

  private void setIsOwnerRecord(String sheetName, String isOwner) throws Exception{
      String isOwnerRecord  = sheetName + "/" + "Owner";
      RecordStore store = RecordStore.openRecordStore(isOwnerRecord, true);
      byte[] arrayOfByte = isOwner.getBytes();
      store.addRecord(arrayOfByte, 0, arrayOfByte.length);
      store.closeRecordStore();
  }

  public String[] getContactList(String sheetName) {
        String[] list = null;

        try{
          String sharedContacts = sheetName + "/" + "Contacts";
          RecordStore rs = RecordStore.openRecordStore(sharedContacts, false);
          list = new String[rs.getNumRecords()];
          for (int i = 1; i <= rs.getNumRecords(); i++) {
            byte[] arrayOfByte = rs.getRecord(i);
            String str = new String(arrayOfByte);
            list[i-1] = str;
          }
          rs.closeRecordStore();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return list;
  }

  private void deleteRecord(String sheetName) {
      try {
        RecordStore.deleteRecordStore(sheetName);
        RecordStore.deleteRecordStore(sheetName + "/" + "Contacts");
        RecordStore.deleteRecordStore(sheetName + "/" + "Owner");
        RecordStore.deleteRecordStore(sheetName + "/" + "Permissions");
        RecordStore.deleteRecordStore(sheetName + "/" + "MyPermission");


        RecordStore rs = RecordStore.openRecordStore("SpreadSheet", true);
        String[] arrayOfString = new String[1];
        int numRecords = rs.getNumRecords();

        if (numRecords > 0)
        {
          arrayOfString = new String[numRecords];
          for (int j = 1; j <= numRecords; j++)
          {
            byte[] arrayOfByte = rs.getRecord(j);
            String str2 = new String(arrayOfByte);
            if (str2.equals(sheetName))
                continue;
            arrayOfString[(j - 1)] = str2;
          }
        }
        rs.closeRecordStore();

        RecordStore.deleteRecordStore("SpreadSheet");
        rs = RecordStore.openRecordStore("SpreadSheet", true);
        for (int j = 0; j < arrayOfString.length; j++)
        {
          if (arrayOfString[j] == null)
            continue;
          String str2 = arrayOfString[j];
          byte[] arrayOfByte = str2.getBytes();
          rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
        }
        rs.closeRecordStore();
      } catch (Exception e) {
        e.printStackTrace();
      }
  }

  class MessageReader implements Runnable {
        private int pendingMessages = 0;

        // The run method performs the actual message reading.
        public void run() {
            while (!done) {
                synchronized(this) {
                    if (pendingMessages == 0) {
                        try {
                            wait();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    pendingMessages--;

                    try {
                        Message msg = smsconn.receive();

                        if(msg instanceof TextMessage) {
                            TextMessage tmsg = (TextMessage) msg;
                            System.out.println("Received Message: " + tmsg.getPayloadText());
                            String text = tmsg.getPayloadText();

                            String addr = tmsg.getAddress();
                            int index1 = addr.indexOf("sms://") + 6;
                            int index2 = addr.indexOf(port) - 1;
                            String sender = addr.substring(index1, index2);

                            SMSHeader sms = MessageParser.parse(text);

                            //Special Case - Set new Owner
                            if (sms.getRow() == -1 && sms.getCol() == -1)
                            {
                                if (table != null && table.getSheetName() != null && table.getSheetName().equals(sms.getSheetName())) {
                                    table.setIsOwner(true);
                                    table.deleteContact(sender);
                                    table.save();
                                    continue;
                                } else {
                                    setIsOwnerRecord(sms.getSheetName(), "1");
                                    deleteContactFromRecord(sms.getSheetName(), sender);

                                }
                            }
                            //Special Case - Delete Spreadsheet (command sent from spreadsheet owner)
                            else if (sms.getRow() == -2 && sms.getCol() == -2) {
                                if (table != null && table.getSheetName() != null && table.getSheetName().equals(sms.getSheetName())) {
                                      Alert alert = new Alert("Alert");
                                      alert.setString("Spreadsheet has been deleted by owner.");
                                      alert.setTimeout(2000);
                                      display.setCurrent(alert, mainMenu);

                                      deleteRecord(sms.getSheetName());
                                } else {
                                    deleteRecord(sms.getSheetName());
                                }
                            }
                            //Special Case - Add new contact
                            else if (sms.getRow() == -3 && sms.getCol() == -3) {
                                if (table != null && table.getSheetName() != null && table.getSheetName().equals(sms.getSheetName())) {
                                    table.AddContact(sms.getData());
                                    table.mapContact(sms.getData(), sms.getPermission());
                                    table.save();
                                } else {
                                    addContactToRecord(sms.getSheetName(), sms.getData());
                                    addContactPermissionToRecord(sms.getSheetName(), sms.getPermission());
                                }
                            }
                            //Special Case - Delete contact (command sent from a shared contact who has deleted the spreadsheet)
                            else if (sms.getRow() == -4 && sms.getCol() == -4) {
                                if (table != null && table.getSheetName() != null && table.getSheetName().equals(sms.getSheetName())) {
                                    table.deleteContact(sender);
                                    table.save();
                                } else {
                                    deleteContactFromRecord(sms.getSheetName(), sender);
                                }
                            }
                            else {
                                if(table != null && table.getSheetName() != null && table.getSheetName().equals(sms.getSheetName())) {
                                    table.updateCell(sms.getRow(), sms.getCol(), sms.getData(), tmsg.getTimestamp());
                                    table.AddContact(sender);
                                    table.setPermission(sms.getPermission());
                                    table.save();
                                } else {
                                    addDataToRecord(sms.getSheetName(), sms.getRow(), sms.getCol(), sms.getData());

                                    addContactToRecord(sms.getSheetName(), sender);

                                    addMyPermissionToRecord(sms.getSheetName(), sms.getPermission());
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public synchronized void handleMessage() {
            pendingMessages++;
            notify();
        }
    }
}