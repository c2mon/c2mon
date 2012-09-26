package cern.c2mon.publisher.lemon;

import static java.lang.String.format;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.publisher.Publisher;
import cern.c2mon.shared.client.tag.TagConfig;

/**
 * This class publishes updates of DIAMON CLIC metrics to LEMON For each
 * computer we are collecting the individual metric updates by scheduling a
 * packet sender.
 * 
 * The current version implements only the non-authenticated LEMON protocol
 * 
 * @author Peter Jurcso
 */

@Service
public class LemonPublisher implements Publisher {

	// Setup simple logger
	private static final Logger log = Logger.getLogger(LemonPublisher.class);

	// Thread pool size for scheduler default: 10 threads
	static int POOL_SIZE = Integer.getInteger(
			"cern.c2mon.publisher.lemon.poolSize", 16);

	// Period for update collection
	// Following the first update we are collecting updates for this period for
	// a computer
	static int UPDATE_RECEIVING_PERIOD_SEC = Integer.getInteger(
			"cern.c2mon.publisher.lemon.updateReceivingPeriod", 15);

	// Store scheduled publishing to allow clean shutdown
	private final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<String, ScheduledFuture<?>>();

	// Prepare scheduler
	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(POOL_SIZE);

	private class LemonPacketSender implements Runnable {

		private String computerName;

		public LemonPacketSender(String computerName) {
			this.computerName = computerName;
		}

		@Override
		public void run() {

			log.info("Sending packet for computer " + computerName);

			// Accept schedule for the computer
			
			// To ensure that during the update nobody touches the hash
			synchronized (computerName) {
				updateScheduled.remove(computerName);
			}
		}
	}

	// Mapping of LEMON ids to C2MON Metrics Short Names
	ConcurrentMap<Long, List<String>> lemonId2Metrics = new ConcurrentHashMap<Long, List<String>>();

	// Mapping of C2MON Metrics Short Names to LEMON ids
	ConcurrentMap<String, Long> diamonMetrics2LemonId = new ConcurrentHashMap<String, Long>();

	// Map of computers with pending update
	ConcurrentMap<String, Boolean> updateScheduled = new ConcurrentHashMap<String, Boolean>();

	/*
	 * Loads Lemon template from local file or from web server with the
	 * following format:
	 * 
	 * lemon id (number), comma separated list of metrics short names
	 */
	void loadLemonTemplate(final URL url) throws IOException {

		log.debug("Opening template URL: " + url);
		BufferedReader br = null;
		try {

			br = new BufferedReader(new InputStreamReader(url.openStream()));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {

				// Split by comma
				String[] tokens = strLine.split(",");

				// If the line contains least a comma
				if (tokens.length > 1) {

					//
					long lemonId = Long.parseLong(tokens[0].trim());

					List<String> metricsShortNames = new ArrayList<String>();
					for (int i = 1; i < tokens.length; i++) {

						// Put the metric short name into the list
						metricsShortNames.add(tokens[i].trim());

						// Put the metric short name into the hash with the
						// LEMON id
						diamonMetrics2LemonId.put(tokens[i].trim(), lemonId);
					}

					// Put the LEMON id into the hash with the list of metric
					// short names
					lemonId2Metrics.put(lemonId, metricsShortNames);

				}

				// System.out.println(strLine);
			}

		} finally {
			// Close the input stream
			br.close();
			log.info("LEMON template loaded.");

		}
	} // loadLemonTemplate

	// Extract host name (between two :s) from Metric name
	private String getHostname(final String metricUniqueName) {
		return metricUniqueName.split(":")[1];
	}

	private boolean isScheduled(String computerName) {
		boolean result = false;

		// To ensure that during the update nobody touches the hash
		synchronized (computerName) {
			
			// Not in the hash
			if (!updateScheduled.containsKey(computerName)) {
				log.debug(computerName + " not in the has: to be scheduled");
				updateScheduled.put(computerName, true);
				// Alread in the hash but not scheduled
			} else if (!updateScheduled.get(computerName)) {
				log.debug(computerName
						+ " in the has but not yet scheduled: to be scheduled");
				updateScheduled.put(computerName, true);

			} else {
				result = true;
				log.debug(computerName + " is in the has and already scheduled");

			}
		}

		return result;

	} // isScheduled

	@Override
	public void onUpdate(ClientDataTagValue cdt, TagConfig cdtConfig) {
		// TODO Auto-generated method stub
		if (cdt.getDataTagQuality().isValid()) {
			String hostname = getHostname(cdt.getName());
			log.debug("Valid update received for " + hostname);
			if (!isScheduled(hostname)) {

				// schedule packet sender
				if (log.isTraceEnabled()) {

					log.trace(format(
							"scheduling LemonPacketSender for computer %s",
							hostname));
				}

				ScheduledFuture<?> sf = scheduler.schedule(
						new LemonPacketSender(hostname),
						UPDATE_RECEIVING_PERIOD_SEC, TimeUnit.SECONDS);

				scheduledTasks.put(hostname, sf);

			}

		}
		else
		{
			log.info("Invalid update received");
		}
	} // onUpdate

	@Override
	public void shutdown() {
		
		// Nice stopping of already scheduled publishings
		for (ScheduledFuture<?> sf : scheduledTasks.values()) {
			if (!sf.isDone()) {
				sf.cancel(false);
			}
		}

	} // shutdown

}
