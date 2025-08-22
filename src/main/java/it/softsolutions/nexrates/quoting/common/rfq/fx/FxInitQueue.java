package it.softsolutions.nexrates.quoting.common.rfq.fx;

import javafx.application.Platform;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Ensures JavaFX initialization tasks (Scene creation, FXML load, bindings, CSS)
 * run one-at-a-time on the FX Application Thread to prevent starvation when many RFQs arrive.
 */
public final class FxInitQueue {
	private static final BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();
	private static final AtomicBoolean draining = new AtomicBoolean(false);

	private FxInitQueue() {}

	public static void submit(Runnable fxInitTask) {
		if (fxInitTask == null) return;
		queue.add(fxInitTask);
		drain();
	}

	private static void drain() {
		if (!draining.compareAndSet(false, true)) return;
		Platform.runLater(() -> {
			try {
				Runnable task;
				while ((task = queue.poll()) != null) {
					try {
						task.run();
					} catch (Throwable t) {
						// Swallow to keep draining; callers should log/handle inside tasks as needed
					}
				}
			} finally {
				draining.set(false);
				// If items were added during drain, loop again
				if (!queue.isEmpty()) drain();
			}
		});
	}
}