package index.grid;

import java.util.*;

import roadNetwork.GeoPoint;
import roadNetwork.MBR;
import roadNetwork.Vertex;

public class GridPoint {
	private Map<Integer, List<Vertex>> dict = new HashMap<Integer, List<Vertex>>();

	private int nCol;
	private int nRow;
	private double cellSize;
	private MBR mbr;

	public GridPoint(Collection<Vertex> vertices, MBR mbr, double cellSize) {
		nCol = (int) (Math.ceil(mbr.getWidth() / cellSize));
		nRow = (int) (Math.ceil(mbr.getHeight() / cellSize));
		this.cellSize = cellSize;
		this.mbr = mbr;
		buildIndex(vertices);
	}

	private void buildIndex(Collection<Vertex> vertices) {
		for (Vertex v : vertices) {
			int id = getCell(v.toPoint());
			List<Vertex> list = null;
			boolean got = dict.containsKey(id);
			if (!got) {
				list = new ArrayList<Vertex>();
				dict.put(id, list);
			}
			list = dict.get(id);
			list.add(v);
		}
	}

	private int getCell(GeoPoint p) {
		int row = getRow(p.getLat());
		int col = getColumn(p.getLng());
		return row * nCol + col;
	}

	private int getRow(double lat) {
		if (lat <= mbr.getMinLat()) {
			return 0;
		}
		if (lat >= mbr.getMaxLat()) {
			return nRow - 1;
		}
		return (int) ((lat - mbr.getMinLat()) / cellSize);
	}

	private int getColumn(double lng) {
		if (lng <= mbr.getMinLng()) {
			return 0;
		}
		if (lng >= mbr.getMaxLng()) {
			return nCol - 1;
		}
		return (int) ((lng - mbr.getMinLng()) / cellSize);
	}

	private List<Integer> getCells(MBR mbr) {
		List<Integer> rst = new ArrayList<Integer>();
		int c1 = getCell(mbr.getTopLeft());
		int c2 = getCell(mbr.getBottomRight());
		int c1col = c1 % nCol;
		int c2col = c2 % nCol;

		int c1row = (c1 - c1col) / nCol;
		int c2row = (c2 - c2col) / nCol;

		int ncol = c2col - c1col + 1;
		int nrow = c2row - c1row + 1;
		for (int i = 0; i < nrow; i++) {
			for (int j = 0; j < ncol; j++) {
				rst.add(c1col + j + (c1row + i) * nCol);
			}
		}
		return rst;
	}

	public HashSet<Vertex> rangeQuery(MBR rect) {
		HashSet<Vertex> result = new HashSet<Vertex>();
		List<Integer> cands = getCells(rect);
		int cands_count = cands.size();
		for (int i = 0; i < cands_count; i++) {
			List<Vertex> vertices = null;
			boolean got = dict.containsKey(cands.get(i));
			if (got) {
				vertices = dict.get(cands.get(i));
				for (Vertex v : vertices) {
					if (rect.cover(v.toPoint())) {
						result.add(v);
					}
				}
			}
		}
		return result;
	}
	
	
}
