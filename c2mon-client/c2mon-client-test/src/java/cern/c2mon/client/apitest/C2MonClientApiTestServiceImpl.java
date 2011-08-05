package cern.c2mon.client.apitest;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cern.c2mon.client.apitest.db.C2MonClientApiTestDao;

@Service
public class C2MonClientApiTestServiceImpl implements C2MonClientApiTestService {

    private static Logger log = Logger.getLogger(C2MonClientApiTestServiceImpl.class);
   
    private C2MonClientApiTestDao dao;
    
    
    @Autowired
    public void setDao(C2MonClientApiTestDao dao) {
          this.dao = dao;
    }
	
    @Transactional(readOnly = true)
	@Override
	public List<MetricDef> getAllMetrics() {
		log.debug("entering getAllMetrics()");
		return dao.getAllMetrics();		
	}

}
