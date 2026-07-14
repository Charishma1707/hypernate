/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.identity;

import hu.bme.mit.ftsrg.hypernate.HypernateException;
import lombok.experimental.StandardException;

/** Exception thrown when an identity check (MSP or attribute) fails. */
@StandardException
public class AccessDeniedException extends HypernateException {}
