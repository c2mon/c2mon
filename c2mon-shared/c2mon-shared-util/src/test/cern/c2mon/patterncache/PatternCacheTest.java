/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.patterncache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * J-unit test of the <code>PatternCache</code> class
 * 
 * @author wbuczak
 */
public class PatternCacheTest {

    private static final String TEST_CREDENTIALS_FILE1 = "testcredentials_01.txt";
    private static final String TEST_CREDENTIALS_FILE2 = "testcredentials_02.txt";

    PatternCache<UserCredentials> pcache;

    @Test
    public void testLoadingFromFile() throws Exception {

        pcache = new PatternCache<UserCredentials>(UserCredentials.class);

        URL url = this.getClass().getResource(TEST_CREDENTIALS_FILE1);
        pcache.load(new File(url.getFile()));

        commonTest();
    }

    @Test
    public void testLoadingFromBuffer() throws Exception {

        pcache = new PatternCache<UserCredentials>(UserCredentials.class);

        StringBuilder strbld = new StringBuilder();
        URL url = this.getClass().getResource(TEST_CREDENTIALS_FILE1);

        Path path = Paths.get(url.getPath());
        for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
            strbld.append(line).append("\n");
        }

        pcache.load(strbld);

        commonTest();
    }

    private void commonTest() {
        assertEquals(4, pcache.getSize());

        assertNotNull(pcache.findMatch("JMX:TEST01"));

        UserCredentials uc = pcache.findMatch("JMX:TEST01");

        assertEquals("user4", uc.getUserName());
        assertEquals("pass4", uc.getUserPasswd());

        uc = pcache.findMatch("some test string");
        assertNull(uc);

        uc = pcache.findMatch("some test string abc");
        assertNotNull(uc);
        assertEquals("user1", uc.getUserName());
        assertEquals("pass1", uc.getUserPasswd());
    }

    @Test
    public void testReloading() throws Exception {

        final class CacheReloader<T extends Cachable> implements Runnable {

            final int NUM_ITERATIONS = 40;

            PatternCache<T> cache;
            int i;

            public CacheReloader(PatternCache<T> cache) {
                this.cache = cache;
            }

            @Override
            public void run() {
                while (i < NUM_ITERATIONS) {
                    String file = TEST_CREDENTIALS_FILE1;
                    if (i++ % 2 == 0) {
                        file = TEST_CREDENTIALS_FILE2;
                    }
                    URL furl = this.getClass().getResource(file);
                    cache.load(new File(furl.getFile()));
                    try {
                        Thread.sleep(120);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }// while
            }
        }

        pcache = new PatternCache<UserCredentials>(UserCredentials.class);

        URL url = this.getClass().getResource(TEST_CREDENTIALS_FILE1);
        pcache.load(new File(url.getFile()));

        CacheReloader<UserCredentials> creloader = new CacheReloader<UserCredentials>(pcache);
        Thread cacheReloader = new Thread(creloader);
        cacheReloader.start();

        for (int i = 0; i < 100; i++) {

            assertNotNull(pcache.findMatch("JMX:TEST01"));

            UserCredentials uc = pcache.findMatch("JMX:TEST01");

            assertEquals("user4", uc.getUserName());
            assertEquals("pass4", uc.getUserPasswd());

            uc = pcache.findMatch("some test string");
            assertNull(uc);

            uc = pcache.findMatch("some test string abc");
            assertNotNull(uc);
            assertEquals("user1", uc.getUserName());
            assertEquals("pass1", uc.getUserPasswd());
            Thread.sleep(30);
        }// for
    }
}
