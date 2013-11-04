package cern.c2mon.daq.opcua.jintegraInterface;

/**
 * Constants from OPCQualityStatus' enum. Generated 10/5/2006 11:24:10 AM
 * from 'G:\Users\t\timoper\Public\opcdaauto.dll'<P>
 * Generated using com2java Version 2.6 Copyright (c) Intrinsyc Software International, Inc.
 * See  <A HREF="http://j-integra.intrinsyc.com/">http://j-integra.intrinsyc.com/</A><P>
 *
 * Generator Options:
 *   AwtForOcxs = False
 *   PromptForTypeLibraries = False
 *   RetryOnReject = False
 *   IDispatchOnly = False
 *   GenBeanInfo = False
 *   LowerCaseMemberNames = True
 *   TreatInStarAsIn = False
 *   ArraysAsObjects = False
 *   OmitRestrictedMethods = False
 *   ClashPrefix = zz_
 *   ImplementConflictingInterfaces = False
 *   DontRenameSameMethods = False
 *   RenameConflictingInterfaceMethods = False
 *   ReuseMethods = False
 *
 * Command Line Only Options:
 *   MakeClsidsPublic = False
 *   DontOverwrite = False
 */
public interface OPCQualityStatus extends java.io.Serializable {
  static final int OPCStatusMask = 252;
  static final int OPCStatusConfigError = 4;
  static final int OPCStatusNotConnected = 8;
  static final int OPCStatusDeviceFailure = 12;
  static final int OPCStatusSensorFailure = 16;
  static final int OPCStatusLastKnown = 20;
  static final int OPCStatusCommFailure = 24;
  static final int OPCStatusOutOfService = 28;
  static final int OPCStatusLastUsable = 68;
  static final int OPCStatusSensorCal = 80;
  static final int OPCStatusEGUExceeded = 84;
  static final int OPCStatusSubNormal = 88;
  static final int OPCStatusLocalOverride = 216;
}
