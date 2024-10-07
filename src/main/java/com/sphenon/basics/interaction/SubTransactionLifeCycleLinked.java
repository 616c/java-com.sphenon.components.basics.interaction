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
import com.sphenon.basics.variatives.*;
import com.sphenon.basics.variatives.classes.*;

public interface SubTransactionLifeCycleLinked extends Transaction {
    /**
       Checks whether the subtransaction can be completed and afterwards
       complete safely called. Obviously, this is only guaranteed if the
       subtransaction is not modified inbetween these two calls.

       Note: this method can and often shall modify the visual state of the
       subtransaction. E.g., if it returns false due to an invalid member, it
       is expected to signal that invalidity to the user. The calling
       transaction is not expected to handle that case, it typically just
       "does nothing".
    */
    public boolean canComplete(CallContext context);

    /**
       Completes the subtransaction, i.e. writes any data that should be
       written and may does other completion stuff. Afterwards, the main
       transaction can safely be commited. Can only be safely called if a
       prior call to canComplete returns true. Otherwise, an unchecked
       exception might be thrown.
    */
    public void    complete(CallContext context);

    /**
       After completion, the subtransaction can be reactivated to put it into
       the state as it was before completion. Specifically, data entered by
       the user is not cleared, in contrast to the reset method.
       This is typically called if there is a problem in the main transaction
       after completion of the subtransaction and editing shall continue.
    */
    public void    reactivate(CallContext context);

    public void    cancel(CallContext context);

    /**
       Puts the subtransaction back into a state as if it was just started
       freshly. This may include the removal of data entered by the user,
       which then is lost.
    */
    public void    reset(CallContext context);
}
