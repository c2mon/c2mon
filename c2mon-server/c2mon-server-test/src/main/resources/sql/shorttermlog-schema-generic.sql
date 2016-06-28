SET DATABASE SQL SYNTAX ORA TRUE;

CREATE TABLE if not exists shorttermlog (
logdate           DATE          NOT NULL,
tagid             INTEGER     NOT NULL,
tagname           VARCHAR(60),
tagvalue          VARCHAR(4000),
TAGVALUEDESC      VARCHAR(1000),
tagdatatype       VARCHAR(10),
tagtime           TIMESTAMP(6),
tagservertime     TIMESTAMP(6) NOT NULL,
tagdaqtime        TIMESTAMP(6),
tagstatus         INTEGER,
tagstatusdesc     VARCHAR(1000),
tagmode           INTEGER,
tagdir            VARCHAR(1));

--create index stl_tagid_ix on shorttermlog(tagid);

CREATE TABLE if not exists SUPERVISION_LOG(
  SUL_ENTITY  VARCHAR(30) NOT NULL,
  SUL_ID      INTEGER NOT NULL,
  SUL_DATE    TIMESTAMP(6) NOT NULL,
  SUL_STATUS  VARCHAR(20) NOT NULL,
  SUL_MESSAGE VARCHAR(200)
);

CREATE TABLE if not exists stl_day_snapshot (
logdate       DATE
,tagid         INTEGER      NOT NULL
,tagname       VARCHAR(60)
,tagvalue      VARCHAR(4000)
,tagdatatype   VARCHAR(10)
,tagtime       TIMESTAMP(6)
,tagservertime TIMESTAMP(6)
,tagdaqtime    TIMESTAMP(6)
,tagstatus     INTEGER
,tagstatusdesc VARCHAR(1000)
,tagmode       INTEGER
,tagvaluedesc  VARCHAR(1000)
);

--create index tdl_tagid_ix on stl_day_snapshot(tagid);

CREATE TABLE if not exists commandtaglog (
cmdid           INTEGER       NOT NULL,
cmdname         VARCHAR(60)    NOT NULL,
cmdmode         INTEGER       NOT NULL,
cmdtime         TIMESTAMP(6)    NOT NULL,
cmdvalue        VARCHAR(4000),
cmddatatype     VARCHAR(10),
cmduser         VARCHAR(25),
cmdhost         VARCHAR(60),
cmdreporttime   TIMESTAMP(6),
cmdreportstatus VARCHAR(50),
cmdreportdesc   VARCHAR(100)
);

CREATE TABLE if not exists SERVER_LIFECYCLE_LOG (
    SLL_EVENT_TYPE      VARCHAR(10) NOT NULL,
    SLL_SERVER_NAME     VARCHAR(20) NOT NULL,
    SLL_TIME            TIMESTAMP(6) NOT NULL   
);


CREATE TABLE if not exists alarmlog (
logdate         DATE,
tagid           INTEGER       NOT NULL,
alarmid         INTEGER       NOT NULL,
faultfamily     VARCHAR(60)   NOT NULL,
faultmember     VARCHAR(60)   NOT NULL,
faultcode       INTEGER       NOT NULL,
active          VARCHAR(1)       NOT NULL,
servertime      TIMESTAMP(6)  NOT NULL,
info            VARCHAR(100)  DEFAULT NULL
);


