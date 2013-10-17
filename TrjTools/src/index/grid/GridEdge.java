package index.grid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import roadNetwork.DistanceType;
import roadNetwork.Edge;
import roadNetwork.GeoPoint;
import roadNetwork.MBR;
import util.Constants;

public class GridEdge {
	private Map<Integer, List<Edge>> dict = new HashMap<Integer, List<Edge>>();

	private int nCol;
	private int nRow;
	private double cellSize;
	private MBR mbr;
	
	public GridEdge(Collection<Edge> edges, MBR mbr, double cellSize)
    {
		nCol = (int) (Math.ceil(mbr.getWidth() / cellSize));
		nRow = (int) (Math.ceil(mbr.getHeight() / cellSize));
        this.cellSize = cellSize;
        this.mbr = mbr;
        buildIndex(edges);
    }
	private int getRow(double lat)
    {
        if (lat <= mbr.getMinLat())
        {
            return 0;
        }
        if (lat >= mbr.getMaxLat())
        {
            return nRow - 1;
        }
        return (int)((lat - mbr.getMinLat()) / cellSize);
    }
	private int getColumn(double lng)
    {
        if (lng <= mbr.getMinLng())
        {
            return 0;
        }
        if (lng >= mbr.getMaxLng())
        {
            return nCol - 1;
        }
        return (int)((lng - mbr.getMinLng()) / cellSize);
    }
	private int getCell(GeoPoint p)
    {
        int row = getRow(p.getLat());
        int col = getColumn(p.getLng());
        return row * nCol + col;
    }
	private void buildIndex(Collection<Edge> edges)
    {
        //insert edges into the grid
        for(Edge e :edges)
        {
            List<Integer> ids = getCells(e);
            for (int j = 0; j < ids.size(); j++)
            {
                List<Edge> list = null;
                boolean got = dict.containsKey(ids.get(j));
                if (!got)
                {
                    list = new ArrayList<Edge>();
                    dict.put(ids.get(j), list);
                }
                list = dict.get(ids.get(j));
                list.add(e);
            }
        }
    }
	private List<Integer> getCells(Edge e)
    {
        return getCells(e.getMbr());
    }
	private List<Integer> getCells(MBR mbr)
    {
        List<Integer> rst = new ArrayList<Integer>();
        int c1 = getCell(mbr.getTopLeft());
        int c2 = getCell(mbr.getBottomRight());
        int c1col = c1 % nCol;
        int c2col = c2 % nCol;

        int c1row = (c1 - c1col) / nCol;
        int c2row = (c2 - c2col) / nCol;

        int ncol = c2col - c1col + 1;
        int nrow = c2row - c1row + 1;
        for (int i = 0; i < nrow; i++)
        {
            for (int j = 0; j < ncol; j++)
            {
                rst.add(c1col + j + (c1row + i) * nCol);
            }
        }
        return rst;
    }
	
	public HashSet<Edge> rangeQuery(GeoPoint p, double radius)
    {
        HashSet<Edge> result = new HashSet<Edge>();
        List<Integer> cands = null;
        //get mbr
        double d_radius = radius * Constants.D_PER_M;	//radius in degree
        double minLat, minLng, maxLat, maxLng;
        double radius2 = radius * radius;
        minLng = p.getLng() - d_radius;
        maxLng = p.getLng() + d_radius;
        minLat = p.getLat() - d_radius;
        maxLat = p.getLat() + d_radius;
        MBR rect = new MBR(minLng, minLat, maxLng, maxLat);
        cands = getCells(rect);
        int cands_count = cands.size();
        for (int i = 0; i < cands_count; i++)
        {
            List<Edge> edges = null;
            boolean got = dict.containsKey(cands.get(i));
            if (got)
            {
            	edges = dict.get(cands.get(i));
                int count = edges.size();
                for (int j = 0; j < count; j++)
                {
                	DistanceType dt = edges.get(j).dist2From(p);
                    if (dt.distance <= radius2)
                    {
                        result.add(edges.get(j));
                    }
                }
            }
        }
        return result;
    }
	
	
}
