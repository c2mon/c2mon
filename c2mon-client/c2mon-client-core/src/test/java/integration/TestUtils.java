package integration;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;

public class TestUtils {

    protected static int simplePostRequest(String address, String message) throws Exception {
        URL url = new URL(address);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("Accept", "application/json");
        con.setReadTimeout(5000);

        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = message.getBytes("utf-8");
            os.write(input, 0, input.length);
        }
        return con.getResponseCode();
    }

    protected static class CustomTagListener implements TagListener {
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
            System.out.println("\n\n\n\nUPDATED VALUE FOR TAG " + tagUpdate.getName() + " - " + currentTagValue);
        }
    }
}
