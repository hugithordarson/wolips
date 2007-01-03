/*
 * ====================================================================
 * 
 * The ObjectStyle Group Software License, Version 1.0
 * 
 * Copyright (c) 2006 The ObjectStyle Group and individual authors of the
 * software. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowlegement: "This product includes software
 * developed by the ObjectStyle Group (http://objectstyle.org/)." Alternately,
 * this acknowlegement may appear in the software itself, if and wherever such
 * third-party acknowlegements normally appear.
 * 
 * 4. The names "ObjectStyle Group" and "Cayenne" must not be used to endorse or
 * promote products derived from this software without prior written permission.
 * For written permission, please contact andrus@objectstyle.org.
 * 
 * 5. Products derived from this software may not be called "ObjectStyle" nor
 * may "ObjectStyle" appear in their names without prior written permission of
 * the ObjectStyle Group.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * OBJECTSTYLE GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the ObjectStyle Group. For more information on the ObjectStyle
 * Group, please see <http://objectstyle.org/>.
 *  
 */
package org.objectstyle.wolips.eomodeler.sql;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabase;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOSchemaGeneration;
import com.webobjects.eoaccess.EOSynchronizationFactory;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSSet;
import com.webobjects.jdbcadaptor.JDBCAdaptor;
import com.webobjects.jdbcadaptor.JDBCContext;

/**
 * Declare a class named "org.objectstyle.wolips.eomodeler.EOModelProcessor"
 * with the following methods: public void processModel(EOModel _model,
 * NSMutableArray _entities, NSMutableDictionary _flags); public void
 * processSQL(StringBuffer _sqlBuffer, EOModel _model, NSMutableArray _entities,
 * NSMutableDictionary _flags);
 * 
 * or declare an "eomodelProcessorClassName" in your extra info dictionary that
 * has methods of the same signature.
 * 
 * processModel will be called prior to sql generation, and processSQL will be
 * called after sql generation but before it retuns to EOModeler.
 * 
 * @author mschrag
 * 
 */
public class EOFSQLGenerator {
	private NSMutableArray _entities;

	private EOModel _model;

	private EOModelGroup _modelGroup;

	private Object _modelProcessor;

	public EOFSQLGenerator(String modelName, List modelFolders, List entityNames, Map selectedDatabaseConfig) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Map databaseConfig = selectedDatabaseConfig;
		if (databaseConfig == null) {
			databaseConfig = new HashMap();
		}

		_modelGroup = new EOModelGroup();

		Iterator modelFoldersIter = modelFolders.iterator();
		while (modelFoldersIter.hasNext()) {
			File modelFolder = (File) modelFoldersIter.next();
			_modelGroup.addModelWithPathURL(modelFolder.toURL());
		}

		String prototypeEntityName = (String) databaseConfig.get("prototypeEntityName");
		if (prototypeEntityName != null) {
			replacePrototypes(_modelGroup, prototypeEntityName);
		}

		_entities = new NSMutableArray();
		_model = _modelGroup.modelNamed(modelName);
		Map overrideConnectionDictionary = (Map) databaseConfig.get("connectionDictionary");
		if (overrideConnectionDictionary != null) {
			NSMutableDictionary connectionDictionary = new NSMutableDictionary();
			Iterator overrideConnectionDictionaryIter = overrideConnectionDictionary.entrySet().iterator();
			while (overrideConnectionDictionaryIter.hasNext()) {
				Map.Entry overrideEntry = (Map.Entry) overrideConnectionDictionaryIter.next();
				Object key = overrideEntry.getKey();
				Object value = overrideEntry.getValue();
				if (key instanceof String && value instanceof String) {
					connectionDictionary.setObjectForKey(value, key);
				}
			}
			_model.setConnectionDictionary(connectionDictionary);
			String eomodelProcessorClassName = (String) connectionDictionary.objectForKey("eomodelProcessorClassName");
			if (eomodelProcessorClassName != null) {
				findModelProcessor(eomodelProcessorClassName, true);
			}
		}
		if (_modelProcessor == null) {
			findModelProcessor("org.objectstyle.wolips.eomodeler.EOModelProcessor", false);
		}
		if (entityNames == null || entityNames.size() == 0) {
			Enumeration entitiesEnum = _model.entities().objectEnumerator();
			while (entitiesEnum.hasMoreElements()) {
				EOEntity entity = (EOEntity) entitiesEnum.nextElement();
				if (!isPrototype(entity)) {// &&
					// entityUsesSeparateTable(entity))
					// {
					_entities.addObject(entity);
				}
			}
		} else {
			Iterator entityNamesIter = entityNames.iterator();
			while (entityNamesIter.hasNext()) {
				String entityName = (String) entityNamesIter.next();
				EOEntity entity = _model.entityNamed(entityName);
				_entities.addObject(entity);
			}
		}

		ensureSingleTableInheritanceParentEntitiesAreIncluded();
		ensureSingleTableInheritanceChildEntitiesAreIncluded();
		localizeEntities();
	}

	protected void replacePrototypes(EOModelGroup modelGroup, String prototypeEntityName) {
		NSMutableDictionary removedPrototypeEntities = new NSMutableDictionary();

		EOModel prototypesModel = null;
		Enumeration modelsEnum = modelGroup.models().objectEnumerator();
		while (modelsEnum.hasMoreElements()) {
			EOModel model = (EOModel) modelsEnum.nextElement();
			EOEntity eoAdaptorPrototypesEntity = _modelGroup.entityNamed("EO" + model.adaptorName() + "Prototypes");
			if (eoAdaptorPrototypesEntity != null) {
				prototypesModel = eoAdaptorPrototypesEntity.model();
				// System.out.println("EOFSQLGenerator.EOFSQLGenerator:
				// removing " + eoAdaptorPrototypesEntity.name() + " from "
				// + prototypesModel.name());
				prototypesModel.removeEntity(eoAdaptorPrototypesEntity);
				removedPrototypeEntities.setObjectForKey(eoAdaptorPrototypesEntity, eoAdaptorPrototypesEntity.name());
			}
		}

		EOEntity eoPrototypesEntity = _modelGroup.entityNamed("EOPrototypes");
		if (eoPrototypesEntity != null) {
			prototypesModel = eoPrototypesEntity.model();
			prototypesModel.removeEntity(eoPrototypesEntity);
			// System.out.println("EOFSQLGenerator.EOFSQLGenerator: removing
			// " + eoPrototypesEntity.name() + " from " +
			// prototypesModel.name());
			removedPrototypeEntities.setObjectForKey(eoPrototypesEntity, eoPrototypesEntity.name());
		}

		EOEntity prototypesEntity = _modelGroup.entityNamed(prototypeEntityName);
		if (prototypesEntity == null) {
			prototypesEntity = (EOEntity) removedPrototypeEntities.objectForKey(prototypeEntityName);
		} else {
			prototypesModel = prototypesEntity.model();
			prototypesModel.removeEntity(prototypesEntity);
		}
		if (prototypesEntity != null && prototypesModel != null) {
			// System.out.println("EOFSQLGenerator.EOFSQLGenerator: setting
			// " + prototypesEntity.name() + " to EOPrototypes in " +
			// prototypesModel.name());
			prototypesEntity.setName("EOPrototypes");
			prototypesModel.addEntity(prototypesEntity);
		}

		Enumeration resetModelsEnum = _modelGroup.models().objectEnumerator();
		while (resetModelsEnum.hasMoreElements()) {
			EOModel model = (EOModel) resetModelsEnum.nextElement();
			model._resetPrototypeCache();
		}
	}

	protected void localizeEntities() {
		Enumeration entitiesEnum = new NSArray(_entities).objectEnumerator();
		while (entitiesEnum.hasMoreElements()) {
			EOEntity entity = (EOEntity) entitiesEnum.nextElement();
			createLocalizedAttributes(entity);
		}
	}

	protected void ensureSingleTableInheritanceParentEntitiesAreIncluded() {
		Enumeration entitiesEnum = new NSArray(_entities).objectEnumerator();
		while (entitiesEnum.hasMoreElements()) {
			EOEntity entity = (EOEntity) entitiesEnum.nextElement();
			ensureSingleTableInheritanceParentEntitiesAreIncluded(entity);
		}
	}

	protected void ensureSingleTableInheritanceChildEntitiesAreIncluded() {
		Enumeration entitiesEnum = _model.entities().objectEnumerator();
		while (entitiesEnum.hasMoreElements()) {
			EOEntity entity = (EOEntity) entitiesEnum.nextElement();
			if (isSingleTableInheritance(entity)) {
				EOEntity parentEntity = entity.parentEntity();
				if (_entities.containsObject(parentEntity) && !_entities.containsObject(entity)) {
					_entities.addObject(entity);
				}
			}
		}
	}

	protected void ensureSingleTableInheritanceParentEntitiesAreIncluded(EOEntity entity) {
		if (isSingleTableInheritance(entity)) {
			EOEntity parentEntity = entity.parentEntity();
			if (!_entities.containsObject(parentEntity)) {
				_entities.addObject(parentEntity);
				ensureSingleTableInheritanceParentEntitiesAreIncluded(entity);
			}
		}
	}

	protected boolean isPrototype(EOEntity _entity) {
		String entityName = _entity.name();
		boolean isPrototype = (entityName.startsWith("EO") && entityName.endsWith("Prototypes"));
		return isPrototype;
	}

	protected boolean isSingleTableInheritance(EOEntity entity) {
		EOEntity parentEntity = entity.parentEntity();
		return parentEntity != null && entity.externalName() != null && entity.externalName().equalsIgnoreCase(parentEntity.externalName());
	}

	protected void createLocalizedAttributes(EOEntity entity) {
		NSArray attributes = entity.attributes().immutableClone();
		NSArray classProperties = entity.classProperties().immutableClone();
		NSArray attributesUsedForLocking = entity.attributesUsedForLocking().immutableClone();
		if (attributes == null) {
			attributes = NSArray.EmptyArray;
		}
		if (classProperties == null) {
			classProperties = NSArray.EmptyArray;
		}
		if (attributesUsedForLocking == null) {
			attributesUsedForLocking = NSArray.EmptyArray;
		}
		NSMutableArray mutableClassProperties = classProperties.mutableClone();
		NSMutableArray mutableAttributesUsedForLocking = attributesUsedForLocking.mutableClone();
		for (Enumeration e = attributes.objectEnumerator(); e.hasMoreElements();) {
			EOAttribute attribute = (EOAttribute) e.nextElement();
			NSDictionary userInfo = attribute.userInfo();
			String name = attribute.name();
			if (userInfo != null) {
				Object l = userInfo.objectForKey("ERXLanguages");
				if (l != null && !(l instanceof NSArray)) {
					l = (entity.model().userInfo() != null ? entity.model().userInfo().objectForKey("ERXLanguages") : null);
				}

				NSArray languages = (NSArray) l;
				if (languages != null && languages.count() > 0) {
					String columnName = attribute.columnName();
					for (int i = 0; i < languages.count(); i++) {
						String language = (String) languages.objectAtIndex(i);
						String newName = name + "_" + language;
						String newColumnName = columnName + "_" + language;

						EOAttribute newAttribute = new EOAttribute();
						newAttribute.setName(newName);
						entity.addAttribute(newAttribute);

						newAttribute.setPrototype(attribute.prototype());
						newAttribute.setColumnName(newColumnName);
						newAttribute.setAllowsNull(attribute.allowsNull());
						newAttribute.setClassName(attribute.className());
						newAttribute.setExternalType(attribute.externalType());
						newAttribute.setWidth(attribute.width());
						newAttribute.setUserInfo(attribute.userInfo());

						if (classProperties.containsObject(attribute)) {
							mutableClassProperties.addObject(newAttribute);
						}
						if (attributesUsedForLocking.containsObject(attribute)) {
							mutableAttributesUsedForLocking.addObject(newAttribute);
						}
					}
					entity.removeAttribute(attribute);
					mutableClassProperties.removeObject(attribute);
					mutableAttributesUsedForLocking.removeObject(attribute);
				}
			}

			entity.setClassProperties(mutableClassProperties);
			entity.setAttributesUsedForLocking(mutableAttributesUsedForLocking);
		}
	}

	protected boolean isInherited(EOAttribute attribute) {
		boolean inherited = false;
		EOEntity parentEntity = attribute.entity().parentEntity();
		while (!inherited && parentEntity != null) {
			inherited = (parentEntity.attributeNamed(attribute.name()) != null);
			parentEntity = parentEntity.parentEntity();
		}
		return inherited;
	}

	protected void fixDuplicateSingleTableInheritanceDropStatements(EOSynchronizationFactory syncFactory, NSMutableDictionary flags, StringBuffer sqlBuffer) {
		if ("YES".equals(flags.objectForKey(EOSchemaGeneration.DropTablesKey))) {
			NSMutableArray dropEntities = new NSMutableArray(_entities);
			for (int entityNum = dropEntities.count() - 1; entityNum >= 0; entityNum--) {
				EOEntity entity = (EOEntity) dropEntities.objectAtIndex(entityNum);
				if (isSingleTableInheritance(entity)) {
					dropEntities.removeObjectAtIndex(entityNum);
				}
			}
			if (dropEntities.count() != _entities.count()) {
				NSMutableDictionary dropFlags = new NSMutableDictionary();
				dropFlags.setObjectForKey("YES", EOSchemaGeneration.DropTablesKey);
				dropFlags.setObjectForKey("NO", EOSchemaGeneration.DropPrimaryKeySupportKey);
				dropFlags.setObjectForKey("NO", EOSchemaGeneration.CreateTablesKey);
				dropFlags.setObjectForKey("NO", EOSchemaGeneration.CreatePrimaryKeySupportKey);
				dropFlags.setObjectForKey("NO", EOSchemaGeneration.PrimaryKeyConstraintsKey);
				dropFlags.setObjectForKey("NO", EOSchemaGeneration.ForeignKeyConstraintsKey);
				dropFlags.setObjectForKey("NO", EOSchemaGeneration.CreateDatabaseKey);
				dropFlags.setObjectForKey("NO", EOSchemaGeneration.DropDatabaseKey);
				flags.setObjectForKey("NO", EOSchemaGeneration.DropTablesKey);
				String dropSql = syncFactory.schemaCreationScriptForEntities(dropEntities, dropFlags);
				sqlBuffer.append(dropSql);
				sqlBuffer.append("\n");
			}
		}
	}

	public String getSchemaCreationScript(Map flagsMap) {
		NSMutableDictionary flags = new NSMutableDictionary();
		if (flagsMap != null) {
			Iterator entriesIter = flagsMap.entrySet().iterator();
			while (entriesIter.hasNext()) {
				Map.Entry flag = (Map.Entry) entriesIter.next();
				flags.setObjectForKey(flag.getValue(), flag.getKey());
			}
		}

		callModelProcessorMethodIfExists("processModel", new Object[] { _model, _entities, flags });

		EODatabaseContext dbc = new EODatabaseContext(new EODatabase(_model));
		EOAdaptorContext ac = dbc.adaptorContext();
		EOSynchronizationFactory sf = ((JDBCAdaptor) ac.adaptor()).plugIn().synchronizationFactory();

		StringBuffer sqlBuffer = new StringBuffer();
		fixDuplicateSingleTableInheritanceDropStatements(sf, flags, sqlBuffer);

		String sql = sf.schemaCreationScriptForEntities(_entities, flags);
		sql = sql.replaceAll("CREATE TABLE ([^\\s(]+)\\(", "CREATE TABLE $1 (");
		sqlBuffer.append(sql);

		callModelProcessorMethodIfExists("processSQL", new Object[] { sqlBuffer, _model, _entities, flags });

		String sqlBufferStr = sqlBuffer.toString();
		return sqlBufferStr;
	}

	public Object callModelProcessorMethodIfExists(String methodName, Object[] _objs) {
		try {
			Object results = null;
			if (_modelProcessor != null) {
				Method[] methods = _modelProcessor.getClass().getMethods();
				Method matchingMethod = null;
				for (int methodNum = 0; matchingMethod == null && methodNum < methods.length; methodNum++) {
					Method method = methods[methodNum];
					if (method.getName().equals(methodName)) {
						Class[] parameterTypes = method.getParameterTypes();
						boolean parametersMatch = false;
						if ((_objs == null || _objs.length == 0) && parameterTypes.length == 0) {
							parametersMatch = true;
						} else if (_objs != null && _objs.length == parameterTypes.length) {
							parametersMatch = true;
							for (int parameterTypeNum = 0; parametersMatch && parameterTypeNum < parameterTypes.length; parameterTypeNum++) {
								Class parameterType = parameterTypes[parameterTypeNum];
								if (_objs[parameterTypeNum] != null && !parameterType.isAssignableFrom(_objs.getClass())) {
									parametersMatch = false;
								}
							}
						}
						matchingMethod = method;
					}
				}
				if (matchingMethod != null) {
					results = matchingMethod.invoke(_modelProcessor, _objs);
				} else {
					System.out.println("EOFSQLGenerator.callModelProcessorMethodIfExists: Missing delegate " + methodName);
				}
			}
			return results;
		} catch (Throwable t) {
			throw new RuntimeException("Failed to execute " + methodName + " on " + _modelProcessor + ".", t);
		}
	}

	public void findModelProcessor(String modelProcessorClassName, boolean throwExceptionIfMissing) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		try {
			Class modelProcessorClass = Class.forName(modelProcessorClassName);
			_modelProcessor = modelProcessorClass.newInstance();
		} catch (ClassNotFoundException e) {
			System.out.println("EOFSQLGenerator.getModelProcessor: Missing model processor " + modelProcessorClassName);
			if (throwExceptionIfMissing) {
				throw e;
			}
		} catch (InstantiationException e) {
			if (throwExceptionIfMissing) {
				throw e;
			}
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			if (throwExceptionIfMissing) {
				throw e;
			}
			e.printStackTrace();
		} catch (RuntimeException e) {
			if (throwExceptionIfMissing) {
				throw e;
			}
			e.printStackTrace();
		}
	}

	public void executeSQL(String sql) throws SQLException {
		System.out.println("EOFSQLGenerator.executeSQL: " + _model.connectionDictionary());
		EODatabaseContext databaseContext = new EODatabaseContext(new EODatabase(_model));
		EODatabaseChannel databaseChannel = databaseContext.availableChannel();
		EOAdaptorChannel adaptorChannel = databaseChannel.adaptorChannel();
		if (!adaptorChannel.isOpen()) {
			adaptorChannel.openChannel();
		}
		JDBCContext jdbccontext = (JDBCContext) adaptorChannel.adaptorContext();
		try {
			jdbccontext.beginTransaction();
			Connection conn = jdbccontext.connection();
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(sql);
			conn.commit();
		} catch (SQLException sqlexception) {
			sqlexception.printStackTrace(System.out);
			jdbccontext.rollbackTransaction();
			throw sqlexception;
		}
		adaptorChannel.closeChannel();
	}

	public Map externalTypes() {
		EODatabaseContext dbc = new EODatabaseContext(new EODatabase(_model));
		EOAdaptorContext ac = dbc.adaptorContext();
		NSDictionary jdbc2Info = ((JDBCAdaptor) ac.adaptor()).plugIn().jdbcInfo();
		return (Map) toJavaCollections(jdbc2Info);
	}

	protected static Object toJavaCollections(Object obj) {
		Object result;
		if (obj instanceof NSDictionary) {
			Map map = new HashMap();
			NSDictionary nsDict = (NSDictionary) obj;
			Enumeration keysEnum = nsDict.allKeys().objectEnumerator();
			while (keysEnum.hasMoreElements()) {
				Object key = keysEnum.nextElement();
				Object value = nsDict.objectForKey(key);
				key = toJavaCollections(key);
				value = toJavaCollections(value);
				map.put(key, value);
			}
			result = map;
		} else if (obj instanceof NSArray) {
			List list = new LinkedList();
			NSArray nsArray = (NSArray) obj;
			Enumeration valuesEnum = nsArray.objectEnumerator();
			while (valuesEnum.hasMoreElements()) {
				Object value = valuesEnum.nextElement();
				value = toJavaCollections(value);
				list.add(value);
			}
			result = list;
		} else if (obj instanceof NSSet) {
			Set set = new HashSet();
			NSSet nsSet = (NSSet) obj;
			Enumeration valuesEnum = nsSet.objectEnumerator();
			while (valuesEnum.hasMoreElements()) {
				Object value = valuesEnum.nextElement();
				value = toJavaCollections(value);
				set.add(value);
			}
			result = set;
		} else {
			result = obj;
		}
		return result;
	}

	public static void main(String[] argv) throws MalformedURLException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		Map flags = new HashMap();
		flags.put(EOSchemaGeneration.DropTablesKey, "YES");
		flags.put(EOSchemaGeneration.DropPrimaryKeySupportKey, "YES");
		flags.put(EOSchemaGeneration.CreateTablesKey, "YES");
		flags.put(EOSchemaGeneration.CreatePrimaryKeySupportKey, "YES");
		flags.put(EOSchemaGeneration.PrimaryKeyConstraintsKey, "YES");
		flags.put(EOSchemaGeneration.ForeignKeyConstraintsKey, "YES");
		flags.put(EOSchemaGeneration.CreateDatabaseKey, "NO");
		flags.put(EOSchemaGeneration.DropDatabaseKey, "NO");

		File[] paths = new File[] { new File("/Library/Frameworks/JavaBusinessLogic.framework/Resources/Movies.eomodeld"), new File("/Library/Frameworks/JavaBusinessLogic.framework/Resources/Rentals.eomodeld") };
		// probably should have an option to change the connection dict to use a
		// specific plugin or url
		// all entities in one model
		EOFSQLGenerator generator1 = new EOFSQLGenerator("Movies", Arrays.asList(paths), null, null);
		System.out.println("EOFSQLGenerator.main: " + NSBundle.mainBundle());
		System.out.println(generator1.getSchemaCreationScript(null));
		//
		// System.out.println("***********************************");
		//
		// // only movie entity
		//
		// EOFSQLGenerator generator2 = new EOFSQLGenerator("Movies",
		// Arrays.asList(paths), Arrays.asList(new String[] { "Movie" }),
		// optionsCreate);
		// System.out.println(generator2.getSchemaCreationScript());
	}
}
