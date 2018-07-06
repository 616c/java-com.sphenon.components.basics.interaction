package com.sphenon.basics.interaction.classes;

/****************************************************************************
  Copyright 2001-2018 Sphenon GmbH

  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  License for the specific language governing permissions and limitations
  under the License.
*****************************************************************************/

import com.sphenon.basics.context.*;
import com.sphenon.basics.variatives.*;
import com.sphenon.basics.variatives.classes.*;

import com.sphenon.basics.interaction.*;

public class Class_Anchor {

    static public<T> T _anchor(CallContext context, T object_to_anchor, Anchor base_anchor) {
        if (object_to_anchor instanceof Anchorable && base_anchor != null) {
            object_to_anchor = ((T)(((Anchorable) object_to_anchor).createAnchor(context, base_anchor._getWorkspace(context), base_anchor._getTransaction(context))));
        }
        return object_to_anchor;
    }

    static public<T> T _deanchor(CallContext context, T object) {
        while (object instanceof Anchor) {
            object = ((T)(((Anchor) object)._getDelegate(context)));
        }
        return object;
    }
}
