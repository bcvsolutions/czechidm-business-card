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

import java.util.Map;

public class SimpleProcessor extends TemplateProcessor {
	protected static SimpleProcessor instance = null;
	
	public synchronized static SimpleProcessor getInstance() {
	   if(instance == null) {
		   instance = new SimpleProcessor();
	   }
	   return instance;
	}

	/**
	 * Generates file filling given template (xsl transformation) with parameters from map.
	 * @param template - template name
	 * @param params - map of parameters to fill in
	 * @param lang - template language
	 * @return file contents
	 */
	@Override
	public byte[] generateByteA(String template, Map params, String lang) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Generates file filling given template (xsl transformation) with parameters from map.
	 * @param template - template name
	 * @param params - map of parameters to fill in
	 * @param lang - template language
	 * @return filename
	 */
	@Override
	public String generateFile(String template, Map params, String lang) {
		// TODO Auto-generated method stub
		return null;
	}
	
}