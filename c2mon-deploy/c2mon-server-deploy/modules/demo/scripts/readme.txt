Author: vilches

The script folder containing the python scripts needed to create the demo iso. To launh the scri type:
    - python demo.py if you want to download the release versions of the server, DAQ and configviewer
    - python demo.py -s or --snapshot if you want to download the snapshot versions of the server, DAQ and configviewer 
    
The demo.py script needs other scripts done before:
    - commonbuild.py
    - common.py
    - config.py
    - maven.py (modified)
    - tools.py
    
* maven.py
This script has been modified to take into account war files also. The modified line is the following:

 # Demo: Adding war packaging for taking the c2mon-web-configviewer
 if p.getPackaging() != 'tar.gz' and p.getPackaging() != 'tgz' and p.getPackaging() != 'zip' and p.getPackaging() != 'war':
   continue
  
  
/****************************************************************************/   
NOTE: to remove the demo folder after creating the iso uncoment this line:

print "Cleaning directory"
#shutil.rmtree('demo')