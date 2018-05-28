/*
 * Copyright 2016-2018 Red Hat, Inc. and/or its affiliates
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
package org.wildfly.tool.plugin;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.tools.plugin.ExtendedMojoDescriptor;
import org.apache.maven.tools.plugin.PluginToolsRequest;
import org.apache.maven.tools.plugin.generator.GeneratorException;
import org.apache.maven.tools.plugin.generator.GeneratorUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 */
public class PluginAdocGenerator implements org.apache.maven.tools.plugin.generator.Generator {

    /**
     * locale
     */
    private final Locale locale;

    public PluginAdocGenerator() {
        this.locale = Locale.ENGLISH;
    }

    public PluginAdocGenerator(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void execute(File destinationDirectory, PluginToolsRequest request) throws GeneratorException {
        try {
            if (request.getPluginDescriptor().getMojos() != null) {
                @SuppressWarnings("unchecked")
                List<MojoDescriptor> mojos = request.getPluginDescriptor().getMojos();

                for (MojoDescriptor descriptor : mojos) {
                    processMojoDescriptor(descriptor, destinationDirectory);
                }
            }
        } catch (IOException e) {
            throw new GeneratorException(e.getMessage(), e);
        }

    }

    /**
     * @param mojoDescriptor not null
     * @param destinationDirectory not null
     * @throws IOException if any
     */
    protected void processMojoDescriptor(MojoDescriptor mojoDescriptor, File destinationDirectory)
            throws IOException {
        File outputFile = new File(destinationDirectory, getMojoFilename(mojoDescriptor, "adoc"));
        String encoding = "UTF-8";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outputFile, encoding);
            writeBody(mojoDescriptor, writer);

            writer.flush();
        } finally {
            IOUtil.close(writer);
        }
    }

    /**
     * @param mojo not null
     * @param ext not null
     * @return the output file name
     */
    private String getMojoFilename(MojoDescriptor mojo, String ext) {
        return mojo.getGoal() + "-mojo." + ext;
    }

    /**
     * @param mojoDescriptor not null
     * @param w not null
     */
    private void writeBody(MojoDescriptor mojoDescriptor, PrintWriter w) {
        writeTitle(w, mojoDescriptor.getGoal()); // title

        writeSection(w, mojoDescriptor.getFullGoalName(), getString("pluginasciidoc.mojodescriptor.fullname"),
                mojoDescriptor.getPluginDescriptor().getGroupId() + ":"
                + mojoDescriptor.getPluginDescriptor().getArtifactId() + ":"
                + mojoDescriptor.getPluginDescriptor().getVersion() + ":" + mojoDescriptor.getGoal());

        if (StringUtils.isNotEmpty(mojoDescriptor.getDeprecated())) {
            w.println();
            w.println("." + getString("pluginasciidoc.mojodescriptor.deprecated"));
            w.println("#### " + getString("pluginasciidoc.mojodescriptor.deprecated"));
            w.println();
            w.print(makeHtmlValid(mojoDescriptor.getDeprecated()));
            w.println();
        }

        w.println();
        w.println("." + getString("pluginasciidoc.description"));
        w.println("#### " + getString("pluginasciidoc.description"));
        w.println();
        if (StringUtils.isNotEmpty(mojoDescriptor.getDescription())) {
            w.println(makeHtmlValid(mojoDescriptor.getDescription()));
        } else {
            w.println(getString("pluginasciidoc.nodescription"));
        }
        w.println();

        writeGoalAttributes(mojoDescriptor, w);

        writeGoalParameterTable(mojoDescriptor, w);
    }

    private void writeTitle(PrintWriter w, String title) {
        w.print("### ");
        w.println(title);
        w.println();
    }

    private void writeSection(PrintWriter w, String title, String... content) {
        w.print(".");
        w.println(title);
        w.print("#### ");
        w.println(title);
        for (String line : content) {
            w.println(line);
        }
        w.println();
    }

    /**
     * @param mojoDescriptor not null
     * @param w not null
     */
    private void writeGoalAttributes(MojoDescriptor mojoDescriptor, PrintWriter w) {
        w.println("." + getString("pluginasciidoc.mojodescriptor.attributes"));
        w.println();

        String value;
        if (mojoDescriptor.isProjectRequired()) {
            w.print("* ");
            w.println(getString("pluginasciidoc.mojodescriptor.projectRequired"));
        }

        if (mojoDescriptor.isRequiresReports()) {
            w.print("* ");
            w.println(getString("pluginasciidoc.mojodescriptor.reportingMojo"));
        }

        if (mojoDescriptor.isAggregator()) {
            w.print("* ");
            w.println(getString("pluginasciidoc.mojodescriptor.aggregator"));
        }

        if (mojoDescriptor.isDirectInvocationOnly()) {
            w.print("* ");
            w.println(getString("pluginasciidoc.mojodescriptor.directInvocationOnly"));
        }

        value = mojoDescriptor.isDependencyResolutionRequired();
        if (StringUtils.isNotEmpty(value)) {
            w.print("* ");
            w.println(format("pluginasciidoc.mojodescriptor.dependencyResolutionRequired", value));
        }

        if (mojoDescriptor instanceof ExtendedMojoDescriptor) {
            ExtendedMojoDescriptor extendedMojoDescriptor = (ExtendedMojoDescriptor) mojoDescriptor;

            value = extendedMojoDescriptor.getDependencyCollectionRequired();
            if (StringUtils.isNotEmpty(value)) {
                w.print("* ");
                w.println(format("pluginasciidoc.mojodescriptor.dependencyCollectionRequired", value));
            }

            if (extendedMojoDescriptor.isThreadSafe()) {
                w.print("* ");
                w.println(getString("pluginasciidoc.mojodescriptor.threadSafe"));
            }

        }

        value = mojoDescriptor.getSince();
        if (StringUtils.isNotEmpty(value)) {
            w.print("* ");
            w.println(format("pluginasciidoc.mojodescriptor.since", value));
        }

        value = mojoDescriptor.getPhase();
        if (StringUtils.isNotEmpty(value)) {
            w.print("* ");
            w.println(format("pluginasciidoc.mojodescriptor.phase", value));
        }

        value = mojoDescriptor.getExecutePhase();
        if (StringUtils.isNotEmpty(value)) {
            w.print("* ");
            w.println(format("pluginasciidoc.mojodescriptor.executePhase", value));
        }

        value = mojoDescriptor.getExecuteGoal();
        if (StringUtils.isNotEmpty(value)) {
            w.print("* ");
            w.println(format("pluginasciidoc.mojodescriptor.executeGoal", value));
        }

        value = mojoDescriptor.getExecuteLifecycle();
        if (StringUtils.isNotEmpty(value)) {
            w.print("* ");
            w.println(format("pluginasciidoc.mojodescriptor.executeLifecycle", value));
        }

        if (mojoDescriptor.isOnlineRequired()) {
            w.print("* ");
            w.println(getString("pluginasciidoc.mojodescriptor.onlineRequired"));
        }

        if (!mojoDescriptor.isInheritedByDefault()) {
            w.print("* ");
            w.println(getString("pluginasciidoc.mojodescriptor.inheritedByDefault"));
        }
        w.println();
    }

    /**
     * @param mojoDescriptor not null
     * @param w not null
     */
    private void writeGoalParameterTable(MojoDescriptor mojoDescriptor, PrintWriter w) {
        @SuppressWarnings("unchecked")
        List<Parameter> parameterList = mojoDescriptor.getParameters();

        // remove components and read-only parameters
        List<Parameter> list = filterParameters(parameterList);

        if (list != null && list.size() > 0) {
            writeParameterSummary(mojoDescriptor, list, w);

            writeParameterDetails(mojoDescriptor, list, w);
        } else {
            writeSubsection(getString("pluginasciidoc.mojodescriptor.parameters"), w);
            w.println(getString("pluginasciidoc.mojodescriptor.noParameter"));
            w.println();
        }
    }

    private void writeSubsection(String name, PrintWriter w) {
        w.print(".");
        w.println(name);
        w.print("##### ");
        w.println(name);
        w.println();
    }

    /**
     * Filter parameters to only retain those which must be documented, ie not components nor readonly.
     *
     * @param parameterList not null
     * @return the parameters list without components.
     */
    private List<Parameter> filterParameters(List<Parameter> parameterList) {
        List<Parameter> filtered = new ArrayList<Parameter>();

        if (parameterList != null) {
            for (Parameter parameter : parameterList) {
                if (parameter.isEditable()) {
                    String expression = parameter.getExpression();

                    if (expression == null || !expression.startsWith("${component.")) {
                        filtered.add(parameter);
                    }
                }
            }
        }

        return filtered;
    }

    /**
     * @param mojoDescriptor not null
     * @param parameterList not null
     * @param w not null
     */
    private void writeParameterDetails(MojoDescriptor mojoDescriptor, List<Parameter> parameterList, PrintWriter w) {
        writeSubsection(getString("pluginasciidoc.mojodescriptor.parameter.details"), w);

        for (Iterator<Parameter> parameters = parameterList.iterator(); parameters.hasNext();) {
            Parameter parameter = parameters.next();

            w.println(format("pluginasciidoc.mojodescriptor.parameter.name_internal", parameter.getName()));

            if (StringUtils.isNotEmpty(parameter.getDeprecated())) {
                w.println(format("pluginasciidoc.mojodescriptor.parameter.deprecated",
                        makeHtmlValid(parameter.getDeprecated())));
            }

            if (StringUtils.isNotEmpty(parameter.getDescription())) {
                w.println(makeHtmlValid(parameter.getDescription()));
            } else {
                w.println(getString("pluginasciidoc.nodescription"));
            }

            w.println();
            writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.type"), parameter.getType(), w);

            if (StringUtils.isNotEmpty(parameter.getSince())) {
                writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.since"), parameter.getSince(), w);
            } else {
                if (StringUtils.isNotEmpty(mojoDescriptor.getSince())) {
                    writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.since"), mojoDescriptor.getSince(),
                            w);
                }
            }

            if (parameter.isRequired()) {
                writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.required"), getString("pluginasciidoc.yes"),
                        w);
            } else {
                writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.required"), getString("pluginasciidoc.no"),
                        w);
            }

            String expression = parameter.getExpression();
            String property = getPropertyFromExpression(expression);
            if (property == null) {
                writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.expression"), expression, w);
            } else {
                writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.property"), property, w);
            }

            writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.default"),
                    escapeXml(parameter.getDefaultValue()), w);

            writeDetail(getString("pluginasciidoc.mojodescriptor.parameter.alias"), escapeXml(parameter.getAlias()),
                    w);

            w.println();

            if (parameters.hasNext()) {
                w.println();
            }

            w.println();
        }
        w.println();
    }

    private String getPropertyFromExpression(String expression) {
        if (StringUtils.isNotEmpty(expression) && expression.startsWith("${") && expression.endsWith("}")
                && !expression.substring(2).contains("${")) {
            // expression="${xxx}" -> property="xxx"
            return expression.substring(2, expression.length() - 1);
        }
        // no property can be extracted
        return null;
    }

    /**
     * @param param not null
     * @param value could be null
     * @param w not null
     */
    private void writeDetail(String param, String value, PrintWriter w) {
        if (StringUtils.isNotEmpty(value)) {
            w.print("* ");
            w.println(format("pluginasciidoc.detail", new String[]{param, value}));
        }
    }

    /**
     * @param mojoDescriptor not null
     * @param parameterList not null
     * @param w not null
     */
    private void writeParameterSummary(MojoDescriptor mojoDescriptor, List<Parameter> parameterList, PrintWriter w) {
        List<Parameter> requiredParams = getParametersByRequired(true, parameterList);
        if (requiredParams.size() > 0) {
            writeParameterList(mojoDescriptor, getString("pluginasciidoc.mojodescriptor.requiredParameters"),
                    requiredParams, w);
        }

        List<Parameter> optionalParams = getParametersByRequired(false, parameterList);
        if (optionalParams.size() > 0) {
            writeParameterList(mojoDescriptor, getString("pluginasciidoc.mojodescriptor.optionalParameters"),
                    optionalParams, w);
        }
    }

    /**
     * @param mojoDescriptor not null
     * @param title not null
     * @param parameterList not null
     * @param w not null
     */
    private void writeParameterList(MojoDescriptor mojoDescriptor, String title, List<Parameter> parameterList,
            PrintWriter w) {
        w.print(".");
        w.println(title);
        w.println("[%autowidth.stretch]");
        w.println("|====");

        w.print("|");
        w.print(getString("pluginasciidoc.mojodescriptor.parameter.name"));
        w.print("|");
        w.print(getString("pluginasciidoc.mojodescriptor.parameter.type"));
        w.print("|");
        w.print(getString("pluginasciidoc.mojodescriptor.parameter.since"));
        w.print("|");
        w.println(getString("pluginasciidoc.mojodescriptor.parameter.description"));

        for (Parameter parameter : parameterList) {

            w.print("|");
            w.print(format("pluginasciidoc.mojodescriptor.parameter.name_link", parameter.getName()));

            //type
            w.print("|");
            int index = parameter.getType().lastIndexOf(".");
            w.print("`" + parameter.getType().substring(index + 1) + "`");

            // since
            w.print("|");
            if (StringUtils.isNotEmpty(parameter.getSince())) {
                w.print("`" + parameter.getSince() + "`");
            } else {
                if (StringUtils.isNotEmpty(mojoDescriptor.getSince())) {
                    w.print("`" + mojoDescriptor.getSince() + "`");
                } else {
                    w.print("`-`");
                }
            }

            // description
            w.print("|");
            String description;
            if (StringUtils.isNotEmpty(parameter.getDeprecated())) {
                description = format("pluginasciidoc.mojodescriptor.parameter.deprecated",
                        makeHtmlValid(parameter.getDeprecated()));
            } else if (StringUtils.isNotEmpty(parameter.getDescription())) {
                description = makeHtmlValid(parameter.getDescription());
            } else {
                description = getString("pluginasciidoc.nodescription");
            }
            w.println(description + " +");

            if (StringUtils.isNotEmpty(parameter.getDefaultValue())) {
                w.print(format("pluginasciidoc.mojodescriptor.parameter.defaultValue",
                        escapeXml(parameter.getDefaultValue())));
                w.println(" +");
            }

            String property = getPropertyFromExpression(parameter.getExpression());
            if (property != null) {
                w.print(format("pluginasciidoc.mojodescriptor.parameter.property.description", property));
                w.println(" +");
            }

            if (StringUtils.isNotEmpty(parameter.getAlias())) {
                w.print(format("pluginasciidoc.mojodescriptor.parameter.alias.description",
                        escapeXml(parameter.getAlias())));
            }
            w.println();
        }
        w.println();
        w.println("|====");
        w.println();
    }

    private String makeHtmlValid(String description) {
        String result = GeneratorUtils.makeHtmlValid(description);
        result = result.replaceAll("<code>", "`");
        result = result.replaceAll("</code>", "`");
        return result;
    }

    /**
     * @param required      <code>true</code> for required parameters, <code>false</code> otherwise.
     * @param parameterList not null
     * @return list of parameters depending the value of <code>required</code>
     */
    private List<Parameter> getParametersByRequired(boolean required, List<Parameter> parameterList) {
        List<Parameter> list = new ArrayList<Parameter>();

        for (Parameter parameter : parameterList) {
            if (parameter.isRequired() == required) {
                list.add(parameter);
            }
        }

        return list;
    }

    /**
     * Gets the resource bundle for the <code>locale</code> instance variable.
     *
     * @return The resource bundle for the <code>locale</code> instance variable.
     */
    private ResourceBundle getBundle() {
        return ResourceBundle.getBundle("pluginasciidoc", locale, getClass().getClassLoader());
    }

    /**
     * @param key not null
     * @return Localized, text identified by <code>key</code>.
     * @see #getBundle()
     */
    private String getString(String key) {
        return getBundle().getString(key);
    }

    /**
     * Convenience method.
     *
     * @param key not null
     * @param arg1 not null
     * @return Localized, formatted text identified by <code>key</code>.
     * @see #format(String, Object[])
     */
    private String format(String key, Object arg1) {
        return format(key, new Object[]{arg1});
    }

    /**
     * Looks up the value for <code>key</code> in the <code>ResourceBundle</code>,
     * then formats that value for the specified <code>Locale</code> using <code>args</code>.
     *
     * @param key not null
     * @param args not null
     * @return Localized, formatted text identified by <code>key</code>.
     */
    private String format(String key, Object[] args) {
        String pattern = getString(key);
        // we don't need quoting so spare us the confusion in the resource bundle to double them up in some keys
        pattern = StringUtils.replace(pattern, "'", "''");

        MessageFormat messageFormat = new MessageFormat("");
        messageFormat.setLocale(locale);
        messageFormat.applyPattern(pattern);

        return messageFormat.format(args);
    }

    /**
     * @param text the string to escape
     * @return A string escaped with XML entities
     */
    private String escapeXml(String text) {
        if (text != null) {
            text = text.replaceAll("&", "&amp;");
            text = text.replaceAll("<", "&lt;");
            text = text.replaceAll(">", "&gt;");
            text = text.replaceAll("\"", "&quot;");
            text = text.replaceAll("\'", "&apos;");
        }
        return text;
    }
}
