package ch.cern.tim.jec;

import ch.cern.tim.jec.ConnectionData;
import ch.cern.tim.jec.JECPFrames;
import ch.cern.tim.jec.PLCDriver;

public class TestPLCDriver implements PLCDriver {
    
    private JECPFrames lastSend;
    
    @Override
    public int Connect(ConnectionData param) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int Disconnect(ConnectionData param) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int Receive(JECPFrames buffer) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int Receive(JECPFrames buffer, int timeout) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int Send(JECPFrames Frame) {
        setLastSend(Frame);
        return 0;
    }

    /**
     * @param lastSend the lastSend to set
     */
    public void setLastSend(JECPFrames lastSend) {
        this.lastSend = lastSend;
    }

    /**
     * @return the lastSend
     */
    public JECPFrames getLastSend() {
        return lastSend;
    }

}
