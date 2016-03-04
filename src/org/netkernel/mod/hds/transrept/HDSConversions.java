package org.netkernel.mod.hds.transrept;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringWriter;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.netkernel.layer0.nkf.NKFException;
import org.netkernel.layer0.representation.ByteArrayRepresentation;
import org.netkernel.layer0.representation.IReadableBinaryStreamRepresentation;
import org.netkernel.layer0.representation.impl.HDSBuilder;
import org.netkernel.layer0.util.Base64;
import org.netkernel.layer0.util.PairList;
import org.netkernel.layer0.util.XMLUtils;
import org.netkernel.mod.hds.HDSFactory;
import org.netkernel.mod.hds.IHDSDocument;
import org.netkernel.mod.hds.IHDSMutator;
import org.netkernel.mod.hds.IHDSNode;
import org.netkernel.mod.hds.impl.HDSDocumentImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.Attributes2Impl;
import org.xml.sax.helpers.DefaultHandler;

public class HDSConversions
{
	public static final String HDS_LIST_DOCUMENT_ELEMENT="hds";
	public static final String HDS_NAMESPACE_URI="http://netkernel.org/hds";
	
	public static IHDSDocument parseXML(InputStream aStream) throws SAXException, ParserConfigurationException, IOException
	{
		SAXParserFactory spf =  SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		SAXParser parser=spf.newSAXParser();
		HDSSaxHandler h = new HDSSaxHandler();
		parser.parse(aStream,h);
		return h.getDocument();
	}
	
	public static IHDSDocument parseDOM(Node aNode) throws Exception
	{	IHDSMutator b = HDSFactory.newDocument();
		recurseParseDOM(aNode,b);
		return b.toDocument(false);
	}
	
	private static void recurseParseDOM(Node aNode, IHDSMutator aBuilder) throws Exception
	{	
		switch (aNode.getNodeType())
		{	case Node.ELEMENT_NODE:
			{	String name=aNode.getNodeName();
				String text=XMLUtils.getText(aNode,true,false);
				if (text.length()==0)
				{	aBuilder.pushNode(name);
				}
				else
				{	aBuilder.pushNode(name,text);
				}
				Element e=(Element)aNode;
				NamedNodeMap atts=e.getAttributes();
				for (int i=0; i<atts.getLength(); i++)
				{	Node att=atts.item(i);
					if (HDS_NAMESPACE_URI.equals(att.getNamespaceURI()))
					{	String type=att.getNodeValue();
						Object value=parseHDSType(text,type);
						aBuilder.setValue(value);
					}
					else if (att.getNodeValue().equals(HDS_NAMESPACE_URI) && att.getNodeName().startsWith("xmlns:"))
					{	//silently ignore this namespace declaration
					}
					else
					{	aBuilder.addNode("@"+att.getNodeName(), att.getNodeValue());
					}
					
				}
				Element child=XMLUtils.getFirstChildElement(aNode);
				while(child!=null)
				{	recurseParseDOM(child, aBuilder);
					child=XMLUtils.getNextSiblingElement(child);
				}
				
				aBuilder.popNode();
			}
			break;
			case Node.DOCUMENT_NODE:
			{	Element e = ((Document)aNode).getDocumentElement();
				if(e.getNodeName().equals(HDS_LIST_DOCUMENT_ELEMENT))
				{	e=XMLUtils.getFirstChildElement(e);	//Flatten to an HDS list
					recurseParseDOM(e,aBuilder);
					while((e=XMLUtils.getNextSiblingElement(e))!=null)
					{	recurseParseDOM(e,aBuilder);					
					}
				}
				else
				{	recurseParseDOM(e,aBuilder);
				}
			}
			break;
		}
	}
	
	private static class HandlerState
	{
		private StringBuilder sb;
		//private List mChildren;
		private String mType;
		private PairList mPendingPrefixMappings;
		
		private static final IHDSNode[] sNoChildren = new IHDSNode[0];
		
		public void addPrefixMapping(String aPrefixName, String aURI)
		{
			if (mPendingPrefixMappings==null)
			{	mPendingPrefixMappings = new PairList(2);
			}
			mPendingPrefixMappings.put(aPrefixName, aURI);
		}
		
		public PairList getPrefixMappings()
		{	return mPendingPrefixMappings;
		}
		/*
		public IHDSNode[] getChildren()
		{	IHDSNode[] result;
			if (mChildren!=null)
			{	int l=mChildren.size();
				result = new IHDSNode[l];
				for (int i=0; i<l; i++)
				{	result[i]=(IHDSNode)mChildren.get(i);
				}
			}
			else
			{	result = sNoChildren;
			}
			return result;
		}
		*/
		
		public Object getValue() throws Exception
		{	Object result=null;
			String s="";
			if (sb!=null)
			{	boolean isWhitespace=true;
				for (int i=sb.length()-1; i>=0; i--)
				{	char c=sb.charAt(i);
					if (c!=' ' && c!='\n' && c!='\r' && c!='\t')
					{	isWhitespace=false;
						break;
					}
				}
				if (!isWhitespace)
				{	s=sb.toString();
				}
			}
			if (s.length()>0 || mType!=null)
			{	if (mType!=null)
				{	result=parseHDSType(s, mType);
				}
				else
				{	result=s;
				}
			}
			return result;
		}
		
		/*
		public void addChild(IHDSNode aChild)
		{	if (mChildren==null)
			{	mChildren=new ArrayList(8);
			}
			mChildren.add(aChild);
		}
		*/
		
		public void addText(char[] ch, int start, int length)
		{	if (sb==null)
			{	sb=new StringBuilder(64);
			}
			
			sb.append(ch, start, length);
		}
		
		public void setType(String aType)
		{	mType=aType;
		}
	}
	
	private static class HDSSaxHandler extends DefaultHandler
	{
		private IHDSMutator mDoc=HDSFactory.newDocument();
		private HandlerState mCursorState = new HandlerState();
		private List mHandlerState=new ArrayList();
		
		public IHDSDocument getDocument()
		{	return mDoc.toDocument(false);
		}
		
		@Override
		public void startPrefixMapping(String aPrefixName, String aURI) throws SAXException
		{	if (!aURI.equals(HDS_NAMESPACE_URI))
			{	mCursorState.addPrefixMapping(aPrefixName, aURI);
			}
		}

		public void startElement(String uri, String localName, String qName, Attributes attributes)
		{	
			if (qName.equals(HDS_LIST_DOCUMENT_ELEMENT) && mDoc.getFirstNodeOrNull("/*")==null)
			{	mCursorState = new HandlerState();
			}
			else
			{
				mDoc.pushNode(qName);
				
				PairList prefixMappings = mCursorState!=null?mCursorState.getPrefixMappings():null;
				
				mCursorState = new HandlerState();
				
				if (prefixMappings!=null)
				{	for (int i=0; i<prefixMappings.size(); i++)
					{	String prefix=(String)prefixMappings.getValue1(i);
						String uriValue=(String)prefixMappings.getValue2(i);
						String attName=prefix.length()>0?"@xmlns:"+prefix:"@xmlns";
						mDoc.addNode(attName, uriValue);
					}
				}
				
				int l=attributes.getLength();
				for (int i=0; i<l; i++)
				{	String name=attributes.getQName(i);
					String text=attributes.getValue(i);
					String ns=attributes.getURI(i);
					
					if (ns.equals(HDS_NAMESPACE_URI))
					{	mCursorState.setType(text);
					}
					else if (text.equals(HDS_NAMESPACE_URI) && name.startsWith("xmlns:"))
					{	//ignore hds namespace declarations
					}
					else
					{	mDoc.addNode("@"+name, text);
					}
				}
	
				mHandlerState.add(mCursorState);
			}
		}
		
		public void endElement(String uri, String localName, String qName) throws SAXException
		{	
			try
			{	mDoc.setValue(mCursorState.getValue());
			}
			catch (Exception e)
			{	throw new SAXParseException("Failed to recreate value for node",null,e);
			}
			int i=mHandlerState.size();
			if (i>0)
			{	mHandlerState.remove(i-1);
				if (i>=2)
				{	mCursorState=(HandlerState)mHandlerState.get(i-2);
				}
				mDoc.popNode();
			}
		}
		
		public void characters(char[] ch, int start, int length)
		{	mCursorState.addText(ch, start, length);
		}
		
	}
	
	public static IHDSDocument convertHDS1(org.netkernel.layer0.representation.IHDSNode aHDS)
	{	IHDSMutator m=HDSFactory.newDocument();
		org.netkernel.layer0.representation.IHDSNode root=aHDS;
		if (aHDS.getName()==null && aHDS.getValue()==null)
		{	for (org.netkernel.layer0.representation.IHDSNode child: aHDS.getChildren())
			{	recurseConvertHDS(m,child);
			}
		}
		else
		{	recurseConvertHDS(m,aHDS);
		}
		return m.toDocument(false);
	}
	
	private static void recurseConvertHDS(IHDSMutator aMutator, org.netkernel.layer0.representation.IHDSNode aHDS)
	{	aMutator.pushNode(aHDS.getName(), aHDS.getValue());
		for (org.netkernel.layer0.representation.IHDSNode child : aHDS.getChildren())
		{	recurseConvertHDS(aMutator,child);
		}
		aMutator.popNode();
	}
	
	
	
	public static Document toDOM(IHDSDocument aRoot) throws NKFException, ParserConfigurationException
	{	return toDOM(aRoot,true);
	}
	
	public static Document toDOM(IHDSDocument aRoot, boolean aToString) throws NKFException, ParserConfigurationException
	{	Document d=XMLUtils.newDocument();
		IHDSNode root=aRoot.getRootNode();
		IHDSNode n=null;
		Element e=null;

		IHDSNode[] children=root.getChildren();
		if(children.length>1)
		{	//This is a flattened HDS list - create a default hds document element
			e=d.createElement(HDS_LIST_DOCUMENT_ELEMENT);
			n=root;
		}
		else if (children.length==0)
		{	e=d.createElement(HDS_LIST_DOCUMENT_ELEMENT);
			n=null;
		}
		else
		{	n=children[0];
		}
		
		if(e==null)
		{	e=d.createElement(n.getName());
		}
		if (!aToString)
		{	e.setAttribute("xmlns:hds", HDS_NAMESPACE_URI);
		}
		d.appendChild(e);

		if (n!=null)
		{	Object value=n.getValue();
			if(value!=null)
			{	if (aToString)
				{	Node t=d.createTextNode(value.toString());
					e.appendChild(t);
				}
				else
				{	String[] v=serialiseHDSType(value);
					e.setAttributeNS(HDS_NAMESPACE_URI, "hds:type", v[1]);
					Node t=d.createTextNode(v[0]);
					e.appendChild(t);
				}
			}
			NSLookup lookup = new NSLookup();
			recurseHDS(d, e, n, lookup,false, aToString);
		}
		return d;
	}
	
	private static void recurseHDS(Document d, Element e, IHDSNode n, NSLookup aLookup, boolean aAddThis, boolean aToString) throws NKFException
	{	
		Element e2=e;
		if (aAddThis)
		{
			String name=n.getName();
			Object value=n.getValue();
			if (name==null)
			{	throw new NKFException("bad node with null name");
			}
			else if(name.startsWith("@"))
			{	//Attribute
				if(value==null)
				{	value=""; 	//make this an empty attribute						
				}
				addAttribute(d, e, name.substring(1), value.toString(), aLookup);
			}
			else
			{	//Element
				e2=createElement(d,e,name,aLookup);
				if(value!=null)
				{	if (aToString)
					{	
						String v=value.toString();
						Node t=createNodeFor(v,d);
						e2.appendChild(t);
					}
					else
					{	String[] v=serialiseHDSType(value);
						Node t;
						if (v[1].equals(HDSType.STRING.name()))
						{	t=createNodeFor(v[0], d);
						}
						else
						{	e2.setAttributeNS(HDS_NAMESPACE_URI, "hds:type", v[1]);
							t=d.createTextNode(v[0]);
						}
						e2.appendChild(t);
					}
				}
				e.appendChild(e2);
			}
		}
		
		IHDSNode[] children=n.getChildren();
		if(children!=null)
		{	for(int i=0; i<children.length; i++)
			{	IHDSNode n2=children[i];
				recurseHDS(d, e2, n2, aLookup,true, aToString);
			}
		}
	}
	
	private static Node createNodeFor(String v, Document d)
	{	Node t;
		if (v.startsWith("<!--") && v.endsWith("-->") && v.length()>7)
		{	t=d.createComment(v.substring(4,v.length()-3));
		}
		else if (v.startsWith("<![CDATA[") && v.endsWith("]]>"))
		{	t=d.createCDATASection(v.substring(9,v.length()-3));
		}
		else
		{	t=d.createTextNode(v);
		}
		return t;
	}
	
	private static Element createElement(Document aDocument, Element aParent, String aName, NSLookup aLookup)
	{
		int i=aName.indexOf(':');
		if (i>=0)
		{	String prefix=aName.substring(0,i);
			String ns=aLookup.getURL(aParent, prefix);
			return aDocument.createElementNS(ns, aName);
		}
		else
		{	return aDocument.createElement(aName);
		}
	}
	
	private static void addAttribute(Document aDocument, Element aParent, String aName, String aValue, NSLookup aLookup)
	{
		int i=aName.indexOf(':');
		if (i>=0)
		{	String prefix=aName.substring(0,i);

			if (prefix.equals("xmlns"))
			{	String nsdecl = aName.substring(i+1);
				aLookup.putNS(aParent,nsdecl,aValue);
				aParent.setAttribute(aName,aValue);
			}
			else
			{	String ns=aLookup.getURL(aParent, prefix);
				aParent.setAttributeNS(ns, aName, aValue);
			}
		}
		else
		{	aParent.setAttribute(aName,aValue);
		}
	}

	private static class NSLookup
	{
		private TreeMap<String,String> mNamespaces = new TreeMap<String,String>();
		
		public void putNS(Element aElement, String aPrefix, String aURL)
		{	String key=aPrefix+":"+XMLUtils.getPathFor(aElement);
			mNamespaces.put(key, aURL);
		}
		
		public String getURL(Element aElement, String aPrefix)
		{	
			String result=null;
			String key=aPrefix+":"+XMLUtils.getPathFor(aElement);
			result=mNamespaces.get(key);
			if (result==null)
			{	SortedMap<String,String> m=mNamespaces.headMap(key);
				if (!m.isEmpty())
				{	String possibleMatch = m.lastKey();
					if (key.startsWith(possibleMatch))
					{	result=m.get(possibleMatch);
					}
				}
			}
			return result;			
		}
	}
	
	private enum HDSType { BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BOOLEAN, CHAR, STRING, DATE, OTHER };

	
	private static Object parseHDSType(String aText, String aType) throws Exception
	{	
		//TODO
		HDSType lt;
		try
		{	lt=HDSType.valueOf(aType.toUpperCase());
		}
		catch (IllegalArgumentException e2)
		{	lt=HDSType.OTHER;
		}
		Object result;
		switch (lt)
		{	case BYTE:
				result=Byte.parseByte(aText);
				break;
			case SHORT:
				result=Short.parseShort(aText);
				break;
			case INTEGER:
				result=Integer.parseInt(aText);
				break;
			case LONG:
				result=Long.parseLong(aText);
				break;
			case FLOAT:
				result=Float.parseFloat(aText);
				break;
			case DOUBLE:
				result=Double.parseDouble(aText);
				break;
			case BOOLEAN:
				result=Boolean.parseBoolean(aText);
				break;
			case CHAR:
				if (aText.length()==1)
				{	result=aText.charAt(0);
				}
				else
				{	throw new Exception("char value expected to be single character");
				}
				break;
			case STRING:
				result=aText;
				break;
			case DATE:
				long time=Long.parseLong(aText);
				result = new Date(time);
				break;
			case OTHER:
				byte[] bytes=Base64.decode(aText);
				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
				result=ois.readObject();
				break;
			default:
				result=aText;
		}
		return result;
	}
	
	private static Map<Class,HDSType> sTypes=new HashMap<Class,HDSType>();
	static
	{	sTypes.put(Byte.class, HDSType.BYTE);
		sTypes.put(Short.class, HDSType.SHORT);
		sTypes.put(Integer.class, HDSType.INTEGER);
		sTypes.put(Long.class, HDSType.LONG);
		sTypes.put(Float.class, HDSType.FLOAT);
		sTypes.put(Double.class, HDSType.DOUBLE);
		sTypes.put(Boolean.class, HDSType.BOOLEAN);
		sTypes.put(Character.class, HDSType.CHAR);
		sTypes.put(String.class, HDSType.STRING);
		sTypes.put(Date.class, HDSType.DATE);
		sTypes.put(java.sql.Date.class, HDSType.DATE);
	}
	
	private static String[] serialiseHDSType(Object aValue) throws NKFException
	{
		String text;
		String typeString;
		HDSType type = sTypes.get(aValue.getClass());
		if (type==null)
		{	type=HDSType.OTHER;
		}
		
		switch(type)
		{	case OTHER:
				try
				{	ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
					ObjectOutputStream oos = new ObjectOutputStream(os);
					oos.writeObject(aValue);
					oos.flush();
					text=Base64.encodeBytes(os.toByteArray(), Base64.GZIP);
					typeString=aValue.getClass().getName();
				}
				catch (IOException e)
				{	throw new NKFException("Failed to serialise ["+aValue.getClass().getName()+"]",null,e);
				}
				break;
			case DATE:
				text=Long.toString(((Date)aValue).getTime());
				typeString=type.name();
				break;
			default:
				text=aValue.toString();
				typeString=type.name();
				break;
		}
		return new String[] {text,typeString};
	}
	
	
	public static Object serializeHDS(IHDSDocument aDoc, Class aClass) throws Exception
	{
		Transformer xform = TransformerFactory.newInstance().newTransformer();
        XMLReader reader = new HDSReader(aDoc);
        if (aClass==String.class)
        {   StringWriter sw=new StringWriter(1024);
            xform.transform(new SAXSource(reader,null), new StreamResult(sw));
            return sw.toString();
        }
        else if (aClass==IReadableBinaryStreamRepresentation.class)
        {	ByteArrayOutputStream baos=new ByteArrayOutputStream(1024);
            xform.transform(new SAXSource(reader,null), new StreamResult(baos));
            return new ByteArrayRepresentation(baos);
        }
        else throw new IllegalArgumentException("unsupported class");
	}
	
	private static class HDSReader implements XMLReader
	{
		private IHDSDocument mDoc;
		private ContentHandler mHandler;
		public HDSReader(IHDSDocument aDoc)
		{	mDoc=aDoc;
		}
		
		@Override
		public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException
		{	return false;
		}

		@Override
		public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException
		{
		}

		@Override
		public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException
		{	return null;
		}

		@Override
		public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException
		{
		}

		@Override
		public void setEntityResolver(EntityResolver resolver)
		{
		}

		@Override
		public EntityResolver getEntityResolver()
		{	return null;
		}

		@Override
		public void setDTDHandler(DTDHandler handler)
		{
		}

		@Override
		public DTDHandler getDTDHandler()
		{	return null;
		}

		@Override
		public void setContentHandler(ContentHandler handler)
		{	mHandler=handler;
		}

		@Override
		public ContentHandler getContentHandler()
		{	return null;
		}

		@Override
		public void setErrorHandler(ErrorHandler handler)
		{
		}

		@Override
		public ErrorHandler getErrorHandler()
		{	return null;
		}

		@Override
		public void parse(InputSource input) throws IOException, SAXException
		{
			mHandler.startDocument();
			IHDSNode n=mDoc.getRootNode();
			if (n.getChildren().length>1)
			{	
				Attributes2Impl attributes=new Attributes2Impl();
				attributes.addAttribute(null, "hds", "xmlns:hds", "CDATA", HDS_NAMESPACE_URI);
				mHandler.startElement(null, HDS_LIST_DOCUMENT_ELEMENT, HDS_LIST_DOCUMENT_ELEMENT, attributes);
				for (IHDSNode child: n.getChildren())
				{	recurseParse(child,false);
				}
				mHandler.endElement(null, HDS_LIST_DOCUMENT_ELEMENT, HDS_LIST_DOCUMENT_ELEMENT);
			}
			else
			{	if (n.getChildren().length==1)
				{	recurseParse(n.getChildren()[0],true);
				}
			}
			mHandler.endDocument();
		}
		
		private Map<String,String> mPrefixLookup = new HashMap<String, String>();
		
		private void recurseParse(IHDSNode aNode, boolean aRoot) throws SAXException
		{
			String name=aNode.getName();
			String localName;
			String uri=null;
			if (name==null)
			{	name=localName="null";
			}
			else
			{	int i=name.indexOf(':');
				if (i>=0)
				{	String prefix=name.substring(0,i);
					localName=name.substring(i+1);
					//uri=mPrefixLookup.get(prefix);
				}
				else
				{	localName=name;
					//uri=mPrefixLookup.get("");
				}
			}
			
			Attributes2Impl attributes = new Attributes2Impl();
			
			for (IHDSNode child : aNode.getChildren())
			{	String childName=child.getName();
				if (childName==null)
				{	System.out.println("here null child name");
				}
				if (childName!=null && childName.startsWith("@"))
				{	String attrName=childName.substring(1);
					Object value=child.getValue();
					if (childName.startsWith("@xmlns"))
					{	if (value!=null && value instanceof String)
						{	
							if (childName.length()==6)
							{	mPrefixLookup.put("", (String)value);
								attributes.addAttribute(null, attrName, attrName, "CDATA", (String)value);
							}
							else
							{	String prefix=childName.substring(7);
								mPrefixLookup.put(prefix, (String)value);
								attributes.addAttribute(null, attrName, attrName, "CDATA", (String)value);
							}
						}
					}
					else
					{	if (value==null)
						{	value="";
						}
						else if (!(value instanceof String))
						{	value=value.toString();
						}
					
						int i=attrName.indexOf(':');
						String attrLocalName;
						String attrUri=null;
						if (i>=0)
						{	String prefix=attrName.substring(0,i);
							attrLocalName=attrName.substring(i+1);
							attrUri=mPrefixLookup.get(prefix);						
						}
						else
						{	attrLocalName=attrName;
						}
						attributes.addAttribute(attrUri, attrLocalName, attrName, "CDATA", (String)value);
					}
				}
			}
			
			if (aRoot)
			{	attributes.addAttribute(null, "hds", "xmlns:hds", "CDATA", HDS_NAMESPACE_URI);
			}
			
			String text=null;
			try
			{	Object value=aNode.getValue();
				if (value!=null)
				{	String[] v=serialiseHDSType(value);
					text=v[0];
					if (!v[1].equals(HDSType.STRING.name()) || (v[1].equals(HDSType.STRING.name()) && v[0].length()==0))
					{	attributes.addAttribute(null, "type", "hds:type", "CDATA", v[1]);
					}
				}
			}
			catch (NKFException e)
			{	throw new SAXException(e);
			}
			
			mHandler.startElement(uri, localName, name, attributes);
			if (text!=null)
			{	mHandler.characters(text.toCharArray(), 0, text.length());
			}
			
			for (IHDSNode child : aNode.getChildren())
			{	String childName=child.getName();
				if (childName==null || !childName.startsWith("@"))
				{	recurseParse(child,false);
				}
			}	
			mHandler.endElement(uri, localName, name);
		}

		@Override
		public void parse(String systemId) throws IOException, SAXException
		{
		}
	}
		
	public static org.netkernel.layer0.representation.IHDSNode convertToHDS1(IHDSDocument aDoc)
	{	HDSBuilder b=new HDSBuilder();
		IHDSNode root=aDoc.getRootNode();
		for (IHDSNode child : root.getChildren())
		{	recurseConvertToHDS1(b,child);
		}
		return b.getRoot();
	}
	
	private static void recurseConvertToHDS1(HDSBuilder aBuilder, IHDSNode aHDS)
	{	aBuilder.pushNode(aHDS.getName(), aHDS.getValue());
		for (IHDSNode child : aHDS.getChildren())
		{	recurseConvertToHDS1(aBuilder,child);
		}
		aBuilder.popNode();
	}
}
