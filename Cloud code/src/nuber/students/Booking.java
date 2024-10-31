package nuber.students; // Define the package this class belongs to

import java.util.Date; // Import the Date class to get the current time
import java.util.concurrent.Callable; // Import the Callable interface to define an asynchronous task
import java.util.concurrent.atomic.AtomicInteger; // Import AtomicInteger for thread-safe counting

public class Booking implements Callable<BookingResult> { // Define the Booking class, implementing Callable with a BookingResult return type

	final NuberDispatch dispatch; // Declare a final variable dispatch, representing a dispatch object
	private final Passenger passenger; // Declare a final variable passenger, representing a passenger object
	private long startTime; // Declare a variable startTime to record the booking start time
	private long endTime; // Declare a variable endTime to record the booking end time
	private static AtomicInteger jobCounter = new AtomicInteger(0); // Static counter to generate a unique job ID

	// Constructor, initializing dispatch and passenger, recording the start time
	public Booking(NuberDispatch dispatch, Passenger passenger) {
		this.dispatch = dispatch; // Assign the passed dispatch to the member variable
		this.passenger = passenger; // Assign the passed passenger to the member variable
		this.startTime = new Date().getTime(); // Get the current timestamp and assign it to startTime
	}

	public Passenger getPassenger() { // Get the passenger object
		return passenger; // Return the passenger object
	}

	@Override
	public BookingResult call() throws Exception { // Implement the call method of the Callable interface
		// 1. Ask Dispatch for an available driver
		Driver driver = null; // Declare a Driver type variable driver, initialized to null
		// Loop to get an available driver until successfully obtained
		while ((driver = dispatch.getAvailableDriver()) == null) {
			synchronized (this) { // Enter a synchronized block to ensure thread safety
				wait(); // Wait until there is an available driver
			}
		}

		// Pass the current Booking object to the Driver
		driver = new Driver(driver.getName(), driver.getMaxSleep()); // Create a new Driver object, copying the driver's name and max sleep time

		// 2. Call Driver.pickUpPassenger()
		driver.pickUpPassenger(passenger); // Call the driver's pickUpPassenger method to pick up the passenger

		// 3. Call Driver.driveToDestination()
		driver.driveToDestination(); // Call the driver's driveToDestination method to drive to the destination

		// 4. Record the end time
		endTime = new Date().getTime(); // Get the current timestamp and assign it to endTime, recording the end time

		// 5. Add the driver back to the available list
		dispatch.addAvailableDriver(driver); // Add the driver object back to the available driver list

		// 6. Return the BookingResult with an integer jobID
		return new BookingResult( // Create and return a BookingResult object
				jobCounter.incrementAndGet(), // Use the counter to generate a unique jobID
				passenger, // Pass the passenger object to BookingResult
				driver, // Pass the driver object to BookingResult
				(endTime - startTime) // Calculate and pass the booking duration (in milliseconds)
		);
	}
}
