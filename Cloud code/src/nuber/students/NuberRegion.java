package nuber.students;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NuberRegion {

	private final NuberDispatch dispatch;
	private final String regionName;
	private final int maxSimultaneousJobs;
	private final BlockingQueue<Booking> bookingsQueue = new LinkedBlockingQueue<>();
	private final ExecutorService executorService;
	private final AtomicInteger activeBookings = new AtomicInteger(0);
	private volatile boolean isShutdown = false;

	/**
	 * Constructor for the NuberRegion class.
	 *
	 * @param dispatch          The dispatch service reference.
	 * @param regionName        The name of the region.
	 * @param maxSimultaneousJobs The maximum number of simultaneous jobs.
	 */
	public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs) {
		this.dispatch = dispatch;
		this.regionName = regionName;
		this.maxSimultaneousJobs = maxSimultaneousJobs;
		this.executorService = Executors.newFixedThreadPool(maxSimultaneousJobs);

		Thread bookingProcessor = new Thread(() -> {
			while (!isShutdown) {
				try {
					Booking booking = bookingsQueue.take();
					if (activeBookings.incrementAndGet() <= maxSimultaneousJobs) {
						executorService.submit(() -> {
							try {
								BookingResult result = booking.call();
								dispatch.logEvent(booking, "Booking completed for " + booking.getPassenger().getName() + ": " + result);
							} catch (Exception e) {
								dispatch.logEvent(booking, "Error processing booking for " + booking.getPassenger().getName() + ": " + e.getMessage());
							} finally {
								activeBookings.decrementAndGet();
							}
						});
					} else {
						bookingsQueue.put(booking);
						activeBookings.decrementAndGet();
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		});
		bookingProcessor.setDaemon(true);
		bookingProcessor.start();
	}

	/**
	 * Books a ride for a passenger.
	 *
	 * @param waitingPassenger The waiting passenger.
	 * @return A Future representing the booking result.
	 */
	public Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
		if (isShutdown) {
			dispatch.logEvent(null, "Booking request rejected: " + regionName + " is shutting down.");
			return null;
		}

		Booking booking = new Booking(dispatch, waitingPassenger);
		CompletableFuture<BookingResult> future = new CompletableFuture<>();

		bookingsQueue.add(booking);

		executorService.submit(() -> {
			try {
				BookingResult result = booking.call();
				future.complete(result);
			} catch (Exception e) {
				future.completeExceptionally(e);
			}
		});

		return future;
	}

	/**
	 * Shuts down the region and releases resources.
	 */
	public void shutdown() {
		isShutdown = true;
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

	/**
	 * Gets the shutdown status of the region.
	 *
	 * @return True if the region is shut down, false otherwise.
	 */
	public boolean isShutdown() {
		return isShutdown;
	}
}