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
								dispatch.logEvent(booking, "Booking completed: " + result);
							} catch (Exception e) {
								dispatch.logEvent(booking, "Error in booking: " + e.getMessage());
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

	public Future<BookingResult> bookPassenger(Passenger waitingPassenger) {
		if (isShutdown) {
			dispatch.logEvent(null, "Booking rejected: Region " + regionName + " is shutting down.");
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

	public boolean isShutdown() {
		return isShutdown;
	}
}