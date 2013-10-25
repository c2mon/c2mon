/**
 * Copyright (c) 2013 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.driver.common;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.tim.shared.common.datatag.ValueChangeMonitor;
import cern.tim.shared.daq.datatag.ISourceDataTag;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

/**
 * This class implements Value Change Monitoring (VCM) mechanism, which is used by the DAQ core for selected tags, to detect
 * the value changes in time
 * 
 * @author wbuczak
 */
@Service
public class ValueChangeMonitorEngine implements UpdateListener {

    private Map<Long, Long> equipmentHandlers = new ConcurrentHashMap<Long, Long>();

    private Map<Long, List<EPStatement>> registeredStatements = new ConcurrentHashMap<Long, List<EPStatement>>();

    private static final Logger logger = Logger.getLogger(ValueChangeMonitorEngine.class);

    static EPServiceProvider epService;

    // the following pattern is used for missing event detection (in time window)
    static final String eplExpTemplate1_a = "insert into ValueChangeMonitorEventStream select '%d' as id, false as value "
            + "from pattern [every (timer:interval(%d msec) and not cern.c2mon.driver.common.ValueChangeMonitorEvent(id=%d))]";

    // the following pattern is used to detect events arriving as expected (in time window)
    static final String eplExpTemplate1_b = "insert into ValueChangeMonitorEventStream select '%d' as id, true as value "
            + "from pattern [every (timer:interval(%d msec) and cern.c2mon.driver.common.ValueChangeMonitorEvent(id=%d))]";

    // the following pattern is used to detect pairs of events with value differences matching the expected criteria
    static final String eplExpTemplate2_a = "insert into ValueChangeMonitorEventStream select '%d' as id, true as value "
            + "from cern.c2mon.driver.common.ValueChangeMonitorEvent(id=%d) "
            + "match_recognize ( "
            + "partition by id "
            + "measures E1.value as v1, E2.value as v2 " 
            + "pattern (E1 E2)  define E2 as E2.value - E1.value %s %s )";

    // the following pattern is used to detect pairs of events with value differences NOT matching the expected criteria
    static final String eplExpTemplate2_b = "insert into ValueChangeMonitorEventStream select '%d' as id, false as value "
            + "from cern.c2mon.driver.common.ValueChangeMonitorEvent(id=%d) "
            + "match_recognize ( "
            + "partition by id "
            + "measures E1.value as v1, E2.value as v2 " 
            + "pattern (E1 E2)  define E2 as E2.value - E1.value %s %s )";

    private static volatile ValueChangeMonitorEngine theInstnance;

    private DriverKernel kernel;

    public static ValueChangeMonitorEngine getInstance() {
        if (theInstnance == null) {
            theInstnance = new ValueChangeMonitorEngine();
            epService = EPServiceProviderManager.getDefaultProvider();
        }

        return theInstnance;
    }

    public static void stop() {
        if (theInstnance != null) {
            epService.getEPAdministrator().destroyAllStatements();
            if (!epService.isDestroyed()) {
                epService.destroy();

            }

            theInstnance = null;
        }
    }

    @Autowired
    public void setDriverKernel(DriverKernel kernel) {
        this.kernel = kernel;
    }

    public void register(Long equipmentId, ISourceDataTag tag, ValueChangeMonitor vcm) {
        if (registeredStatements.containsKey(tag.getId())) {
            logger.warn(format(
                    " tag %d is already registered. Registration can be done once only. You need to unregister first",
                    tag.getId()));
            return;
        }

        List<EPStatement> statements = new ArrayList<EPStatement>();

        // only time window is declared
        if (vcm.hasTimeWindow() && !vcm.hasStep()) {

            String expression1 = String.format(eplExpTemplate1_a, tag.getId(), vcm.getTimeWindow(), tag.getId());
            EPStatement s1 = epService.getEPAdministrator().createEPL(expression1);
            statements.add(s1);
            s1.addListener(this);

            String expression2 = String.format(eplExpTemplate1_b, tag.getId(), vcm.getTimeWindow(), tag.getId());
            EPStatement s2 = epService.getEPAdministrator().createEPL(expression2);
            statements.add(s2);
            s2.addListener(this);

            // only value step is declared
        } else if (!vcm.hasTimeWindow() && vcm.hasStep()) {

            String expression1 = String.format(eplExpTemplate2_a, tag.getId(), tag.getId(), vcm.getOperator()
                    .getOperatorEpl(), vcm.getStep());
            EPStatement s1 = epService.getEPAdministrator().createEPL(expression1);
            statements.add(s1);
            s1.addListener(this);

            String expression2 = String.format(eplExpTemplate2_b, tag.getId(), tag.getId(), vcm.getOperator()
                    .getNegOperatorEpl(), vcm.getStep());
            EPStatement s2 = epService.getEPAdministrator().createEPL(expression2);
            statements.add(s2);
            s2.addListener(this);

            // both, time window and value step are defined
        } else {

            String expression1 = String.format(eplExpTemplate1_a, tag.getId(), vcm.getTimeWindow(), tag.getId());
            EPStatement s1 = epService.getEPAdministrator().createEPL(expression1);
            statements.add(s1);
            s1.addListener(this);

            String expression2 = String.format(eplExpTemplate2_a, tag.getId(), tag.getId(), vcm.getOperator()
                    .getOperatorEpl(), vcm.getStep());
            EPStatement s2 = epService.getEPAdministrator().createEPL(expression2);
            statements.add(s2);
            s2.addListener(this);
        }

        registeredStatements.put(tag.getId(), statements);
        equipmentHandlers.put(tag.getId(), equipmentId);
    }

    public void unregister(ISourceDataTag tag) {
        if (!registeredStatements.containsKey(tag.getId())) {
            logger.warn(format("tag %d is not registered", tag.getId()));
            return;
        }
        for (EPStatement s : registeredStatements.get(tag.getId())) {
            s.stop();
            s.destroy();
        }// for

        registeredStatements.remove(tag.getId());
        equipmentHandlers.remove(tag.getId());
    }

    public void sendEvent(ValueChangeMonitorEvent event) {
        epService.getEPRuntime().sendEvent(event);
    }

    @Override
    public void update(EventBean[] newEvents, EventBean[] oldEvents) {

        EventBean event = newEvents[0];

        Long tagId = Long.parseLong(event.get("id").toString());
        Boolean tagValue = Boolean.parseBoolean(event.get("value").toString());
        String valueDescr = "evaluated by value-change-monitor";

        if (logger.isDebugEnabled()) {
            logger.debug(format("event: %s received [tagid :%s value: %s]", newEvents[0].getEventType().getName(),
                    tagId, tagValue.toString()));
        }

        if (kernel != null) {

            if (logger.isDebugEnabled()) {
                logger.debug(format("sending tag: %d with value: %s  descr: %s]", tagId, tagValue.toString(),
                        valueDescr));
            }

            EquipmentMessageHandler emh = kernel.getEquipmentMessageHandlersTable().get(equipmentHandlers.get(tagId));
            ISourceDataTag sdt = emh.getEquipmentConfiguration().getSourceDataTag(tagId);

            emh.getEquipmentMessageSender()
                    .sendTagFiltered(sdt, tagValue, System.currentTimeMillis(), valueDescr, true);
        } else {
            logger.warn(format("VCM engine is not operational. DriverKernel is not set."));
        }

    }

}
