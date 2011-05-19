-- schema needed for the configuration module; 
-- it is self-contained and can be used to create schema for testing

CREATE TABLE timconfig (
 configid NUMBER  NOT NULL
,configname VARCHAR2(35) NOT NULL
,configdesc VARCHAR2(100)
,author  VARCHAR2(35) DEFAULT 'TIMCONFIG' NOT NULL
,configstate VARCHAR2(1) DEFAULT 'D' NOT NULL
,createdate DATE DEFAULT SYSDATE NOT NULL
,applydate DATE
,status  VARCHAR2(3)
);

ALTER TABLE timconfig
ADD CONSTRAINT TIMCONFIG_PK PRIMARY KEY (configid) USING INDEX;

CREATE TABLE timconfigelt (
seqid  NUMBER  NOT NULL
,configid NUMBER  NOT NULL
,modetype VARCHAR2(12) NOT NULL
,elementtype VARCHAR2(25) NOT NULL
,elementpkey VARCHAR2(30) NOT NULL
,as_status VARCHAR2(500)
);

ALTER TABLE timconfigelt
ADD CONSTRAINT TIMCONFIGELT_PK PRIMARY KEY (seqid) USING INDEX;

ALTER TABLE timconfigelt
ADD CONSTRAINT TIMCONFIGELT_FK FOREIGN KEY (configid) REFERENCES timconfig (configid);

CREATE INDEX TCE_CONID_ELPKEY_IDX ON TIMCONFIGELT (CONFIGID,ELEMENTPKEY);

CREATE TABLE timconfigval (
seqid  NUMBER
,elementfield VARCHAR2(240)
,elementvalue VARCHAR2(4000)
,configid NUMBER
);

ALTER TABLE timconfigval
ADD CONSTRAINT TIMCONFIGVAL_PK PRIMARY KEY (seqid, elementfield) USING INDEX;

ALTER TABLE timconfigval
ADD CONSTRAINT TIMCONFIGVAL_FK FOREIGN KEY (seqid) REFERENCES timconfigelt (seqid);
