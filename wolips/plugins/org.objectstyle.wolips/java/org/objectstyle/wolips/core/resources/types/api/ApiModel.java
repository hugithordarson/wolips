/* ====================================================================
 *
 * The ObjectStyle Group Software License, Version 1.0
 *
 * Copyright (c) 2005 The ObjectStyle Group,
 * and individual authors of the software.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        ObjectStyle Group (http://objectstyle.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "ObjectStyle Group" and "Cayenne"
 *    must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact andrus@objectstyle.org.
 *
 * 5. Products derived from this software may not be called "ObjectStyle"
 *    nor may "ObjectStyle" appear in their names without prior written
 *    permission of the ObjectStyle Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE OBJECTSTYLE GROUP OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the ObjectStyle Group.  For more
 * information on the ObjectStyle Group, please see
 * <http://objectstyle.org/>.
 *
 */
package org.objectstyle.wolips.core.resources.types.api;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.objectstyle.wolips.locate.LocatePlugin;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class ApiModel {
	private Document _document;

	private URL _url;

	private Reader _reader;

	private IFile _eclipseFile;

	private File _file;

	private boolean _isDirty;

	public ApiModel(File file) throws ApiModelException {
		_file = file;
		if (!file.exists()) {
			String javaFileName = LocatePlugin.getDefault().fileNameWithoutExtension(file);
			try {
				FileWriter writer = new FileWriter(file);
				try {
					writer.write(ApiModel.blankContent(javaFileName));
				} finally {
					writer.close();
				}
			} catch (IOException e) {
				throw new ApiModelException("Failed to create blank API file.", e);
			}
		}
		parse();
	}

	public ApiModel(IFile file) throws ApiModelException {
		_eclipseFile = file;
		_file = file.getLocation().toFile();
		if (!file.exists()) {
			String javaFileName = LocatePlugin.getDefault().fileNameWithoutExtension(file);
			try {
				file.create(new ByteArrayInputStream(ApiModel.blankContent(javaFileName).getBytes()), true, new NullProgressMonitor());
			} catch (CoreException e) {
				throw new ApiModelException("Failed to create blank API file.", e);
			}
		}
		parse();
	}

	public ApiModel(URL url) throws ApiModelException {
		_url = url;
		parse();
	}

	public ApiModel(Reader reader) throws ApiModelException {
		_reader = reader;
		parse();
	}

	public static String blankContent(String name) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version = \"1.0\" encoding = \"UTF-8\" standalone = \"yes\"?>\n");
		sb.append("<wodefinitions>\n");
		sb.append("  <wo wocomponentcontent = \"false\" class = \"" + name + "\">");
		sb.append("  </wo>\n");
		sb.append("</wodefinitions>\n");
		return sb.toString();
	}

	public String getLocation() {
		String location;
		if (_url != null) {
			location = _url.toExternalForm();
		} else if (_file != null) {
			location = _file.getAbsolutePath();
		} else {
			location = null;
		}
		return location;
	}

	private void parse() throws ApiModelException {
		try {
			DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			if (_url != null) {
				_document = documentBuilder.parse(_url.toExternalForm());
			} else if (_file != null) {
				_document = documentBuilder.parse(_file);
			} else {
				_document = documentBuilder.parse(new InputSource(_reader));
			}
		} catch (Throwable e) {
			throw new ApiModelException("Failed to parse API file " + getLocation() + ".", e);
		}
	}

	public Wodefinitions getWODefinitions() {
		Element element = _document.getDocumentElement();
		return new Wodefinitions(element, this);
	}

	public Wo getWo() {
		Wodefinitions wodefinitions = getWODefinitions();
		if (wodefinitions == null) {
			return null;
		}
		return wodefinitions.getWo();
	}

	public void saveChanges() throws ApiModelException {
		if (_file == null) {
			throw new ApiModelException("You can not saveChanges to an ApiModel that is not backed by a file.");
		}
		try {
			FileWriter writer = new FileWriter(_file);
			try {
				saveChanges(writer);
			} finally {
				writer.close();
			}
			if (_eclipseFile != null) {
				try {
					_eclipseFile.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
				} catch (CoreException e) {
					// ignore
				}
			}
		} catch (IOException ioe) {
			throw new ApiModelException("Failed to save changes to API file.", ioe);
		}
	}

	public void saveChanges(Writer writer) throws ApiModelException {
		try {
			TransformerFactory xformerFactory = TransformerFactory.newInstance();
			xformerFactory.setAttribute("indent-number", new Integer(4));
			OutputFormat outputFormat = new OutputFormat("XML", "UTF-8", true);
			outputFormat.setIndent(1);
			outputFormat.setIndenting(true);
			XMLSerializer serializer = new XMLSerializer(writer, outputFormat);
			serializer.asDOMSerializer();
			serializer.serialize(_document);
			_isDirty = false;
		} catch (Throwable t) {
			throw new ApiModelException("Failed to save API file " + getLocation() + ".", t);
		}
	}

	public boolean isDirty() {
		return _isDirty;
	}

	public void markAsDirty() {
		_isDirty = true;
	}
}