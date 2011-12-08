set echo off
set show off
set verify off
set feedback off
set heading off
set linesize 1000
set pagesize 0

select dipdata from (
  select 1 num, 'a' dummy, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>' dipdata from dual union
  select 2 num, 'a' dummy, '<!DOCTYPE DataTags SYSTEM "TIMDipPublisherDataTags.dtd">' dipdata from dual union
  select 3 num, 'a' dummy, '<?xml-stylesheet type="text/xsl" href="TIMDipPublisherDataTags.xsl"?>' dipdata from dual union
  select 4 num, 'a' dummy, '<DataTags>' dipdata from dual union
  select distinct 5 num, dip_address dummy,
         '<DataTag><id>'||dip_point_id||'</id><dip-topic>'||dip_address||'</dip-topic><comment>Publication for '||dip_comment||'</comment></DataTag>' dipdata
  from timref.vconf_dippub_new where dip_publisher = '&1' union
  select 999999 num, 'a' dummy, '</DataTags>' dipdata from dual)
order by num, dummy;
exit;
	      
