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

package com.sun.facelets.tag.jstl.core;

import java.io.Serializable;

/**
 * @author Jacob Hookom
 * @version $Id: IterationStatus.java,v 1.5 2009-02-03 21:33:01 edburns Exp $
 */
public final class IterationStatus implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final int index;
    
    private final boolean first;
    
    private final boolean last;

    private final boolean even;

    private final Integer begin;

    private final Integer end;

    private final Integer step;

    /**
     * 
     */
    public IterationStatus(boolean first, boolean last, int index, Integer begin, Integer end, Integer step) {
        this.index = index;
        this.begin = begin;
        this.end = end;
        this.step = step;
        this.first = first;
        this.last = last;
        this.even = (index - begin.intValue() / step.intValue()) % 2 == 0;
    }

    public boolean isFirst() {
        return this.first;
    }

    public boolean isLast() {
        return this.last;
    }

    public boolean isEven() {
        return even;
    }

    public boolean isOdd() {
        return !even;
    }

    public Integer getBegin() {
        return begin;
    }

    public Integer getEnd() {
        return end;
    }

    public int getIndex() {
        return index;
    }

    public Integer getStep() {
        return step;
    }

}
