package nuber.students;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NuberDispatch {

	private static final int MAX_DRIVERS = 999;
	private final boolean logEvents;
	private final BlockingQueue<Driver> idleDrivers = new LinkedBlockingQueue<>();

	public NuberDispatch(Map<String, Integer> regionInfo, boolean logEvents) {
		this.logEvents = logEvents;
		this.executorService = Executors.newFixedThreadPool(MAX_DRIVERS);

		for (Map.Entry<String, Integer> entry : regionInfo.entrySet()) {
			String regionName = entry.getKey();
			int maxBookings = entry.getValue();
			regions.put(regionName, new NuberRegion(this, regionName, maxBookings));
		}
	}


	private final Map<String, NuberRegion> regions = new HashMap<>();
	private final AtomicInteger bookingsAwaitingDriver = new AtomicInteger(0);
	private final ExecutorService executorService;

	public synchronized boolean addDriver(Driver newDriver) {
		if (idleDrivers.size() < MAX_DRIVERS) {
			idleDrivers.add(newDriver);
			return true;
		}
		return false;
	}

	public Driver getAvailableDriver() throws InterruptedException {
		return idleDrivers.take();
	}

	public void addAvailableDriver(Driver driver) {
		idleDrivers.offer(driver);
	}

	public void logEvent(Booking booking, String message) {
		if (logEvents) {
			System.out.println(booking + ": " + message);
		}
	}

	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		NuberRegion nuberRegion = regions.get(region);
		if (nuberRegion == null || nuberRegion.isShutdown()) {
			return CompletableFuture.completedFuture(null);
		}

		bookingsAwaitingDriver.incrementAndGet();

		return executorService.submit(() -> {
			Booking booking = new Booking(this, passenger);
			return booking.call();
		});
	}

	public int getBookingsAwaitingDriver() {
		return bookingsAwaitingDriver.get();
	}

	public void shutdown() {
		regions.values().forEach(NuberRegion::shutdown);
		executorService.shutdown();

		try {
			if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
				executorService.shutdownNow();
			}
		} catch (InterruptedException e) {
			executorService.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}