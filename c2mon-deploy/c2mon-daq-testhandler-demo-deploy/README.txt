Starting the DAQs
-----------------

To start the two pre-configured demo DAQ processes (P_TESTHANDLER01 and P_TESTHANDLER02), 
go to the c2mon-demo/daq/bin directory and execute the startup script as follows:

$ cd c2mon-1.x.x/c2mon-daq/bin
$ ./daqprocess.sh start P_TESTHANDLER01
$ ./daqprocess.sh start P_TESTHANDLER02


Checking the status of a DAQ
----------------------------

To see, if the two DAQ process are now up and running you can execute the following
commands:

$ cd c2mon-1.x.x/c2mon-daq/bin
$ ./daqprocess.sh status P_TESTHANDLER01
$ ./daqprocess.sh status P_TESTHANDLER02


Stopping the DAQs
-----------------

$ cd c2mon-1.x.x/c2mon-daq/bin
$ ./daqprocess.sh stop P_TESTHANDLER01
$ ./daqprocess.sh stop P_TESTHANDLER02