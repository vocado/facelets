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

import javax.faces.component.UIComponentBase;

public final class ComponentRef extends UIComponentBase {

    public final static String COMPONENT_TYPE = "facelets.ui.ComponentRef";
    public final static String COMPONENT_FAMILY = "facelets";
    
    public ComponentRef() {
        super();
    }

    public String getFamily() {
        return COMPONENT_FAMILY;
    }

}
