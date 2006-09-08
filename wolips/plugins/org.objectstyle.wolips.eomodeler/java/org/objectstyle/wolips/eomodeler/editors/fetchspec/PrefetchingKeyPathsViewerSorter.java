package org.objectstyle.wolips.eomodeler.editors.fetchspec;

import org.objectstyle.wolips.eomodeler.model.EOFetchSpecification;
import org.objectstyle.wolips.eomodeler.utils.TablePropertyViewerSorter;

public class PrefetchingKeyPathsViewerSorter extends TablePropertyViewerSorter {
	public PrefetchingKeyPathsViewerSorter(String[] _columnProperties) {
		super(_columnProperties);
	}

	public Object getComparisonValue(Object _obj, String _property) {
		Object comparisonValue;
		if (_property == EOFetchSpecification.PREFETCHING_RELATIONSHIP_KEY_PATH) {
			comparisonValue = _obj;
		} else {
			comparisonValue = super.getComparisonValue(_obj, _property);
		}
		return comparisonValue;
	}

}
