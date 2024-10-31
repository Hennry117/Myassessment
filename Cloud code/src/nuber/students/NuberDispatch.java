package nuber.students;

import java.util.*; // Import the collections framework
import java.util.concurrent.*; // Import concurrent utilities
import java.util.concurrent.atomic.AtomicInteger; // Import AtomicInteger class

public class NuberDispatch { // Define the NuberDispatch class

	private final int MAX_DRIVERS = 999; // Define the maximum number of drivers
	private boolean logEvents = false; // Flag to log events
	private final BlockingQueue<Driver> idleDrivers = new LinkedBlockingQueue<>(); // Blocking queue to store idle drivers
	private final HashMap<String, NuberRegion> regions = new HashMap<>(); // HashMap to store region information
	private final AtomicInteger bookingsAwaitingDriver = new AtomicInteger(0); // Counter for bookings awaiting drivers
	private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_DRIVERS); // Create a fixed thread pool

	/**
	 * Constructor for the NuberDispatch class.
	 *
	 * @param regionInfo HashMap containing region names and maximum booking numbers.
	 * @param logEvents  Flag indicating whether to log events.
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
		this.logEvents = logEvents; // Initialize the logging flag
		for (Map.Entry<String, Integer> entry : regionInfo.entrySet()) { // Traverse region information
			String regionName = entry.getKey(); // Get the region name
			int maxBookings = entry.getValue(); // Get the maximum bookings
			regions.put(regionName, new NuberRegion(this, regionName, maxBookings)); // Create and store NuberRegion instance
		}
	}

	/**
	 * Add a new driver to the idle driver queue.
	 *
	 * @param newDriver The driver to add
	 * @return Returns true if the driver was added successfully, otherwise false.
	 */
	public synchronized boolean addDriver(Driver newDriver) {
		if (idleDrivers.size() < MAX_DRIVERS) { // Check if the number of idle drivers exceeds the max limit
			idleDrivers.add(newDriver); // Add the driver to the idle queue
			return true; // Return success
		} else {
			return false; // Return failure
		}
	}

	/**
	 * Get an available driver, blocking if no drivers are available.
	 *
	 * @return An available driver
	 * @throws InterruptedException if the thread is interrupted while waiting.
	 */
	public synchronized Driver getAvailableDriver() throws InterruptedException {
		while (idleDrivers.isEmpty()) { // If there are no idle drivers
			wait(); // Wait until a driver is available
		}
		notifyAll(); // Wake up all waiting threads
		return idleDrivers.poll(); // Return an idle driver
	}

	/**
	 * Re-adds the driver to the idle queue and wakes up waiting threads.
	 *
	 * @param driver The driver to add
	 */
	public synchronized void addAvailableDriver(Driver driver) {
		idleDrivers.add(driver); // Add the driver to the idle queue
		notifyAll(); // Wake up all waiting threads
	}

	/**
	 * Logs an event if logging is enabled.
	 *
	 * @param booking  Related booking information.
	 * @param message  The message to log.
	 */
	public void logEvent(Booking booking, String message) {
		if (!logEvents) return; // If logging is not enabled, return immediately
		System.out.println(booking + ": " + message); // Print the log message
	}

	/**
	 * Book a passenger in a specified region.
	 *
	 * @param passenger Passenger information.
	 * @param region    The booking region.
	 * @return A Future object representing the booking result.
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		NuberRegion nuberRegion = regions.get(region); // Get the specified region
		if (nuberRegion == null || nuberRegion.isShutdown()) { // Check if the region is valid
			return null; // Return null if the region is invalid or shut down
		}
		bookingsAwaitingDriver.incrementAndGet(); // Increase the count of bookings awaiting a driver
		Callable<BookingResult> callable = () -> { // Create a Callable object to handle the booking
			Booking booking = new Booking(this, passenger); // Create a booking object
			return booking.call(); // Execute the booking and return the result
		};
		return executorService.submit(callable); // Submit the task to the thread pool and return a Future
	}

	/**
	 * Get the current number of bookings awaiting a driver.
	 *
	 * @return The number of bookings awaiting a driver.
	 */
	public int getBookingsAwaitingDriver() {
		return bookingsAwaitingDriver.get(); // Return the current count of bookings awaiting a driver
	}

	/**
	 * Shuts down the dispatch service, stopping all regions and closing the thread pool.
	 */
	public void shutdown() {
		for (NuberRegion region : regions.values()) {
			region.shutdown(); // Shut down all regions
		}
		executorService.shutdown(); // Shut down the thread pool
		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) { // Wait for the thread pool to terminate
				executorService.shutdownNow(); // Forcefully shut down the thread pool
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow(); // Handle interrupted exception by forcing shutdown
			Thread.currentThread().interrupt(); // Reset the thread's interrupt status
		}
	}
}
