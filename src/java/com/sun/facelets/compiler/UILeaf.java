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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.Renderer;

import com.sun.facelets.el.ELText;

class UILeaf extends UIComponentBase {
    
    private final static Map facets = new HashMap(){
    
        public void putAll(Map map) {
            // do nothing
        }
    
        public Object put(Object name, Object value) {
            return null;
        }
    };
    
    private UIComponent parent;

    public ValueBinding getValueBinding(String binding) {
        return null;
    }

    public void setValueBinding(String name, ValueBinding binding) {
        // do nothing
    }

    public ValueExpression getValueExpression(String name) {
        return null;
    }

    public void setValueExpression(String name, ValueExpression arg1) {
        // do nothing
    }

    public String getFamily() {
        return "facelets.LiteralText";
    }

    public UIComponent getParent() {
        return this.parent;
    }

    public void setParent(UIComponent parent) {
        this.parent = parent;
    }

    public boolean isRendered() {
        return true;
    }

    public void setRendered(boolean rendered) {
        // do nothing
    }

    public String getRendererType() {
        return null;
    }

    public void setRendererType(String rendererType) {
        // do nothing
    }

    public boolean getRendersChildren() {
        return true;
    }

    public List getChildren() {
        return Collections.EMPTY_LIST;
    }

    public int getChildCount() {
        return 0;
    }

    public UIComponent findComponent(String id) {
        return null;
    }

    public Map getFacets() {
        return facets;
    }

    public int getFacetCount() {
        return 0;
    }

    public UIComponent getFacet(String name) {
        return null;
    }

    public Iterator getFacetsAndChildren() {
        return Collections.EMPTY_LIST.iterator();
    }

    public void broadcast(FacesEvent event) throws AbortProcessingException {
        // do nothing
    }

    public void decode(FacesContext faces) {
        // do nothing
    }

    public void encodeBegin(FacesContext faces) throws IOException {
        // do nothing
    }

    public void encodeChildren(FacesContext faces) throws IOException {
        // do nothing
    }

    public void encodeEnd(FacesContext faces) throws IOException {
        // do nothing
    }

    public void encodeAll(FacesContext faces) throws IOException {
        this.encodeBegin(faces);
    }

    protected void addFacesListener(FacesListener faces) {
        // do nothing
    }

    protected FacesListener[] getFacesListeners(Class faces) {
        return null;
    }

    protected void removeFacesListener(FacesListener faces) {
        // do nothing
    }

    public void queueEvent(FacesEvent event) {
        // do nothing
    }

    public void processRestoreState(FacesContext faces, Object state) {
        // do nothing
    }

    public void processDecodes(FacesContext faces) {
        // do nothing
    }

    public void processValidators(FacesContext faces) {
        // do nothing
    }

    public void processUpdates(FacesContext faces) {
        // do nothing
    }

    public Object processSaveState(FacesContext faces) {
        return null;
    }

    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    protected Renderer getRenderer(FacesContext faces) {
        return null;
    }

    public Object saveState(FacesContext faces) {
        return null;
    }

    public void restoreState(FacesContext faces, Object state) {
        // do nothing
    }

    public boolean isTransient() {
        return true;
    }

    public void setTransient(boolean tranzient) {
        // do nothing
    }

}
