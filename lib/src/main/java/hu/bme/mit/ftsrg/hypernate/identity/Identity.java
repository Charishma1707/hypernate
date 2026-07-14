/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.identity;

import hu.bme.mit.ftsrg.hypernate.HypernateException;
import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.shim.ChaincodeStub;

/**
 * A thin, typed wrapper around Fabric's {@link ClientIdentity} providing clean and consistent
 * identity, MSP, and attribute checking semantics.
 */
public final class Identity {

  private final ClientIdentity clientIdentity;

  /**
   * Constructs a new Identity wrapper around Fabric's ClientIdentity.
   *
   * @param stub the chaincode stub used to retrieve the client identity
   * @throws HypernateException if the client identity cannot be initialized
   */
  public Identity(final ChaincodeStub stub) {
    try {
      this.clientIdentity = new ClientIdentity(stub);
    } catch (Exception e) {
      throw new IdentityException("Failed to initialize ClientIdentity from stub", e);
    }
  }

  /**
   * Package-private constructor for testing purposes, allowing a mocked ClientIdentity to be
   * injected.
   *
   * @param clientIdentity the mocked/stubbed ClientIdentity
   */
  Identity(final ClientIdentity clientIdentity) {
    this.clientIdentity = clientIdentity;
  }

  /**
   * Gets the unique ID of the submitting identity.
   *
   * @return the unique ID string
   */
  public String getId() {
    return this.clientIdentity.getId();
  }

  /**
   * Gets the MSP ID of the submitting identity's organization.
   *
   * @return the MSP ID string
   */
  public String getMspId() {
    return this.clientIdentity.getMSPID();
  }

  /**
   * Gets the value of the specified attribute. Throws an exception if the attribute is not found.
   *
   * @param name the name of the attribute
   * @return the attribute value
   * @throws AttributeNotFoundException if the attribute does not exist
   */
  public String mustGetAttribute(final String name) throws AttributeNotFoundException {
    final String value = tryGetAttribute(name);
    if (value == null) {
      throw new AttributeNotFoundException("Attribute not found: " + name);
    }
    return value;
  }

  /**
   * Gets the value of the specified attribute, returning null if it does not exist.
   *
   * @param name the name of the attribute
   * @return the attribute value, or null if absent
   */
  public String tryGetAttribute(final String name) {
    return this.clientIdentity.getAttributeValue(name);
  }

  /**
   * Checks if the identity has the specified attribute with the expected value.
   *
   * @param name the name of the attribute
   * @param value the expected value of the attribute
   * @return true if the attribute exists and matches the expected value, false otherwise
   */
  public boolean hasAttribute(final String name, final String value) {
    final String actualValue = tryGetAttribute(name);
    return actualValue != null && actualValue.equals(value);
  }

  /**
   * Requires that the identity has the specified attribute with the expected value. Throws an
   * exception if the check fails.
   *
   * @param name the name of the attribute
   * @param value the expected value of the attribute
   * @throws AccessDeniedException if the attribute check fails
   */
  public void requireAttribute(final String name, final String value) throws AccessDeniedException {
    if (!hasAttribute(name, value)) {
      throw new AccessDeniedException(
          String.format(
              "Access denied: missing or invalid attribute '%s' (expected '%s')", name, value));
    }
  }

  /**
   * Checks if the submitting identity is from the specified MSP.
   *
   * @param mspId the expected MSP ID
   * @return true if the user belongs to the specified MSP, false otherwise
   */
  public boolean isFromMsp(final String mspId) {
    return getMspId().equals(mspId);
  }

  /**
   * Requires that the submitting identity is from the specified MSP. Throws an exception if the
   * check fails.
   *
   * @param mspId the expected MSP ID
   * @throws AccessDeniedException if the user belongs to a different MSP
   */
  public void requireMsp(final String mspId) throws AccessDeniedException {
    if (!isFromMsp(mspId)) {
      throw new AccessDeniedException(
          String.format(
              "Access denied: required MSP '%s', but user belongs to MSP '%s'", mspId, getMspId()));
    }
  }
}
