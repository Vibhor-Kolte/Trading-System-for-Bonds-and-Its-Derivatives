package it.softsolutions.nexrates.quoting.common.rfq.fx;

/**
 * Minimal timestamp markers to instrument FX/Swing phases without external deps.
 */
public final class FxPerfMarkers {
	private FxPerfMarkers() {}

	public static void mark(String tag, String rfqId) {
		long now = System.currentTimeMillis();
		// Keep it simple and low-cost
		System.out.println("PERF|" + tag + "|" + rfqId + "|ms=" + now);
	}
}