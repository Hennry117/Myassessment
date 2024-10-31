package nuber.students;

import java.util.*; // Import the collections framework
import java.util.concurrent.*; // Import the concurrency utilities package
import java.util.concurrent.atomic.AtomicInteger; // Import the atomic integer class

public class NuberDispatch { // Define the NuberDispatch class

	private final int MAX_DRIVERS = 999; // Define the maximum number of drivers
	private boolean logEvents = false; // Flag for logging events
	private final BlockingQueue<Driver> idleDrivers = new LinkedBlockingQueue<>(); // Blocking queue to store idle drivers
	private final HashMap<String, NuberRegion> regions = new HashMap<>(); // HashMap to store region information
	private final AtomicInteger bookingsAwaitingDriver = new AtomicInteger(0); // Count of bookings waiting for a driver
	private final ExecutorService executorService = Executors.newFixedThreadPool(MAX_DRIVERS); // Create a fixed thread pool

	/**
	 * Constructor for the NuberDispatch class.
	 *
	 * @param regionInfo HashMap containing region names and maximum bookings.
	 * @param logEvents  Flag for logging events.
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents) {
		this.logEvents = logEvents; // Initialize the logging flag
		for (Map.Entry<String, Integer> entry : regionInfo.entrySet()) { // Iterate through region information
			String regionName = entry.getKey(); // Get the region name
			int maxBookings = entry.getValue(); // Get the maximum bookings
			regions.put(regionName, new NuberRegion(this, regionName, maxBookings)); // Create and store a NuberRegion instance
		}
	}

	/**
	 * Add a new driver to the idle drivers queue.
	 *
	 * @param newDriver The driver to be added
	 * @return true if added successfully, otherwise false.
	 */
	public synchronized boolean addDriver(Driver newDriver) {
		if (idleDrivers.size() < MAX_DRIVERS) { // Check if idle driver count exceeds maximum
			idleDrivers.add(newDriver); // Add driver to idle queue
			return true; // Return success
		} else {
			return false; // Return failure
		}
	}

	/**
	 * Retrieve an available driver, blocking if no driver is available.
	 *
	 * @return An available driver
	 * @throws InterruptedException if the thread is interrupted while waiting.
	 */
	public synchronized Driver getAvailableDriver() throws InterruptedException {
		while (idleDrivers.isEmpty()) { // If no drivers are idle
			wait(); // Wait until a driver is available
		}
		notifyAll(); // Wake up all waiting threads
		return idleDrivers.poll(); // Return an idle driver
	}

	/**
	 * Re-add a driver to the idle queue and wake up waiting threads.
	 *
	 * @param driver The driver to be added
	 */
	public synchronized void addAvailableDriver(Driver driver) {
		idleDrivers.add(driver); // Add driver to idle queue
		notifyAll(); // Wake up all waiting threads
	}

	/**
	 * Log an event if logging is enabled.
	 *
	 * @param booking  Associated booking information.
	 * @param message  Message to be logged.
	 */
	public void logEvent(Booking booking, String message) {
		if (!logEvents) return; // If logging is not enabled, return
		System.out.println(booking + ": " + message); // Print the log message
	}

	/**
	 * Book a passenger in the specified region.
	 *
	 * @param passenger Passenger information.
	 * @param region    Booking region.
	 * @return A Future object representing the booking result.
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		NuberRegion nuberRegion = regions.get(region); // Get the specified region
		if (nuberRegion == null || nuberRegion.isShutdown()) { // Check if region is valid
			return null; // If region is invalid or shut down, return null
		}
		bookingsAwaitingDriver.incrementAndGet(); // Increment bookings waiting for a driver
		Callable<BookingResult> callable = () -> { // Create a Callable object to handle booking
			Booking booking = new Booking(this, passenger); // Create a booking object
			return booking.call(); // Execute booking and return the result
		};
		return executorService.submit(callable); // Submit the task to the thread pool and return the Future
	}

	/**
	 * Get the current count of bookings awaiting a driver.
	 *
	 * @return The count of bookings waiting for a driver.
	 */
	public int getBookingsAwaitingDriver() {
		return bookingsAwaitingDriver.get(); // Return the current bookings waiting for a driver
	}

	/**
	 * Shut down the dispatch service, stopping all regions and closing the thread pool.
	 */
	public void shutdown() {
		for (NuberRegion region : regions.values()) {
			region.shutdown(); // Shut down all regions
		}
		executorService.shutdown(); // Shut down the thread pool
		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) { // Wait for thread pool to terminate
				executorService.shutdownNow(); // Forcefully shut down the thread pool
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow(); // Handle interruption by forcing shut down
			Thread.currentThread().interrupt(); // Reset thread's interrupt status
		}
	}
}
