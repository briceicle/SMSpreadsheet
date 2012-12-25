package com.obsolete.fydp.spreadsheet;

/**
 *
 * @author bnkengsa
 */
import java.util.Date;
import java.util.Vector;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotOpenException;
import java.util.Hashtable;

public class Table extends CustomItem
  implements ItemCommandListener
{
  private static final Command CMD_EDIT = new Command("Edit", 8, 1);
  private static final Command CMD_SAVE = new Command("Share & Save", 8, 1);
  private static final Command CMD_DELETE = new Command("Delete", 8, 1);
  private static final Command CMD_COPY = new Command("Copy", 8, 1);
  private static final Command CMD_PASTE = new Command("Paste", 8, 1);
  private static final Command CMD_CUT = new Command("Cut", 8, 1);
  private static final Command CMD_GOTO = new Command("Goto", 8, 1);
  private static final Command CMD_FIND = new Command("Find", 8, 1);
  private static final Command CMD_ADD = new Command("Add Contact", 8, 1);
  private static final Command CMD_EXIT = new Command("Exit", 8, 1);
  private Display display;
  private Form form;
  private int currentRow;
  private int currentColumn;
  private int leftColumn = 0;
  private int rightColumn = 0;
  private int topRow = 0;
  private int bottomRow = 0;
  private int rows = 0;
  private int columns = 0;
  private Cell[][] cells = new Cell[20][20];
  private Date lastSaved;
  private int unitHeight = 0;
  private int unitWidth = 0;
  private int borderWidth = 0;
  private int borderHeight = 0;
  private int tableWidth = 0;
  private int tableHeight = 0;
  private int width = 0;
  private int height = 0;
  private Font font;
  private SpreadSheet parent;
  private boolean traversalState = false;
  private int textBoxHeight = 20;
  private int padding = 3;
  private RecordStore rs;
  private String copyData = "";
  private String recordSetName = null;
  private boolean isOwner = false;
  private String permission;
  private boolean contactAdded = false;

  private Vector contacts  = new Vector();
  private Hashtable map = new Hashtable();

  public Table(String paramString) {
      super(paramString);
      this.recordSetName = paramString;
  }
  public Table(String paramString, Display paramDisplay, SpreadSheet paramSpreadSheet, int paramInt1, int paramInt2)
  {
    super("");
    this.display = paramDisplay;
    addCommand(CMD_EDIT);
    addCommand(CMD_CUT);
    addCommand(CMD_COPY);
    addCommand(CMD_PASTE);
    addCommand(CMD_DELETE);
    addCommand(CMD_GOTO);
    addCommand(CMD_FIND);
    addCommand(CMD_SAVE);
    addCommand(CMD_ADD);
    addCommand(CMD_EXIT);
    setDefaultCommand(CMD_EDIT);

    setItemCommandListener(this);

    this.form = new Form(paramString);
    this.display.setCurrent(this.form);
    this.form.append(this);
    this.form.setItemStateListener(this.parent);

    this.parent = paramSpreadSheet;
    this.width = paramInt1;
    this.height = paramInt2;
    this.borderHeight = 16;
    this.borderWidth = 17;
    this.tableHeight = (paramInt2 - this.borderHeight - this.textBoxHeight - this.padding - 15);
    this.tableWidth = (paramInt1 - this.borderWidth);
    long time = System.currentTimeMillis();
    this.lastSaved = new Date(time);
    for (int i = 0; i < 20; i++) {
      for (int j = 0; j < 20; j++) {
        this.cells[i][j] = new Cell(i, j, new Date(time), this);
      }
    }
    this.unitHeight = 30;
    this.unitWidth = 100;
    this.rows = (this.tableHeight / this.unitHeight);
    this.columns = (this.tableWidth / this.unitWidth);
    this.topRow = 0;
    this.leftColumn = 0;
    this.bottomRow = (this.rows - 1);
    this.rightColumn = (this.columns - 1);
    this.currentRow = 1;
    this.currentColumn = 1;
    this.permission = "R";
  }

  protected int getMinContentHeight()
  {
    return this.height - 10;
  }

  protected int getMinContentWidth()
  {
    return this.width;
  }

  protected int getPrefContentHeight(int paramInt)
  {
    return this.height - 10;
  }

  protected int getPrefContentWidth(int paramInt)
  {
    return this.width;
  }

  public void paint(Graphics paramGraphics, int paramInt1, int paramInt2)
  {
    this.font = Font.getFont(0, 0, 8);
    paramGraphics.setFont(this.font);
    paramGraphics.drawRect(0, 0, this.width - 4, 20);
    for (int i = 0; i < this.rows; i++)
    {
      paramGraphics.setColor(150, 150, 150);
      paramGraphics.fillRect(0, this.unitHeight * i + this.borderHeight + this.padding + this.textBoxHeight, this.borderWidth, this.unitHeight * (i + 1));
      paramGraphics.setColor(0, 0, 0);
      paramGraphics.drawString("" + (this.topRow + i), 0, this.unitHeight * i + this.borderHeight + this.padding + this.textBoxHeight, 20);
    }
    for (int i = 0; i < this.columns; i++)
    {
      paramGraphics.setColor(150, 150, 150);
      paramGraphics.fillRect(this.unitWidth * i + this.borderWidth, this.padding + this.textBoxHeight, this.unitWidth * (i + 1), this.borderHeight);
      paramGraphics.setColor(0, 0, 0);
      paramGraphics.drawString("" + (this.leftColumn + i), this.unitWidth * i + this.borderWidth, this.padding + this.textBoxHeight, 20);
    }
    paramGraphics.setColor(150, 150, 150);
    paramGraphics.fillRect(0, this.padding + this.textBoxHeight, this.borderWidth, this.borderHeight);
    paramGraphics.setColor(200, 200, 200);
    for (int i = 0; i < this.rows; i++)
      for (int j = 0; j < this.columns; j++)
        paramGraphics.drawRect(j * this.unitWidth + this.borderWidth, i * this.unitHeight + this.borderHeight + this.padding + this.textBoxHeight, this.unitWidth, this.unitHeight);
    paramGraphics.setColor(81, 223, 253);
    paramGraphics.fillRect((this.currentColumn - this.leftColumn) * this.unitWidth + this.borderWidth + 1, (this.currentRow - this.topRow) * this.unitHeight + this.borderHeight + this.padding + this.textBoxHeight + 1, this.unitWidth - 1, this.unitHeight - 1);
    paramGraphics.setColor(177, 79, 46);
    paramGraphics.fillRect(0, (this.currentRow - this.topRow) * this.unitHeight + this.borderHeight + this.padding + this.textBoxHeight, this.borderWidth, this.unitHeight);
    paramGraphics.fillRect((this.currentColumn - this.leftColumn) * this.unitWidth + this.borderWidth, this.padding + this.textBoxHeight, this.unitWidth, this.borderHeight);
    paramGraphics.setColor(0, 0, 0);
    paramGraphics.drawString("" + this.currentRow, 0, (this.currentRow - this.topRow) * this.unitHeight + this.borderHeight + this.padding + this.textBoxHeight, 20);
    paramGraphics.drawString("" + this.currentColumn, (this.currentColumn - this.leftColumn) * this.unitWidth + this.borderWidth, this.padding + this.textBoxHeight, 20);
    paramGraphics.setColor(0, 0, 0);
    int i = this.topRow;
    for (int j = 0; i <= this.bottomRow; j++)
    {
      int k = this.leftColumn;
      for (int m = 0; k <= this.rightColumn; m++)
      {
        //String str = this.cells[i][k].getData();
        String str = this.cells[i][k].getDisplayData();
        if (str.length() > 9)
          paramGraphics.drawString(str.substring(0, 6) + "...", m * this.unitWidth + this.borderWidth + 1, j * this.unitHeight + this.borderHeight + this.padding + this.textBoxHeight, 20);
        else
          paramGraphics.drawString(str, m * this.unitWidth + this.borderWidth + 1, j * this.unitHeight + this.borderHeight + this.padding + this.textBoxHeight, 20);
        if ((i == this.currentRow) && (k == this.currentColumn))
        {
          int n = 0;
          int i1;
          for (i1 = 0; i1 < str.length(); i1++)
          {
            n += this.font.charWidth(str.charAt(i1));
            if (n > this.width - 15)
              break;
          }
          if (n > this.tableWidth)
            paramGraphics.drawString(str.substring(0, i1 - 3) + "...", 2, 2, 20);
          else
            paramGraphics.drawString(str, 2, 2, 20);
        }
        k++;
      }
      i++;
    }
  }

  public boolean traverse(int paramInt1, int paramInt2, int paramInt3, int[] paramArrayOfInt)
  {
    if (!this.traversalState)
    {
      this.traversalState = true;
      return true;
    }
    int i = paramInt1;
    if (i == 1)
    {
      if (this.currentRow > 0)
        this.currentRow -= 1;
      if (this.currentRow < this.topRow)
      {
        this.topRow = this.currentRow;
        this.bottomRow = (this.topRow + (this.rows - 1));
      }
      repaint();
    }
    else if (i == 6)
    {
      if (this.currentRow < 19)
        this.currentRow += 1;
      if (this.bottomRow < this.currentRow)
      {
        this.bottomRow = this.currentRow;
        this.topRow = (this.bottomRow - (this.rows - 1));
      }
      repaint();
    }
    else if (i == 2)
    {
      if (this.currentColumn > 0)
        this.currentColumn -= 1;
      if (this.currentColumn < this.leftColumn)
      {
        this.leftColumn = this.currentColumn;
        this.rightColumn = (this.leftColumn + (this.columns - 1));
      }
      repaint();
    }
    else if (i == 5)
    {
      if (this.currentColumn < 19)
        this.currentColumn += 1;
      if (this.rightColumn < this.currentColumn)
      {
        this.rightColumn = this.currentColumn;
        this.leftColumn = (this.rightColumn - (this.columns - 1));
      }
      repaint();
    }
    else
    {
      return true;
    }
    return true;
  }

  public void editCell(String paramString)
  {
    this.cells[this.currentRow][this.currentColumn].setData(paramString);
    this.cells[this.currentRow][this.currentColumn].setModified(true);
    repaint();
    this.traversalState = false;
    this.cells[this.currentRow][this.currentColumn].setLastModified(new Date(System.currentTimeMillis()));
  }

  public void updateCell(int row, int col, String text, Date timeStamp){
     if (this.cells[row][col].getModified()) {
      Vector options = new Vector();
      options.addElement(this.cells[row][col].getData());
      options.addElement(text);
      Alert alert = new Alert("Alert");
      alert.setString("Conflict at cell " + row + "," + col);
      ConflictHandlerForm conflictForm = new ConflictHandlerForm(this, this.display, options);
      this.display.setCurrent(alert, conflictForm.getList());
     }
     this.cells[row][col].setData(text);
     repaint();
     this.traversalState = false;
  }

  public String getCell(int row, int col) {
    return this.cells[row][col].getData();
  }

  public Cell getCellObject(int row, int col) {
    return this.cells[row][col];
  }

  public void openRecordStore(String paramString)
  {
    this.recordSetName = paramString;
    try
    {
      this.rs = RecordStore.openRecordStore(this.recordSetName, true);
      for (int j = 1; j <= this.rs.getNumRecords(); j++)
      {
        byte[] arrayOfByte = this.rs.getRecord(j);
        String str1 = new String(arrayOfByte);
        int k = str1.indexOf(',');
        int m = str1.indexOf(',', k + 1);
        String str2 = str1.substring(0, k);
        String str3 = str1.substring(k + 1, m);
        int row = Integer.parseInt(str2);
        int col = Integer.parseInt(str3);
        String str4 = str1.substring(m + 1, str1.length());
        this.cells[row][col].setData(str4);
      }
      this.rs.closeRecordStore();

      String sharedContacts = this.recordSetName + "/" + "Contacts";
      this.rs = RecordStore.openRecordStore(sharedContacts, true);
      for (int i = 1; i <= this.rs.getNumRecords(); i++) {
          byte[] arrayOfByte = this.rs.getRecord(i);
          String str = new String(arrayOfByte);
          this.AddContact(str);
      }
      this.rs.closeRecordStore();

      String permissions = this.recordSetName + "/" + "Permissions";
      this.rs = RecordStore.openRecordStore(permissions, true);
      for (int i = 1; i <= this.rs.getNumRecords(); i++) {
          byte[] arrayOfByte = this.rs.getRecord(i);
          String perm = new String(arrayOfByte);
          String phoneNo = (String)this.contacts.elementAt(i-1);
          this.mapContact(phoneNo, perm);

      }
      this.rs.closeRecordStore();

      String myPermission = this.recordSetName + "/" + "MyPermission";
      this.rs = RecordStore.openRecordStore(myPermission, true);
      for (int i = 1; i <= this.rs.getNumRecords(); i++) {
          byte[] arrayOfByte = this.rs.getRecord(i);
          this.permission = new String(arrayOfByte);
      }
      this.rs.closeRecordStore();

      String ownerRecord = this.recordSetName + "/" + "Owner";
      this.rs = RecordStore.openRecordStore(ownerRecord, true);
      for (int i = 1; i <= this.rs.getNumRecords(); i++) {
          byte[] arrayOfByte = this.rs.getRecord(i);
          String str = new String(arrayOfByte);
          if (str.equals("1"))
              this.isOwner  = true;
          else
              this.isOwner = false;
      }
      this.rs.closeRecordStore();

    }
    catch (RecordStoreNotOpenException e) {
        e.printStackTrace();
    }
    catch (InvalidRecordIDException e) {
        e.printStackTrace();
    }
    catch (RecordStoreException e) {
        e.printStackTrace();
    }
    catch (NullPointerException e) {
        e.printStackTrace();
    }
  }

  public void saveAs(String paramString)
  {
    this.recordSetName = paramString;
    try
    {
      this.rs = RecordStore.openRecordStore(this.recordSetName, true);
      for (int i = 0; i < 20; i++)
        for (int j = 0; j < 20; j++)
        {
          this.cells[i][j].setModified(false);
          String str = this.cells[i][j].getData();

          this.sendUpdate(paramString, i, j, this.cells[i][j].getData());

          byte[] arrayOfByte = null;
          arrayOfByte = str.getBytes();

          if (arrayOfByte.length == 0)
            continue;
          str = "" + i + "," + j + "," + str;
          arrayOfByte = str.getBytes();
          this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
        }
      this.rs.closeRecordStore();

      String sharedContacts = this.recordSetName + "/" + "Contacts";
      this.rs = RecordStore.openRecordStore(sharedContacts, true);
      for (int i = 0; i < this.contacts.size(); i++) {
          String str = (String)this.contacts.elementAt(i);
          byte[] arrayOfByte = str.getBytes();
          if (arrayOfByte.length == 0)
              continue;
          this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
      }
      this.rs.closeRecordStore();

      String permissions = this.recordSetName + "/" + "Permissions";
      this.rs = RecordStore.openRecordStore(permissions, true);
      for (int i = 0; i < this.contacts.size(); i++) {
          String phoneNo = (String)this.contacts.elementAt(i);
          String str = (String)this.map.get(phoneNo);
          byte[] arrayOfByte = str.getBytes();
          if (arrayOfByte.length == 0)
              continue;
          this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
      }
      this.rs.closeRecordStore();

      String myPermission = this.recordSetName + "/" + "MyPermission";
      this.rs = RecordStore.openRecordStore(myPermission, true);
      byte[] bytes = this.permission.getBytes();
      this.rs.addRecord(bytes, 0, bytes.length);
      this.rs.closeRecordStore();


      String ownerRecord = this.recordSetName + "/" + "Owner";
      this.rs = RecordStore.openRecordStore(ownerRecord, true);
      String str = "0";
      if (this.IsOwner())
          str = "1";
      byte[] arrayOfBytes = str.getBytes();
      this.rs.addRecord(arrayOfBytes, 0, arrayOfBytes.length);
      this.rs.closeRecordStore();


      this.rs = RecordStore.openRecordStore("SpreadSheet", true);
      byte[] arrayOfByte = this.recordSetName.getBytes();
      this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
      this.rs.closeRecordStore();
      
      this.form.setTitle(paramString);

      this.contactAdded = false;

      // make sure each all shared contacts know about each other.
      this.informContactsAboutEachOther(paramString);
    }
    catch (RecordStoreNotOpenException e) {
        e.printStackTrace();
    }
    catch (InvalidRecordIDException e) {
        e.printStackTrace();
    }
    catch (RecordStoreException e) {
        e.printStackTrace();
    }
  }

  public void save() {
      try {
          RecordStore.deleteRecordStore(this.recordSetName);
      } catch (Exception e) {
          //Do Nothing
      }
      try {
          this.rs = RecordStore.openRecordStore(this.recordSetName, true);
          for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
              this.cells[i][j].setModified(false);
              String str = this.cells[i][j].getData();
              this.sendUpdate(this.recordSetName, i, j, this.cells[i][j].getData());
              byte[] localObject = str.getBytes();
              if (str.length() == 0)
                continue;
              str = "" + i + "," + j + "," + str;
              localObject = str.getBytes();
              this.rs.addRecord(localObject, 0, str.length());
            }
          }
          this.rs.closeRecordStore();

          String sharedContacts = this.recordSetName + "/" + "Contacts";
          RecordStore.deleteRecordStore(sharedContacts);
          this.rs = RecordStore.openRecordStore(sharedContacts, true);
          for (int i = 0; i < this.contacts.size(); i++) {
            String str = (String)this.contacts.elementAt(i);
            byte[] arrayOfByte = str.getBytes();
            if (arrayOfByte.length == 0)
              continue;
            this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
          }
          this.rs.closeRecordStore();

          String permissions = this.recordSetName + "/" + "Permissions";
          RecordStore.deleteRecordStore(permissions);
          this.rs = RecordStore.openRecordStore(permissions, true);
          for (int i = 0; i < this.contacts.size(); i++) {
              String phoneNo = (String)this.contacts.elementAt(i);
              String str = (String)this.map.get(phoneNo);
              byte[] arrayOfByte = str.getBytes();
              if (arrayOfByte.length == 0)
                  continue;
              this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
          }
          this.rs.closeRecordStore();

          String myPermission = this.recordSetName + "/" + "MyPermission";
          RecordStore.deleteRecordStore(myPermission);
          this.rs = RecordStore.openRecordStore(myPermission, true);
          byte[] bytes = this.permission.getBytes();
          this.rs.addRecord(bytes, 0, bytes.length);
          this.rs.closeRecordStore();

          String ownerRecord = this.recordSetName + "/" + "Owner";
          RecordStore.deleteRecordStore(ownerRecord);
          this.rs = RecordStore.openRecordStore(ownerRecord, true);
          String str = "0";
          if (this.IsOwner())
            str = "1";
          byte[] arrayOfBytes = str.getBytes();
          this.rs.addRecord(arrayOfBytes, 0, arrayOfBytes.length);
          this.rs.closeRecordStore();

          this.lastSaved = new Date(System.currentTimeMillis());

      } catch (Exception e) {
          e.printStackTrace();
      }
  }

  public void findText(String paramString)
  {
    if (paramString.equals("No match found"))
      return;
    int i = paramString.indexOf(',');
    int j = paramString.indexOf('-', i + 1);
    String str1 = paramString.substring(0, i);
    String str2 = paramString.substring(i + 1, j);
    int k = Integer.parseInt(str1);
    int m = Integer.parseInt(str2);
    setRowColumn(k, m);
  }

  public void setRowColumn(int paramInt1, int paramInt2)
  {
    this.currentRow = paramInt1;
    this.currentColumn = paramInt2;
    this.leftColumn = (this.currentColumn - this.columns / 2);
    this.rightColumn = (this.currentColumn + this.columns / 2);
    this.topRow = (this.currentRow - this.rows / 2);
    this.bottomRow = (this.currentRow + this.rows / 2);
    if (paramInt1 > 19 - this.rows / 2)
    {
      this.topRow = (19 - this.rows + 1);
      this.bottomRow = 19;
    }
    if (paramInt1 < this.rows / 2)
    {
      this.topRow = 0;
      this.bottomRow = (this.rows - 1);
    }
    if (paramInt2 < this.columns / 2)
    {
      this.leftColumn = 0;
      this.rightColumn = (this.columns - 1);
    }
    if (paramInt2 > 19 - this.columns / 2)
    {
      this.leftColumn = (19 - this.columns + 1);
      this.rightColumn = 19;
    }
    repaint();
    this.traversalState = false;
  }

  public void commandAction(Command paramCommand, Item paramItem)
  {
    Object localObject;
    if (paramCommand == CMD_EDIT)
    {
      if (permission.equals("W")) {
        localObject = new TextInput(this.cells[this.currentRow][this.currentColumn].getData(), this, this.display);
        this.display.setCurrent((Displayable)localObject);
      } else {
         Alert alert = new Alert("Alert");
         alert.setString("You don't have permission to edit this spreadsheet!");
         alert.setTimeout(2000);
         this.display.setCurrent(alert);
      }
    }
    if (paramCommand == CMD_DELETE)
    {
        if (permission.equals("W")) {
             this.cells[this.currentRow][this.currentColumn].setData("");
             repaint();
             this.cells[this.currentRow][this.currentColumn].setModified(true);
             this.cells[this.currentRow][this.currentColumn].setLastModified(new Date(System.currentTimeMillis()));
      } else {
          Alert alert = new Alert("Alert");
          alert.setString("You don't have permission to edit this spreadsheet!");
          alert.setTimeout(2000);
          this.display.setCurrent(alert);
      }
    }
    if (paramCommand == CMD_CUT)
    {
        if (permission.equals("W")) {
            this.copyData = this.cells[this.currentRow][this.currentColumn].getData();
            this.cells[this.currentRow][this.currentColumn].setData("");
            repaint();
             this.cells[this.currentRow][this.currentColumn].setModified(true);
             this.cells[this.currentRow][this.currentColumn].setLastModified(new Date(System.currentTimeMillis()));
        } else {
          Alert alert = new Alert("Alert");
          alert.setString("You don't have permission to edit this spreadsheet!");
          alert.setTimeout(2000);
          this.display.setCurrent(alert);
      }
    }
    if (paramCommand == CMD_COPY)
      if (permission.equals("W")) {
        this.copyData = this.cells[this.currentRow][this.currentColumn].getData();
      } else {
          Alert alert = new Alert("Alert");
          alert.setString("You don't have permission to edit this spreadsheet!");
          alert.setTimeout(2000);
          this.display.setCurrent(alert);
      }
    if (paramCommand == CMD_PASTE)
    {
      if (permission.equals("W")) {
          this.cells[this.currentRow][this.currentColumn].setData((this.copyData));
          repaint();
          this.cells[this.currentRow][this.currentColumn].setModified(true);
           this.cells[this.currentRow][this.currentColumn].setLastModified(new Date(System.currentTimeMillis()));
      } else {
          Alert alert = new Alert("Alert");
          alert.setString("You don't have permission to edit this spreadsheet!");
          alert.setTimeout(2000);
          this.display.setCurrent(alert);
      }
    }
    if (paramCommand == CMD_GOTO)
    {
      localObject = new GotoForm(this, this.display);
      this.display.setCurrent((Displayable)localObject);
    }
    if (paramCommand == CMD_FIND)
    {
      localObject = new FindForm(this, this.display);
      this.display.setCurrent((Displayable)localObject);
    }
    if (paramCommand == CMD_ADD) {
      if (permission.equals("W")) {
        localObject = new SharedListForm(this, this.display);
        this.display.setCurrent(((SharedListForm)localObject).getList());
      } else {
          Alert alert = new Alert("Alert");
          alert.setString("You don't have permission to add new contacts!");
          alert.setTimeout(2000);
          this.display.setCurrent(alert);
      }
    }
    if (paramCommand == CMD_SAVE) {
      if (permission.equals("W")) {
          try
          {
            if (this.recordSetName == null)
            {
              localObject = new TextInput("Save as", this, this.display, 10);
              this.display.setCurrent((Displayable)localObject);
            }
            else
            {
              RecordStore.deleteRecordStore(this.recordSetName);
              this.rs = RecordStore.openRecordStore(this.recordSetName, true);
              for (int i = 0; i < 20; i++) {
                for (int j = 0; j < 20; j++) {
                  this.cells[i][j].setModified(false);
                  String str = this.cells[i][j].getData();
                  this.sendUpdate(this.recordSetName, i, j, this.cells[i][j].getData());
                  localObject = str.getBytes();
                  if (str.length() == 0)
                    continue;
                  str = "" + i + "," + j + "," + str;
                  localObject = str.getBytes();
                  this.rs.addRecord((byte[])localObject, 0, str.length());
                }
              }
              this.rs.closeRecordStore();

              String sharedContacts = this.recordSetName + "/" + "Contacts";
              RecordStore.deleteRecordStore(sharedContacts);
              this.rs = RecordStore.openRecordStore(sharedContacts, true);
              for (int i = 0; i < this.contacts.size(); i++) {
                String str = (String)this.contacts.elementAt(i);
                byte[] arrayOfByte = str.getBytes();
                if (arrayOfByte.length == 0)
                  continue;
                this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
              }
              this.rs.closeRecordStore();

              String permissions = this.recordSetName + "/" + "Permissions";
              RecordStore.deleteRecordStore(permissions);
              this.rs = RecordStore.openRecordStore(permissions, true);
              for (int i = 0; i < this.contacts.size(); i++) {
                  String phoneNo = (String)this.contacts.elementAt(i);
                  String str = (String)this.map.get(phoneNo);
                  byte[] arrayOfByte = str.getBytes();
                  if (arrayOfByte.length == 0)
                      continue;
                  this.rs.addRecord(arrayOfByte, 0, arrayOfByte.length);
              }
              this.rs.closeRecordStore();

              String myPermission = this.recordSetName + "/" + "MyPermission";
              RecordStore.deleteRecordStore(myPermission);
              this.rs = RecordStore.openRecordStore(myPermission, true);
              byte[] bytes = this.permission.getBytes();
              this.rs.addRecord(bytes, 0, bytes.length);
              this.rs.closeRecordStore();

              String ownerRecord = this.recordSetName + "/" + "Owner";
              RecordStore.deleteRecordStore(ownerRecord);
              this.rs = RecordStore.openRecordStore(ownerRecord, true);
              String str = "0";
              if (this.IsOwner())
                str = "1";
              byte[] arrayOfBytes = str.getBytes();
              this.rs.addRecord(arrayOfBytes, 0, arrayOfBytes.length);
              this.rs.closeRecordStore();

              this.lastSaved = new Date(System.currentTimeMillis());
              this.contactAdded = false;
            }

          }
          catch (RecordStoreNotOpenException e) {
            e.printStackTrace();
          }
          catch (InvalidRecordIDException e) {
            e.printStackTrace();
          }
          catch (RecordStoreException e) {
            e.printStackTrace();
          }
      } else {
          Alert alert = new Alert("Alert");
          alert.setString("You don't have permission to share this spreadsheet!");
          alert.setTimeout(2000);
          this.display.setCurrent(alert);
      }
    }

    if (paramCommand == CMD_EXIT) {
        this.display.setCurrent(this.parent.getMainMenu());
        this.parent.reset();
    }
  }

  public void AddContact(String phoneNo){
      if(!this.contacts.contains(phoneNo))
        this.contacts.addElement(phoneNo);
      if (this.contacts.size() == 1)
          mapContact(phoneNo, "W");
  }

  public void setContactAdded(boolean value) {
      this.contactAdded = value;
  }

  public void mapContact(String phoneNo, String permission) {
      this.map.put(phoneNo, permission);
  }

  public String getPermission(String phoneNo) {
      return (String) this.map.get(phoneNo);
  }

  public void deleteContact(String phoneNo) {
      this.contacts.removeElement(phoneNo);
  }

  public Vector getContacts() {
      return this.contacts;
  }

  public String getSheetName() {
        return recordSetName;
  }

  public void setIsOwner(boolean bool) {
      this.isOwner = bool;
  }

  public boolean IsOwner() {
      return this.isOwner;
  }

  public void setPermission(String perm) {
      this.permission = perm;
  }

  public void sendUpdate(String sheetName, int row, int col, String data){
      long lastModifiedTime = this.cells[row][col].getLastModified().getTime();
      if(this.lastSaved.getTime() < lastModifiedTime || (this.contactAdded && data.length() > 0)) {
        for(int i = 0; i < this.contacts.size(); i++){
          String phoneNo = (String) this.contacts.elementAt(i);
          
          SMSHeader msg = new SMSHeader(sheetName, (String)map.get(phoneNo), 0, 1, 1, row, col, data);
          SendSMS sms = new SendSMS(this.parent, phoneNo, msg.toString(), this.parent.getPort());
          sms.start();
        }
      }
  }

  private void informContactsAboutEachOther(String sheetName) {
      for (int i = 0; i < this.contacts.size(); i++) {
          String phoneNo1 = (String)this.contacts.elementAt(i);
          for (int j = i; j < this.contacts.size(); j++) {
              String phoneNo2 = (String) this.contacts.elementAt(j);
              if (phoneNo1.equals(phoneNo2)) {
                  continue;
              }
              SMSHeader msg1 = new SMSHeader(sheetName, (String)map.get(phoneNo2), 0, 1, 1, -3, -3, phoneNo2);
              SendSMS sms1 = new SendSMS(this.parent, phoneNo1, msg1.toString(), this.parent.getPort());
              sms1.start();

              SMSHeader msg2 = new SMSHeader(sheetName, (String)map.get(phoneNo1), 0, 1, 1, -3, -3, phoneNo1);
              SendSMS sms2 = new SendSMS(this.parent, phoneNo2, msg2.toString(), this.parent.getPort());
              sms2.start();
          }
      }
  }

}
