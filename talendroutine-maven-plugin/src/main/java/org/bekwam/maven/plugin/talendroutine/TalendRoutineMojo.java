package org.bekwam.maven.plugin.talendroutine;

/*
 * Copyright (C) 2011-2014 Bekwam, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * Goal which creates manifest information needed in a Talend Open Studio
 * Routines archive file
 *
 * @author walkerca
 * @since 1.0.0
 */
@Mojo( name = "generate", defaultPhase = LifecyclePhase.PROCESS_SOURCES )
public class TalendRoutineMojo extends AbstractMojo {
	
    private final static String PROJECT_FILE_NAME = "talend.project";
    
	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	@Parameter( defaultValue="${project}", property="project", required=true)
	private MavenProject project;
	
	/**
     * Location of the file.
     */
    @Parameter( defaultValue = "${project.build.directory}", property = "outputDir", required = true )
    private File outputDir;

    /**
     * Label used in Routine name display
     */
    @Parameter(defaultValue="${project.name}", property="label", required=true)
    private String label;
    
    /**
     * Description used in Routine description display
     */
    @Parameter(defaultValue="Talend routines", property="description", required=true)
    private String description;

    /**
     * Purpose used in Routine purpose display
     */
    @Parameter(defaultValue="Talend routines", property="purpose", required=true)
    private String purpose;
    
    /**
     * Major version of Routine
     */
    @Parameter(defaultValue="1", property="majorVersion", required=true)
    private int majorVersion;
    
    /**
     * Minor version of Routine
     */
    @Parameter(defaultValue="0", property="minorVersion", required=true)
    private int minorVersion;
    
    /**
     * Folder containing Routine
     */
    @Parameter(defaultValue="routines",property="path",required=true)
    private String path;
    
    public void execute() throws MojoExecutionException {
    	
        File f = outputDir;

        if ( !f.exists() ) {
            f.mkdirs();
        }

        String versionString = majorVersion + "." + minorVersion;        
        String filename = label + "_" + majorVersion + "." + minorVersion + ".properties";
        String xmiId = "_" + RandomStringUtils.randomAlphanumeric(22);
        
        getLog().debug("generating filename=" + filename );
        getLog().debug("using xmiId=" + xmiId);
        File propertiesFile = new File( f, filename );
        File talendProjectFile = new File( f, PROJECT_FILE_NAME );
        
        FileWriter w = null;
        BufferedWriter bw = null;
        
        try {
        	
            w = new FileWriter( propertiesFile );
            bw = new BufferedWriter( w );
            
            DateTime dt = new DateTime();
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            String date_s= fmt.print(dt);

            bw.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ); bw.newLine();
            bw.write( "<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:TalendProperties=\"http://www.talend.org/properties\">" );  bw.newLine();
            bw.write( "  <TalendProperties:Property xmi:id=\"" + xmiId + "\" id=\"_NkZ6AJEEEeC4zfWT--Xipg\" label=\"" + label + "\" purpose=\"" + purpose + "\" description=\"" + description + "\" creationDate=\"" + date_s + "\" modificationDate=\"" + date_s + "\" version=\"" + versionString + "\" statusCode=\"PROD\" item=\"_Ebj3Mg4sEeGtbb8OJdAnvg\"> ");  bw.newLine();
            bw.write( "    <author href=\"../../../talend.project#" + xmiId + "\"/>" );  bw.newLine();
            bw.write( "  </TalendProperties:Property>" );  bw.newLine();
            bw.write( "  <TalendProperties:ItemState xmi:id=\"" + "\" path=\"" + path + "\"/>" );  bw.newLine();
            bw.write( "  <TalendProperties:RoutineItem xmi:id=\"" + xmiId + "\" property=\"" + xmiId + "\" state=\"" + xmiId + "\"> ");  bw.newLine();
            bw.write( "    <content href=\"" + label + "_" + versionString + ".item#/0\"/>" );  bw.newLine();
            
            Set<Artifact> artifacts = project.getDependencyArtifacts();
            
            for( Artifact a : artifacts ) {

            	if( !StringUtils.equals( a.getScope(), "test") && 
            			StringUtils.equals( a.getType(), "jar" ) ) {
            		
            		String jarName = a.getArtifactId() + "-" + a.getVersion() + ".jar";
            		String message = a.getGroupId() + ":" + a.getArtifactId();

            		getLog().debug("jarName=" + jarName + ", message=" + message);
            		
            		bw.write( "    <imports xmi:id=\"" + xmiId + "\" mESSAGE=\"" + message + "\" mODULE=\"" + jarName + "\" nAME=\"" + a.getArtifactId() + "\" rEQUIRED=\"true\" />" );  bw.newLine();
            	}
            }
            
            bw.write( "    </TalendProperties:RoutineItem>" );  bw.newLine();
            bw.write( "</xmi:XMI>");  bw.newLine();
        }
        catch ( IOException e ) {
            throw new MojoExecutionException( "Error creating file " + propertiesFile, e );
        }
        finally {
            if ( bw != null ) {
                try {
                    bw.close();
                }
                catch ( IOException e ) {
                	getLog().warn("error closing buffered writer for file=" + filename);
                }
            }
        }
        
        try {
            w = new FileWriter( talendProjectFile );
            bw = new BufferedWriter( w );
            
            bw.write( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" ); bw.newLine();            
            bw.write( "<xmi:XMI xmi:version=\"2.0\" xmlns:xmi=\"http://www.omg.org/XMI\" xmlns:TalendProperties=\"http://www.talend.org/properties\">" ); bw.newLine();
            bw.write( "  <TalendProperties:Project xmi:id=\"" + xmiId + "\" label=\"TALENDPROJECT\" description=\"Project for testing new components\" language=\"java\" technicalLabel=\"TALENDPROJECT\" local=\"true\" productVersion=\"Talend Open Studio-4.2.2.r63143\" itemsRelationVersion=\"1.1\">" ); bw.newLine();   
            bw.write( "  </TalendProperties:Project>" ); bw.newLine();
            bw.write( "  <TalendProperties:User xmi:id=\"" + xmiId + "\" login=\"exportuser@talend.com\"/>" ); bw.newLine();
            bw.write( "</xmi:XMI>" ); bw.newLine();

        }
        catch ( IOException e ) {
            throw new MojoExecutionException( "Error creating file " + talendProjectFile, e );
        }
        finally {
            if ( bw != null ) {
                try {
                    bw.close();
                }
                catch ( IOException e ) {
                	getLog().warn("error closing buffered writer for file=" + filename);
                }
            }
        }
        	
    }
    
}
