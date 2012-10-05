package cern.c2mon.publisher.lemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

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
public class LemonPublisher implements Publisher
{

	// Setup simple logger
	private static final Logger log = Logger.getLogger(LemonPublisher.class);

	// Set this to something else to block real udp communication
	static int LEMON_SERVER_NONET = Integer.getInteger("cern.c2mon.publisher.lemon.noNet", 0);

	// Lemon server's address
	static String LEMON_SERVER_NAME = System
			.getProperty("cern.c2mon.publisher.lemon.serverName", "cs-ccr-inf1.cern.ch");

	// Lemon server's upd port
	static int LEMON_SERVER_PORT = Integer.getInteger("cern.c2mon.publisher.lemon.serverPort", 12409);

	// Thread pool size for scheduler default: 10 threads
	static int POOL_SIZE = Integer.getInteger("cern.c2mon.publisher.lemon.poolSize", 16);

	// Period for update collection
	// Following the first update we are collecting updates for this period for
	// a computer
	static int UPDATE_RECEIVING_PERIOD_SEC = Integer.getInteger("cern.c2mon.publisher.lemon.updateReceivingPeriod", 15);

	// Store scheduled publishing to allow clean shutdown
	private final ConcurrentMap<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<String, ScheduledFuture<?>>();

	// Prepare scheduler
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(POOL_SIZE);

	private class LemonPacketSender implements Runnable
	{

		private String computerName;

		public LemonPacketSender(String computerName)
		{
			this.computerName = computerName;
		}

		@Override
		public void run()
		{

			// Message block for LEMON to avoid message doubling caused by
			// multi-metrics blocks
			ConcurrentMap<Long, String> lemonMessageBlock = new ConcurrentHashMap<Long, String>();

			// Final message
			String finalLemonMessage;

			// Total blocks in the message
			int totalBlocks = 0;

			// Timestamp of start
			long startTime = System.currentTimeMillis();

			// initial update? if yes we will ignore the timestamp
			Boolean initialPublication = false;

			if (initialUpdate.containsKey(computerName))
			{
				initialPublication = initialUpdate.get(computerName);
				log.debug(computerName + " will send initial update now");

			}

			log.info(computerName + " Preparing packet");

			for (ConcurrentHashMap.Entry<Long, List<String>> lemonEntry : lemonId2Metric.entrySet())
			{

				// Lemon metrics id
				Long blockId = lemonEntry.getKey();
				// Final block
				String block = "";

				// Elements of the block
				String blockElements = "";

				// Metrics in the block
				int blockMetrics = 0;

				// Missing metrics
				int missingMetrics = 0;

				// Old metrics
				int oldMetrics = 0;

				for (String requiredMetricShortName : lemonEntry.getValue())
				{
					String requiredMetric = "CLIC:" + computerName + ":" + requiredMetricShortName;
					blockMetrics++;
					if (metricReceived.containsKey(requiredMetric))
					{
						log.debug(computerName + " " + requiredMetric + " value: "
								+ metricReceived.get(requiredMetric).getValue().toString() + " Delay: "
								+ (startTime - metricReceived.get(requiredMetric).getTimestamp()) + "ms");

						blockElements += " " + metricReceived.get(requiredMetric).getValue().toString();

						if (initialPublication
								|| (startTime - metricReceived.get(requiredMetric).getTimestamp()) <= UPDATE_RECEIVING_PERIOD_SEC * 2000)
						{
							// Real value
							// blockElements += " " +
							// metricReceived.get(requiredMetric).getValue().toString();

						} else
						{
							oldMetrics++;
						}
					} else
					{
						// Dummy to be used?
						if (requiredMetricShortName.startsWith("!"))
						{
							// dummy value, prefixed with !
							blockElements += " " + requiredMetricShortName.substring(1);
							log.debug(computerName + " Dummy value (" + requiredMetricShortName.substring(1)
									+ ") used to build LemonId: " + blockId);
						} else
						{
							missingMetrics++;
							log.info(computerName + " Missing metrics:" + requiredMetric + " to build LemonId:"
									+ blockId);
						}
					}
				} // while (metrics list iteration)

				// If we have no missing metrics
				if ((missingMetrics < 1) && (blockMetrics > oldMetrics))
				{

					block = computerName.toLowerCase() + "#" + blockId + " " + (startTime / 1000L) + blockElements
							+ "#";

					log.debug(computerName + " Block constructed: " + block);

					lemonMessageBlock.put(blockId, block);
					totalBlocks++;
				} else
				{
					if (missingMetrics > 0)
					{
						log.info(computerName + " Block ignored: " + blockId + " Reason: " + missingMetrics
								+ " metric(s) missing to construct");
					}
					if ((blockMetrics <= oldMetrics))
					{
						log.info(computerName + " Block ignored: " + blockId + " No new data since last update");
					}

				} // end of missing metrics verification
			} // for

			// Do we have any update ready?
			if (totalBlocks > 0)
			{

				finalLemonMessage = "A1 0 ";
				for (String messageBlock : lemonMessageBlock.values())
				{
					finalLemonMessage += messageBlock;
				}

				// To avoid sending upd packets during tests
				if (LEMON_SERVER_NONET == 0)
				{
					log.info(computerName + " SEND UDP " + LEMON_SERVER_NAME + ":" + LEMON_SERVER_PORT + " ->  "
							+ finalLemonMessage);

					DatagramSocket clientSocket;
					InetAddress IPAddress;
					try
					{
						clientSocket = new DatagramSocket();
						IPAddress = InetAddress.getByName(LEMON_SERVER_NAME);
						byte[] sendData = new byte[1024];
						String sentence = finalLemonMessage;
						sendData = sentence.getBytes();
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress,
								LEMON_SERVER_PORT);
						clientSocket.send(sendPacket);
						clientSocket.close();

					} catch (SocketException e)
					{
						log.error("Impossible to create socket");
					} catch (UnknownHostException e)
					{
						log.error("Unknown host " + LEMON_SERVER_NAME);
					} catch (IOException e)
					{
						log.error("Unable to send packet to " + LEMON_SERVER_NAME + ":" + LEMON_SERVER_PORT);

					}
				} else
				{
					log.info(computerName + " NO-NET-MODE: Prepared but not sent to " + LEMON_SERVER_NAME + ":"
							+ LEMON_SERVER_PORT + " ->  " + finalLemonMessage);

				} // if nonet

			} else
			{
				log.info(computerName + " No update to be sent");
			}// if (totalBlocks>0

			// To ensure that during the update nobody touches the hash
			synchronized (computerName)
			{
				updateScheduled.remove(computerName);
				log.debug(computerName + " No longer scheduled");
			}
			// Mark initial update
			if (initialUpdate.containsKey(computerName))
			{
				if (initialUpdate.get(computerName))
				{
					initialUpdate.put(computerName, false);
					log.debug(computerName + " inital update sent");
				}

			}
		} // run()
	}

	// Mapping of LEMON ids to C2MON Metrics Short Names
	ConcurrentMap<Long, List<String>> lemonId2Metric = new ConcurrentHashMap<Long, List<String>>();

	// Map of computers with pending update
	ConcurrentMap<String, Boolean> updateScheduled = new ConcurrentHashMap<String, Boolean>();

	// Map of received updates by computer
	ConcurrentMap<String, Boolean> initialUpdate = new ConcurrentHashMap<String, Boolean>();

	// Map of received updates by computer tag
	ConcurrentMap<String, LemonMetric> metricReceived = new ConcurrentHashMap<String, LemonMetric>();

	@PostConstruct
	void init()
	{
		String template = System.getProperty("cern.c2mon.publisher.lemon.template");
		if (template == null)
		{
			log.fatal("Template file is not defined");
			System.exit(-1);
		} else
		{
			try
			{
				loadLemonTemplate(new URL(template));
			} catch (Exception e)
			{
				log.fatal("Could not load template file " + template);
				System.exit(-1);
			}
		}
		template = System.getProperty("cern.c2mon.publisher.lemon.static");
		if (template == null)
		{
			log.fatal("Statid data file is not defined");
			System.exit(-1);
		} else
		{
			try
			{
				loadLemonStaticData(new URL(template));
			} catch (Exception e)
			{
				log.fatal("Could not load static data file " + template);
				System.exit(-1);
			}
		}
	}

	/*
	 * Loads Lemon template from local file or from web server with the
	 * following format:
	 * 
	 * lemon id (number), comma separated list of metrics short names
	 */
	void loadLemonTemplate(final URL url) throws IOException
	{

		log.debug("Opening template URL: " + url);
		BufferedReader br = null;
		try
		{

			br = new BufferedReader(new InputStreamReader(url.openStream()));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null)
			{

				// Split by comma
				String[] tokens = strLine.split(",");

				// If the line contains least a comma
				if (tokens.length > 1)
				{

					//
					long lemonId = Long.parseLong(tokens[0].trim());

					List<String> metricsShortNames = new ArrayList<String>();
					for (int i = 1; i < tokens.length; i++)
					{

						// Put the metric short name into the list
						metricsShortNames.add(tokens[i].trim().toUpperCase());

					}

					// Put the LEMON id into the hash with the list of metric
					// short names
					lemonId2Metric.put(lemonId, metricsShortNames);
					log.debug("Template line loaded: " + lemonId + " = " + metricsShortNames);

				}

				// System.out.println(strLine);
			}

		} finally
		{
			// Close the input stream
			br.close();
			log.info("LEMON template loaded.");

		}
	} // loadLemonTemplate

	/*
	 * Loads Lemon Static data from local file or from web server with the
	 * following format:
	 * 
	 * host name, comma separated list of the following elements:
	 */
	void loadLemonStaticData(final URL url) throws IOException
	{

		log.debug("Opening static data URL: " + url);
		long dataTS = System.currentTimeMillis();
		BufferedReader br = null;
		try
		{

			br = new BufferedReader(new InputStreamReader(url.openStream()));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null)
			{

				// Split by comma
				String[] tokens = strLine.split(",");

				// If the line contains least a comma
				if (tokens.length > 17)
				{

					String host = tokens[0].trim().toUpperCase();
					String clic = "CLIC:" + host + ":LEMON.STATIC.";
					log.debug("Currently loading: " + clic + " " + tokens.length + " tokens");
					// interface name
					metricReceived.put(clic + "INTERFACE",
							new LemonMetric(clic + "INTERFACE", tokens[1].trim(), dataTS));
					metricReceived.put(clic + "IP", new LemonMetric(clic + "IP", tokens[2].trim(), dataTS));
					metricReceived.put(clic + "MASK", new LemonMetric(clic + "MASK", tokens[3].trim(), dataTS));
					metricReceived.put(clic + "BROADCAST",
							new LemonMetric(clic + "BROADCAST", tokens[4].trim(), dataTS));
					metricReceived.put(clic + "GATEWAY", new LemonMetric(clic + "GATEWAY", tokens[5].trim(), dataTS));
					metricReceived.put(clic + "MAC", new LemonMetric(clic + "MAC", tokens[6].trim(), dataTS));
					int lemonStaticMTU = 0;
					try
					{
						lemonStaticMTU = new Integer(tokens[7].trim());
					} catch (NumberFormatException ex)
					{
						log.info("Non numeric value for " + clic + "MTU: " + tokens[7].trim());
					}

					metricReceived.put(clic + "MTU", new LemonMetric(clic + "MTU", lemonStaticMTU, dataTS));
					int lemonStaticDuplex = 0;

					try
					{
						lemonStaticDuplex = new Integer(tokens[8].trim());
					} catch (NumberFormatException ex)
					{
						log.info("Non numeric value for " + clic + "DUPLEX: " + tokens[8].trim());
					}

					metricReceived.put(clic + "DUPLEX", new LemonMetric(clic + "DUPLEX", lemonStaticDuplex, dataTS));

					metricReceived.put(clic + "VENDOR", new LemonMetric(clic + "VENDOR", tokens[9].trim(), dataTS));
					metricReceived.put(clic + "MODEL", new LemonMetric(clic + "MODEL", tokens[10].trim(), dataTS));
					int lemonStaticCPUSpeed = 0;

					try
					{
						lemonStaticCPUSpeed = new Integer(tokens[11].trim());
					} catch (NumberFormatException ex)
					{
						log.info("Non numeric value for " + clic + "CPU.SPEED: " + tokens[11].trim());
					}
					metricReceived.put(clic + "CPU.SPEED", new LemonMetric(clic + "CPU.SPEED", lemonStaticCPUSpeed,
							dataTS));

					int lemonStaticCPUMips = 0;
					try
					{
						lemonStaticCPUMips = new Integer(tokens[12].trim());
					} catch (NumberFormatException ex)
					{
						log.info("Non numeric value for " + clic + "CPU.MIPS: " + tokens[12].trim());
					}
					metricReceived.put(clic + "CPU.MIPS",
							new LemonMetric(clic + "CPU.MIPS", lemonStaticCPUMips, dataTS));
					int lemonStaticCPU = 0;
					try
					{
						lemonStaticCPU = new Integer(tokens[13].trim());
					} catch (NumberFormatException ex)
					{
						log.info("Non numeric value for " + clic + "CPU: " + tokens[13].trim());
					}
					metricReceived.put(clic + "CPU", new LemonMetric(clic + "CPU", lemonStaticCPU, dataTS));

					int lemonStaticCPUCORE = 0;
					try
					{
						lemonStaticCPUCORE = new Integer(tokens[14].trim());
					} catch (NumberFormatException ex)
					{
						log.info("Non numeric value for " + clic + "CPU.CORE: " + tokens[14].trim());
					}
					metricReceived.put(clic + "CPU.CORE",
							new LemonMetric(clic + "CPU.CORE", lemonStaticCPUCORE, dataTS));

					metricReceived.put(clic + "OS", new LemonMetric(clic + "OS", tokens[15].trim(), dataTS));
					metricReceived.put(clic + "OS.KERNEL", new LemonMetric(clic + "OS.KERNEL", tokens[16].trim(),
							dataTS));
					metricReceived.put(clic + "OS.ARCH", new LemonMetric(clic + "OS.ARCH", tokens[17].trim(), dataTS));
					metricReceived.put(clic + "OS.DIST", new LemonMetric(clic + "OS.DIST", tokens[18].trim(), dataTS));

				}

				// System.out.println(strLine);
			}

		} finally
		{
			// Close the input stream
			br.close();
			log.info(metricReceived.keySet().size() + " entries of LEMON static data has been loaded.");

		}
	} // loadLemonStaticData

	// Extract host name (between two :s) from Metric name
	private String getHostname(final String metricUniqueName)
	{
		return metricUniqueName.split(":")[1];
	}

	private boolean isScheduled(String computerName)
	{
		boolean result = false;

		// To ensure that during the update nobody touches the hash
		synchronized (computerName)
		{

			// Not in the hash
			if (!updateScheduled.containsKey(computerName))
			{
				log.debug(computerName + " not in the hash: to be scheduled");
				updateScheduled.put(computerName, true);
				// Already in the hash but not scheduled
			} else if (!updateScheduled.get(computerName))
			{
				log.debug(computerName + " in the hash but not yet scheduled: to be scheduled");
				updateScheduled.put(computerName, true);

			} else
			{
				result = true;
				log.debug(computerName + " is in the hash and already scheduled");

			}
		}

		return result;

	} // isScheduled

	@Override
	public void onUpdate(ClientDataTagValue cdt, TagConfig cdtConfig)
	{

		// Just taking into account valid updates
		if (cdt.getDataTagQuality().isValid())
		{

			String metricName = cdt.getName();
			String hostname = getHostname(metricName);
			String clicComputer = "CLIC:" + hostname + ":";
			long metricTS = cdt.getTimestamp().getTime();

			log.debug(hostname + " Valid update received for " + metricName + " at " + metricTS);

			// Put the update into the hash
			metricReceived.put(metricName, new LemonMetric(metricName, cdt.getValue(), metricTS));

			// HANDMADE LEMON STUFF

			// NETWORK IN
			if (metricName.endsWith("SYS.NET.IN"))
			{
				long previousIn = 0;
				float currentIn = 0;
				long cumulatedIn = 0;
				if (metricReceived.containsKey(clicComputer + "LEMON.SYS.NET.IN"))
				{
					previousIn = Long.parseLong(metricReceived.get(clicComputer + "LEMON.SYS.NET.IN").getValue()
							.toString());
				}
				try
				{
					currentIn = Float.parseFloat(cdt.getValue().toString()) * 60;
				} catch (Exception e)
				{
					log.info(metricName + " is not numeric:  " + cdt.getValue().toString());
				}
				cumulatedIn = Math.round(currentIn) + previousIn;
				metricReceived.put(clicComputer + "LEMON.SYS.NET.IN", new LemonMetric(
						clicComputer + "LEMON.SYS.NET.IN", cumulatedIn, metricTS));
				log.debug(clicComputer + "LEMON.SYS.NET.IN updated to " + cumulatedIn + " (was " + previousIn + ")");
			}
			// NETWORK OUT
			if (metricName.endsWith("SYS.NET.OUT"))
			{
				long previousOut = 0;
				float currentOut = 0;
				long cumulatedOut = 0;
				if (metricReceived.containsKey(clicComputer + "LEMON.SYS.NET.OUT"))
				{
					previousOut = Long.parseLong(metricReceived.get(clicComputer + "LEMON.SYS.NET.OUT").getValue()
							.toString());
				}
				try
				{
					currentOut = Float.parseFloat(cdt.getValue().toString()) * 60;
				} catch (Exception e)
				{
					log.info(metricName + " is not numeric:  " + cdt.getValue().toString());
				}
				cumulatedOut = Math.round(currentOut) + previousOut;
				metricReceived.put(clicComputer + "LEMON.SYS.NET.OUT", new LemonMetric(clicComputer
						+ "LEMON.SYS.NET.OUT", cumulatedOut, metricTS));
				log.debug(clicComputer + "LEMON.SYS.NET.OUT updated to " + cumulatedOut + " (was " + previousOut + ")");
			}
			// BOOT TIME
			if (metricName.endsWith("SYS.KERN.UPTIME"))
			{
				long bootTime = 0;
				try
				{
					bootTime = metricTS / 1000L - Long.parseLong(cdt.getValue().toString());
				} catch (Exception e)
				{
					log.info(metricName + " is not numeric:  " + cdt.getValue().toString());
				}
				metricReceived.put(clicComputer + "LEMON.SYS.BOOTTIME", new LemonMetric(clicComputer
						+ "LEMON.SYS.BOOTTIME", bootTime, metricTS));
				log.debug(clicComputer + "LEMON.SYS.BOOTTIME updated to " + bootTime);
			}

			// Average create processes
			if (metricName.endsWith("PROC.ACTIVESTATE.DELTA"))
			{
				float deltaAvg = 0;
				try
				{
					deltaAvg = Float.parseFloat(cdt.getValue().toString()) / 60;
				} catch (Exception e)
				{
					log.info(metricName + " is not numeric:  " + cdt.getValue().toString());
				}
				metricReceived.put(clicComputer + "LEMON.PROC.ACTIVESTATE.DELTA.AVG", new LemonMetric(clicComputer
						+ "LEMON.PROC.ACTIVESTATE.DELTA.AVG", deltaAvg, metricTS));
				log.debug(clicComputer + "LEMON.PROC.ACTIVESTATE.DELTA.AVG updated to " + deltaAvg);
			}
			// End of handmade LEMON stuff

			// Not in the hash
			if (!initialUpdate.containsKey(hostname))
			{
				initialUpdate.put(hostname, true);
				log.debug(hostname + " had no initial update yet " + initialUpdate.get(hostname));

			}

			if (!isScheduled(hostname))
			{

				// schedule packet sender
				log.debug(hostname + " Scheduling LemonPacketSender");

				ScheduledFuture<?> sf = scheduler.schedule(new LemonPacketSender(hostname),
						UPDATE_RECEIVING_PERIOD_SEC, TimeUnit.SECONDS);

				scheduledTasks.put(hostname, sf);

			}

			log.debug("Update for " + cdt.getName() + " has been processed.");

		} else
		{
			log.info("Invalid update received, ignored");
		}
	} // onUpdate

	@Override
	public void shutdown()
	{

		// Nice stopping of already scheduled publishing
		for (ScheduledFuture<?> sf : scheduledTasks.values())
		{
			if (!sf.isDone())
			{
				sf.cancel(false);
			}
		}

	} // shutdown

}
