package il.festinger.historia;


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.*;

final class CallDetailsScreen extends MainScreen
{
	private LabelField typeLabel;
	private LabelField timeLabel;
	private LabelField nameLabel;
	private LabelField errorLabel;
	private LabelField durationLabel;
	private LabelField cellIDLabel;
	private Bitmap incomingBitmap = Bitmap.getBitmapResource("call_type_incoming.png");	// incoming icon
	private Bitmap outgoingBitmap = Bitmap.getBitmapResource("call_type_outgoing.png");	// outgoing icon
	private Bitmap errorBitmap = Bitmap.getBitmapResource("call_type_error.png");	// missed icon
	private Bitmap nameBitmap = Bitmap.getBitmapResource("call_details_contact_card.png");	// name icon
	private Bitmap timeBitmap = Bitmap.getBitmapResource("call_details_date_and_time.png");	// time icon
	private Bitmap durationBitmap = Bitmap.getBitmapResource("call_details_duration.png");	// duration icon
	private Bitmap cellIDBitmap = Bitmap.getBitmapResource("call_details_cell_id.png");	// cell ID icon
	private BitmapField typeBitmapField;	// type icon field
	private BitmapField nameBitmapField;	// name icon field
	private BitmapField timeBitmapField;	// time icon field
	private BitmapField errorBitmapField;	// error icon field
	private BitmapField durationBitmapField;	// duration icon field
	private BitmapField cellIDBitmapField;	// cell ID icon field
	private Bitmap titleBitmap = Bitmap.getBitmapResource("window_title.png");	// title icon
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
	private HorizontalFieldManager horizontalTitle;	// stores the title
	private HorizontalFieldManager horizontalType;	// stores the title
	private HorizontalFieldManager horizontalDuration;	// stores the duration
	private HorizontalFieldManager horizontalTime;	// stores the title
	private HorizontalFieldManager horizontalName;	// stores the title
	private HorizontalFieldManager horizontalCellID;	// stores the cell ID
	private HorizontalFieldManager horizontalError;	// stores the Error
	
    public CallDetailsScreen(CallStrings callStrings) //class of a call
    {
	    super();
    	try
    	{
			Graphics g = this.getGraphics();	// for getting font height
			int spaceHeight = (g.getFont().getHeight()) * 50/100;	// space height
	        String stringTime = callStrings.strings[CallStrings.TIME];	// stores time of call
	        // ---- sets title ----
	        titleBitmapField = new BitmapField(titleBitmap);
			titleLabel = new LabelField(" " + callStrings.strings[CallStrings.NUMBER]);
			horizontalTitle = new HorizontalFieldManager();
			horizontalTitle.add(titleBitmapField);
			horizontalTitle.add(titleLabel);
			horizontalTitle.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),17));	// set font size
			this.setTitle(horizontalTitle);
	        //-----------UI-------------------------------------   
			nameBitmapField = new BitmapField(nameBitmap);	// name bitmap field
			timeBitmapField = new BitmapField(timeBitmap);	// time bitmap field
			durationBitmapField = new BitmapField(durationBitmap);	// duration bitmap field
			if (callStrings.strings[CallStrings.TYPE].equals("Incoming"))
				typeBitmapField = new BitmapField(incomingBitmap);	// type bitmap field
			else if (callStrings.strings[CallStrings.TYPE].equals("Outgoing"))
				typeBitmapField = new BitmapField(outgoingBitmap);	// type bitmap field
			else
				typeBitmapField = new BitmapField(errorBitmap);	// type bitmap field
			
			
			cellIDBitmapField = new BitmapField(cellIDBitmap);	// duration bitmap field
			
			typeLabel = new LabelField("  " + callStrings.strings[CallStrings.TYPE] + " Call");
			timeLabel = new LabelField("  " + stringTime);
			durationLabel = new LabelField("  " + callStrings.strings[CallStrings.DURATION]);
			nameLabel = new LabelField(callStrings.strings[CallStrings.NAME]);
			cellIDLabel = new LabelField("  " + callStrings.strings[CallStrings.CELLID]);
			
			horizontalType = new HorizontalFieldManager();	// stores the type
			horizontalType.add(new LabelField(" "));
			horizontalType.add(typeBitmapField);
			horizontalType.add(typeLabel);
			
			horizontalDuration = new HorizontalFieldManager();	// stores the duration
			horizontalDuration.add(new LabelField(" "));
			horizontalDuration.add(durationBitmapField);
			horizontalDuration.add(durationLabel);
			
			horizontalTime = new HorizontalFieldManager();	// stores the time
			horizontalTime.add(new LabelField(" "));
			horizontalTime.add(timeBitmapField);
			horizontalTime.add(timeLabel);
			
			horizontalName = new HorizontalFieldManager();	// stores the name
			horizontalName.add(new LabelField(" "));
			horizontalName.add(nameBitmapField);
			horizontalName.add(new LabelField("  "));
			horizontalName.add(nameLabel);
			
			horizontalCellID = new HorizontalFieldManager();	// stores the cell id
			horizontalCellID.add(new LabelField(" "));
			horizontalCellID.add(cellIDBitmapField);
			horizontalCellID.add(cellIDLabel);
			
			this.add(new SpaceField(Display.getWidth(),spaceHeight));
			this.add(horizontalType);
			if (!callStrings.strings[CallStrings.ERROR].equals("No Error"))	// if error occured
			{
				errorBitmapField = new BitmapField(errorBitmap);	// type bitmap field
				errorLabel = new LabelField("  " + callStrings.strings[CallStrings.ERROR] );
				horizontalError = new HorizontalFieldManager();	// stores the name
				horizontalError.add(new LabelField(" "));
				horizontalError.add(errorBitmapField);
				horizontalError.add(errorLabel);
				this.add(new SpaceField(Display.getWidth(),spaceHeight));
				this.add(horizontalError);
			}
			this.add(new SpaceField(Display.getWidth(),spaceHeight));
			this.add(horizontalTime);
			this.add(new SpaceField(Display.getWidth(),spaceHeight));
			this.add(horizontalDuration);
			this.add(new SpaceField(Display.getWidth(),spaceHeight));
			this.add(horizontalName);
			this.add(new SpaceField(Display.getWidth(),spaceHeight));
			this.add(horizontalCellID);
	
	        //------------UI--------------------------------------
    	}
    	catch(Exception e)
    	{
    		Dialog.inform("Error opening call details. If error persists contact support.\n" + e.getMessage());
    		UiApplication.getUiApplication().popScreen(this);
    	}
    }
    public boolean onClose()	// restore previous values
    {
    	UiApplication.getUiApplication().popScreen(this);
    	return true;
    }
}

