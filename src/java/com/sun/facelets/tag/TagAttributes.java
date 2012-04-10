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

package com.sun.facelets.tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A set of TagAttributes, usually representing all attributes on a Tag.
 * 
 * @see com.sun.facelets.tag.Tag
 * @see com.sun.facelets.tag.TagAttribute
 * @author Jacob Hookom
 * @version $Id: TagAttributes.java,v 1.3 2008-07-13 19:01:35 rlubke Exp $
 */
public final class TagAttributes {
    private final static TagAttribute[] EMPTY = new TagAttribute[0];

    private final TagAttribute[] attrs;

    private final String[] ns;

    private final List nsattrs;

    /**
     * 
     */
    public TagAttributes(TagAttribute[] attrs) {
        this.attrs = attrs;

        // grab namespaces
        int i = 0;
        Set set = new HashSet();
        for (i = 0; i < this.attrs.length; i++) {
            set.add(this.attrs[i].getNamespace());
        }
        this.ns = (String[]) set.toArray(new String[set.size()]);
        Arrays.sort(ns);

        // assign attrs
        this.nsattrs = new ArrayList();
        for (i = 0; i < ns.length; i++) {
            nsattrs.add(i, new ArrayList());
        }
        int nsIdx = 0;
        for (i = 0; i < this.attrs.length; i++) {
            nsIdx = Arrays.binarySearch(ns, this.attrs[i].getNamespace());
            ((List) nsattrs.get(nsIdx)).add(this.attrs[i]);
        }
        for (i = 0; i < ns.length; i++) {
            List r = (List) nsattrs.get(i);
            nsattrs.set(i, r.toArray(new TagAttribute[r.size()]));
        }
    }

    /**
     * Return an array of all TagAttributes in this set
     * 
     * @return a non-null array of TagAttributes
     */
    public TagAttribute[] getAll() {
        return this.attrs;
    }

    /**
     * Using no namespace, find the TagAttribute
     * 
     * @see #get(String, String)
     * @param localName
     *            tag attribute name
     * @return the TagAttribute found, otherwise null
     */
    public TagAttribute get(String localName) {
        return get("", localName);
    }

    /**
     * Find a TagAttribute that matches the passed namespace and local name.
     * 
     * @param ns
     *            namespace of the desired attribute
     * @param localName
     *            local name of the attribute
     * @return a TagAttribute found, otherwise null
     */
    public TagAttribute get(String ns, String localName) {
        if (ns != null && localName != null) {
            int idx = Arrays.binarySearch(this.ns, ns);
            if (idx >= 0) {
                TagAttribute[] uia = (TagAttribute[]) this.nsattrs.get(idx);
                for (int i = 0; i < uia.length; i++) {
                    if (localName.equals(uia[i].getLocalName())) {
                        return uia[i];
                    }
                }
            }
        }
        return null;
    }

    /**
     * Get all TagAttributes for the passed namespace
     * 
     * @param namespace
     *            namespace to search
     * @return a non-null array of TagAttributes
     */
    public TagAttribute[] getAll(String namespace) {
        int idx = 0;
        if (namespace == null) {
            idx = Arrays.binarySearch(this.ns, "");
        } else {
            idx = Arrays.binarySearch(this.ns, namespace);
        }
        if (idx >= 0) {
            return (TagAttribute[]) this.nsattrs.get(idx);
        }
        return EMPTY;
    }

    /**
     * A list of Namespaces found in this set
     * 
     * @return a list of Namespaces found in this set
     */
    public String[] getNamespaces() {
        return this.ns;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < this.attrs.length; i++) {
            sb.append(this.attrs[i]);
            sb.append(' ');
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }
}
