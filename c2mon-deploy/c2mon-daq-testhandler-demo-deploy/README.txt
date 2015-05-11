Starting the DAQs
-----------------

To start the two pre-configured DAQ processes (P_TESTHANLDER03 and P_TESTHANLDER04), 
go to the c2mon-demo/daq/bin directory and execute the startup script as follows:

$ cd c2mon-demo/daq/bin
$ ./daqprocess.sh start P_TESTHANLDER03
$ ./daqprocess.sh start P_TESTHANLDER04


Checking the status of a DAQ
----------------------------

To see, if the two DAQ process are now up and running you can execute the following
commands:

$ cd c2mon-demo/daq/bin
$ ./daqprocess.sh status P_TESTHANLDER03
$ ./daqprocess.sh status P_TESTHANLDER04


Stopping the DAQs
-----------------

$ cd c2mon-demo/daq/bin
$ ./daqprocess.sh stop P_TESTHANLDER03
$ ./daqprocess.sh stop P_TESTHANLDER04