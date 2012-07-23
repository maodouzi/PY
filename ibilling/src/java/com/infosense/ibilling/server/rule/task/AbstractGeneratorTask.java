/*
 * JBILLING CONFIDENTIAL
 * _____________________
 *
 * [2003] - [2012] Enterprise jBilling Software Ltd.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Enterprise jBilling Software.
 * The intellectual and technical concepts contained
 * herein are proprietary to Enterprise jBilling Software
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden.
 */

package com.infosense.ibilling.server.rule.task;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.xmlrules.DigesterLoader;
import org.apache.log4j.Logger;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;
import org.drools.util.DroolsStreamUtils;
import org.xml.sax.SAXException;

import com.infosense.ibilling.common.Util;
import com.infosense.ibilling.server.pluggableTask.PluggableTask;
import com.infosense.ibilling.server.pluggableTask.TaskException;
import com.infosense.ibilling.server.pluggableTask.admin.ParameterDescription;

/**
 * Provides implementation for parsing the rulesGenerate input data as
 * XML using Digester. Also compiles the rules generated by the 
 * subclass. Relative path names are assumed to be from the 
 * 'jbilling/resource/rules' directory.
 *
 * Parameters: 
 *  - 'config_filename' (default: 'rules-generator-config.xml') - the 
 *    configuration file. 
 *  - 'output_filename' - where the compiled package will be saved.
 */
public abstract class AbstractGeneratorTask extends PluggableTask 
        implements IRulesGenerator {

	public static final ParameterDescription PARAM_CONFIG_FILENAME = 
		new ParameterDescription("config_filename", false, ParameterDescription.Type.STR);
	
	public static final ParameterDescription PARAM_OUTPUT_FILENAME = 
		new ParameterDescription("output_filename", true, ParameterDescription.Type.STR);
	
    private static final String PARAM_CONFIG_FILENAME_DEFAULT = "rules-generator-config.xml";

    private static final Logger LOG = Logger.getLogger(AbstractGeneratorTask.class);

    private Object data;
    private String rules;

    public AbstractGeneratorTask() {
        super();
    }

    //initializer for pluggable params
    { 
    	descriptions.add(PARAM_CONFIG_FILENAME);
        descriptions.add(PARAM_OUTPUT_FILENAME);
    }

    
    /**
     * Parses and validates the XML input data.
     */
    public void unmarshal(String objects) throws TaskException {
        try {
            // get the configuration filename
            String configFilename = PARAM_CONFIG_FILENAME_DEFAULT;
            if (parameters.get(PARAM_CONFIG_FILENAME.getName()) != null) {
                configFilename = (String) parameters.get(PARAM_CONFIG_FILENAME.getName());
            }

            // if the filename is relative, prepend default directory
            configFilename = getAbsolutePath(configFilename);
            LOG.debug("Config filename: " + configFilename);

            // load the parsing rules
            Digester digester = DigesterLoader.createDigester(new URL("file:///" + configFilename));
            digester.setUseContextClassLoader(true);

            // parse the input string
            //digester.setValidating(true);
            data = digester.parse(new StringReader(objects));
        } catch (IOException ioe) {
            LOG.error("Error parsing XML file: " + ioe.toString());
            throw new TaskException(ioe);
        } catch (SAXException saxe) {
            LOG.error("Error parsing XML file: " + saxe.toString());
            throw new TaskException(saxe);
        }
    }

    /**
     * Compiles and saves the rules. Calls the subclass to generate 
     * the rules.
     */
    public void process() throws TaskException {
        // get the subclass to generate the rules from the data
        rules = generateRules(data);

        LOG.debug("Generated rules: \n" + rules);

        // compile the rules into a binary package
        PackageBuilder builder = new PackageBuilder();
        try {
            builder.addPackageFromDrl(new StringReader(rules));
        } catch (Exception e) {
            LOG.error("Error building drools package.");
            throw new TaskException(e);
        }
        if(builder.hasErrors()) {
            String error = "Error building drools package: \n" + 
                    builder.getErrors().toString();
            LOG.error(error);
            throw new TaskException(error);
        }

        Package pkg = builder.getPackage();

        // get output filename
        if (parameters.get(PARAM_OUTPUT_FILENAME.getName()) == null) {
            throw new TaskException("No '" + PARAM_OUTPUT_FILENAME.getName() + 
                    "' parameter specified.");
        }
        String outputFilename = (String) parameters.get(PARAM_OUTPUT_FILENAME.getName());
        // if the filename is relative, prepend default directory
        outputFilename = getAbsolutePath(outputFilename);
        LOG.debug("Output filename: " + outputFilename);

        // write to file
        //ObjectOutputStream out = null;
        FileOutputStream fout = null;
        try {
            //out = new ObjectOutputStream(new FileOutputStream(outputFilename));
            //out.writeObject(pkg);
            fout = new FileOutputStream(outputFilename);
            DroolsStreamUtils.streamOut(fout, pkg);
        } catch (FileNotFoundException fnfe) {
            LOG.error("Error writing rules package file.");
            throw new TaskException(fnfe);
        } catch (IOException ioe) {
            LOG.error("Error writing rules package file.");
            throw new TaskException(ioe);
        } finally {
            if (fout != null) {
                try {
                    fout.close();
                } catch (IOException ioe) {
                }
            }
        }
    }

    /**
     * Subclass implements this method to generate the rules to be 
     * compiled. Gets passed the objects created by Digester.
     */
    protected abstract String generateRules(Object objects) 
            throws TaskException;

    /**
     * Prepends default directory if filename is relative.
     */
    protected String getAbsolutePath(String filename) {
        if (!(new File(filename)).isAbsolute()) {
            filename = Util.getSysProp("base_dir") + "rules/" + 
                filename;
        }
        return filename;
    }

    /**
     * For testing
     */
    public Object getData() {
        return data;
    }

    /**
     * For testing
     */
    public String getRules() {
        return rules;
    }
}