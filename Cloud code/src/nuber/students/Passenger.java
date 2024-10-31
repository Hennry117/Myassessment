package nuber.students; // Define the package of this class

/**
 * The Passenger class represents a passenger, extending the Person class.
 */
public class Passenger extends Person {

	/**
	 * Constructor to create a new Passenger instance.
	 *
	 * @param name     The name of the passenger.
	 * @param maxSleep The maximum sleep time (travel time) for the passenger.
	 */
	public Passenger(String name, int maxSleep) {
		super(name, maxSleep); // Call the constructor of the parent class, Person
	}

	/**
	 * Get the travel time for the passenger.
	 *
	 * @return The travel time of the passenger, a random integer between 0 and maxSleep.
	 */
	public int getTravelTime() {
		return (int) (Math.random() * maxSleep); // Generate and return a random travel time
	}
}
