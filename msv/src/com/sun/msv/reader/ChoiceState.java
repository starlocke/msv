/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tranquilo.reader;

import com.sun.tranquilo.grammar.Expression;

public class ChoiceState extends ExpressionWithChildState
{
	protected Expression castExpression( Expression exp, Expression child )
	{
		// first one.
		if( exp==null )	return child;
		else			return reader.pool.createChoice(exp,child);
	}
}
