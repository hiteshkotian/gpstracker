import edu.rit.ds.registry.RegistryProxy;
import edu.rit.ds.RemoteEventListener;
import java.rmi.server.UnicastRemoteObject;
import edu.rit.ds.registry.RegistryEventListener;
import edu.rit.ds.registry.RegistryEvent;
import java.rmi.RemoteException;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryEventFilter;

/**
 * Class Customer is the customer side of the GPS Office system. The customer in
 * this systems is any end user who wishes to send package from a GPS office to
 * a place whose coordinates are given. The Customer class intercepts all the
 * RemoteEvents generated by the GPSOffice and checks if the details are about
 * the package it sent and if it is so then it prints out the details of the
 * package in route. The Customer instance's program life cycle ends when the
 * package has safely reached it's destination.
 * 
 * Class Customer also has a main program that takes the user input.
 * Usage() : java Customer <host> <port> <name> <X> <Y>. 
 * <host> : Name of the host computer where the Registry Server is running. 
 * <port> : Port number to which the Registry Server is listening. 
 * <name> : Name of the city where the originating GPS office is located. 
 * <X> : Package's Destination X coordinate.
 * <Y> : Package's Destination X coordinate.
 */

public class Customer {

	/**
	 * RegistryProxy object.
	 */
	private static RegistryProxy registry;

	/**
	 * RemoteEventListener object to listen to any DeliveryEvent that the
	 * GPSOffice is generating.
	 */
	private static RemoteEventListener<DeliveryEvent> nodeListener;

	/**
	 * Final tracking number of the package sent by the customer.
	 */
	public long trackingNumber;

	/**
	 * Package object of the query sent by the customer.
	 */
	private Package packet;

	/**
	 * Method that instantiates the RegistryProxy object and calls the
	 * routeMessage() function of the GPSOffice object from which the user
	 * wishes to send the package.
	 * 
	 * @param host
	 *            Name of the host computer where the RegistryServer is running.
	 * @param port
	 *            Port number to which the Registry Server is listening.
	 * @param city
	 *            City from where the user wishes to send the package.
	 * 
	 * @exception RemoteException
	 *                Thrown when an exception is encountered in the server
	 *                side.
	 * @exception NotBoundException
	 *                Thrown when the GPSOffice for the given city is not
	 *                registered in the registry.
	 */
	public void sendPackage(String host, int port, String city,
			double destinationX, double destinationY,
			RemoteEventListener<DeliveryEvent> nodeListener) {
		try {
			// Create an instantiate the RegistryProxy for the given host and
			// port. Then get the object reference to the GPSOffice object of
			// the city
			// selected by the user and call the deliverPackage() function which
			// returns a package
			// which in return the Customer sends to the GPSOffice object for
			// delivery.
			GPSInterface gpsObj = (GPSInterface) registry.lookup(city);
			this.packet = gpsObj.deliverPackage(destinationX, destinationY);
			this.trackingNumber = this.packet.getTrackingNumber();
			gpsObj.routeMessage(this.packet, nodeListener);
		} catch (RemoteException e) {
			System.out.println(e.getMessage());
		} catch (NotBoundException e) {
			System.out.println(city + " does not have "
					+ "a GPS office registered");
			System.exit(1);
		}
	}

	/**
	 * Customer class main program.
	 */
	public static void main(String[] args) throws Exception {

		// If the required number of arguments are not entered by the user,
		// then print the usage for the Customer class and exit.
		if (args.length != 5) {
			usage();
		}

		// Get all the values from the command line argument.
		String host = args[0];
		int port = parseInteger(args[1], "port");
		String name = args[2];
		double destinationX = parseDouble(args[3], "X");
		double destinationY = parseDouble(args[4], "Y");

		// Create an instance of the Customer class.
		final Customer custom = new Customer();

		// Instantiate the RegistryProxy object on the host and port specified
		// by the customer.
		registry = new RegistryProxy(host, port);

		// Instantiate the RemoteEventListener to listen to any events of type
		// DeliveryEvent.
		nodeListener = new RemoteEventListener<DeliveryEvent>() {
			public void report(long seqnum, DeliveryEvent event) {
				// Print the delivery notification.
				System.out.println(event.deliveryNotification);
				// Exit the program if the message has been delivered or has
				// been dropped.
				if (event.deliverySuccess == 1) {
					System.exit(0);
				} else if (event.deliverySuccess == 2) {
					System.exit(1);
				}

			}
		};
		try {
			UnicastRemoteObject.exportObject(nodeListener, 0);
		} catch (RemoteException exc) {
			exc.printStackTrace();
		}
		// Call the sendPackage() method to send the package to the destination.
		custom.sendPackage(host, port, name, destinationX, destinationY,
				nodeListener);
	}

	/**
	 * Function that looks up the object sent as an argument, in the registry
	 * and registers itself as an EventListener.
	 * 
	 * @param objectName
	 *            Name of the city of the GPSOffice.
	 * 
	 * @exception RemoteException
	 *                thrown when a remote error is encountered.
	 * 
	 * @exception NotBoundException
	 *                thrown when the given object is not registered in the
	 *                registry.
	 */
	private static void listenToNode(String objectName) {
		try {
			GPSInterface gpsNode = (GPSInterface) registry.lookup(objectName);
			gpsNode.addListener(nodeListener);
		} catch (NotBoundException exc) {
			System.err.println(objectName + " does not have "
					+ "a GPS office registered");
		} catch (RemoteException exc) {
			System.out.println(exc.getMessage());
		}
	}

	/**
	 * Function that returns a string that tells the user how the program is
	 * expected to be compiled and also explains what each command line argument
	 * stands for.
	 */
	public static void usage() {
		String usageString = "\nUsage() : " + "java Customer <host> "
				+ "<port> <name> <X> <Y>\n";
		usageString += "<host> : Name of the host computer where"
				+ " the Registry Server is running.\n";
		usageString += "<port> : Port number to which the"
				+ " Registry Server is listening.\n";
		usageString += "<name> : Name of the city where the originating"
				+ " GPS office is located.\n";
		usageString += "<X> : Package's Destination X coordinate.\n";
		usageString += "<Y> : Package's Destination X coordinate.\n";
		System.out.println(usageString);
		System.exit(1);
	}

	/**
	 * Function that converts the string parameter to an integer and returns the
	 * value.
	 * 
	 * @parameter value String value to be parsed as an integer.
	 * @parameter arg The name of the argument for which the value is parsed as
	 *            an integer.
	 * 
	 * @return int The integer value after being parsed from the string.
	 * 
	 * @exception NumberFormatException
	 *                Thrown when the String value cannot be parsed as an
	 *                Integer.
	 */
	public static int parseInteger(String argument, String value) {
		try {
			return Integer.parseInt(argument);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("<" + value + "> = " + argument
					+ " : Is not an Integer");
		}
	}

	/**
	 * Function that converts the string parameter to an integer and returns the
	 * value.
	 * 
	 * @parameter value String value to be parsed as an integer.
	 * @parameter arg The name of the argument for which the value is parsed as
	 *            an integer.
	 * 
	 * @return int The integer value after being parsed from the string.
	 * 
	 * @exception NumberFormatException
	 *                Thrown when the String value cannot be parsed as an
	 *                Integer.
	 */
	public static double parseDouble(String argument, String value) {
		try {
			return Double.parseDouble(argument);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("<" + value + "> = " + argument
					+ " : Is not of type Double");
		}
	}
}