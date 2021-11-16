delete from PROPERTYFIELD;
delete from FIELD;
delete from DEVICEPROPERTY;
delete from PROPERTY;
delete from DEVICECOMMAND;
delete from COMMAND;
delete from DEVICE;
delete from DEVICECLASS;
delete from ALARM;
delete from DATATAG where tagcontroltag=0;
delete from COMMANDTAG;
delete from EQUIPMENT where eq_parent_id is not null;
delete from EQUIPMENT;
delete from PROCESS;
delete from DATATAG where tagcontroltag=1;

