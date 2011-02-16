/**
 * Copyright (c) 2010 Stefan Henss.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Stefan Henss - initial API and implementation.
 */
package completion.templates.bugs;

import org.eclipse.swt.widgets.Button;

public class CompletionOnReturn {

    public String completionOnReturn() {
        Button b = new Button(null, 0);
        // Should there be patterns at all? If so, take care only
        // one-liners with appropriate return types are proposed.
        // Additional: Large template that is not displayed
        return b.<^Space>;
    }
}
