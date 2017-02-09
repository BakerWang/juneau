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
package org.apache.juneau.urlencoding;

import static org.apache.juneau.urlencoding.UonSerializerContext.*;

import java.lang.reflect.*;
import java.util.*;

import org.apache.juneau.*;
import org.apache.juneau.annotation.*;
import org.apache.juneau.serializer.*;
import org.apache.juneau.transform.*;

/**
 * Serializes POJO models to UON (a notation for URL-encoded query parameter values).
 *
 * <h5 class='section'>Media types:</h5>
 * <p>
 * 	Handles <code>Accept</code> types: <code>text/uon</code>
 * <p>
 * 	Produces <code>Content-Type</code> types: <code>text/uon</code>
 *
 * <h5 class='section'>Description:</h5>
 * <p>
 * 	This serializer provides several serialization options.  Typically, one of the predefined DEFAULT serializers will be sufficient.
 * 	However, custom serializers can be constructed to fine-tune behavior.
 *
 * <h5 class='section'>Configurable properties:</h5>
 * <p>
 * 	This class has the following properties associated with it:
 * <ul>
 * 	<li>{@link UonSerializerContext}
 * 	<li>{@link BeanContext}
 * </ul>
 * <p>
 * 	The following shows a sample object defined in Javascript:
 * </p>
 * <p class='bcode'>
 * 	{
 * 		id: 1,
 * 		name: <js>'John Smith'</js>,
 * 		uri: <js>'http://sample/addressBook/person/1'</js>,
 * 		addressBookUri: <js>'http://sample/addressBook'</js>,
 * 		birthDate: <js>'1946-08-12T00:00:00Z'</js>,
 * 		otherIds: <jk>null</jk>,
 * 		addresses: [
 * 			{
 * 				uri: <js>'http://sample/addressBook/address/1'</js>,
 * 				personUri: <js>'http://sample/addressBook/person/1'</js>,
 * 				id: 1,
 * 				street: <js>'100 Main Street'</js>,
 * 				city: <js>'Anywhereville'</js>,
 * 				state: <js>'NY'</js>,
 * 				zip: 12345,
 * 				isCurrent: <jk>true</jk>,
 * 			}
 * 		]
 * 	}
 * </p>
 * <p>
 * 	Using the "strict" syntax defined in this document, the equivalent
 * 		UON notation would be as follows:
 * </p>
 * <p class='bcode'>
 * 	$o(
 * 		<xa>id</xa>=$n(<xs>1</xs>),
 * 		<xa>name</xa>=<xs>John+Smith</xs>,
 * 		<xa>uri</xa>=<xs>http://sample/addressBook/person/1</xs>,
 * 		<xa>addressBookUri</xa>=<xs>http://sample/addressBook</xs>,
 * 		<xa>birthDate</xa>=<xs>1946-08-12T00:00:00Z</xs>,
 * 		<xa>otherIds</xa>=<xs>%00</xs>,
 * 		<xa>addresses</xa>=$a(
 * 			$o(
 * 				<xa>uri</xa>=<xs>http://sample/addressBook/address/1</xs>,
 * 				<xa>personUri</xa>=<xs>http://sample/addressBook/person/1</xs>,
 * 				<xa>id</xa>=$n(<xs>1</xs>),
 * 				<xa>street</xa>=<xs>100+Main+Street</xs>,
 * 				<xa>city</xa>=<xs>Anywhereville</xs>,
 * 				<xa>state</xa>=<xs>NY</xs>,
 * 				<xa>zip</xa>=$n(<xs>12345</xs>),
 * 				<xa>isCurrent</xa>=$b(<xs>true</xs>)
 * 			)
 * 		)
 * 	)
 * </p>
 * <p>
 * 	A secondary "lax" syntax is available when the data type of the
 * 		values are already known on the receiving end of the transmission:
 * </p>
 * <p class='bcode'>
 * 	(
 * 		<xa>id</xa>=<xs>1</xs>,
 * 		<xa>name</xa>=<xs>John+Smith</xs>,
 * 		<xa>uri</xa>=<xs>http://sample/addressBook/person/1</xs>,
 * 		<xa>addressBookUri</xa>=<xs>http://sample/addressBook</xs>,
 * 		<xa>birthDate</xa>=<xs>1946-08-12T00:00:00Z</xs>,
 * 		<xa>otherIds</xa>=<xs>%00</xs>,
 * 		<xa>addresses</xa>=(
 * 			(
 * 				<xa>uri</xa>=<xs>http://sample/addressBook/address/1</xs>,
 * 				<xa>personUri</xa>=<xs>http://sample/addressBook/person/1</xs>,
 * 				<xa>id</xa>=<xs>1</xs>,
 * 				<xa>street</xa>=<xs>100+Main+Street</xs>,
 * 				<xa>city</xa>=<xs>Anywhereville</xs>,
 * 				<xa>state</xa>=<xs>NY</xs>,
 * 				<xa>zip</xa>=<xs>12345</xs>,
 * 				<xa>isCurrent</xa>=<xs>true</xs>
 * 			)
 * 		)
 * 	)
 * </p>
 *
 * <h5 class='section'>Example:</h5>
 * <p class='bcode'>
 * 	<jc>// Serialize a Map</jc>
 * 	Map m = <jk>new</jk> ObjectMap(<js>"{a:'b',c:1,d:false,e:['f',1,false],g:{h:'i'}}"</js>);
 *
 * 	<jc>// Serialize to value equivalent to JSON.</jc>
 * 	<jc>// Produces "$o(a=b,c=$n(1),d=$b(false),e=$a(f,$n(1),$b(false)),g=$o(h=i))"</jc>
 * 	String s = UonSerializer.<jsf>DEFAULT</jsf>.serialize(s);
 *
 * 	<jc>// Serialize to simplified value (for when data type is already known by receiver).</jc>
 * 	<jc>// Produces "(a=b,c=1,d=false,e=(f,1,false),g=(h=i))"</jc>
 * 	String s = UonSerializer.<jsf>DEFAULT_SIMPLE</jsf>.serialize(s);
 *
 * 	<jc>// Serialize a bean</jc>
 * 	<jk>public class</jk> Person {
 * 		<jk>public</jk> Person(String s);
 * 		<jk>public</jk> String getName();
 * 		<jk>public int</jk> getAge();
 * 		<jk>public</jk> Address getAddress();
 * 		<jk>public boolean</jk> deceased;
 * 	}
 *
 * 	<jk>public class</jk> Address {
 * 		<jk>public</jk> String getStreet();
 * 		<jk>public</jk> String getCity();
 * 		<jk>public</jk> String getState();
 * 		<jk>public int</jk> getZip();
 * 	}
 *
 * 	Person p = <jk>new</jk> Person(<js>"John Doe"</js>, 23, <js>"123 Main St"</js>, <js>"Anywhere"</js>, <js>"NY"</js>, 12345, <jk>false</jk>);
 *
 * 	<jc>// Produces "$o(name=John Doe,age=23,address=$o(street=123 Main St,city=Anywhere,state=NY,zip=$n(12345)),deceased=$b(false))"</jc>
 * 	String s = UonSerializer.<jsf>DEFAULT</jsf>.serialize(s);
 *
 * 	<jc>// Produces "(name=John Doe,age=23,address=(street=123 Main St,city=Anywhere,state=NY,zip=12345),deceased=false)"</jc>
 * 	String s = UonSerializer.<jsf>DEFAULT_SIMPLE</jsf>.serialize(s);
 * </p>
 */
@Produces("text/uon")
public class UonSerializer extends WriterSerializer {

	/** Reusable instance of {@link UonSerializer}, all default settings. */
	public static final UonSerializer DEFAULT = new UonSerializer().lock();

	/** Reusable instance of {@link UonSerializer.Simple}. */
	public static final UonSerializer DEFAULT_SIMPLE = new Simple().lock();

	/** Reusable instance of {@link UonSerializer.Readable}. */
	public static final UonSerializer DEFAULT_READABLE = new Readable().lock();

	/** Reusable instance of {@link UonSerializer.Encoding}. */
	public static final UonSerializer DEFAULT_ENCODING = new Encoding().lock();

	/** Reusable instance of {@link UonSerializer.SimpleEncoding}. */
	public static final UonSerializer DEFAULT_SIMPLE_ENCODING = new SimpleEncoding().lock();

	/**
	 * Equivalent to <code><jk>new</jk> UonSerializer().setSimpleMode(<jk>true</jk>);</code>.
	 */
	@Produces(value="text/uon-simple",contentType="text/uon")
	public static class Simple extends UonSerializer {
		/** Constructor */
		public Simple() {
			setSimpleMode(true);
		}
	}

	/**
	 * Equivalent to <code><jk>new</jk> UonSerializer().setUseWhitespace(<jk>true</jk>).setUseIndentation(<jk>true</jk>);</code>.
	 */
	public static class Readable extends UonSerializer {
		/** Constructor */
		public Readable() {
			setUseWhitespace(true);
			setUseIndentation(true);
		}
	}

	/**
	 * Equivalent to <code><jk>new</jk> UonSerializer().setEncodeChars(<jk>true</jk>);</code>.
	 */
	public static class Encoding extends UonSerializer {
		/** Constructor */
		public Encoding() {
			setEncodeChars(true);
		}
	}

	/**
	 * Equivalent to <code><jk>new</jk> UonSerializer().setSimpleMode(<jk>true</jk>).setEncodeChars(<jk>true</jk>);</code>.
	 */
	@Produces(value="text/uon-simple",contentType="text/uon")
	public static class SimpleEncoding extends UonSerializer {
		/** Constructor */
		public SimpleEncoding() {
			setSimpleMode(true);
			setEncodeChars(true);
		}
	}


	/**
	 * Workhorse method. Determines the type of object, and then calls the
	 * appropriate type-specific serialization method.
	 * @param session The context that exist for the duration of a serialize.
	 * @param out The writer to serialize to.
	 * @param o The object being serialized.
	 * @param eType The expected type of the object if this is a bean property.
	 * @param attrName The bean property name if this is a bean property.  <jk>null</jk> if this isn't a bean property being serialized.
	 * @param pMeta The bean property metadata.
	 * @param quoteEmptyStrings <jk>true</jk> if this is the first entry in an array.
	 * @param isTop If we haven't recursively called this method.
	 *
	 * @return The same writer passed in.
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected SerializerWriter serializeAnything(UonSerializerSession session, UonWriter out, Object o, ClassMeta<?> eType,
			String attrName, BeanPropertyMeta pMeta, boolean quoteEmptyStrings, boolean isTop) throws Exception {

		if (o == null) {
			out.appendObject(null, false, false, isTop);
			return out;
		}

		if (eType == null)
			eType = object();

		boolean addTypeProperty;		// Add "_type" attribute to element?
		ClassMeta<?> aType;			// The actual type
		ClassMeta<?> sType;			// The serialized type

		aType = session.push(attrName, o, eType);
		boolean isRecursion = aType == null;

		// Handle recursion
		if (aType == null) {
			o = null;
			aType = object();
		}

		sType = aType.getSerializedClassMeta();
		addTypeProperty = (session.isAddBeanTypeProperties() && ! eType.equals(aType));

		// Swap if necessary
		PojoSwap swap = aType.getPojoSwap();
		if (swap != null) {
			o = swap.swap(session, o);

			// If the getSwapClass() method returns Object, we need to figure out
			// the actual type now.
			if (sType.isObject())
				sType = session.getClassMetaForObject(o);
		}

		// '\0' characters are considered null.
		if (o == null || (sType.isChar() && ((Character)o).charValue() == 0))
			out.appendObject(null, false, false, isTop);
		else if (sType.isBean())
			serializeBeanMap(session, out, session.toBeanMap(o), addTypeProperty);
		else if (sType.isUri() || (pMeta != null && pMeta.isUri()))
			out.appendUri(o, isTop);
		else if (sType.isMap()) {
			if (o instanceof BeanMap)
				serializeBeanMap(session, out, (BeanMap)o, addTypeProperty);
			else
				serializeMap(session, out, (Map)o, eType);
		}
		else if (sType.isCollection()) {
			serializeCollection(session, out, (Collection) o, eType);
		}
		else if (sType.isArray()) {
			serializeCollection(session, out, toList(sType.getInnerClass(), o), eType);
		}
		else {
			out.appendObject(o, quoteEmptyStrings, false, isTop);
		}

		if (! isRecursion)
			session.pop();
		return out;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SerializerWriter serializeMap(UonSerializerSession session, UonWriter out, Map m, ClassMeta<?> type) throws Exception {

		m = session.sort(m);

		ClassMeta<?> keyType = type.getKeyType(), valueType = type.getValueType();

		int depth = session.getIndent();
		out.startFlag('o');

		Iterator mapEntries = m.entrySet().iterator();

		while (mapEntries.hasNext()) {
			Map.Entry e = (Map.Entry) mapEntries.next();
			Object value = e.getValue();
			Object key = session.generalize(e.getKey(), keyType);
			out.cr(depth).appendObject(key, session.isUseWhitespace(), false, false).append('=');
			serializeAnything(session, out, value, valueType, (key == null ? null : session.toString(key)), null, session.isUseWhitespace(), false);
			if (mapEntries.hasNext())
				out.append(',');
		}

		if (m.size() > 0)
			out.cr(depth-1);
		out.append(')');

		return out;
	}

	private SerializerWriter serializeBeanMap(UonSerializerSession session, UonWriter out, BeanMap<?> m, boolean addTypeProperty) throws Exception {
		int depth = session.getIndent();

		out.startFlag('o');

		boolean addComma = false;

		for (BeanPropertyValue p : m.getValues(session.isTrimNulls(), addTypeProperty ? session.createBeanTypeNameProperty(m) : null)) {
			BeanPropertyMeta pMeta = p.getMeta();
			ClassMeta<?> cMeta = p.getClassMeta();

			String key = p.getName();
			Object value = p.getValue();
			Throwable t = p.getThrown();
			if (t != null)
				session.addBeanGetterWarning(pMeta, t);

			if (session.canIgnoreValue(cMeta, key, value))
				continue;

			if (addComma)
				out.append(',');

			out.cr(depth).appendObject(key, false, false, false).append('=');

			serializeAnything(session, out, value, cMeta, key, pMeta, false, false);

			addComma = true;
		}

		if (m.size() > 0)
			out.cr(depth-1);
		out.append(')');

		return out;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private SerializerWriter serializeCollection(UonSerializerSession session, UonWriter out, Collection c, ClassMeta<?> type) throws Exception {

		ClassMeta<?> elementType = type.getElementType();

		c = session.sort(c);

		out.startFlag('a');

		int depth = session.getIndent();
		boolean quoteEmptyString = (c.size() == 1 || session.isUseWhitespace());

		for (Iterator i = c.iterator(); i.hasNext();) {
			out.cr(depth);
			serializeAnything(session, out, i.next(), elementType, "<iterator>", null, quoteEmptyString, false);
			if (i.hasNext())
				out.append(',');
		}

		if (c.size() > 0)
			out.cr(depth-1);
		out.append(')');

		return out;
	}


	//--------------------------------------------------------------------------------
	// Entry point methods
	//--------------------------------------------------------------------------------

	@Override /* Serializer */
	public UonSerializerSession createSession(Object output, ObjectMap op, Method javaMethod, Locale locale, TimeZone timeZone, MediaType mediaType) {
		return new UonSerializerSession(getContext(UonSerializerContext.class), op, output, javaMethod, locale, timeZone, mediaType);
	}

	@Override /* Serializer */
	protected void doSerialize(SerializerSession session, Object o) throws Exception {
		UonSerializerSession s = (UonSerializerSession)session;
		serializeAnything(s, s.getWriter(), o, null, "root", null, false, true);
	}


	//--------------------------------------------------------------------------------
	// Properties
	//--------------------------------------------------------------------------------

	/**
	 * <b>Configuration property:</b>  Use simplified output.
	 * <p>
	 * <ul>
	 * 	<li><b>Name:</b> <js>"UonSerializer.simpleMode"</js>
	 * 	<li><b>Data type:</b> <code>Boolean</code>
	 * 	<li><b>Default:</b> <jk>false</jk>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 * <p>
	 * If <jk>true</jk>, type flags will not be prepended to values in most cases.
	 * <p>
	 * Use this setting if the data types of the values (e.g. object/array/boolean/number/string)
	 * 	is known on the receiving end.
	 * <p>
	 * It should be noted that the default behavior produces a data structure that can
	 * 	be losslessly converted into JSON, and any JSON can be losslessly represented
	 * 	in a URL-encoded value.  However, this strict equivalency does not exist
	 * 	when simple mode is used.
	 * <p>
	 * <table class='styled'>
	 * 	<tr>
	 * 		<th>Input (in JSON)</th>
	 * 		<th>Normal mode output</th>
	 * 		<th>Simple mode output</th>
	 * 	</tr>
	 * 	<tr>
	 * 		<td class='code'>{foo:'bar',baz:'bing'}</td>
	 * 		<td class='code'>$o(foo=bar,baz=bing)</td>
	 * 		<td class='code'>(foo=bar,baz=bing)</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td class='code'>{foo:{bar:'baz'}}</td>
	 * 		<td class='code'>$o(foo=$o(bar=baz))</td>
	 * 		<td class='code'>(foo=(bar=baz))</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td class='code'>['foo','bar']</td>
	 * 		<td class='code'>$a(foo,bar)</td>
	 * 		<td class='code'>(foo,bar)</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td class='code'>['foo',['bar','baz']]</td>
	 * 		<td class='code'>$a(foo,$a(bar,baz))</td>
	 * 		<td class='code'>(foo,(bar,baz))</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td class='code'>true</td>
	 * 		<td class='code'>$b(true)</td>
	 * 		<td class='code'>true</td>
	 * 	</tr>
	 * 	<tr>
	 * 		<td class='code'>123</td>
	 * 		<td class='code'>$n(123)</td>
	 * 		<td class='code'>123</td>
	 * 	</tr>
	 * </table>
	 * <p>
	 * <h5 class='section'>Notes:</h5>
	 * <ul>
	 * 	<li>This is equivalent to calling <code>setProperty(<jsf>UON_simpleMode</jsf>, value)</code>.
	 * 	<li>This introduces a slight performance penalty.
	 * </ul>
	 *
	 * @param value The new value for this property.
	 * @return This object (for method chaining).
	 * @throws LockedException If {@link #lock()} was called on this class.
	 * @see UonSerializerContext#UON_simpleMode
	 */
	public UonSerializer setSimpleMode(boolean value) throws LockedException {
		return setProperty(UON_simpleMode, value);
	}

	/**
	 * <b>Configuration property:</b>  Use whitespace.
	 * <p>
	 * <ul>
	 * 	<li><b>Name:</b> <js>"UonSerializer.useWhitespace"</js>
	 * 	<li><b>Data type:</b> <code>Boolean</code>
	 * 	<li><b>Default:</b> <jk>false</jk>
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 * <p>
	 * If <jk>true</jk>, whitespace is added to the output to improve readability.
	 * <p>
	 * <h5 class='section'>Notes:</h5>
	 * <ul>
	 * 	<li>This is equivalent to calling <code>setProperty(<jsf>UON_useWhitespace</jsf>, value)</code>.
	 * 	<li>This introduces a slight performance penalty.
	 * </ul>
	 *
	 * @param value The new value for this property.
	 * @return This object (for method chaining).
	 * @throws LockedException If {@link #lock()} was called on this class.
	 * @see UonSerializerContext#UON_useWhitespace
	 */
	public UonSerializer setUseWhitespace(boolean value) throws LockedException {
		return setProperty(UON_useWhitespace, value);
	}

	/**
	 * <b>Configuration property:</b>  Encode non-valid URI characters.
	 * <p>
	 * <ul>
	 * 	<li><b>Name:</b> <js>"UonSerializer.encodeChars"</js>
	 * 	<li><b>Data type:</b> <code>Boolean</code>
	 * 	<li><b>Default:</b> <jk>false</jk> for {@link UonSerializer}, <jk>true</jk> for {@link UrlEncodingSerializer}
	 * 	<li><b>Session-overridable:</b> <jk>true</jk>
	 * </ul>
	 * <p>
	 * Encode non-valid URI characters with <js>"%xx"</js> constructs.
	 * <p>
	 * If <jk>true</jk>, non-valid URI characters will be converted to <js>"%xx"</js> sequences.
	 * Set to <jk>false</jk> if parameter value is being passed to some other code that will already
	 * 	perform URL-encoding of non-valid URI characters.
	 * <p>
	 * <h5 class='section'>Notes:</h5>
	 * <ul>
	 * 	<li>This is equivalent to calling <code>setProperty(<jsf>UON_encodeChars</jsf>, value)</code>.
	 * 	<li>This introduces a slight performance penalty.
	 * </ul>
	 *
	 * @param value The new value for this property.
	 * @return This object (for method chaining).
	 * @throws LockedException If {@link #lock()} was called on this class.
	 * @see UonSerializerContext#UON_encodeChars
	 */
	public UonSerializer setEncodeChars(boolean value) throws LockedException {
		return setProperty(UON_encodeChars, value);
	}

	@Override /* Serializer */
	public UonSerializer setMaxDepth(int value) throws LockedException {
		super.setMaxDepth(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setInitialDepth(int value) throws LockedException {
		super.setInitialDepth(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setDetectRecursions(boolean value) throws LockedException {
		super.setDetectRecursions(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setIgnoreRecursions(boolean value) throws LockedException {
		super.setIgnoreRecursions(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setUseIndentation(boolean value) throws LockedException {
		super.setUseIndentation(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setAddBeanTypeProperties(boolean value) throws LockedException {
		super.setAddBeanTypeProperties(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setQuoteChar(char value) throws LockedException {
		super.setQuoteChar(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setTrimNullProperties(boolean value) throws LockedException {
		super.setTrimNullProperties(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setTrimEmptyCollections(boolean value) throws LockedException {
		super.setTrimEmptyCollections(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setTrimEmptyMaps(boolean value) throws LockedException {
		super.setTrimEmptyMaps(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setTrimStrings(boolean value) throws LockedException {
		super.setTrimStrings(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setRelativeUriBase(String value) throws LockedException {
		super.setRelativeUriBase(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setAbsolutePathUriBase(String value) throws LockedException {
		super.setAbsolutePathUriBase(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setSortCollections(boolean value) throws LockedException {
		super.setSortCollections(value);
		return this;
	}

	@Override /* Serializer */
	public UonSerializer setSortMaps(boolean value) throws LockedException {
		super.setSortMaps(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeansRequireDefaultConstructor(boolean value) throws LockedException {
		super.setBeansRequireDefaultConstructor(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeansRequireSerializable(boolean value) throws LockedException {
		super.setBeansRequireSerializable(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeansRequireSettersForGetters(boolean value) throws LockedException {
		super.setBeansRequireSettersForGetters(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeansRequireSomeProperties(boolean value) throws LockedException {
		super.setBeansRequireSomeProperties(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanMapPutReturnsOldValue(boolean value) throws LockedException {
		super.setBeanMapPutReturnsOldValue(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanConstructorVisibility(Visibility value) throws LockedException {
		super.setBeanConstructorVisibility(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanClassVisibility(Visibility value) throws LockedException {
		super.setBeanClassVisibility(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanFieldVisibility(Visibility value) throws LockedException {
		super.setBeanFieldVisibility(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setMethodVisibility(Visibility value) throws LockedException {
		super.setMethodVisibility(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setUseJavaBeanIntrospector(boolean value) throws LockedException {
		super.setUseJavaBeanIntrospector(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setUseInterfaceProxies(boolean value) throws LockedException {
		super.setUseInterfaceProxies(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setIgnoreUnknownBeanProperties(boolean value) throws LockedException {
		super.setIgnoreUnknownBeanProperties(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setIgnoreUnknownNullBeanProperties(boolean value) throws LockedException {
		super.setIgnoreUnknownNullBeanProperties(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setIgnorePropertiesWithoutSetters(boolean value) throws LockedException {
		super.setIgnorePropertiesWithoutSetters(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setIgnoreInvocationExceptionsOnGetters(boolean value) throws LockedException {
		super.setIgnoreInvocationExceptionsOnGetters(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setIgnoreInvocationExceptionsOnSetters(boolean value) throws LockedException {
		super.setIgnoreInvocationExceptionsOnSetters(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setSortProperties(boolean value) throws LockedException {
		super.setSortProperties(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setNotBeanPackages(String...values) throws LockedException {
		super.setNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setNotBeanPackages(Collection<String> values) throws LockedException {
		super.setNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addNotBeanPackages(String...values) throws LockedException {
		super.addNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addNotBeanPackages(Collection<String> values) throws LockedException {
		super.addNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeNotBeanPackages(String...values) throws LockedException {
		super.removeNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeNotBeanPackages(Collection<String> values) throws LockedException {
		super.removeNotBeanPackages(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setNotBeanClasses(Class<?>...values) throws LockedException {
		super.setNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setNotBeanClasses(Collection<Class<?>> values) throws LockedException {
		super.setNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addNotBeanClasses(Class<?>...values) throws LockedException {
		super.addNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addNotBeanClasses(Collection<Class<?>> values) throws LockedException {
		super.addNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeNotBeanClasses(Class<?>...values) throws LockedException {
		super.removeNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeNotBeanClasses(Collection<Class<?>> values) throws LockedException {
		super.removeNotBeanClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanFilters(Class<?>...values) throws LockedException {
		super.setBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanFilters(Collection<Class<?>> values) throws LockedException {
		super.setBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addBeanFilters(Class<?>...values) throws LockedException {
		super.addBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addBeanFilters(Collection<Class<?>> values) throws LockedException {
		super.addBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeBeanFilters(Class<?>...values) throws LockedException {
		super.removeBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeBeanFilters(Collection<Class<?>> values) throws LockedException {
		super.removeBeanFilters(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setPojoSwaps(Class<?>...values) throws LockedException {
		super.setPojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setPojoSwaps(Collection<Class<?>> values) throws LockedException {
		super.setPojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addPojoSwaps(Class<?>...values) throws LockedException {
		super.addPojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addPojoSwaps(Collection<Class<?>> values) throws LockedException {
		super.addPojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removePojoSwaps(Class<?>...values) throws LockedException {
		super.removePojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removePojoSwaps(Collection<Class<?>> values) throws LockedException {
		super.removePojoSwaps(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setImplClasses(Map<Class<?>,Class<?>> values) throws LockedException {
		super.setImplClasses(values);
		return this;
	}

	@Override /* CoreApi */
	public <T> CoreApi addImplClass(Class<T> interfaceClass, Class<? extends T> implClass) throws LockedException {
		super.addImplClass(interfaceClass, implClass);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanDictionary(Class<?>...values) throws LockedException {
		super.setBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanDictionary(Collection<Class<?>> values) throws LockedException {
		super.setBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addToBeanDictionary(Class<?>...values) throws LockedException {
		super.addToBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addToBeanDictionary(Collection<Class<?>> values) throws LockedException {
		super.addToBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeFromBeanDictionary(Class<?>...values) throws LockedException {
		super.removeFromBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeFromBeanDictionary(Collection<Class<?>> values) throws LockedException {
		super.removeFromBeanDictionary(values);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setBeanTypePropertyName(String value) throws LockedException {
		super.setBeanTypePropertyName(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setDefaultParser(Class<?> value) throws LockedException {
		super.setDefaultParser(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setLocale(Locale value) throws LockedException {
		super.setLocale(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setTimeZone(TimeZone value) throws LockedException {
		super.setTimeZone(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setMediaType(MediaType value) throws LockedException {
		super.setMediaType(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setDebug(boolean value) throws LockedException {
		super.setDebug(value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setProperty(String name, Object value) throws LockedException {
		super.setProperty(name, value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer setProperties(ObjectMap properties) throws LockedException {
		super.setProperties(properties);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer addToProperty(String name, Object value) throws LockedException {
		super.addToProperty(name, value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer putToProperty(String name, Object key, Object value) throws LockedException {
		super.putToProperty(name, key, value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer putToProperty(String name, Object value) throws LockedException {
		super.putToProperty(name, value);
		return this;
	}

	@Override /* CoreApi */
	public UonSerializer removeFromProperty(String name, Object value) throws LockedException {
		super.removeFromProperty(name, value);
		return this;
	}


	//--------------------------------------------------------------------------------
	// Overridden methods
	//--------------------------------------------------------------------------------

	@Override /* CoreApi */
	public UonSerializer setClassLoader(ClassLoader classLoader) throws LockedException {
		super.setClassLoader(classLoader);
		return this;
	}

	@Override /* Lockable */
	public UonSerializer lock() {
		super.lock();
		return this;
	}

	@Override /* Lockable */
	public UonSerializer clone() {
		try {
			UonSerializer c = (UonSerializer)super.clone();
			return c;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e); // Shouldn't happen
		}
	}
}
