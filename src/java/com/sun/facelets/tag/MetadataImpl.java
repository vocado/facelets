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

import com.sun.facelets.FaceletContext;

/**
 * 
 * @author Jacob Hookom
 * @version $Id: MetadataImpl.java,v 1.3 2008-07-13 19:01:36 rlubke Exp $
 */
final class MetadataImpl extends Metadata {

    private final Metadata[] mappers;
    private final int size;
    
    public MetadataImpl(Metadata[] mappers) {
        this.mappers = mappers;
        this.size = mappers.length;
    }

    public void applyMetadata(FaceletContext ctx, Object instance) {
        for (int i = 0; i < size; i++) {
            this.mappers[i].applyMetadata(ctx, instance);
        }
    }

}
