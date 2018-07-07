package ch.tbz.wup.services;

import java.util.List;

import ch.tbz.wup.models.Area;

public interface IAreaParser {
	public List<Area> readAreas(String filePath);
}
