package il.festinger.historia;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.*;

final class TypeListScreen extends MainScreen
{
	private Bitmap titleBitmap = Bitmap.getBitmapResource("window_title.png");	// title icon
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
	private HorizontalFieldManager horizontalTitle;
	private CheckboxField checkboxIncoming;
	private CheckboxField checkboxOutgoing;
	private CheckboxField checkboxMissed;
	private CheckboxField checkboxUnanswered;
    public TypeListScreen() //class of a call
    {
	    super();
    	try
    	{
			Graphics g = this.getGraphics();	// for getting font height
			int spaceHeight = (g.getFont().getHeight()) * 50/100;	// space height
	        // ---- sets title ----
	        titleBitmapField = new BitmapField(titleBitmap);
			titleLabel = new LabelField(" Settings");
			horizontalTitle = new HorizontalFieldManager();
			horizontalTitle.add(titleBitmapField);
			horizontalTitle.add(titleLabel);
			horizontalTitle.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),17));	// set font size
			this.setTitle(horizontalTitle);
	        //-----------UI-------------------------------------   
			//-----add to top--------
			checkboxIncoming = new CheckboxField("Incoming calls",((String)(Historia.keys.elementAt(Historia.LOGINCOMING))).compareTo("0") == 0 ? false : true );
			checkboxOutgoing = new CheckboxField("Outgoing calls",((String)(Historia.keys.elementAt(Historia.LOGOUTGOING))).compareTo("0") == 0 ? false : true );
			checkboxMissed = new CheckboxField("Missed calls",((String)(Historia.keys.elementAt(Historia.LOGMISSED))).compareTo("0") == 0 ? false : true );
			checkboxUnanswered = new CheckboxField("Unanswered calls",((String)(Historia.keys.elementAt(Historia.LOGUNANSWERED))).compareTo("0") == 0 ? false : true );
			
			add(new SpaceField(Display.getWidth(),spaceHeight));
			add(checkboxIncoming);
			add(new SpaceField(Display.getWidth(),spaceHeight));
			add(new CustomSeparatorField()); 
			
			add(new SpaceField(Display.getWidth(),spaceHeight));
			add(checkboxOutgoing);
			add(new SpaceField(Display.getWidth(),spaceHeight));
			add(new CustomSeparatorField()); 
			
			add(new SpaceField(Display.getWidth(),spaceHeight));
			add(checkboxMissed);
			add(new SpaceField(Display.getWidth(),spaceHeight));
			add(new CustomSeparatorField());
			
			add(new SpaceField(Display.getWidth(),spaceHeight));
			add(checkboxUnanswered);
			add(new SpaceField(Display.getWidth(),spaceHeight));
			add(new CustomSeparatorField()); 
			
	        //------------UI--------------------------------------
    	}
    	catch(Exception e)
    	{
    		Dialog.inform("Error opening Type List . If error persists contact support.\n" + e.getMessage());
    		UiApplication.getUiApplication().popScreen(this);
    	}
    }
    protected boolean onSave()		// if save was chosen
    {
    	if (checkboxIncoming.isDirty())
    		Historia.keys.setElementAt(checkboxIncoming.getChecked() == true ? "1" : "0",Historia.LOGINCOMING);
    	if (checkboxOutgoing.isDirty())
    		Historia.keys.setElementAt(checkboxOutgoing.getChecked() == true ? "1" : "0",Historia.LOGOUTGOING);
    	if (checkboxMissed.isDirty())
    		Historia.keys.setElementAt(checkboxMissed.getChecked() == true ? "1" : "0",Historia.LOGMISSED);
    	if (checkboxUnanswered.isDirty())
    		Historia.keys.setElementAt(checkboxUnanswered.getChecked() == true ? "1" : "0",Historia.LOGUNANSWERED);
		Historia.saveConfiguration();  
    	return true;
    }
	
}


