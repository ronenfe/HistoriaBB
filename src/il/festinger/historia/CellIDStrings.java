package il.festinger.historia;


public class CellIDStrings	// class for each call strings
{
	private int cellID;
	private String location;
	CellIDStrings(int cellID, String location)	// constructor to store data in object
	{
		this.cellID = cellID;
		this.location = location;
	};
	CellIDStrings()	// default constructor
	{
	};

	public void setLocation(String location)	// create a new record from call log
	{
		this.location = location;
	}
	public String toString()	// used in writing to csv
	{
		return String.valueOf(cellID) + location;
	}
	public int getCellID()	// create a new record from call log
	{
		return cellID;
	}
	public String getLocation()	// create a new record from call log
	{
		return location;
	}
}