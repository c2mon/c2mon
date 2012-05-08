DELETE FROM timconfigval;
DELETE FROM timconfigelt;
DELETE FROM timconfig;

-- test data for configuration module tests

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (1,'test configuration name', 'test configuration description', 'mbrightw', '?', sysdate);
     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1,1,'CREATE','DataTag','5000000');
  

  -- insert datatag config info
  
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'name','Config_test_datatag');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'description','test description config datatag');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'dataType','Float');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'mode','2');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'isLogged','false');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'unit','config unit m/sec');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'valueDictionary','20=value_description');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'dipAddress','testConfigDIPaddress');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'japcAddress','testConfigJAPCaddress');

insert into timconfigval (seqid, elementfield, elementvalue) values (1,'equipmentId','150');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'minValue','12.2');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'maxValue','23.3');
insert into timconfigval (seqid, elementfield, elementvalue) values (1,'address',
        '<DataTagAddress>
          <HardwareAddress class="ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl">
           <opc-item-name>CW_TEMP_IN_COND3</opc-item-name>         
          </HardwareAddress>        
        </DataTagAddress>');
        
-- insert controltag config info for a process and equipment using separate configuration

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (2,'create control tags', 'creates a control tag', 'mbrightw', '?', sysdate);
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (2,2,'CREATE','ControlTag','500');
        
insert into timconfigval (seqid, elementfield, elementvalue) values (2,'name','Process status');
insert into timconfigval (seqid, elementfield, elementvalue) values (2,'description','test');
insert into timconfigval (seqid, elementfield, elementvalue) values (2,'dataType','Integer');
insert into timconfigval (seqid, elementfield, elementvalue) values (2,'mode','2');
insert into timconfigval (seqid, elementfield, elementvalue) values (2,'isLogged','false');

insert into timconfigval (seqid, elementfield, elementvalue) values (2,'minValue','12');
insert into timconfigval (seqid, elementfield, elementvalue) values (2,'maxValue','22');

-- insert configuration for creating a command tag

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (3,'create command tag', 'creates a command tag', 'mbrightw', '?', sysdate);
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (3,3,'CREATE','CommandTag','10000');
  
          
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'name','Test CommandTag');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'description','test description');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'dataType','String');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'sourceRetries','2');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'sourceTimeout','200');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'mode','2');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'clientTimeout','30000');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'execTimeout','6000');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'equipmentId','150');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'rbacClass','RBAC class');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'rbacDevice','RBAC device');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'rbacProperty','RBAC property');
insert into timconfigval (seqid, elementfield, elementvalue) values (3,'hardwareAddress','<HardwareAddress class="ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>100</command-pulse-length></HardwareAddress>');

-- insert configuration for updating a datatag

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (4,'update test datatag', 'update datatag 5000000', 'mbrightw', '?', sysdate);
     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (4,4,'UPDATE','DataTag','5000000');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (4,'japcAddress','testConfigJAPCaddress2');

insert into timconfigval (seqid, elementfield, elementvalue) values (4,'maxValue','26');
insert into timconfigval (seqid, elementfield, elementvalue) values (4,'address',
        '<DataTagAddress>
          <HardwareAddress class="ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl">
           <opc-item-name>CW_TEMP_IN_COND4</opc-item-name>         
          </HardwareAddress>        
        </DataTagAddress>');
     
-- command tag update configuration

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (5,'update command tag', 'updates a command tag', 'mbrightw', '?', sysdate);
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (5,5,'UPDATE','CommandTag','10000');
            
insert into timconfigval (seqid, elementfield, elementvalue) values (5,'name','Test CommandTag Updated');
insert into timconfigval (seqid, elementfield, elementvalue) values (5,'rbacClass','new RBAC class');
insert into timconfigval (seqid, elementfield, elementvalue) values (5,'rbacDevice','new RBAC device');
-- updated command pulse length 100->150 in address
insert into timconfigval (seqid, elementfield, elementvalue) values (5,'hardwareAddress','<HardwareAddress class="ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>150</command-pulse-length></HardwareAddress>');

-- control tag update

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (6,'update control tags', 'updates a control tag', 'mbrightw', '?', sysdate);
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (6,6,'UPDATE','ControlTag','500');
        
insert into timconfigval (seqid, elementfield, elementvalue) values (6,'description','modified description');

-- remove datatag

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (7,'remove test datatag', 'remove datatag 200001', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (7,7,'REMOVE','DataTag','200001');
  
-- remove controltag

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (8,'remove test control', 'remove control 1250', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (8,8,'REMOVE','ControlTag','1250');
  
-- remove datatag

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (9,'remove test command', 'remove command 11000', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (9,9,'REMOVE','CommandTag','11000');
  
-- create ruletag
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (10,'create rule', 'creates a rule', 'mbrightw', '?', sysdate);
     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (10,10,'CREATE','RuleTag','50100');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (10,'name','test ruletag');
insert into timconfigval (seqid, elementfield, elementvalue) values (10,'description','test ruletag description');
insert into timconfigval (seqid, elementfield, elementvalue) values (10,'dataType','Float');
insert into timconfigval (seqid, elementfield, elementvalue) values (10,'mode','1');
insert into timconfigval (seqid, elementfield, elementvalue) values (10,'isLogged','true');
insert into timconfigval (seqid, elementfield, elementvalue) values (10,'unit','config unit m/sec');
insert into timconfigval (seqid, elementfield, elementvalue) values (10,'dipAddress','testConfigDIPaddress');
insert into timconfigval (seqid, elementfield, elementvalue) values (10,'japcAddress','testConfigJAPCaddress');

insert into timconfigval (seqid, elementfield, elementvalue) values (10,'ruleText','(#5000000 < 0)|(#5000000 > 200)[1],true[0]');

--update rule
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (11,'update rule', 'updates a rule', 'mbrightw', '?', sysdate);
     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (11,11,'UPDATE','RuleTag','50100');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (11,'japcAddress','newTestConfigJAPCaddress');
insert into timconfigval (seqid, elementfield, elementvalue) values (11,'ruleText','true[0]');

--remove rule
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (12,'remove test rule', 'remove rule 60007', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (12,12,'REMOVE','RuleTag','60007');
  
--create equipment (uses available control tags and TESTHANDLER3 Process for simplicity)
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (13,'create equipment', 'create equipment 110', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (13,13,'CREATE','Equipment','110');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (13,'address','serverHostName=VGTCVENTTEST');
insert into timconfigval (seqid, elementfield, elementvalue) values (13,'stateTagId','1250');
insert into timconfigval (seqid, elementfield, elementvalue) values (13,'processId','50');
insert into timconfigval (seqid, elementfield, elementvalue) values (13,'description','test description');
insert into timconfigval (seqid, elementfield, elementvalue) values (13,'handlerClass','ch.cern.tim.driver.');
insert into timconfigval (seqid, elementfield, elementvalue) values (13,'name','E_CONFIG_TEST');
insert into timconfigval (seqid, elementfield, elementvalue) values (13,'commFaultTagId','1252');

--update equipment (contains 2 elts in config: create controltag and update equipment)
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (25,'update equipment', 'update equipment 110', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (26,25,'UPDATE','Equipment','110');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (26,'address','serverHostName=VGTCVENTTEST;test');
insert into timconfigval (seqid, elementfield, elementvalue) values (26,'description','updated description');
insert into timconfigval (seqid, elementfield, elementvalue) values (26,'aliveTagId','1251');

-- test control tag that should be loaded to DAQ when equipment is updated (should be performed first 25<26);
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (25,25,'CREATE','ControlTag','501');
        
insert into timconfigval (seqid, elementfield, elementvalue) values (25,'name','Equipment alive');
insert into timconfigval (seqid, elementfield, elementvalue) values (25,'description','test');
insert into timconfigval (seqid, elementfield, elementvalue) values (25,'dataType','Integer');
insert into timconfigval (seqid, elementfield, elementvalue) values (25,'mode','2');
insert into timconfigval (seqid, elementfield, elementvalue) values (25,'isLogged','false');
insert into timconfigval (seqid, elementfield, elementvalue) values (25,'address',
        '<DataTagAddress><HardwareAddress class="ch.cern.tim.shared.datatag.address.impl.OPCHardwareAddressImpl"><opc-item-name>CW_TEMP_IN_COND4</opc-item-name></HardwareAddress></DataTagAddress>');

insert into timconfigval (seqid, elementfield, elementvalue) values (25,'minValue','12');
insert into timconfigval (seqid, elementfield, elementvalue) values (25,'maxValue','22');

--remove equipment
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (15,'remove equipment', 'remove equipment 150', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (15,15,'REMOVE','Equipment','150');
  
--remove another equipment with rules & alarms attached (from permanent test data)
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (29,'remove equipment', 'remove process 150', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (29,29,'REMOVE','Equipment','150');
  
 --create process (uses P_TESTHANDLER03 control tag)
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (16,'create process', 'create process 2', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (16,16,'CREATE','Process','2');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (16,'stateTagId','1220');
insert into timconfigval (seqid, elementfield, elementvalue) values (16,'aliveTagId','1221');
insert into timconfigval (seqid, elementfield, elementvalue) values (16,'description','test description');
insert into timconfigval (seqid, elementfield, elementvalue) values (16,'maxMessageDelay','1000');
insert into timconfigval (seqid, elementfield, elementvalue) values (16,'name','P_TEST');
insert into timconfigval (seqid, elementfield, elementvalue) values (16,'maxMessageSize','200');

--update process
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (17,'update process', 'update process 2', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (17,17,'UPDATE','Process','2');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (17,'maxMessageDelay','4000');
insert into timconfigval (seqid, elementfield, elementvalue) values (17,'description','updated description');

--remove process
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (18,'remove process', 'remove process 2', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (18,18,'REMOVE','Process','2');

--remove another process with rules & alarms attached (from permanent test data)
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (28,'remove process', 'remove process 50', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (28,28,'REMOVE','Process','50');

--create subequipment (as child of TestHandler03, uses pre-existing control tags ) - should succeed
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (19,'create subequipment', 'create subequipment 200', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (19,19,'CREATE','SubEquipment','200');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (19,'stateTagId','1250');
insert into timconfigval (seqid, elementfield, elementvalue) values (19,'aliveTagId','1251');
insert into timconfigval (seqid, elementfield, elementvalue) values (19,'aliveInterval','30000');
insert into timconfigval (seqid, elementfield, elementvalue) values (19,'commFaultTagId','1252');
insert into timconfigval (seqid, elementfield, elementvalue) values (19,'parent_equip_id','150');
insert into timconfigval (seqid, elementfield, elementvalue) values (19,'description','test description');
insert into timconfigval (seqid, elementfield, elementvalue) values (19,'name','SUB_E_TEST');

--update subequipment (should fail as attempt to update parent)
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (20,'update subequipment', 'update subequipment 200', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (20,20,'UPDATE','SubEquipment','200');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (20,'parent_equip_id','151');

--remove subequipment - should succeed
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (21,'remove subequipment', 'remove subequipment 250', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (21,21,'REMOVE','SubEquipment','250');

  --create alarm - should succeed
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (22,'create alarm', 'create alarm 300000', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (22,22,'CREATE','Alarm','300000');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (22,'dataTagId','200003'); --attached to data tag
insert into timconfigval (seqid, elementfield, elementvalue) values (22,'faultFamily','fault family');
insert into timconfigval (seqid, elementfield, elementvalue) values (22,'faultMember','fault member');
insert into timconfigval (seqid, elementfield, elementvalue) values (22,'faultCode','223');
insert into timconfigval (seqid, elementfield, elementvalue) values (22,'alarmCondition','<AlarmCondition class="ch.cern.tim.shared.alarm.ValueAlarmCondition"><alarm-value type="Boolean">true</alarm-value></AlarmCondition>');

--update alarm - should succeed
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (23,'update alarm', 'update alarm 300000', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (23,23,'UPDATE','Alarm','300000');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (23,'faultFamily','updated fault family');

--remove alarm - should succeed
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (24,'remove alarm', 'remove alarm 350000', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (24,24,'REMOVE','Alarm','350000');
  
--remove tag with attached alarm - should succeed
insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
  values (27,'remove tag with alarm', 'remove tag 60000 with alarm', 'mbrightw', '?', sysdate);     
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (27,27,'REMOVE','RuleTag','60000');
