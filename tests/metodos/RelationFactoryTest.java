package metodos;

import static org.junit.Assert.*;

import java.util.LinkedList;
import java.util.List;

import imeav.relationextraction.IRelationFactory;
import imeav.relationextraction.RelationFactory;
import imeav.utilities.Vec4i;

import org.junit.Test;

public class RelationFactoryTest {

	@Test
	public void test() {
		IRelationFactory relationFactory = new RelationFactory();
		List<Vec4i> segments = new LinkedList<Vec4i>();
		segments.add(new Vec4i(0, 0, 3, 3));
		segments.add(new Vec4i(3, 3, 5, 5));
		
		segments.add(new Vec4i(0, 4, 2, 2));
		segments.add(new Vec4i(2, 2, 4, 0));
		
		assertEquals(2, relationFactory.getRelations(segments).size());
	}

}
