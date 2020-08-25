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
class ListNodeVisitor extends AbstractNodeVisitor implements NodeVisitor {
    private int listDepth = 0;
    private char prefixChar = '*';

    ListNodeVisitor(final StringBuilder builder) {
        super(builder);
    }

    @Override
    public void head(final Node node, final int depth) {
        if (nodeNameMatches(node, "ul", "ol")) {
            listDepth++;
            if ("ol".equals(node.nodeName())) {
                prefixChar = '.';
            } else {
                prefixChar = '*';
            }
            if (listDepth == 1) {
                appendNewLine();
            }
        } else {
            if (node.childNodeSize() > 0) {
                final String text = getText(node.childNode(0));
                if (!text.trim().isEmpty()) {
                    for (int i = 0; i < listDepth; i++) {
                        append(prefixChar);
                    }
                    append(' ');
                }
            }
        }
    }

    @Override
    public void tail(final Node node, final int depth) {
        if (nodeNameMatches(node, "ul", "ol")) {
            listDepth = Math.max(0, listDepth - 1);
        } else {
            appendNewLine();
        }
    }

    @Override
    public boolean canProcess(final Node node) {
        return nodeNameMatches(node, "ul", "ol", "li");
    }
}
