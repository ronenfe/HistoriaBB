package il.festinger.historia;


import java.util.Date;


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.DeviceInfo;import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ActiveRichTextField;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.*;


final class LicenseScreen extends MainScreen
{
	static final int TRIAL_PERIOD = 12;
	private int key;  // store the product key
    private int tryCount = 10;  // number of tries to enter a key.
	private Bitmap titleBitmap = Bitmap.getBitmapResource("window_title_about_registration.png");	// title icon
	private BitmapField titleBitmapField;	// title icon field
	private LabelField titleLabel;	//title label
	private HorizontalFieldManager horizontalTitle;	// stores the title
    private VerticalFieldManager fieldManagerMain;
    private VerticalFieldManager fieldManagerHeading;	// manager for heading
//    private VerticalFieldManager fieldManagerSite;	// manager for site
//    private HorizontalFieldManager fieldManagerSupport;	// manager for support
    private VerticalFieldManager fieldManagerStatus;	// manager for status
    private VerticalFieldManager fieldManagerBottom;	// manager for status
//    private VerticalFieldManager fieldManagerDeviceID;	// manager for device id
//    private VerticalFieldManager fieldManagerKey;	// manager for entering key
    private HorizontalFieldManager fieldManagerButton;	// manager for buttons
    private LabelField labelHeading;
    private LabelField labelStatus;
    private LabelField labelDeviceID;
    private LabelField labelCopyright;
    private BasicEditField basicKeyInput;
    private ButtonField buttonRegister;
    private ButtonField buttonTrial;
    private ActiveRichTextField activeRichTextFieldCopyright;
    private ActiveRichTextField activeRichTextFieldCopyright2;
    private String owner; // device ID
    private ScreenClosedListener listener;	// listener for closing event of the window 
    public LicenseScreen(ScreenClosedListener passedListener) 	// license window
    {
	    super();
    	try
    	{
			Historia.daysPassed = (int)((new Date().getTime() - Long.parseLong(Historia.keys.elementAt(Historia.FIRSTTIME).toString()))/86400000);	// time software was ran for the first time
	        listener = passedListener;
	        owner =  Integer.toHexString(DeviceInfo.getDeviceId()).toUpperCase();
	        
	        //-----------UI-------------------------------------
	        // ---- sets title ----
	        titleBitmapField = new BitmapField(titleBitmap);
	        if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0)
	        	titleLabel = new LabelField(" Register");
	        else
	        	titleLabel = new LabelField(" About");
			horizontalTitle = new HorizontalFieldManager();
			horizontalTitle.add(new LabelField(" "));
			horizontalTitle.add(titleBitmapField);
			horizontalTitle.add(titleLabel);
			horizontalTitle.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),17));	// set font size
			this.setTitle(horizontalTitle);
	       //--------------------------------
			
			Graphics g = this.getGraphics();	// for getting font height
			int spaceHeight = (g.getFont().getHeight()) * 25/100;	// space height
	        
			//main field
	        fieldManagerMain = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH);

	        // ---------------add icon-----------
	        fieldManagerMain.add(new SpaceField(Display.getWidth(),spaceHeight));
	        fieldManagerMain.add(new BitmapField(Bitmap.getBitmapResource("historia_logo_about_screen.png")
	        		,BitmapField.USE_ALL_WIDTH | BitmapField.HCENTER));
	        //--------------
	        
			//---------- "SBSH Historia X.X" 
	        fieldManagerHeading = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH);
	        fieldManagerHeading.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),
	        		Font.getDefault().getHeight() * 400/300));	// set font size
	        labelHeading = new LabelField("SBSH Historia 1.0 RC4",LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
	        fieldManagerHeading.add(labelHeading);
	        fieldManagerMain.add(new SpaceField(Display.getWidth(),spaceHeight));
	        fieldManagerMain.add(fieldManagerHeading);
	        //--------------------------------
	        
	      //-----------------------------registered status---------------------------
	        if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0 && (Historia.daysPassed >= TRIAL_PERIOD || Historia.daysPassed < 0))	//status field, choose color
	        {
	        	
		        fieldManagerStatus = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH)
		        {
		        	public void paint(Graphics graphics)
		            {
		        		graphics.setColor(Color.RED);
						graphics.clear();
						super.paint(graphics);
		            }
		        };

	        }
	        else
	        {
		        fieldManagerStatus = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH)
		        {
		        	public void paint(Graphics graphics)
		            {
		        		graphics.setColor(Color.DARKBLUE);
						graphics.clear();
						super.paint(graphics);
		            }
		        };
	        }
	        fieldManagerStatus.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),
	        		Font.getDefault().getHeight() * 75/100));	// set font size
			if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0)
			{
		        if ( Historia.daysPassed < TRIAL_PERIOD -1)
		        	labelStatus = new LabelField("Trial version, " + (TRIAL_PERIOD - Historia.daysPassed) + " days left",LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
		        else if (Historia.daysPassed == TRIAL_PERIOD - 1)
		        	labelStatus = new LabelField("Trial version, 1 day left",LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
		        else
		        	labelStatus = new LabelField("Trial version expired",LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
			}
			else
			{
				labelStatus = new LabelField("Registered license",LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
			}       
	        fieldManagerStatus.add(labelStatus);
	        fieldManagerMain.add(fieldManagerStatus);
	        fieldManagerMain.add(new SpaceField(Display.getWidth(),spaceHeight));
	      //-------------------------------------------------------------
	        
	        //-------------------- for non registered , add deviceid, key and buttons
	        if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0)
	        {
	 	        fieldManagerButton = new HorizontalFieldManager(HorizontalFieldManager.FIELD_HCENTER);
		        labelDeviceID = new LabelField("Device ID: " + owner ,LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
		        fieldManagerMain.add(labelDeviceID);
		        basicKeyInput = new BasicEditField("Key: ", "",5,BasicEditField.FIELD_HCENTER)
		        {
			        public void layout (int width, int height)
			        {
		
			            super.layout (width, height);
		
			            setExtent (getPreferredWidth(), getPreferredHeight());
		
			        }
			        public int getPreferredWidth()
			        {
			        	return fieldManagerMain.getFont().getAdvance("Key: 00000000");
			        }
			        public int getPreferredHeight()
			        {
			        	return fieldManagerMain.getFont().getHeight();
			        }
			        
	    		};
		        fieldManagerMain.add(basicKeyInput);
		        buttonRegister = new ButtonField("Register",ButtonField.CONSUME_CLICK );
		        fieldManagerButton.add(buttonRegister);
		        buttonTrial = new ButtonField("Trial",ButtonField.CONSUME_CLICK |
		        		((Historia.daysPassed >= TRIAL_PERIOD || Historia.daysPassed < 0 )? ButtonField.NON_FOCUSABLE : 0))
		        {
		        	public boolean isFocusable()
		        	{
		        		return isEditable();
		        	}
		        };
		        fieldManagerButton.add(new LabelField(" "));
		        fieldManagerButton.add(buttonTrial);
		        
		        fieldManagerMain.add(new SpaceField(Display.getWidth(),spaceHeight));
		        fieldManagerMain.add(fieldManagerButton);
	        }    
	      //---------------------------------------------
	        fieldManagerBottom = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH);
	        fieldManagerBottom.setFont(Font.getDefault().derive(Font.getDefault().getStyle(),
	        		13));
		   //-----------------------------copyright field---------------------------
	        labelCopyright = new LabelField("© 2002-2010 SBSH Mobile Software",LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
	        fieldManagerBottom.add(labelCopyright);
	        //-------------------------------------------------------------------------
	        
	        //-----------------------------site field---------------------------
	        activeRichTextFieldCopyright = new ActiveRichTextField("Visit us: http://www.sbsh.net    " , ActiveRichTextField.FIELD_HCENTER)
	        {
		        public void layout (int width, int height)
		        {
	
		            super.layout (width, height);
	
		            setExtent (getPreferredWidth(), getPreferredHeight());
	
		        }
		        public int getPreferredWidth()
		        {
		        	return fieldManagerBottom.getFont().getAdvance("Visit us: http://www.sbsh.net");
		        }
		        public int getPreferredHeight()
		        {
		        	return fieldManagerBottom.getFont().getHeight();
		        }
		        
    		};
//	        fieldManagerSite.add(activeRichTextFieldCopyright);
	        fieldManagerBottom.add(activeRichTextFieldCopyright);
	        //-----------------------------------------------------------------------
	        
	        //-----------------------------support field---------------------------
//	        fieldManagerSupport = new HorizontalFieldManager();
	        activeRichTextFieldCopyright2 = new ActiveRichTextField("Support Team: support@sbsh.net   ", ActiveRichTextField.FIELD_HCENTER)
	        {
		        public void layout (int width, int height)
		        {
	
		            super.layout (width, height);
	
		            setExtent (getPreferredWidth(), getPreferredHeight());
	
		        }
		        public int getPreferredWidth()
		        {
		        	return fieldManagerBottom.getFont().getAdvance("Support Team: support@sbsh.net");
		        }
		        public int getPreferredHeight()
		        {
		        	return fieldManagerBottom.getFont().getHeight();
		        }
		        
    		};
	        fieldManagerBottom.add(activeRichTextFieldCopyright2);
	        setStatus(fieldManagerBottom);
		  //------------------------------------------------------------
	        
	        add(fieldManagerMain);
	        
	        //------------END OF UI--------------------------------------
	        
	        //------------- button listener
	        if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") == 0)
	        {
		        FieldChangeListener listenerRegister = new FieldChangeListener()	//register button
		        {
		        	public void fieldChanged(Field field, int context)
		        	{
		        		 key = 0;   // initialize key
		                 String keyString;
		                 if (basicKeyInput.getText().compareTo("") != 0) //if text is entered in key box
		                 {
		                     // generate key        
		                     rpn();
		                     keyString = Integer.toString(key);
		                     String mask = "00000";	// mask key with leading zeros
		                     keyString = mask.substring(0 , mask.length() - keyString.length()) + keyString;
		                     if (basicKeyInput.getText().compareTo(keyString) == 0) // if key entered is correct
		                     {
		                         //save values to file
		                        Historia.keys.setElementAt(keyString, Historia.KEYNUMBER);
		                        Historia.keys.setElementAt("r", Historia.REGISTERED);
		                        Historia.saveConfiguration();
		                        Dialog.inform("Thanks for registering.");
		                         //go on to main form
		                        customOnClose();
		                     }
		                     else // if key is wrong
		                     {
		                         tryCount--; //decrease trycount
		                         if (tryCount == 0)
		                         {
		                             Dialog.inform("You have reached maximum number of tries, application will now get closed.");
		                             onClose();
		                         }
		                         else
		                        	 Dialog.inform("Wrong key, please try again.");
		                     }
		                 }
		                 else // if key is not inserted
		                 {
		                	 Dialog.inform("Please enter your key.");
		                 }   
		        	};
		        };
		        buttonRegister.setChangeListener(listenerRegister);	// listen to button
		        FieldChangeListener listenerTrial = new FieldChangeListener()	//trial button
		        {
		        	public void fieldChanged(Field field, int context)
		        	{
		        		customOnClose();
		        	}
		        };
		        if(Historia.daysPassed >= TRIAL_PERIOD || Historia.daysPassed < 0 )
		        	buttonTrial.setEditable(false);	// disable button
		        else
		        	buttonTrial.setChangeListener(listenerTrial);	// listen to button
	    	}
	        //---------------------------------
    	}
    	catch(Exception e)
		{
			Dialog.alert("Error in License window, if error persists contact support.\n" + e.getMessage());
			System.exit(1);
		}
    }
    
    public boolean onClose()	// close window
    {
    	if (((String)Historia.keys.elementAt(Historia.REGISTERED)).compareTo("n-r") != 0 ||
    			Historia.daysPassed < TRIAL_PERIOD && Historia.daysPassed >= 0)
    	{
    		this.close();
    		return true;
    	}
    	System.exit(0);
    	return true;
    }
    
    public void customOnClose()	// go to main window
    {
    	UiApplication.getUiApplication().popScreen(this);
    }
    private void rpn()  // c 17 * key 7219 + * 4759 + i +
    {
        for (int i = 0; i < owner.length(); i++)  // run on each char of text
        {
            key += 7219;
            key %= 65536;
            key *= (owner.charAt(i) * 17)%65536; 
            key %= 65536;
            key += (4759 + i)%65536;
            key %= 65536;
        }
    };
    protected void onUndisplay() 	//listen to closed event
    {
    	listener.notifyScreenClosed(this);
    } 
}

