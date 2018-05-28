package hasan.throttler;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import hasan.throttler.SlidingWindow.ThrottleResult;

public class BasicThrottler {
	private static Logger logger = org.slf4j.LoggerFactory.getLogger(BasicThrottler.class);
	private int windowSize;
	private int totalMaxCount;
	private int instanceMaxCount;
	private long period;
	private TimeUnit unit;

	private SlidingWindow window;
	private ScheduledExecutorService scheduledExecutorService;
	private ScheduledFuture<?> scheduledFuture;

	public BasicThrottler(int windowSize, int totalMaxCount) {
		this(windowSize, totalMaxCount, 0, 1, TimeUnit.SECONDS);
	}

	public BasicThrottler(int windowSize, int totalMaxCount, int instanceMaxCount) {
		this(windowSize, totalMaxCount, instanceMaxCount, 1, TimeUnit.SECONDS);
	}

	public BasicThrottler(int windowSize, int totalMaxCount, long period, TimeUnit unit) {
		this(windowSize, totalMaxCount, 0, period, unit);
	}

	public BasicThrottler(int windowSize, int totalMaxCount, int instanceMaxCount, long period, TimeUnit unit) {
		this.windowSize = windowSize;
		this.totalMaxCount = totalMaxCount;
		this.instanceMaxCount = instanceMaxCount;
		this.period = period;
		this.unit = unit;
	}

	public void start() {
		window = new SlidingWindow(windowSize, totalMaxCount, instanceMaxCount);
		scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new BasicThrottlerThreadFactory());
		scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
			logger.debug("before sliding: {}", Arrays.toString(window.getWindow()));
			window.slide();
			logger.debug("after sliding: {}", Arrays.toString(window.getWindow()));
		}, period, period, unit);
	}

	public void stop() {
		scheduledFuture.cancel(true);
		scheduledExecutorService.shutdown();
		window.clean();
	}

	public boolean isAllowed() {
		return window.isAllowed();
	}

	public ThrottleResult allowed() {
		return window.allowed();
	}

	public void reinitializeWindow(int windowSize, int totalMaxCount, int instanceMaxCount) {
		window.initialize(windowSize, totalMaxCount, instanceMaxCount);
	}

	private static class BasicThrottlerThreadFactory implements ThreadFactory {
		@Override
		public Thread newThread(Runnable r) {
			return new Thread(r, "WindowResetter");
		}
	}
}
