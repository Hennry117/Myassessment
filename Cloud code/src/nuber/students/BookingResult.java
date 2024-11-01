package nuber.students; // Define the package this class belongs to

public class BookingResult { // Define the BookingResult class

	public int jobID; // Declare an integer variable jobID to store the job ID
	public Passenger passenger; // Declare a Passenger type variable passenger to store passenger information
	public Driver driver; // Declare a Driver type variable driver to store driver information
	public long tripDuration; // Declare a long variable tripDuration to store trip duration (in milliseconds)

	// Constructor to initialize the attributes of the BookingResult object
	public BookingResult(int jobID, Passenger passenger, Driver driver, long tripDuration) {
		this.jobID = jobID; // Assign the passed jobID to the jobID member variable
		this.passenger = passenger; // Assign the passed passenger to the passenger member variable
		this.driver = driver; // Assign the passed driver to the driver member variable
		this.tripDuration = tripDuration; // Assign the passed tripDuration to the tripDuration member variable
	}
}
