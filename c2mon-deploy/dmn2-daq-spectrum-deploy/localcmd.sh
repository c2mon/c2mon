#!/bin/bash
cd deploy
pwd
CPATH="../../c2mon-daq-spectrum/build/classes:../../c2mon-daq-spectrum/build/test-classes:lib/*:."
java -cp ${CPATH}  cern.c2mon.daq.spectrum.util.SpectrumCmdLineClient $1 $2 $3 $4 $5 $6 $7 $8 $9


