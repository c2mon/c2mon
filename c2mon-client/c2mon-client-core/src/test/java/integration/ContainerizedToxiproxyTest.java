package integration;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.C2monServiceGateway;
import cern.c2mon.client.core.service.TagService;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.Collection;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@SpringBootTest
@Testcontainers
@TestPropertySource("classpath:c2mon-client-test.properties")
public class ContainerizedToxiproxyTest {

    private static final DockerImageName MQ_IMAGE = DockerImageName.parse("img-mq:1.9.11-SNAPSHOT");
    private static final DockerImageName DB_IMAGE = DockerImageName.parse("gitlab-registry.cern.ch/c2mon/c2mon/mysql:mysql-5.7.15-c2mon-1.9.11-SNAPSHOT");
    private static final DockerImageName C2MON_IMAGE = DockerImageName.parse("img-server:1.9.11-SNAPSHOT");
    private static final DockerImageName DAQ_REST_IMAGE = DockerImageName.parse("gitlab-registry.cern.ch/c2mon/c2mon-daq-rest:1.9.11-SNAPSHOT");
    private static final DockerImageName TOXIPROXY_IMAGE = DockerImageName.parse("shopify/toxiproxy:2.1.4");
    private static final String TOXIPROXY_NETWORK_ALIAS = "toxiproxy";

    @ClassRule
    public static Network network = Network.newNetwork();

    @ClassRule
    public static ToxiproxyContainer toxiproxy = new ToxiproxyContainer(TOXIPROXY_IMAGE)
            .withNetwork(network)
            .withNetworkAliases(TOXIPROXY_NETWORK_ALIAS);

    @ClassRule
    public static GenericContainer<?> mq = new GenericContainer<>(MQ_IMAGE)
            .withNetwork(network)
            .withNetworkAliases("mq")
            .withExposedPorts(61616, 61614, 1883, 8086, 8161)
            .withStartupCheckStrategy(
                    new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(10))
            );

    @ClassRule
    public static GenericContainer<?> db = new GenericContainer<>(DB_IMAGE)
            .withNetwork(network)
            .withNetworkAliases("db")
            .withExposedPorts(3306)
            .withEnv("MYSQL_ALLOW_EMPTY_PASSWORD", "yes")
            .waitingFor(
                    Wait.forLogMessage(".*ready for connections.*\\n", 1))
            .withStartupCheckStrategy(
                    new MinimumDurationRunningStartupCheckStrategy(Duration.ofSeconds(5))
            );

    public static GenericContainer<?> c2mon = new GenericContainer<>(C2MON_IMAGE)
            .withNetwork(network)
            .withNetworkAliases("c2mon")
            .dependsOn(mq)
            .dependsOn(db)
            .withEnv("C2MON_SERVER_ELASTICSEARCH_ENABLED", "false")
            .withEnv("C2MON_SERVER_JMS_EMBEDDED", "false")
            .withEnv("C2MON_SERVER_CACHEDBACCESS_JDBC_VALIDATION-QUERY", "SELECT 1")
            .withEnv("C2MON_SERVER_JDBC_DRIVER-CLASS-NAME", "com.mysql.jdbc.Driver")
            .withEnv("C2MON_SERVER_JDBC_URL", "jdbc:mysql://db/tim")
            .withEnv("C2MON_SERVER_JDBC_USERNAME", "root")
            .withEnv("C2MON_SERVER_CACHEDBACCESS_JDBC_JDBC-URL", "jdbc:mysql://db/tim")
            .withEnv("C2MON_SERVER_HISTORY_JDBC_JDBC-URL", "jdbc:mysql://db/tim")
            .withEnv("C2MON_SERVER_CONFIGURATION_JDBC_JDBC-URL", "jdbc:mysql://db/tim")
            .withEnv("C2MON_SERVER_TESTMODE", "false")
            .waitingFor(
                    Wait.forLogMessage(".*C2MON server is now initialised.*", 1));

    public static GenericContainer<?> daq_rest = new GenericContainer<>(DAQ_REST_IMAGE)
            .withNetwork(network)
            .withNetworkAliases("daq_rest")
            .withExposedPorts(8080)
            .withEnv("_JAVA_OPTIONS", "-Dc2mon.daq.name=P_DAQ_REST_13 -Dc2mon.daq.jms.url=tcp://mq:61616 -Dc2mon.client.jms.url=tcp://mq:61616")
            .dependsOn(c2mon)
            .waitingFor(
                    Wait.forLogMessage(".*DAQ initialized and running.*", 1));


    private static TagService tagService;
    private static ContainerizedToxiproxyTest.CustomTagListener customTagListener = new ContainerizedToxiproxyTest.CustomTagListener();
    private static TestUtils.CustomTagListener updateTagListener = new TestUtils.CustomTagListener();

    private static String tagName = "testTag";
    private static double initialTagValue = 23.0;

    private static ToxiproxyContainer.ContainerProxy proxy;
    private static String mqProxyUrl;

    private static int numberOfTagsToSend = 3;

    @BeforeClass
    public static void setup() {

        //Create a proxy for the ActiveMQ
        proxy = toxiproxy.getProxy(mq, 61616);
        proxy.setConnectionCut(false);

        mqProxyUrl = "tcp://" + TOXIPROXY_NETWORK_ALIAS + ":" + proxy.getOriginalProxyPort();

        //Set the proxy connection url as the jms url for the server and client
        System.setProperty("c2mon.client.jms.url", "tcp://" + mq.getContainerIpAddress() + ":" + mq.getMappedPort(61616));
        System.setProperty("c2mon.server.jms.url", mqProxyUrl);

        System.out.println(db.getLogs());

        //TODO Added a 5 seconds sleep because C2mon sometimes fails to connect to the database
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        c2mon.withEnv("C2MON_SERVER_JMS_URL", mqProxyUrl);
        c2mon.start();

        System.out.println(c2mon.getLogs());

        daq_rest.withEnv("_JAVA_OPTIONS", "-Dc2mon.daq.name=P_DAQ_REST_13 -Dc2mon.daq.jms.url=" + mqProxyUrl + " -Dc2mon.client.jms.url=" + mqProxyUrl);
        daq_rest.start();

        System.out.println(daq_rest.getLogs());

    }

    @AfterClass
    public static void cleanup() {
        System.setProperty("c2mon.client.jms.url", "tcp://0.0.0.0:61616");
        System.setProperty("c2mon.server.jms.url", "tcp://0.0.0.0:61616");
    }

    @Test
    public void createNewDataTagsWithConnectionDownTest() throws InterruptedException {
        String address = String.format("http://%s:%d/update",
                daq_rest.getContainerIpAddress(),
                daq_rest.getMappedPort(8080));

        proxy.setConnectionCut(true);

        for (int i = 0; i < numberOfTagsToSend; i++) {

            String newDaqInfo = String.format("{\"name\": \"%s\", \"value\": %f, \"metadata\": {\"building\": 123, \"responsible\": \"Jon Doe\"}}",
                    tagName + i,
                    initialTagValue);

            try {
                int response = TestUtils.simplePostRequest(address, newDaqInfo);
                //Connection was cut so the server response will be 500 or a read timeout will be thrown
                assertEquals(500, response);
            } catch (Exception e) {
                e.printStackTrace();
                if (!(e instanceof SocketTimeoutException)) fail("Exception should not be sent");
            }
        }

        proxy.setConnectionCut(false);

        tagService = C2monServiceGateway.getTagService();
        tagService.subscribeByName(tagName + "?", customTagListener);

        assertEquals(numberOfTagsToSend, customTagListener.getNumberOfTagsSubscribed());
    }

    @Test
    public void updateDataTagWithReducedBandwidth() {

        String testTag = tagName + "1";

        tagService.subscribeByName(testTag, updateTagListener);

        if (!tagService.findByName(testTag).iterator().hasNext()) fail("No tag was found");

        String address = String.format("http://%s:%d/tags/%s",
                daq_rest.getContainerIpAddress(),
                daq_rest.getMappedPort(8080),
                testTag);

        String updatedTagValue = "1337.0";

        try {
            proxy.toxics().bandwidth("Reduced Bandwidth", ToxicDirection.DOWNSTREAM, 1);
        } catch (IOException e) {
            e.printStackTrace();
            fail("Exception thrown due to Toxiproxy problems");
        }

        try {
            int response = TestUtils.simplePostRequest(address, updatedTagValue);
            assertEquals(response, 200);
        } catch (Exception e) {
            fail("Exception should not be sent");
        }

        await().until(updateTagListener::getCurrentTagValue, equalTo(Double.parseDouble(updatedTagValue)));
    }

    private static class CustomTagListener implements TagListener {
        private int numberOfTagsSubscribed = 0;

        public int getNumberOfTagsSubscribed() {
            return numberOfTagsSubscribed;
        }

        @Override
        public void onInitialUpdate(Collection<Tag> initialValues) {
            numberOfTagsSubscribed = initialValues.size();
        }

        @Override
        public void onUpdate(Tag tagUpdate) {

        }
    }
}
