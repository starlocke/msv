/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.datatype.xsd;

import org.relaxng.datatype.ValidationContext;
import com.sun.msv.datatype.SerializationContext;

/**
 * simple "ur-type" type.
 * 
 * type of the value object is <code>java.lang.String</code>.
 * See http://www.w3.org/TR/xmlschema-1/#simple-ur-type-itself for the spec
 * 
 * @author Kohsuke KAWAGUCHI
 */
public class SimpleURType extends ConcreteType {
	
	public static final SimpleURType theInstance = new SimpleURType();
	
	protected SimpleURType() {
		super("anySimpleType",WhiteSpaceProcessor.thePreserve);
	}
	
	final public XSDatatype getBaseType() {
		return null;
	}
	
	/**
	 * simple ur-type accepts anything.
	 */
	protected final boolean checkFormat( String content, ValidationContext context ) {
		return true;
	}
	
	/**
	 * the value object of the simple ur-type is the lexical value itself.
	 */
	public Object convertToValue( String lexicalValue, ValidationContext context ) {
		return lexicalValue;
	}
	public Class getJavaObjectType() {
		return String.class;
	}

	public String convertToLexicalValue( Object value, SerializationContext context ) {
		if( value instanceof String )
			return (String)value;
		else
			throw new IllegalArgumentException();
	}
	
	/**
	 * no facet is applicable to the simple ur-type.
	 */
	public final int isFacetApplicable( String facetName ) {
		return NOT_ALLOWED;
	}
}
