/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.msv.verifier.jarv;

import org.iso_relax.verifier.*;
import com.sun.msv.grammar.ExpressionPool;
import com.sun.msv.grammar.xmlschema.XMLSchemaGrammar;
import com.sun.msv.reader.xmlschema.XMLSchemaReader;
import com.sun.msv.reader.util.IgnoreController;
import com.sun.msv.verifier.regexp.REDocumentDeclaration;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * VerifierFactory implementation for XML Schema.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class XSFactoryImpl extends FactoryImpl {
	
	public XSFactoryImpl( SAXParserFactory factory ) { super(factory); }

	public Verifier newVerifier( String uri )
			throws VerifierConfigurationException, SAXException	{
		XMLSchemaGrammar g = XMLSchemaReader.parse(uri,factory,new IgnoreController());
		if(g==null)		return null;	// load failure
		return getVerifier(g);
	}

	public Verifier newVerifier( InputSource source )
			throws VerifierConfigurationException, SAXException	{
		XMLSchemaGrammar g = XMLSchemaReader.parse(source,factory,new IgnoreController());
		if(g==null)		return null;	// load failure
		return getVerifier(g);
	}
}