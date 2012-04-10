/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.el.ELException;
import javax.faces.FacesException;
import javax.faces.context.FacesContext;

import com.sun.facelets.Facelet;
import com.sun.facelets.FaceletException;
import com.sun.facelets.FaceletFactory;
import com.sun.facelets.FaceletHandler;
import com.sun.facelets.compiler.Compiler;
import com.sun.facelets.util.ParameterCheck;
import com.sun.facelets.util.Resource;

/**
 * Default FaceletFactory implementation.
 * 
 * @author Jacob Hookom
 * @version $Id: DefaultFaceletFactory.java,v 1.10 2007/04/09 01:13:17 youngm
 *          Exp $
 */
public final class DefaultFaceletFactory extends FaceletFactory {

	protected final static Logger log = Logger.getLogger("facelets.factory");

	private final Compiler compiler;

	private Map facelets;

	private Map relativeLocations;

	private final ResourceResolver resolver;

	private final URL baseUrl;

	private final long refreshPeriod;

	public DefaultFaceletFactory(Compiler compiler, ResourceResolver resolver)
			throws IOException {
		this(compiler, resolver, -1);
	}

	public DefaultFaceletFactory(Compiler compiler, ResourceResolver resolver,
			long refreshPeriod) {
		ParameterCheck.notNull("compiler", compiler);
		ParameterCheck.notNull("resolver", resolver);
		this.compiler = compiler;
		this.facelets = new HashMap();
		this.relativeLocations = new HashMap();
		this.resolver = resolver;
		this.baseUrl = resolver.resolveUrl("/");
		// this.location = url;
		log.fine("Using ResourceResolver: " + resolver);
		this.refreshPeriod = (refreshPeriod >= 0) ? refreshPeriod * 1000 : -1;
		log.fine("Using Refresh Period: " + this.refreshPeriod);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sun.facelets.FaceletFactory#getFacelet(java.lang.String)
	 */
	public Facelet getFacelet(String uri) throws IOException, FaceletException,
			FacesException, ELException {
		URL url = (URL) this.relativeLocations.get(uri);
		if (url == null) {
			url = this.resolveURL(this.baseUrl, uri);
			if (url != null) {
				Map newLoc = new HashMap(this.relativeLocations);
				newLoc.put(uri, url);
				this.relativeLocations = newLoc;
			} else {
				throw new IOException("'" + uri + "' not found.");
			}
		}
		return this.getFacelet(url);
	}

	/**
	 * Resolves a path based on the passed URL. If the path starts with '/',
	 * then resolve the path against
	 * {@link javax.faces.context.ExternalContext#getResource(java.lang.String) javax.faces.context.ExternalContext#getResource(java.lang.String)}.
	 * Otherwise create a new URL via
	 * {@link URL#URL(java.net.URL, java.lang.String) URL(URL, String)}.
	 * 
	 * @param source
	 *            base to resolve from
	 * @param path
	 *            relative path to the source
	 * @return resolved URL
	 * @throws IOException
	 */
	public URL resolveURL(URL source, String path) throws IOException {
		if (path.startsWith("/")) {
			URL url = this.resolver.resolveUrl(path);
			if (url == null) {
				throw new FileNotFoundException(path
						+ " Not Found in ExternalContext as a Resource");
			}
			return url;
		} else {
			return new URL(source, path);
		}
	}

	/**
	 * Create a Facelet from the passed URL. This method checks if the cached
	 * Facelet needs to be refreshed before returning. If so, uses the passed
	 * URL to build a new instance;
	 * 
	 * @param url
	 *            source url
	 * @return Facelet instance
	 * @throws IOException
	 * @throws FaceletException
	 * @throws FacesException
	 * @throws ELException
	 */
	public Facelet getFacelet(URL url) throws IOException, FaceletException,
			FacesException, ELException {
		ParameterCheck.notNull("url", url);
		String key = url.toString();
		DefaultFacelet f = (DefaultFacelet) this.facelets.get(key);
		if (f == null || this.needsToBeRefreshed(f)) {
			f = this.createFacelet(url);
			if (this.refreshPeriod != 0) {
				Map newLoc = new HashMap(this.facelets);
				newLoc.put(key, f);
				this.facelets = newLoc;
			}
		}
		return f;
	}

	/**
	 * Template method for determining if the Facelet needs to be refreshed.
	 * 
	 * @param facelet
	 *            Facelet that could have expired
	 * @return true if it needs to be refreshed
	 */
	protected boolean needsToBeRefreshed(DefaultFacelet facelet) {
		// if set to 0, constantly reload-- nocache
		if (this.refreshPeriod == 0)
			return true;
		// if set to -1, never reload
		if (this.refreshPeriod == -1)
			return false;
		long ttl = facelet.getCreateTime() + this.refreshPeriod;
		URL url = facelet.getSource();
		InputStream is = null;
		if (System.currentTimeMillis() > ttl) {
			try {
				URLConnection conn = url.openConnection();
				is = conn.getInputStream();
				long atl = conn.getLastModified();
				return atl > ttl;
			} catch (Exception e) {
				throw new FaceletException("Error Checking Last Modified for "
						+ facelet.getAlias(), e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception e) {
						// do nothing
					}
				}
			}
		}
		return false;
	}

	/**
	 * Uses the internal Compiler reference to build a Facelet given the passed
	 * URL.
	 * 
	 * @param url
	 *            source
	 * @return a Facelet instance
	 * @throws IOException
	 * @throws FaceletException
	 * @throws FacesException
	 * @throws ELException
	 */
	private DefaultFacelet createFacelet(URL url) throws IOException,
			FaceletException, FacesException, ELException {
		if (log.isLoggable(Level.FINE)) {
			log.fine("Creating Facelet for: " + url);
		}
		String alias = "/"
				+ url.getFile().replaceFirst(this.baseUrl.getFile(), "");
		try {
			FaceletHandler h = this.compiler.compile(url, alias);
			DefaultFacelet f = new DefaultFacelet(this, this.compiler
					.createExpressionFactory(), url, alias, h);
			return f;
		} catch (FileNotFoundException fnfe) {
			throw new FileNotFoundException("Facelet " + alias + " not found at: "
					+ url.toExternalForm());
		}
	}

	/**
	 * Compiler this factory uses
	 * 
	 * @return final Compiler instance
	 */
	public Compiler getCompiler() {
		return this.compiler;
	}

	public long getRefreshPeriod() {
		return refreshPeriod;
	}
}
