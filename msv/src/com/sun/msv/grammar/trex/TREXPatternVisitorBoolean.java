/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tranquilo.grammar.trex;

import com.sun.tranquilo.grammar.ExpressionVisitorBoolean;

/**
 * TREX version of ExpressionVisitorBoolean
 */
public interface TREXPatternVisitorBoolean extends ExpressionVisitorBoolean
{
	boolean onConcur( ConcurPattern p );
	boolean onInterleave( InterleavePattern p );
}
