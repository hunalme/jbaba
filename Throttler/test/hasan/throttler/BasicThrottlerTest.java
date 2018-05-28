package hasan.throttler;

import java.util.Random;

import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hasan.throttler.BasicThrottler;

class BasicThrottlerTest {
	private static Logger logger = LoggerFactory.getLogger(BasicThrottlerTest.class);
	
	public static void main(String[] args) throws Exception {
		DOMConfigurator.configureAndWatch("./log4j.xml");
//		instanceLimitTest();
//		totalLimitTest();
		expandTest();
	}

	private static void expandTest() throws Exception {
		BasicThrottler throttler = new BasicThrottler(10, 100, 0);
		throttler.start();

		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 300; i++) {
			boolean allowed = throttler.isAllowed();
			logger.debug("{} - {}", i, allowed);
			Thread.sleep(50 + random.nextInt(50));
		}

		throttler.reinitializeWindow(20, 200, 15);

		for (int i = 0; i < 300; i++) {
			boolean allowed = throttler.isAllowed();
			logger.debug("{} - {}", i, allowed);
			Thread.sleep(50 + random.nextInt(50));
		}
		
		throttler.stop();
	}

	private static void totalLimitTest() throws Exception {
		BasicThrottler throttler = new BasicThrottler(10, 100);
		throttler.start();

		Random random = new Random(System.currentTimeMillis());
		for (int i = 0; i < 1000; i++) {
			boolean allowed = throttler.isAllowed();
			logger.debug("{} - {}", i, allowed);
			Thread.sleep(50 + random.nextInt(50));
		}

		throttler.stop();
	}

	private static void instanceLimitTest() throws InterruptedException {
		BasicThrottler throttler = new BasicThrottler(10, 100, 15);
		throttler.start();

		for (int i = 0; i < 1000; i++) {
			boolean allowed = throttler.isAllowed();
			logger.debug("{} - {}", i, allowed);
			Thread.sleep(50);
		}

		throttler.stop();
	}
}
