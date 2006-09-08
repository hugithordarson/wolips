package org.objectstyle.wolips.wodclipse.wod.model;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.objectstyle.wolips.wodclipse.wod.completion.WodBindingUtils;

public class BindingValueKeyPath {
	private IJavaProject myJavaProject;

	private IType myContextType;

	private String[] myBindingKeyNames;

	private BindingValueKey[] myBindingKeys;

	private boolean myValid;

	private boolean myAmbiguous;

	public BindingValueKeyPath(String _keyPath, IType _contextType, IJavaProject _javaProject) throws JavaModelException {
		String[] bindingKeyNames = _keyPath.split("\\.");
		// Split tosses empty tokens, so we check to see if we're on the last
		// "." and fake an empty token in the list
		if (_keyPath.length() > 0 && _keyPath.charAt(_keyPath.length() - 1) == '.') {
			String[] bindingKeyNamesWithFinalBlank = new String[bindingKeyNames.length + 1];
			System.arraycopy(bindingKeyNames, 0, bindingKeyNamesWithFinalBlank, 0, bindingKeyNames.length);
			bindingKeyNamesWithFinalBlank[bindingKeyNamesWithFinalBlank.length - 1] = "";
			myBindingKeyNames = bindingKeyNamesWithFinalBlank;
		} else {
			myBindingKeyNames = bindingKeyNames;
		}

		myJavaProject = _javaProject;
		myContextType = _contextType;

		myValid = true;
		IType currentType = _contextType;
		List bindingKeysList = new LinkedList();
		for (int i = 0; currentType != null && i < myBindingKeyNames.length; i++) {
			if (myBindingKeyNames[i].startsWith("@")) {
				myAmbiguous = true;
				currentType = null;
			} else {
				List bindingKeys = WodBindingUtils.createMatchingBindingKeys(_javaProject, currentType, myBindingKeyNames[i], true, WodBindingUtils.ACCESSORS_OR_VOID);
				if (!bindingKeys.isEmpty()) {
					// NTS: Deal with multiple matches ...
					BindingValueKey bindingKey = (BindingValueKey) bindingKeys.get(0);
					bindingKeysList.add(bindingKey);
					currentType = bindingKey.getNextType();
				} else {
					myValid = false;
				}
			}
		}
		myBindingKeys = (BindingValueKey[]) bindingKeysList.toArray(new BindingValueKey[bindingKeysList.size()]);

		if (!myValid) {
			myValid = myBindingKeyNames.length == 1 && "true".equalsIgnoreCase(myBindingKeyNames[0]) || "false".equalsIgnoreCase(myBindingKeyNames[0]) || "yes".equalsIgnoreCase(myBindingKeyNames[0]) || "no".equalsIgnoreCase(myBindingKeyNames[0]);
		}
	}

	public boolean isAmbiguous() {
		return myAmbiguous;
	}

	public boolean isValid() {
		return myValid;
	}

	public String getLastBindingKeyName() {
		String lastBindingKeyName;
		if (myBindingKeyNames.length > 0) {
			lastBindingKeyName = myBindingKeyNames[myBindingKeyNames.length - 1];
		} else {
			lastBindingKeyName = null;
		}
		return lastBindingKeyName;
	}

	public BindingValueKey getLastBindingKey() {
		BindingValueKey bindingKey;
		if (myBindingKeys.length > 0) {
			bindingKey = myBindingKeys[myBindingKeys.length - 1];
		} else {
			bindingKey = null;
		}
		return bindingKey;
	}

	public IType getLastType() throws JavaModelException {
		BindingValueKey lastBindingKey = getLastBindingKey();
		IType lastType;
		if (lastBindingKey != null) {
			lastType = lastBindingKey.getNextType();
		} else {
			lastType = myContextType;
		}
		return lastType;
	}

	public List getPartialMatchesForLastBindingKey() throws JavaModelException {
		List bindingKeysList;
		IType lastType = getLastType();
		if (lastType != null) {
			// Jump forward to the last '.' and look for valid "get" method
			// completion
			// proposals based on the partial token
			String bindingKeyName = getLastBindingKeyName();
			bindingKeysList = WodBindingUtils.createMatchingBindingKeys(myJavaProject, lastType, bindingKeyName, false, WodBindingUtils.ACCESSORS_ONLY);
		} else {
			bindingKeysList = null;
		}
		return bindingKeysList;
	}

	public String[] getRelatedToFileNames() {
		Set relatedToFileNamesSet = new HashSet();
		relatedToFileNamesSet.add(myContextType.getResource().getName());
		for (int i = 0; i < myBindingKeys.length; i++) {
			IResource resource = myBindingKeys[i].getDeclaringType().getResource();
			if (resource != null) {
				relatedToFileNamesSet.add(resource.getName());
			}
		}
		// System.out.println("BindingValueKeyPath.getRelatedToFileNames: " +
		// relatedToFileNamesSet);
		String[] relatedToFileNames = (String[]) relatedToFileNamesSet.toArray(new String[relatedToFileNamesSet.size()]);
		return relatedToFileNames;
	}

	public int getLength() {
		return myBindingKeyNames.length;
	}
}
