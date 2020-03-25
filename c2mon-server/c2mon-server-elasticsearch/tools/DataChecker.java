package cern.c2mon.elasticsearch;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

/**
 * Utility which allows to compare the number of the alarms reported in C2MON alarm log file and the number of the
 * documents in respective index on ES instance where C2MON is writing the alarm data
 *
 * @author sboychen
 */
public class DataChecker {

    private static final Logger LOG = Logger.getLogger(DataChecker.class);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final Map<Long, AtomicInteger> numberOfAlarmsPerMinute = new HashMap<>();
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private final String endpoint;

    /**
     * Sets up the data checker with provided credentials for a specific endpoint
     *
     * @param endpoint to setup the data checker to
     * @param username to be used for authentication
     * @param password to be used for authentication
     */
    public DataChecker(String endpoint, String username, String password) {
        this.endpoint = endpoint;
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
        credentialsProvider.setCredentials(AuthScope.ANY, credentials);
    }

    /**
     * Performs consistency check between the data in C2MON alarm log file and re
     *
     * @param c2monAlarmLogFilePath path to C2MON alarm log file to be analysed
     * @param index to be check for the number of documents
     */
    public void checkDataConsistency(String c2monAlarmLogFilePath, String index) {
        loadLocalData(c2monAlarmLogFilePath);

        LOG.info("Number of checks to be performed : " + numberOfAlarmsPerMinute.size());

        AtomicInteger failures = new AtomicInteger(0);
        numberOfAlarmsPerMinute.forEach((key, value) -> {
            try {
                Integer remoteCount = getDocumentCountPerMinute(index, key, Duration.of(60, ChronoUnit.SECONDS));
                LOG.debug("TIMESTAMP " + key + " ->  LOCAL: " + value.get() + " REMOTE " + remoteCount);
                if (remoteCount != value.get()) {
                    failures.incrementAndGet();
                    LOG.error("Check failed: TIMESTAMP " + key);
                }
            } catch (IOException e) {
                LOG.error("Error reading from ES: " + e.getMessage(), e);
            }
        });

        LOG.info("Completed with " + failures.get() + " errors.");
    }

    private void loadLocalData(String c2monAlarmLogFilePath) {
        File file = new File(c2monAlarmLogFilePath);
        if (!file.exists()) {
            LOG.error("Error reading data file: file does not exist.");
            return;
        }

        try (Stream<String> stream = Files.lines(file.toPath())) {
            stream.forEach(this::processLine);
        } catch (IOException e) {
            LOG.error("Error processing data file: " + e.getMessage(), e);
        }
    }

    private void processLine(String line) {
        String[] tokens = line.split("\\s+");
        if (tokens.length < 2) {
            LOG.error("Error processing line: Number of tokens is invalid - " + line);
            return;
        }

        String[] time = tokens[1].split(":");
        if (time.length < 3) {
            LOG.error("Error processing time: Number of tokens is invalid - " + time);
            return;
        }

        String dateTime = tokens[0] + " " + time[0] + ":" + time[1] + ":00";

        try {
            long timestamp = DATE_FORMAT.parse(dateTime).getTime();
            if (!numberOfAlarmsPerMinute.containsKey(timestamp)) {
                numberOfAlarmsPerMinute.put(timestamp, new AtomicInteger(0));
            }
            numberOfAlarmsPerMinute.get(timestamp).incrementAndGet();
        } catch (ParseException e) {
            LOG.error("Error parsing time: " + e.getMessage(), e);
        }
    }

    private Integer getDocumentCountPerMinute(String index, long startTimestamp, Duration duration) throws IOException {
        HttpPost request = new HttpPost("https://" + endpoint + "/" + index + "/_count?pretty");
        request.addHeader("Accept", "application/json");

        String query = "{\n" +
                "    \"query\": {\n" +
                "        \"range\" : {\n" +
                "            \"timestamp\" : {\n" +
                "                \"gte\" : " + startTimestamp + ",\n" +
                "                \"lt\" : " + (startTimestamp + duration.toMillis()) + "\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        HttpEntity stringEntity = new StringEntity(query, ContentType.APPLICATION_JSON);
        request.setEntity(stringEntity);

        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
        HttpResponse response = client.execute(request);

        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            String json = IOUtils.toString(response.getEntity().getContent(), Charset.forName("UTF-8"));

            JSONObject responseJson = new JSONObject(json);
            return (Integer) responseJson.get("count");
        } else {
            throw new IOException("An error occurred retrieving document count from server: "
                    + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
        }
    }
}
