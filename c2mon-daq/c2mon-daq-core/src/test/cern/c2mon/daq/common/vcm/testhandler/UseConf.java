/*
 * Copyright CERN 2010, All Rights Reserved.
 */
package cern.c2mon.daq.common.vcm.testhandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
 * This annotation is used to mark EquipmentMessageHandler tests which requires external XML configuration 
 * for the execution
 * 
 * @author wbuczak
 */

@Target(value = { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface UseConf {
    String value() default "";
}
