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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.area.AreaTreeModel;
import org.apache.fop.area.AreaTreeParser;
import org.apache.fop.area.RenderPagesModel;
import org.apache.fop.fonts.FontInfo;
import org.apache.fop.render.Renderer;
import org.apache.fop.render.xml.XMLRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import eu.bcvsolutions.idm.bsc.config.domain.BscConfiguration;
import eu.bcvsolutions.idm.core.api.exception.CoreException;

@Component
public class FOPProcessor extends TemplateProcessor {
	protected static FOPProcessor instance = null;

	protected static HashMap<String, String> formats = new HashMap<String, String>() {
		{
			put("PDF", MimeConstants.MIME_PDF);
			put("PNG", MimeConstants.MIME_PNG);
			put("TXT", MimeConstants.MIME_PLAIN_TEXT);
			put("PS", MimeConstants.MIME_POSTSCRIPT);
			put("PCL", MimeConstants.MIME_PCL);
			put("RTF", MimeConstants.MIME_RTF);
		}
	};

	private final BscConfiguration bscConfiguration;

	@Autowired
	public FOPProcessor(BscConfiguration bscConfiguration) {
		super(bscConfiguration);
		this.bscConfiguration = bscConfiguration;
	}

	private String getTemplateContent() throws IOException {
		String templateContent = findFileTemplateView();
		if (templateContent == null) {
			throw new IOException("Template not found");
		}
		return templateContent;
	}

	private FopFactory getFopFactory() throws IOException {
		try {
			String fopConfigPath = bscConfiguration.getFopConfigPath();
			if (!StringUtils.isBlank(fopConfigPath)) {
				return FopFactory.newInstance(new File(fopConfigPath));
			}
			throw new CoreException("Path for FOP config not set");
		} catch (SAXException e) {
			throw new IOException("Can't load FOP config.", e);
		}
	}

	/**
	 * Converts an XSL-FO document to an area tree XML file of PDF type.
	 *
	 * @return Path to area tree XML file
	 * @throws IOException
	 */
	public File convertToAreaTreeXML(Map<String, Object> params) throws IOException {
		String templateContent = getTemplateContent();
		return convertToAreaTreeXML(params, templateContent);
	}
	
	public File convertToAreaTreeXML(Map<String, Object> params, String templateContent) throws IOException {
		return convertToAreaTreeXML(valueToString("params", params), templateContent);
	}
	
	/**
	 * Converts an XSL-FO document to an area tree XML file of PDF type.
	 * Calling this method directly is useful mainly for testing and prototyping.
	 * 
	 * @param xml template variables
	 * @param templateContent template content as raw string
	 * @return Path to area tree XML file
	 * @throws IOException
	 */
	public File convertToAreaTreeXML(String xml, String templateContent) throws IOException {
		FopFactory fopFactory = getFopFactory();
		// Create a user agent
		FOUserAgent userAgent = fopFactory.newFOUserAgent();

		// Create an instance of the target renderer so the XMLRenderer can use
		// its font setup
		Renderer targetRenderer = null;
		try {
			targetRenderer = userAgent.getRendererFactory().createRenderer(userAgent, MimeConstants.MIME_PDF);
		} catch (FOPException e) {
			throw new IOException("Cannot create FOP renderer.", e);
		}
		// Create the XMLRenderer to create the area tree XML
		XMLRenderer xmlRenderer = new XMLRenderer(userAgent);

		// Tell the XMLRenderer to mimic the target renderer
		xmlRenderer.mimicRenderer(targetRenderer);

		// Make sure the prepared XMLRenderer is used
		userAgent.setRendererOverride(xmlRenderer);

		// Setup output
		File outfile = File.createTempFile("fop", ".pdf");
		OutputStream out = new BufferedOutputStream(new FileOutputStream(outfile));
		try {
			// Construct fop (the MIME type here is unimportant due to the override
			// on the user agent)
			Fop fop = fopFactory.newFop(null, userAgent, out);

			// Setup XSLT
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer(new StreamSource(new StringReader(templateContent)));
			Source src = new StreamSource(new StringReader(xml));

			// Resulting SAX events (the generated FO) must be piped through to
			// FOP
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start XSLT transformation and FOP processing
			transformer.transform(src, res);
		} catch (TransformerException | SAXException e) {
			throw new IOException("Cannot create area tree to PDF.", e);
		} finally {
			out.close();
		}
		return outfile;
	}

	/**
	 * Concatenates an array of area tree XML files to a single PDF file.
	 * 
	 * @param files
	 *            the collection of area tree XML files
	 * @throws IOException
	 *             In case of an I/O problem
	 */
	public void concatToPDF(Collection<File> files, OutputStream out) throws IOException {
		try {
			// Setup fonts and user agent
			FontInfo fontInfo = new FontInfo();
			FopFactory fopFactory = getFopFactory();
			FOUserAgent userAgent = fopFactory.newFOUserAgent();

			// Construct the AreaTreeModel that will received the individual
			// pages
			AreaTreeModel treeModel = new RenderPagesModel(userAgent, MimeConstants.MIME_PDF, fontInfo, out);

			// Iterate over all area tree files
			AreaTreeParser parser = new AreaTreeParser();
			for (File file : files) {
				parser.parse(new StreamSource(file), treeModel, userAgent);
			}

			// Signal the end of the processing. The renderer can finalize the
			// target document.
			treeModel.endDocument();
		} catch (TransformerException | SAXException e) {
			throw new IOException("Cannot concat area tree to PDF.", e);
		} finally {
			out.close();
		}
	}


	/**
	 * Convert given value to string
	 * 
	 * @param obj
	 *            - object to string
	 * @param name
	 *            - for more complex expansion of containers
	 * @return string
	 */
	@Override
	protected String valueToString(String name, Object obj) {
		if (name == null)
			return "";
		if (obj == null)
			return "<" + name.toString() + "></" + name.toString() + ">\n";
		if (obj instanceof Map)
			return mapToXml(name, (Map<Object, Object>) obj);
		if (obj instanceof Collection)
			return collToXml(name, (Collection<?>) obj);
		return "<" + name + ">" + obj + "</" + name.toString() + ">\n";
	}

	protected <E> String collToXml(String name, Collection<E> obj) {
		String out = "";
		for (Object o : obj) {
			out = out + valueToString(name, o);
		}
		return out;
	}

	protected String mapToXml(String name, Map<Object, Object> obj) {
		String out = "<" + name + ">\n";
		for (Map.Entry<Object, Object> entry : obj.entrySet()) {
			out = out + valueToString(entry.getKey().toString(), entry.getValue());
		}
		out = out + "</" + name + ">\n";
		return out;
	}
}
