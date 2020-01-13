package cern.c2mon.server.common.status;

import cern.c2mon.server.common.AbstractCacheableImpl;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class SupervisionStateTagCacheObject extends AbstractCacheableImpl implements SupervisionStateTag {

}
