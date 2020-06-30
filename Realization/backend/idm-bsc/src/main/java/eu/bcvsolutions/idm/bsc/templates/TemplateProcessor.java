/**
 * CzechIdM
 * Copyright (C) 2015 BCV solutions s.r.o., Czech Republic
 * 
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License 2.1 as published by the Free Software Foundation;
 * 
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free 
 * Software Foundation, Inc., 51 Franklin Street, Fifth Floor, 
 * Boston, MA 02110-1301 USA
 * 
 * You can contact us on website http://www.bcvsolutions.eu.
 */

package eu.bcvsolutions.idm.bsc.templates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class TemplateProcessor {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(TemplateProcessor.class);

	/**
	 * Generates byte array filling given template with parameters from map.
	 * @param template - template name
	 * @param params - map of parameters to fill in
	 * @param lang - template language
	 * @return file contents
	 * @throws IOException 
	 */
	public abstract byte[] generateByteA(String template, Map params, String lang) throws IOException;
	
	/**
	 * Generates byte array filling given template with parameters from map.
	 * @param template - template name
	 * @param params - map of parameters to fill in
	 * @return file contents
	 * @throws IOException 
	 */
	public byte[] generateByteA(String template, Map params) throws IOException {
		return generateByteA(template, params, null);
	}
	
	/**
	 * Generates file filling given template with parameters from map.
	 * @param template - template name
	 * @param params - map of parameters to fill in
	 * @param lang - template language
	 * @return filename
	 * @throws IOException 
	 */
	public abstract String generateFile(String template, Map params, String lang) throws IOException;
	
	
	/**
	 * Generates file filling given template with parameters from map.
	 * @param template - template name
	 * @param params - maps of parameters to fill in
	 * @return filename
	 * @throws IOException 
	 */
	public String generateFile(String template, Map params) throws IOException {
		return generateFile(template, params, null);
	}
	
	/**
	 * Finds template in repository.
	 * If template of given language is not found, returns default language template.
	 * Return null, if no template is found.
	 * 
	 * @param templateName
	 * @param language
	 * 
	 * @return
	 */
	protected static String findFileTemplateView(String templateName, String language) {
//		TODO make configurable
		try (Stream<String> fileStream = Files.lines(Paths.get("C:\\work\\modules\\czechidm-business-card\\Realization\\backend\\idm-bsc\\src\\main\\resources\\eu\\bcvsolutions\\idm\\fileTemplate\\fop-businessCard.xml"))){
			return fileStream.collect(Collectors.joining("\n"));
		} catch (IOException e) {
			LOG.error("Can't load template", e);
		}
		return null;
	}

	/**
	 * Convert given value to string
	 * @param obj - object to string
	 * @param name - for more complex expansion of containers
	 * @return string
	 */
	protected String valueToString(String name, Object obj) {
		if (obj == null)
			return "";
		return obj.toString();
	}
}