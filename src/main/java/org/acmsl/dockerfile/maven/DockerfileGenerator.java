/*
                        Dockerfile Maven Plugin

    Copyright (C) 2014-today  Jose San Leandro Armendariz
                              chous@acm-sl.org

    This library is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    version 2 of the License, or any later version.

    This library is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General Public License for more details.

    You should have received a copy of the GNU General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

    Thanks to ACM S.L. for distributing this library under the GPL license.
    Contact info: jose.sanleandro@acm-sl.com

 ******************************************************************************
 *
 * Filename: DockerfileGenerator.java
 *
 * Author: Jose San Leandro Armendariz.
 *
 * Description: Generates Dockerfile files using StringTemplate.
 */
package org.acmsl.dockerfile.maven;

/*
 * Importing StringTemplate classes.
 */
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STErrorListener;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupDir;
import org.stringtemplate.v4.STGroupFile;
import org.stringtemplate.v4.misc.STMessage;

/*
 * Importing some ACM-SL Java Commons classes.
 */
import org.acmsl.commons.logging.UniqueLogFactory;

/*
 * Importing Apache Commons Logging classes.
 */
import org.apache.commons.logging.Log;

/*
 * Importing NotNull annotations.
 */
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Importing some JDK classes.
 */
import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * Importing checkthread.org annotations.
 */
import org.checkthread.annotations.ThreadSafe;

/**
 * Generates Dockerfile files using StringTemplate.
 * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
 * Created: 2014/12/03
 */
@ThreadSafe
public class DockerfileGenerator
{
    /**
     * The default StringTemplate error listener.
     */
    @NotNull
    protected static final STErrorListener ST_ERROR_LISTENER =
        new STErrorListener()
        {
            /**
             * {@inheritDoc}
             */
            @Override
            public void compileTimeError(@NotNull final STMessage stMessage)
            {
                @Nullable final Log log = UniqueLogFactory.getLog(DockerfileGenerator.class);

                if (log != null)
                {
                    log.error(stMessage.toString());
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void runTimeError(@NotNull final STMessage stMessage)
            {
                @Nullable final Log log = UniqueLogFactory.getLog(DockerfileGenerator.class);

                if (log != null)
                {
                    log.error(stMessage.toString());
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void IOError(@NotNull final STMessage stMessage)
            {
                @Nullable final Log log = UniqueLogFactory.getLog(DockerfileGenerator.class);

                if (log != null)
                {
                    log.error(stMessage.toString());
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void internalError(@NotNull final STMessage stMessage)
            {
                @Nullable final Log log = UniqueLogFactory.getLog(DockerfileGenerator.class);

                if (log != null)
                {
                    log.error(stMessage.toString());
                }
            }
        };

    /**
     * The input parameters to the template.
     */
    @NotNull
    private final Map<String, ?> m__mInput;

    /**
     * The template file.
     */
    @NotNull
    private final File m__Template;

    /**
     * Creates a new instance.
     * @param input the input.
     * @param template the template.
     */
    public DockerfileGenerator(
        @NotNull final Map<String, ?> input, @NotNull final File template)
    {
        this.m__mInput = input;
        this.m__Template = template;
    }

    /**
     * Retrieves the input parameters.
     * @return such parameters.
     */
    protected final Map<String, ?> immutableGetInput()
    {
        return this.m__mInput;
    }

    /**
     * Retrieves the input parameters.
     * @return such parameters.
     */
    @SuppressWarnings("unused")
    @NotNull
    public Map<String, ?> getInput()
    {
        @NotNull final Map<String, ?> result =
            new HashMap<String, Object>(this.m__mInput);

        return result;
    }

    /**
     * Retrieves the template.
     * @return such file.
     */
    @NotNull
    public File getTemplate()
    {
        return this.m__Template;
    }

    /**
     * Generates a new Dockerfile using given information.
     * @return the Dockerfile content.
     */
    @NotNull
    public String generateDockerfile()
    {
        return generateDockerfile(immutableGetInput(), getTemplate());
    }

    /**
     * Generates a new Dockerfile using given information.
     * @param input the input.
     * @param template the template.
     * @return the Dockerfile content.
     */
    @NotNull
    protected String generateDockerfile(
        @NotNull final Map<String, ?> input, @NotNull final File template)
    {
        @NotNull final STGroup templateGroup =
            retrieveGroup(
                template,
                Arrays.asList(Literals.ORG_ACMSL_DOCKERFILE),
                ST_ERROR_LISTENER,
                Charset.defaultCharset());

        @NotNull final ST st = templateGroup.getInstanceOf(Literals.SOURCE_L);

        st.add(Literals.C_U, input);

        return st.render();
    }

    /**
     * Retrieves the string template group.
     * @param template the template.
     * @param lookupPaths the lookup paths.
     * @param errorListener the {@link STErrorListener} instance.
     * @param charset the charset.
     * @return such instance.
     */
    @NotNull
    protected STGroup retrieveGroup(
        @NotNull final File template,
        @NotNull final List<String> lookupPaths,
        @NotNull final STErrorListener errorListener,
        @NotNull final Charset charset)
    {
        return
            configureGroupFile(
                new STGroupFile(template.getAbsolutePath(), charset.displayName()),
                lookupPaths,
                errorListener,
                charset);
    }

    /**
     * Retrieves the string template group.
     * @param path the path.
     * @param lookupPaths the lookup paths.
     * @param errorListener the {@link STErrorListener} instance.
     * @param charset the charset.
     * @return such instance.
     */
    @NotNull
    protected STGroup retrieveGroup(
        @NotNull final String path,
        @NotNull final List<String> lookupPaths,
        @NotNull final STErrorListener errorListener,
        @NotNull final Charset charset)
    {
        return
            configureGroupFile(
                new STGroupFile(path, charset.displayName()),
                lookupPaths,
                errorListener,
                charset);
    }

    /**
     * Retrieves the string template group.
     * @param groupFile the group file.
     * @param lookupPaths the lookup paths.
     * @param errorListener the {@link STErrorListener} instance.
     * @param charset the charset.
     * @return such instance.
     */
    @NotNull
    protected STGroup configureGroupFile(
        @NotNull final STGroupFile groupFile,
        @NotNull final List<String> lookupPaths,
        @NotNull final STErrorListener errorListener,
        @NotNull final Charset charset)
    {
        @NotNull final STGroupFile result = groupFile;

        for (@Nullable final String lookupPath : lookupPaths)
        {
            if (lookupPath != null)
            {
                result.importTemplates(new STGroupDir(lookupPath, charset.displayName()));
            }
        }

        result.isDefined(Literals.SOURCE_L);
        result.setListener(errorListener);

        return result;
    }

}
