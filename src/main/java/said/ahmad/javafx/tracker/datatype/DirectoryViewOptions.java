package said.ahmad.javafx.tracker.datatype;

import javafx.scene.control.TableColumn;

/**
 * Datatype that use one integer in background to save properties of columns
 * such as visibility and order.<br>
 * The idea behind is using bit operator to optimize data size and load
 */
public class DirectoryViewOptions {
	// Remember bit operators:
	// ~ used for not.
	// >> right shift
	// << left shift
	// & and
	// | or
	// check bit at position k: ((value >> k) & 1) == 0 or 1
	// set a bit at position k: value | (1 << k)
	// unset bit at position k: value & ~(1 << k)

	// -------------- Convention on column preference -----------------
	// Format 32 bit: X000 0000 0000 PPPP PPPP 0000 0ASV

	// -- X: MSB unused bit to get rid of conflict of arithmetic shift
	// a bit at position: 31

	// -- V: boolean stand for Visibility Check -> 1 Visible, 0 hidden.
	// a bit at position: 0
	// booleans used for visibility of columns
	// code snippet:
	// getValue using desiredCol.isVisible()
	// setValue using desiredCol.setVisible(boolean);

	// -- AS: boolean stand for sort Check
	// S -> 1 included in sort, 0 discarded.
	// D -> 1 DESCENDING sortType, 0 ASCENDING sort type
	// a bit at position: 1
	// we can use sort order
	// code snippet:
	// getValue using table.getSortOrder().get(index) == desiredCol
	// setValue using table.getSortOrder().add(tableColumn)
	// this will give it the least priority since it is inserted at the end

	// -- PPPP PPPP: byte stand for priority used in sort
	// -> 0 top priority... 255 least
	// a byte at position: 8
	// code snippet:
	// getValue table.getSortOrder().indexOf(desiredColumn)
	// setValue table.getSortOrder().insert(byte, desiredColumn)
	// implementation may differ just a general idea

	private int iconColumn = 1; // visible default, and not used to sort by
	private int nameColumn = 1;
	private int noteColumn = 0;
	private int sizeColumn = 0;
	private int dateModifiedColumn = 0;
	private int hBoxActionColumn = 1;

	public boolean isColumnVisible(COLUMN column) {
		// mask with first bit (1 = ..0001)
		int mask = 0b001;
		switch (column) {
			case ICON :
				return (iconColumn & mask) == mask;
			case NAME :
				return (nameColumn & mask) == mask;
			case NOTE :
				return (noteColumn & mask) == mask;
			case SIZE :
				return (sizeColumn & mask) == mask;
			case DATE_MODIFIED :
				return (dateModifiedColumn & mask) == mask;
			case HBOX_ACTION :
				return (hBoxActionColumn & mask) == mask;
			default :
				System.err.println("Column visibility check option not defined yet for this column: " + column);
				return false;
		}
	}

	public void setColumnVisible(COLUMN column, boolean isVisible) {
		// clear the first bit
		// then 'OR' to copy isVisible value
		int mask = 0b001;
		switch (column) {
			case ICON :
				iconColumn = (iconColumn & ~(mask)) | (isVisible ? mask : 0);
				break;
			case NAME :
				nameColumn = (nameColumn & ~(mask)) | (isVisible ? mask : 0);
				break;
			case NOTE :
				noteColumn = (noteColumn & ~(mask)) | (isVisible ? mask : 0);
				break;
			case SIZE :
				sizeColumn = (sizeColumn & ~(mask)) | (isVisible ? mask : 0);
				break;
			case DATE_MODIFIED :
				dateModifiedColumn = (dateModifiedColumn & ~(mask)) | (isVisible ? mask : 0);
				break;
			case HBOX_ACTION :
				hBoxActionColumn = (hBoxActionColumn & ~(mask)) | (isVisible ? mask : 0);
				break;
			default :
				System.err.println("Column visibility set option is not defined yet for this column: " + column);
		}
	}

	public boolean isColumnSorted(COLUMN column) {
		// mask with second bit (1 << 1 = ..0010)
		int mask = (1 << 1);
		switch (column) {
			case ICON :
				return (iconColumn & mask) == mask;
			case NAME :
				return (nameColumn & mask) == mask;
			case NOTE :
				return (noteColumn & mask) == mask;
			case SIZE :
				return (sizeColumn & mask) == mask;
			case DATE_MODIFIED :
				return (dateModifiedColumn & mask) == mask;
			case HBOX_ACTION :
				return (hBoxActionColumn & mask) == mask;
			default :
				System.err.println("Column sorted check option is not defined yet for this column: " + column);
				return false;
		}
	}

	public TableColumn.SortType getColumnSortType(COLUMN column) {
		// mask with third bit (1 << 1 = ..0100)
		int mask = 0b100;
		boolean isDescending = false;
		switch (column) {
			case ICON :
				isDescending = (iconColumn & mask) == mask;
				break;
			case NAME :
				isDescending = (nameColumn & mask) == mask;
				break;
			case NOTE :
				isDescending = (noteColumn & mask) == mask;
				break;
			case SIZE :
				isDescending = (sizeColumn & mask) == mask;
				break;
			case DATE_MODIFIED :
				isDescending = (dateModifiedColumn & mask) == mask;
				break;
			case HBOX_ACTION :
				isDescending = (hBoxActionColumn & mask) == mask;
				break;
			default :
				System.err.println("Column sorted check option is not defined yet for this column: " + column);
				return TableColumn.SortType.DESCENDING;
		}
		return isDescending ? TableColumn.SortType.DESCENDING : TableColumn.SortType.ASCENDING;
	}

	public void setColumnSorted(COLUMN column, boolean isSorted, TableColumn.SortType sortType) {
		// clear the second and third bit
		int mask = 0b110;
		// then 'OR' to copy the byte value
		int newBits = isSorted ? 1 << 1 : 0; // set sorted value
		// set sort type
		newBits = newBits | (sortType.equals(TableColumn.SortType.DESCENDING) ? 1 << 2 : 0);
		switch (column) {
			case ICON :
				iconColumn = (iconColumn & ~(mask)) | newBits;
				break;
			case NAME :
				nameColumn = (nameColumn & ~(mask)) | newBits;
				break;
			case NOTE :
				noteColumn = (noteColumn & ~(mask)) | newBits;
				break;
			case SIZE :
				sizeColumn = (sizeColumn & ~(mask)) | newBits;
				break;
			case DATE_MODIFIED :
				dateModifiedColumn = (dateModifiedColumn & ~(mask)) | newBits;
				break;
			case HBOX_ACTION :
				hBoxActionColumn = (hBoxActionColumn & ~(mask)) | newBits;
				break;
			default :
				System.err.println("Column sorted set option not defined yet for this column: " + column);
		}
	}

	/**
	 * Return a number between 0 and 255. <br>
	 * 0 the highest priority order. <br>
	 * .... <br>
	 * 255 the least priority order. <br>
	 * 
	 * @param column
	 * @return Number between 0 and 255
	 */
	public int getColumnPrioritySort(COLUMN column) {
		int mask = 0b1111111100000000;
		switch (column) {
			case ICON :
				return (iconColumn & mask) >> 8;
			case NAME :
				return (nameColumn & mask) >> 8;
			case NOTE :
				return (noteColumn & mask) >> 8;
			case SIZE :
				return (sizeColumn & mask) >> 8;
			case DATE_MODIFIED :
				return (dateModifiedColumn & mask) >> 8;
			case HBOX_ACTION :
				return (hBoxActionColumn & mask) >> 8;
			default :
				System.err.println("Column priority get option is not defined yet for this column: " + column);
				return 255;
		}
	}

	/**
	 * Priority to be set must be a positive number between 0 and 255
	 * 
	 * @param column
	 * @param priority
	 *            A number between 0 and 255
	 */
	public void setColumnPrioritySort(COLUMN column, int priority) {
		// clear the second 8th bit
		int mask = 0b1111111100000000;
		// then 'OR' to copy the byte value
		priority = priority << 8;
		switch (column) {
			case ICON :
				iconColumn = (iconColumn & ~(mask)) | priority;
				break;
			case NAME :
				nameColumn = (nameColumn & ~(mask)) | priority;
				break;
			case NOTE :
				noteColumn = (noteColumn & ~(mask)) | priority;
				break;
			case SIZE :
				sizeColumn = (sizeColumn & ~(mask)) | priority;
				break;
			case DATE_MODIFIED :
				dateModifiedColumn = (dateModifiedColumn & ~(mask)) | priority;
				break;
			case HBOX_ACTION :
				hBoxActionColumn = (hBoxActionColumn & ~(mask)) | priority;
				break;
			default :
				System.err.println("Column sorted set option not defined yet for this column: " + column);
		}
	}

	public enum COLUMN {
		ICON, NAME, NOTE, SIZE, DATE_MODIFIED, HBOX_ACTION
	}
}