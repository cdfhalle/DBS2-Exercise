package exercise1;

import de.hpi.dbs2.ChosenImplementation;
import de.hpi.dbs2.dbms.Block;
import de.hpi.dbs2.dbms.BlockManager;
import de.hpi.dbs2.dbms.BlockOutput;
import de.hpi.dbs2.dbms.Relation;
import de.hpi.dbs2.exercise1.SortOperation;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

@ChosenImplementation(true)
public class TPMMSJava extends SortOperation {
    public TPMMSJava(@NotNull BlockManager manager, int sortColumnIndex) {
        super(manager, sortColumnIndex);
    }
    @Override
    public int estimatedIOCost(@NotNull Relation relation) {
        throw new UnsupportedOperationException("TODO");
    }

    @Override
    public void sort(@NotNull Relation relation, @NotNull BlockOutput output) throws RelationSizeExceedsCapacityException {

        int listSize = getBlockManager().getFreeBlocks();
        int listCount = relation.getEstimatedSize() / listSize;

        if (listSize * listSize < relation.getEstimatedSize()) {
            throw new RelationSizeExceedsCapacityException();
        }

        //Store iterators to all list heads in iterators
        ArrayList<Iterator<Block>> iterators = new ArrayList<Iterator<Block>>();
        for (int i = 0; i < listCount; i++) {
            iterators.add(relation.iterator());
            for (int j = 0; j < i * listSize; j++) {
                iterators.get(i).next();
            }
            // hier jetzt liste reinladen und sorten mit Blocksorter
            for (int j = 0; j < listSize; j++) {
                getBlockManager().load(iterators.get(i).next());

            }
        }
    }
}
