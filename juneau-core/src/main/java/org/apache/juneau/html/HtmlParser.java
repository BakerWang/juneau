// ***************************************************************************************************************************
// * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements.  See the NOTICE file *
// * distributed with this work for additional information regarding copyright ownership.  The ASF licenses this file        *
// * to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance            *
// * with the License.  You may obtain a copy of the License at                                                              *
// *                                                                                                                         *
// *  http://www.apache.org/licenses/LICENSE-2.0                                                                             *
// *                                                                                                                         *
// * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an  *
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the        *
// * specific language governing permissions and limitations under the License.                                              *
// ***************************************************************************************************************************
package org.apache.juneau.html;

import static javax.xml.stream.XMLStreamConstants.*;
import static org.apache.juneau.html.HtmlTag.*;
import static org.apache.juneau.internal.StringUtils.*;

import java.lang.reflect.*;
import java.util.*;

import javax.xml.stream.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.json.*;
import org.apache.juneau.parser.*;
import org.apache.juneau.transform.*;
import org.apache.juneau.xml.*;

/**
 * Parses text generated by the {@link HtmlSerializer} class back into a POJO model.
 *
 * <h5 class='section'>Media types:</h5>
 * <p>
 * Handles <code>Content-Type</code> types: <code>text/html</code>
 *
 * <h5 class='section'>Description:</h5>
 * <p>
 * See the {@link HtmlSerializer} class for a description of the HTML generated.
 * <p>
 * This class is used primarily for automated testing of the {@link HtmlSerializer} class.
 *
 * <h5 class='section'>Configurable properties:</h5>
 * <p>
 * This class has the following properties associated with it:
 * <ul>
 * 	<li>{@link HtmlSerializerContext}
 * </ul>
 */
@SuppressWarnings({ "rawtypes", "unchecked", "hiding" })
@Consumes("text/html,text/html+stripped")
public class HtmlParser extends XmlParser {

	/** Default parser, all default settings.*/
	public static final HtmlParser DEFAULT = new HtmlParser(PropertyStore.create());


	private final HtmlParserContext ctx;

	/**
	 * Constructor.
	 * @param propertyStore The property store containing all the settings for this object.
	 */
	public HtmlParser(PropertyStore propertyStore) {
		super(propertyStore);
		this.ctx = createContext(HtmlParserContext.class);
	}

	@Override /* CoreObject */
	public HtmlParserBuilder builder() {
		return new HtmlParserBuilder(propertyStore);
	}

	/*
	 * Reads anything starting at the current event.
	 * <p>
	 * Precondition:  Must be pointing at outer START_ELEMENT.
	 * Postcondition:  Pointing at outer END_ELEMENT.
	 */
	private <T> T parseAnything(HtmlParserSession session, ClassMeta<T> eType, XMLStreamReader r, Object outer, boolean isRoot, BeanPropertyMeta pMeta) throws Exception {

		if (eType == null)
			eType = (ClassMeta<T>)object();
		PojoSwap<T,Object> transform = (PojoSwap<T,Object>)eType.getPojoSwap();
		ClassMeta<?> sType = eType.getSerializedClassMeta();
		session.setCurrentClass(sType);

		int event = r.getEventType();
		if (event != START_ELEMENT)
			throw new XMLStreamException("parseAnything must be called on outer start element.", r.getLocation());

		if (! isRoot)
			event = r.next();
		boolean isEmpty = (event == END_ELEMENT);

		// Skip until we find a start element, end document, or non-empty text.
		if (! isEmpty)
			event = skipWs(r);

		if (event == END_DOCUMENT)
			throw new XMLStreamException("Unexpected end of stream in parseAnything for type '"+eType+"'", r.getLocation());

		// Handle @Html(asXml=true) beans.
		HtmlClassMeta hcm = sType.getExtendedMeta(HtmlClassMeta.class);
		if (hcm.isAsXml())
			return super.parseAnything(session, eType, null, r, outer, false, pMeta);

		Object o = null;

		boolean isValid = true;
		HtmlTag tag = (event == CHARACTERS ? null : HtmlTag.forString(r.getName().getLocalPart(), false));

		if (tag == HTML)
			tag = skipToData(r);

		if (isEmpty) {
			o = "";
		} else if (tag == null || tag.isOneOf(BR,BS,FF,SP)) {
			String text = session.parseText(r);
			if (sType.isObject() || sType.isCharSequence())
				o = text;
			else if (sType.isChar())
				o = text.charAt(0);
			else if (sType.isBoolean())
				o = Boolean.parseBoolean(text);
			else if (sType.isNumber())
				o = parseNumber(text, (Class<? extends Number>)eType.getInnerClass());
			else if (sType.canCreateNewInstanceFromString(outer))
				o = sType.newInstanceFromString(outer, text);
			else if (sType.canCreateNewInstanceFromNumber(outer))
				o = sType.newInstanceFromNumber(session, outer, parseNumber(text, sType.getNewInstanceFromNumberClass()));
			else
				isValid = false;

		} else if (tag == STRING) {
			String text = session.getElementText(r);
			if (sType.isObject() || sType.isCharSequence())
				o = text;
			else if (sType.isChar())
				o = text.charAt(0);
			else if (sType.canCreateNewInstanceFromString(outer))
				o = sType.newInstanceFromString(outer, text);
			else if (sType.canCreateNewInstanceFromNumber(outer))
				o = sType.newInstanceFromNumber(session, outer, parseNumber(text, sType.getNewInstanceFromNumberClass()));
			else
				isValid = false;
			skipTag(r, xSTRING);

		} else if (tag == NUMBER) {
			String text = session.getElementText(r);
			if (sType.isObject())
				o = parseNumber(text, Number.class);
			else if (sType.isNumber())
				o = parseNumber(text, (Class<? extends Number>)sType.getInnerClass());
			else if (sType.canCreateNewInstanceFromNumber(outer))
				o = sType.newInstanceFromNumber(session, outer, parseNumber(text, sType.getNewInstanceFromNumberClass()));
			else
				isValid = false;
			skipTag(r, xNUMBER);

		} else if (tag == BOOLEAN) {
			String text = session.getElementText(r);
			if (sType.isObject() || sType.isBoolean())
				o = Boolean.parseBoolean(text);
			else
				isValid = false;
			skipTag(r, xBOOLEAN);

		} else if (tag == P) {
			String text = session.getElementText(r);
			if (! "No Results".equals(text))
				isValid = false;
			skipTag(r, xP);

		} else if (tag == NULL) {
			skipTag(r, NULL);
			skipTag(r, xNULL);

		} else if (tag == A) {
			o = parseAnchor(session, r, eType);
			skipTag(r, xA);

		} else if (tag == TABLE) {

			String typeName = getAttribute(r, session.getBeanTypePropertyName(), "object");
			ClassMeta cm = session.getClassMeta(typeName, pMeta, eType);

			if (cm != null) {
				sType = eType = cm;
				typeName = sType.isArray() ? "array" : "object";
			} else if (! "array".equals(typeName)) {
				// Type name could be a subtype name.
				typeName = "object";
			}

			if (typeName.equals("object")) {
				if (sType.isObject()) {
					o = parseIntoMap(session, r, (Map)new ObjectMap(session), sType.getKeyType(), sType.getValueType(), pMeta);
				} else if (sType.isMap()) {
					o = parseIntoMap(session, r, (Map)(sType.canCreateNewInstance(outer) ? sType.newInstance(outer) : new ObjectMap(session)), sType.getKeyType(), sType.getValueType(), pMeta);
				} else if (sType.canCreateNewBean(outer)) {
					BeanMap m = session.newBeanMap(outer, sType.getInnerClass());
					o = parseIntoBean(session, r, m).getBean();
				} else {
					isValid = false;
				}
				skipTag(r, xTABLE);

			} else if (typeName.equals("array")) {
				if (sType.isObject())
					o = parseTableIntoCollection(session, r, (Collection)new ObjectList(session), sType.getElementType(), pMeta);
				else if (sType.isCollection())
					o = parseTableIntoCollection(session, r, (Collection)(sType.canCreateNewInstance(outer) ? sType.newInstance(outer) : new ObjectList(session)), sType.getElementType(), pMeta);
				else if (sType.isArray()) {
					ArrayList l = (ArrayList)parseTableIntoCollection(session, r, new ArrayList(), sType.getElementType(), pMeta);
					o = session.toArray(sType, l);
				}
				else
					isValid = false;
				skipTag(r, xTABLE);

			} else {
				isValid = false;
			}

		} else if (tag == UL) {
			String typeName = getAttribute(r, session.getBeanTypePropertyName(), "array");
			ClassMeta cm = session.getClassMeta(typeName, pMeta, eType);
			if (cm != null)
				sType = eType = cm;

			if (sType.isObject())
				o = parseIntoCollection(session, r, (Collection)new ObjectList(session), sType.getElementType(), pMeta);
			else if (sType.isCollection() || sType.isObject())
				o = parseIntoCollection(session, r, (Collection)(sType.canCreateNewInstance(outer) ? sType.newInstance(outer) : new ObjectList(session)), sType.getElementType(), pMeta);
			else if (sType.isArray())
				o = session.toArray(sType, parseIntoCollection(session, r, new ArrayList(), sType.getElementType(), pMeta));
			else
				isValid = false;
			skipTag(r, xUL);

		}

		if (! isValid)
			throw new XMLStreamException("Unexpected tag '"+tag+"' for type '"+eType+"'", r.getLocation());

		if (transform != null && o != null)
			o = transform.unswap(session, o, eType);

		if (outer != null)
			setParent(eType, o, outer);

		skipWs(r);
		return (T)o;
	}

	/**
	 * For parsing output from HtmlDocSerializer, this skips over the head, title, and links.
	 */
	private static HtmlTag skipToData(XMLStreamReader r) throws XMLStreamException {
		while (true) {
			int event = r.next();
			if (event == START_ELEMENT && "div".equals(r.getLocalName()) && "data".equals(r.getAttributeValue(null, "id"))) {
				r.nextTag();
				event = r.getEventType();
				boolean isEmpty = (event == END_ELEMENT);
				// Skip until we find a start element, end document, or non-empty text.
				if (! isEmpty)
					event = skipWs(r);
				if (event == END_DOCUMENT)
					throw new XMLStreamException("Unexpected end of stream looking for data.", r.getLocation());
				return (event == CHARACTERS ? null : HtmlTag.forString(r.getName().getLocalPart(), false));
			}
		}
	}

	private static String getAttribute(XMLStreamReader r, String name, String def) {
		for (int i = 0; i < r.getAttributeCount(); i++)
			if (r.getAttributeLocalName(i).equals(name))
				return r.getAttributeValue(i);
		return def;
	}

	/*
	 * Reads an anchor tag and converts it into a bean.
	 */
	private static <T> T parseAnchor(HtmlParserSession session, XMLStreamReader r, ClassMeta<T> beanType) throws XMLStreamException {
		String href = r.getAttributeValue(null, "href");
		String name = session.getElementText(r);
		Class<T> beanClass = beanType.getInnerClass();
		if (beanClass.isAnnotationPresent(HtmlLink.class)) {
			HtmlLink h = beanClass.getAnnotation(HtmlLink.class);
			BeanMap<T> m = session.newBeanMap(beanClass);
			m.put(h.hrefProperty(), href);
			m.put(h.nameProperty(), name);
			return m.getBean();
		}
		return session.convertToType(href, beanType);
	}

	private static Map<String,String> getAttributes(XMLStreamReader r) {
		Map<String,String> m = new TreeMap<String,String>() ;
 		for (int i = 0; i < r.getAttributeCount(); i++)
 			m.put(r.getAttributeLocalName(i), r.getAttributeValue(i));
		return m;
	}

	/*
	 * Reads contents of <table> element.
	 * Precondition:  Must be pointing at <table> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private <K,V> Map<K,V> parseIntoMap(HtmlParserSession session, XMLStreamReader r, Map<K,V> m, ClassMeta<K> keyType, ClassMeta<V> valueType, BeanPropertyMeta pMeta) throws Exception {
		while (true) {
			HtmlTag tag = nextTag(r, TR, xTABLE);
			if (tag == xTABLE)
				break;
			tag = nextTag(r, TD, TH);
			// Skip over the column headers.
			if (tag == TH) {
				skipTag(r);
				r.nextTag();
				skipTag(r);
			} else {
				K key = parseAnything(session, keyType, r, m, false, pMeta);
				nextTag(r, TD);
				V value = parseAnything(session, valueType, r, m, false, pMeta);
				setName(valueType, value, key);
				m.put(key, value);
			}
			nextTag(r, xTR);
		}

		return m;
	}

	/*
	 * Reads contents of <ul> element.
	 * Precondition:  Must be pointing at event following <ul> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private <E> Collection<E> parseIntoCollection(HtmlParserSession session, XMLStreamReader r, Collection<E> l, ClassMeta<E> elementType, BeanPropertyMeta pMeta) throws Exception {
		while (true) {
			HtmlTag tag = nextTag(r, LI, xUL);
			if (tag == xUL)
				break;
			l.add(parseAnything(session, elementType, r, l, false, pMeta));
		}
		return l;
	}

	/*
	 * Reads contents of <ul> element into an Object array.
	 * Precondition:  Must be pointing at event following <ul> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private Object[] parseArgs(HtmlParserSession session, XMLStreamReader r, ClassMeta<?>[] argTypes) throws Exception {
		HtmlTag tag = HtmlTag.forEvent(r);

		// Special case:
		// Serializing args containing a single bean (or multiple beans of the same type) will end up serialized as a <table _type='array'>
		if (tag == TABLE) {
			List<Object> l = (List<Object>)parseAnything(session, session.getClassMeta(List.class, argTypes[0]), r, session.getOuter(), true, null);
			return l.toArray(new Object[l.size()]);
		}

		Object[] o = new Object[argTypes.length];
		int i = 0;
		while (true) {
			tag = nextTag(r, LI, xUL);
			if (tag == xUL)
				break;
			o[i] = parseAnything(session, argTypes[i], r, session.getOuter(), false, null);
			i++;
		}
		return o;
	}

	/*
	 * Reads contents of <ul> element.
	 * Precondition:  Must be pointing at event following <ul> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private <E> Collection<E> parseTableIntoCollection(HtmlParserSession session, XMLStreamReader r, Collection<E> l, ClassMeta<E> elementType, BeanPropertyMeta pMeta) throws Exception {

		if (elementType == null)
			elementType = (ClassMeta<E>)object();

		HtmlTag tag = nextTag(r, TR);
		List<String> keys = new ArrayList<String>();
		while (true) {
			tag = nextTag(r, TH, xTR);
			if (tag == xTR)
				break;
			keys.add(session.getElementText(r));
		}

		while (true) {
			r.nextTag();
			tag = HtmlTag.forEvent(r);
			if (tag == xTABLE)
				break;

			String type = getAttribute(r, session.getBeanTypePropertyName(), null);
			ClassMeta elementType2 = session.getClassMeta(type, pMeta, null);
			if (elementType2 != null)
				elementType = elementType2;

			if (elementType.canCreateNewBean(l)) {
				BeanMap m = session.newBeanMap(l, elementType.getInnerClass());
				for (int i = 0; i < keys.size(); i++) {
					tag = nextTag(r, TD, NULL);
					if (tag == NULL) {
						m = null;
						nextTag(r, xNULL);
						break;
					}
					String key = keys.get(i);
					BeanMapEntry e = m.getProperty(key);
					if (e == null) {
						//onUnknownProperty(key, m, -1, -1);
						parseAnything(session, object(), r, l, false, null);
					} else {
						BeanPropertyMeta bpm = e.getMeta();
						ClassMeta<?> cm = bpm.getClassMeta();
						Object value = parseAnything(session, cm, r, m.getBean(false), false, bpm);
						setName(cm, value, key);
						bpm.set(m, value);
					}
				}
				l.add(m == null ? null : (E)m.getBean());
			} else {
				String c = getAttributes(r).get(session.getBeanTypePropertyName());
				Map m = (Map)(elementType.isMap() && elementType.canCreateNewInstance(l) ? elementType.newInstance(l) : new ObjectMap(session));
				for (int i = 0; i < keys.size(); i++) {
					tag = nextTag(r, TD, NULL);
					if (tag == NULL) {
						m = null;
						nextTag(r, xNULL);
						break;
					}
					String key = keys.get(i);
					if (m != null) {
						ClassMeta<?> kt = elementType.getKeyType(), vt = elementType.getValueType();
						Object value = parseAnything(session, vt, r, l, false, pMeta);
						setName(vt, value, key);
						m.put(session.convertToType(key, kt), value);
					}
				}
				if (m != null && c != null) {
					ObjectMap m2 = (m instanceof ObjectMap ? (ObjectMap)m : new ObjectMap(m).setBeanSession(session));
					m2.put(session.getBeanTypePropertyName(), c);
					l.add((E)session.cast(m2, pMeta, null));
				} else {
					l.add((E)m);
				}
			}
			nextTag(r, xTR);
		}
		return l;
	}

	/*
	 * Reads contents of <table> element.
	 * Precondition:  Must be pointing at event following <table> event.
	 * Postcondition:  Pointing at next START_ELEMENT or END_DOCUMENT event.
	 */
	private <T> BeanMap<T> parseIntoBean(HtmlParserSession session, XMLStreamReader r, BeanMap<T> m) throws Exception {
		while (true) {
			HtmlTag tag = nextTag(r, TR, xTABLE);
			if (tag == xTABLE)
				break;
			tag = nextTag(r, TD, TH);
			// Skip over the column headers.
			if (tag == TH) {
				skipTag(r);
				r.nextTag();
				skipTag(r);
			} else {
				String key = session.getElementText(r);
				nextTag(r, TD);
				BeanPropertyMeta pMeta = m.getPropertyMeta(key);
				if (pMeta == null) {
					onUnknownProperty(session, key, m, -1, -1);
					parseAnything(session, object(), r, null, false, null);
				} else {
					ClassMeta<?> cm = pMeta.getClassMeta();
					Object value = parseAnything(session, cm, r, m.getBean(false), false, pMeta);
					setName(cm, value, key);
					pMeta.set(m, value);
				}
			}
			nextTag(r, xTR);
		}
		return m;
	}

	/*
	 * Reads the next tag.  Advances past anything that's not a start or end tag.  Throws an exception if
	 * 	it's not one of the expected tags.
	 * Precondition:  Must be pointing before the event we want to parse.
	 * Postcondition:  Pointing at the tag just parsed.
	 */
	private static HtmlTag nextTag(XMLStreamReader r, HtmlTag...expected) throws XMLStreamException {
		int et = r.next();

		while (et != START_ELEMENT && et != END_ELEMENT && et != END_DOCUMENT)
			et = r.next();

		if (et == END_DOCUMENT)
			throw new XMLStreamException("Unexpected end of document: " + r.getLocation());

		HtmlTag tag = HtmlTag.forEvent(r);
		if (expected.length == 0)
			return tag;
		for (HtmlTag t : expected)
			if (t == tag)
				return tag;

		throw new XMLStreamException("Unexpected tag: " + tag + ".  Expected one of the following: " + JsonSerializer.DEFAULT.toString(expected), r.getLocation());
	}

	/**
	 * Skips over the current element and advances to the next element.
	 * <p>
	 * Precondition:  Pointing to opening tag.
	 * Postcondition:  Pointing to next opening tag.
	 *
	 * @param r The stream being read from.
	 * @throws XMLStreamException
	 */
	private static void skipTag(XMLStreamReader r) throws XMLStreamException {
		int et = r.getEventType();

		if (et != START_ELEMENT)
			throw new XMLStreamException("skipToNextTag() call on invalid event ["+XmlUtils.toReadableEvent(r)+"].  Must only be called on START_ELEMENT events.");

		String n = r.getLocalName();

		int depth = 0;
		while (true) {
			et = r.next();
			if (et == START_ELEMENT) {
				String n2 = r.getLocalName();
					if (n.equals(n2))
				depth++;
			} else if (et == END_ELEMENT) {
				String n2 = r.getLocalName();
				if (n.equals(n2))
					depth--;
				if (depth < 0)
					return;
			}
		}
	}

	private static void skipTag(XMLStreamReader r, HtmlTag...expected) throws XMLStreamException {
		HtmlTag tag = HtmlTag.forEvent(r);
		if (tag.isOneOf(expected))
			r.next();
		else
			throw new XMLStreamException("Unexpected tag: " + tag + ".  Expected one of the following: " + JsonSerializer.DEFAULT.toString(expected), r.getLocation());
	}

	private static int skipWs(XMLStreamReader r)  throws XMLStreamException {
		int event = r.getEventType();
		while (event != START_ELEMENT && event != END_ELEMENT && event != END_DOCUMENT && r.isWhiteSpace())
			event = r.next();
		return event;
	}


	//--------------------------------------------------------------------------------
	// Entry point methods
	//--------------------------------------------------------------------------------

	@Override /* Parser */
	public HtmlParserSession createSession(Object input, ObjectMap op, Method javaMethod, Object outer, Locale locale, TimeZone timeZone, MediaType mediaType) {
		return new HtmlParserSession(ctx, op, input, javaMethod, outer, locale, timeZone, mediaType);
	}

	@Override /* Parser */
	protected <T> T doParse(ParserSession session, ClassMeta<T> type) throws Exception {
		HtmlParserSession s = (HtmlParserSession)session;
		return parseAnything(s, type, s.getXmlStreamReader(), s.getOuter(), true, null);
	}

	@Override /* ReaderParser */
	protected <K,V> Map<K,V> doParseIntoMap(ParserSession session, Map<K,V> m, Type keyType, Type valueType) throws Exception {
		HtmlParserSession s = (HtmlParserSession)session;
		return parseIntoMap(s, s.getXmlStreamReader(), m, (ClassMeta<K>)s.getClassMeta(keyType), (ClassMeta<V>)s.getClassMeta(valueType), null);
	}

	@Override /* ReaderParser */
	protected <E> Collection<E> doParseIntoCollection(ParserSession session, Collection<E> c, Type elementType) throws Exception {
		HtmlParserSession s = (HtmlParserSession)session;
		return parseIntoCollection(s, s.getXmlStreamReader(), c, (ClassMeta<E>)s.getClassMeta(elementType), null);
	}

	@Override /* ReaderParser */
	protected Object[] doParseArgs(ParserSession session, ClassMeta<?>[] argTypes) throws Exception {
		HtmlParserSession s = (HtmlParserSession)session;
		return parseArgs(s, s.getXmlStreamReader(), argTypes);
	}
}
