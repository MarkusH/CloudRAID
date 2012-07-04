/*
 * Copyright 2011 - 2012 by the CloudRAID Team
 * see AUTHORS for more details
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package de.dhbw_mannheim.cloudraid.api.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import de.dhbw_mannheim.cloudraid.api.impl.responses.IRestApiResponse;

/**
 * @author Markus Holtermann
 * 
 */
public class RestApiUrlMapping {

	/**
	 * @author Markus Holtermann
	 * 
	 */
	public class MatchResult {
		/**
		 * 
		 */
		private ArrayList<String> args = null;
		/**
		 * 
		 */
		private Method function;

		/**
		 * @param function
		 *            The function to call
		 * @param args
		 *            The arguments to the function
		 */
		public MatchResult(Method function, ArrayList<String> args) {
			this.function = function;
			this.args = args;
		}

		/**
		 * @return The arguments to pass to the function;
		 */
		public ArrayList<String> getArgs() {
			return this.args;
		}

		/**
		 * @return The function object
		 */
		public Method getFunction() {
			return this.function;
		}

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.function.getName());
			sb.append("(");
			if (this.args.size() == 0) {
			} else {
				sb.append("\n");
				for (String s : this.args) {
					sb.append(s + "\n,");
				}
			}
			sb.append(")");
			return sb.toString();
		}
	}

	/**
	 * @param klass
	 *            Class used for lookup
	 * @param function
	 *            The function name
	 * @return Return the Method object described by <code>klass</code> and
	 *         <code>function</code>.
	 * @throws NoSuchMethodException
	 *             If a matching method is not found or if the name is
	 *             "<init>"or "<clinit>".
	 * @throws SecurityException
	 *             If a security manager, s, is present and any of the following
	 *             conditions is met:
	 *             <ul>
	 *             <li>invocation of s.checkMemberAccess(this, Member.PUBLIC)
	 *             denies access to the method</li>
	 *             <li>the caller's class loader is not the same as or an
	 *             ancestor of the class loader for the current class and
	 *             invocation of s.checkPackageAccess() denies access to the
	 *             package of this class</li>
	 *             </ul>
	 * 
	 * @see Class#getMethod(String, Class...)
	 */
	public static Method findFunction(Class<?> klass, String function)
			throws NoSuchMethodException, SecurityException {
		return klass.getMethod(function, HttpServletRequest.class,
				IRestApiResponse.class, ArrayList.class);
	}

	/**
	 * 
	 */
	private Method function = null;

	/**
	 * 
	 */
	private String method = null;

	/**
	 * 
	 */
	private Pattern pattern = null;

	/**
	 * @param pattern
	 *            The pattern to match the URL
	 * @param klass
	 *            The class used for lookup
	 * @param function
	 *            The function name to call on the given class
	 * @throws IllegalArgumentException
	 *             Thrown if the pattern or the function is <code>null</code>
	 * @throws NoSuchMethodException
	 *             If a matching method is not found or if the name is
	 *             "<init>"or "<clinit>".
	 * @throws SecurityException
	 *             If a security manager, s, is present and any of the following
	 *             conditions is met:
	 *             <ul>
	 *             <li>invocation of s.checkMemberAccess(this, Member.PUBLIC)
	 *             denies access to the method</li>
	 *             <li>the caller's class loader is not the same as or an
	 *             ancestor of the class loader for the current class and
	 *             invocation of s.checkPackageAccess() denies access to the
	 *             package of this class</li>
	 *             </ul>
	 * 
	 * @see RestApiUrlMapping#RestApiUrlMapping(Pattern, String, Method)
	 * @see RestApiUrlMapping#findFunction(Class, String)
	 * @see Class#getMethod(String, Class...)
	 */
	public RestApiUrlMapping(Pattern pattern, Class<?> klass, String function)
			throws IllegalArgumentException, NoSuchMethodException,
			SecurityException {
		this(pattern, null, RestApiUrlMapping.findFunction(klass, function));
	}

	/**
	 * @param pattern
	 *            The pattern to match the URL
	 * @param function
	 *            The function to call
	 * @throws IllegalArgumentException
	 *             Thrown if the pattern or the function is <code>null</code>
	 * 
	 * @see RestApiUrlMapping#RestApiUrlMapping(Pattern, String, Method)
	 */
	public RestApiUrlMapping(Pattern pattern, Method function)
			throws IllegalArgumentException {
		this(pattern, null, function);
	}

	/**
	 * @param pattern
	 *            The pattern to match the URL
	 * @param method
	 *            The HTTP method (<code>GET</code>, <code>POST</code>,
	 *            <code>PUT</code>, ...)
	 * @param klass
	 *            The class used for lookup
	 * @param function
	 *            The function name to call on the given class
	 * @throws IllegalArgumentException
	 *             Thrown if the pattern or the function is <code>null</code>
	 * @throws NoSuchMethodException
	 *             If a matching method is not found or if the name is
	 *             "<init>"or "<clinit>".
	 * @throws SecurityException
	 *             If a security manager, s, is present and any of the following
	 *             conditions is met:
	 *             <ul>
	 *             <li>invocation of s.checkMemberAccess(this, Member.PUBLIC)
	 *             denies access to the method</li>
	 *             <li>the caller's class loader is not the same as or an
	 *             ancestor of the class loader for the current class and
	 *             invocation of s.checkPackageAccess() denies access to the
	 *             package of this class</li>
	 *             </ul>
	 * 
	 * @see RestApiUrlMapping#RestApiUrlMapping(Pattern, String, Method)
	 * @see RestApiUrlMapping#findFunction(Class, String)
	 * @see Class#getMethod(String, Class...)
	 */
	public RestApiUrlMapping(Pattern pattern, String method, Class<?> klass,
			String function) throws IllegalArgumentException,
			NoSuchMethodException, SecurityException {
		this(pattern, method, RestApiUrlMapping.findFunction(klass, function));
	}

	/**
	 * @param pattern
	 *            The pattern to match the URL
	 * @param method
	 *            The HTTP method (<code>GET</code>, <code>POST</code>,
	 *            <code>PUT</code>, ...)
	 * @param function
	 *            The function to call
	 * @throws IllegalArgumentException
	 *             Thrown if the pattern or the function is <code>null</code>
	 * 
	 * @see RestApiUrlMapping#RestApiUrlMapping(Pattern, String, Method)
	 */
	public RestApiUrlMapping(Pattern pattern, String method, Method function)
			throws IllegalArgumentException {
		if (null == pattern) {
			throw new IllegalArgumentException("Pattern must not be null!");
		}
		if (null == function) {
			throw new IllegalArgumentException("Function must not be null!");
		}
		this.pattern = pattern;
		this.method = method;
		this.function = function;
	}

	/**
	 * @param pattern
	 *            The pattern to match the URL
	 * @param klass
	 *            The class used for lookup
	 * @param function
	 *            The function name to call on the given class
	 * @throws IllegalArgumentException
	 *             Thrown if the pattern or the function is <code>null</code>
	 * @throws NoSuchMethodException
	 *             If a matching method is not found or if the name is
	 *             "<init>"or "<clinit>".
	 * @throws SecurityException
	 *             If a security manager, s, is present and any of the following
	 *             conditions is met:
	 *             <ul>
	 *             <li>invocation of s.checkMemberAccess(this, Member.PUBLIC)
	 *             denies access to the method</li>
	 *             <li>the caller's class loader is not the same as or an
	 *             ancestor of the class loader for the current class and
	 *             invocation of s.checkPackageAccess() denies access to the
	 *             package of this class</li>
	 *             </ul>
	 * 
	 * @see RestApiUrlMapping#RestApiUrlMapping(Pattern, String, Method)
	 * @see RestApiUrlMapping#findFunction(Class, String)
	 * @see Class#getMethod(String, Class...)
	 */
	public RestApiUrlMapping(String pattern, Class<?> klass, String function)
			throws IllegalArgumentException, NoSuchMethodException,
			SecurityException {
		this(Pattern.compile(pattern), null, RestApiUrlMapping.findFunction(
				klass, function));
	}

	/**
	 * @param pattern
	 *            The pattern to match the URL
	 * @param function
	 *            The function to call
	 * @throws IllegalArgumentException
	 *             Thrown if the pattern or the function is <code>null</code>
	 * 
	 * @see RestApiUrlMapping#RestApiUrlMapping(Pattern, String, Method)
	 */
	public RestApiUrlMapping(String pattern, Method function)
			throws IllegalArgumentException {
		this(Pattern.compile(pattern), null, function);
	}

	/**
	 * @param pattern
	 *            The pattern to match the URL
	 * @param method
	 *            The HTTP method (<code>GET</code>, <code>POST</code>,
	 *            <code>PUT</code>, ...)
	 * @param klass
	 *            The class used for lookup
	 * @param function
	 *            The function name to call on the given class
	 * @throws IllegalArgumentException
	 *             Thrown if the pattern or the function is <code>null</code>
	 * @throws NoSuchMethodException
	 *             If a matching method is not found or if the name is
	 *             "<init>"or "<clinit>".
	 * @throws SecurityException
	 *             If a security manager, s, is present and any of the following
	 *             conditions is met:
	 *             <ul>
	 *             <li>invocation of s.checkMemberAccess(this, Member.PUBLIC)
	 *             denies access to the method</li>
	 *             <li>the caller's class loader is not the same as or an
	 *             ancestor of the class loader for the current class and
	 *             invocation of s.checkPackageAccess() denies access to the
	 *             package of this class</li>
	 *             </ul>
	 * 
	 * @see RestApiUrlMapping#RestApiUrlMapping(Pattern, String, Method)
	 * @see RestApiUrlMapping#findFunction(Class, String)
	 * @see Class#getMethod(String, Class...)
	 */
	public RestApiUrlMapping(String pattern, String method, Class<?> klass,
			String function) throws IllegalArgumentException,
			NoSuchMethodException, SecurityException {
		this(Pattern.compile(pattern), method, RestApiUrlMapping.findFunction(
				klass, function));
	}

	/**
	 * @param pattern
	 *            The pattern to match the URL
	 * @param method
	 *            The HTTP method (<code>GET</code>, <code>POST</code>,
	 *            <code>PUT</code>, ...)
	 * @param function
	 *            The function to call
	 * @throws IllegalArgumentException
	 *             Thrown if the pattern or the function is <code>null</code>
	 * 
	 * @see RestApiUrlMapping#RestApiUrlMapping(Pattern, String, Method)
	 */
	public RestApiUrlMapping(String pattern, String method, Method function)
			throws IllegalArgumentException {
		this(Pattern.compile(pattern), method, function);
	}

	/**
	 * Matches this {@link RestApiUrlMapping mapping} against a
	 * {@link HttpServletRequest}
	 * 
	 * @param request
	 *            The request to match against
	 * @return returns the {@link MatchResult} on match else null
	 */
	public MatchResult match(HttpServletRequest request) {
		String pathInfo = request.getPathInfo().substring(
				request.getServletPath().length());
		return this.match(pathInfo, request.getMethod());
	}

	/**
	 * Matches this {@link RestApiUrlMapping mapping} against a the given path
	 * 
	 * @param pathInfo
	 *            The path to match against
	 * @return returns the {@link MatchResult} on match else null
	 */
	public MatchResult match(String pathInfo) {
		return this.match(pathInfo, null);
	}

	/**
	 * Matches this {@link RestApiUrlMapping mapping} against a the given path
	 * and the HTTP Method. If the method is null it is ignored for matching.
	 * 
	 * @param pathInfo
	 *            The path to match against
	 * @param method
	 *            The HTTP method (<code>GET</code>, <code>POST</code>,
	 *            <code>PUT</code>, ...)
	 * @return returns the {@link MatchResult} on match else null
	 */
	public MatchResult match(String pathInfo, String method) {
		if (null == this.method || this.method.equalsIgnoreCase(method)) {
			Matcher m = this.pattern.matcher(pathInfo);
			if (m.matches()) {
				ArrayList<String> args = new ArrayList<String>();
				int i;
				for (i = 1; i <= m.groupCount(); i++) {
					args.add(m.group(i));
				}
				return new MatchResult(this.function, args);
			}
		}
		return null;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.pattern.pattern());
		if (null != this.method) {
			sb.append(" (" + this.method + ")");
		} else {
			sb.append(" (*)");
		}
		sb.append(" --> ");
		sb.append(this.function.getName());
		return sb.toString();
	}
}
