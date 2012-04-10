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

package com.sun.facelets.tag.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.servlet.http.HttpServletResponse;

import com.sun.facelets.util.DevTools;
import com.sun.facelets.util.FastWriter;

/**
 * @author Jacob Hookom
 * @version $Id: UIDebug.java,v 1.6 2008-07-13 19:01:41 rlubke Exp $
 */
public final class UIDebug extends UIComponentBase {

    public final static String COMPONENT_TYPE = "facelets.ui.Debug";
    public final static String COMPONENT_FAMILY = "facelets";
    private static long nextId = System.currentTimeMillis();
    private final static String KEY = "facelets.ui.DebugOutput";
    public final static String DEFAULT_HOTKEY = "D";
    private String hotkey = DEFAULT_HOTKEY;
    
    public UIDebug() {
        super();
        this.setTransient(true);
        this.setRendered(true);
        this.setRendererType(null);
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public List getChildren() {
        return new ArrayList() {
            public boolean add(Object o) {
                throw new IllegalStateException("<ui:debug> does not support children");
            }

            public void add(int index, Object o) {
                throw new IllegalStateException("<ui:debug> does not support children");
            }
        };
    }

    public void encodeBegin(FacesContext faces) throws IOException {

        String actionId = faces.getApplication().getViewHandler().getActionURL(faces, faces.getViewRoot().getViewId());
        
        StringBuffer sb = new StringBuffer(512);
        sb.append("<script language=\"javascript\" type=\"text/javascript\">\n");
        sb.append("//<![CDATA[\n");
        sb.append("function faceletsDebug(URL) { day = new Date(); id = day.getTime(); eval(\"page\" + id + \" = window.open(URL, '\" + id + \"', 'toolbar=0,scrollbars=1,location=0,statusbar=0,menubar=0,resizable=1,width=800,height=600,left = 240,top = 212');\"); };");
        sb.append("var faceletsOrigKeyup = document.onkeyup; document.onkeyup = function(e) { if (window.event) e = window.event; if (String.fromCharCode(e.keyCode) == '" + this.getHotkey() + "' & e.shiftKey & e.ctrlKey) faceletsDebug('");
        sb.append(actionId);
        sb.append('?');
        sb.append(KEY);
        sb.append('=');
        sb.append(writeDebugOutput(faces));
        sb.append("'); else if (faceletsOrigKeyup) faceletsOrigKeyup(e); };\n");
        sb.append("//]]>\n");
        sb.append("</script>\n");
        
        ResponseWriter writer = faces.getResponseWriter();
        writer.write(sb.toString());
    }
    
    private static String writeDebugOutput(FacesContext faces) throws IOException {
        FastWriter fw = new FastWriter();
        DevTools.debugHtml(fw, faces);
        
        Map session = faces.getExternalContext().getSessionMap();
        Map debugs = (Map) session.get(KEY);
        if (debugs == null) {
            debugs = new LinkedHashMap() {
                protected boolean removeEldestEntry(Entry eldest) {
                    return (this.size() > 5);
                }
            };
            session.put(KEY, debugs);
        }
        String id = "" + nextId++;
        debugs.put(id, fw.toString());
        return id;
    }
    
    private static String fetchDebugOutput(FacesContext faces, String id) {
        Map session = faces.getExternalContext().getSessionMap();
        Map debugs = (Map) session.get(KEY);
        if (debugs != null) {
            return (String) debugs.get(id);
        }
        return null;
    }
    
    public static boolean debugRequest(FacesContext faces) {
        String id = (String) faces.getExternalContext().getRequestParameterMap().get(KEY);
        if (id != null) {
            Object resp = faces.getExternalContext().getResponse();
            if (!faces.getResponseComplete()
                && resp instanceof HttpServletResponse) {
                try {
                    HttpServletResponse httpResp = (HttpServletResponse) resp;
                    String page = fetchDebugOutput(faces, id);
                    if (page != null) {
                        httpResp.setContentType("text/html");
                        httpResp.getWriter().write(page);
                    } else {
                        httpResp.setContentType("text/plain");
                        httpResp.getWriter().write("No Debug Output Available");
                    }
                    httpResp.flushBuffer();
                    faces.responseComplete();
                } catch (IOException e) {
                    return false;
                }
                return true;
            }
        }
        return false;
    }
    
    public String getHotkey() {
        return this.hotkey;
    }
    
    public void setHotkey(String hotkey) {
        this.hotkey = (hotkey != null) ? hotkey.toUpperCase() : "";
    }

}
