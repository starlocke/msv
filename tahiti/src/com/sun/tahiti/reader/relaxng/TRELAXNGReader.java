package com.sun.tahiti.reader.relaxng;

import com.sun.msv.grammar.*;
import com.sun.msv.datatype.DatabindableDatatype;
import com.sun.msv.reader.trex.ng.RELAXNGReader;
import com.sun.msv.reader.ExpressionState;
import com.sun.msv.reader.GrammarReaderController;
import com.sun.msv.util.StartTagInfo;
import com.sun.tahiti.reader.RelationNormalizer;
import com.sun.tahiti.grammar.*;
import javax.xml.parsers.SAXParserFactory;
import java.util.Map;

public class TRELAXNGReader extends RELAXNGReader {

	public static final String TahitiNamespace = 
		"http://www.sun.com/xml/tahiti/";
	
	public TRELAXNGReader(
		GrammarReaderController controller,
		SAXParserFactory parserFactory,
		StateFactory stateFactory,
		ExpressionPool pool ) {
		
		super( controller, parserFactory, stateFactory, pool );
	}

	public TRELAXNGReader( GrammarReaderController controller, SAXParserFactory parserFactory ) {
		super( controller, parserFactory );
	}
	
	/**
	 * gets Type of Java object from TypedStringExp.
	 */
	protected Class getJavaType( TypedStringExp texp ) {
		if( texp.dt instanceof DatabindableDatatype ) {
			return ((DatabindableDatatype)texp.dt).getJavaObjectType();
		}
		
		// if any other attempt fails, use String.
		return String.class;
	}
	
	/**
	 * a map from TypedStringExp to the PrimitiveItem which wraps it.
	 * used to unify PrimitiveItems.
	 */
	private final Map primitiveItems = new java.util.HashMap();
	
	protected Expression interceptExpression( ExpressionState state, Expression exp ) {
		// if an error was found, stop further processing.
		if( hadError )	return exp;
		
		
		if( exp instanceof TypedStringExp ) {
			// if this is a typed string, then wrap it by the PrimitiveItem.
			
			if( primitiveItems.containsKey(exp) )
				// if this exp is already wrapped, use it instead of creating another one.
				// this will reduce the size of the LL grammar for data-binding.
				exp = (Expression)primitiveItems.get(exp);
			else {
				// if this is the first time, wrap it and memorize it.
				PrimitiveItem p = new PrimitiveItem(getJavaType((TypedStringExp)exp));
				primitiveItems.put( exp, p );
				p.exp = exp;
				exp = p;
			}
		}
		
		// check Tahiti attributes.
		
		final StartTagInfo tag = state.getStartTag();
		String role = tag.getAttribute(TahitiNamespace,"role");
		
		if( role==null )	return exp;	// the "role" attribute is not present.
		
		
		ReferenceExp roleExp;
		
		if( role.equals("superClass") ) {
			roleExp = new SuperClassItem();
		} else {
			if( role.equals("class") ) {
				roleExp = new ClassItem(decideName(state,exp,role));
			} else
			if( role.equals("field") ) {
				roleExp = new FieldItem(decideName(state,exp,role));
			} else
			if( role.equals("interface") ) {
				roleExp = new InterfaceItem(decideName(state,exp,role));
			} else {
				reportError( ERR_UNDEFINED_ROLE, role );
				return exp;
			}
		}
		
		setDeclaredLocationOf(roleExp);	// memorize where this expression is defined.
		
		
		if( tag.localName.equals("define") ) {
			// if this is <define>, then
			// this tag should be placed as the sole child of <define>.
			ReferenceExp rexp = (ReferenceExp)exp;
			roleExp.exp = rexp.exp;
			rexp.exp = roleExp;
		} else {
			// wrap the expression by this tag.
			roleExp.exp = exp;
			exp = roleExp;
		}
		
		return exp;
	}

	/**
	 * compute the name for the item.
	 * 
	 * @param role
	 *		the role of this expression. One of "field","interface", and "class".
	 */
	protected String decideName( ExpressionState state, Expression exp, String role ) {
		
		final StartTagInfo tag = state.getStartTag();
	
		String name = tag.getAttribute(TahitiNamespace,"name");
		// if we have t:name attribute, use it.
		if(name!=null)	return name;
		
		// if the current tag has the name attribute, use it.
		// this is the case of <define/>,<ref/>, and sometimes
		// <element/> and <attribute/>
		name = tag.getAttribute("name");
		if(name!=null)	return name;
		
		// otherwise, sniff the name.
		
		// if it's element/attribute, then we may be able to use its name.
		if( exp instanceof NameClassAndExpression ) {
			NameClass nc = ((NameClassAndExpression)exp).getNameClass();
			if( nc instanceof SimpleNameClass )
				return xmlNameToJavaName(role,((SimpleNameClass)nc).localName);
			
			// if it's not a simple type, abort.
		}
		
		// we can't generate a proper name. bail out
		reportError( ERR_NAME_NEEDED );
		return "";
	}
	
	/**
	 * convert XML names (like element names) to the corresponding Java names.
	 * 
	 * @param role
	 *		the role of this expression. One of "field","interface", and "class".
	 */
	protected String xmlNameToJavaName( String role, String xmlName ) {
		// TODO
		if( role.equals("class") || role.equals("interface") )
			return Character.toUpperCase(xmlName.charAt(0))+xmlName.substring(1);
		return xmlName;
	}
	
	public void wrapUp() {
		// First, let the super class do its job.
		super.wrapUp();
		
		// if we already have an error, abort further processing.
		if(hadError)	return;
		
		/*
			2nd step. Find all class-class or class-interface relationships
			and change them to class-field-class or class-field-interface
			respectively.
		*/
		grammar.start = RelationNormalizer.normalize( this, grammar.start );
	}

// TODO: add more arguments to produce user-friendly messages.
	
	public static final String ERR_UNDEFINED_ROLE = // arg:1
		null; // "{0}" is a bad value for the role attribute.
	public static final String ERR_NAME_NEEDED = // arg:0
		null;	// failed to generate a proper name for this role.
				// specify t:name attribute.
	
	
}
