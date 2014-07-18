REM Note that this DDL is not guaranteed to include
REM   all properties of the original object.  It will also
REM   not include CLUSTER or PARTITION information.  Please
REM   report any other problems to support@benthicsoftware.com
CREATE TABLE DB_DAQ (
	DBD_TAG_ID               NUMBER(10) NOT NULL,
	DBD_NAME                 VARCHAR2(100) NOT NULL,
	DBD_VALUE                VARCHAR2(2000) NOT NULL,
	DBD_DATA_TYPE            VARCHAR2(30) NOT NULL,
	DBD_SOURCE_TIMESTAMP     TIMESTAMP(3) NOT NULL,
	DBD_QUALITY              NUMBER,
	DBD_QUALITY_DESCRIPTION  VARCHAR2(500),
	DBD_CRE_DAT              DATE);

ALTER TABLE DB_DAQ ADD (
	CONSTRAINT DB_DAQ_PK PRIMARY KEY (DBD_TAG_ID),
	CONSTRAINT DB_DAQ_DBD_NAME UNIQUE (DBD_NAME));

CREATE OR REPLACE TRIGGER on_update
AFTER UPDATE ON db_daq
FOR EACH ROW
DECLARE
l_prepared_timestamp	VARCHAR2(50);
l_message				VARCHAR2(1800);

BEGIN
	dbms_output.put_line(:old.dbd_tag_id || ' : ' || :new.dbd_name || ' ' || :new.dbd_value || ' ' ||:new.dbd_source_timestamp || ' ' || :new.dbd_quality || ' ' || :new.dbd_quality_description);
	dbms_lock.sleep(0.01);
	l_prepared_timestamp := rpad(to_char(:new.dbd_source_timestamp,'dd/mm/yyyy hh24:mi:ss.ff'), 23);
	-- 23 is the length of the timestamp with precision to a milisecond (3 places after the '.')
	-- 1800 is the limitation of the message length that can be passed in the signal procedure
	l_message := substr(:new.dbd_name || ';' || :new.dbd_value || ';' || l_prepared_timestamp || ';' || :new.dbd_quality || ';' || :new.dbd_quality_description, 1, 1800);
	dbms_alert.signal( :old.dbd_tag_id , l_message);
END;
/


CREATE OR REPLACE
PACKAGE CLIENT_API 
IS
                             
C_TIM_QUALITY_OK						CONSTANT NUMBER(2)  := 0;	
C_TIM_QUALITY_UNKNOWN					CONSTANT NUMBER(2)  := 5;   
C_ALIVE_TAG_MAX_VALUE					CONSTANT NUMBER(10) := 24 *60;

PROCEDURE STP_UPDATE_DATA_TAG(
			 p_tag_name					IN VARCHAR2 
        	,p_tag_value				IN 	VARCHAR2
        	,p_tag_timestamp			IN TIMESTAMP
            ,p_exitcode            		IN OUT  NUMBER
            ,p_exittext            		IN OUT  VARCHAR2
			);
		
PROCEDURE STP_INVALIDATE_DATA_TAG(
		 	 p_tag_name					IN 		VARCHAR2
			,p_tag_timestamp 			IN 		TIMESTAMP
			,p_tag_quality_description	IN		VARCHAR2 
		    ,p_exitcode            		IN OUT  NUMBER
            ,p_exittext            		IN OUT  VARCHAR2
			);
		
PROCEDURE STP_SEND_ALIVE_TAG(                          
 			 p_tag_name  	   			IN      VARCHAR2
	     	,p_exitcode 		   		IN OUT  NUMBER
			,p_exittext 		   		IN OUT  VARCHAR2
			);


END;
/

CREATE OR REPLACE
PACKAGE BODY CLIENT_API
IS

/*-----------------------------------------------------------------------*/
/*                                                                       */
/* Module   : STP_UPDATE_DATA_TAG                                        */
/* Goal     : Update the data tag to a given value.	                     */
/* Keywords : UPDATE DATA TAG						                     */
/* Type     : DATA_ACTION 												 */
/*                                                                       */
/*-----------------------------------------------------------------------*/
/* Description:                                                          */
/*                                                                       */
/* This procedure should be called from the client databases to update   */
/* their respective datatag.                        					 */       
/* The name of the datatag should be passed, its new value and the time- */
/* stamp. The quality will be automatically set to C_TIM_QUALITY_OK      */
/* which is understood on the TIM side, and the quality description will */
/* be set to null. If the client wishes to explicitly invalidate the     */
/* datatag, the STP_INVALIDATE_DATA_TAG procedure should be used instead.*/
/*                                                                       */
/*-----------------------------------------------------------------------*/
/* History:                                                              */
/*                                                                       */
/* 2011-01-28 : Aleksandra Wardzinska - Creation.                        */
/*                                                                       */
/* YYYY-MM-DD : First name and Name - Review                             */
/*                                                                       */
/*-----------------------------------------------------------------------*/
PROCEDURE STP_UPDATE_DATA_TAG(
			 p_tag_name				IN 		VARCHAR2
        	,p_tag_value			IN 		VARCHAR2
        	,p_tag_timestamp		IN 		TIMESTAMP
            ,p_exitcode            	IN OUT  NUMBER
            ,p_exittext            	IN OUT  VARCHAR2
			) IS              
			
			PRAGMA AUTONOMOUS_TRANSACTION;

BEGIN
  p_exitcode := 0;
  p_exittext := NULL;

  DBMS_APPLICATION_INFO.SET_CLIENT_INFO('CLIENT_API.STP_UPDATE_DATA_TAG');
  DBMS_APPLICATION_INFO.SET_ACTION(SUBSTR('n:'||p_tag_name ||',t:'||p_tag_timestamp,1,25));

  IF p_tag_name IS NULL THEN 
    p_exitcode := 20110;
  	p_exittext := 'The name of the datatag cannot be null.';
  	RETURN;
  END IF;
  
  IF p_tag_timestamp IS NULL THEN 
  	p_exitcode := 20111;
  	p_exittext := 'Source timestamp cannot be null.';
  	RETURN;
  END IF;
  
  IF p_tag_value IS NULL THEN
  	p_exitcode := 20112;
  	p_exittext := 'The value of the datatag cannot be null.';
  	RETURN;
  END IF;	
  
	UPDATE db_daq
	   SET dbd_value = p_tag_value
	      ,dbd_source_timestamp = p_tag_timestamp
		  ,dbd_quality = C_TIM_QUALITY_OK
		  ,dbd_quality_description = NULL
	 WHERE dbd_name = p_tag_name
	      ;
   COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    p_exitcode := SQLCODE;
    p_exittext := SUBSTR(SQLERRM, 1, 250);
END STP_UPDATE_DATA_TAG;		

/*-----------------------------------------------------------------------*/
/*                                                                       */
/* Module   : STP_INVALIDATE_DATA_TAG                                    */
/* Goal     : Mark the data tag as invalid.			                     */
/* Keywords : INVALIDATE DATATAG              							 */
/* Type     : DATA_ACTION 						   						 */
/*                                                                       */
/*-----------------------------------------------------------------------*/
/* Description:                                                          */
/*                                                                       */
/* This procedure should be called from the client databases which want  */
/* to invalidate their specific data tag. The name of the datatag should */
/* be passed, the source timestamp and the description of the reason for */
/* invalidation. The C_TIM_QUALITY_UNKNOWN will be set automatically for */
/* the quality of the datatag.											 */
/*                                                                       */
/*-----------------------------------------------------------------------*/
/* History:                                                              */
/*                                                                       */
/* 2011-01-28 : Aleksandra Wardzinska - Creation.                        */
/*                                                                       */
/* YYYY-MM-DD : First name and Name - Review                             */
/*                                                                       */
/*-----------------------------------------------------------------------*/
PROCEDURE STP_INVALIDATE_DATA_TAG(
		 	 p_tag_name					IN 		VARCHAR2
			,p_tag_timestamp 			IN 		TIMESTAMP
			,p_tag_quality_description	IN		VARCHAR2 
		    ,p_exitcode            		IN OUT  NUMBER
            ,p_exittext            		IN OUT  VARCHAR2
			) IS                          
			
			PRAGMA AUTONOMOUS_TRANSACTION;		

BEGIN
  p_exitcode := 0;
  p_exittext := NULL;
 
  DBMS_APPLICATION_INFO.SET_CLIENT_INFO('CLIENT_API.STP_INVALIDATE_DATA_TAG');
  DBMS_APPLICATION_INFO.SET_ACTION(SUBSTR('n:'||p_tag_name||',t:'||p_tag_timestamp || ',q:'||p_tag_quality_description,1,25));
  
  IF p_tag_name IS NULL THEN 
    p_exitcode := 20110;
  	p_exittext := 'The name of the datatag cannot be null.';
  	RETURN;
  END IF;
  
  IF p_tag_timestamp IS NULL THEN 
  	p_exitcode := 20111;
  	p_exittext := 'Source timestamp cannot be null.';
  	RETURN;
  END IF;

	UPDATE db_daq
	   SET dbd_source_timestamp = p_tag_timestamp        
	   	  ,dbd_quality = C_TIM_QUALITY_UNKNOWN
	   	  ,dbd_quality_description = p_tag_quality_description
	 WHERE dbd_name = p_tag_name 	  
	   	  ;	  
	COMMIT;  	  
	
EXCEPTION
  WHEN OTHERS THEN
    p_exitcode := SQLCODE;
    p_exittext := SUBSTR(SQLERRM, 1, 250);
END STP_INVALIDATE_DATA_TAG; 		

/*-----------------------------------------------------------------------*/
/*                                                                       */
/* Module   : STP_SEND_ALIVE_TAG                                         */
/* Goal     : Updates the alive tag to a new value                       */
/* Keywords : SEND UPDATE ALIVE TAG							             */
/* Type     : DATA_ACTION   										     */
/*                                                                       */
/*-----------------------------------------------------------------------*/
/* Description:                                                          */
/*                                                                       */
/* This procedure should be called from the client databases that want   */
/* to send their alive tag.	The name of the alive datatag should be      */
/* passed. The alive is a simple counter reset every 24 * 60 values,	 */
/* which corresponds to 1 day if the alive is sent once per minute.      */
/* The timestamp of the alive tag is automatically set to systimestamp.  */
/*                                                                       */
/*-----------------------------------------------------------------------*/
/* History:                                                              */
/*                                                                       */
/* 2011-01-28 : Aleksandra Wardzinska - Creation.                        */
/*                                                                       */
/* YYYY-MM-DD : First name and Name - Review                             */
/*                                                                       */
/*-----------------------------------------------------------------------*/
PROCEDURE STP_SEND_ALIVE_TAG(                          
			 p_tag_name  	   	   IN      VARCHAR2
	     	,p_exitcode 		   IN OUT  NUMBER
			,p_exittext 		   IN OUT  VARCHAR2
			) IS
			
			PRAGMA AUTONOMOUS_TRANSACTION;

  l_max_value							   NUMBER(5);    

BEGIN
  p_exitcode := 0;
  p_exittext := NULL;
 
  DBMS_APPLICATION_INFO.SET_CLIENT_INFO('CLIENT_API.STP_SEND_ALIVE_TAG');
  DBMS_APPLICATION_INFO.SET_ACTION(SUBSTR('n:'||p_tag_name,1,25));
  
  IF p_tag_name IS NULL THEN 
    p_exitcode := 20110;
  	p_exittext := 'The name of the datatag cannot be null.';
  	RETURN;
  END IF;

  l_max_value := C_ALIVE_TAG_MAX_VALUE;
         
  UPDATE db_daq 
	 SET dbd_value = TO_CHAR( DECODE(TO_NUMBER(dbd_value), l_max_value, 1, TO_NUMBER(dbd_value) + 1)) 
	    ,dbd_source_timestamp = SYSTIMESTAMP  
   WHERE dbd_name = p_tag_name
   		;
	
  COMMIT;

EXCEPTION
  WHEN OTHERS THEN
    p_exitcode := SQLCODE;
    p_exittext := SUBSTR(SQLERRM, 1, 250);
END STP_SEND_ALIVE_TAG;                                                                              



END;
/


