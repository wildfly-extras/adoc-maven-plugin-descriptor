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

import org.apache.maven.plugin.plugin.AbstractGeneratorMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.tools.plugin.generator.Generator;

/**
 * Generates a <code>AdocMojo</code> class.
 * @author Emmanuel Hugonnet (c) 2018 Red Hat, inc.
 */
@Mojo(name = "asciidoc-descriptor", defaultPhase = LifecyclePhase.PROCESS_CLASSES, threadSafe = true,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class AdocGeneratorMojo
        extends AbstractGeneratorMojo {

    /**
     * The directory where the generated <code>HelpMojo</code> file will be put.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/plugin")
    protected File outputDirectory;

    /**
     * {@inheritDoc}
     */
    protected File getOutputDirectory() {
        return outputDirectory;
    }

    /**
     * {@inheritDoc}
     */
    protected Generator createGenerator() {
        return new PluginAdocGenerator();
    }

    /**
     * {@inheritDoc}
     */
    public void execute()
            throws MojoExecutionException {
        // force value for this plugin
        skipErrorNoDescriptorsFound = true;

        super.execute();

        if (!project.getCompileSourceRoots().contains(outputDirectory.getAbsolutePath()) && !skip) {
            project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
        }

    }

}
