/* SPDX-License-Identifier: Apache-2.0 */
package hu.bme.mit.ftsrg.hypernate.identity;

import hu.bme.mit.ftsrg.hypernate.HypernateException;
import lombok.experimental.StandardException;

/** Exception thrown when a required identity attribute is not found. */
@StandardException
public class AttributeNotFoundException extends HypernateException {}
