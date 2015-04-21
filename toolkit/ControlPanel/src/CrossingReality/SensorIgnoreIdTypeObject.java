package CrossingReality;


/**
 * Provides the representation of a sensor object for use by the 'prevent auto add this session' functionality of
 * the Control Panel with regards to a sensor that has been removed from the Control Panel. Sensors are unique by
 * combination of type & node id, so to store a reference to which sensor it is, we need to store which node id it
 * is on and the type string.
 * @author 060005151
 * @version 15/04/2011
 */
public class SensorIgnoreIdTypeObject {

	private String id;
	private String type;

	public SensorIgnoreIdTypeObject (String theId, String theType) {
		this.id = theId;
		this.type = theType;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean equals(Object obj) {
		if (obj instanceof SensorIgnoreIdTypeObject) {
			if (((SensorIgnoreIdTypeObject) obj).getId().equalsIgnoreCase(this.id) &&
					((SensorIgnoreIdTypeObject) obj).getType().equalsIgnoreCase(this.type)) {
				return true;
			}
		}
		return false;
	}

}
