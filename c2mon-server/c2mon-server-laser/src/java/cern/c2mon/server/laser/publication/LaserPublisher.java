package cern.c2mon.server.laser.publication;

import java.sql.Timestamp;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Service;

import cern.laser.source.alarmsysteminterface.ASIException;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterface;
import cern.laser.source.alarmsysteminterface.AlarmSystemInterfaceFactory;
import cern.laser.source.alarmsysteminterface.FaultState;
import cern.tim.server.cache.CacheRegistrationService;
import cern.tim.server.cache.TimCacheListener;
import cern.tim.server.common.alarm.Alarm;
import cern.tim.server.common.config.ServerConstants;

/**
 * Bean responsible for submitting C2MON alarms to LASER.
 */
// starts as singleton bean in Spring context
@Service
public class LaserPublisher implements TimCacheListener<Alarm>, SmartLifecycle, LaserPublisherMBean {

	/**
	 * The alarm source name this publisher is called.
	 */
	private String sourceName;

	/**
	 * Our Logger
	 */
	private Logger log = Logger.getLogger(LaserPublisher.class);

//	private AlarmSourceManager sourceManager = null;

	/** Reference to the LASER alarm system interface. */
	private AlarmSystemInterface asi = null;

	/**
	 * Flag for lifecycle calls.
	 */
	private volatile boolean running = false;

	/**
	 * Service for registering as listener to C2MON caches.
	 */
	private CacheRegistrationService cacheRegistrationService;

	/**
	 */
	private StatisticsModule stats = new StatisticsModule();
	
	/**
	 * Autowired constructor.
	 * 
	 * @param cacheRegistrationService
	 *            the C2MON cache registration service bean
	 */
	@Autowired
	public LaserPublisher(final CacheRegistrationService cacheRegistrationService) {
		super();
		this.cacheRegistrationService = cacheRegistrationService;

	}

	/**
	 * 
	 * @param sourceName
	 *            the alarm source name this publisher should be called.
	 */
	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	/**
	 * 
	 * @return the alarm source name this publisher is called.
	 */
	public String getSourceName() {
		return sourceName;
	}

	/**
	 * Called at server startup.
	 * 
	 * @throws Exception
	 *             in case the underlying alarm system could not be initiated.
	 */
	@PostConstruct
	public void init() throws Exception {
		cacheRegistrationService.registerToAlarms(this);
		asi = AlarmSystemInterfaceFactory.createSource(getSourceName());
	}

	@Override
	public void notifyElementUpdated(Alarm cacheable) {

		FaultState fs = null;
		fs = AlarmSystemInterfaceFactory.createFaultState(cacheable.getFaultFamily(), cacheable.getFaultMember(), cacheable.getFaultCode());
	
		stats.update(cacheable);
		
		if (cacheable.isActive()){
			fs.setDescriptor(cacheable.getState());
			fs.setUserTimestamp(cacheable.getTimestamp());
	
			if (cacheable.getInfo() != null) {
				Properties prop = fs.getUserProperties();
				prop.put(FaultState.ASI_PREFIX_PROPERTY, cacheable.getInfo());
				fs.setUserProperties(prop);
			}
		}else{
			fs.setDescriptor(FaultState.TERMINATE);
		    fs.setUserTimestamp(new Timestamp(System.currentTimeMillis()));
		}
		
		if (log.isDebugEnabled()) {
			log.debug("Pushing alarm to LASER :\n" + fs);
		}
		boolean result = true;
		try {
			asi.push(fs);
		} catch (ASIException e) {
			// Ooops, didn't work. log the exception.
			result = false;
			StringBuilder str = new StringBuilder("Alarm System Interface Exception. Unable to send FaultState ");
			str.append(cacheable.getFaultFamily());
			str.append(':');
			str.append(cacheable.getFaultMember());
			str.append(':');
			str.append(cacheable.getFaultCode());
			str.append(" to LASER.");
			log.error(str, e);
		} catch (Exception e) {
			result = false;
			StringBuilder str = new StringBuilder("sendFaultState() : Unexpected Exception. Unable to send FaultState ");
			str.append(cacheable.getFaultFamily());
			str.append(':');
			str.append(cacheable.getFaultMember());
			str.append(':');
			str.append(cacheable.getFaultCode());
			str.append(" to LASER.");
			log.error(str, e);
		}
		// Keep track of the sent alarm in the Alarm log
		StringBuilder str = new StringBuilder();
		str.append(cacheable.getTimestamp());
		str.append("\t");
		str.append(cacheable.getFaultFamily());
		str.append(':');
		str.append(cacheable.getFaultMember());
		str.append(':');
		str.append(cacheable.getFaultCode());
		str.append('\t');
		str.append(cacheable.getState());
		if (cacheable.getInfo() != null) {
			str.append('\t');
			str.append(cacheable.getInfo());
		}
		if (result)
			log.info(str);
		else
			log.error(str);
	}

	// below server lifecycle methods: complete start/stop (no need to allow for
	// stop/restart, just final shutdown)
	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void start() {
		running = true;
	}

	@Override
	public void stop() {
		asi.close();
		running = false;
	}

	@Override
	public int getPhase() {
		return ServerConstants.PHASE_STOP_LAST;
	}

	@Override
	public long getProcessedAlarms() {
		return stats.getTotalProcessed();
	}

	@Override
	public void resetStatistics() {
		stats.resetStatistics();	
	}

	@Override
	public void resetStatistics(String alarmID) {
		stats.resetStatistics(alarmID);
	}

	@Override
	public List<String> getRegisteredAlarms() {
		return stats.getStatsList();
	}

	@Override
	public String getStatsForAlarm(String id) {
		if (stats.getStatsForAlarm(id) == null){
			return "Not found!";
		}else {
			return stats.getStatsForAlarm(id).toString();
		}
	}

}
