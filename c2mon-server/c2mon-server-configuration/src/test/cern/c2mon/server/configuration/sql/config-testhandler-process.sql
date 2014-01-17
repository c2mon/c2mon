-- Configuration used to create a TestHandler process to check on the test C2MON system.

insert into timconfig (configid, configname, configdesc, author, configstate, createdate)
     values (10000,'Create TestHandler DAQ', 'Creates a TestHandler DAQ.', 'mbrightw', '?', sysdate);
--remove process first
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000000,10000,'REMOVE','Process','25');

-- create Process control tags   
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000001,10000,'CREATE','ControlTag','1000');
        
insert into timconfigval (seqid, elementfield, elementvalue) values (1000001,'name','CM.PRE.P_TESTHANDLER01:STATUS');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000001,'description','DEFAUT FONCTIONNEMENT DAQ TIM');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000001,'dataType','String');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000001,'mode','0');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000001,'isLogged','true');

insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000002,10000,'CREATE','ControlTag','1001');
        
insert into timconfigval (seqid, elementfield, elementvalue) values (1000002,'name','CM.PRE.P_TESTHANDLER01:PROCESS_ALIVE');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000002,'description','PROCESS ALIVE MESSAGE');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000002,'dataType','Long');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000002,'mode','0');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000002,'isLogged','false');

-- create Process
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000003,10000,'CREATE','Process','25');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (1000003,'stateTagId','1000');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000003,'aliveTagId','1001');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000003,'description','TIM process for testing purposes');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000003,'aliveInterval','60000');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000003,'maxMessageDelay','1000');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000003,'name','P_TESTHANDLER01');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000003,'maxMessageSize','100');

--create Equipment control tags

insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000004,10000,'CREATE','ControlTag','1002');
        
insert into timconfigval (seqid, elementfield, elementvalue) values (1000004,'name','CM.MEY.TIMDB:STATUS');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000004,'description','DEFAUT FONCTIONNEMENT EQUIPEMENT TIM');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000004,'dataType','String');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000004,'mode','0');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000004,'isLogged','true');

insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000005,10000,'CREATE','ControlTag','1003');
        
insert into timconfigval (seqid, elementfield, elementvalue) values (1000005,'name','CM.MEY.TIMDB:EQUIP_ALIVE');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000005,'description','PROCESS ALIVE MESSAGE');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000005,'dataType','Long');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000005,'mode','0');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000005,'isLogged','false');

insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000006,10000,'CREATE','ControlTag','1004');
        
insert into timconfigval (seqid, elementfield, elementvalue) values (1000006,'name','CM.MEY.TIMDB:COMM_FAULT');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000006,'description','DEFAUT COMMUNICATION');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000006,'dataType','Boolean');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000006,'mode','0');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000006,'isLogged','false');

-- create Equipment
insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000007,10000,'CREATE','Equipment','100');
  
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'address','interval=1000;eventProb=0.1;inRangeProb=1;outDeadBandProb=1;switchProb=1;startIn=0.2;aliveInterval=60000');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'stateTagId','1002');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'aliveTagId','1003');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'aliveInterval','60000');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'processId','25');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'description','Test MessageHandler');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'handlerClass','cern.c2mon.driver.testhandler.TestMessageHandler');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'name','E_TEST_TESTHANDLER01');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000007,'commFaultTagId','1004');

-- add CommandTag

insert into timconfigelt (seqid, configid, modetype, elementtype, elementpkey)
  values (1000008,10000,'CREATE','CommandTag','10000');
          
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'name','TESTHANDLER_03_TEST_COMMAND');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'description','Test Command Tag');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'dataType','Boolean');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'sourceRetries','2');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'sourceTimeout','200');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'mode','0');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'clientTimeout','30000');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'execTimeout','6000');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'equipmentId','100');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'rbacClass','RBAC class');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'rbacDevice','RBAC device');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'rbacProperty','RBAC property');
insert into timconfigval (seqid, elementfield, elementvalue) values (1000008,'hardwareAddress','<HardwareAddress class="cern.c2mon.shared.common.datatag.address.impl.OPCHardwareAddressImpl"><opc-item-name>PLC_B_CMD_ACQ_DEF_5A6</opc-item-name><command-pulse-length>100</command-pulse-length></HardwareAddress>');


