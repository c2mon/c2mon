-- DAQ PROCESS
INSERT INTO PROCESS (PROCID, PROCNAME, PROCDESC, PROCSTATE_TAGID, PROCALIVE_TAGID, PROCALIVEINTERVAL, PROCMAXMSGSIZE, PROCMAXMSGDELAY, PROCSTATE)
VALUES (12,'P_DB01','PROCESS DB01',210,211,60000,100,300,'DOWN');

-- EQUIPMENT
INSERT INTO EQUIPMENT (EQID, EQNAME, EQDESC, EQHANDLERCLASS, EQSTATE_TAGID, EQALIVEINTERVAL, EQCOMMFAULT_TAGID, EQ_PROCID, EQADDRESS)
VALUES (52, 'E_DB_DB01', 'DB01 EQUIPMENT', 'cern.c2mon.driver.db.DBMessageHandler', 212, 60000, 213, 12,
'dbName=timdb-dev;dbUrl=jdbc:oracle:thin:@oradev10.cern.ch:10520:D10;dbUsername=timdbdaq;dbPassword=***');

-- SUBEQUIPMENT EDMS
INSERT INTO EQUIPMENT (EQID, EQNAME, EQDESC, EQHANDLERCLASS, EQSTATE_TAGID, EQALIVEINTERVAL, EQCOMMFAULT_TAGID, EQ_PARENT_ID, EQALIVE_TAGID)
VALUES (521, 'E_DB_SUB_EDMS', 'SUBEQUIPMENT OF DB01', '-', 215, 60000, 216, 52, 217);

-- SUBEQUIPMENT ZABBIX
INSERT INTO EQUIPMENT (EQID, EQNAME, EQDESC, EQHANDLERCLASS, EQSTATE_TAGID, EQALIVEINTERVAL, EQCOMMFAULT_TAGID, EQ_PARENT_ID, EQALIVE_TAGID)
VALUES (522, 'E_DB_SUB_ZABBIX', 'SUBEQUIPMENT OF DB01', '-', 218, 60000, 219, 52, 230);


-- CONTROL TAGS --
-- process control tags
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (210, 'P_DB01:STATUS', 'TEST', 0,'String', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (211, 'P_DB01:ALIVE', 'TEST', 0,'Integer', 1, 0, 0, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);

-- equipment control tags
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (212, 'E_DB_DB01:STATUS',  'TEST', 0,'String', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (213, 'E_DB_DB01:COMM_FAULT', 'TEST',  0,'Boolean', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (214, 'E_DB_DB01:ALIVE', 'TEST',  0,'Integer', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);

-- subequipment control tags
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (215, 'E_SUB_EDMS:STATUS', 'TEST',  0,'String', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (216, 'E_SUB_EDMS:COMM_FAULT', 'TEST',  0,'Boolean', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP, TAGADDRESS)
VALUES (217, 'E_SUB_EDMS:ALIVE', 'TEST',  0,'Integer', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP,
'<DataTagAddress><HardwareAddress class="cern.c2mon.shared.datatag.address.impl.DBHardwareAddressImpl"><db-item-name>EDMS.CEDARMGR.TEST_DATATAG_ALIVE</db-item-name></HardwareAddress></DataTagAddress>');

-- subequipment control tags
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (218, 'E_SUB_ZABBIX:STATUS',  'TEST', 0,'String', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP)
VALUES (219, 'E_SUB_ZABBIX:COMM_FAULT',  'TEST', 0,'Boolean', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP);
INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAGTIMESTAMP, TAGSRVTIMESTAMP, TAGADDRESS)
VALUES (230, 'E_SUB_ZABBIX:ALIVE',  'TEST', 0,'Integer', 1, 0, 1, 1, 'UNINITIALISED', SYSTIMESTAMP, SYSTIMESTAMP,
'<DataTagAddress><HardwareAddress class="cern.c2mon.shared.datatag.address.impl.DBHardwareAddressImpl"><db-item-name>ZABBIX.DB_ACCOUNT.FRANCESCO_TEST_DATATAG_ALIVE</db-item-name></HardwareAddress></DataTagAddress>');


-- routine to insert test datatags:
DECLARE

l_id_count 		NUMBER;
l_insert_stmt  	VARCHAR2(2000);                                                                                   -- the dynamic insert statements

BEGIN

-- insert a datatag --
l_insert_stmt := 'INSERT INTO DATATAG (TAGID, TAGNAME, TAGDESC, TAGMODE, TAGDATATYPE, TAGCONTROLTAG, TAGSIMULATED, TAGLOGGED, TAGQUALITYCODE, TAGQUALITYDESC, TAG_EQID,
																						TAGTIMESTAMP, TAGSRVTIMESTAMP, TAGADDRESS)
					VALUES (:value1, :value2, ''test datatag'', 0, :value3, 0, 0, 1, 1, ''UNINITIALISED'', 52,
					SYSTIMESTAMP, SYSTIMESTAMP,
                  ''<DataTagAddress><HardwareAddress class="cern.c2mon.shared.datatag.address.impl.DBHardwareAddressImpl"><db-item-name>''||:value4||''</db-item-name></HardwareAddress></DataTagAddress>'')';

-- standard datatags for SUB_EDMS
FOR l_id IN 405000..405100 LOOP
  EXECUTE IMMEDIATE l_insert_stmt
  USING l_id
        , 'EDMSDB.CEDARMGR.TEST_DATATAG_' || l_id
        , 'Float'
        , 'EDMSDB.CEDARMGR.TEST_DATATAG_' || l_id ;
END LOOP;

-- standard Boolean datatags for SUB_ZABBIX
FOR l_id IN 406000..406010 LOOP
  EXECUTE IMMEDIATE l_insert_stmt
  USING l_id
        , 'ZABBIX.DB_ACCOUNT.FRANCESCO_TEST_DATATAG_' || l_id
        , 'Boolean'
        , 'ZABBIX.DB_ACCOUNT.FRANCESCO_TEST_DATATAG_' || l_id ;
END LOOP;

-- standard Integer datatags for SUB_ZABBIX
FOR l_id IN 406011..406020 LOOP
  EXECUTE IMMEDIATE l_insert_stmt
  USING l_id
        , 'ZABBIX.DB_ACCOUNT.FRANCESCO_TEST_DATATAG_' || l_id
        , 'Integer'
        , 'ZABBIX.DB_ACCOUNT.FRANCESCO_TEST_DATATAG_' || l_id ;
END LOOP;

END;

-- routine to insert test control and standard datatags on the timdbdaq account (equipment account)
DECLARE

ec	NUMBER(10);
et	VARCHAR2(250);

BEGIN
dbms_output.enable;
tests.insert_random_data (
		p_dbd_name_like 			=>	'ZABBIX.DB_ACCOUNT.FRANCESCO_TEST_DATATAG'
		,p_dbd_data_type			=>	'Boolean'
		,p_rows_count				=>	10
		,p_start_tag_id             =>  406000
		,p_exitcode					=>	ec
		,p_exittext					=>	et
);
dbms_output.put_line('exitcode: ' || ec || '; exittext: ' || et);

tests.insert_random_data (
		p_dbd_name_like 			=>	'ZABBIX.DB_ACCOUNT.FRANCESCO_TEST_DATATAG'
		,p_dbd_data_type			=>	'Integer'
		,p_rows_count				=>	10
		,p_start_tag_id             =>  406011
		,p_exitcode					=>	ec
		,p_exittext					=>	et
);
dbms_output.put_line('exitcode: ' || ec || '; exittext: ' || et);

tests.INSERT_ALIVE_TAG(
         p_alive_tag_id				=>	230
		,p_dbd_name					=> 'E_SUB_ZABBIX:ALIVE'
		,p_exitcode 				=>	ec
		,p_exittext 				=>	et
		);
dbms_output.put_line('exitcode: ' || ec || '; exittext: ' || et);

tests.insert_random_data (
		p_dbd_name_like 			=>	'EDMSDB.CEDARMGR.TEST_DATATAG'
		,p_dbd_data_type			=>	'Float'
		,p_rows_count				=>	100
		,p_start_tag_id             =>  405000
		,p_exitcode					=>	ec
		,p_exittext					=>	et
);
dbms_output.put_line('exitcode: ' || ec || '; exittext: ' || et);

tests.INSERT_ALIVE_TAG(
         p_alive_tag_id				=>	217
		,p_dbd_name					=>  'E_SUB_EDMS:ALIVE'
		,p_exitcode 				=>	ec
		,p_exittext 				=>	et
		);
dbms_output.put_line('exitcode: ' || ec || '; exittext: ' || et);


END;




