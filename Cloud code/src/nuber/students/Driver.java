package nuber.students;

import java.util.concurrent.ThreadLocalRandom;

public class Driver extends Person {

	private Passenger currentPassenger;
	private Booking booking;

	/**
	 * Constructor for the Driver class.
	 *
	 * @param driverName Name of the driver.
	 * @param maxSleep   Maximum delay in milliseconds to simulate.
	 */
	public Driver(String driverName, int maxSleep) {
		super(driverName, maxSleep);
	}

	/**
	 * Stores the provided passenger as the driver's current passenger and then sleeps the thread for a random duration.
	 *
	 * @param newPassenger The passenger to pick up.
	 * @throws InterruptedException If the thread is interrupted during sleep.
	 */
	public void pickUpPassenger(Passenger newPassenger) throws InterruptedException {
		this.currentPassenger = newPassenger;
		int delay = ThreadLocalRandom.current().nextInt(0, maxSleep + 1);
		logEvent("Picking up " + newPassenger.getName() + ". Delay: " + delay + " ms.");
		Thread.sleep(delay);
	}

	/**
	 * Sleeps the thread for a duration determined by the current passenger's getTravelTime method.
	 *
	 * @throws InterruptedException If the thread is interrupted during sleep.
	 */
	public void driveToDestination() throws InterruptedException {
		if (currentPassenger == null) {
			throw new IllegalStateException("No current passenger to deliver.");
		}
		int travelTime = currentPassenger.getTravelTime();
		logEvent("Driving " + currentPassenger.getName() + " to destination. Travel time: " + travelTime + " ms.");
		Thread.sleep(travelTime);
	}

	/**
	 * Logs an event if logging is enabled.
	 *
	 * @param message The message to be logged.
	 */
	private void logEvent(String message) {
		if (booking != null && booking.dispatch != null) {
			booking.dispatch.logEvent(booking, message);
		} else {
			System.out.println(message);
		}
	}
}