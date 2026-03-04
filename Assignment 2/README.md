# Ring Buffer (Single Writer, Multiple Readers)

## Project Overview

This project implements a **thread-safe Ring Buffer** in Java that supports **one writer and multiple independent readers**. The buffer has a **fixed capacity N** and stores data in a circular structure. A single writer continuously writes items into the buffer, while multiple readers are able to read items independently without interfering with each other.

Unlike traditional queue structures where reading removes elements, this ring buffer allows **multiple readers to access the same data without deleting it from the buffer**. Each reader maintains its own reading position, which allows readers to consume data at different speeds.

When the buffer becomes full, the writer is allowed to **overwrite the oldest data**. If a reader is too slow and the data it wanted to read has already been overwritten, the reader will **skip the missing items and continue reading from the oldest available element**. In such cases the reader receives information about how many items were missed.

This design satisfies the assignment requirements:

- Fixed capacity buffer
- Single writer
- Multiple independent readers
- Reading does not remove items
- Writer can overwrite old data
- Slow readers may miss items

---

# Design Explanation

The solution follows **Object-Oriented Design principles** by separating responsibilities between multiple classes instead of placing everything into a single class. Each class has a clearly defined role in the system.

The main components of the design are:

- RingBuffer
- RingBufferReader
- ReadResult
- Slot (internal helper class)

---

# RingBuffer

The **RingBuffer** class represents the core data structure that stores the elements.

Responsibilities of this class include:

- Managing the fixed-size circular storage
- Tracking the global write position
- Handling overwrite logic
- Creating reader instances
- Ensuring thread safety using a read-write lock

The buffer internally stores elements in an array of **Slot objects**. Each slot stores both the data and the sequence number of the element written to that position. The sequence number allows the system to detect when data has been overwritten.

Important attributes:

- `capacity` – maximum number of elements the buffer can hold
- `writeSequence` – global counter that increases with each write operation
- `slots` – circular storage array
- `lock` – `ReentrantReadWriteLock` used to synchronize concurrent access

The writer always writes to the index:
index = sequence % capacity

If the buffer is full, the writer simply overwrites the oldest entry.

---

# RingBufferReader

The **RingBufferReader** class represents an independent reader that consumes data from the buffer.

Each reader maintains its own position using:
nextSequenceToRead

This means that:

- Multiple readers can read from the same buffer simultaneously
- One reader does not affect the progress of another reader
- Reading does not remove elements from the buffer

The reader interacts with the buffer through the method:
read()

The reader requests data from the buffer using its current sequence number and then advances its position after successfully reading an item.

If a reader falls behind and the requested data has already been overwritten, the reader will jump forward to the oldest available data and will be informed about how many elements were missed.

---

# ReadResult

The **ReadResult** class represents the result of a read operation.

Instead of returning only the data element, the system returns a structured result that includes additional information about missed items.

The class contains two attributes:

- `item` – the element that was read (can be null if no new data exists)
- `missedCount` – number of elements that were overwritten before the reader could read them

This allows the system to inform readers when they have fallen behind the writer.

---

# Slot (Internal Class)

The **Slot** class represents a single position inside the circular buffer.

Each slot stores:

- `sequence` – the sequence number assigned to the stored item
- `value` – the actual data element

The sequence number helps determine whether the data in the slot is still valid or has already been overwritten.

This mechanism is essential for detecting **missed data when readers fall behind**.

---

# Thread Safety

The implementation uses **ReentrantReadWriteLock** to ensure thread safety.

Two types of locks are used:

### Write Lock
Used by the writer during write operations to guarantee exclusive access when modifying the buffer.

### Read Lock
Used by readers when reading from the buffer so that multiple readers can access the data concurrently.

This approach allows:

- One writer
- Multiple readers simultaneously

while maintaining data consistency.

---

# Overwrite Behavior

Because the buffer has a fixed size, older entries must eventually be overwritten when the writer continues producing data.

The oldest available sequence in the buffer is calculated as:
oldestAvailable = max(0, writeSequence - capacity)

If a reader attempts to read a sequence smaller than this value, it means that the requested data has already been overwritten.

In this case:

1. The reader calculates how many elements were missed.
2. The reader jumps forward to the oldest available sequence.
3. The reader continues reading from that position.

---

# UML Class Diagram

The following class diagram illustrates the relationships between the main components of the system.

<img width="1158" height="346" alt="laman class" src="https://github.com/user-attachments/assets/b0f297e1-2f17-4e48-8b73-34a0d8d48b78" />


The diagram shows:

- `RingBuffer` containing multiple `Slot` objects
- `RingBuffer` creating multiple `RingBufferReader` instances
- `RingBufferReader` reading data from the buffer
- `ReadResult` representing the result of read operations

This structure ensures clear separation of responsibilities and supports multiple independent readers.

---

# Sequence Diagram – write()

The write sequence diagram shows how the writer inserts a new element into the buffer.

<img width="548" height="518" alt="laman write" src="https://github.com/user-attachments/assets/6e4197ef-03c1-4d80-a10d-7d9ecf4600eb" />


Main steps in the write operation:

1. The writer calls `write(item)` on the RingBuffer.
2. The buffer acquires the write lock.
3. The buffer calculates the correct index using the sequence number.
4. The value and sequence number are stored in the corresponding slot.
5. The global write sequence is incremented.
6. The write lock is released.

If the buffer is full, the new element overwrites the oldest slot automatically.

---

# Sequence Diagram – read()

The read sequence diagram shows how a reader retrieves an item from the buffer.

<img width="924" height="775" alt="laman read" src="https://github.com/user-attachments/assets/445580db-f8be-420e-a293-08fd17704da1" />


The reading process follows three possible scenarios:

### No New Data

If the reader's sequence is greater than or equal to the current write sequence, no new data is available and an empty result is returned.

### Reader Missed Data

If the reader's sequence is smaller than the oldest available sequence, the reader has fallen behind and some data has already been overwritten. The reader jumps forward to the oldest available element and receives information about how many items were missed.

### Normal Read

If the reader's sequence is within the valid range, the reader retrieves the item directly and advances its reading position.

This logic allows readers to operate independently while still supporting buffer overwrites.

---

# How to Run the Project

1. Clone the repository
git clone <repository-url>

2. Navigate to the project directory
cd ring-buffer-project

3. Compile the Java files
javac *.java

4. Run the demo program
java Demo

The demo program demonstrates writing items into the buffer and reading them using multiple readers.

---

# Conclusion

This implementation demonstrates a robust **Single Writer / Multiple Reader Ring Buffer** using object-oriented design and proper synchronization techniques.

Key features of the solution include:

- Fixed-capacity circular buffer
- Independent readers with separate cursors
- Safe concurrent access
- Overwrite support
- Detection of missed data

