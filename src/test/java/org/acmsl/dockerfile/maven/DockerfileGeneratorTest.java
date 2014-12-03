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
 * Filename: DockerfileGeneratorTes.java
 *
 * Author: Jose San Leandro Armendariz.
 *
 * Description: Tests for DockerfileGenerator.
 */
package org.acmsl.dockerfile.maven;

/*
 * Importing JDK classes.
 */
import java.util.Date;
import java.util.Map;
import java.util.HashMap;

/*
 * Importing NotNull annotations.
 */
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/*
 * Importing checkthread.org annotations.
 */
import org.checkthread.annotations.ThreadSafe;

/*
 * Importing JUnit classes.
 */
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Test;

/**
 * Tests for {@link DockerfileGenerator}.
 * @author <a href="mailto:chous@acm-sl.org">Jose San Leandro Armendariz</a>
 * Created: 2014/12/03
 */
@ThreadSafe
@RunWith(JUnit4.class)
public class DockerfileGeneratorTest
{
    /**
     * Checks whether the generator can find the template.
     */
    @Test
    public void generator_finds_the_template()
    {
        @NotNull final Map<String, String> input = new HashMap<String, String>();

        @NotNull final String testValue = "test-value-" + new Date();

        input.put("key", testValue);

        @NotNull final DockerfileGenerator generator = new DockerfileGenerator(input);

        Assert.assertNotNull(generator);

        @NotNull final String dockerfileContents = generator.generateDockerfile();

        Assert.assertNotNull(dockerfileContents);

        Assert.assertTrue(dockerfileContents.contains(testValue));
    }
}
