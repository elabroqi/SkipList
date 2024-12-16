import java.util.*;
import static java.lang.Integer.MAX_VALUE;


public class SkipListSet<T extends Comparable<T>> implements SortedSet<T> {
    int numElements;
    int maxHeight = 23;
    int activeHeight =  1;

    private SkipListSetItem<T>[] head;


    private Comparator<? super T> comp;

    LinkedList<LinkedList<SkipListSetItem<T>>> nodes = new LinkedList<>();

    public SkipListSet(Object arg) {
        if (arg == null) {
            throw new NullPointerException("SkipListSet cannot be initialized with null.");
        }
    }
    public SkipListSet(Collection<? extends T> collection) {
        this();

        if (collection == null) {
            throw new NullPointerException("The collection cannot be null.");
        }
        for (T element : collection) {
            this.add(element);
        }
    }

    @SuppressWarnings("unchecked")
    public SkipListSet() {
        head = (SkipListSetItem<T>[]) new SkipListSetItem[maxHeight];

        activeHeight = 0;

        head[0] = new SkipListSetItem<>((T) (Integer) Integer.MIN_VALUE);
        head[0].up = null;
        head[0].down = null;
    }

    public SkipListSet(SkipListSet<T> other) {
        this();
        for (T element : other) {
            this.add(element);
        }
    }

    private class SkipListSetIterator implements Iterator<T> {
        SkipListSetItem<T> currentNode = head[0];

        @Override
        public boolean hasNext() {
            return currentNode != null && currentNode.next != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in the skip list");
            }

            currentNode = currentNode.next;
            return currentNode.value;
        }

        @Override
        public void remove() {
            if (currentNode == null) {
                throw new IllegalStateException();
            }

            SkipListSetItem<T> node = currentNode;

            while (node != null) {  //starting from the bottom layer, manipulates pointers as you move up a level
                if (node.previous != null) {
                    node.previous.next = node.next;
                }
                if (node.next != null) {
                    node.next.previous = node.previous;
                }

                node = node.up;
            }

            while (activeHeight > 1 && head[activeHeight].next == null) { //if any level only has a null pointer we remove it
                head[activeHeight] = null;
                activeHeight--;
            }
            numElements--;

        }
    }

    private static class SkipListSetItem<T>{
        private T value;
        private SkipListSetItem<T> previous;
        private SkipListSetItem<T> next;
        private SkipListSetItem<T> up;
        private SkipListSetItem<T> down;
        private int height;

        public SkipListSetItem(T value){
           this.value = value;
           this.previous = null;
           this.next = null;
           this.up = null;
           this.down = null;
           this.height = 1;
        }

        public T getValue(){
            return value;
        }
        public SkipListSetItem<T> getPrevious (){
            return previous;
        }
        public SkipListSetItem<T> getNext (){
            return next;
        }
        public SkipListSetItem<T> getUp (){
            return up;
        }
        public SkipListSetItem<T> getDown (){
            return down;
        }
        public int getHeight(){return height;}

        public void setValue(T value){
            this.value = value;
        }

        public void setPrevious(SkipListSetItem<T> previous){
            this.previous = previous;
        }

        public void setNext(SkipListSetItem<T> next){
            this.next = next;
        }

        public void setUp(SkipListSetItem<T> up){
            this.up = up;
        }

        public void setDown(SkipListSetItem<T> down){
            this.down = down;
        }

        public void setHeight(int height){ this.height = 0;}

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    @Override
    public int size() {
        if(numElements > MAX_VALUE){
            return MAX_VALUE;
        } else {
            return numElements;
        }
    }

    @Override
    public boolean isEmpty() {
        int size = size();
        if(size == 0){
            return true;
        }
        return false;
    }

    @Override
    public boolean contains(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        try {
            T element = (T) o;
            SkipListSetItem<T> node = search(element);
            return node != null && node.value.equals(element);
        } catch(ClassCastException e) {
            throw new ClassCastException();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new SkipListSetIterator();
    }

    @Override
    public Object[] toArray() {
        int size = nodes.getFirst().size();
        Object[] objectArray = new Object[size];
        SkipListSetItem<T> traversalNode = head[0].next;

        int i = 0;
        while(traversalNode != null){
            objectArray[i] = traversalNode.value;
            traversalNode = traversalNode.next;
            i++;
        }
        return objectArray;  //returns an array in sorted order of all the elements in the bottom layer
    }

    @Override
    public <T> T[] toArray(T[] a) {
        int size = numElements;

        if (a == null || a.length < size) {
            a = (T[]) java.lang.reflect.Array.newInstance(a != null ? a.getClass().getComponentType() : Object.class, size);
        }

        SkipListSetItem<T> current = (SkipListSetItem<T>) head[0].next;
        int index = 0;

        while (current != null) {
            a[index++] = current.value;
            current = current.next;
        }

        if (a.length > size) {
            a[size] = null;
        }

        return a; //given an array, size to numElements, loads values from bottom layer to given array in sorted order
    }

    public int adjustHeight(){
        int randomValue;
        do {
            randomValue = (int) (Math.random() * (1 << maxHeight)) & ((1 << maxHeight) - 1);
        } while (randomValue == 0); // kept getting 0, so added this so that the returnIndex method works

        return returnIndex(randomValue);
         //generating a random bitwise number and masking it with maxHeight
        // the probability of a bit being ("1") in every index in increasing order reduces by half
    }

    public int returnIndex(int bitwise){
       int k = 0;
        while(k < maxHeight){
           if((bitwise & (1<<k)) != 0){ //returning the first index that encounters a 1. This index indicates where the height stops
               return k;
           }
           k++;
       }
        return -1;
    }

    public SkipListSetItem<T> search (T e){
        SkipListSetItem<T> node = head[activeHeight]; //starting at the topmost head node (-infiniti)
        if(node != null) {
            while (node.down != null) {  //if we're able to go to the next top node then traverese through top nodes until you reach the spot to insert
                if (node.next != null) {
                    if (node.next.value.compareTo(e) <= 0) {
                        node = node.next;
                    } else if (node.next.value.compareTo(e) > 0) {
                        node = node.down;
                    }
                } else {
                    node = node.down;
                }
            }

            while (node.next != null && node.next.value.compareTo(e) <= 0) {   //checking bottom layer if there are any more nodes to traverse through to insert correctly
                node = node.next;
            }
            return node;  //returns the node before the node to insert will go
        }
        return null;
    }


    @Override
    public boolean add(T e) {

        if (e == null) {
            throw new NullPointerException();
        }

        if (contains(e)) {  //checks for duplicates
            return false;
        }

        SkipListSetItem<T> currentNode = search(e); //search returns the node from the bottom layer
        SkipListSetItem<T> newNode = new SkipListSetItem<>(e);


        //linking bottom layer first
        if (currentNode.next != null) {
            newNode.next = currentNode.next;
            currentNode.next.previous = newNode;
        }
        currentNode.next = newNode;
        newNode.previous = currentNode;

        //updating height based on the number of elements, if #nodes == power of 2 update height to log2(#nodes). My height is 1-base index so 1 and 2 #nodes will be maxHeight of 1
        numElements++;
        if ((numElements & (numElements - 1)) == 0) {
            updateMaxHeight();
        }

        //using coin flip method to adjust the height and doesn't allow for the height to go past the maxHeight
        int newHeight = Math.min(adjustHeight(), maxHeight);

        //node heights are 1-base index
        newNode.height = Math.max(1, newHeight);

        int level = 0;
        SkipListSetItem<T> lowerNode = newNode;
        SkipListSetItem<T> tmp = currentNode; //the node we used to insert newNode
        while (level < newHeight) {   //adding new levels if newHeight is greater than 0
            while (tmp.up == null && tmp.previous != null) { //keep grabbing the previous node if it has the height needed
                tmp = tmp.previous;
            }
            if (tmp.previous == null && tmp.up == null) { //if no more nodes have the height needed we create a new head level
                if (activeHeight < newHeight) { //ensure were not adding unneccessary levels
                    activeHeight++;
                    SkipListSetItem<T> newHead = new SkipListSetItem<>((T) (Integer) Integer.MIN_VALUE);  //
                    newHead.down = head[activeHeight - 1];                                                //  links up the head nodes
                    head[activeHeight - 1].up = newHead;                                                  //     up and down
                    head[activeHeight] = newHead;                                                         //
                    tmp.up = newHead;  //since the up was null we're assigning it to the new head node
                } else{
                    break;
                }
            }
            tmp = tmp.up;  //moving up the node we used to insert our newNode to the next level

            //creating a new upper node of our new node and linking them vertically to each other
            SkipListSetItem<T> newUpperNode = new SkipListSetItem<>(e);
            newUpperNode.down = lowerNode;
            lowerNode.up = newUpperNode;

            //now that tmp is on the next level we can connect horizontally
            newUpperNode.previous = tmp;
            if (tmp.next != null) {   //checks to see if there are other nodes in the same level that newUpperNode might insert btwn
                newUpperNode.next = tmp.next;
                tmp.next.previous = newUpperNode;
            }
            //if not it will set equal to null
            tmp.next = newUpperNode;

            //setting the newUpperNode to be the lowerNode
            lowerNode = newUpperNode;
            //incrementing level to move on to the next if needed
            level++;
        }

        if (nodes.isEmpty()) { //adding elements into the node structure
            nodes.add(new LinkedList<>());
        }
        nodes.getFirst().add(newNode);

        return true;
    }

    private void updateMaxHeight() {
        int calculatedHeight = (int) Math.floor(Math.log(numElements) / Math.log(2));  //will take log2(#nodes) and give the new maxHeight
        maxHeight = Math.max(calculatedHeight, 1); //my height is 1-base index so 1 and 2 #nodes will have maxHeight of 1
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean remove(Object o) {
        SkipListSetItem<T> currentNode = search((T)o);  //search returns the node from the bottom layer so we traverse until node is found
        if (currentNode == null) { //if node not found return false
            return false;
        }

        while(currentNode != null) {
            if(currentNode.previous != null) {  //starts at bottom level, manipulates the pointers
                currentNode.previous.next = currentNode.next;
            }
            if(currentNode.next != null){
                currentNode.next.previous = currentNode.previous;
            }

            currentNode = currentNode.up;  //goes up a level, manipulates the pointers, until its up is null
        }

        while(activeHeight > 1 && head[activeHeight].next == null){ //removing levels that only contain null
            head[activeHeight] = null;
            activeHeight--;
        }
        numElements--;  //node removed, elements decreased

        return true;
    }

    public void reBalance() {
        ArrayList<T> rebalanceNodes = new ArrayList<>();
        SkipListSetItem<T> node = head[0].getNext();

        while (node != null) {  //storing all the nodes in an array
            rebalanceNodes.add(node.value);
            node = node.next;
        }

        clear();  //clearing the skip list, leaving the bottom level initialized to a head node (-infinity)

        this.addAll(rebalanceNodes);  //adds all the elements back only the height of nodes change not the skiplist height
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object element : c){    //loops through each element in the collection and checks if the element exists
            if(!contains(element)){
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean modified = false;

        for(T element : c){  //loops through each element in the collection and adds, if it already exists add returns false
            if(add(element)){
                modified = true;
            }
        }
        return modified; //returns true if non-existing elements were added
    }

    @Override
    public boolean retainAll(Collection<?> c) {
       Iterator<T> iterator = this.iterator();
       boolean modified = false;

       while(iterator.hasNext()){     //loops through the bottom layer
           T element = iterator.next();
           if(!c.contains(element)){  //if the collection does not have an element from the bottom layer
               iterator.remove();   //it removes
               modified = true;
           }
       }
        return modified;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        if(c == null){
            throw new NullPointerException();
        }
        try {
            boolean modified = false;
            for (Object element : c) {  //for every element in the collection
                SkipListSetItem<T> node = search((T) element); //search to find the element we want to delete

                if (node != null && node.value.equals(element)) { //once found we remove
                    remove((T) element);
                    modified = true;
                }
            }
            return modified;

        } catch(ClassCastException e) {
            throw new ClassCastException();
        }
    }

    @Override
    public void clear() {
        for (int i = 0; i <= activeHeight; i++) {
            head[i] = null;   //setting all the head nodes to null
        }
        head[0] = new SkipListSetItem<>((T) (Integer) Integer.MIN_VALUE);  //initializing the (-infinity)

        activeHeight = 0;
        numElements = 0;

        nodes.clear();  //clearing the nodes structure
    }

    @Override
    public T first() {
        if (numElements == 0 || head[0].next == null) {
            throw new NoSuchElementException();
        }
        return head[0].next.value;
    }


    @Override
    public T last() {
        if (numElements == 0 || head[0].next == null) {
            throw new NoSuchElementException();
        }
        SkipListSetItem<T> node = head[0];

        while (node.next != null) {
            node = node.next;
        }

        return node.value;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        SkipListSetItem<T> current = head[0].next;
        while (current != null) {
            if (current.value != null) {
                hash += current.value.hashCode();  //summing up all the values in the bottom layer
            }
            current = current.next;
        }
        return hash;
    }

    @Override
    public boolean equals(Object o ) {
        if (o == null) {
            return false;
        }

        Set<T> o1 = null;
        if (o instanceof Set<?>) {  //checks if 'o' is compatible type
            o1 = (Set<T>) o;  //casting Set<T> so I can get the size
            if (o1.size() == this.size()){ //comparing sizes
                for (T element : this) {
                    if (!contains(element)) { //if any of the elements don't match returns false
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public Comparator<? super T> comparator() {
        return comp;
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        throw new UnsupportedOperationException();
    }
}

