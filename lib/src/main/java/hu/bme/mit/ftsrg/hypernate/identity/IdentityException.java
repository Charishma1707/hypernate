/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.identity;

import hu.bme.mit.ftsrg.hypernate.HypernateException;
import lombok.experimental.StandardException;

/** Exception thrown for general identity-related errors, including initialization failures. */
@StandardException
public class IdentityException extends HypernateException {}
