package cern.c2mon.server.configuration.parser.util;

import cern.c2mon.shared.client.configuration.api.Configuration;
import cern.c2mon.shared.client.configuration.api.alarm.Alarm;
import cern.c2mon.shared.client.configuration.api.equipment.Equipment;
import cern.c2mon.shared.client.configuration.api.equipment.SubEquipment;
import cern.c2mon.shared.client.configuration.api.process.Process;
import cern.c2mon.shared.client.configuration.api.tag.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.builderAlarmWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAlarmUtil.builderAlarmWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAliveTagUtil.builderAliveTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationAliveTagUtil.builderAliveTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommandTagUtil.builderCommandTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommandTagUtil.builderCommandTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationStatusTagUtil.builderStatusTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommFaultTagUtil.builderCommFaultTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationCommFaultTagUtil.builderCommFaultTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.builderDataTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationDataTagUtil.builderDataTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.builderEquipmentWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationEquipmentUtil.builderEquipmentWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.builderProcessWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationProcessUtil.builderProcessWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil.builderRuleTagWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationRuleTagUtil.builderRuleTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationStatusTagUtil.builderStatusTagWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil.builderSubEquipmentWithAllFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationSubEquipmentUtil.builderSubEquipmentWithPrimFields;
import static cern.c2mon.server.configuration.parser.util.ConfigurationUtil.getConfBuilder;

public class ConfigurationAllTogetherUtil {

  /**
   * returns a Configuration which build ALL possible configurations ones
   * processId: 0
   * equipmentId: 1
   * subEquipmentId: 2
   *
   * @return
   */
  public static Pair<Configuration, List<Properties>> buildAllMandatory() {
    List<Properties> result = new ArrayList<>();
    Long id = 0l;

    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithPrimFields(id++, 24l, 6l); // 0
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithPrimFields(id++, 0l, 25l ,7l, 4l ); // 1
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipment = builderSubEquipmentWithPrimFields(id++, 1l, 26l ,8l, 5l ); // 2

    id++;
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithPrimFields(id++, "equipment", 1l); // 4
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagS = builderCommFaultTagWithPrimFields(id++, "subEquipment", 2l); // 5

    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithPrimFields(id++, "process", 0l); // 6
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithPrimFields(id++, "equipment", 1l); // 7
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagS = builderAliveTagWithPrimFields(id++, "subEquipment", 2l); // 8

    Pair<DataTag.DataTagBuilder, Properties> dataTagE = builderDataTagWithPrimFields(id++, "equipment", 1l); // 9
    Pair<DataTag.DataTagBuilder, Properties> dataTagS = builderDataTagWithPrimFields(id++, "subEquipment", 2l); // 10

    Pair<RuleTag.RuleTagBuilder, Properties> ruleTag = builderRuleTagWithPrimFields(id++); // 11

    id++;
    Pair<Alarm.AlarmBuilder, Properties> alarmAP = builderAlarmWithPrimFields(id++, "aliveTag", 6l); // 13
    Pair<Alarm.AlarmBuilder, Properties> alarmCE = builderAlarmWithPrimFields(id++, "commFaultTag", 4l); // 14
    Pair<Alarm.AlarmBuilder, Properties> alarmAE = builderAlarmWithPrimFields(id++, "aliveTag", 7l); // 15
    Pair<Alarm.AlarmBuilder, Properties> alarmDE = builderAlarmWithPrimFields(id++, "dataTag", 9l); // 16
    Pair<Alarm.AlarmBuilder, Properties> alarmCS = builderAlarmWithPrimFields(id++, "commFaultTag", 5l); // 17
    Pair<Alarm.AlarmBuilder, Properties> alarmAS = builderAlarmWithPrimFields(id++, "aliveTag", 8l); // 18
    Pair<Alarm.AlarmBuilder, Properties> alarmDS = builderAlarmWithPrimFields(id++, "dataTag", 10l); // 19
    Pair<Alarm.AlarmBuilder, Properties> alarmR = builderAlarmWithPrimFields(id++, "ruleTag", 11l); // 20
    Pair<Alarm.AlarmBuilder, Properties> alarmSP = builderAlarmWithPrimFields(id++, "statusTag", 24l); // 21
    Pair<Alarm.AlarmBuilder, Properties> alarmSE = builderAlarmWithPrimFields(id++, "statusTag", 25l); // 22
    Pair<Alarm.AlarmBuilder, Properties> alarmSS = builderAlarmWithPrimFields(id++, "statusTag", 26l); // 23

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithPrimFields(id++, "process", 0l); // 24
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithPrimFields(id++, "equipment", 1l); // 25
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagS = builderStatusTagWithPrimFields(id++, "subEquipment", 2l); // 26

    Pair<CommandTag.CommandTagBuilder, Properties> commandTag = builderCommandTagWithPrimFields(id++, 1L); //27

    Configuration conf = getConfBuilder()
        .process(process._1.aliveTag(aliveTagP._1.alarm(alarmAP._1.build()).build()).stateTag(statusTagP._1.alarm(alarmSP._1.build()).build())
            .equipment(equipment._1.commFaultTag(commFaultTagE._1.alarm(alarmCE._1.build()).build()).aliveTag(aliveTagE._1.alarm(alarmAE._1.build()).build()).stateTag(statusTagE._1.alarm(alarmSE._1.build()).build()).dataTag((DataTag<Number>) dataTagE._1.alarm(alarmDE._1.build()).build()).commandTag(commandTag._1.build())
                .subEquipment(subEquipment._1.commFaultTag(commFaultTagS._1.alarm(alarmCS._1.build()).build()).aliveTag(aliveTagS._1.alarm(alarmAS._1.build()).build()).stateTag(statusTagS._1.alarm(alarmSS._1.build()).build()).dataTag((DataTag<Number>) dataTagS._1.alarm(alarmDS._1.build()).build())
                    .build()).build()).build())
        .rule(ruleTag._1.alarm(alarmR._1.build()).build()).build();


    result.add(statusTagP._2);
    result.add(aliveTagP._2);
    result.add(statusTagE._2);
    result.add(commFaultTagE._2);
    result.add(aliveTagE._2);
    result.add(statusTagS._2);
    result.add(commFaultTagS._2);
    result.add(aliveTagS._2);
    result.add(process._2);
    result.add(equipment._2);
    result.add(subEquipment._2);
    result.add(dataTagE._2);
    result.add(dataTagS._2);
    result.add(ruleTag._2);

    result.add(alarmSP._2);
    result.add(alarmAP._2);
    result.add(alarmSE._2);
    result.add(alarmCE._2);
    result.add(alarmAE._2);
    result.add(alarmDE._2);
    result.add(alarmSS._2);
    result.add(alarmCS._2);
    result.add(alarmAS._2);
    result.add(alarmDS._2);

    result.add(alarmR._2);
    result.add(commandTag._2);

    return new Pair<>(conf, result);
  }

  public static Pair<Configuration, List<Properties>> buildAllWithAllFields() {
    List<Properties> result = new ArrayList<>();
    Long id = 0l;

    Pair<Process.ProcessBuilder, Properties> process = builderProcessWithAllFields(id++, 24l, 6l); // 0
    Pair<Equipment.EquipmentBuilder, Properties> equipment = builderEquipmentWithAllFields(id++, 0l, 25l ,4l, 7l ); // 1
    Pair<SubEquipment.SubEquipmentBuilder, Properties> subEquipment = builderSubEquipmentWithAllFields(id++, 1l, 26l ,5l, 8l ); // 2

    id++;
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagE = builderCommFaultTagWithAllFields(id++, "equipment", 1l); // 4
    Pair<CommFaultTag.CommFaultTagBuilder, Properties> commFaultTagS = builderCommFaultTagWithAllFields(id++, "subEquipment", 2l); // 5

    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagP = builderAliveTagWithAllFields(id++, "process", 0l); // 6
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagE = builderAliveTagWithAllFields(id++, "equipment", 1l); // 7
    Pair<AliveTag.AliveTagBuilder, Properties> aliveTagS = builderAliveTagWithAllFields(id++, "subEquipment", 2l); // 8

    Pair<DataTag.DataTagBuilder, Properties> dataTagE = builderDataTagWithAllFields(id++, "equipment", 1l); // 9
    Pair<DataTag.DataTagBuilder, Properties> dataTagS = builderDataTagWithAllFields(id++, "subEquipment", 2l); // 10

    Pair<RuleTag.RuleTagBuilder, Properties> ruleTag = builderRuleTagWithAllFields(id++); // 11

    id++;
    Pair<Alarm.AlarmBuilder, Properties> alarmAP = builderAlarmWithAllFields(id++, 6l); // 13
    Pair<Alarm.AlarmBuilder, Properties> alarmCE = builderAlarmWithAllFields(id++, 4l); // 14
    Pair<Alarm.AlarmBuilder, Properties> alarmAE = builderAlarmWithAllFields(id++, 7l); // 15
    Pair<Alarm.AlarmBuilder, Properties> alarmDE = builderAlarmWithAllFields(id++, 9l); // 16
    Pair<Alarm.AlarmBuilder, Properties> alarmCS = builderAlarmWithAllFields(id++, 5l); // 17
    Pair<Alarm.AlarmBuilder, Properties> alarmAS = builderAlarmWithAllFields(id++, 8l); // 18
    Pair<Alarm.AlarmBuilder, Properties> alarmDS = builderAlarmWithAllFields(id++, 10l); // 19
    Pair<Alarm.AlarmBuilder, Properties> alarmR = builderAlarmWithAllFields(id++, 11l); // 20
    Pair<Alarm.AlarmBuilder, Properties> alarmSP = builderAlarmWithAllFields(id++, 24l); // 21
    Pair<Alarm.AlarmBuilder, Properties> alarmSE = builderAlarmWithAllFields(id++, 25l); // 22
    Pair<Alarm.AlarmBuilder, Properties> alarmSS = builderAlarmWithAllFields(id++, 26l); // 23

    Pair<StatusTag.StatusTagBuilder, Properties> statusTagP = builderStatusTagWithAllFields(id++, "process", 0l); // 24
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagE = builderStatusTagWithAllFields(id++, "equipment", 1l); // 25
    Pair<StatusTag.StatusTagBuilder, Properties> statusTagS = builderStatusTagWithAllFields(id++, "subEquipment", 2l); // 26

    Pair<CommandTag.CommandTagBuilder, Properties> commandTag = builderCommandTagWithAllFields(id++, 1L); //27

    Configuration conf = getConfBuilder()
        .process(process._1.aliveTag(aliveTagP._1.alarm(alarmAP._1.build()).build()).stateTag(statusTagP._1.alarm(alarmSP._1.build()).build())
            .equipment(equipment._1.commFaultTag(commFaultTagE._1.alarm(alarmCE._1.build()).build()).aliveTag(aliveTagE._1.alarm(alarmAE._1.build()).build()).stateTag(statusTagE._1.alarm(alarmSE._1.build()).build()).dataTag((DataTag<Number>) dataTagE._1.alarm(alarmDE._1.build()).build()).commandTag(commandTag._1.build())
                .subEquipment(subEquipment._1.commFaultTag(commFaultTagS._1.alarm(alarmCS._1.build()).build()).aliveTag(aliveTagS._1.alarm(alarmAS._1.build()).build()).stateTag(statusTagS._1.alarm(alarmSS._1.build()).build()).dataTag((DataTag<Number>) dataTagS._1.alarm(alarmDS._1.build()).build())
                    .build()).build()).build())
        .rule(ruleTag._1.alarm(alarmR._1.build()).build()).build();


    result.add(statusTagP._2);
    result.add(aliveTagP._2);
    result.add(statusTagE._2);
    result.add(commFaultTagE._2);
    result.add(aliveTagE._2);
    result.add(statusTagS._2);
    result.add(commFaultTagS._2);
    result.add(aliveTagS._2);
    result.add(process._2);
    result.add(equipment._2);
    result.add(subEquipment._2);
    result.add(dataTagE._2);
    result.add(dataTagS._2);
    result.add(ruleTag._2);

    result.add(alarmSP._2);
    result.add(alarmAP._2);
    result.add(alarmSE._2);
    result.add(alarmCE._2);
    result.add(alarmAE._2);
    result.add(alarmDE._2);
    result.add(alarmSS._2);
    result.add(alarmCS._2);
    result.add(alarmAS._2);
    result.add(alarmDS._2);

    result.add(alarmR._2);
    result.add(commandTag._2);

    return new Pair<>(conf, result);
  }
}
