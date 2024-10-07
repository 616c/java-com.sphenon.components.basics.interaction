package com.sphenon.basics.interaction;

/****************************************************************************
  Copyright 2001-2024 Sphenon GmbH

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
import com.sphenon.basics.message.*;
import com.sphenon.basics.exception.*;
import com.sphenon.basics.customary.*;

public class InteractionContext extends SpecificContext {

    static public InteractionContext getOrCreate(Context context) {
        InteractionContext interaction_context = (InteractionContext) context.getSpecificContext(InteractionContext.class);
        if (interaction_context == null) {
            interaction_context = new InteractionContext(context);
            context.setSpecificContext(InteractionContext.class, interaction_context);
        }
        return interaction_context;
    }

    static public InteractionContext get(Context context) {
        InteractionContext interaction_context = (InteractionContext) context.getSpecificContext(InteractionContext.class);
        if (interaction_context != null) {
            return interaction_context;
        }
        return null;
    }

    static public InteractionContext create(Context context) {
        InteractionContext interaction_context = new InteractionContext(context);
        context.setSpecificContext(InteractionContext.class, interaction_context);
        return interaction_context;
    }

    protected InteractionContext (Context context) {
        super(context);
        this.transaction = null;
        this.workspace = null;
    }

    static public CallContext createMergedInteractionContext(CallContext context, Transaction transaction, Workspace workspace){
        CallContext local_context = context;
        LocationContext lc = null;
        if (transaction instanceof Located) {
            LocationContext super_lc = ((Located) transaction).getLocationContext(context);
            lc = Context.create(null, super_lc);
        } else {
            lc = com.sphenon.basics.context.classes.RootContext.createLocationContext();
        }
        com.sphenon.basics.interaction.InteractionContext sc = com.sphenon.basics.interaction.InteractionContext.create((Context) lc);
        sc.setWorkspace(context, workspace);
        sc.setTransaction(context, transaction);
        local_context = Context.create(context, lc);
        return local_context;
    }
    
    protected Transaction transaction;

    public void setTransaction(CallContext context, Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction(CallContext cc) {
        InteractionContext interaction_context;
        Transaction result;
        return (this.transaction != null ?
                     this.transaction
                  : (    (interaction_context = (InteractionContext) this.getLocationContext(InteractionContext.class)) != null
                      && (result = interaction_context.getTransaction(cc)) != null
                    ) ? result
                  : (    (interaction_context = (InteractionContext) this.getCallContext(InteractionContext.class)) != null
                      && (result = interaction_context.getTransaction(cc)) != null
                    ) ? result
                  : null
               );
    }

    protected Workspace workspace;

    public void setWorkspace(CallContext context, Workspace workspace) {
        this.workspace = workspace;
    }

    public Workspace getWorkspace(CallContext cc) {
        InteractionContext interaction_context;
        Workspace result;
        return (this.workspace != null ?
                     this.workspace
                  : (    (interaction_context = (InteractionContext) this.getLocationContext(InteractionContext.class)) != null
                      && (result = interaction_context.getWorkspace(cc)) != null
                    ) ? result
                  : (    (interaction_context = (InteractionContext) this.getCallContext(InteractionContext.class)) != null
                      && (result = interaction_context.getWorkspace(cc)) != null
                    ) ? result
                  : null
               );
    }

}
