import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

/**
 * GridSpanLayout is a simple layout manager similar to
 * {@link java.awt.GridLayout}, but allows components to span multiple rows and
 * columns. Places each component on the next available column starting from the
 * top row.
 * 
 * @author bitinvert.com
 */

@SuppressWarnings("serial")
class GridSpanLayout implements LayoutManager,java.io.Serializable {
	/**
	 * Generated serial
	 */
	//	private static final long serialVersionUID = -1355819205021504244L;
	
	/**
	 * Number of rows for the grid. Immutable. Must be a non-negative integer.
	 */
	private final int rows;
	
	/**
	 * Number of columns for the grid. Immutable. Must be a non-negative integer.
	 */
	private final int cols;
	
	/**
	 * Gap between rows, measured in pixels. Must be a non-negative integer.
	 */
	private int rowGap;
	
	/**
	 * Gap between columns, measured in pixels. Must be a non-negative integer.
	 */
	private int colGap;
	
	/**
	 * Table that keeps track of spanned and allocated areas. The first dimension
	 * represents rows. The second dimension represents columns. The third
	 * dimension holds two integers; the first integer represents the row span of
	 * the area, and the second integer represents the column span of the area.
	 * Spans must be non-negative integers. A span of 0 denotes that the area is
	 * being allocated by another area.
	 */
	private int spans[][][];
	
	//// commented for file size reduction
	//	/**
	//	 * Creates a GridSpanLayout with the specified number of rows and columns.
	//	 * Each area in the layout is given an equal size.
	//	 *
	//	 * @param rows
	//	 *          number of rows
	//	 * @param cols
	//	 *          number of columns
	//	 */
	//	public GridSpanLayout(int rows, int cols) {
	//		this(rows,cols,0,0);
	//	}
	
	/**
	 * Creates a GridSpanLayout with the specified number of rows and columns, and
	 * with the specified spacing between rows and columns. Each area in the
	 * layout is given an equal size.
	 * 
	 * @param rows
	 *          number of rows
	 * @param cols
	 *          number of columns
	 * @param rowGap
	 *          gap between rows
	 * @param colGap
	 *          gap between columns
	 */
	public GridSpanLayout(int rows, int cols, int rowGap, int colGap) {
		this.rows = rows;
		this.cols = cols;
		this.rowGap = rowGap;
		this.colGap = colGap;
		spans = new int[rows][cols][2]; // [2] = [rowSpan,colSpan]
		for(int i = 0;i < rows;i++) {
			for(int j = 0;j < cols;j++) {
				spans[i][j][0] = spans[i][j][1] = 1;
			}
		}
	}
	
	/**
	 * Sets the amount of rows and columns that an area in the layout grid spans.
	 * 
	 * @param row
	 *          row of the area
	 * @param col
	 *          column of the area
	 * @param rowSpan
	 *          number of rows the area should span
	 * @param colSpan
	 *          number of columns the area should span
	 */
	public void setSpan(int row, int col, int rowSpan, int colSpan) {
		//// commented for file size reduction
		//		// <Validate>
		//		if(spans[row][col][0] == 0 || spans[row][col][1] == 0) {
		//			throw new IllegalArgumentException("Block currently allocated");
		//		}
		//		if(rowSpan < 1 || colSpan < 1)
		//			throw new IllegalArgumentException("rowSpan and colSpan must be >=1");
		//		if((rowSpan + row > rows) || (colSpan + col > cols)) {
		//			throw new IllegalArgumentException(
		//			    "rowSpan and colSpan must be within the table range");
		//		}
		//		for(int i = row + 1;i < row + rowSpan;i++) {
		//			for(int j = col + 1;j < col + colSpan;j++) {
		//				boolean rowSpanned = spans[i][j][0] != 1;
		//				boolean colSpanned = spans[i][j][1] != 1;
		//				if(rowSpanned || colSpanned) {
		//					throw new IllegalArgumentException("Attempted to span into allocated area");
		//				}
		//			}
		//		}
		//		// </Validate>
		//
		//		// <Reset>
		//		int oldRowSpan = spans[row][col][0];
		//		int oldColSpan = spans[row][col][1];
		//		for(int i = row;i < row + oldRowSpan;i++) {
		//			for(int j = col;j < col + oldColSpan;j++) {
		//				spans[i][j][0] = spans[i][j][1] = 1;
		//			}
		//		}
		//		// </Reset>
		
		// <Allocate>
		for(int i = row;i < row + rowSpan;i++) {
			for(int j = col;j < col + colSpan;j++) {
				spans[i][j][0] = spans[i][j][1] = 0;
			}
		}
		spans[row][col][0] = rowSpan;
		spans[row][col][1] = colSpan;
		// </Allocate>
	}
	
	//// commented for file size reduction
	//	public int getRows() {
	//		return rows;
	//	}
	//
	//	public int getRowGap() {
	//		return rowGap;
	//	}
	//
	//	public int getCols() {
	//		return cols;
	//	}
	//
	//	public int getColGap() {
	//		return colGap;
	//	}
	//
	//	public void setRowGap(int rowGap) {
	//		this.rowGap = rowGap;
	//	}
	//
	//	public void setColGap(int colGap) {
	//		this.colGap = colGap;
	//	}
	//
	//	public int[][][] getSpans() {
	//		return spans;
	//	}
	//
	//	public void resetSpans() {
	//		for(int i = 0;i < rows;i++) {
	//			for(int j = 0;j < cols;j++) {
	//				spans[i][j][0] = spans[i][j][1] = 1;
	//			}
	//		}
	//	}
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}
	
	@Override
	public void removeLayoutComponent(Component comp) {
	}
	
	/**
	 * Determines the preferred size of the container argument using this layout.
	 * <p>
	 * The preferred width is determined by the width of the area that prefers the
	 * most width, multiplied by the number of columns, plus the width of the gaps
	 * between the columns, plus the left and right insets of the container.
	 * <p>
	 * The preferred height is determined by the height of the area that prefers
	 * the most height, multiplied by the number of rows, plus the height of the
	 * gaps between the rows, plus the top and bottom insets of the container.
	 * 
	 * @param parent
	 *          container in which to do the layout
	 * @return the preferred dimensions to lay out the subcomponents of the
	 *         specified container
	 */
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return getLayoutSize(parent,false);
	}
	
	/**
	 * Determines the minimum size of the container argument using this layout.
	 * <p>
	 * The minimum width is determined by the width of the area that requires the
	 * most width, multiplied by the number of columns, plus the width of the gaps
	 * between the columns, plus the left and right insets of the container.
	 * <p>
	 * The minimum height is determined by the height of the area that requires
	 * the most height, multiplied by the number of rows, plus the height of the
	 * gaps between the rows, plus the top and bottom insets of the container.
	 * 
	 * @param parent
	 *          container in which to do the layout
	 * @return the minimum dimensions to lay out the subcomponents of the
	 *         specified container
	 */
	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return getLayoutSize(parent,true);
	}
	
	private Dimension getLayoutSize(Container parent, boolean minimum) {
		synchronized(parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			int w = 0;
			int h = 0;
			Component comps[] = parent.getComponents();
			int comp = 0;
			for(int i = 0;(i < rows) && (comp < comps.length);i++) {
				for(int j = 0;(j < cols) && (comp < comps.length);j++) {
					int rowSpan = spans[i][j][0];
					int colSpan = spans[i][j][1];
					boolean isSource = rowSpan != 0 && colSpan != 0;
					if(isSource) {
						Dimension d;
						if(minimum)
							d = comps[comp++].getMinimumSize();
						else
							d = comps[comp++].getPreferredSize();
						if(w < d.width / colSpan) {
							w = d.width / colSpan;
						}
						if(h < d.height / rowSpan) {
							h = d.height / rowSpan;
						}
					}
				}
			}
			// @formatter:off
			return new Dimension(
				insets.left + insets.right + cols * w + (cols - 1) * colGap,
				insets.top + insets.bottom + rows * h + (rows - 1) * rowGap);
			// @formatter:on
		}
	}
	
	/**
	 * Lays out the specified container using this layout's constraints.
	 * 
	 * @param parent
	 *          container in which to do the layout
	 */
	@Override
	public void layoutContainer(Container parent) {
		synchronized(parent.getTreeLock()) {
			Insets insets = parent.getInsets();
			Component comps[] = parent.getComponents();
			
			if(comps.length == 0)
				return;
			
			int comp = 0;
			
			int wGaps = (cols - 1) * colGap;
			int wNoInsets = parent.getWidth() - (insets.left + insets.right);
			int wComponent = (wNoInsets - wGaps) / cols;
			int wExtra = (wNoInsets - (wComponent * cols + wGaps)) / 2;
			
			int hGaps = (rows - 1) * rowGap;
			int hNoInsets = parent.getHeight() - (insets.top + insets.bottom);
			int hComponent = (hNoInsets - hGaps) / rows;
			int hExtra = (hNoInsets - (hComponent * rows + hGaps)) / 2;
			
			/* wExtra and hExtra are used to center the layout in the parent container */

			int y = insets.top + hExtra;
			for(int r = 0;(r < rows) && (comp < comps.length);r++) {
				int x = insets.left + wExtra;
				for(int c = 0;(c < cols) && (comp < comps.length);c++) {
					int rowSpan = spans[r][c][0];
					int colSpan = spans[r][c][1];
					boolean isSource = rowSpan != 0 || colSpan != 0;
					if(isSource) {
						int w = (wComponent + colGap) * colSpan - colGap;
						int h = (hComponent + rowGap) * rowSpan - rowGap;
						comps[comp++].setBounds(x,y,w,h);
					}
					x += wComponent + colGap;
				}
				y += hComponent + rowGap;
			}
		}
	}
	
	//// commented for file size reduction
	//	/**
	//	 * @return the string representation of this layout's configuration.
	//	 */
	//	@Override
	//	public String toString() {
	//		String table = "[\n";
	//		for(int i = 0;i < rows;i++) {
	//			for(int j = 0;j < cols;j++) {
	//				int rowSpan = spans[i][j][0];
	//				int colSpan = spans[i][j][1];
	//				if(rowSpan > 1 || colSpan > 1) {
	//					table += " (" + i + "," + j + ")=[" + rowSpan + "," + colSpan + "]\n";
	//				}
	//			}
	//		}
	//		table += "]";
	//		return getClass().getName() + "[rows=" + rows + ",cols=" + cols + ",rowGap=" + rowGap
	//		    + ",colGap=" + colGap + "]\n" + table;
	//	}
}
