CREATE TABLE IF NOT EXISTS SHORTTERMLOG (
  logdate           DATE         NOT NULL,
  tagid             INTEGER      NOT NULL,
  tagname           VARCHAR(200),
  tagvalue          VARCHAR(4000),
  tagvaluedesc      VARCHAR(1000),
  tagdatatype       VARCHAR(200),
  tagtime           TIMESTAMP DEFAULT '1970-01-02 00:00:01',
  tagservertime     TIMESTAMP NOT NULL DEFAULT '1970-01-02 00:00:01',
  tagdaqtime        TIMESTAMP DEFAULT '1970-01-02 00:00:01',
  tagstatus         INTEGER,
  tagstatusdesc     VARCHAR(1000),
  tagmode           INTEGER,
  tagdir            VARCHAR(1)
);

ALTER TABLE SHORTTERMLOG ADD INDEX tagservertime_tagid_idx (tagservertime, tagid);

CREATE TABLE IF NOT EXISTS SUPERVISION_LOG (
  SUL_ENTITY  VARCHAR(30)  NOT NULL,
  SUL_ID      INTEGER      NOT NULL,
  SUL_DATE    TIMESTAMP    NOT NULL DEFAULT '1970-01-02 00:00:01',
  SUL_STATUS  VARCHAR(20)  NOT NULL,
  SUL_MESSAGE VARCHAR(200)
);


CREATE TABLE IF NOT EXISTS STL_DAY_SNAPSHOT (
  logdate       DATE,
  tagid         INTEGER      NOT NULL,
  tagname       VARCHAR(60),
  tagvalue      VARCHAR(4000),
  tagdatatype   VARCHAR(200),
  tagtime       TIMESTAMP DEFAULT '1970-01-02 00:00:01',
  tagservertime TIMESTAMP DEFAULT '1970-01-02 00:00:01',
  tagdaqtime    TIMESTAMP DEFAULT '1970-01-02 00:00:01',
  tagstatus     INTEGER,
  tagstatusdesc VARCHAR(1000),
  tagmode       INTEGER,
  tagvaluedesc  VARCHAR(1000)
);

CREATE TABLE IF NOT EXISTS COMMANDTAGLOG (
  cmdid           INTEGER       NOT NULL,
  cmdname         VARCHAR(60)   NOT NULL,
  cmdmode         INTEGER       NOT NULL,
  cmdtime         TIMESTAMP     NOT NULL DEFAULT '1970-01-02 00:00:01',
  cmdvalue        VARCHAR(4000),
  cmddatatype     VARCHAR(200),
  cmduser         VARCHAR(25),
  cmdhost         VARCHAR(60),
  cmdreporttime   TIMESTAMP    DEFAULT '1970-01-02 00:00:01',
  cmdreportstatus VARCHAR(50),
  cmdreportdesc   VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS SERVER_LIFECYCLE_LOG (
  SLL_EVENT_TYPE  VARCHAR(10) NOT NULL,
  SLL_SERVER_NAME VARCHAR(20) NOT NULL,
  SLL_TIME        TIMESTAMP NOT NULL DEFAULT '1970-01-02 00:00:01'
);

CREATE TABLE IF NOT EXISTS ALARMLOG (
  logdate         DATE,
  tagid           INTEGER       NOT NULL,
  alarmid         INTEGER       NOT NULL,
  faultfamily     VARCHAR(60)   NOT NULL,
  faultmember     VARCHAR(60)   NOT NULL,
  faultcode       INTEGER       NOT NULL,
  active          VARCHAR(1)    NOT NULL,
  servertime      TIMESTAMP     NOT NULL DEFAULT '1970-01-02 00:00:01',
  sourcetime      TIMESTAMP     NOT NULL DEFAULT '1970-01-02 00:00:01',
  info            VARCHAR(100)  DEFAULT NULL,
  oscillating     VARCHAR(1)
);


