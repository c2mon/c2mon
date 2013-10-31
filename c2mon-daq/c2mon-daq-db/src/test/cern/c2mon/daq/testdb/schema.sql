drop table DB_DAQ if exists;

create table DB_DAQ
( DBD_TAG_ID			   NUMBER(10) 	 NOT NULL,	 -- id of the TAG
 ,DBD_NAME                 VARCHAR(100)  NOT NULL    -- this corresponds to an "address" of a TAG  (probably will contain: DBname, user, unique ID for database'
 ,DBD_VALUE                VARCHAR(2000) NOT NULL    -- value (we do not want to accept NULL, client must adapt)
 ,DBD_DATA_TYPE            VARCHAR(30)   NOT NULL    -- datatype of the value
 ,DBD_SOURCE_TIMESTAMP     TIMESTAMP(3)  NOT NULL    -- timestamp at the source
 ,DBD_QUALITY              NUMBER                    -- quality of the value
 ,DBD_QUALITY_DESCRIPTION  VARCHAR(500)              -- quality descrptiom
 ,DBD_CRE_DAT              DATE                      -- registration date (in this table)
	CONSTRAINT DB_DAQ_PK PRIMARY KEY (DBD_NAME),
	CONSTRAINT DB_DAQ_TAG_ID UNIQUE (DBD_TAG_ID));
);

CREATE OR REPLACE TRIGGER on_update
AFTER UPDATE ON db_daq
FOR EACH ROW
DECLARE
l_prepared_timestamp	VARCHAR2(50);
l_message				VARCHAR2(1800);
BEGIN
	dbms_lock.sleep(0.01);
	l_prepared_timestamp := rpad(to_char(:new.dbd_source_timestamp,'dd/mm/yyyy hh24:mi:ss.ff'), 23);
	-- 23 is the length of the timestamp with precision to a milisecond (3 places after the '.')
	-- 1800 is the limitation of the message length that can be passed in the signal procedure
	l_message := substr(:new.dbd_name || ';' || :new.dbd_value || ';' || l_prepared_timestamp || ';' || :new.dbd_quality || ';' || :new.dbd_quality_description, 1, 1800);
	dbms_alert.signal( :old.dbd_tag_id , l_message);
END;
