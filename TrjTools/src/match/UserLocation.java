package match;

import roadNetwork.GeoPoint;

public class UserLocation {
	private String direction;
	private GeoPoint location;
	
	public UserLocation(GeoPoint location,String direction){
		this.location = location;
		this.direction = direction;
	}
	
	public double getLng(){
		return this.location.getLng();
	}
	
	public double getLat(){
		return this.location.getLat();
	}
	
	public void setLng(double lng){
		this.location.setLng(lng);
	}
	
	public void setLat(double lat){
		this.location.setLat(lat);
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public GeoPoint getLocation() {
		return location;
	}

	public void setLocation(GeoPoint location) {
		this.location = location;
	}
	
	

}
