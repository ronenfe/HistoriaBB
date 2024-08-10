package il.festinger.historia;



import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.*;

final class GeneralSettings extends MainScreen
{
	private HorizontalFieldManager horizontalTitle;	// stores the title
	private Bitmap titleBitmap = Bitmap.getBitmapResource("view_title_settings.png");	// title icon
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
    private LabelField labelNumberOfCalls;
    private BasicEditField textNumberOfCalls;
    private ObjectChoiceField objectChoiceFieldDate;
    private ObjectChoiceField objectChoiceFieldTime;
    private ObjectChoiceField objectChoiceFieldLayout;
    private ObjectChoiceField objectChoiceFieldFontSize;
    private ObjectChoiceField objectChoiceFieldLocation;
    private CheckboxField monitorCheckbox;
    private ButtonField buttonReset;
    private VerticalFieldManager fieldManagerButton;	// manager for buttons 
    private int size;	// size of calls vector
    private String[] ObjectsDate = {"Month/Day/Year","Day/Month/Year"}; // date formats
    private String[] ObjectsTime = {"12Hour","24Hour"}; // date formats
    private String[] ObjectsLayout = {"Normal","Compact"}; // layout formats
    private String[] ObjectsLocation = {"Device Memory","Media Card"}; // layout formats
    private String[] ObjectsFontSize = {"System","7","8","9","10","11","12","13","14","15","16","17","18","19","20","21","22","23","24","25","27","28","29","30"}; // font formats
    public GeneralSettings() //settings window
    {
        super();
        titleBitmapField = new BitmapField(titleBitmap);	// sets title
		titleLabel = new LabelField(" Settings");
		horizontalTitle = new HorizontalFieldManager();
		horizontalTitle.add(new LabelField(" "));
		horizontalTitle.add(titleBitmapField);
		horizontalTitle.add(titleLabel);
		horizontalTitle.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),17));	// set font size
		this.setTitle(horizontalTitle);
        //-----------UI-------------------------------------       
        Graphics g = this.getGraphics();	// for getting font height
        int spaceHeight = (g.getFont().getHeight()) * 50/100;	// space height
        size = Historia.getVector().size();
        labelNumberOfCalls = new LabelField("Current Number of Calls in File: " + size);
        textNumberOfCalls = new BasicEditField("Limit Number of Calls in File to (10 - 1000):", (String)Historia.keys.elementAt(Historia.NUMBEROFCALLS),6,BasicEditField.FILTER_INTEGER);
        objectChoiceFieldDate = new ObjectChoiceField("Date Format:",ObjectsDate,Integer.parseInt((String)Historia.keys.elementAt(Historia.DATEFORMAT)));
        objectChoiceFieldTime = new ObjectChoiceField("Time Format:",ObjectsTime,Integer.parseInt((String)Historia.keys.elementAt(Historia.TIMEFORMAT)));
        objectChoiceFieldLayout = new ObjectChoiceField("Layout:",ObjectsLayout,Integer.parseInt((String)Historia.keys.elementAt(Historia.LAYOUT)));
        objectChoiceFieldLocation = new ObjectChoiceField("Storing Location:",ObjectsLocation,Integer.parseInt((String)Historia.keys.elementAt(Historia.LOCATION)));
        objectChoiceFieldFontSize = new ObjectChoiceField("Font Size:",ObjectsFontSize,Integer.parseInt((String)Historia.keys.elementAt(Historia.FONTSIZE)));
        monitorCheckbox = new CheckboxField("Monitor Calls in Background", ((String) Historia.keys.elementAt(Historia.MONITOR)).compareTo("0") == 0 ? false : true);
        fieldManagerButton = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH);
        buttonReset = new ButtonField("Reset Settings",ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | ButtonField.FIELD_HCENTER);
        fieldManagerButton.add(buttonReset);
        
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(objectChoiceFieldDate);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        
        add(new CustomSeparatorField());     
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(objectChoiceFieldTime);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        
        add(new CustomSeparatorField());        
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(objectChoiceFieldLayout);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        
        add(new CustomSeparatorField());
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(objectChoiceFieldFontSize);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        String modelNum = DeviceInfo.getDeviceName();
        if ((modelNum.startsWith("8") && !modelNum.startsWith("87")) || modelNum.startsWith("9")) 
        {
                //microSD card supported
        	add(new CustomSeparatorField());
            add(new SpaceField(Display.getWidth(),spaceHeight));
            add(objectChoiceFieldLocation);
            add(new SpaceField(Display.getWidth(),spaceHeight));
        }
             
        add(new CustomSeparatorField());        
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(labelNumberOfCalls);
        add(textNumberOfCalls);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        
        add(new CustomSeparatorField());
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add (monitorCheckbox);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        
        add(new CustomSeparatorField());
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(fieldManagerButton);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        //------------UI--------------------------------------
        FieldChangeListener listenerReset = new FieldChangeListener()	//reset button
        {
        	public void fieldChanged(Field field, int context)
        	{
        		if (Dialog.ask(Dialog.D_YES_NO,"Settings will be restored to original state, old calls may be deleted. Are you sure?",Dialog.NO) == Dialog.YES)  // warn the user
    			{
            		Historia.keys.setElementAt("0", Historia.LASTCALL);
            		Historia.keys.setElementAt("1000", Historia.NUMBEROFCALLS);
            		Historia.keys.setElementAt("1", Historia.MONITOR);
            		Historia.keys.setElementAt("1", Historia.CALENDAR);
            		Historia.keys.setElementAt("0", Historia.LAYOUT);
            		Historia.keys.setElementAt("0", Historia.FONTSIZE);
            		Historia.keys.setElementAt("0", Historia.TIMEFORMAT);
            		Historia.keys.setElementAt("0", Historia.DATEFORMAT);
            		Historia.saveConfiguration();
        			Dialog.inform("Application needs to be closed. Please restart the device to apply all changes.");
        			System.exit(1);
    			}
    		}
        };
        buttonReset.setChangeListener(listenerReset);	// listen to button
    }
    public boolean containsOnlyNumbers(String str)
    {
        
        //It can't contain only numbers if it's null or empty...
        if (str == null || str.length() == 0)
            return false;
        
        for (int i = 0; i < str.length(); i++)
        {

            //If we find a non-digit character we return false.
            if (!Character.isDigit(str.charAt(i)))
                return false;
        }
        
        return true;
    }

    protected boolean onSave()		// if save was chosen
    {
    	 String stringCallsLimit = textNumberOfCalls.getText();  // save calls limit from input
		 int callsLimit = 0;
		 if (containsOnlyNumbers(stringCallsLimit))
			 callsLimit = Integer.parseInt(stringCallsLimit);
		 
		 if (callsLimit < 10 || callsLimit > 1000)
		 {
             textNumberOfCalls.setText((String)Historia.keys.elementAt(Historia.NUMBEROFCALLS));
             Dialog.inform("Number of calls should be between 10 and 1000.");
             return false;
		 }
         if (callsLimit < size)   // if calls limit chosen is less than current number of calls
         {
             if (Dialog.ask(Dialog.D_YES_NO, "This will delete all calls after call number " + textNumberOfCalls.getText() + ", are you sure ?") == Dialog.YES)  // warn the user
             {
                 // if user agreed to delete calls
        		saveSettings();
                return true;          
             }
              // if user canceled restore back call limit from file
             textNumberOfCalls.setText((String)Historia.keys.elementAt(Historia.NUMBEROFCALLS));
             return false;
         }
         // if calls limit larger than current calls limit      
		saveSettings();
    	UiApplication.getUiApplication().invokeLater (new Runnable() {
		    public void run()
		    {
		    	Status.show("Saving...",Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS),100);
		    }
    		});
		
        return true;	
    }
    void changeLocation()
    {
		Main.storageStatus = Main.checkStorage();
		int prevLocation = Integer.parseInt((String)Historia.keys.elementAt(Historia.LOCATION));
    	Historia.keys.setElementAt(Integer.toString(objectChoiceFieldLocation.getSelectedIndex()),Historia.LOCATION);
    	FileConnection fc;
    	
		if (Main.storageStatus == 0)	// if in mass storage mode
		{
			Main.displayStorageError();
			Historia.keys.setElementAt(Integer.toString(prevLocation),Historia.LOCATION);
			return;
		}	
		else if (Main.storageStatus == -1)
		{
			if (Dialog.ask(Dialog.D_YES_NO,"SD Card is not inserted. Do you still want to change location to Device Memory?" +
					" Old file will not be moved to this new location, old calls may become missing.\n",Dialog.NO) == Dialog.NO)
			{
				Historia.keys.setElementAt(Integer.toString(prevLocation),Historia.LOCATION);
				return;
			}
			else	// user wants to change to device memory
			{
				try
		    	{
					fc = (FileConnection) Connector.open(Historia.targetFolder +"Historia.txt",Connector.READ_WRITE);
					if (fc.exists())
					{
						fc.delete();
						fc.close();
					}
		    	}
				catch (Exception e) // file exception 
				{
					Dialog.alert("Application cannot access calls.\n");
					return;
				}				
				return;
			}
		}
		else if (Main.storageStatus == 2)	// if in mass storage mode
		{
			
			Historia.keys.setElementAt(Integer.toString(prevLocation),Historia.LOCATION);
			Dialog.alert("SD-Card is not available. Insert card and try again.");
			return;
		}	
    	try
    	{
	    	if (objectChoiceFieldLocation.getSelectedIndex() == 0 )	// if user chose to save to device memory
			{
				fc = (FileConnection) Connector.open(Historia.targetFolder +"Historia.txt",Connector.READ_WRITE);
				String OldTargetFolder = Historia.targetFolder;
				if (fc.exists())
				{
					fc.delete();
					fc.close();
				}
				Historia.targetFolder = "file:///store/home/user/documents/"; //where to save and get the csv file
				fc = (FileConnection) Connector.open(Historia.targetFolder,Connector.READ_WRITE);
				if(!fc.exists())  // if directory Documents does not exist, create it
				{
					fc.mkdir();
				}
				Historia.targetFolder += "Historia/";
				fc = (FileConnection) Connector.open(Historia.targetFolder,Connector.READ_WRITE);
				if(!fc.exists())  // if directory Historia does not exist, create it
				{
					fc.mkdir();
				}
				Main.saveCSV();
				try
				{
					fc = (FileConnection)  Connector.open(OldTargetFolder,Connector.READ_WRITE);
					fc.delete();
					fc.close();
				}
				catch(Exception e)
				{
					
				}
			}
			else
			{
				fc = (FileConnection) Connector.open(Historia.targetFolder + "Historia.txt",Connector.READ_WRITE);
				String OldTargetFolder = Historia.targetFolder;
				if (fc.exists())
				{
					fc.delete();
					fc.close();
				}
				Historia.targetFolder = "file:///SDCard/documents/"; //where to save and get the csv file
				fc = (FileConnection) Connector.open(Historia.targetFolder,Connector.READ_WRITE);
				if(!fc.exists())  // if directory Documents does not exist, create it
				{
					fc.mkdir();
				}
				Historia.targetFolder += "Historia/";
				fc = (FileConnection) Connector.open(Historia.targetFolder,Connector.READ_WRITE);
				if(!fc.exists())  // if directory Historia does not exist, create it
				{
					fc.mkdir();
				}
				Main.saveCSV();
				try
				{
					fc = (FileConnection)  Connector.open(OldTargetFolder,Connector.READ_WRITE);
					fc.delete();
					fc.close();
				}
				catch(Exception e)
				{
					
				}
			}
    	}
		catch (Exception e) // file exception 
		{
			Dialog.alert("Application cannot access calls.\n");
			return;
		}
    }
    void saveSettings()
    {
    	if (textNumberOfCalls.isDirty())
    		Historia.keys.setElementAt(textNumberOfCalls.getText(),Historia.NUMBEROFCALLS);
		if (objectChoiceFieldDate.isDirty())
		{
			Historia.keys.setElementAt(Integer.toString(objectChoiceFieldDate.getSelectedIndex()),Historia.DATEFORMAT);
			Main.saveCSV();
		}
		if (objectChoiceFieldTime.isDirty())
		{
			Historia.keys.setElementAt(Integer.toString(objectChoiceFieldTime.getSelectedIndex()),Historia.TIMEFORMAT);
			Main.saveCSV();
		}
		if (objectChoiceFieldLayout.isDirty())
		{
			Historia.keys.setElementAt(Integer.toString(objectChoiceFieldLayout.getSelectedIndex()),Historia.LAYOUT);
		}
		if (objectChoiceFieldFontSize.isDirty())
		{
			Historia.keys.setElementAt(Integer.toString(objectChoiceFieldFontSize.getSelectedIndex()),Historia.FONTSIZE);
		}
    	if ( monitorCheckbox.isDirty())
    	{
    		if(monitorCheckbox.getChecked())
	    	{
	    		Historia.keys.setElementAt("1",Historia.MONITOR);
	            Dialog.inform("Please restart the device for monitoring to be enabled.");
	    	}
	    	else
	    	{
	    		Historia.keys.setElementAt("0",Historia.MONITOR);
	    		Dialog.inform("Please restart the device for monitoring to be disabled.");
	    	}
    	}
		if (objectChoiceFieldLocation.isDirty())
		{
			changeLocation();
		}
		Historia.saveConfiguration();  
    }
}

