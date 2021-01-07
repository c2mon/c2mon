package integration;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.TagService;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SpringBootTest
@Testcontainers
public class ContainerizedTest {

    @ClassRule
    public static DockerComposeContainer environment =
            new DockerComposeContainer(new File("src/test/resources/docker-compose.yaml"))
                    .withExposedService("mq_1", 61616)
                    .withExposedService("db_1", 3306)
                    .withExposedService("daq-rest_1", 8080)
                    .withTailChildContainers(true)
                    .withLocalCompose(true);

    private static TagService tagService;
    private static CustomTagListener customTagListener = new CustomTagListener();

    private static String tagName = "testTag";
    private static double initialTagValue = 23.0;

    /**
     * Create a new data tag by making a POST request to the daq-rest service and subscribe to it with the tagService
     */
    @BeforeClass
    public static void setUp() {

        String address = String.format("http://%s:%d/update",
                environment.getServiceHost("daq-rest_1", 8080),
                environment.getServicePort("daq-rest_1", 8080));

        String newDaqInfo = String.format(
                "{\"name\": \"%s\", \"value\": %f, \"metadata\": {\"building\": 123, \"responsible\": \"Jon Doe\"}}",
                tagName,
                initialTagValue);

        try {
            int response = simplePostRequest(address, newDaqInfo);
            assertEquals(response, 200);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Exception should not be sent");
        }

        tagService = C2monServiceGateway.getTagService();
        tagService.subscribeByName(tagName, customTagListener);
    }


    @Test
    public void testSubscribingToNewDataTag() {
        if (!tagService.findByName(tagName).iterator().hasNext()) fail("No tag was found");

        Tag tag = tagService.findByName(tagName).iterator().next();

        assertEquals(tagName, tag.getName(), tagName);
        assertEquals(initialTagValue, tag.getValue());
    }

    @Test
    public void testReceiveUpdatesFromNewDataTag() {
        if (!tagService.findByName(tagName).iterator().hasNext()) fail("No tag was found");

        String address = String.format("http://%s:%d/tags/%s",
                environment.getServiceHost("daq-rest_1", 8080),
                environment.getServicePort("daq-rest_1", 8080),
                tagName);

        String updatedTagValue = "1337.0";

        try {
            int response = simplePostRequest(address, updatedTagValue);
            assertEquals(response, 200);
        } catch (Exception e) {
            fail("Exception should not be sent");
        }

        await().until(customTagListener::getCurrentTagValue, equalTo(Double.parseDouble(updatedTagValue)));
    }

    private static int simplePostRequest(String address, String message) throws Exception {
        URL url = new URL(address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");

        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = message.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return con.getResponseCode();
    }

    private static class CustomTagListener implements TagListener {
        public double currentTagValue = 0.0;

        public double getCurrentTagValue() {
            return currentTagValue;
        }

        @Override
        public void onInitialUpdate(Collection<Tag> initialValues) {
            currentTagValue = (double) initialValues.iterator().next().getValue();
        }

        @Override
        public void onUpdate(Tag tagUpdate) {
            currentTagValue = (double) tagUpdate.getValue();
        }
    }
}
