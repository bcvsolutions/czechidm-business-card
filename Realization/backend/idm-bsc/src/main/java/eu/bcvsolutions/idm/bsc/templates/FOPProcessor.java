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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
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
import org.xml.sax.SAXException;

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

	public synchronized static FOPProcessor getInstance() {
		if (instance == null) {
			instance = new FOPProcessor();
		}
		return instance;
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from xml.
	 * 
	 * @param template
	 *            - template name
	 * @param xml
	 *            - xml data
	 * @param lang
	 *            - template language
	 * @param format
	 *            - output format
	 * @return file contents
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public byte[] generateByteA(String template, String xml, String lang, String format) throws IOException {
		byte[] temp = IOUtils.toByteArray(new FileInputStream(new File(foptransform(template, xml, lang, format))));
		return temp;
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from xml.
	 * 
	 * @param template
	 *            - template name
	 * @param xml
	 *            - xml data
	 * @param lang
	 *            - template language
	 * @return file contents
	 * @throws IOException
	 */
	public byte[] generateByteA(String template, String xml, String lang) throws IOException {
		return generateByteA(template, xml, lang, "PDF");
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from xml.
	 * 
	 * @param template
	 *            - template name
	 * @param xml
	 *            - xml data
	 * @return file contents
	 * @throws IOException
	 */
	public byte[] generateByteA(String template, String xml) throws IOException {
		return generateByteA(template, xml, null, "PDF");
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from map.
	 * 
	 * @param template
	 *            - template name
	 * @param params
	 *            - map of parameters to fill in
	 * @param lang
	 *            - template language
	 * @param format
	 *            - output format
	 * @return file contents
	 * @throws IOException
	 */
	public byte[] generateByteA(String template, Map params, String lang, String format) throws IOException {
		return generateByteA(template, valueToString("params", params), lang, format);
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from map.
	 * 
	 * @param template
	 *            - template name
	 * @param params
	 *            - map of parameters to fill in
	 * @param lang
	 *            - template language
	 * @return file contents
	 * @throws IOException
	 */
	@Override
	public byte[] generateByteA(String template, Map params, String lang) throws IOException {
		return generateByteA(template, params, lang, "PDF");
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from xml.
	 * 
	 * @param template
	 *            - template name
	 * @param xml
	 *            - xml data
	 * @param lang
	 *            - template language
	 * @param format
	 *            - output format
	 * @return filename
	 * @throws IOException
	 */
	public String generateFile(String template, String xml, String lang, String format) throws IOException {
		return foptransform(template, xml, lang, format);
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from xml.
	 * 
	 * @param template
	 *            - template name
	 * @param xml
	 *            - xml data
	 * @param lang
	 *            - template language
	 * @return filename
	 * @throws IOException
	 */
	public String generateFile(String template, String xml, String lang) throws IOException {
		return generateFile(template, xml, lang, "PDF");
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from xml.
	 * 
	 * @param template
	 *            - template name
	 * @param xml
	 *            - xml data
	 * @return filename
	 * @throws IOException
	 */
	public String generateFile(String template, String xml) throws IOException {
		return generateFile(template, xml, null, "PDF");
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from map.
	 * 
	 * @param template
	 *            - template name
	 * @param params
	 *            - map of parameters to fill in
	 * @param lang
	 *            - template language
	 * @param format
	 *            - output format
	 * @return filename
	 * @throws IOException
	 */
	public String generateFile(String template, Map params, String lang, String format) throws IOException {
		return generateFile(template, valueToString("params", params), lang, format);
	}

	/**
	 * Generates file filling given template (xsl transformation) with
	 * parameters from map.
	 * 
	 * @param template
	 *            - template name
	 * @param params
	 *            - map of parameters to fill in
	 * @param lang
	 *            - template language
	 * @return filename
	 * @throws IOException
	 */
	@Override
	public String generateFile(String template, Map params, String lang) throws IOException {
		return generateFile(template, params, lang, "PDF");
	}

	/**
	 * Write tempfile with generated output.
	 * 
	 * @param template
	 *            - template name
	 * @param xml
	 *            - xml data
	 * @param lang
	 *            - template language
	 * @param format
	 *            - output format
	 * @return
	 * @throws IOException
	 */
	protected String foptransform(String template, String xml, String lang, String format) throws IOException {
		String templatecontent = getTemplateContent(template, lang);
		FopFactory fopFactory = getFopFactory();
		File outfile = File.createTempFile("fop", "." + format.toLowerCase());
		OutputStream out;
		try {
			out = new BufferedOutputStream(new FileOutputStream(outfile));
		} catch (FileNotFoundException e) {
			throw new IOException("Temp file lost", e);
		}
		Fop fop = null;
		try {
			fop = fopFactory.newFop(formats.get(format.toUpperCase()), out);
		} catch (FOPException e) {
			throw new IOException("Can't create FOP", e);
		}
		TransformerFactory factory = TransformerFactory.newInstance();
		Transformer transformer;
		try {
			transformer = factory.newTransformer(new StreamSource(new StringReader(templatecontent)));
		} catch (TransformerConfigurationException e) {
			throw new IOException("Can't create transformer - likely broken template", e);
		}
		Source src = new StreamSource(new StringReader(xml));
		Result res;
		try {
			res = new SAXResult(fop.getDefaultHandler());
		} catch (FOPException e) {
			throw new IOException("Can't parse data xml", e);
		}
		try {
			transformer.transform(src, res);
		} catch (TransformerException | NullPointerException e) {
			throw new IOException("Transformation failed. likely broken data or template", e);
		}
		out.close();
		return outfile.getAbsolutePath();
	}

	private String getTemplateContent(String template, String lang) throws IOException {
		String templateContent = findFileTemplateView(template, lang);
		if (templateContent == null) {
			throw new IOException("Template not found");
		}
		return templateContent;
	}

	private FopFactory getFopFactory() throws IOException {
		try {
			// TODO make configurable
			return FopFactory.newInstance(new File("C:\\work\\modules\\czechidm-business-card\\Realization\\backend\\idm-bsc\\src\\main\\resources\\eu\\bcvsolutions\\idm\\fileTemplate\\fop.conf"));
		} catch (SAXException e) {
			throw new IOException("Can't load FOP config.", e);
		}
	}

	/**
	 * Converts an XSL-FO document to an area tree XML file of PDF type.
	 * 
	 * @param template IdM template name
	 * @param lang template language
	 * @return Path to area tree XML file
	 * @throws IOException
	 */
	public File convertToAreaTreeXML(String template, String lang, Map<String, Object> params) throws IOException {
		String templateContent = getTemplateContent(template, lang);
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
	 * @param pdffile
	 *            the target PDF file
	 * @throws IOException
	 *             In case of an I/O problem
	 * @throws TransformerException
	 *             In case of a XSL transformation problem
	 * @throws SAXException
	 *             In case of an XML-related problem
	 */
	public void concatToPDF(Collection<File> files, File pdffile) throws IOException {
		// Setup output
		OutputStream out = new BufferedOutputStream(new FileOutputStream(pdffile));
		concatToPDF(files, out);
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
