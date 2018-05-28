package hasan.throttler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlidingWindow {
	private static Logger logger = LoggerFactory.getLogger(SlidingWindow.class);
	
	enum ThrottleResult {
		ALLOWED, REJECTED_CURRENT_LIMIT, REJECTED_TOTAL_LIMIT;
	}
	private int windowSize;
	private int totalMaxCount;
	private int instanceMaxCount;
	private int[] window;

	public SlidingWindow(int windowSize, int totalMaxCount) {
		this(windowSize, totalMaxCount, 0);
	}

	public SlidingWindow(int windowSize, int totalMaxCount, int instanceMaxCount) {
		initialize(windowSize, totalMaxCount, instanceMaxCount);
	}

	public synchronized void initialize(int windowSize, int totalMaxCount, int instanceMaxCount) {
		this.windowSize = windowSize;
		this.totalMaxCount = totalMaxCount;
		this.instanceMaxCount = instanceMaxCount;
		if (!isInitialized()) {
			this.window = new int[windowSize];
		} else {
			this.window = copyWindow();
		}
	}

	private int[] copyWindow() {
		int[] newWindow = new int[windowSize];
		System.arraycopy(window, 0, newWindow, 0, Math.min(window.length, newWindow.length));
		return newWindow;
	}

	private boolean isInitialized() {
		return window != null;
	}

	public synchronized boolean isAllowed() {
		return allowed() == ThrottleResult.ALLOWED;
	}
	
	public synchronized ThrottleResult allowed() {
		if (!checkCurrentLimit()) {
			//reject
			logger.debug("Rejected by current limit");
			return ThrottleResult.REJECTED_CURRENT_LIMIT;
		}
		if (!checkTotalLimit()) {
			//reject
			logger.debug("Rejected by total limit");
			return ThrottleResult.REJECTED_TOTAL_LIMIT;
		}
		// allow
		window[0]++;
		logger.debug("allowed");
		return ThrottleResult.ALLOWED;
	}

	private boolean checkTotalLimit() {
		return total() < totalMaxCount;
	}

	private boolean checkCurrentLimit() {
		return instanceMaxCount == 0 || window[0] < instanceMaxCount;
	}

	private int total() {
		int sum = 0;
		for (int i = 0; i < windowSize; i++) {
			sum += window[i];
		}
		return sum;
	}

	public synchronized void slide() {
		logger.trace("sliding window");
		for (int i = windowSize - 1; i > 0; i--) {
			window[i] = window[i - 1];
		}
		window[0] = 0;
	}

	public void clean() {
		logger.trace("cleaning window");
		for (int i = 0; i < windowSize; i++) {
			window[i] = 0;
		}
	}

	public int getWindowSize() {
		return windowSize;
	}

	public int getCurrentCount() {
		return window[0];
	}

	public int getTotalMaxCount() {
		return totalMaxCount;
	}

	public int getInstanceMaxCount() {
		return instanceMaxCount;
	}

	public int[] getWindow() {
		return window;
	}
}