package cern.c2mon.server.eslog.listener;

import cern.c2mon.server.cache.CacheRegistrationService;
import cern.c2mon.server.common.datatag.DataTagCacheObject;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.server.eslog.logger.TransportConnector;
import cern.c2mon.server.eslog.structure.DataTagESLogConverter;
import cern.c2mon.server.test.CacheObjectCreation;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Tests the TagLogCacheListener's utility methods.
 *
 * @author Alban Marguet
 */

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TagESLogCacheListenerTest {

	@InjectMocks
	TagESLogCacheListener cacheListener;

	@Mock
	DataTagESLogConverter esLogConverter;

	@Mock
	CacheRegistrationService cacheRegistrationService;

	@Mock
	TransportConnector connector;


	@Test
	public void testTagIsLoggedToES() {
		DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
		tag.setLogged(true);
		cacheListener.notifyElementUpdated(Collections.<Tag>singletonList(tag));

		verify(esLogConverter).convertToTagES(eq(tag));
		verify(connector).indexTags(anyList());
	}

	@Test
	public void testTagIsNotLoggedToES() {
		DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();
		tag.setLogged(false);
		cacheListener.notifyElementUpdated(Collections.<Tag>singletonList(tag));

		verify(esLogConverter, never()).convertToTagES(tag);
		verify(connector).indexTags(anyList());
	}

	@Test
	public void testNotifyElementUpdate() {
		ArrayList<Tag> list = new ArrayList<>();

		DataTagCacheObject tag1 = CacheObjectCreation.createTestDataTag();
		tag1.setLogged(true);

		DataTagCacheObject tag2 = CacheObjectCreation.createTestDataTag();
		tag2.setLogged(true);
		tag2.setId(2L);

		DataTagCacheObject tag3 = CacheObjectCreation.createTestDataTag();
		tag3.setLogged(false);
		list.add(tag1);
		list.add(tag2);
		list.add(tag3);

		cacheListener.notifyElementUpdated(list);
		verify(esLogConverter).convertToTagES(eq(tag1));
		verify(esLogConverter).convertToTagES(eq(tag2));
		verify(esLogConverter, atMost(2)).convertToTagES(eq(tag3));
		verify(connector).indexTags(anyList());
	}

	@Test
	public void testSizeTagESCollection() {
		Collection<Tag> tags = new ArrayList<>();
		int logged = 0;

		for (int i = 0; i < 100; i++) {
			DataTagCacheObject tag = CacheObjectCreation.createTestDataTag();

			if (i % 3 == 0) {
				tag.setLogged(true);
				logged++;
			}

			tags.add(tag);
		}

		cacheListener.notifyElementUpdated(tags);
		assertEquals(logged, cacheListener.getTagESCollection().size());
	}
}