# Identity Wrapper

The `Identity` class provides a thin, typed wrapper around Hyperledger Fabric's raw `ClientIdentity`. It simplifies access control checks by offering consistent API semantics, automatic exception wrapping, and direct integration with the transaction context.

## Motivation

In standard Hyperledger Fabric chaincodes, checking client attributes and MSP IDs typically involves writing repetitive boilerplate:
* Instantiating `ClientIdentity` (which throws several checked exceptions: `CertificateException`, `IOException`, `JSONException`).
* Manually checking for `null` when retrieving attribute values.
* Explicitly throwing exceptions on access control failures.

Hypernate's `Identity` class eliminates this by wrappingchecked exceptions into runtime exceptions at construction time and providing `must*`, `try*`, and `require*` semantics.

---

## API & Semantics

The `Identity` wrapper offers the following operations:

### Identity Details
* `getId()`: Returns the unique identifier of the submitting client identity.
* `getMspId()`: Returns the Membership Service Provider (MSP) ID of the client's organization.

### Attribute Verification
* `mustGetAttribute(name)`: Retrieves the attribute value, throwing an `AttributeNotFoundException` if it is not present.
* `tryGetAttribute(name)`: Safely retrieves the attribute value, returning `null` if the attribute does not exist.
* `hasAttribute(name, value)`: Returns a boolean indicating whether the client possesses the specified attribute and if its value matches.
* `requireAttribute(name, value)`: Asserts that the client has the specified attribute and value, throwing an `AccessDeniedException` if they do not.

### MSP (Organization) Verification
* `isFromMsp(mspId)`: Returns a boolean indicating whether the client belongs to the specified MSP ID.
* `requireMsp(mspId)`: Asserts that the client belongs to the specified MSP ID, throwing an `AccessDeniedException` if they do not.

---

## Usage Example

The identity wrapper is lazily initialized and accessible directly via `HypernateContext`:

```java
public void transferAsset(final HypernateContext ctx, final String assetID, final String newOwner) {
    // 1. Enforce that only users from Org1MSP can execute this transaction
    ctx.getIdentity().requireMsp("Org1MSP");

    // 2. Enforce that the user possesses the "admin" role
    ctx.getIdentity().requireAttribute("role", "admin");

    // 3. Proceed with transaction logic
    Asset asset = ctx.getRegistry().mustRead(Asset.class, assetID);
    asset.setOwner(newOwner);
    ctx.getRegistry().mustUpdate(asset);
}
```

---

## Exception Hierarchy

All custom identity exceptions extend `HypernateException`:
* **`IdentityException`**: Thrown for general identity setup and initialization errors.
* **`AttributeNotFoundException`**: Thrown when a mandatory attribute is missing during `mustGetAttribute()`.
* **`AccessDeniedException`**: Thrown when security constraints are violated during `requireAttribute()` or `requireMsp()`.
