package cern.c2mon.daq.jmx.mbeans;

import java.util.List;
import java.util.Map;

public interface CacheMBean {

    int getCacheSize();

    void setCacheSize(int size);

    List<String> getCacheListeners();

    int[] getValues();

    void setCachedObjects(int objects);

    int getCachedObjects();

    long getCurrentSrvTime();

    String test1();

    String test2(boolean condition);

    String test2(Integer val);

    Map<String, ?> getTestMap();
}
