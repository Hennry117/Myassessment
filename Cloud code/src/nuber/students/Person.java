package nuber.students; // Define the package this class belongs to

/**
 * The Person class represents a person with basic attributes and methods.
 * This class is abstract and meant to be inherited by other classes, such as Passenger.
 */
public abstract class Person {

	// A sample array of names to be used for random name generation
	public final static String[] SAMPLE_NAMES = {
			"Bryan", "Olivia", "Vincent", "Kenneth", "Debra", "Jack",
			"Harold", "Isabella", "Jerry", "Stephen", "Larry", "Ruth",
			"Diane", "Gerald", "Brandon", "Virginia", "Helen", "Gary",
			"Noah", "Michell", "Alexis", "Zachary", "Gregory", "Arthur",
			"Dennis", "Terry", "Rose", "Jeffrey", "Jean", "Jane", "Brenda",
			"Louis", "Mary", "Julia", "Sandra", "Catherine", "Adam",
			"Samantha", "Amber", "Ralp", "Jacob", "Raymond", "Rachel",
			"Kelly", "Danielle", "John", "Melissa", "Albert", "Brian",
			"Eugne", "Jeremy", "Nathan", "Beverly", "Margaret", "Natalie",
			"Charlotte", "Ann", "Betty", "Randy", "Tyler", "Emma", "Willie",
			"Charles", "Lisa", "Anthony", "Sara", "Sean", "James", "Johnny",
			"Jud", "Evelyn", "Theresa", "Gloria", "Emily", "Denise", "Frank",
			"Steven", "Jacqueline", "Diana", "Ronald", "Kayla", "Joe",
			"Nicole", "Scott", "Henry", "Lawrence", "Ethan", "Stephanie",
			"Kevin", "Kathleen", "Angela", "Joyce", "Sarah", "Benjamin",
			"Carl", "Cynthia", "Nicholas", "Andrea", "Robert", "Martha",
			"Susan", "Ryan", "Alexander", "Donna", "Thomas", "Brittany",
			"Timothy", "Hannah", "Heather", "Linda", "Joan", "Pamela",
			"Maria", "Kyle", "Logan", "Paul", "Andrew", "Dylan", "Christina",
			"Kimberly", "Patricia", "Victoria", "Philip", "Shirley",
			"Billy", "Jonathan", "Roy", "Christopher", "Roger", "Anna",
			"Richard", "Doris", "Bruce", "Peter", "Dorothy", "Amanda",
			"Marilyn", "Christine", "Marie", "Karen", "Jordan", "Wayne",
			"Edward", "Justin", "Walter", "Rebecca", "Sharon", "Jesse",
			"Joshua", "Sophia", "Grace", "Deborah", "Ashley", "Joseph",
			"Matthew", "Alan", "Julie", "Abigail", "Mark", "Megan",
			"Juan", "Michael", "Frances", "George", "Eric", "William",
			"Cheryl", "Daniel", "Katherine", "Amy", "Laura", "Donald",
			"Jennifer", "Judith", "Carolyn", "Christian", "Janice",
			"Barbara", "Elijah", "Nancy", "Aaron", "Teresa", "Bobby",
			"Douglas", "Russell", "Jose", "Keith", "Kathryn", "Samuel",
			"Austin", "Jason", "Jessica", "David", "Lauren", "Patrick",
			"Gabriel", "Alice", "Elizabeth", "Madison", "Carol"
	};

	// Index for the next random name
	private static int nextNameIndex = 0;

	// The person's name
	public String name;

	// Maximum sleep time for the passenger
	protected int maxSleep;

	/**
	 * Constructor to create a new Person instance.
	 *
	 * @param name     The name of the person
	 * @param maxSleep The maximum sleep time
	 */
	public Person(String name, int maxSleep) {
		this.name = name; // Initialize the name
		this.maxSleep = maxSleep; // Initialize the maximum sleep time
	}

	/**
	 * Get the name of the person.
	 *
	 * @return The name of the person
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the maximum sleep time for the passenger.
	 *
	 * @return The maximum sleep time
	 */
	public int getMaxSleep() {
		return maxSleep;
	}

	/**
	 * Get a random name, cycling through the SAMPLE_NAMES array.
	 *
	 * @return A random name
	 */
	public static String getRandomName() {
		nextNameIndex = ++nextNameIndex % SAMPLE_NAMES.length; // Update the index and ensure it's within array bounds
		return SAMPLE_NAMES[nextNameIndex]; // Return the next random name
	}
}
