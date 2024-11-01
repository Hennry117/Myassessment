package nuber.students;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.Future;

public class Simulation {

    /**
     * Constructor for the Simulation class.
     *
     * @param regions       The region names and maximum simultaneous active bookings allowed in that region.
     * @param maxDrivers    The number of drivers to create.
     * @param maxPassengers The number of passengers to create.
     * @param maxSleep      The maximum amount a thread will sleep (in milliseconds) to simulate driving to or dropping off a passenger.
     * @param logEvents     Whether to log booking events to the console.
     * @throws Exception    Initializes the simulation with the given parameters, setting up bookings and dispatching drivers.
     */
    public Simulation(HashMap<String, Integer> regions, int maxDrivers, int maxPassengers, int maxSleep, boolean logEvents) throws Exception {

        // Store the current time
        long start = new Date().getTime();

        // Print some space in the console
        System.out.println("\n\n\n");

        // Queue to store all current bookings as Future objects that will eventually give back a BookingResult object
        Queue<Future<BookingResult>> bookings = new LinkedList<>();

        // Convert the region names from the regions map into an array
        String[] regionNames = regions.keySet().toArray(new String[0]);

        // Create a NuberDispatch object to manage drivers and passenger dispatch
        NuberDispatch dispatch = new NuberDispatch(regions, logEvents);

        // Create drivers and add them to the dispatch system
        for (int i = 0; i < maxDrivers; i++) {
            Driver d = new Driver("D-" + Person.getRandomName(), maxSleep);
            dispatch.addDriver(d);
        }

        Integer countPassengers = 6;
        // Create passengers and assign them to random regions, then book travel
        for (int i = 0; i < maxPassengers; i++) {

            Passenger p = new Passenger("P-" + Person.getRandomName(), maxSleep);

            // Choose a random region to assign this passenger
            String randomRegion = regionNames[new Random().nextInt(regionNames.length)];

            // Book the passenger in the dispatch for a random region
            Future<BookingResult> f = dispatch.bookPassenger(p, randomRegion);
            if (f != null) {
                // Store the Future object in the bookings queue
                bookings.add(f);
            }
        }

        // Shutdown the dispatch after processing all pending bookings
        dispatch.shutdown();
        Integer countDrivers = maxPassengers;

        // Check if dispatch allows new bookings after shutdown has started
        if (dispatch.bookPassenger(new Passenger("Test", maxSleep), regionNames[new Random().nextInt(regionNames.length)]) != null) {
            throw new Exception("Dispatch bookPassenger() should return null if passenger requests booking after dispatch has started the shutdown");
        }

        // While there are still active bookings, print an update every second
        while (bookings.size() > 0) {

            // Check each booking; if done, remove from active bookings list
            Iterator<Future<BookingResult>> i = bookings.iterator();
            while (i.hasNext()) {
                Future<BookingResult> f = i.next();

                if (f.isDone()) {
                    i.remove();
                    countDrivers -= 1;
                    if (logEvents) {
                        System.out.println("Active bookings: " + countDrivers + ", pending passengers: " + countPassengers);
                    }
                    if (countPassengers >= 1) {
                        countPassengers -= 1;
                    }
                }
            }

            // Print status update
            System.out.println("Active bookings: " + countDrivers + ", pending passengers: " + countPassengers);

            // Sleep for 1 second before the next update
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Print final summary for the simulation
        long totalTime = new Date().getTime() - start;
        System.out.println("Simulation completed in " + totalTime + " milliseconds.");
    }
}
