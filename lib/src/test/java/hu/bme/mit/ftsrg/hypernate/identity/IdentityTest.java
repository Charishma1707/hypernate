/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.identity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class IdentityTest {

  @Mock private ClientIdentity mockClientIdentity;
  @Mock private ChaincodeStub mockStub;

  private Identity identity;

  @BeforeEach
  void setUp() {
    identity = new Identity(mockClientIdentity);
  }

  @Test
  void constructor_throws_IdentityException_on_ClientIdentity_failure() {
    // If the stub returns null creator, the ClientIdentity constructor will throw an exception
    given(mockStub.getCreator()).willReturn(null);

    assertThrows(IdentityException.class, () -> new Identity(mockStub));
  }

  @Test
  void getId_returns_id_from_client_identity() {
    given(mockClientIdentity.getId()).willReturn("user-123");

    assertEquals("user-123", identity.getId());
  }

  @Test
  void getMspId_returns_msp_id_from_client_identity() {
    given(mockClientIdentity.getMSPID()).willReturn("Org1MSP");

    assertEquals("Org1MSP", identity.getMspId());
  }

  @Nested
  class attribute_checks {

    @Test
    void mustGetAttribute_returns_value_when_present() {
      given(mockClientIdentity.getAttributeValue("role")).willReturn("admin");

      assertEquals("admin", identity.mustGetAttribute("role"));
    }

    @Test
    void mustGetAttribute_throws_AttributeNotFoundException_when_absent() {
      given(mockClientIdentity.getAttributeValue("role")).willReturn(null);

      assertThrows(AttributeNotFoundException.class, () -> identity.mustGetAttribute("role"));
    }

    @Test
    void tryGetAttribute_returns_value_when_present() {
      given(mockClientIdentity.getAttributeValue("department")).willReturn("engineering");

      assertEquals("engineering", identity.tryGetAttribute("department"));
    }

    @Test
    void tryGetAttribute_returns_null_when_absent() {
      given(mockClientIdentity.getAttributeValue("department")).willReturn(null);

      assertNull(identity.tryGetAttribute("department"));
    }

    @Test
    void hasAttribute_returns_true_when_matches() {
      given(mockClientIdentity.getAttributeValue("role")).willReturn("editor");

      assertTrue(identity.hasAttribute("role", "editor"));
    }

    @Test
    void hasAttribute_returns_false_when_mismatches() {
      given(mockClientIdentity.getAttributeValue("role")).willReturn("viewer");

      assertFalse(identity.hasAttribute("role", "editor"));
    }

    @Test
    void hasAttribute_returns_false_when_absent() {
      given(mockClientIdentity.getAttributeValue("role")).willReturn(null);

      assertFalse(identity.hasAttribute("role", "editor"));
    }

    @Test
    void requireAttribute_passes_when_matches() {
      given(mockClientIdentity.getAttributeValue("role")).willReturn("admin");

      assertDoesNotThrow(() -> identity.requireAttribute("role", "admin"));
    }

    @Test
    void requireAttribute_throws_AccessDeniedException_when_mismatches() {
      given(mockClientIdentity.getAttributeValue("role")).willReturn("user");

      assertThrows(AccessDeniedException.class, () -> identity.requireAttribute("role", "admin"));
    }

    @Test
    void requireAttribute_throws_AccessDeniedException_when_absent() {
      given(mockClientIdentity.getAttributeValue("role")).willReturn(null);

      assertThrows(AccessDeniedException.class, () -> identity.requireAttribute("role", "admin"));
    }
  }

  @Nested
  class msp_checks {

    @Test
    void isFromMsp_returns_true_when_matches() {
      given(mockClientIdentity.getMSPID()).willReturn("Org1MSP");

      assertTrue(identity.isFromMsp("Org1MSP"));
    }

    @Test
    void isFromMsp_returns_false_when_mismatches() {
      given(mockClientIdentity.getMSPID()).willReturn("Org1MSP");

      assertFalse(identity.isFromMsp("Org2MSP"));
    }

    @Test
    void requireMsp_passes_when_matches() {
      given(mockClientIdentity.getMSPID()).willReturn("Org1MSP");

      assertDoesNotThrow(() -> identity.requireMsp("Org1MSP"));
    }

    @Test
    void requireMsp_throws_AccessDeniedException_when_mismatches() {
      given(mockClientIdentity.getMSPID()).willReturn("Org1MSP");

      assertThrows(AccessDeniedException.class, () -> identity.requireMsp("Org2MSP"));
    }
  }
}
