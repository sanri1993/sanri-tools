/*
 * Copyright (c) 2007, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package com.sanri.tools.modules.jvm.service.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tomas Hurka
 */
public abstract class HeapHistogram {

    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    public abstract Date getTime();
    public abstract long getTotalInstances();
    public abstract long getTotalBytes();
    public abstract List<ClassInfo> getHeapHistogram();
    public abstract long getTotalHeapInstances();
    public abstract long getTotalHeapBytes();
    public abstract Set<ClassInfo> getPermGenHistogram();
    public abstract long getTotalPerGenInstances();
    public abstract long getTotalPermGenHeapBytes();
    
    public static abstract class ClassInfo {
        
        public abstract String getName();
        public abstract long getInstancesCount();
        public abstract long getBytes();
        
        @Override
        public int hashCode() {
            return getName().hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ClassInfo) {
                return getName().equals(((ClassInfo)obj).getName());
            }
            return false;
        }
    }
}
