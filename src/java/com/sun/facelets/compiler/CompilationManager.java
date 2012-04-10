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

package com.sun.facelets.compiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.facelets.FaceletHandler;
import com.sun.facelets.tag.Tag;
import com.sun.facelets.tag.TagAttribute;
import com.sun.facelets.tag.TagAttributeException;
import com.sun.facelets.tag.TagAttributes;
import com.sun.facelets.tag.TagDecorator;
import com.sun.facelets.tag.TagException;
import com.sun.facelets.tag.TagLibrary;
import com.sun.facelets.tag.ui.ComponentRefHandler;
import com.sun.facelets.tag.ui.CompositionHandler;
import com.sun.facelets.tag.ui.UILibrary;

/**
 * Compilation unit for managing the creation of a single FaceletHandler based
 * on events from an XML parser.
 * 
 * @see com.sun.facelets.compiler.Compiler
 * 
 * @author Jacob Hookom
 * @version $Id: CompilationManager.java,v 1.15 2008-07-13 19:01:34 rlubke Exp $
 */
final class CompilationManager {

    private final static Logger log = Logger.getLogger("facelets.compiler");

    private final Compiler compiler;

    private final TagLibrary tagLibrary;

    private final TagDecorator tagDecorator;

    private final NamespaceManager namespaceManager;

    private final Stack units;

    private int tagId;

    private boolean finished;
    
    private final String alias;

    public CompilationManager(String alias, Compiler compiler) {
        
        // this is our alias
        this.alias = alias;

        // grab compiler state
        this.compiler = compiler;
        this.tagDecorator = compiler.createTagDecorator();
        this.tagLibrary = compiler.createTagLibrary();

        // namespace management
        this.namespaceManager = new NamespaceManager();

        // tag uids
        this.tagId = 0;

        // for composition use
        this.finished = false;

        // our compilationunit stack
        this.units = new Stack();
        this.units.push(new CompilationUnit());
    }
    
    public void writeInstruction(String value) {
        if (this.finished) {
            return;
        }

        // don't carelessly add empty tags
        if (value.length() == 0) {
            return;
        }

        TextUnit unit;
        if (this.currentUnit() instanceof TextUnit) {
            unit = (TextUnit) this.currentUnit();
        } else {
            unit = new TextUnit(this.alias, this.nextTagId());
            this.startUnit(unit);
        }
        unit.writeInstruction(value);
    }

    public void writeText(String value) {

        if (this.finished) {
            return;
        }

        // don't carelessly add empty tags
        if (value.length() == 0) {
            return;
        }

        TextUnit unit;
        if (this.currentUnit() instanceof TextUnit) {
            unit = (TextUnit) this.currentUnit();
        } else {
            unit = new TextUnit(this.alias, this.nextTagId());
            this.startUnit(unit);
        }
        unit.write(value);
    }

    public void writeComment(String text) {
        if (this.compiler.isTrimmingComments())
            return; 

        if (this.finished) {
            return;
        }
          
        // don't carelessly add empty tags
        if (text.length() == 0) {
            return;
        }
          
        TextUnit unit;
        if (this.currentUnit() instanceof TextUnit) {
            unit = (TextUnit) this.currentUnit();
        } else {
            unit = new TextUnit(this.alias, this.nextTagId());
            this.startUnit(unit);
        }
          
        unit.writeComment(text);
    }

    public void writeWhitespace(String text) {
        if (!this.compiler.isTrimmingWhitespace()) {
            this.writeText(text);
        }
    }

    private String nextTagId() {
        return Integer.toHexString(Math.abs(this.alias.hashCode() ^ 13 * this.tagId++));
    }

    public void pushTag(Tag orig) {

        if (this.finished) {
            return;
        }

        if (log.isLoggable(Level.FINE)) {
            log.fine("Tag Pushed: " + orig);
        }

        Tag t = this.tagDecorator.decorate(orig);
        String[] qname = this.determineQName(t);
        t = this.trimAttributes(t);

        boolean handled = false;

        if (isTrimmed(qname[0], qname[1])) {
            log.fine("Composition Found, Popping Parent Tags");
            this.units.clear();
            NamespaceUnit nsUnit = this.namespaceManager
                    .toNamespaceUnit(this.tagLibrary);
            this.units.push(nsUnit);
            this.startUnit(new TrimmedTagUnit(this.tagLibrary, qname[0], qname[1], t, this
                    .nextTagId()));
            log.fine("New Namespace and [Trimmed] TagUnit pushed");
        } else if (isRemove(qname[0], qname[1])) {
            this.units.push(new RemoveUnit());
        } else if (this.tagLibrary.containsTagHandler(qname[0], qname[1])) {
            this.startUnit(new TagUnit(this.tagLibrary, qname[0], qname[1], t, this.nextTagId()));
        } else if (this.tagLibrary.containsNamespace(qname[0])) {
            throw new TagException(orig, "Tag Library supports namespace: "+qname[0]+", but no tag was defined for name: "+qname[1]);
        } else {
            TextUnit unit;
            if (this.currentUnit() instanceof TextUnit) {
                unit = (TextUnit) this.currentUnit();
            } else {
                unit = new TextUnit(this.alias, this.nextTagId());
                this.startUnit(unit);
            }
            unit.startTag(t);
        }
    }

    public void popTag() {

        if (this.finished) {
            return;
        }

        CompilationUnit unit = this.currentUnit();

        if (unit instanceof TextUnit) {
            TextUnit t = (TextUnit) unit;
            if (t.isClosed()) {
                this.finishUnit();
            } else {
                t.endTag();
                return;
            }
        }

        unit = this.currentUnit();
        if (unit instanceof TagUnit) {
            TagUnit t = (TagUnit) unit;
            if (t instanceof TrimmedTagUnit) {
                this.finished = true;
                return;
            }
        }

        this.finishUnit();
    }

    public void popNamespace(String ns) {
        this.namespaceManager.popNamespace(ns);
        if (this.currentUnit() instanceof NamespaceUnit) {
            this.finishUnit();
        }
    }

    public void pushNamespace(String prefix, String uri) {

        if (log.isLoggable(Level.FINE)) {
            log.fine("Namespace Pushed " + prefix + ": " + uri);
        }

        this.namespaceManager.pushNamespace(prefix, uri);
        NamespaceUnit unit;
        if (this.currentUnit() instanceof NamespaceUnit) {
            unit = (NamespaceUnit) this.currentUnit();
        } else {
            unit = new NamespaceUnit(this.tagLibrary);
            this.startUnit(unit);
        }
        unit.setNamespace(prefix, uri);
    }

    public FaceletHandler createFaceletHandler() {
        return ((CompilationUnit) this.units.get(0)).createFaceletHandler();
    }

    private CompilationUnit currentUnit() {
        if (!this.units.isEmpty()) {
            return (CompilationUnit) this.units.peek();
        }
        return null;
    }

    private void finishUnit() {
        Object obj = this.units.pop();

        if (log.isLoggable(Level.FINE)) {
            log.fine("Finished Unit: " + obj);
        }
    }

    private CompilationUnit searchUnits(Class type) {
        CompilationUnit unit = null;
        int i = this.units.size();
        while (unit == null && --i >= 0) {
            if (type.isAssignableFrom(this.units.get(i).getClass())) {
                unit = (CompilationUnit) this.units.get(i);
            }
        }
        return unit;
    }

    private void startUnit(CompilationUnit unit) {

        if (log.isLoggable(Level.FINE)) {
            log.fine("Starting Unit: " + unit + " and adding it to parent: "
                    + this.currentUnit());
        }

        this.currentUnit().addChild(unit);
        this.units.push(unit);
    }

    private Tag trimAttributes(Tag tag) {
        Tag t = this.trimJSFCAttribute(tag);
        t = this.trimNSAttributes(t);
        return t;
    }

    protected static boolean isRemove(String ns, String name) {
        return UILibrary.Namespace.equals(ns)
                && "remove".equals(name);
    }

    protected static boolean isTrimmed(String ns, String name) {
        return UILibrary.Namespace.equals(ns)
                && (CompositionHandler.Name.equals(name) || ComponentRefHandler.Name.equals(name));
    }

    private String[] determineQName(Tag tag) {
        TagAttribute attr = tag.getAttributes().get("jsfc");
        if (attr != null) {
            if (log.isLoggable(Level.FINE)) {
                log.fine(attr + " JSF Facelet Compile Directive Found");
            }
            String value = attr.getValue();
            String namespace, localName;
            int c = value.indexOf(':');
            if (c == -1) {
                namespace = this.namespaceManager.getNamespace("");
                localName = value;
            } else {
                String prefix = value.substring(0, c);
                namespace = this.namespaceManager.getNamespace(prefix);
                if (namespace == null) {
                    throw new TagAttributeException(tag, attr,
                            "No Namespace matched for: " + prefix);
                }
                localName = value.substring(c + 1);
            }
            return new String[] { namespace, localName };
        } else {
            return new String[] { tag.getNamespace(), tag.getLocalName() };
        }
    }

    private Tag trimJSFCAttribute(Tag tag) {
        TagAttribute attr = tag.getAttributes().get("jsfc");
        if (attr != null) {
            TagAttribute[] oa = tag.getAttributes().getAll();
            TagAttribute[] na = new TagAttribute[oa.length - 1];
            int p = 0;
            for (int i = 0; i < oa.length; i++) {
                if (!"jsfc".equals(oa[i].getLocalName())) {
                    na[p++] = oa[i];
                }
            }
            return new Tag(tag, new TagAttributes(na));
        }
        return tag;
    }

    private Tag trimNSAttributes(Tag tag) {
        TagAttribute[] attr = tag.getAttributes().getAll();
        int remove = 0;
        for (int i = 0; i < attr.length; i++) {
            if (attr[i].getQName().startsWith("xmlns")
                    && this.tagLibrary.containsNamespace(attr[i].getValue())) {
                remove |= 1 << i;
                if (log.isLoggable(Level.FINE)) {
                    log.fine(attr[i] + " Namespace Bound to TagLibrary");
                }
            }
        }
        if (remove == 0) {
            return tag;
        } else {
            List attrList = new ArrayList(attr.length);
            int p = 0;
            for (int i = 0; i < attr.length; i++) {
                p = 1 << i;
                if ((p & remove) == p) {
                    continue;
                }
                attrList.add(attr[i]);
            }
            attr = (TagAttribute[]) attrList.toArray(new TagAttribute[attrList
                    .size()]);
            return new Tag(tag.getLocation(), tag.getNamespace(), tag
                    .getLocalName(), tag.getQName(), new TagAttributes(attr));
        }
    }
}
