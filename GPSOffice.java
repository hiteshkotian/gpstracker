import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import edu.rit.ds.Lease;
import edu.rit.ds.RemoteEventGenerator;
import edu.rit.ds.RemoteEventListener;
import edu.rit.ds.registry.AlreadyBoundException;
import edu.rit.ds.registry.NotBoundException;
import edu.rit.ds.registry.RegistryProxy;

/**
 * The GPSOffice represents each node in the GPSOffice system. The GPSOffice
 * system contains one or more objects of this class. The Customer sends a
 * package to any one of the GPSOffice object which then routes it to the
 * neighbor nearest to the destination of the package or routes it directly to
 * the destination if the node itself is closer to the destination as compared
 * to it's neighbors.
 * 
 * Class GPSOffoce has a parameterized constructor that the Start class of
 * rit.edu.ds package considers as the main program for the class. 
 * Usage() : java Start GPSOffice <host> <port> <name> <X> <Y>. 
 * <host> : Name of the host computer where the Registry Server is running. 
 * <port> : Port number to which the Registry Server is listening.
 * <name> : Name of the city where GPSOffice object is located. 
 * <X> : GPSOffice's X coordinate. 
 * <Y> : GPSOffice's Y coordinate.
 * 
 * @author Hitesh Chidambar Kotian (hxk6871).
 * 
 */
public class GPSOffice implements GPSInterface {

	/**
	 * Name of the city in which the object is located.
	 */
	final private String name;

	/**
	 * Address of the host of the registry.
	 */
	private String host;

	/**
	 * Port at which the registry is listening.
	 */
	private int port;

	/**
	 * X coordinate of the object.
	 */
	private double X;

	/**
	 * Y coordinate of the object.
	 */
	private double Y;

	/**
	 * Instance of the RegistryProxy.
	 */
	private static RegistryProxy registry;

	/**
	 * Instance of a remote event generator that will notify all the remote
	 * events of any remote events that have taken place.
	 */
	private RemoteEventGenerator<DeliveryEvent> eventGenerator;

	/**
	 * Instance of ScheduledExecutorService that will create a thread pool for
	 * the client requests.
	 */
	private ScheduledExecutorService threadPool;

	/**
	 * HashMap storing the names of the recent neighbors computed to their x, y
	 * coordinates and their distance from the current neighbor.
	 */
	HashMap<String, Double[]> neighbors;

	/**
	 * 
	 * GPSOffice Constructor Creates and instance of the GPSOffice which is
	 * exported in the registry server of the GPS system. The command line
	 * arguments are : <host> - Name of the host computer where the Registry
	 * Server is running. <port> - Port number to which the Registry Server is
	 * listening. <name> - Name of the city where the GPS office is located. <X>
	 * - GPS office's X coordinate. <Y> - GPS office's Y coordinate.
	 * 
	 * @param args
	 *            Command line arguments.
	 * 
	 * @exception AlreadyBoundException
	 *                Thrown if there is a GPSOffice office object in the
	 *                registry with the same name.
	 * @exception RemoteException
	 *                Thrown if a remote error is encountered.
	 */
	public GPSOffice(String[] args) {
		// Check if all the arguments have been supplied by the user.
		// If not then throw an exception.
		if (args.length != 5) {
			usage();
		}

		// Get all the variable values if all the values have been given.
		host = args[0];
		port = parseInt(args[1], "port");
		name = args[2];
		X = parseDouble(args[3], "X");
		Y = parseDouble(args[4], "Y");

		// Create an object of the RegistryProxy and export the current object
		try {
			registry = new RegistryProxy(host, port);

		} catch (RemoteException e) {
			System.err.println("Registry Already bound on " + host + ", "
					+ port);
			System.exit(1);
		}

		// Initialize the RemoteEventGenerator object which will report and
		// event of class DeliveryEvent.
		eventGenerator = new RemoteEventGenerator<DeliveryEvent>();

		// Bind the current object in the registry.
		try {
			UnicastRemoteObject.exportObject(this, 0);
			registry.bind(this.name, this);
		} catch (AlreadyBoundException exc) {
			try {
				UnicastRemoteObject.unexportObject(this, true);
			} catch (NoSuchObjectException exc2) {
			}
			System.err.println("GPSOffice() : <name> = \"" + this.name
					+ "\" already exists");
			System.exit(1);
		} catch (RemoteException exc) {
			System.err
					.println("Remote Error while binding the object in the registry");
			System.exit(1);
		}

		// Initialize the ScheduledExecutorThread object for the GPSOffice
		// object.
		threadPool = Executors.newSingleThreadScheduledExecutor();

		// Initialize the neighbors hashmap.
		neighbors = new HashMap<String, Double[]>();
	}

	/**
	 * Function that returns a string that tells the user how the program is
	 * expected to be compiled and also explains what each command line argument
	 * stands for.
	 */
	public static void usage() {
		String usageString = "\nUsage() : " + "java Start GPSOffice <host> "
				+ "<port> <name> <X> <Y>\n";
		usageString += "<host> : Name of the host computer where"
				+ " the Registry Server is running.\n";
		usageString += "<port> : Port number to which the"
				+ " Registry Server is listening.\n";
		usageString += "<name> : Name of the city where the GPS"
				+ " office is located.\n";
		usageString += "<X> : GPS office's X coordinate.\n";
		usageString += "<Y> : GPS office's Y coordinate.\n";
		System.out.println(usageString);
		System.exit(1);
	}

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
	public String getName() throws RemoteException {
		return this.name;
	}

	/**
	 * Remote function that returns the X-Coordinate of the GPSOffice.
	 * 
	 * @return double X-coordinate of the GPSOFfice object is located in.
	 * 
	 * @exception RemoteException
	 *                thrown if a remote error is encountered.
	 */
	public double getX() throws RemoteException {
		return this.X;
	}

	/**
	 * Remote function that returns the Y-Coordinate of the GPSOffice.
	 * 
	 * @return double Y-coordinate of the GPSOFfice object is located in.
	 * 
	 * @exception RemoteException
	 */
	public double getY() throws RemoteException {
		return this.Y;
	}

	/**
	 * Function that computes the neighbors for the current node and returns the
	 * neighbor closest to the destination x and y coordinates.
	 * 
	 * @param x
	 *            X-coordinate of the destination.
	 * @param y
	 *            Y-coordinate of the destination.
	 * 
	 * @return Name of the node which is near the destination.
	 * 
	 * @exception RemoteException
	 *                Thrown when a remote exception is encountered.
	 * 
	 * @exception NotBoundException
	 *                Thrown when a node name is not registered in the registry.
	 */
	public String getNextNeighbor(double x, double y) {
		String minNode = null;
		String nodeName = null;
		try {
			// Get the list of all the nodes registered in the registry.
			List<String> nodeList = registry.list();
			Iterator<String> node = nodeList.iterator();
			// Compute the nearest neighbors.
			while (node.hasNext()) {
				// For all nodes except the current node and the nodes already
				// present in the hashmap calculate the distance and add the
				// nearest 3 neighbors in the hashmap.
				nodeName = node.next();
				if (nodeName.equals(this.name)
						|| neighbors.containsKey(nodeName)) {
					continue;
				}
				GPSInterface nodeRef = null;
				try {
					nodeRef = (GPSInterface) registry.lookup(nodeName);
				} catch (RemoteException e) {
					System.out.println("see here");
				}
				String currName;
				try {
					currName = nodeRef.getName();
				} catch (RemoteException e) {
					// In case of a remote exception ignore the node and
					// continue with the other nodes.
					continue;
				}
				double distance = this.getDistance(nodeRef);
				// If there are less than 3 nodes in the hashmap simply add the
				// node entry in the hashmap.
				if (neighbors.size() < 3) {
					Double[] params = { nodeRef.getX(), nodeRef.getY(),
							distance };
					neighbors.put(nodeRef.getName(), params);
				}
				// If there are already three nodes entries in the hashmap
				// remove the node entry with the maximum distance.
				else {
					String maximumNode = nodeName;
					double maxDistance = distance;
					Set<String> neighborNames = neighbors.keySet();
					Iterator<String> myIterator = neighborNames.iterator();
					String currentNode = null;
					while (myIterator.hasNext()) {
						currentNode = myIterator.next();
						Double[] params = neighbors.get(currentNode);
						double distance1 = params[2];
						if (distance1 > maxDistance) {
							maximumNode = currentNode;
							maxDistance = distance1;
						}
					}
					if (neighbors.containsKey(maximumNode)) {
						neighbors.remove(maximumNode);
						Double[] params = { nodeRef.getX(), nodeRef.getY(),
								distance };
						neighbors.put(nodeRef.getName(), params);

					}
				}
			}

			// Compute the distance between the node and the destination.
			double minDistance = this.getDistance(X, x, Y, y);
			minNode = this.name;
			Set<String> neighborNames = neighbors.keySet();
			Iterator<String> neighbor = neighborNames.iterator();
			// From all the current neighbors get the neighbor which is near the
			// destination.
			while (neighbor.hasNext()) {
				nodeName = neighbor.next();
				Double[] param = neighbors.get(nodeName);
				double newDistance = this.getDistance(x, param[0], y, param[1]);
				if (newDistance < minDistance) {
					minDistance = newDistance;
					minNode = nodeName;
				}
			}
		} catch (RemoteException e) {
			System.out.println("Remote Exception");
		} catch (NotBoundException e) {
			System.out.println("Not Bound Exception for " + minNode);
		}
		return minNode;
	}

	/**
	 * Computes the distance between a GPSOffice object and the current office
	 * object.
	 * 
	 * @param gpsOffice
	 *            GPSOffice object.
	 * @return The distance between the current office object and the GPSOffice
	 *         object.
	 * @throws RemoteException
	 *             Thrown when a remote exception is encountered.
	 */
	private double getDistance(GPSInterface gpsOffice) throws RemoteException {
		double xDiff = Math.pow(this.X - gpsOffice.getX(), 2);
		double yDiff = Math.pow(this.Y - gpsOffice.getY(), 2);
		double distance = Math.sqrt(xDiff + yDiff);
		return distance;
	}

	/**
	 * Computes the distance between two points.
	 * 
	 * @param x1
	 *            X-coordinate of the first point.
	 * @param x2
	 *            X-coordinate of the second point.
	 * @param y1
	 *            Y-coordinate of the first point.
	 * @param y2
	 *            Y-coordinate of the second point.
	 * 
	 * @return Distance between the two points.
	 * 
	 * @throws RemoteException
	 *             Thrown when a remote exception is encountered.
	 */
	private double getDistance(double x1, double x2, double y1, double y2)
			throws RemoteException {
		double xDiff = Math.pow(x1 - x2, 2);
		double yDiff = Math.pow(y1 - y2, 2);
		double distance = Math.sqrt(xDiff + yDiff);
		return distance;
	}

	/**
	 * Function that computes the nearest neighbor to the given destination of
	 * the packet. If the current node is nearest to the destination then
	 * directly route the packet to the destination. Otherwise forward it to the
	 * nearest node amongst the neighbors.
	 * 
	 * @param packet
	 *            Package object that has to be delivered.
	 * @param nodeListener
	 *            The RemoteEventListener of the customer object sending the
	 *            packet.
	 * @exception RemoteException
	 *                thrown when a remote error is encountered.
	 * 
	 * @exception NotBoundException
	 *                thrown when the object looked up in the registry is not
	 *                present.
	 */
	public void routeMessage(final Package packet,
			final RemoteEventListener<DeliveryEvent> nodeListener)
			throws RemoteException {
		// Notify the RemoteEventListener objects that the package has reached
		// the GPSOffice.
		eventGenerator.reportEvent(new DeliveryEvent("Package number "
				+ packet.getTrackingNumber() + " arrived at " + this.name
				+ " office", packet.getTrackingNumber(), 0));
		try {
			nodeListener.report(
					0,
					new DeliveryEvent("Package number "
							+ packet.getTrackingNumber() + " arrived at "
							+ this.name + " office",
							packet.getTrackingNumber(), 0));
		} catch (RemoteException e) {
		}

		// Store the name of the GPSOffice object returned by the
		// getNearestNeighbor() function of the RoutingTable.
		final String destination = this.getNextNeighbor(packet.getX(),
				packet.getY());

		if (destination == null) {
			eventGenerator.reportEvent(new DeliveryEvent("Packet number "
					+ packet.getTrackingNumber() + " lost by " + destination,
					packet.getTrackingNumber(), 2));

			nodeListener.report(
					0,
					new DeliveryEvent("Packet number "
							+ packet.getTrackingNumber() + " lost by "
							+ destination,// this.name,
							packet.getTrackingNumber(), 2));
		}
		// Make the thread sleep for 3 seconds.
		try {
			Thread.sleep(3000);
		} catch (InterruptedException exc) {
			return;
		}

		final String currentName = this.name;
		// If the current node is nearest to the destination then directly route
		// the package to the destination.
		if (destination.equals(this.name)) {
			eventGenerator.reportEvent(new DeliveryEvent("Package number "
					+ packet.getTrackingNumber() + " delivered from "
					+ this.name + " office to (" + packet.getX() + ", "
					+ packet.getY() + ")", packet.getTrackingNumber(), 1));
			try {
				nodeListener.report(0, new DeliveryEvent("Package number "
						+ packet.getTrackingNumber() + " delivered from "
						+ this.name + " office to (" + packet.getX() + ", "
						+ packet.getY() + ")", packet.getTrackingNumber(), 1));
			} catch (RemoteException e) {
			}

		}
		// If the current GPSOffice is not the nearest node then forward the
		// package to the node that is nearest to the destination.
		// We use a threadpool to send all the requests to the next node.
		else {

			// Get the object reference to the next node from the
			// registry server.
			try {
				final GPSInterface routingNode = (GPSInterface) registry
						.lookup(destination);
				// Notify the RemoteEventListener objects that the package has
				// departed
				// the GPSOffice.
				eventGenerator.reportEvent(new DeliveryEvent("Package "
						+ packet.getTrackingNumber() + " departed from "
						+ currentName + " office", packet.getTrackingNumber(),
						0));

				try {
					nodeListener
							.report(0,
									new DeliveryEvent("Package "
											+ packet.getTrackingNumber()
											+ " departed from " + currentName
											+ " office", packet
											.getTrackingNumber(), 0));
				} catch (RemoteException e) {
				}

				threadPool.execute(new Runnable() {
					public void run() {
						try {
							// Route the message to the node.
							routingNode.routeMessage(packet, nodeListener);
						}
						// If an exception is caught here, that means the
						// GPSOffice object has crashed.
						// In such cases notify the RemoteEventListeners that
						// the package has been dropped
						// and also broadcast a message to all the GPSOffice
						// objects to recompute their neighbors
						// and remove the crashed GPSOffice object from their
						// tables.
						catch (Exception e) {
							eventGenerator.reportEvent(new DeliveryEvent(
									"Packet number "
											+ packet.getTrackingNumber()
											+ " lost by " + currentName, packet
											.getTrackingNumber(), 2));
							try {
								nodeListener.report(0,
										new DeliveryEvent("Packet number "
												+ packet.getTrackingNumber()
												+ " lost by " + currentName,
												packet.getTrackingNumber(), 2));
							} catch (RemoteException e1) {
							}
							neighbors.remove(destination);

						}
					}
				});
			} catch (NotBoundException exc) {
				// If a not bound exception is encountered then remove the gps
				// office
				// entry from the neighbor table and recompute the neighbors and
				// forward the package.
				neighbors.remove(destination);
				final String nextneighbor = this.getNextNeighbor(packet.getX(),
						packet.getY());

				if (nextneighbor.equals(this.name)) {
					eventGenerator.reportEvent(new DeliveryEvent(
							"Package number " + packet.getTrackingNumber()
									+ " delivered from " + this.name
									+ " office to (" + packet.getX() + ", "
									+ packet.getY() + ")", packet
									.getTrackingNumber(), 1));
					try {
						nodeListener.report(
								0,
								new DeliveryEvent("Package number "
										+ packet.getTrackingNumber()
										+ " delivered from " + this.name
										+ " office to (" + packet.getX() + ", "
										+ packet.getY() + ")", packet
										.getTrackingNumber(), 1));
					} catch (RemoteException e) {
					}

				}

				try {

					final GPSInterface routingNode = (GPSInterface) registry
							.lookup(nextneighbor);

					eventGenerator.reportEvent(new DeliveryEvent("Package "
							+ packet.getTrackingNumber() + " departed from "
							+ currentName + " office", packet
							.getTrackingNumber(), 0));

					try {
						nodeListener.report(0, new DeliveryEvent("Package "
								+ packet.getTrackingNumber()
								+ " departed from " + currentName + " office",
								packet.getTrackingNumber(), 0));
					} catch (RemoteException e) {
					}

					threadPool.execute(new Runnable() {
						public void run() {
							try {
								// Route the message to the node.
								routingNode.routeMessage(packet, nodeListener);
							}
							// If an exception is caught here, that means the
							// GPSOffice object has crashed.
							// In such cases notify the RemoteEventListeners
							// that
							// the package has been dropped
							// and also broadcast a message to all the GPSOffice
							// objects to recompute their neighbors
							// and remove the crashed GPSOffice object from
							// their
							// tables.
							catch (Exception e) {
								eventGenerator.reportEvent(new DeliveryEvent(
										"Packet number "
												+ packet.getTrackingNumber()
												+ " lost by " + currentName
												+ " office", packet
												.getTrackingNumber(), 2));
								try {
									nodeListener.report(
											0,
											new DeliveryEvent(
													"Packet number "
															+ packet.getTrackingNumber()
															+ " lost by "
															+ currentName
															+ " office",
													packet.getTrackingNumber(),
													2));
								} catch (RemoteException e1) {
								}
								neighbors.remove(nextneighbor);

							}
						}
					});
				} catch (NotBoundException exc1) {
					System.out.println(destination + " is not bound exception");
				}
			}
		}

	}

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
	public Package deliverPackage(double X, double Y) throws RemoteException {
		Package packet = new Package(X, Y);
		return packet;
	}

	/**
	 * Function that converts the string parameter to an integer and returns the
	 * value.
	 * 
	 * @param value
	 *            String value to be parsed as an integer.
	 * @param arg
	 *            The name of the argument for which the value is parsed as an
	 *            integer.
	 * 
	 * @return int The integer value after being parsed from the string.
	 * 
	 * @exception NumberFormatException
	 *                Thrown when the String value cannot be parsed as an
	 *                Integer.
	 */
	public static int parseInt(String value, String arg) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("GPSOffice() : Invalid <" + arg
					+ ">: \"" + value + "\"");
		}
	}

	/**
	 * Function that converts the string parameter to a double and returns the
	 * value.
	 * 
	 * @param value
	 *            String value to be parsed as double.
	 * @param arg
	 *            The name of the argument for which the value is parsed as an
	 *            integer.
	 * 
	 * @return double The double value after being parsed from the string.
	 * 
	 * @exception NumberFormatException
	 *                Thrown when the String value cannot be parsed as a double.
	 */
	public static double parseDouble(String value, String arg) {
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException exc) {
			throw new IllegalArgumentException("GPSOffice() : Invalid <" + arg
					+ ">: \"" + value + "\"");
		}
	}

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
			throws RemoteException {
		return eventGenerator.addListener(listener);
	}

}