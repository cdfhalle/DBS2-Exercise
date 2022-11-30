package exercise1;

import de.hpi.dbs2.ChosenImplementation;
import de.hpi.dbs2.dbms.*;
import de.hpi.dbs2.dbms.utils.BlockSorter;
import de.hpi.dbs2.exercise1.SortOperation;
import org.checkerframework.checker.units.qual.A;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

@ChosenImplementation(true)
public class TPMMSJava extends SortOperation {
    public TPMMSJava(@NotNull BlockManager manager, int sortColumnIndex) {
        super(manager, sortColumnIndex);
    }

    @Override
    public int estimatedIOCost(@NotNull Relation relation) {
        return 4 * relation.getEstimatedSize();
    }

    public void outputTuple(@NotNull Tuple tuple, @NotNull BlockOutput output, @NotNull Block outputBlock) {
        if (outputBlock.isFull()) {
            output.output(outputBlock);
        }
        outputBlock.append(tuple);
    }

    public void sortPhase1(@NotNull Relation relation, int listSize, int listCount) {
        //Store iterators to all list heads in iterators
        ArrayList<Iterator<Block>> iterators = new ArrayList<Iterator<Block>>();

        for (int i = 0; i < listCount; i++) {

            iterators.add(relation.iterator());
            for (int j = 0; j < i * listSize; j++) {
                iterators.get(i).next();
            }
            // hier jetzt liste reinladen und sorten mit Blocksorter
            ArrayList<Block> loadedBlocks = new ArrayList();
            for (int j = 0; j < listSize; j++) {
                loadedBlocks.add(getBlockManager().load(iterators.get(i).next()));
            }
            BlockSorter.INSTANCE.sort(relation, loadedBlocks, relation.getColumns().getColumnComparator(getSortColumnIndex()));
            for (int j = 0; j < listSize; j++) {
                getBlockManager().release(loadedBlocks.get(j), true);
            }
        }
    }

    public void sortPhase2withList(@NotNull Relation relation, @NotNull BlockOutput output, int listSize, int listCount) {
        Block[][] blocks = new Block[listCount][listSize];
        int[] blockPointers = new int[listCount];
        int[] tuplePointers = new int[listCount];
        Comparator comparator = relation.getColumns().getColumnComparator(getSortColumnIndex());

        // nextTuple(i) returns next Tuple of List. blocks[i][blockPointers[i]].get(tuplePointers[i])
        Function<Integer, Tuple> getNextTuple = (i) -> {
            return blocks[i][blockPointers[i]].get(tuplePointers[i]);
        };
        // isEmpty(i) checks for list if it contains Tuple. Check if blockPointers[i] >= listSize
        Function<Integer, Boolean> isEmpty = (i) -> {
            return blockPointers[i] >= listSize;
        };
        // currBlock(i) returns current Block of List i. blocks[i][blockPointers[i]]
        Function<Integer, Block> currBlock = (i) -> {
            return blocks[i][blockPointers[i]];
        };
        Iterator<Block> iterator = relation.iterator();
        for (int i = 0; i < listCount; i++) {
            for (int j = 0; j < listSize; j++) {
                blocks[i][j] = iterator.next();
            }
        }

        for (int i = 0; i < listCount; i++) {
           getBlockManager().load(blocks[i][0]);
        }

        boolean sorted = false;
        int listWithMinTuple = 0;

        Tuple minTuple=null;
        Block outputBlock = getBlockManager().allocate(true);


        while (!sorted) {

            // findMinTuple:
            for (int i = 0; i < listCount; i++) {
                if(minTuple == null && !isEmpty.apply(i)){
                    minTuple = getNextTuple.apply(i);
                    listWithMinTuple = i;
                }
                if (!isEmpty.apply(i) && comparator.compare(getNextTuple.apply(i), minTuple) < 0) {
                    minTuple = getNextTuple.apply(i);
                    listWithMinTuple = i;
                }
            }
            //output Tuple
            outputTuple(minTuple, output, outputBlock);

            minTuple = null;
            //count indexes of outputtet tuple up
            if (tuplePointers[listWithMinTuple] < currBlock.apply(listWithMinTuple).getCapacity()-1) {
                tuplePointers[listWithMinTuple]++;
            } else {
                getBlockManager().release(currBlock.apply(listWithMinTuple), false);
                blockPointers[listWithMinTuple]++;
                if (!isEmpty.apply(listWithMinTuple)) {
                    getBlockManager().load(currBlock.apply(listWithMinTuple));
                    tuplePointers[listWithMinTuple] = 0;
                }
            }

            //check if sorting is done
            sorted = true;
            for (int i = 0; i < listCount; i++) {
                if (!isEmpty.apply(i)) {
                    sorted = false;
                }
            }
        }
        //write last tuples in outputblock to Output and release Block
        output.output(outputBlock);
        getBlockManager().release(outputBlock, false);
    }


    @Override
    public void sort(@NotNull Relation relation, @NotNull BlockOutput output) throws RelationSizeExceedsCapacityException {

        int listSize = getBlockManager().getFreeBlocks();
        int listCount = relation.getEstimatedSize() / listSize;
        if (listSize * listSize < relation.getEstimatedSize()) {
            throw new RelationSizeExceedsCapacityException();
        }
        sortPhase1(relation, listSize, listCount);
        //sortPhase2(relation, output, listSize, listCount);

        sortPhase2withList(relation, output, listSize, listCount);
    }

}
