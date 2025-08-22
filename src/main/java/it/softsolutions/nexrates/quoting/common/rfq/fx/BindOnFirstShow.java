package it.softsolutions.nexrates.quoting.common.rfq.fx;

import javafx.application.Platform;

import java.awt.Component;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Defers JavaFX bindings until the hosting Swing component is first shown,
 * avoiding hidden RFQs initializing FX bindings.
 */
public final class BindOnFirstShow {
	private BindOnFirstShow() {}

	public static void bindOnFirstShow(Component component, Runnable bindAll) {
		if (component == null || bindAll == null) return;
		AtomicBoolean done = new AtomicBoolean(false);
		HierarchyListener[] holder = new HierarchyListener[1];
		holder[0] = new HierarchyListener() {
			@Override public void hierarchyChanged(HierarchyEvent e) {
				if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && component.isShowing()) {
					if (done.compareAndSet(false, true)) {
						component.removeHierarchyListener(this);
						Platform.runLater(bindAll);
					}
				}
			}
		};
		component.addHierarchyListener(holder[0]);
	}
}