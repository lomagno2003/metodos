package trunk.tests.metodos;

import static org.junit.Assert.assertEquals;
import imeav.utilities.Filter;
import imeav.utilities.SimilarAngleSegmentFilter;
import imeav.utilities.Vec4i;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class SimilarAngleSegmentFilterTest {

	@Test
	public void test() {
		Vec4i testVector = new Vec4i(0, 0, 3, 3);
		Filter<Vec4i> filter = new SimilarAngleSegmentFilter(testVector, 0.1);

		Map<Vec4i, Boolean> dataSet = new HashMap<Vec4i, Boolean>();
		dataSet.put(new Vec4i(3, 3, 5, 5), true);
		dataSet.put(new Vec4i(0, 5, 5, 0), false);
		dataSet.put(new Vec4i(2, 2, 3, 3), true);
		dataSet.put(new Vec4i(-2, -2, 0, 0), true);
		dataSet.put(new Vec4i(2, 2, 2, 5), false);

		for (Vec4i segment : dataSet.keySet()) {
			assertEquals("Vector: (" + segment.v0 + "," + segment.v1 + ","
					+ segment.v2 + "," + segment.v3 + ")",
					dataSet.get(segment), filter.evaluate(segment));
		}
	}

}
