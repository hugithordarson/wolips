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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectstyle.cayenne.wocompat.PropertyListSerialization;
import org.objectstyle.wolips.eomodeler.utils.ComparisonUtils;
import org.objectstyle.wolips.eomodeler.utils.StringUtils;

public class EOStoredProcedure extends UserInfoableEOModelObject implements ISortableEOModelObject {
  public static final String NAME = "name";
  public static final String EXTERNAL_NAME = "externalName";
  public static final String ARGUMENT = "argument";
  public static final String ARGUMENTS = "arguments";

  private EOModel myModel;
  private String myName;
  private String myExternalName;
  private List myArguments;
  private EOModelMap myStoredProcedureMap;

  public EOStoredProcedure() {
    myStoredProcedureMap = new EOModelMap();
    myArguments = new LinkedList();
  }

  public EOStoredProcedure(String _name) {
    this();
    myName = _name;
  }

  public void pasted() {
    Iterator argumentsIter = getArguments().iterator();
    while (argumentsIter.hasNext()) {
      EOArgument argument = (EOArgument) argumentsIter.next();
      argument.pasted();
    }
  }

  public EOStoredProcedure cloneStoredProcedure() throws DuplicateNameException {
    EOStoredProcedure storedProcedure = new EOStoredProcedure(myName);
    storedProcedure.myName = myName;
    storedProcedure.myExternalName = myExternalName;

    Iterator argumentsIter = myArguments.iterator();
    while (argumentsIter.hasNext()) {
      EOArgument argument = (EOArgument) argumentsIter.next();
      if (getArgumentNamed(argument.getName()) == null) {
        EOArgument clonedArgument = argument.cloneArgument();
        clonedArgument.setName(findUnusedArgumentName(clonedArgument.getName()));
        storedProcedure.addArgument(clonedArgument);
      }
    }

    return storedProcedure;
  }

  public Set getReferenceFailures() {
    Set referenceFailures = new HashSet();
    Iterator argumentsIter = myArguments.iterator();
    while (argumentsIter.hasNext()) {
      EOArgument argument = (EOArgument) argumentsIter.next();
      referenceFailures.addAll(argument.getReferenceFailures());
    }

    if (myModel != null) {
      Iterator entitiesIter = myModel.getEntities().iterator();
      while (entitiesIter.hasNext()) {
        EOEntity entity = (EOEntity) entitiesIter.next();
        if (entity.getDeleteProcedure() == this) {
          referenceFailures.add(new EOModelVerificationFailure(entity.getFullyQualifiedName() + " uses " + myName + " as its delete procedure."));
        }
        if (entity.getInsertProcedure() == this) {
          referenceFailures.add(new EOModelVerificationFailure(entity.getFullyQualifiedName() + " uses " + myName + " as its insert procedure."));
        }
        if (entity.getNextPrimaryKeyProcedure() == this) {
          referenceFailures.add(new EOModelVerificationFailure(entity.getFullyQualifiedName() + " uses " + myName + " as its next primary key procedure."));
        }
        if (entity.getFetchWithPrimaryKeyProcedure() == this) {
          referenceFailures.add(new EOModelVerificationFailure(entity.getFullyQualifiedName() + " uses " + myName + " as its fetch with primary key procedure."));
        }
        if (entity.getFetchAllProcedure() == this) {
          referenceFailures.add(new EOModelVerificationFailure(entity.getFullyQualifiedName() + " uses " + myName + " as its fetch all procedure."));
        }
        
        Iterator fetchSpecsIter = entity.getFetchSpecs().iterator();
        while (fetchSpecsIter.hasNext()) {
          EOFetchSpecification fetchSpec = (EOFetchSpecification)fetchSpecsIter.next();
          if (fetchSpec.getStoredProcedure() == this) {
            referenceFailures.add(new EOModelVerificationFailure(fetchSpec.getFullyQualifiedName() + " uses " + myName + " as its stored procedure."));
          }
        }
      }
    }

    return referenceFailures;
  }

  public void _setModel(EOModel _model) {
    myModel = _model;
  }

  public EOModel getModel() {
    return myModel;
  }

  protected void _argumentChanged(EOArgument _argument, String _propertyName, Object _oldValue, Object _newValue) {
    firePropertyChange(EOStoredProcedure.ARGUMENT, null, _argument);
  }

  protected void _propertyChanged(String _propertyName, Object _oldValue, Object _newValue) {
    if (myModel != null) {
      myModel._storedProcedureChanged(this, _propertyName, _oldValue, _newValue);
    }
  }

  public int hashCode() {
    return ((myModel == null) ? 1 : myModel.hashCode()) * ((myName == null) ? super.hashCode() : myName.hashCode());
  }

  public boolean equals(Object _obj) {
    boolean equals = false;
    if (_obj instanceof EOStoredProcedure) {
      EOStoredProcedure storedProcedure = (EOStoredProcedure) _obj;
      equals = (storedProcedure == this) || (ComparisonUtils.equals(storedProcedure.myModel, myModel) && ComparisonUtils.equals(storedProcedure.myName, myName));
    }
    return equals;
  }

  public void setName(String _name) throws DuplicateStoredProcedureNameException {
    setName(_name, true);
  }

  public void setName(String _name, boolean _fireEvents) throws DuplicateStoredProcedureNameException {
    if (myModel != null) {
      myModel._checkForDuplicateStoredProcedureName(this, _name, null);
      myModel._storedProcedureNameChanged(myName, _name);
    }
    String oldName = myName;
    myName = _name;
    if (_fireEvents) {
      firePropertyChange(EOStoredProcedure.NAME, oldName, myName);
    }
  }

  public String getName() {
    return myName;
  }

  public String getExternalName() {
    return myExternalName;
  }

  public void setExternalName(String _externalName) {
    String oldExternalName = myExternalName;
    myExternalName = _externalName;
    firePropertyChange(EOStoredProcedure.EXTERNAL_NAME, oldExternalName, myExternalName);
  }

  public EOArgument getArgumentNamed(String _name) {
    EOArgument matchingArgument = null;
    Iterator argumentsIter = myArguments.iterator();
    while (matchingArgument == null && argumentsIter.hasNext()) {
      EOArgument argument = (EOArgument) argumentsIter.next();
      if (ComparisonUtils.equals(argument.getName(), _name)) {
        matchingArgument = argument;
      }
    }
    return matchingArgument;
  }

  public String findUnusedArgumentName(String _newName) {
    boolean unusedNameFound = (getArgumentNamed(_newName) == null);
    String unusedName = _newName;
    for (int dupeNameNum = 1; !unusedNameFound; dupeNameNum++) {
      unusedName = _newName + dupeNameNum;
      EOArgument renameArgument = getArgumentNamed(unusedName);
      unusedNameFound = (renameArgument == null);
    }
    return unusedName;
  }

  public void _checkForDuplicateArgumentName(EOArgument _argument, String _newName, Set _failures) throws DuplicateNameException {
    EOArgument existingArgument = getArgumentNamed(_newName);
    if (existingArgument != null && existingArgument != _argument) {
      if (_failures == null) {
        throw new DuplicateArgumentNameException(_newName, this);
      }

      String unusedName = findUnusedArgumentName(_newName);
      existingArgument.setName(unusedName, true);
      _failures.add(new DuplicateArgumentFailure(this, _newName, unusedName));
    }
  }

  public EOArgument addBlankArgument(String _name) throws DuplicateNameException {
    EOArgument argument = new EOArgument(findUnusedArgumentName(_name));
    argument.setAllowsNull(Boolean.TRUE, false);
    addArgument(argument);
    return argument;
  }

  public void addArgument(EOArgument _argument) throws DuplicateNameException {
    addArgument(_argument, null, true);
  }

  public void addArgument(EOArgument _argument, Set _failures, boolean _fireEvents) throws DuplicateNameException {
    _argument._setStoredProcedure(this);
    _checkForDuplicateArgumentName(_argument, _argument.getName(), _failures);
    _argument.pasted();
    List oldArguments = null;
    if (_fireEvents) {
      oldArguments = myArguments;
      List newArguments = new LinkedList();
      newArguments.addAll(myArguments);
      newArguments.add(_argument);
      myArguments = newArguments;
      firePropertyChange(EOStoredProcedure.ARGUMENTS, oldArguments, myArguments);
    }
    else {
      myArguments.add(_argument);
    }
  }

  public void removeArgument(EOArgument _argument) {
    List oldArguments = myArguments;
    List newArguments = new LinkedList();
    newArguments.addAll(myArguments);
    newArguments.remove(_argument);
    myArguments = newArguments;
    firePropertyChange(EOStoredProcedure.ARGUMENTS, oldArguments, newArguments);
    _argument._setStoredProcedure(null);
  }

  public List getArguments() {
    return myArguments;
  }

  public void loadFromMap(EOModelMap _map, Set _failures) throws EOModelException {
    myStoredProcedureMap = _map;
    myName = _map.getString("name", true);
    myExternalName = _map.getString("externalName", true);

    List argumentsList = _map.getList("arguments", false);
    if (argumentsList != null) {
      Iterator argumentsIter = argumentsList.iterator();
      while (argumentsIter.hasNext()) {
        EOModelMap argumentMap = new EOModelMap((Map) argumentsIter.next());
        EOArgument argument = new EOArgument();
        argument.loadFromMap(argumentMap, _failures);
        addArgument(argument, _failures, false);
      }
    }
    loadUserInfo(_map);
  }

  public EOModelMap toMap() {
    EOModelMap fetchSpecMap = myStoredProcedureMap.cloneModelMap();
    fetchSpecMap.setString("name", myName, true);
    fetchSpecMap.setString("externalName", myExternalName, true);

    List arguments = new LinkedList();
    Iterator argumentsIter = myArguments.iterator();
    while (argumentsIter.hasNext()) {
      EOArgument argument = (EOArgument) argumentsIter.next();
      EOModelMap argumentMap = argument.toMap();
      arguments.add(argumentMap);
    }
    fetchSpecMap.setList("arguments", arguments, true);
    writeUserInfo(fetchSpecMap);
    return fetchSpecMap;
  }

  public void loadFromFile(File _storedProcedureFile, Set _failures) throws EOModelException {
    try {
      EOModelMap entityMap = new EOModelMap((Map) PropertyListSerialization.propertyListFromFile(_storedProcedureFile, new EOModelParserDataStructureFactory()));
      loadFromMap(entityMap, _failures);
    }
    catch (Throwable e) {
      throw new EOModelException("Failed to load stored procedure from '" + _storedProcedureFile + "'.", e);
    }
  }

  public void saveToFile(File _storedProcedureFile) {
    EOModelMap storedProcedureMap = toMap();
    PropertyListSerialization.propertyListToFile(_storedProcedureFile, storedProcedureMap);
  }

  public void resolve(Set _failures) {
    Iterator argumentsIter = myArguments.iterator();
    while (argumentsIter.hasNext()) {
      EOArgument argument = (EOArgument) argumentsIter.next();
      argument.resolve(_failures);
    }
  }

  public void verify(Set _failures) {
    String name = getName();
    if (name == null || name.trim().length() == 0) {
      _failures.add(new EOModelVerificationFailure(myModel.getName() + "/" + myName + " has an empty name."));
    }
    else {
      if (name.indexOf(' ') != -1) {
        _failures.add(new EOModelVerificationFailure(myModel.getName() + "/" + myName + "'s name has a space in it."));
      }
      if (!StringUtils.isUppercaseFirstLetter(myName)) {
        _failures.add(new EOModelVerificationFailure("Entity names should be capitalized, but " + myModel.getName() + "/" + myName + " is not."));
      }
    }

    String externalName = getExternalName();
    if (externalName == null || externalName.trim().length() == 0) {
      _failures.add(new EOModelVerificationFailure(myModel.getName() + "/" + getName() + " has an empty table name."));
    }
    else if (externalName.indexOf(' ') != -1) {
      _failures.add(new EOModelVerificationFailure(myModel.getName() + "/" + getName() + "'s table name '" + externalName + "' has a space in it."));
    }

    Iterator argumentsIter = myArguments.iterator();
    while (argumentsIter.hasNext()) {
      EOArgument argument = (EOArgument) argumentsIter.next();
      argument.verify(_failures);
    }
  }

  public String getFullyQualifiedName() {
    return ((myModel == null) ? "?" : myModel.getFullyQualifiedName()) + "/StoredProc:" + getName();
  }

  public String toString() {
    return "[EOStoredProcedure: name = " + myName + "; arguments = " + myArguments + "]";
  }
}
