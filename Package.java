import java.io.Serializable;

/**
 * The class Package contains the routing information and the tracking number of the 
 * package to be sent in the system. This class is serializable hence in this program we send 
 * an instance of this object to the GPSOffice object which unpacks it and routes it to
 * the correct destination. 
 * Whenever the Customer contacts the GPSOffice object to deliver a package, the message is 
 * packed in an instance of this class, which is then sent over the entire network.
 * 
 * @author Hitesh Chidambar Kotian(hxk6871).
 *
 */
public class Package implements Serializable {

	/**
	 * Coordinates of the destination to which this package is to be sent.
	 */
	private final double destinationX;
	private final double destinationY;
	
	/**
	 * Tracking number of the package that is set by the GPSOffice object.
	 */
	private final long trackingNumber;

	/**
	 * Constructor that sets the values for all the variables.
	 * 
	 * @param destinationX
	 * 		x coordinate of the destination.
	 * @param destinationY
	 * 		y coordinate of the destination.
	 */
	public Package(double destinationX, double destinationY) {
		this.destinationX = destinationX;
		this.destinationY = destinationY;
		trackingNumber = System.currentTimeMillis();
	}

	/**
	 * Function returning the x coordinate of the destination to which
	 * the package is to be sent.
	 * 
	 * @return double
	 * 		The x coordinate of the destination.
	 */
	public double getX() {
		return destinationX;
	}

	/**
	 * Function returning the y coordinate of the destination to which
	 * the package is to be sent.
	 * 
	 * @return double
	 * 		The y coordinate of the destination.
	 */
	public double getY() {
		return destinationY;
	}

	/**
	 * Function returning the tracking number of the package.
	 * 
	 * @return long
	 * 		The tracking number of the package.
	 */
	public long getTrackingNumber() {
		return trackingNumber;
	}
}