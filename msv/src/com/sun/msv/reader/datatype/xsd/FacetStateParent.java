/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tranquilo.reader.datatype.xsd;

import com.sun.tranquilo.datatype.TypeIncubator;

/**
 * Interface implemented by the parent state of FacetState.
 * 
 * the parent holds a Facets object, to which FacetState will add
 * facets.
 * 
 * After all facets are added, the parent state should derive a
 * new type.
 */
public interface FacetStateParent
{
	/** gets an incubator object that the owner holds. */
	TypeIncubator getIncubator();
}
