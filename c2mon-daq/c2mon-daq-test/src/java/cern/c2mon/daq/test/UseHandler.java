/* 
 * Copyright CERN 2011, All Rights Reserved.
 */
package cern.c2mon.daq.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cern.c2mon.driver.common.EquipmentMessageHandler;

/*
 * This annotation is used to mark which handler needs to be used for particular test
 * 
 * @author wbuczak
 */
@Target(value = { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface UseHandler {
    Class<? extends EquipmentMessageHandler> value();
}
