package roadNetwork;

import index.grid.GridEdge;
import index.grid.GridPoint;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import match.MatchResult;
import util.Constants;

public class Graph {
	private HashMap<Long, Edge> edges;
	private HashMap<Long, Vertex> vertices;
	private MBR mbr;
	private GridPoint vertexIndex;
	private GridEdge edgeIndex;
	
	private double edgeCellSize = 500 * Constants.D_PER_M;
    private double vertexCellSize = 100 * Constants.D_PER_M;
    private long base_id = 100000000000l;

	public HashMap<Long, Edge> getEdges() {
		return edges;
	}

	public HashMap<Long, Vertex> getVertices() {
		return vertices;
	}
	
	public GridPoint getVertexIndex() {
		return vertexIndex;
	}

	public GridEdge getEdgeIndex() {
		if (this.edgeIndex == null)
        {
            this.edgeIndex = new GridEdge(edges.values(), mbr, edgeCellSize);
        }
        return edgeIndex; 
	}

	public Graph(String vertexFile, String edgeFile, String geometryFile) {
		loadVertices(vertexFile);
		loadEdges(edgeFile);
		loadGeometry(geometryFile);
		buildRNIndex();
	}

	private void buildRNIndex() {
		this.vertexIndex = new GridPoint(vertices.values(), mbr, vertexCellSize);
	}

	private void loadVertices(String fileName) {
		this.mbr = MBR.EMPTY;
		vertices = new HashMap<Long, Vertex>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split("\t");
				if (fields.length == 3) {
					long id = Long.parseLong(fields[0]);
					double lat = Double.parseDouble(fields[1]);
					double lng = Double.parseDouble(fields[2]);
					Vertex v = new Vertex(id, lat, lng);
					vertices.put(id, v);
					this.mbr.include(new GeoPoint(lat, lng));
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void loadEdges(String fileName)
    {
        edges = new HashMap<Long, Edge>();
        try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split("\t");
				long id = Long.parseLong(fields[0]);
				long startId = Long.parseLong(fields[1]);
				long endId = Long.parseLong(fields[2]);
				Vertex start = vertices.get(startId);
                Vertex end = vertices.get(endId);
                Edge e = new Edge(id, start, end);
                edges.put(id, e);
                start.registerEdge(e);
                end.registerEdge(e);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
	
	private void loadGeometry(String fileName)
    {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] fields = line.split("\t",2);
				if(fields.length == 2){
					long edgeId = Long.parseLong(fields[0]);
					Edge e = this.edges.get(edgeId);
					e.setGeoString(fields[1]);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
       
    }
	
	public HashSet<Edge> rangeQuery(GeoPoint p, double radius)
    {
        return getEdgeIndex().rangeQuery(p, radius);
    }
	
	public HashSet<Edge> rangeQuery(GeoPoint p, double radius, double maxRadius)
    {
        return rangeQuery(p, radius,maxRadius,3);
    }
	
	public HashSet<Edge> rangeQuery(GeoPoint p, double radius, double maxRadius, int minSize)
    {
        HashSet<Edge> result = null;
        while (radius <= maxRadius && (result == null || result.size() <= minSize))
        {
            result = rangeQuery(p, radius);
            radius *= 2;
        }
        return result;
    }
	
	public HashSet<Vertex> vertexRangeQuery(MBR rect)
    {
        return this.vertexIndex.rangeQuery(rect);
    }
	
	public HashSet<Vertex> vertexRangeQuery(GeoPoint p, double radius)
    {
        double minLat, minLng, maxLat, maxLng;
        double d_radius = radius * Constants.D_PER_M;	//radius in degree
        minLng = p.getLng() - d_radius;
        maxLng = p.getLng() + d_radius;
        minLat = p.getLat() - d_radius;
        maxLat = p.getLat() + d_radius;
        MBR rect = new MBR(minLng, minLat, maxLng, maxLat);
        return this.vertexIndex.rangeQuery(rect);
    }

}
