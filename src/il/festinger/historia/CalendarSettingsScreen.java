package il.festinger.historia;

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.ui.container.*;

final class CalendarSettingsScreen extends MainScreen
{
	private HorizontalFieldManager horizontalTitle;	// stores the title
	private Bitmap titleBitmap = Bitmap.getBitmapResource("view_title_settings.png");	// title icon
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
    private ObjectChoiceField objectChoiceFieldType;
    private ObjectChoiceField objectChoiceFieldNumbers;
    private CheckboxField calendarCheckbox;
    private ButtonField buttonType;
    private ButtonField buttonNumbers;
    private VerticalFieldManager fieldManagerButtonType;	// manager for buttons
    private VerticalFieldManager fieldManagerButtonNumbers;	// manager for buttons 
    private String[] ObjectsType = {"All","Custom"}; // type of calls to log
    private String[] ObjectsNumbers = {"All","Custom"}; // date formats
    private NumbersListScreen numbersListScreen;
    private TypeListScreen typeListScreen;
    private Vector vectorNumbers;
    public CalendarSettingsScreen(Vector passedVectorNumbers) //settings window
    {
        super();
        vectorNumbers = passedVectorNumbers;
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
        objectChoiceFieldType = new ObjectChoiceField("Call Type to log:",ObjectsType,(String)Historia.keys.elementAt(Historia.CALLTYPETOLOG))
        {
        	public boolean isFocusable()
        	{
        		return isEditable();
        	}
        };
        objectChoiceFieldNumbers = new ObjectChoiceField("Numbers to log:",ObjectsNumbers,(String)Historia.keys.elementAt(Historia.NUMBERSTOLOG))
        {
        	public boolean isFocusable()
        	{
        		return isEditable();
        	}
        };
       
        
        calendarCheckbox = new CheckboxField("Enable call logging in Calendar", ((String) Historia.keys.elementAt(Historia.CALENDAR)).compareTo("0") == 0 ? false : true);
        
        fieldManagerButtonType = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH);
        buttonType = new ButtonField("Customize Type",ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | ButtonField.FIELD_RIGHT | ButtonField.NEVER_DIRTY)
        {
        	public boolean isFocusable()
        	{
        		return isEditable();
        	}
        };
        fieldManagerButtonType.add(buttonType);
        
        fieldManagerButtonNumbers = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH);
        buttonNumbers = new ButtonField("Numbers List",ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | ButtonField.FIELD_RIGHT |ButtonField.NEVER_DIRTY)
        {
        	public boolean isFocusable()
        	{
        		return isEditable();
        	}
        };
        fieldManagerButtonNumbers.add(buttonNumbers);
        
        add(new CustomSeparatorField());
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add (calendarCheckbox);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        
        add(new CustomSeparatorField());     
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(objectChoiceFieldType);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(fieldManagerButtonType);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        
        add(new CustomSeparatorField());     
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(objectChoiceFieldNumbers);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(fieldManagerButtonNumbers);
        add(new SpaceField(Display.getWidth(),spaceHeight));
        add(new NullField());

        

        //------------UI--------------------------------------
        final FieldChangeListener listenerType = new FieldChangeListener()	//type button
        {
        	public void fieldChanged(Field field, int context)
        	{
        		typeListScreen = new TypeListScreen();	// create the cell id  window
    			UiApplication.getUiApplication().pushScreen(typeListScreen);
    		}
        };

        
        final FieldChangeListener listenerNumbers = new FieldChangeListener()	//numbers button
        {
        	public void fieldChanged(Field field, int context)
        	{
        		numbersListScreen = new NumbersListScreen(vectorNumbers);	// create the cell id  window
    			UiApplication.getUiApplication().pushScreen(numbersListScreen);
    		}
        };

        
       
        	
        final FieldChangeListener listenerObjectType = new FieldChangeListener()	//listener to objectfield type
        {
        	public void fieldChanged(Field field, int context)
        	{
        		if (objectChoiceFieldType.getSelectedIndex() == 0 && buttonType.getChangeListener() != null)	// all is selected
        		{
        			buttonType.setChangeListener(null);
        			buttonType.setEditable(false);	// disable button
        		}
        		else if (objectChoiceFieldType.getSelectedIndex() == 1 && buttonType.getChangeListener() == null)
        		{
        			buttonType.setChangeListener(listenerType);
        			buttonType.setEditable(true);	// disable button
        		}
        			
    		}
        };
        
        final FieldChangeListener listenerObjectNumbers = new FieldChangeListener()	//numbers button
        {
        	public void fieldChanged(Field field, int context)
        	{
           		if (objectChoiceFieldNumbers.getSelectedIndex() == 0 && buttonNumbers.getChangeListener() != null)	// all is selected
        		{
           			buttonNumbers.setChangeListener(null);
           			buttonNumbers.setEditable(false);	// disable button
        		}
        		else if (objectChoiceFieldNumbers.getSelectedIndex() == 1 && buttonNumbers.getChangeListener() == null)
        		{
        			buttonNumbers.setChangeListener(listenerNumbers);
        			buttonNumbers.setEditable(true);	// disable button
        		}
    		}
        };
        
        FieldChangeListener listenerCalendarCheckbox = new FieldChangeListener()	//listener to calendar checkbox
        {
        	public void fieldChanged(Field field, int context)
        	{
        		if (calendarCheckbox.getChecked() == true)	// if checkbox is checked
        		{
    			  if (((String) Historia.keys.elementAt((Historia.CALLTYPETOLOG))).compareTo("All") == 0)
    		        {
    					buttonType.setChangeListener(null);
    					buttonType.setEditable(false);	// disable button
    				}
    				else
    				{
    					buttonType.setChangeListener(listenerType);
    					buttonType.setEditable(true);	// disable button
    				}
        	        if (((String) Historia.keys.elementAt((Historia.NUMBERSTOLOG))).compareTo("All") == 0)
        	        {
        	        	buttonNumbers.setChangeListener(null);
        	        	buttonNumbers.setEditable(false);	// disable button
        			}
        			else
        			{
        				buttonNumbers.setChangeListener(listenerNumbers);
        				buttonNumbers.setEditable(true);	// disable button
        			}
                	objectChoiceFieldType.setEditable(true);
                	objectChoiceFieldType.setChangeListener(listenerObjectType);
                	objectChoiceFieldNumbers.setEditable(true);
                	objectChoiceFieldNumbers.setChangeListener(listenerObjectNumbers);
        		}
        		else
        		{
                	buttonType.setEditable(false);	// disable button
                	buttonType.setChangeListener(null);
                	buttonNumbers.setEditable(false);	// disable button
                	buttonNumbers.setChangeListener(null);
                	objectChoiceFieldType.setEditable(false);
                	objectChoiceFieldType.setChangeListener(null);
                	objectChoiceFieldNumbers.setEditable(false);
                	objectChoiceFieldNumbers.setChangeListener(null);
        		}
        		UiApplication.getUiApplication().invokeAndWait(new Runnable(){
        			public void run()
        			{
        				((CalendarSettingsScreen)UiApplication.getUiApplication().getActiveScreen()).invalidate();
        			}
        		});
	        			
        	}
        };
        calendarCheckbox.setChangeListener(listenerCalendarCheckbox);
        
		if (calendarCheckbox.getChecked() == true)	// if checkbox is checked
		{
		  if (((String) Historia.keys.elementAt((Historia.CALLTYPETOLOG))).compareTo("All") == 0)
	        {
				buttonType.setChangeListener(null);
				buttonType.setEditable(false);	// disable button
			}
			else
			{
				buttonType.setChangeListener(listenerType);
				buttonType.setEditable(true);	// disable button
			}
	        if (((String) Historia.keys.elementAt((Historia.NUMBERSTOLOG))).compareTo("All") == 0)
	        {
	        	buttonNumbers.setChangeListener(null);
	        	buttonNumbers.setEditable(false);	// disable button
			}
			else
			{
				buttonNumbers.setChangeListener(listenerNumbers);
				buttonNumbers.setEditable(true);	// disable button
			}
        	objectChoiceFieldType.setEditable(true);
        	objectChoiceFieldType.setChangeListener(listenerObjectType);
        	objectChoiceFieldNumbers.setEditable(true);
        	objectChoiceFieldNumbers.setChangeListener(listenerObjectNumbers);
		}
		else
		{
        	buttonType.setEditable(false);	// disable button
        	buttonType.setChangeListener(null);
        	buttonNumbers.setEditable(false);	// disable button
        	buttonNumbers.setChangeListener(null);
        	objectChoiceFieldType.setEditable(false);
        	objectChoiceFieldType.setChangeListener(null);
        	objectChoiceFieldNumbers.setEditable(false);
        	objectChoiceFieldNumbers.setChangeListener(null);
		}
		this.invalidate();
    }

    protected boolean onSave()		// if save was chosen
    {	
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
    private void saveSettings()
    {

    	if ( calendarCheckbox.isDirty())
    	{
    		if (calendarCheckbox.getChecked())
    			Historia.keys.setElementAt("1",Historia.CALENDAR);
    		else 
    			Historia.keys.setElementAt("0",Historia.CALENDAR);
    	}
		if (objectChoiceFieldType.isDirty())
		{
			if (objectChoiceFieldType.getSelectedIndex() == 0 )	// all is selected
    		{
				Historia.keys.setElementAt("All",Historia.CALLTYPETOLOG);
    		}
    		else
    		{
    			Historia.keys.setElementAt("Custom",Historia.CALLTYPETOLOG);
    		}
		}
		if (objectChoiceFieldNumbers.isDirty())
		{
			if (objectChoiceFieldNumbers.getSelectedIndex() == 0 )	// all is selected
    		{
				Historia.keys.setElementAt("All",Historia.NUMBERSTOLOG);
    		}
    		else
    		{
    			Historia.keys.setElementAt("Custom",Historia.NUMBERSTOLOG);
    		}
		}		
		Historia.saveConfiguration();  
    }
}

