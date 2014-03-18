import java.rmi.Remote;
import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.NotBoundException;
import java.rmi.RemoteException;

/**
 * The Java RMI remote interface to the class GPSOffice.
 * 
 * @author Hitesh Chidambar Kotian(hxk6871)
 * 
 */
public interface GPSInterface extends Remote {

	/**
	 * Function that computes the nearest neighbor to the given destination of
	 * the packet. If the current node is nearest to the destination then
	 * directly route the packet to the destination. Otherwise forward it to the
	 * nearest node amongst the neighbors.
	 * 
	 * @param packet
	 *            Package object that has to be delivered.
	 * @param nodeListener
	 *            RemoteEventListener object.
	 * 
	 * @exception RemoteException
	 *                thrown when a remote error is encountered.
	 * 
	 * @exception NotBoundException
	 *                thrown when the object looked up in the registry is not
	 *                present.
	 */
	public void routeMessage(Package packet,
			RemoteEventListener<DeliveryEvent> nodeListener)
			throws RemoteException;

	/**
	 * Remote function that returns the name of the city in which the GPSOffice
	 * object is located in.
	 * 
	 * @return String Name of the city in which the GPSOFfice object is located
	 *         in.
	 * 
	 * @exception RemoteException
	 *                thrown if a remote error is encountered.
	 */
	public String getName() throws RemoteException;

	/**
	 * Remote function that returns the X-Coordinate of the GPSOffice.
	 * 
	 * @return double X-coordinate of the GPSOFfice object is located in.
	 * 
	 * @exception RemoteException
	 *                thrown if a remote error is encountered.
	 */
	public double getX() throws RemoteException;

	/**
	 * Remote function that returns the Y-Coordinate of the GPSOffice.
	 * 
	 * @return double Y-coordinate of the GPSOFfice object is located in.
	 * 
	 * @exception RemoteException
	 */
	public double getY() throws RemoteException;

	/**
	 * Function that sends a lease object to the RemoteEventListener objects so
	 * that they can intercept any RemoteEvents generated by the GPSOffice
	 * object.
	 * 
	 * @param listener
	 *            Object reference to the RemoteEventListener class object.
	 * 
	 * @return Lease A Lease object that sets up a connection between the
	 *         listener and the GPSOffice object.
	 * 
	 * @exception RemoteException
	 *                Thrown when a remote error occurs.
	 */
	public Lease addListener(RemoteEventListener<DeliveryEvent> listener)
			throws RemoteException;

	/**
	 * Function that is called by the Customer when it wishes to deliver a
	 * packet. This function wraps up all the parameters of the package in a
	 * Package object and returns it to the Customer object.
	 * 
	 * @param X
	 *            X-coordinate of the destination.
	 * @param Y
	 *            Y-coordinate of the destination.
	 * 
	 * @return Package The package object created by the object.
	 * 
	 * @exception RemoteException
	 *                Thrown when a remote error occurs.
	 */
	public Package deliverPackage(double X, double Y) throws RemoteException;

}