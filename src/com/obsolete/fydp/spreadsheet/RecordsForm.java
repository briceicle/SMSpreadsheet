package com.obsolete.fydp.spreadsheet;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;

public class RecordsForm extends Form
  implements CommandListener
{
  private static final Command CMD_DELETE = new Command("Delete", 8, 1);
  private static final Command CMD_CANCEL = new Command("Cancel", 3, 1);

  private SpreadSheet parent;
  private Display display;
  private List recordsList;
  private RecordStore rs;
  private int records = 0;
  private int selectedIndex;
  private String selectedRecord;

  public RecordsForm(Display paramDisplay, SpreadSheet sheet)
  {
    super("Select a record");
    this.parent = sheet;
    this.display = paramDisplay;
    try
    {
      this.rs = RecordStore.openRecordStore("SpreadSheet", true);
      this.records = this.rs.getNumRecords();
      System.out.println("Number of records: " + this.records);
      if (this.records > 0)
      {
        String[] arrayOfString = new String[this.records];
        for (int i = 1; i <= this.records; i++)
        {
          byte[] arrayOfByte = this.rs.getRecord(i);
          String str = new String(arrayOfByte);
          arrayOfString[(i - 1)] = str;
        }
        
        this.recordsList = new List("Select a record", 3, arrayOfString, null);
        this.recordsList.addCommand(CMD_DELETE);
        this.recordsList.addCommand(CMD_CANCEL);
        this.recordsList.setCommandListener(this);
      }
      this.rs.closeRecordStore();
    }
    catch (RecordStoreNotOpenException localRecordStoreNotOpenException)
    {
    }
    catch (InvalidRecordIDException localInvalidRecordIDException)
    {
    }
    catch (RecordStoreException localRecordStoreException)
    {
    }
    catch (NullPointerException localNullPointerException)
    {
    }

  }

  public void commandAction(Command paramCommand, Displayable paramDisplayable)
  {
    int i;
    String str1;
    if (paramCommand == CMD_CANCEL) {
        this.display.setCurrent(this.parent.getMainMenu());
    }else if (paramCommand == CMD_DELETE) {
      {
        i = ((List)paramDisplayable).getSelectedIndex();
        if (i < 0)
            return;
        str1 = ((List)paramDisplayable).getString(i);
        if (str1 == null)
            return;

        this.selectedIndex = i;
        this.selectedRecord = str1;

        boolean isOwner = false;
        String ownerRecord = str1 + "/" + "Owner";
        try {
          RecordStore rs1 = RecordStore.openRecordStore(ownerRecord, true);
          for (int j = 1; j <= rs1.getNumRecords(); j++) {
            byte[] arrayOfByte = rs1.getRecord(j);
            String str = new String(arrayOfByte);
            if (str.equals("1"))
              isOwner  = true;
            else
              isOwner = false;
          }
          rs1.closeRecordStore();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (isOwner) {
         DeleteSheetForm deleteForm = new DeleteSheetForm(this.display, this.parent, this);
         this.display.setCurrent(deleteForm.getList());
        } else {
            // Non Owner - delete record
            this.deleteRecord();

            // send message to be removed from all shared contacts' contact lists
            String[] contacts = this.parent.getContactList(this.selectedRecord);
            if (contacts != null) {
                for (int j = 0; j < contacts.length; j++) {
                    String phoneNo = contacts[j];
                    SMSHeader msg = new SMSHeader(this.selectedRecord, "",0, 1, 1, -4, -4, "");
                    SendSMS sms = new SendSMS(this.parent, phoneNo, msg.toString(), this.parent.getPort());
                    sms.start();
                }
            }
        }

      }
    } else if (paramDisplayable.equals(this.recordsList))
      {
        i = ((List)paramDisplayable).getSelectedIndex();
        str1 = ((List)paramDisplayable).getString(i);
        this.parent.openSheet(str1);
      }
    
  }


  public int getNumRecords(){
      return this.records;
  }

  public List getRecordList(){
      return this.recordsList;
  }

  public void setDisplay(Display display){
      this.display = display;
  }

  public String getSelectedRecord() {
      return this.selectedRecord;
  }

  public void deleteRecord() {
      try {
        RecordStore.deleteRecordStore(this.selectedRecord);
        this.recordsList.delete(this.selectedIndex);

        this.rs = RecordStore.openRecordStore("SpreadSheet", true);
        String[] arrayOfString = new String[1];
        this.records = this.rs.getNumRecords();

        if (this.records > 0)
        {
          arrayOfString = new String[this.records];
          for (int j = 1; j <= this.records; j++)
          {
            if (j - 1 == this.selectedIndex)
              continue;
            byte[] arrayOfByte = this.rs.getRecord(j);
            String str2 = new String(arrayOfByte);
            arrayOfString[(j - 1)] = str2;
          }
        }
        this.rs.closeRecordStore();
        RecordStore.deleteRecordStore("SpreadSheet");
        this.rs = RecordStore.openRecordStore("SpreadSheet", true);
        for (int j = 0; j < arrayOfString.length; j++)
        {
          if (arrayOfString[j] == null)
            continue;
          String str2 = arrayOfString[j];
          byte[] arrayOfByte = str2.getBytes();
          this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
        }
        this.rs.closeRecordStore();
      }
        catch (Exception e) {
            e.printStackTrace();
        }

  }

}