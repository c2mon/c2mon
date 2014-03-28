/**
 * Copyright (c) 2014 European Organisation for Nuclear Research (CERN), All Rights Reserved.
 */

package cern.c2mon.configloader.aspects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.springframework.stereotype.Service;

@Aspect
@Service
public class TraceAspect {

    private static final Logger LOG = LoggerFactory.getLogger(TraceAspect.class);

    @Before("execution(* cern.c2mon.configloader*..*.*(..))")
    public void traceBegin(JoinPoint joinPoint) {
        if (LOG.isTraceEnabled())
            LOG.trace("entering method {}()..", joinPoint.getSignature().getName());       
    }

    @After("execution(* cern.c2mon.configloader*..*.*(..))")
    public void traceEnd(JoinPoint joinPoint) {
        if (LOG.isTraceEnabled())
            LOG.trace("leaving method {}()", joinPoint.getSignature().getName());
    }

}