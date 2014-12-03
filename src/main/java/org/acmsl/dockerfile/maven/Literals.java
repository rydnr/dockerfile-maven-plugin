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
 * Filename: Literals.java
 *
 * Author: Jose San Leandro Armendariz
 *
 * Description: Literals for Dockerfile Maven Plugin.
 *
 * Date: 2014/12/01
 *
 */
package org.acmsl.dockerfile.maven;

/*
 * Importing checkthread.org annotations.
 */
import org.checkthread.annotations.ThreadSafe;

/**
 * Literals for Dockerfile Maven Plugin.
 * @author <a href="mailto:queryj@acm-sl.org">Jose San Leandro</a>
 * @since 1.0
 * Created: 2014/12/01
 */
@ThreadSafe
public class Literals
{
    /**
     * The string literal: "dockerfile".
     */
    public static final String DOCKERFILE_L = "dockerfile";

    /**
     * String literal: "(unknown)"
     */
    public static final String UNKNOWN_L = "(unknown)";

    /**
     * String literal: "version".
     */
    public static final String VERSION_L = "version";

    /**
     * String literal: "dockerfile.version".
     */
    public static final String DOCKERFILE_VERSION = DOCKERFILE_L + ".version";

    /**
     * String literal: "outputDir".
     */
    public static final String OUTPUT_DIR_CC = "outputDir";

    /**
     * String literal: "dockerfile.outputdir".
     */
    public static final String DOCKERFILE_OUTPUT_DIR = DOCKERFILE_L + ".outputdir";

    /**
     * String literal: "Cannot read my own pom".
     */
    public static final String CANNOT_READ_MY_OWN_POM = "Cannot read my own pom";

    /**
     * String literal: "source".
     */
    public static final String SOURCE_L = "source";

    /**
     * String literal: "C".
     */
    public static final String C_U = "C";

    /**
     * String literal: "org/acmsl/dockerfile".
     */
    public static final String ORG_ACMSL_DOCKERFILE = "org/acmsl/dockerfile";

    /**
     * String literal: "org/acmsl/dockerfile/Dockerfile.stg".
     */
    public static final String ORG_ACMSL_DOCKERFILE_DOCKERFILE_STG = "org/acmsl/dockerfile/Dockerfile.stg";
}
