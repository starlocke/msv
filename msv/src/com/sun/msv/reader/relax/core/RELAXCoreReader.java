/*
 * @(#)$Id$
 *
 * Copyright 2001 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * This software is the proprietary information of Sun Microsystems, Inc.  
 * Use is subject to license terms.
 * 
 */
package com.sun.tranquilo.reader.relax.core;

import java.util.Map;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;
import com.sun.tranquilo.reader.GrammarReaderController;
import com.sun.tranquilo.reader.State;
import com.sun.tranquilo.reader.ExpressionState;
import com.sun.tranquilo.reader.relax.RELAXReader;
import com.sun.tranquilo.datatype.DataType;
import com.sun.tranquilo.grammar.Expression;
import com.sun.tranquilo.grammar.ExpressionPool;
import com.sun.tranquilo.grammar.relax.RELAXModule;
import com.sun.tranquilo.grammar.relax.NoneType;
import com.sun.tranquilo.grammar.relax.AttPoolClause;
import com.sun.tranquilo.util.StartTagInfo;

/**
 * reads RELAX module (classic RELAX module; no namespace extension)
 * by SAX2 and constructs abstract grammar model.
 * 
 * This class does not recognize extensions introduced by RELAX Namespace
 * (like anyOtherElement, or &lt;ref label="..." namespace="..." /&gt;.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public class RELAXCoreReader extends RELAXReader
{
	/** loads RELAX module */
	public static RELAXModule parse( String moduleURL,
		SAXParserFactory factory, GrammarReaderController controller, ExpressionPool pool )
	{
		RELAXCoreReader reader = new RELAXCoreReader(controller,factory,pool);
		reader.parse(moduleURL);
		
		return reader.getResult();
	}

	/** loads RELAX module */
	public static RELAXModule parse( InputSource module,
		SAXParserFactory factory, GrammarReaderController controller, ExpressionPool pool )
	{
		RELAXCoreReader reader = new RELAXCoreReader(controller,factory,pool);
		reader.parse(module);
		
		return reader.getResult();
	}
	
	
	public RELAXCoreReader(
		GrammarReaderController controller,
		SAXParserFactory parserFactory,
		ExpressionPool pool )
	{
		super(controller,parserFactory,pool,new RootModuleState(null));
	}

	public RELAXCoreReader(
		GrammarReaderController controller,
		SAXParserFactory parserFactory,
		ExpressionPool pool, String expectedTargetNamespace )
	{
		super(controller,parserFactory,pool,new RootModuleState(expectedTargetNamespace));
	}
	
	/**
	 * RELAX module object being under construction.
	 * 
	 * object is created when target namespace is identified.
	 */
	protected RELAXModule module;

	/** obtains parsed grammar object only if parsing was successful. */
	public final RELAXModule getResult()
	{
		if(hadError)	return null;
		else			return module;
	}
	
	
	/**
	 * map from type name of Candidate Recommendation to the current type.
	 */
	private static final Map deprecatedTypes = initDeprecatedTypes();
	private static Map initDeprecatedTypes()
	{
		Map m = new java.util.HashMap();
		m.put("uriReference",		com.sun.tranquilo.datatype.AnyURIType.theInstance );
		m.put("decimal",			com.sun.tranquilo.datatype.NumberType.theInstance );
		m.put("timeDuration",		com.sun.tranquilo.datatype.DurationType.theInstance );
		m.put("CDATA",				com.sun.tranquilo.datatype.NormalizedStringType.theInstance );
		return m;
	}
	public DataType getBackwardCompatibleType( String typeName )
	{
		DataType dt = (DataType)deprecatedTypes.get(typeName);
		if( dt!=null )
			reportWarning( WRN_DEPRECATED_TYPENAME, typeName, dt.getName() );
		return dt;
	}
	
	/**
	 * gets DataType object from type name.
	 * 
	 * If undefined type name is specified, this method is responsible
	 * to report an error, and recovers.
	 */
	public DataType resolveDataType( String typeName )
	{
		// look up user defined types first
		DataType dt = (DataType)module.userDefinedTypes.getType(typeName);
		
		if(dt==null)
		{
			dt = getBackwardCompatibleType(typeName);
			if(dt!=null)	return dt;
			
			reportError( ERR_UNDEFINED_DATATYPE, typeName );
			return NoneType.theInstance;	// recover by assuming a valid DataType
		}
		else
			return dt;
	}
	
	
	public State createDefaultExpressionChildState( StartTagInfo tag )
	{
		if(! RELAXCoreNamespace.equals(tag.namespaceURI) )	return null;
		
		if(tag.localName.equals("mixed"))			return new MixedState();
		if(tag.localName.equals("element"))			return new InlineElementState();
		
		return super.createDefaultExpressionChildState(tag);
	}

	/** returns true if the given state can have "occurs" attribute. */
	protected boolean canHaveOccurs( ExpressionState state )
	{
		return
			super.canHaveOccurs(state)
		||	state instanceof InlineElementState;
	}

	protected Expression resolveElementRef( String namespace, String label )
	{
		if( namespace!=null )
		{
			reportError( ERR_NAMESPACE_NOT_SUPPROTED );
			return Expression.nullSet;
		}
		Expression exp = module.elementRules.getOrCreate(label);
		backwardReference.memorizeLink(exp);
		return exp;
	}
	protected Expression resolveHedgeRef( String namespace, String label )
	{
		if( namespace!=null )
		{
			reportError( ERR_NAMESPACE_NOT_SUPPROTED );
			return Expression.nullSet;
		}
		Expression exp = module.hedgeRules.getOrCreate(label);
		backwardReference.memorizeLink(exp);
		return exp;
	}
	protected Expression resolveAttPoolRef( String namespace, String role )
	{
		if( namespace!=null )
		{
			reportError( ERR_NAMESPACE_NOT_SUPPROTED );
			return Expression.nullSet;
		}
		
		AttPoolClause c = module.attPools.getOrCreate(role);
		backwardReference.memorizeLink(c);
		return c;
	}
	
	
// error messages	
	
	public static final String ERR_NAMESPACE_NOT_SUPPROTED =
		"RELAXReader.NamespaceNotSupported";		// arg:0
	public static final String ERR_INCONSISTENT_TARGET_NAMESPACE	// arg:2
		= "RELAXReader.InconsistentTargetNamespace";
	public static final String ERR_MISSING_TARGET_NAMESPACE	// arg:0
		= "RELAXReader.MissingTargetNamespace";
	public static final String ERR_MULTIPLE_TAG_DECLARATIONS	// arg:1
		= "RELAXReader.MultipleTagDeclarations";
	public static final String ERR_MORE_THAN_ONE_INLINE_TAG	// arg:0
		= "RELAXReader.MoreThanOneInlineTag";
	public static final String ERR_MULTIPLE_ATTPOOL_DECLARATIONS	// arg:1
		= "RELAXReader.MultipleAttPoolDeclarations";
	public static final String ERR_UNDEFINED_ELEMENTRULE	// arg:1
		= "RELAXReader.UndefinedElementRule";
	public static final String ERR_UNDEFINED_HEDGERULE	// arg:1
		= "RELAXReader.UndefinedHedgeRule";
	public static final String ERR_UNDEFINED_TAG	// arg:1
		= "RELAXReader.UndefinedTag";
	public static final String ERR_UNDEFINED_ATTPOOL	// arg:1
		= "RELAXReader.UndefinedAttPool";
	public static final String ERR_LABEL_COLLISION	// arg:1
		= "RELAXReader.LabelCollision";
	public static final String ERR_ROLE_COLLISION	// arg:1
		= "RELAXReader.RoleCollision";
	public static final String ERR_NO_EXPROTED_LABEL	// arg:0
		= "RELAXReader.NoExportedLabel";
	public static final String ERR_EXPROTED_HEDGERULE_CONSTRAINT
		= "RELAXReader.ExportedHedgeRuleConstraint";	// arg:1
	public static final String ERR_MULTIPLE_ATTRIBUTE_CONSTRAINT // arg:1
		= "RELAXReader.MultipleAttributeConstraint";
	public static final String ERR_ID_ABUSE // arg:0
		= "RELAXReader.IdAbuse";
	public static final String ERR_ID_ABUSE_1 // arg:1
		= "RELAXReader.IdAbuse.1";
	public static final String WRN_DEPRECATED_TYPENAME = // arg:2
		"RELAXReader.Warning.DeprecatedTypeName";
	public static final String WRN_ILLEGAL_RELAXCORE_VERSION	// arg:1
		= "RELAXReader.Warning.IllegalRelaxCoreVersion";
}
