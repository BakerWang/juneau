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
package org.apache.juneau;

import static org.apache.juneau.Visibility.*;
import static org.junit.Assert.*;

import org.apache.juneau.a.*;
import org.apache.juneau.json.*;
import org.junit.*;

@SuppressWarnings("javadoc")
public class VisibilityTest {

	//====================================================================================================
	// testVisibility
	//====================================================================================================
	@Test
	public void testClassDefault() throws Exception {
		JsonSerializer s1 = JsonSerializer.DEFAULT_LAX.clone().setBeansRequireSomeProperties(false);
		JsonSerializer s2 = JsonSerializer.DEFAULT_LAX.clone().setBeansRequireSomeProperties(false).setBeanClassVisibility(PROTECTED);
		JsonSerializer s3 = JsonSerializer.DEFAULT_LAX.clone().setBeansRequireSomeProperties(false).setBeanClassVisibility(Visibility.DEFAULT);
		JsonSerializer s4 = JsonSerializer.DEFAULT_LAX.clone().setBeansRequireSomeProperties(false).setBeanClassVisibility(PRIVATE);

		A1 a1 = A1.create();
		String r;

		s1.setBeanFieldVisibility(NONE);
		s2.setBeanFieldVisibility(NONE);
		s3.setBeanFieldVisibility(NONE);
		s4.setBeanFieldVisibility(NONE);

		r = s1.serialize(a1);
		assertEquals("{f5:5}", r);

		r = s2.serialize(a1);
		assertEquals("{f5:5}", r);

		r = s3.serialize(a1);
		assertEquals("{f5:5}", r);

		r = s4.serialize(a1);
		assertEquals("{f5:5}", r);

		s1.setBeanFieldVisibility(PUBLIC);
		s2.setBeanFieldVisibility(PUBLIC);
		s3.setBeanFieldVisibility(PUBLIC);
		s4.setBeanFieldVisibility(PUBLIC);

		r = s1.serialize(a1);
		assertEquals("{f1:1,f5:5,g2:{f1:1,f5:5},g3:'A3',g4:'A4',g5:'A5'}", r);

		r = s2.serialize(a1);
		assertEquals("{f1:1,f5:5,g2:{f1:1,f5:5},g3:{f1:1,f5:5},g4:'A4',g5:'A5'}", r);

		r = s3.serialize(a1);
		assertEquals("{f1:1,f5:5,g2:{f1:1,f5:5},g3:{f1:1,f5:5},g4:{f1:1,f5:5},g5:'A5'}", r);

		r = s4.serialize(a1);
		assertEquals("{f1:1,f5:5,g2:{f1:1,f5:5},g3:{f1:1,f5:5},g4:{f1:1,f5:5},g5:{f1:1,f5:5}}", r);

		s1.setBeanFieldVisibility(PROTECTED);
		s2.setBeanFieldVisibility(PROTECTED);
		s3.setBeanFieldVisibility(PROTECTED);
		s4.setBeanFieldVisibility(PROTECTED);

		r = s1.serialize(a1);
		assertEquals("{f1:1,f2:2,f5:5,g2:{f1:1,f2:2,f5:5},g3:'A3',g4:'A4',g5:'A5'}", r);

		r = s2.serialize(a1);
		assertEquals("{f1:1,f2:2,f5:5,g2:{f1:1,f2:2,f5:5},g3:{f1:1,f2:2,f5:5},g4:'A4',g5:'A5'}", r);

		r = s3.serialize(a1);
		assertEquals("{f1:1,f2:2,f5:5,g2:{f1:1,f2:2,f5:5},g3:{f1:1,f2:2,f5:5},g4:{f1:1,f2:2,f5:5},g5:'A5'}", r);

		r = s4.serialize(a1);
		assertEquals("{f1:1,f2:2,f5:5,g2:{f1:1,f2:2,f5:5},g3:{f1:1,f2:2,f5:5},g4:{f1:1,f2:2,f5:5},g5:{f1:1,f2:2,f5:5}}", r);

		s1.setBeanFieldVisibility(Visibility.DEFAULT);
		s2.setBeanFieldVisibility(Visibility.DEFAULT);
		s3.setBeanFieldVisibility(Visibility.DEFAULT);
		s4.setBeanFieldVisibility(Visibility.DEFAULT);

		r = s1.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f5:5,g2:{f1:1,f2:2,f3:3,f5:5},g3:'A3',g4:'A4',g5:'A5'}", r);

		r = s2.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f5:5,g2:{f1:1,f2:2,f3:3,f5:5},g3:{f1:1,f2:2,f3:3,f5:5},g4:'A4',g5:'A5'}", r);

		r = s3.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f5:5,g2:{f1:1,f2:2,f3:3,f5:5},g3:{f1:1,f2:2,f3:3,f5:5},g4:{f1:1,f2:2,f3:3,f5:5},g5:'A5'}", r);

		r = s4.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f5:5,g2:{f1:1,f2:2,f3:3,f5:5},g3:{f1:1,f2:2,f3:3,f5:5},g4:{f1:1,f2:2,f3:3,f5:5},g5:{f1:1,f2:2,f3:3,f5:5}}", r);

		s1.setBeanFieldVisibility(PRIVATE);
		s2.setBeanFieldVisibility(PRIVATE);
		s3.setBeanFieldVisibility(PRIVATE);
		s4.setBeanFieldVisibility(PRIVATE);

		r = s1.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,f5:5,g2:{f1:1,f2:2,f3:3,f4:4,f5:5},g3:'A3',g4:'A4',g5:'A5'}", r);

		r = s2.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,f5:5,g2:{f1:1,f2:2,f3:3,f4:4,f5:5},g3:{f1:1,f2:2,f3:3,f4:4,f5:5},g4:'A4',g5:'A5'}", r);

		r = s3.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,f5:5,g2:{f1:1,f2:2,f3:3,f4:4,f5:5},g3:{f1:1,f2:2,f3:3,f4:4,f5:5},g4:{f1:1,f2:2,f3:3,f4:4,f5:5},g5:'A5'}", r);

		r = s4.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,f5:5,g2:{f1:1,f2:2,f3:3,f4:4,f5:5},g3:{f1:1,f2:2,f3:3,f4:4,f5:5},g4:{f1:1,f2:2,f3:3,f4:4,f5:5},g5:{f1:1,f2:2,f3:3,f4:4,f5:5}}", r);

		s1.setMethodVisibility(NONE);
		s2.setMethodVisibility(NONE);
		s3.setMethodVisibility(NONE);
		s4.setMethodVisibility(NONE);

		r = s1.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,g2:{f1:1,f2:2,f3:3,f4:4},g3:'A3',g4:'A4',g5:'A5'}", r);

		r = s2.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,g2:{f1:1,f2:2,f3:3,f4:4},g3:{f1:1,f2:2,f3:3,f4:4},g4:'A4',g5:'A5'}", r);

		r = s3.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,g2:{f1:1,f2:2,f3:3,f4:4},g3:{f1:1,f2:2,f3:3,f4:4},g4:{f1:1,f2:2,f3:3,f4:4},g5:'A5'}", r);

		r = s4.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,g2:{f1:1,f2:2,f3:3,f4:4},g3:{f1:1,f2:2,f3:3,f4:4},g4:{f1:1,f2:2,f3:3,f4:4},g5:{f1:1,f2:2,f3:3,f4:4}}", r);

		s1.setMethodVisibility(PROTECTED);
		s2.setMethodVisibility(PROTECTED);
		s3.setMethodVisibility(PROTECTED);
		s4.setMethodVisibility(PROTECTED);

		r = s1.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6,g2:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g3:'A3',g4:'A4',g5:'A5'}", r);

		r = s2.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6,g2:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g3:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g4:'A4',g5:'A5'}", r);

		r = s3.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6,g2:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g3:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g4:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g5:'A5'}", r);

		r = s4.serialize(a1);
		assertEquals("{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6,g2:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g3:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g4:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6},g5:{f1:1,f2:2,f3:3,f4:4,f5:5,f6:6}}", r);

	}

	static class A {
		public int f1;
		public A(){}

		static A create() {
			A x = new A();
			x.f1 = 1;
			return x;
		}
	}
}