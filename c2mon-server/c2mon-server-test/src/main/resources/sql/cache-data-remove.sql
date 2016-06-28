delete from propertyfield;
delete from field;
delete from deviceproperty;
delete from property;
delete from devicecommand;
delete from command;
delete from device;
delete from deviceclass;
delete from alarm;
delete from datatag where tagcontroltag=0;
delete from commandtag;
delete from equipment where eq_parent_id is not null;
delete from equipment;
delete from process;
delete from datatag where tagcontroltag=1;

ALTER SEQUENCE CONFIG_ID_SEQUENCE RESTART WITH 1
ALTER SEQUENCE PROCESS_ID_SEQUENCE RESTART WITH 10000
ALTER SEQUENCE EQUIPMENT_ID_SEQUENCE RESTART WITH 100000
ALTER SEQUENCE TAG_ID_SEQUENCE RESTART WITH 300000
ALTER SEQUENCE ALARM_ID_SEQUENCE RESTART WITH 300000
