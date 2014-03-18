import edu.rit.ds.RemoteEvent;

/**
 * Delivery Event class that is exchanged between the RemoteEventGenerator and
 * RemoteEventListener to send and receive messages respectively.
 * The GPSOffice object creates a new object of this class everytime it generates a
 * new RemoteEvent. The Customer and Headquarters object intercepts this object and 
 * displays it in it's terminal.
 * 
 * @author Hitesh Chidambar Kotian(hxk6871)
 */
public class DeliveryEvent extends RemoteEvent {
	
	/**
	 *  The delivery notification sent form the GPSIffice object to the client.
	 */
	public final String deliveryNotification;
	
	/**
	 *  Tracking number of the package whose notification is sent by the GPSOffice
	 *  object.
	 */
	public final long trackingNumber;

	/**
	 *  A constant flag that determines the status of a packet.
	 *  0 - The package is still in transit.
	 *  1 - The package has successfully reached it's destination.
	 *  2 - The package has been dropped.
	 */
	public final int deliverySuccess;

	/**
	 * Constructor that initializes the properties of the class.
	 * 
	 * @param notification
	 * 		The event notification message.
	 * @param trackingNumber
	 * 		The tracking number of the package.
	 * @param delivery
	 * 		The status of delivery of the package.
	 */
	public DeliveryEvent(String notification, long trackingNumber, int delivery) {
		this.deliveryNotification = notification;
		this.trackingNumber = trackingNumber;
		this.deliverySuccess = delivery;
	}
}