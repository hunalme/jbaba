package hasan.throttler;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import hasan.throttler.SlidingWindow;
import hasan.throttler.SlidingWindow.ThrottleResult;

class SlidingWindowTest {

	@Test
	void test_allowed_when_max_val_not_reached() {
		SlidingWindow t = new SlidingWindow(1, 1);
		assertTrue(t.isAllowed());
	}

	@Test
	void test_reject_when_max_val_reached() {
		SlidingWindow t = new SlidingWindow(1, 1);
		assertTrue(t.isAllowed());
		assertEquals(1, t.getCurrentCount());
		assertFalse(t.isAllowed());
	}

	@Test
	void test_reject_when_max_val_reached2() {
		SlidingWindow t = new SlidingWindow(3, 6);
		t.allowed();
		t.allowed();
		t.allowed();
		t.slide();

		t.allowed();
		t.allowed();
		boolean allowed = t.isAllowed();
		assertTrue(allowed);

		allowed = t.isAllowed();
		assertFalse(allowed);
	}

	@Test
	void test_slide_shifts_window() {
		SlidingWindow t = new SlidingWindow(5, 100);
		t.allowed();
		t.allowed();
		assertEquals(2, t.getCurrentCount());

		t.slide();
		assertEquals(0, t.getWindow()[0]);
		assertEquals(2, t.getWindow()[1]);
	}

	@Test
	void test_slide_shifts_last_value_out_of_window() {
		SlidingWindow t = new SlidingWindow(3, 100);
		t.allowed();
		t.slide();

		t.allowed();
		t.allowed();
		t.slide();

		t.allowed();
		t.allowed();
		t.allowed();
		assertArrayEquals(new int[] { 3, 2, 1 }, t.getWindow());
		t.slide();

		t.allowed();
		t.allowed();
		t.allowed();
		t.allowed();
		assertArrayEquals(new int[] { 4, 3, 2 }, t.getWindow());
	}

	@Test
	void test_instance_limit() {
		SlidingWindow t = new SlidingWindow(5, 10, 3);
		t.allowed();
		t.allowed();
		assertEquals(2, t.getCurrentCount());
		t.slide();

		t.allowed();
		t.allowed();
		t.allowed();
		assertEquals(3, t.getCurrentCount());
		t.slide();

		t.allowed();
		t.allowed();
		t.allowed();
		assertEquals(ThrottleResult.REJECTED_CURRENT_LIMIT, t.allowed());
		assertEquals(3, t.getCurrentCount());
		assertArrayEquals(new int[] { 3, 3, 2, 0, 0 }, t.getWindow());
	}
	
	@Test
	void test_reinitialize_expand() {
		SlidingWindow t = new SlidingWindow(5, 10, 3);
		t.allowed();
		t.allowed();
		t.slide();

		t.allowed();
		t.allowed();
		t.allowed();
		t.slide();

		t.allowed();
		t.allowed();
		t.allowed();
		assertArrayEquals(new int[] { 3, 3, 2, 0, 0 }, t.getWindow());
		
		t.initialize(6, 10, 4);
		assertArrayEquals(new int[] { 3, 3, 2, 0, 0, 0 }, t.getWindow());
	}

	@Test
	void test_reinitialize_shrink() {
		SlidingWindow t = new SlidingWindow(5, 10, 3);
		t.allowed();
		t.allowed();
		t.slide();
		
		t.allowed();
		t.allowed();
		t.allowed();
		t.slide();
		
		t.allowed();
		t.allowed();
		t.allowed();
		assertArrayEquals(new int[] { 3, 3, 2, 0, 0 }, t.getWindow());
		
		t.initialize(4, 10, 4);
		assertArrayEquals(new int[] { 3, 3, 2, 0 }, t.getWindow());
	}
	
	@Test
	void test_reinitialize_shrink_values() {
		SlidingWindow t = new SlidingWindow(5, 10, 3);
		t.allowed();
		t.allowed();
		t.slide();
		
		t.allowed();
		t.allowed();
		t.allowed();
		t.slide();
		
		t.allowed();
		t.allowed();
		t.allowed();
		assertArrayEquals(new int[] { 3, 3, 2, 0, 0 }, t.getWindow());
		
		t.initialize(2, 10, 4);
		assertArrayEquals(new int[] { 3, 3 }, t.getWindow());
		
		assertTrue(t.isAllowed());
		assertFalse(t.isAllowed());
	}
}
