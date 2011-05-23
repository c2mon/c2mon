
-- The SHORTTERMLOG schema.
-- For testing, use the table creation statements with constraints (partitioning and indexing are not required).

-- the STL table contains no data for the moment
-- reserve the tagid range 1-100 for test inserts
                          
CREATE TABLE shorttermlog (
logdate            DATE               NOT NULL,
tagid                NUMBER(9)     NOT NULL,
tagname          VARCHAR2(60),
tagvalue          VARCHAR2(40),
tagdatatype     VARCHAR2(10),
tagtime           TIMESTAMP(6) WITH TIME ZONE NOT NULL,
tagstatus         NUMBER (3),
tagstatusdesc  VARCHAR2(100),
tagmode          NUMBER(1),
tagdir              VARCHAR2(1))

PARTITION BY RANGE (logdate) (
PARTITION TIMLOG001 VALUES LESS THAN (to_date('22-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG002 VALUES LESS THAN (to_date('22-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG003 VALUES LESS THAN (to_date('23-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG004 VALUES LESS THAN (to_date('23-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG005 VALUES LESS THAN (to_date('23-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG006 VALUES LESS THAN (to_date('24-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG007 VALUES LESS THAN (to_date('24-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG008 VALUES LESS THAN (to_date('24-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG009 VALUES LESS THAN (to_date('25-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG010 VALUES LESS THAN (to_date('25-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG011 VALUES LESS THAN (to_date('25-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG012 VALUES LESS THAN (to_date('26-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG013 VALUES LESS THAN (to_date('26-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG014 VALUES LESS THAN (to_date('26-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG015 VALUES LESS THAN (to_date('27-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG016 VALUES LESS THAN (to_date('27-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG017 VALUES LESS THAN (to_date('27-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG018 VALUES LESS THAN (to_date('28-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG019 VALUES LESS THAN (to_date('28-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG020 VALUES LESS THAN (to_date('28-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG021 VALUES LESS THAN (to_date('29-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG022 VALUES LESS THAN (to_date('29-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG023 VALUES LESS THAN (to_date('29-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG024 VALUES LESS THAN (to_date('30-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG025 VALUES LESS THAN (to_date('30-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG026 VALUES LESS THAN (to_date('30-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG027 VALUES LESS THAN (to_date('31-mar-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG028 VALUES LESS THAN (to_date('31-mar-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG029 VALUES LESS THAN (to_date('31-mar-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG030 VALUES LESS THAN (to_date('01-apr-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG031 VALUES LESS THAN (to_date('01-apr-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG032 VALUES LESS THAN (to_date('01-apr-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG033 VALUES LESS THAN (to_date('02-apr-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG034 VALUES LESS THAN (to_date('02-apr-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG035 VALUES LESS THAN (to_date('02-apr-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG036 VALUES LESS THAN (to_date('03-apr-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG037 VALUES LESS THAN (to_date('03-apr-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG038 VALUES LESS THAN (to_date('03-apr-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG039 VALUES LESS THAN (to_date('04-apr-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG040 VALUES LESS THAN (to_date('04-apr-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG041 VALUES LESS THAN (to_date('04-apr-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG042 VALUES LESS THAN (to_date('05-apr-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG043 VALUES LESS THAN (to_date('05-apr-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG044 VALUES LESS THAN (to_date('05-apr-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG045 VALUES LESS THAN (to_date('06-apr-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG046 VALUES LESS THAN (to_date('06-apr-2006 08:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG047 VALUES LESS THAN (to_date('06-apr-2006 16:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA,
PARTITION TIMLOG048 VALUES LESS THAN (to_date('07-apr-2006 00:00:00','dd-mon-yyyy hh24:mi:ss')) TABLESPACE LOGDATA)
/

create index stl_tagid_ix on shorttermlog(tagid) local


-- the Supervision Log table (empty for the time being in test DB)
-- for testing, leave ids 0-10 free for insertions

CREATE TABLE SUPERVISION_LOG(
  SUL_ENTITY  VARCHAR(30) NOT NULL,
  SUL_ID      NUMBER(9) NOT NULL,
  SUL_DATE    TIMESTAMP(6) NOT NULL,
  SUL_STATUS   VARCHAR(20) NOT NULL,
  SUL_MESSAGE VARCHAR(200)
)