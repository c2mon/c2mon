package cern.c2mon.publisher.lemon;

import static java.lang.String.format;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
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

			// Message block for LEMON to avoid message doubling caused by
			// multi-metrics blocks
			ConcurrentMap<Long, String> lemonMessageBlock = new ConcurrentHashMap<Long, String>();
			
			// Final message
			String finalLemonMessage;

			Long blockId = (long) 0;

			// Timestamp of start
			java.util.Date date = new java.util.Date();
			Timestamp runTime = new Timestamp(date.getTime());

			log.info("Preparing packet for computer " + computerName);

			// Scan the map to find updated metrics
			for (ConcurrentMap.Entry<String, ClientDataTagValue> metric : metricReceived
					.entrySet()) {
				String key = metric.getKey();
				ClientDataTagValue value = metric.getValue();
				;
				if (value.getTimestamp().compareTo(runTime) < (UPDATE_RECEIVING_PERIOD_SEC * 1000)) {

					// we got fresh update!!!

					long lemonTS = runTime.getTime() / 1000L;

					blockId = diamonMetric2LemonId.get(getMetricShortName(key));

					// We prepare only defined blocks
					if (blockId > 0) {

						// Iterate on the required diamon metrics to complete
						// the block
						Iterator<String> itr = lemonId2Metric.get(blockId)
								.iterator();
						// Final block
						String block = "";

						// Elements of the block
						String blockElements = "";

						// Missing metrics
						Integer missingMetrics = 0;

						while (itr.hasNext()) {
							String requiredMetric = "CLIC:" + computerName
									+ ":" + itr.next();

							if (metricReceived.containsKey(requiredMetric)) {

								// Real value
								blockElements += " "
										+ metricReceived.get(requiredMetric)
												.getValue().toString();
							} else
								missingMetrics++;
						} // while (metrics list iteration)

						// If we have no missing metrics
						if (missingMetrics == 0) {

							block = "#" + blockId + " " + lemonTS
									+ blockElements;
							log.debug("Block constructed: " + block);
							lemonMessageBlock.put(blockId, block);
						} else {
							log.info("Block ignored:" + missingMetrics
									+ " metrics missing to construct");
						} // end of missing metrics verification
					} // if (blockId > 0..
				} // if (value.getTimestamp()..
			} // for metric received

			finalLemonMessage="A1 0 "+computerName;
			for (String messageBlock: lemonMessageBlock.values())
			{
				finalLemonMessage+=messageBlock;
			}
			finalLemonMessage+="#";
			
			log.debug("Final message: "+finalLemonMessage);
			
			
			// To ensure that during the update nobody touches the hash
			synchronized (computerName) {
				updateScheduled.remove(computerName);
			}
		}
	}

	// Mapping of LEMON ids to C2MON Metrics Short Names
	ConcurrentMap<Long, List<String>> lemonId2Metric = new ConcurrentHashMap<Long, List<String>>();

	// Mapping of C2MON Metrics Short Names to LEMON ids
	ConcurrentMap<String, Long> diamonMetric2LemonId = new ConcurrentHashMap<String, Long>();

	// Map of computers with pending update
	ConcurrentMap<String, Boolean> updateScheduled = new ConcurrentHashMap<String, Boolean>();

	// Map of received updates by computer
	ConcurrentMap<String, String> updateReceived = new ConcurrentHashMap<String, String>();

	// Map of received updates by computer tag
	ConcurrentMap<String, ClientDataTagValue> metricReceived = new ConcurrentHashMap<String, ClientDataTagValue>();

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
						metricsShortNames.add(tokens[i].trim().toUpperCase());

						// Put the metric short name into the hash with the
						// LEMON id
						diamonMetric2LemonId.put(
								tokens[i].trim().toUpperCase(), lemonId);
					}

					// Put the LEMON id into the hash with the list of metric
					// short names
					lemonId2Metric.put(lemonId, metricsShortNames);

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

	// Extract Metric Short name (after the second :) from Metric name
	private String getMetricShortName(final String metricUniqueName) {
		return metricUniqueName.split(":")[2];
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

			// Put the update into the hash
			metricReceived.put(cdt.getName(), cdt);

			// Put the key for the computer
			updateReceived.put(hostname, cdt.getName());

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

			log.debug("Update for " + cdt.getName() + " has been processed.");

		} else {
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
