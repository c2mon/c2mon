/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.daq.opcua.connection.ua;

import static org.junit.Assert.*;

import java.util.EnumSet;

import org.junit.Test;

import cern.c2mon.daq.opcua.connection.ua.UAEndpoint;

import com.prosysopc.ua.PkiFileBasedCertificateValidator.CertificateCheck;
import com.prosysopc.ua.PkiFileBasedCertificateValidator.ValidationResult;

public class UAEndpointTest {
    
    private UAEndpoint endpoint = new UAEndpoint(null, null);
    
    @Test
    public void testOnValidate() {
        EnumSet<CertificateCheck> validNotSelfSigned = EnumSet.of(
                CertificateCheck.Validity, CertificateCheck.Signature);
        EnumSet<CertificateCheck> validWrongSignature = EnumSet.of(
                CertificateCheck.Validity);
        EnumSet<CertificateCheck> notValidRightSignature = EnumSet.of(
                CertificateCheck.Signature);
        EnumSet<CertificateCheck> validSelfSignedNotTrusted = EnumSet.of(
                CertificateCheck.Validity, CertificateCheck.Signature,
                CertificateCheck.SelfSigned);
        EnumSet<CertificateCheck> validSelfSignedTrusted = EnumSet.of(
                CertificateCheck.Validity, CertificateCheck.Signature,
                CertificateCheck.SelfSigned, CertificateCheck.Trusted);
        
        assertEquals(
                ValidationResult.AcceptOnce,
                endpoint.onValidate(null, null, validNotSelfSigned));
        assertEquals(
                ValidationResult.Reject,
                endpoint.onValidate(null, null, validWrongSignature));
        assertEquals(
                ValidationResult.Reject,
                endpoint.onValidate(null, null, notValidRightSignature));
        assertEquals(
                ValidationResult.Reject,
                endpoint.onValidate(null, null, validSelfSignedNotTrusted));
        assertEquals(
                ValidationResult.AcceptOnce,
                endpoint.onValidate(null, null, validSelfSignedTrusted));
    }

}
