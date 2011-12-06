// TIM driver. CERN. All rights reserved.
//  
// T Nick:           Date:       Info:
// -------------------------------------------------------------------------
// D wbuczak    15/July/2004      Class generation from model and first
//                                implementation
// P wbuczak    09/Aug/2004       Changes and some little corrections made
// -------------------------------------------------------------------------


package cern.c2mon.dippublisher.tools;


/**
This class is used for handling command line parameters. 
The class validates the parameters, and remembers the 
(param-name,param-value) pairs so that they can be easly accessed
later during program's execution.
 */
public class CommandParamsHandler {

  /**
  The TIM hashtable used for storing parsed (param-name,param-value) pairs
   */
  TIMDriverHashtable paramsTable = new TIMDriverHashtable();


  /**
  The default constructor
   */
  public CommandParamsHandler()
  {
  }

  /**
  This method parses the commandline array of arguments and tries to 
  fill the hashtable with (param-name,param-value). In case
  of some troubles (inpropper number of tokens found etc..) it throws 
  the IncorrectCommandLineParamsException
   */
  public void initialise(String[] params) 
  {

    for (int i=0;i<params.length;i++) 
    {
      if (params[i].charAt(0) == '-') 
      {
        if (i < params.length-1) 
        {
          if (params[i+1].charAt(0) == '-') 
          {
            paramsTable.put(params[i],"null");     
          }
          else 
          {
            paramsTable.put(params[i],params[i+1]);
          }
        }//if
        else 
        {
          paramsTable.put(params[i],"null");               
        }
      }//if
    }//for
  }


  /**
  This method checks if specified parameter is registered or not
   */
  public boolean hasParam(String paramName)
  {
    return paramsTable.containsKey(paramName);
  }


  /**
  This method returs the value of the specified parameter
   */
  public String getParamValue(String paramName) 
  {
    return (String)paramsTable.get(paramName);
  }
  
}