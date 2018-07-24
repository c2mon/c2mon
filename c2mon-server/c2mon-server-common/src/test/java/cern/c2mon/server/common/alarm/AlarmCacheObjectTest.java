package cern.c2mon.server.common.alarm;

import static org.junit.Assert.*;

import org.junit.Test;

public class AlarmCacheObjectTest {

	@Test
	public void testOscillation() {
		// First case : simulation active state changes within period of time
		// - assert that the oscillation is detected
		AlarmCacheObject aco = new AlarmCacheObject();

		for (int i = 0; i < 7; i++) {
			if (i % 2 == 0) {
				aco.setState(AlarmCondition.ACTIVE);
			} else {
				aco.setState(AlarmCondition.TERMINATE);
			}
		}
		assertTrue(aco.isOscillating());

		aco = new AlarmCacheObject();

		// Second case : no oscillation if less than 5 state changes
		for (int i = 0; i < 5; i++) {
			if (i % 2 == 0) {
				aco.setState(AlarmCondition.ACTIVE);
			} else {
				aco.setState(AlarmCondition.TERMINATE);
			}
		}
		assertFalse(aco.isOscillating());

		// Third case : reset after some time

		aco = new AlarmCacheObject();

		// Second case : no oscillation if less than 5 state changes
		for (int i = 0; i < 5; i++) {
			try {
				Thread.sleep(14000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (i % 2 == 0) {
				aco.setState(AlarmCondition.ACTIVE);
			} else {
				aco.setState(AlarmCondition.TERMINATE);
			}
		}
		assertFalse(aco.isOscillating());

	}

}
