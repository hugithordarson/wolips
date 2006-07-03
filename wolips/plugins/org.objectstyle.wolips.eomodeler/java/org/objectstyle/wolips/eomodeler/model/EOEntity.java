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
package org.objectstyle.wolips.eomodeler.model;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectstyle.cayenne.wocompat.PropertyListSerialization;

public class EOEntity {
  private EOModel myModel;
  private String myName;
  private String myExternalName;
  private String myClassName;
  private String myParent;
  private String myRestrictingQualifier;
  private String myExternalQuery;
  private Boolean myCachesObjects;
  private Boolean myAbstractEntity;
  private Boolean myReadOnly;
  private Integer myMaxNumberOfInstancesToBatchFetch;
  private List myAttributes;
  private List myRelationships;
  private List myFetchSpecs;
  private Map myUserInfo;
  private EOModelMap myEntityMap;
  private EOModelMap myFetchSpecsMap;

  public EOEntity(EOModel _model) {
    myModel = _model;
    myAttributes = new LinkedList();
    myRelationships = new LinkedList();
    myFetchSpecs = new LinkedList();
    myEntityMap = new EOModelMap();
    myFetchSpecsMap = new EOModelMap();
  }

  public EOModel getModel() {
    return myModel;
  }

  public boolean isPrototype() {
    return myName != null && myName.startsWith("EO") && myName.endsWith("Prototypes");
  }

  public String getExternalQuery() {
    return myExternalQuery;
  }

  public void setExternalQuery(String _externalQuery) {
    myExternalQuery = _externalQuery;
  }

  public Integer getMaxNumberOfInstancesToBatchFetch() {
    return myMaxNumberOfInstancesToBatchFetch;
  }

  public void setMaxNumberOfInstancesToBatchFetch(Integer _maxNumberOfInstancesToBatchFetch) {
    myMaxNumberOfInstancesToBatchFetch = _maxNumberOfInstancesToBatchFetch;
  }

  public Boolean isReadOnly() {
    return myReadOnly;
  }

  public void setReadOnly(Boolean _readOnly) {
    myReadOnly = _readOnly;
  }

  public String getName() {
    return myName;
  }

  public void setName(String _name) throws DuplicateEntityNameException {
    myModel._checkForDuplicateEntityName(this, _name);
    myModel._entityNameChanged(_name, myName);
    myName = _name;
  }

  public String getClassName() {
    return myClassName;
  }

  public void setClassName(String _className) {
    myClassName = _className;
  }

  public String getExternalName() {
    return myExternalName;
  }

  public void setExternalName(String _externalName) {
    myExternalName = _externalName;
  }

  public EOEntity getParent() {
    return myModel.getModelGroup().getEntityNamed(myParent);
  }

  public List getChildren() {
    List children = new LinkedList();
    Iterator modelsIter = myModel.getModelGroup().getModels().iterator();
    while (modelsIter.hasNext()) {
      EOModel model = (EOModel) modelsIter.next();
      Iterator entitiesIter = model.getEntities().iterator();
      while (entitiesIter.hasNext()) {
        EOEntity entity = (EOEntity) entitiesIter.next();
        if (entity.getParent() == this) {
          children.add(entity);
        }
      }
    }
    return children;
  }

  public void setParent(EOEntity _entity) {
    if (_entity == null) {
      myParent = null;
    }
    else {
      myParent = _entity.getName();
    }
  }

  public void setParentName(String _parentName) {
    myParent = _parentName;
  }

  public String getParentName() {
    return myParent;
  }

  public Boolean isAbstractEntity() {
    return myAbstractEntity;
  }

  public void setAbstractEntity(Boolean _abstractEntity) {
    myAbstractEntity = _abstractEntity;
  }

  public Boolean isCachesObjects() {
    return myCachesObjects;
  }

  public void setCachesObjects(Boolean _cachesObjects) {
    myCachesObjects = _cachesObjects;
  }

  public String getRestrictingQualifier() {
    return myRestrictingQualifier;
  }

  public void setRestrictingQualifier(String _restrictingQualifier) {
    myRestrictingQualifier = _restrictingQualifier;
  }

  public List getAttributes() {
    return myAttributes;
  }

  public List getRelationships() {
    return myRelationships;
  }

  public List getFetchSpecs() {
    return myFetchSpecs;
  }

  public void _checkForDuplicateAttributeName(EOAttribute _attribute, String _newName) throws DuplicateAttributeNameException {
    EOAttribute attribute = getAttributeNamed(_newName);
    if (attribute != null && attribute != _attribute) {
      throw new DuplicateAttributeNameException(_newName, this);
    }
  }

  public EOFetchSpecification getFetchSpecNamed(String _name) {
    EOFetchSpecification matchingFetchSpec = null;
    Iterator fetchSpecsIter = myFetchSpecs.iterator();
    while (matchingFetchSpec == null && fetchSpecsIter.hasNext()) {
      EOFetchSpecification fetchSpec = (EOFetchSpecification) fetchSpecsIter.next();
      if (fetchSpec.getName().equals(_name)) {
        matchingFetchSpec = fetchSpec;
      }
    }
    return matchingFetchSpec;
  }

  public void _checkForDuplicateFetchSpecName(EOFetchSpecification _fetchSpec, String _newName) throws DuplicateFetchSpecNameException {
    EOFetchSpecification fetchSpec = getFetchSpecNamed(_newName);
    if (fetchSpec != null && fetchSpec != _fetchSpec) {
      throw new DuplicateFetchSpecNameException(_newName, this);
    }
  }

  public void addFetchSpecification(EOFetchSpecification _fetchSpecification) throws DuplicateFetchSpecNameException {
    _checkForDuplicateFetchSpecName(_fetchSpecification, _fetchSpecification.getName());
    myFetchSpecs.add(_fetchSpecification);
  }

  public void removeFetchSpecification(EOFetchSpecification _fetchSpecification) {
    myFetchSpecs.remove(_fetchSpecification);
  }

  public void addAttribute(EOAttribute _attribute) throws DuplicateAttributeNameException {
    _checkForDuplicateAttributeName(_attribute, _attribute.getName());
    myAttributes.add(_attribute);
  }

  public void removeAttribute(EOAttribute _attribute) {
    myAttributes.remove(_attribute);
  }

  public IEOAttribute _getAttributeNamed(String _name) {
    IEOAttribute attribute = getAttributeNamed(_name);
    if (attribute == null) {
      attribute = getRelationshipNamed(_name);
    }
    return attribute;
  }

  public EOAttribute getAttributeNamed(String _name) {
    EOAttribute matchingAttribute = null;
    Iterator attributesIter = myAttributes.iterator();
    while (matchingAttribute == null && attributesIter.hasNext()) {
      EOAttribute attribute = (EOAttribute) attributesIter.next();
      if (attribute.getName().equals(_name)) {
        matchingAttribute = attribute;
      }
    }
    return matchingAttribute;
  }

  public void _checkForDuplicateRelationshipName(EORelationship _relationship, String _newName) throws DuplicateRelationshipNameException {
    EORelationship relationship = getRelationshipNamed(_newName);
    if (relationship != null && relationship != _relationship) {
      throw new DuplicateRelationshipNameException(_newName, this);
    }
  }

  public void addRelationship(EORelationship _relationship) throws DuplicateRelationshipNameException {
    _checkForDuplicateRelationshipName(_relationship, _relationship.getName());
    myRelationships.add(_relationship);
  }

  public void removeRelationship(EORelationship _relationship) {
    myRelationships.remove(_relationship);
  }

  public EORelationship getRelationshipNamed(String _name) {
    EORelationship matchingRelationship = null;
    Iterator relationshipsIter = myRelationships.iterator();
    while (matchingRelationship == null && relationshipsIter.hasNext()) {
      EORelationship relationship = (EORelationship) relationshipsIter.next();
      if (relationship.getName().equals(_name)) {
        matchingRelationship = relationship;
      }
    }
    return matchingRelationship;
  }

  public void setUserInfo(Map _userInfo) {
    myUserInfo = _userInfo;
  }

  public Map getUserInfo() {
    return myUserInfo;
  }

  public void loadFromFile(File _entityFile, File _fetchSpecFile) throws IOException, EOModelException {
    if (_entityFile.exists()) {
      EOModelMap entityMap = new EOModelMap((Map) PropertyListSerialization.propertyListFromFile(_entityFile));
      loadFromMap(entityMap);
    }
    if (_fetchSpecFile.exists()) {
      EOModelMap fspecMap = new EOModelMap((Map) PropertyListSerialization.propertyListFromFile(_fetchSpecFile));
      loadFetchSpecsFromMap(fspecMap);
    }
  }

  public void loadFromMap(EOModelMap _entityMap) throws DuplicateRelationshipNameException, DuplicateAttributeNameException {
    myEntityMap = _entityMap;
    myName = _entityMap.getString("name", true);
    myExternalName = _entityMap.getString("externalName", true);
    myClassName = _entityMap.getString("className", true);
    myParent = _entityMap.getString("parent", true);
    myCachesObjects = _entityMap.getBoolean("cachesObjects");
    myAbstractEntity = _entityMap.getBoolean("isAbstractEntity");
    myReadOnly = _entityMap.getBoolean("readOnly");
    myRestrictingQualifier = _entityMap.getString("restrictingQualifier", true);
    myExternalQuery = _entityMap.getString("externalQuery", true);
    myMaxNumberOfInstancesToBatchFetch = _entityMap.getInteger("maxNumberOfInstancesToBatchFetch");
    myUserInfo = _entityMap.getMap("userInfo", true);

    //Map fetchSpecifications = _entityMap.getMap("fetchSpecificationDictionary");
    // TODO: Fetch Specs

    List attributeList = _entityMap.getList("attributes");
    if (attributeList != null) {
      Iterator attributeIter = attributeList.iterator();
      while (attributeIter.hasNext()) {
        EOModelMap attributeMap = new EOModelMap((Map) attributeIter.next());
        EOAttribute attribute = new EOAttribute(this);
        attribute.loadFromMap(attributeMap);
        addAttribute(attribute);
      }
    }

    List relationshipList = _entityMap.getList("relationships");
    if (relationshipList != null) {
      Iterator relationshipIter = relationshipList.iterator();
      while (relationshipIter.hasNext()) {
        EOModelMap relationshipMap = new EOModelMap((Map) relationshipIter.next());
        EORelationship relationship = new EORelationship(this);
        relationship.loadFromMap(relationshipMap);
        addRelationship(relationship);
      }
    }

    List attributesUsedForLocking = _entityMap.getList("attributesUsedForLocking");
    if (attributesUsedForLocking != null) {
      Iterator attributesUsedForLockingIter = attributesUsedForLocking.iterator();
      while (attributesUsedForLockingIter.hasNext()) {
        String attributeName = (String) attributesUsedForLockingIter.next();
        EOAttribute attribute = getAttributeNamed(attributeName);
        if (attribute != null) {
          attribute.setUsedForLocking(Boolean.TRUE);
        }
      }
    }

    List classProperties = _entityMap.getList("classProperties");
    if (classProperties != null) {
      Iterator classPropertiesIter = classProperties.iterator();
      while (classPropertiesIter.hasNext()) {
        String attributeName = (String) classPropertiesIter.next();
        IEOAttribute attribute = _getAttributeNamed(attributeName);
        if (attribute != null) {
          attribute.setClassProperty(Boolean.TRUE);
        }
      }
    }

    List primaryKeyAttributes = _entityMap.getList("primaryKeyAttributes");
    if (primaryKeyAttributes != null) {
      Iterator primaryKeyAttributesIter = primaryKeyAttributes.iterator();
      while (primaryKeyAttributesIter.hasNext()) {
        String attributeName = (String) primaryKeyAttributesIter.next();
        EOAttribute attribute = getAttributeNamed(attributeName);
        if (attribute != null) {
          attribute.setPrimaryKey(Boolean.TRUE);
        }
      }
    }

    Map internalInfo = _entityMap.getMap("internalInfo");
    if (internalInfo != null) {
      List clientClassPropertyNames = _entityMap.getList("_clientClassPropertyNames");
      if (clientClassPropertyNames != null) {
        Iterator clientClassPropertyNameIter = clientClassPropertyNames.iterator();
        while (clientClassPropertyNameIter.hasNext()) {
          String attributeName = (String) clientClassPropertyNameIter.next();
          EOAttribute attribute = getAttributeNamed(attributeName);
          if (attribute != null) {
            attribute.setClientClassProperty(Boolean.TRUE);
          }
        }
      }
    }
  }

  public void loadFetchSpecsFromMap(EOModelMap _map) throws DuplicateFetchSpecNameException {
    myFetchSpecsMap = _map;
    Iterator fetchSpecIter = _map.entrySet().iterator();
    while (fetchSpecIter.hasNext()) {
      Map.Entry fetchSpecEntry = (Map.Entry) fetchSpecIter.next();
      String fetchSpecName = (String) fetchSpecEntry.getKey();
      EOModelMap fetchSpecMap = new EOModelMap((Map) fetchSpecEntry.getValue());
      EOFetchSpecification fetchSpec = new EOFetchSpecification(this, fetchSpecName);
      fetchSpec.loadFromMap(_map);
      addFetchSpecification(fetchSpec);
    }
  }

  public EOModelMap toEntityMap() {
    EOModelMap entityMap = myEntityMap.cloneModelMap();
    entityMap.setString("name", myName, true);
    entityMap.setString("externalName", myExternalName, true);
    entityMap.setString("className", myClassName, true);
    entityMap.setString("parent", myParent, true);
    entityMap.setBoolean("cachesObjects", myCachesObjects);
    entityMap.setBoolean("isAbstractEntity", myAbstractEntity);
    entityMap.setBoolean("readOnly", myReadOnly);
    entityMap.setString("restrictingQualifier", myRestrictingQualifier, true);
    entityMap.setString("externalQuery", myExternalQuery, true);
    entityMap.setInteger("maxNumberOfInstancesToBatchFetch", myMaxNumberOfInstancesToBatchFetch);

    //Map fetchSpecifications = _entityMap.getMap("fetchSpecificationDictionary");
    // TODO: Fetch Specs

    List classProperties = new LinkedList();
    List primaryKeyAttributes = new LinkedList();
    List attributesUsedForLocking = new LinkedList();
    List clientClassProperties = new LinkedList();
    List attributes = new LinkedList();
    Iterator attributeIter = myAttributes.iterator();
    while (attributeIter.hasNext()) {
      EOAttribute attribute = (EOAttribute) attributeIter.next();
      EOModelMap attributeMap = attribute.toMap();
      attributes.add(attributeMap);
      if (attribute.isClassProperty() != null && attribute.isClassProperty().booleanValue()) {
        classProperties.add(attribute.getName());
      }
      if (attribute.isPrimaryKey() != null && attribute.isPrimaryKey().booleanValue()) {
        primaryKeyAttributes.add(attribute.getName());
      }
      if (attribute.isUsedForLocking() != null && attribute.isUsedForLocking().booleanValue()) {
        attributesUsedForLocking.add(attribute.getName());
      }
      if (attribute.isClientClassProperty() != null && attribute.isClientClassProperty().booleanValue()) {
        clientClassProperties.add(attribute.getName());
      }
    }
    entityMap.setList("attributes", attributes);

    List relationships = new LinkedList();
    Iterator relationshipIter = myRelationships.iterator();
    while (relationshipIter.hasNext()) {
      EORelationship relationship = (EORelationship) relationshipIter.next();
      EOModelMap relationshipMap = relationship.toMap();
      relationships.add(relationshipMap);
      if (relationship.isClassProperty() != null && relationship.isClassProperty().booleanValue()) {
        classProperties.add(relationship.getName());
      }
    }
    entityMap.setList("relationships", relationships);
    entityMap.setList("attributesUsedForLocking", attributesUsedForLocking);
    entityMap.setList("classProperties", classProperties);
    entityMap.setList("primaryKeyAttributes", primaryKeyAttributes);

    Map internalInfoMap = entityMap.getMap("internalInfo");
    if (internalInfoMap == null) {
      internalInfoMap = new HashMap();
      entityMap.put("internalInfo", internalInfoMap);
    }
    internalInfoMap.put("_clientClassPropertyNames", clientClassProperties);

    entityMap.put("userInfo", myUserInfo);

    return entityMap;
  }

  public EOModelMap toFetchSpecsMap() {
    EOModelMap fetchSpecsMap = myFetchSpecsMap.cloneModelMap();
    fetchSpecsMap.clear();
    Iterator fetchSpecIter = myFetchSpecs.iterator();
    while (fetchSpecIter.hasNext()) {
      EOFetchSpecification fetchSpec = (EOFetchSpecification) fetchSpecIter.next();
      EOModelMap fetchSpecMap = fetchSpec.toMap();
      fetchSpecsMap.setMap(fetchSpec.getName(), fetchSpecMap);
    }
    return fetchSpecsMap;
  }

  public void saveToFile(File _entityFile, File _fetchSpecFile) {
    EOModelMap entityMap = toEntityMap();
    PropertyListSerialization.propertyListToFile(_entityFile, entityMap);

    if (myFetchSpecs.size() == 0) {
      _fetchSpecFile.delete();
    }
    else {
      EOModelMap fetchSpecMap = toFetchSpecsMap();
      PropertyListSerialization.propertyListToFile(_fetchSpecFile, fetchSpecMap);
    }
  }

  public void verify(List _failures) {
    if (myParent != null && getParent() == null) {
      _failures.add(new MissingEntityFailure(myParent));
    }

    // TODO

    Iterator attributeIter = myAttributes.iterator();
    while (attributeIter.hasNext()) {
      EOAttribute attribute = (EOAttribute) attributeIter.next();
      attribute.verify(_failures);
    }

    Iterator relationshipIter = myRelationships.iterator();
    while (relationshipIter.hasNext()) {
      EORelationship relationship = (EORelationship) relationshipIter.next();
      relationship.verify(_failures);
    }

    Iterator fetchSpecIter = myFetchSpecs.iterator();
    while (fetchSpecIter.hasNext()) {
      EOFetchSpecification fetchSpec = (EOFetchSpecification) fetchSpecIter.next();
      fetchSpec.verify(_failures);
    }
  }

  public String toString() {
    return "[EOEntity: name = " + myName + "; attributes = " + myAttributes + "; relationships = " + myRelationships + "; fetchSpecs = " + myFetchSpecs + "]";
  }
}