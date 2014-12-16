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
 * Filename: DockerfileMojo.java
 *
 * Author: Jose San Leandro Armendariz.
 *
 * Description: Executes Dockerfile plugin.
 */
package org.acmsl.dockerfile.maven;

/*
 * Importing some ACM-SL Java Commons classes.
 */
import org.acmsl.commons.logging.UniqueLogFactory;
import org.acmsl.commons.utils.io.FileUtils;

/*
 * Importing some Maven classes.
 */
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.deploy.AbstractDeployMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/*
 * Importing NotNull annotations.
 */
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Importing some JDK classes.
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/*
 * Importing checkthread.org annotations.
 */
import org.checkthread.annotations.ThreadSafe;

/**
 * Executes Dockerfile plugin.
 * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
 * Created: 2014/12/01
 */
@SuppressWarnings("unused")
@ThreadSafe
@Mojo(name = Literals.DOCKERFILE_L, defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true, executionStrategy = "once-per-session")
public class DockerfileMojo
    extends AbstractDeployMojo
{
    /**
     * The repo syntax pattern.
     */
    private static final Pattern ALT_REPO_SYNTAX_PATTERN = Pattern.compile("(.+)::(.+)::(.+)");

    /**
     * The location of pom.properties within the jar file.
     */
    protected static final String POM_PROPERTIES_LOCATION =
        "META-INF/maven/org.acmsl/dockerfile-maven-plugin/pom.properties";

    /**
     * The output directory.
     */
    @Parameter (name = Literals.OUTPUT_DIR_CC, property = Literals.OUTPUT_DIR_CC, required = false, defaultValue = "${project.build.outputDirectory}/META-INF/")
    private File m__OutputDir;

    /**
     * The output directory.
     */
    @Parameter (name = Literals.TEMPLATE_L, property = Literals.TEMPLATE_L, required = true)
    private File m__Template;

    /**
     * The file encoding.
     */
    @Parameter (name = Literals.ENCODING_L, property = Literals.ENCODING_L, required = false, defaultValue = "${project.build.sourceEncoding}")
    private String m__strEncoding;

    /**
     * Server Id to map on the &lt;id&gt; under &lt;server&gt; section of settings.xml In most cases, this parameter
     * will be required for authentication.
     */
    @Parameter( property = "repositoryId", defaultValue = "remote-repository", required = true )
    private String repositoryId;

    /**
     * URL where the artifact will be deployed.
     * ie ( file:///C:/m2-repo or scp://host.com/path/to/repo )
     */
    @Parameter( property = "url", required = true )
    private String url;

    /**
     * Whether to deploy snapshots with a unique version or not.
     */
    @Parameter( property = "uniqueVersion", defaultValue = "true" )
    private boolean uniqueVersion;

    /**
     * Map that contains the layouts.
     */
    @Component( role = ArtifactRepositoryLayout.class )
    private Map<String, ArtifactRepositoryLayout> repositoryLayouts;

    /**
     * The current build session instance. This is used for toolchain manager API calls.
     * @readonly
     */
    @Parameter (defaultValue = "${session}", required = true, readonly = true)
    private MavenSession session;

    /**
     */
    @Component
    private ArtifactDeployer deployer;

    /**
     * Component used to create an artifact.
     */
    @Component
    protected ArtifactFactory artifactFactory;

    /**
     * Component used to create a repository.
     */
    @Component
    ArtifactRepositoryFactory repositoryFactory;

    /**
     * Specifies an alternative repository to which the project artifacts should be deployed ( other than those
     * specified in &lt;distributionManagement&gt; ). <br/>
     * Format: id::layout::url
     * <dl>
     * <dt>id</dt>
     * <dd>The id can be used to pick up the correct credentials from the settings.xml</dd>
     * <dt>layout</dt>
     * <dd>Either <code>default</code> for the Maven2 layout or <code>legacy</code> for the Maven1 layout. Maven3 also
     * uses the <code>default</code> layout.</dd>
     * <dt>url</dt>
     * <dd>The location of the repository</dd>
     * </dl>
     */
    @Parameter( property = "altDeploymentRepository" )
    private String altDeploymentRepository;

    /**
     * The alternative repository to use when the project has a snapshot version.
     * 
     * @since 2.8
     * @see DeployMojo#altDeploymentRepository
     */
    @Parameter( property = "altSnapshotDeploymentRepository" )
    private String altSnapshotDeploymentRepository;

    /**
     * The alternative repository to use when the project has a final version.
     * 
     * @since 2.8
     * @see DeployMojo#altDeploymentRepository
     */
    @Parameter( property = "altReleaseDeploymentRepository" )
    private String altReleaseDeploymentRepository;

    /**
     * Specifies the output directory.
     * @param outputDir such directory.
     */
    protected final void immutableSetOutputDir(@NotNull final File outputDir)
    {
        m__OutputDir = outputDir;
    }

    /**
     * Specifies the output directory.
     * @param outputDir such directory.
     */
    public void setOutputDir(@NotNull final File outputDir)
    {
        immutableSetOutputDir(outputDir);
    }

    /**
     * Returns the output directory.
     * @return such directory.
     */
    @Nullable
    protected final File immutableGetOutputDir()
    {
        return m__OutputDir;
    }

    /**
     * Returns the output directory.
     * @return such directory.
     */
    @Nullable
    public File getOutputDir()
    {
        @Nullable final File result;

        @Nullable final String aux = System.getProperty(Literals.DOCKERFILE_OUTPUT_DIR);

        if (aux == null)
        {
            result = immutableGetOutputDir();
        }
        else
        {
            result = new File(aux);
        }

        return result;
    }

    /**
     * Specifies the template.
     * @param template such template.
     */
    protected final void immutableSetTemplate(@NotNull final File template)
    {
        m__Template = template;
    }

    /**
     * Specifies the template.
     * @param template such template.
     */
    public void setTemplate(@NotNull final File template)
    {
        immutableSetTemplate(template);
    }

    /**
     * Returns the template.
     * @return such template.
     */
    @NotNull
    protected final File immutableGetTemplate()
    {
        return m__Template;
    }

    /**
     * Returns the template.
     * @return such template.
     */
    @NotNull
    public File getTemplate()
    {
        @NotNull final File result;

        @Nullable final String aux = System.getProperty(Literals.DOCKERFILE_TEMPLATE);

        if (aux == null)
        {
            result = immutableGetTemplate();
        }
        else
        {
            result = new File(aux);
        }

        return result;
    }

    /**
     * Specifies the encoding.
     * @param encoding the encoding.
     */
    protected final void immutableSetEncoding(@NotNull final String encoding)
    {
        m__strEncoding = encoding;
    }

    /**
     * Specifies the encoding.
     * @param encoding the encoding.
     */
    public void setEncoding(@NotNull final String encoding)
    {
        immutableSetEncoding(encoding);
    }

    /**
     * Retrieves the encoding.
     * @return such information.
     */
    @Nullable
    protected final String immutableGetEncoding()
    {
        return m__strEncoding;
    }

    /**
     * Retrieves the encoding.
     * @return such information.
     */
    @Nullable
    public String getEncoding()
    {
        @Nullable String result = System.getProperty(Literals.DOCKERFILE_ENCODING);

        if (result == null)
        {
            result = immutableGetEncoding();
        }

        return result;
    }

    /**
     * Retrieves the layout.
     * @param id the id.
     * @return the layout.
     */
    @NotNull
    public ArtifactRepositoryLayout getLayout(@NotNull final String id)
        throws MojoExecutionException
    {
        @Nullable final ArtifactRepositoryLayout result = repositoryLayouts.get(id);

        if (result == null)
        {
            throw new MojoExecutionException( "Invalid repository layout: " + id );
        }

        return result;
    }

    /**
     * Executes Dockerfile Maven plugin.
     * @throws org.apache.maven.plugin.MojoExecutionException if the process fails.
     */
    @Override
    public void execute()
        throws MojoExecutionException
    {
        execute(getLog());
    }

    /**
     * Executes Dockerfile Maven plugin.
     * @param log the Maven log.
     * @throws MojoExecutionException if the process fails.
     */
    protected void execute(@NotNull final Log log)
        throws MojoExecutionException
    {
        execute(
            log,
            retrieveOwnVersion(retrievePomProperties(log)),
            retrieveTargetProject(),
            getOutputDir(),
            getTemplate(),
            getEncoding());
    }

    /**
     * Retrieves the version of Dockerfile Maven Plugin currently running.
     * @param properties the pom.properties information.
     * @return the version entry.
     */
    @NotNull
    protected String retrieveOwnVersion(@Nullable final Properties properties)
    {
        @NotNull final String result;

        if (   (properties != null)
            && (properties.containsKey(Literals.VERSION_L)))
        {
            result = properties.getProperty(Literals.VERSION_L);
        }
        else
        {
            result = Literals.UNKNOWN_L;
        }

        return result;
    }

    /**
     * Retrieves the target project.
     * @return such version.
     */
    @NotNull
    protected MavenProject retrieveTargetProject()
    {
        return this.session.getCurrentProject();
    }

    /**
     * Executes Dockerfile Maven Plugin.
     * @param log the Maven log.
     * @param ownVersion the Dockerfile Maven Plugin version.
     * @param targetProject the target project.
     * @param outputDir the output dir.
     * @param template the template.
     * @param encoding the file encoding.
     * @throws MojoExecutionException if the process fails.
     */
    protected void execute(
        @NotNull final Log log,
        @NotNull final String ownVersion,
        @NotNull final MavenProject targetProject,
        @Nullable final File outputDir,
        @Nullable final File template,
        @Nullable final String encoding)
      throws MojoExecutionException
    {
        boolean running = false;

        boolean outputDirFine = false;
        boolean templateFine = false;

        if (outputDir != null)
        {
            if (   (!outputDir.exists())
                && (!outputDir.mkdirs()))
            {
                log.warn("Cannot create output folder: " + outputDir);
            }
            else
            {
                outputDirFine = true;
            }
        }
        else
        {
            log.error(Literals.OUTPUT_DIR_CC + " is null");
        }

        if (template != null)
        {
            if (!template.exists())
            {
                log.warn("Dockerfile template does not exist: " + template);
            }
            else
            {
                templateFine = true;
            }
        }
        else
        {
            log.error(Literals.TEMPLATE_L + " is null");
        }

        @NotNull final Charset actualEncoding;

        if (encoding == null)
        {
            actualEncoding = Charset.defaultCharset();

            log.warn(Literals.ENCODING_L + " not specified. Using " + actualEncoding);
        }
        else
        {
            actualEncoding = Charset.forName(encoding);
        }

        if (   (outputDirFine)
            && (templateFine))
        {
            log.info(
                  "Running Dockerfile Maven Plugin " + ownVersion
                + " on " + targetProject.getGroupId() + ":" + targetProject.getArtifactId()
                + ":" + targetProject.getVersion());

            running = true;

            @NotNull File dockerfile = null;

            try
            {
                dockerfile =
                    generateDockerfile(
                        outputDir,
                        template,
                        targetProject,
                        ownVersion,
                        actualEncoding,
                        FileUtils.getInstance());
            }
            catch (@NotNull final SecurityException securityException)
            {
                log.error("Not allowed to write output file in " + outputDir.getAbsolutePath(), securityException);
            }
            catch (@NotNull final IOException ioException)
            {
                log.error("Cannot write output file in " + outputDir.getAbsolutePath(), ioException);
            }

            if (true) // deploy
            {
                try
                {
                    @NotNull final Artifact artifact =
                        artifactFactory.createArtifactWithClassifier(
                            targetProject.getGroupId(),
                            targetProject.getArtifactId(),
                            targetProject.getVersion(),
                            "",
                            "Dockerfile");

                    @NotNull final ArtifactRepositoryLayout layout = getLayout("default");

                    ArtifactRepository repo =
                        getDeploymentRepository(
                            targetProject,
                            altDeploymentRepository,
                            altReleaseDeploymentRepository,
                            altSnapshotDeploymentRepository);

                    @NotNull final ArtifactRepository deploymentRepository =
                        repositoryFactory.createDeploymentArtifactRepository(
                            repositoryId, url, layout, uniqueVersion);

                    deploy(
                        dockerfile,
                        artifact,
                        deploymentRepository,
                        getLocalRepository(),
                        3); //getRetryFailedDeploymentCount());
                }
                catch (@NotNull final ArtifactDeploymentException e)
                {
                    throw new MojoExecutionException("Error deploying Dockerfile", e);
                }
                catch (@NotNull final MojoFailureException e)
                {
                    throw new MojoExecutionException("Error deploying Dockerfile", e);
                }
            }
        }

        if (!running)
        {
            log.error("NOT running Dockerfile Maven Plugin " + ownVersion);
            throw new MojoExecutionException("Dockerfile Maven Plugin could not start");
        }
    }

    /**
     * Retrieves the pom.properties bundled within the Dockerfile Maven Plugin jar.
     * @param log the Maven log.
     * @return such information.
     */
    @Nullable
    protected Properties retrievePomProperties(@NotNull final Log log)
    {
        @Nullable Properties result = null;

        try
        {
            @Nullable final InputStream pomProperties =
                getClass().getClassLoader().getResourceAsStream(POM_PROPERTIES_LOCATION);

            if (pomProperties != null)
            {
                result = new Properties();

                result.load(pomProperties);
            }
        }
        catch (@NotNull final IOException ioException)
        {
            log.warn(
                Literals.CANNOT_READ_MY_OWN_POM + POM_PROPERTIES_LOCATION,
                ioException);
        }

        return result;
    }

    /**
     * Initializes the logging.
     * @param commonsLoggingLog such log.
     */
    protected void initLogging(@NotNull final org.apache.commons.logging.Log commonsLoggingLog)
    {
        UniqueLogFactory.initializeInstance(commonsLoggingLog);
    }

    /**
     * Generates the dockerfile.
     * @param outputDir the output path.
     * @param template the Dockerfile.stg template.
     * @param target the target project.
     * @param ownVersion my own version.
     * @param encoding the file encoding.
     * @param fileUtils the {@link FileUtils} instance.
     * @return the generated file.
     * @throws IOException if the file cannot be written.
     * @throws SecurityException if we're not allowed to write the file.
     */
    protected File generateDockerfile(
        @NotNull final File outputDir,
        @NotNull final File template,
        @NotNull final MavenProject target,
        @NotNull final String ownVersion,
        @NotNull final Charset encoding,
        @NotNull final FileUtils fileUtils)
      throws IOException,
             SecurityException
    {
        @NotNull final File result;

        @NotNull final Map<String, Object> input = new HashMap<String, Object>();

        input.put(Literals.T_U, target);
        input.put(Literals.VERSION_L, ownVersion);

        @NotNull final DockerfileGenerator generator = new DockerfileGenerator(input, template);

        @NotNull final String contents = generator.generateDockerfile();

        result = new File(outputDir.getAbsolutePath() + File.separator + "Dockerfile");

        fileUtils.writeFile(
            result,
            contents,
            encoding);

        return result;
    }

    /**
     * Retrieves the deployment repository.
     * @param project the project.
     * @param altDeploymentRepository the deployment repository.
     * @param altReleaseDeploymentRepository the release repository.
     * @param altSnapshotDeploymentRepository the snapshot repository.
     * @return the repository.
     */
    protected ArtifactRepository getDeploymentRepository(
        @NotNull final MavenProject project,
        @NotNull final String altDeploymentRepository,
        @NotNull final String altReleaseDeploymentRepository,
        @NotNull final String altSnapshotDeploymentRepository)
      throws MojoExecutionException,
             MojoFailureException
    {
        ArtifactRepository repo = null;

        String altDeploymentRepo;

        if  (ArtifactUtils.isSnapshot( project.getVersion() ) && altSnapshotDeploymentRepository != null)
        {
            altDeploymentRepo = altSnapshotDeploymentRepository;
        }
        else if ( !ArtifactUtils.isSnapshot( project.getVersion() ) && altReleaseDeploymentRepository != null )
        {
            altDeploymentRepo = altReleaseDeploymentRepository;
        }
        else
        {
            altDeploymentRepo = altDeploymentRepository;
        }

        if ( altDeploymentRepo != null )
        {
            getLog().info( "Using alternate deployment repository " + altDeploymentRepo );

            Matcher matcher = ALT_REPO_SYNTAX_PATTERN.matcher( altDeploymentRepo );

            if ( !matcher.matches() )
            {
                throw new MojoFailureException( altDeploymentRepo, "Invalid syntax for repository.",
                                                "Invalid syntax for alternative repository. Use \"id::layout::url\"." );
            }
            else
            {
                String id = matcher.group( 1 ).trim();
                String layout = matcher.group( 2 ).trim();
                String url = matcher.group( 3 ).trim();

                ArtifactRepositoryLayout repoLayout = getLayout( layout );

                repo = repositoryFactory.createDeploymentArtifactRepository( id, url, repoLayout, true );
            }
        }

        if ( repo == null )
        {
            repo = project.getDistributionManagementArtifactRepository();
        }

        if ( repo == null )
        {
            String msg =
                "Deployment failed: repository element was not specified in the POM inside"
                    + " distributionManagement element or in -DaltDeploymentRepository=id::layout::url parameter";

            throw new MojoExecutionException( msg );
        }

        return repo;
    }
}
