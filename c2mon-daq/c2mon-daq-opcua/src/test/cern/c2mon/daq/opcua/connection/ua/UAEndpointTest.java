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
