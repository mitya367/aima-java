package aimax.osm.writer;

import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import aimax.osm.data.MapDataStore;
import aimax.osm.data.entities.EntityAttribute;
import aimax.osm.data.entities.MapNode;
import aimax.osm.data.entities.MapWay;
import aimax.osm.reader.OsmRuntimeException;

/** 
 * Writes a map to file using the standard osm XML format.
 * @author R. Lunde
 */
public class OsmWriter {

	private static Logger LOG = Logger.getLogger("aimax.osm");
	
	/**
	 * Writes all data from <code>mapData</code> to file.
	 */
	public void writeMap(File file, MapDataStore mapData) {
		try  {
			FileOutputStream fs = new FileOutputStream(file);
			OutputStreamWriter writer = new OutputStreamWriter
			(new BufferedOutputStream(fs), "UTF-8");
			writeMap(writer, mapData);
		} catch (FileNotFoundException fnfe) {
			LOG.warning("File does not exist "+file);
		} catch (UnsupportedEncodingException fnfe) {
			LOG.warning("UTF-8 encoding not supported, sorry.");
		}
	}
	
	/**
	 * Reads all data from the file and send it to the sink.
	 */
	public void writeMap(OutputStreamWriter writer, MapDataStore mapData) {

		try {
			StringBuffer text = new StringBuffer();
			text.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			text.append("<osm version=\"0.6\" generator=\"aimax-osm\">\n");
			text.append("<bounds minlat=\"");
			text.append(mapData.getBoundingBox().getLatMin());
			text.append("\" minlon=\"");
			text.append(mapData.getBoundingBox().getLonMin());
			text.append("\" maxlat=\"");
			text.append(mapData.getBoundingBox().getLatMax());
			text.append("\" maxlon=\"");
			text.append(mapData.getBoundingBox().getLonMax());
			text.append("\"/>\n");
			writer.write(text.toString());
			for (MapNode node : mapData.getPOIs())
				writeNode(writer, node);
			for (MapNode node : mapData.getWayNodes())
				writeNode(writer, node);
			for (MapNode node : mapData.getMarks())
				writeNode(writer, node);
			for (MapWay way : mapData.getWays()) 
				writeWay(writer, way);
			writer.write("</osm>\n");
		} catch (IOException e) {
			throw new OsmRuntimeException("Unable to write XML output to file.", e);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					LOG.log(Level.SEVERE, "Unable to close output stream.", e);
				}
			}
		}
	}
	
	public String[] fileFormatDescriptions() {
		return new String[] {"OSM File (osm)"};
	}
	
	public String[] fileFormatExtensions() {
		return new String[] {"osm"};
	}
	
	protected void writeNode(OutputStreamWriter writer, MapNode node) throws IOException {
		StringBuffer text = new StringBuffer();
		text.append("<node id=\"");
		text.append(node.getId());
		text.append("\" lat=\"");
		text.append(node.getLat());
		text.append("\" lon=\"");
		text.append(node.getLon());
		if (node.getAttributes().length == 0) {
			text.append("\"/>\n");
		} else {
			text.append("\">\n");
			addTags(text, node.getName(), node.getAttributes());
			text.append("</node>\n");
		}
		writer.append(text.toString());
	}
	/*<node id="83551472" lat="38.8353186" lon="20.7118425" user="aitolos" uid="653" visible="true" version="2" changeset="4440307" timestamp="2010-04-16T16:35:48Z"/>*/
	protected void writeWay(OutputStreamWriter writer, MapWay way) throws IOException {
		StringBuffer text = new StringBuffer();
		text.append("<way id=\"");
		text.append(way.getId());
		text.append("\">\n");
		for (MapNode node : way.getNodes()) {
			text.append("  <nd ref=\"");
			text.append(node.getId());
			text.append("\"/>\n");
		}
		addTags(text, way.getName(), way.getAttributes());
		text.append("</way>\n");
		writer.append(text.toString());
	}
	/*<way id="25264669" user="mimis" uid="59074" visible="true" version="4" changeset="1973025" timestamp="2009-07-29T08:54:03Z">
  <nd ref="275294269"/>
  <nd ref="275294230"/>
  <nd ref="275294794"/>
  <nd ref="275294203"/>
  <nd ref="275294281"/>
  <nd ref="275294351"/>
  <tag k="highway" v="tertiary"/>
  <tag k="name" v="xy"/>
 </way>*/
	
	protected void addTags(StringBuffer text, String name, EntityAttribute[] atts) {
		if (name != null) {
			text.append("  <tag k=\"name\" v=\"");
			text.append(convertToXML(name));
			text.append("\"/>\n");
		}
		for (EntityAttribute att : atts) {
			text.append("  <tag k=\"");
			text.append(att.getName());
			text.append("\" v=\"");
			text.append(convertToXML(att.getValue()));
			text.append("\"/>\n");
		}
	}
	/**
	 * & is replaced by &amp;
	 * < is replaced by &lt;
     * > is replaced by &gt;
     * " is replaced by &quot;
	 */
	protected String convertToXML(String text) {
		text = text.replace("&", "&amp;");
		text = text.replace("<", "&lt;");
		text = text.replace(">", "&gt;");
		text = text.replace("\"", "&quot;");
		return text;
	}
}