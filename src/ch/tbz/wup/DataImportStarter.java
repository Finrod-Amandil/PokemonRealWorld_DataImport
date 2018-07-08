package ch.tbz.wup;

import java.util.List;

import ch.tbz.wup.models.Area;
import ch.tbz.wup.models.Spawn;
import ch.tbz.wup.persistence.DbContext;
import ch.tbz.wup.persistence.IDbContext;
import ch.tbz.wup.services.IAreaParser;
import ch.tbz.wup.services.ICoordinateConverter;
import ch.tbz.wup.services.KmlParser;
import ch.tbz.wup.services.WGS84ToLV03Converter;

public class DataImportStarter {
	public static void main(String args[]) {
		save();
	}
	
	private static void save() {
		ICoordinateConverter converter = new WGS84ToLV03Converter();
		IAreaParser parser = new KmlParser(converter);
		IDbContext context = new DbContext();
		
		List<Spawn> spawns = context.getAllSpawns();
		List<Area> areas = parser.readAreas("C:\\Users\\severin.zahler\\Desktop\\zurich.kml");
		
		for (Area area : areas) {
			for (Spawn spawn : spawns) {
				if (spawn.getAreaId() == area.getId()) {
					area.addSpawn(spawn);
				}
			}
		}
		
		for (Area area : areas) {
			System.out.println("ID = " + area.getId() + " Name = " + area.getName() + " Type = " + area.getType().toString() + " Spawn count: " + area.getSpawns().size());
		}
		
		context.saveAreas(areas);
	}
	
	private static void load() {
		IDbContext context = new DbContext();
		List<Area> areas = context.getAllAreas();
		for (Area area : areas) {
			System.out.println("ID = " + area.getId() + " Name = " + area.getName() + " Type = " + area.getType().toString());
		}
	}
}
