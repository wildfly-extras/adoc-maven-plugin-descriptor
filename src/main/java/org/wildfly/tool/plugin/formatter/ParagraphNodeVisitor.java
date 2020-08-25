/*
 * Copyright 2016-2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.tool.plugin.formatter;

import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class ParagraphNodeVisitor extends AbstractNodeVisitor implements NodeVisitor {

    ParagraphNodeVisitor(final StringBuilder builder) {
        super(builder);
    }

    @Override
    public void head(final Node node, final int depth) {
        appendNewLine();
    }

    @Override
    public void tail(final Node node, final int depth) {
        appendNewLine();
    }

    @Override
    public boolean canProcess(final Node node) {
        return nodeNameMatches(node, "p", "div");
    }
}
