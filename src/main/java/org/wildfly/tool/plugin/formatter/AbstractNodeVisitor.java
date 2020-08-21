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
import org.jsoup.nodes.TextNode;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractNodeVisitor implements AppendableNodeVisitor {
    private final StringBuilder builder;

    AbstractNodeVisitor(final StringBuilder builder) {
        this.builder = builder;
    }

    @Override
    public AbstractNodeVisitor append(final CharSequence csq) {
        builder.append(csq);
        return this;
    }

    @Override
    public AbstractNodeVisitor append(final CharSequence csq, final int start, final int end) {
        builder.append(csq, start, end);
        return this;
    }

    @Override
    public AbstractNodeVisitor append(final char c) {
        builder.append(c);
        return this;
    }

    @Override
    public AppendableNodeVisitor appendNewLine() {
        builder.append(System.lineSeparator());
        return this;
    }

    @Override
    public String toString() {
        return builder.toString();
    }

    /**
     * Gets the possible text for a node. If the node is not a {@link TextNode} and empty string is returned.
     *
     * @param node the node to get the text from
     *
     * @return the text for the node or an empty string
     */
    String getText(final Node node) {
        if (node instanceof TextNode) {
            final TextNode textNode = (TextNode) node;
            return textNode.getWholeText();
        }
        return "";
    }

    /**
     * Gets the possible text for the first child node. If the child node is not a {@link TextNode} and empty string is
     * returned.
     *
     * @param node the node to get the first child from
     *
     * @return the text for the first child node or an empty string
     */
    String getChildText(final Node node) {
        if (node.childNodeSize() > 0) {
            return getText(node.childNode(0));
        }
        return "";
    }

    /**
     * Checks the node name against the names and returns {@code true} if one of the names matches the nodes name.
     *
     * @param node  the node to check
     * @param names the names that may match
     *
     * @return {@code true} if the node name matches one of the names, otherwise {@code false}
     */
    boolean nodeNameMatches(final Node node, final String... names) {
        final String nodeName = node.nodeName();
        for (String name : names) {
            if (name.equals(nodeName)) {
                return true;
            }
        }
        return false;
    }
}
