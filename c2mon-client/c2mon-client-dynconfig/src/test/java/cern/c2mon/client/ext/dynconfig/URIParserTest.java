package cern.c2mon.client.ext.dynconfig;

import cern.c2mon.client.ext.dynconfig.strategy.TagConfigStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class URIParserTest {

    ConcurrentHashMap<String, List<String>> map;

    @BeforeEach
    void setUp()  {
        map = new ConcurrentHashMap<>();
    }

    @Test
    void toTagNameShouldReturnDefaultIfNotInQueryGiven() {
        URI uri = URI.create("opc.tcp://host:500/path");
        assertEquals(uri.toASCIIString(), URIParser.toTagName(uri));
    }

    @Test
    void toTagNameShouldReturnTagName() {
        URI uri = URI.create("opc.tcp://host:500/path?tagName=tagNameTest&abc");
        assertEquals("tagNameTest", URIParser.toTagName(uri));
    }

    @Test
    void emptyToTagNameShouldShouldReturnEmptyString() {
        URI uri = URI.create("opc.tcp://host:500/path?tagName=&a");
        assertEquals("", URIParser.toTagName(uri));
    }

    @Test
    void escapedCharInTagNameShouldBeDecoded() {
        URI uri = URI.create("opc.tcp://host:500/path?tagName=%20");
        assertEquals(" ", URIParser.toTagName(uri));
    }

    @Test
    void emptyTagTypeShouldReturnDataTag() {
        URI uri = URI.create("opc.tcp://host:500/path?tagType=");
        assertEquals(TagConfigStrategy.TagType.DATA, URIParser.getTagType(uri));
    }

    @Test
    void noTagTypeShouldReturnDataTag() {
        URI uri = URI.create("opc.tcp://host:500/path");
        assertEquals(TagConfigStrategy.TagType.DATA, URIParser.getTagType(uri));
    }


    @Test
    void falseTagTypeShouldReturnDataTag() {
        URI uri = URI.create("opc.tcp://host:500/path?tagType=XXX");
        assertEquals(TagConfigStrategy.TagType.DATA, URIParser.getTagType(uri));
    }


    @Test
    void commandTypeShouldReturnCommandTag() {
        URI uri = URI.create("opc.tcp://host:500/path?tagType=COMMAND");
        assertEquals(TagConfigStrategy.TagType.COMMAND, URIParser.getTagType(uri));
    }

    @Test
    void splitEmptyQueryShouldReturnEmptyMap() {
        URI uri = URI.create("opc.tcp://host:500/path");
        assertTrue(URIParser.splitQuery(uri).isEmpty());
    }

    @Test
    void splitMapWithSingleEntryShouldReturnSingleEntry() {
        URI uri = URI.create("opc.tcp://host:500/path?tagName=1");
        map.put("tagName", Collections.singletonList("1"));
        assertEquals(map, URIParser.splitQuery(uri));
    }

    @Test
    void splitMapWithDoubleEntryShouldReturnBothInList() {
        URI uri = URI.create("opc.tcp://host:500/path?tagName=1&tagName=2");
        map.put("tagName", Arrays.asList("1", "2"));
        assertEquals(map, URIParser.splitQuery(uri));
    }

    @Test
    void splitMapWithManyEntriesShouldReturnAllInMap() {
        URI uri = URI.create("opc.tcp://host:500/path?a=1&b=2");
        map.put("a", Collections.singletonList("1"));
        map.put("b", Collections.singletonList("2"));
        assertEquals(map, URIParser.splitQuery(uri));
    }

    @Test
    void questionMarkShouldAlsoWorkAsSeparator() {
        URI uri = URI.create("opc.tcp://host:500/path?a=1?b=2");
        map.put("a", Collections.singletonList("1"));
        map.put("b", Collections.singletonList("2"));
        assertEquals(map, URIParser.splitQuery(uri));
    }

    @Test
    void queryKeyWithoutValueShouldReadAsEmptyString() {
        URI uri = URI.create("opc.tcp://host:500/path?a=1?b");
        map.put("a", Collections.singletonList("1"));
        map.put("b", Collections.singletonList(""));
        assertEquals(map, URIParser.splitQuery(uri));
    }
}