-- generic schema needed for the configuration module; 
-- it is self-contained and can be used to create schema for testing

CREATE TABLE timconfig (
 configid INTEGER  NOT NULL
,configname VARCHAR(35) NOT NULL
,configdesc VARCHAR(100)
,author  VARCHAR(35) DEFAULT 'TIMCONFIG' NOT NULL
,configstate VARCHAR(1) DEFAULT 'D' NOT NULL
,createdate DATE DEFAULT SYSDATE NOT NULL
,applydate DATE
,status  VARCHAR(3)
);

ALTER TABLE timconfig
ADD CONSTRAINT TIMCONFIG_PK PRIMARY KEY (configid);

CREATE TABLE timconfigelt (
seqid  INTEGER  NOT NULL
,configid INTEGER  NOT NULL
,modetype VARCHAR(12) NOT NULL
,elementtype VARCHAR(25) NOT NULL
,elementpkey VARCHAR(30) NOT NULL
,as_status VARCHAR(500)
,daq_status VARCHAR(20)
);

ALTER TABLE timconfigelt
ADD CONSTRAINT TIMCONFIGELT_PK PRIMARY KEY (seqid);

ALTER TABLE timconfigelt
ADD CONSTRAINT TIMCONFIGELT_FK FOREIGN KEY (configid) REFERENCES timconfig (configid);

CREATE INDEX TCE_CONID_ELPKEY_IDX ON TIMCONFIGELT (CONFIGID,ELEMENTPKEY);

CREATE TABLE timconfigval (
seqid  INTEGER
,elementfield VARCHAR(240) NOT NULL
,elementvalue VARCHAR(4000) NOT NULL
,configid INTEGER
);

ALTER TABLE timconfigval
ADD CONSTRAINT TIMCONFIGVAL_PK PRIMARY KEY (seqid, elementfield);

ALTER TABLE timconfigval
ADD CONSTRAINT TIMCONFIGVAL_FK FOREIGN KEY (seqid) REFERENCES timconfigelt (seqid);
