-- test data should be inserted into the following intervals
-- PROCESS
--    free: 1-49
--  inserted: 50-99
-- EQUIPMENT
--    free: 100-149
--  inserted : 150-199
-- SUBEQUIPMENT
--    free: 200-249
--  inserted : 250-299   
-- TAGS:
-- datatags:
--        free: 100000-199999
--      inserted : 200000-299999
-- controltags:
--        free: 1000-1199
--      inserted: 1200-9999
-- COMMANDS:
-- free: 10000-10999
-- inserted: 11000-49999
-- RULES
--  free: 50000-59999
-- inserted : 60000-99999
-- ALARMS
--  free: 300000-349999
--  inserted: 350000-399999

-- cache database schema
-- IMPORTANT: note the DATATAG table sets an initial size of 100M, which may need modifying for
--            accounts with smaller quotas, and it unnecessarily large for the component tests

CREATE TABLE ALARM (
  ALARMID         NUMBER(9) NOT NULL,
  ALARM_TAGID     NUMBER(9),
  ALARMFFAMILY    VARCHAR2(20) NOT NULL,
  ALARMFMEMBER    VARCHAR2(64) NOT NULL,
  ALARMFCODE      NUMBER(9) NOT NULL,
  ALARMSTATE      VARCHAR2(10),
  ALARMTIME       TIMESTAMP(6),
  ALARMINFO       VARCHAR2(100),
  ALARMCONDITION  VARCHAR2(500)),
  ALA_PUBLISHED   NUMBER(1),
  ALA_PUB_STATE   VARCHAR2(10),
  ALA_PUB_TIME    TIMESTAMP(6),
  ALA_PUB_INFO    VARCHAR2(100);

ALTER TABLE ALARM ADD (
  CONSTRAINT PK_ALARM_ALARMID PRIMARY KEY (ALARMID));

CREATE INDEX I_ALARM_TAGID ON ALARM (ALARM_TAGID);

--

CREATE TABLE COMMANDTAG (
  CMDID               NUMBER(9),
  CMDNAME             VARCHAR2(60) NOT NULL,
  CMDDESC             VARCHAR2(100),
  CMDMODE             NUMBER(1) NOT NULL,
  CMDDATATYPE         VARCHAR2(10) NOT NULL,
  CMDSOURCERETRIES    NUMBER(1) NOT NULL,
  CMDSOURCETIMEOUT    NUMBER(6) NOT NULL,
  CMDEXECTIMEOUT      NUMBER(6) NOT NULL,
  CMDCLIENTTIMEOUT    NUMBER(6) NOT NULL,
  CMDRBACCLASS        VARCHAR2(50),
  CMDRBACDEVICE       VARCHAR2(50),
  CMDRBACPROPERTY     VARCHAR2(50),
  CMDHARDWAREADDRESS  VARCHAR2(4000) NOT NULL,
  CMDMINVALUE         BLOB NOT NULL,
  CMDMAXVALUE         BLOB NOT NULL,
  CMD_EQID            NUMBER(9));

ALTER TABLE COMMANDTAG ADD (
  PRIMARY KEY (CMDID),
  UNIQUE (CMDNAME));


CREATE INDEX IDX_CMDEQID ON COMMANDTAG (CMD_EQID);

--

CREATE TABLE DATATAG (
  TAGID               NUMBER(9) NOT NULL,
  TAGNAME             VARCHAR2(60) NOT NULL,
  TAGDESC             VARCHAR2(100),
  TAGMODE             NUMBER(1) NOT NULL,
  TAGDATATYPE         VARCHAR2(20) NOT NULL,
  TAGCONTROLTAG       NUMBER(1) NOT NULL,
  TAGVALUE            BLOB,
  TAGVALUEDESC        VARCHAR2(1000),
  TAGTIMESTAMP        TIMESTAMP(6),
  TAGDAQTIMESTAMP     TIMESTAMP(6),
  TAGSRVTIMESTAMP     TIMESTAMP(6),
  TAGQUALITYCODE      NUMBER(3),
  TAGQUALITYDESC      VARCHAR2(1000),
  TAGRULE             VARCHAR2(4000),
  TAGRULEIDS          VARCHAR2(500),
  TAG_EQID            NUMBER(9),
  TAGMINVAL           BLOB,
  TAGMAXVAL           BLOB,
  TAGUNIT             VARCHAR2(50),
  TAGSIMULATED        NUMBER(1),
  TAGLOGGED           NUMBER(1),
  TAGVALUEDICTIONARY  VARCHAR2(1000),
  TAGADDRESS          VARCHAR2(4000),
  TAGDIPADDRESS       VARCHAR2(500),
  TAGJAPCADDRESS    VARCHAR2(500))
STORAGE
      (
        INITIAL 100M
        NEXT 20M
        MINEXTENTS 1
        MAXEXTENTS UNLIMITED
        BUFFER_POOL DEFAULT
      )
;

ALTER TABLE DATATAG ADD (
  CONSTRAINT PK__DATATAG_TAGID PRIMARY KEY (TAGID),
  CONSTRAINT UQ__DATATAG_TAGNAME UNIQUE (TAGNAME));



CREATE INDEX I__DATATAG_TAGEQUID ON DATATAG (TAG_EQID);

--

CREATE TABLE EQUIPMENT (
  EQID               NUMBER(9) NOT NULL,
  EQNAME             VARCHAR2(60) NOT NULL,
  EQDESC             VARCHAR2(100),
  EQHANDLERCLASS     VARCHAR2(100) NOT NULL,
  EQADDRESS          VARCHAR2(350),
  EQSTATE_TAGID      NUMBER(9) NOT NULL,
  EQALIVE_TAGID      NUMBER(9),
  EQALIVEINTERVAL    NUMBER(7),
  EQCOMMFAULT_TAGID  NUMBER(9),
  EQ_PROCID          NUMBER(9),
  EQ_PARENT_ID       NUMBER(10),
  EQSTATE            VARCHAR2(20),
  EQSTATUSTIME       TIMESTAMP(6),
  EQSTATUSDESC       VARCHAR2(300)
  );

ALTER TABLE EQUIPMENT ADD (
  CONSTRAINT PK_EQUIPMENT_EQID PRIMARY KEY (EQID),
  CONSTRAINT UQ_EQUIPMENT_EQNAME UNIQUE (EQNAME));



CREATE INDEX I_EQUIPMENT_EQPROCID ON EQUIPMENT (EQ_PROCID);

--

CREATE TABLE PROCESS (
  PROCID             NUMBER(9) NOT NULL,
  PROCNAME           VARCHAR2(60) NOT NULL,
  PROCDESC           VARCHAR2(100),
  PROCSTATE_TAGID    NUMBER(9) NOT NULL,
  PROCALIVE_TAGID    NUMBER(9),
  PROCALIVEINTERVAL  NUMBER(7),
  PROCMAXMSGSIZE     NUMBER(5) NOT NULL,
  PROCMAXMSGDELAY    NUMBER(7) NOT NULL,
  PROCCURRENTHOST    VARCHAR2(25),
  PROCSTATE          VARCHAR2(20),
  PROCSTATUSTIME     TIMESTAMP(6),
  PROCSTATUSDESC     VARCHAR2(300),
  PROCSTARTUPTIME    TIMESTAMP(6),
  PROCREBOOT         NUMBER(1));

ALTER TABLE PROCESS ADD (
  CONSTRAINT PK_PROCESS_PROCID PRIMARY KEY (PROCID),
  CONSTRAINT UQ_PROCESS_PROCNAME UNIQUE (PROCNAME));

ALTER TABLE PROCESS ADD (
  CONSTRAINT FK_PROCALIVETAGID_DATATAG FOREIGN KEY (PROCALIVE_TAGID) REFERENCES DATATAG (TAGID),
  CONSTRAINT FK_PROSTATETAGID_DATATAG FOREIGN KEY (PROCSTATE_TAGID) REFERENCES DATATAG (TAGID));

ALTER TABLE ALARM ADD (
  CONSTRAINT FK_ALARM_TAGID_DATATAG FOREIGN KEY (ALARM_TAGID) REFERENCES DATATAG (TAGID));

ALTER TABLE EQUIPMENT ADD (
  CONSTRAINT FK_EQUIPMENT_EQALIVE_DATATAG FOREIGN KEY (EQALIVE_TAGID) REFERENCES DATATAG (TAGID),
  CONSTRAINT FK_EQUIPMENT_EQCOMMF_DATATAG FOREIGN KEY (EQCOMMFAULT_TAGID) REFERENCES DATATAG (TAGID),
  CONSTRAINT FK_EQUIPMENT_EQSTATE_DATATAG FOREIGN KEY (EQSTATE_TAGID) REFERENCES DATATAG (TAGID),
  CONSTRAINT FK_EQUIPMENT_PARENT_ID FOREIGN KEY (EQ_PARENT_ID) REFERENCES EQUIPMENT (EQID),
  CONSTRAINT FK_EQUIPMENT_PROCID_PROCESS FOREIGN KEY (EQ_PROCID) REFERENCES PROCESS (PROCID));

ALTER TABLE DATATAG ADD (
  CONSTRAINT FK__DATATAG_EQID_EQUIPMENT FOREIGN KEY (TAG_EQID) REFERENCES EQUIPMENT (EQID));

ALTER TABLE COMMANDTAG ADD (
  FOREIGN KEY (CMD_EQID) REFERENCES EQUIPMENT (EQID));
  
  -- views
  
CREATE OR REPLACE VIEW ALIVETIMER (ALIVEID, ALIVETYPE, ALIVEINTERVAL, RELATEDID, RELATEDNAME, RELATEDSTATETAG, PARENTALIVEID, PARENTID, PARENTNAME, PARENTTYPE) AS
(
SELECT
    procalive_tagid ALIVEID,
    'PROC' ALIVETYPE,
    procaliveinterval ALIVEINTERVAL,
    procid RELATEDID,
    procname RELATEDNAME,
    procstate_tagid RELATEDSTATETAG,
    null PARENTALIVEID,
    null PARENTID,
    null PARENTNAME,
    null PARENTTYPE
  FROM process p
  WHERE
    procalive_tagid IS NOT NULL
UNION
  SELECT
    eqalive_tagid ALIVEID,
    'EQ' ALIVETYPE,
    eqaliveinterval ALIVEINTERVAL,
    eqid RELATEDID,
    eqname RELATEDNAME,
    eqstate_tagid RELATEDSTATETAG,
    procalive_tagid PARENTALIVEID,
    procid PARENTID,
    procname PARENTNAME,
    'PROC' PARENTTYPE
  FROM process p, equipment e
  WHERE
    eqalive_tagid IS NOT NULL AND
    procid = eq_procid
UNION
  SELECT
    a.eqalive_tagid ALIVEID,
    'SUBEQ' ALIVETYPE,
    a.eqaliveinterval ALIVEINTERVAL,
    a.eqid RELATEDID,
    a.eqname RELATEDNAME,
    a.eqstate_tagid RELATEDSTATETAG,
    b.eqalive_tagid PARENTALIVEID,
    b.eqid PARENTID,
    b.eqname PARENTNAME,
    'EQ' PARENTTYPE
FROM equipment a, equipment b
WHERE
     a.eqalive_tagid IS NOT NULL AND
     a.eq_parent_id = b.eqid
);



--

CREATE OR REPLACE VIEW COMMFAULTTAG (COMMFAULTID, EQID, EQNAME, EQSTATETAG, EQALIVETAG) AS
SELECT EQUIPMENT.EQCOMMFAULT_TAGID COMMFAULTID
          ,EQUIPMENT.EQID EQID
          ,EQUIPMENT.EQNAME EQNAME
          ,EQUIPMENT.EQSTATE_TAGID EQSTATETAG
          ,EQUIPMENT.EQALIVE_TAGID EQALIVETAG
FROM EQUIPMENT;

