package it.softsolutions.nexrates.quoting.common.rfq.fx;

import javafx.application.Platform;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Common JavaFX helpers: ensure code runs on FX thread and coalesce repeated tasks by key.
 */
public final class FXUtils {
	private FXUtils() {}

	/**
	 * Run the given task on the FX Application Thread. If already on FX, runs immediately.
	 */
	public static void runOnFxThread(Runnable task) {
		if (task == null) return;
		if (Platform.isFxApplicationThread()) {
			task.run();
		} else {
			Platform.runLater(task);
		}
	}

	// Simple coalescing per-key: last-wins scheduling
	private static final ConcurrentHashMap<Object, Runnable> pendingByKey = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Object, AtomicBoolean> scheduledByKey = new ConcurrentHashMap<>();

	/**
	 * Schedule a task to run on the FX thread, coalescing by key so that only the latest
	 * task for that key runs once per pulse. Useful for high-frequency updates per RFQ id.
	 */
	public static void runLaterCoalesced(Object key, Runnable task) {
		if (key == null || task == null) return;
		pendingByKey.put(key, task);
		AtomicBoolean flag = scheduledByKey.computeIfAbsent(key, k -> new AtomicBoolean(false));
		if (flag.compareAndSet(false, true)) {
			Platform.runLater(() -> {
				try {
					Runnable latest = pendingByKey.remove(key);
					if (latest != null) {
						latest.run();
					}
				} finally {
					flag.set(false);
					// If another task snuck in after we grabbed the latest, schedule again
					if (pendingByKey.containsKey(key)) {
						runLaterCoalesced(key, Objects.requireNonNull(pendingByKey.remove(key)));
					}
				}
			});
		}
	}
}