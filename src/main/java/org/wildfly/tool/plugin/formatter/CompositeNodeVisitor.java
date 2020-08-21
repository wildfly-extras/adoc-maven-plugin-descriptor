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

import java.util.Arrays;
import java.util.Collection;

import org.jsoup.nodes.Node;
import org.jsoup.select.NodeVisitor;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class CompositeNodeVisitor implements NodeVisitor {
    private final StringBuilder builder;
    private final Collection<AppendableNodeVisitor> visitors;

    CompositeNodeVisitor(final StringBuilder builder, final AppendableNodeVisitor... visitors) {
        this.builder = builder;
        this.visitors = Arrays.asList(visitors);
    }

    @Override
    public void head(final Node node, final int depth) {
        boolean processed = false;
        for (AppendableNodeVisitor visitor : visitors) {
            if (visitor.canProcess(node)) {
                visitor.head(node, depth);
                processed = true;
            }
        }
        if (!processed && notMatches(node,"body", "html", "head")) {
            builder.append(node.toString());
        }
    }

    @Override
    public void tail(final Node node, final int depth) {
        for (AppendableNodeVisitor visitor : visitors) {
            if (visitor.canProcess(node)) {
                visitor.tail(node, depth);
            }
        }
    }

    private static boolean notMatches(final Node node, final String... names) {
        final String nodeName = node.nodeName();
        for (String name : names) {
            if (name.equals(nodeName)) {
                return false;
            }
        }
        return true;
    }
}
