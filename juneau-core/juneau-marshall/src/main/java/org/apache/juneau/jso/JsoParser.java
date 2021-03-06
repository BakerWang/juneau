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
package org.apache.juneau.jso;

import java.io.*;

import org.apache.juneau.*;
import org.apache.juneau.parser.*;

/**
 * Parses POJOs from HTTP responses as Java {@link ObjectInputStream ObjectInputStreams}.
 *
 * <h5 class='section'>Media types:</h5>
 *
 * Consumes <code>Content-Type</code> types: <code>application/x-java-serialized-object</code>
 */
public final class JsoParser extends InputStreamParser {

	//-------------------------------------------------------------------------------------------------------------------
	// Predefined instances
	//-------------------------------------------------------------------------------------------------------------------

	/** Default parser, all default settings.*/
	public static final JsoParser DEFAULT = new JsoParser(PropertyStore.create());


	//-------------------------------------------------------------------------------------------------------------------
	// Instance
	//-------------------------------------------------------------------------------------------------------------------

	/**
	 * Constructor.
	 *
	 * @param propertyStore The property store containing all the settings for this object.
	 */
	public JsoParser(PropertyStore propertyStore) {
		super(propertyStore, "application/x-java-serialized-object");
	}

	@Override /* CoreObject */
	public JsoParserBuilder builder() {
		return new JsoParserBuilder(propertyStore);
	}

	@Override /* Parser */
	public InputStreamParserSession createSession(ParserSessionArgs args) {
		return new JsoParserSession(args);
	}
}
