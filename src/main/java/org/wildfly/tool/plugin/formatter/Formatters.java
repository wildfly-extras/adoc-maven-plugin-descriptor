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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.NodeTraversor;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class Formatters {

    /**
     * Formats HTML tags into asciidoc equivalents.
     *
     * @param text the text to convert
     *
     * @return the formatted test
     */
    public static String format(final String text) {
        final Document document = Jsoup.parseBodyFragment(text);
        // No need to process if there is no HTML
        if (document.text().equals(text)) {
            return text;
        }
        final StringBuilder result = new StringBuilder();
        final AppendableNodeVisitor[] visitors = {
                new ListNodeVisitor(result),
                new BreakNodeVisitor(result),
                new CodeBlockNodeVisitor(result),
                new ParagraphNodeVisitor(result),
                new BoldNodeVisitor(result),
                new ItalicNodeVisitor(result),
                new TextNodeVisitor(result),
        };
        NodeTraversor.traverse(new CompositeNodeVisitor(result, visitors), document.children());
        return result.toString();
    }
}
