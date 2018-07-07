package ch.tbz.wup.services;

import java.awt.Point;
import java.awt.Polygon;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import ch.tbz.wup.models.Area;
import ch.tbz.wup.models.AreaType;

public class KmlParser implements IAreaParser {
	
	private ICoordinateConverter _converter;
	
	public KmlParser(ICoordinateConverter converter) {
		_converter = converter;
	}
	
	public List<Area> readAreas(String filePath) {
		//JDOM document builder
	    SAXBuilder builder = new SAXBuilder();
	    
	    try {
	      //Create JDOM document
	      Document doc = builder.build(new File(filePath));

	      //Get first element
	      Element root = doc.getRootElement();
	      
	      //Read areas
	      for (Element placemarkElement : root.getChildren("Placemark")) {
	    	  buildArea(placemarkElement);
	      }
	    }
	    catch (JDOMException e) { 
	      System.out.println(e.getMessage());
	    }  
	    catch (IOException e) { 
	      System.out.println(e);
	    } 
	    
	    return Area.getAllAreas();
	}
	
	private Area buildArea(Element placemarkElement) {
		
		//Parse description into name and type if available
		String description = placemarkElement.getChildText("description");
		String typeName = "";
		String name = "";
		int id = 0;
		
		for (String descriptionPart : description.split("\n")) {
			descriptionPart = descriptionPart.trim();
			if (descriptionPart.startsWith("id:")) {
				id = Integer.parseInt(descriptionPart.split(":")[1]);
			}
			else if (descriptionPart.startsWith("type:")) {
				typeName = descriptionPart.split(":")[1];
			}
			else if (descriptionPart.startsWith("name:")) {
				name = descriptionPart.split(":")[1];
			}
			else  {
				System.err.println("Malformed area polygon description: " + description);
				return null;
			}
		}
		
		//Map typeName to enum value
		AreaType type = AreaType.NONE;
		for (AreaType currentType : AreaType.values()) {
			if (currentType.name().equalsIgnoreCase(typeName)) {
				type = currentType;
				if (type == AreaType.REGIONBOUNDS) {
					return null;
				}
				
				break;
			}
		}
		
		String polygonCoordinates = placemarkElement
			.getChild("Polygon")
			.getChild("outerBoundaryIs")
			.getChild("LinearRing")
			.getChildText("coordinates");
		
		Polygon bounds = new Polygon();
		
		for (String pointCoordinates : polygonCoordinates.split(" ")) {
			String[] coordinates = pointCoordinates.split(",");
			double latitude = Double.parseDouble(coordinates[0]);
			double longitude = Double.parseDouble(coordinates[1]);
			Point pointInLV03 = _converter.convertPoint(latitude, longitude);
			
			if (pointInLV03 == null) {
				return null;
			}
			
			bounds.addPoint(pointInLV03.x, pointInLV03.y);
		}
		
		System.out.println("Added new area part: ID = " + id + " Name = " + name + " Type = " + type.toString());
		return Area.add(id, bounds, name, type);
	}
}
