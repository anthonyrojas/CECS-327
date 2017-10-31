import java.rmi.*;
import java.net.*;
import java.util.*;
import javax.json.*;
import javax.json.stream.*;
import javax.json.stream.JsonParser.*;
import java.lang.*;
import java.lang.reflect.Array;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.math.BigInteger;
import java.security.*;
//import org.json.*;
// import a json package

/* JSON Format

 {
    "metadata" :
    {
        file :
        {
            name  : "File1"
            numberOfPages : "3"
            pageSize : "1024"
            size : "2291"
            page :
            {
                number : "1"
                guid   : "22412"
                size   : "1024"
            }
            page :
            {
                number : "2"
                guid   : "46312"
                size   : "1024"
            }
            page :
            {
                number : "3"
                guid   : "93719"
                size   : "243"
            }
        }
    }
}
 
 
 */
/**
 * The distributed file system class that will implement methods for a P2P DFS.
 * 
 * @author Anthony Rojas
 */
public class DFS {
	/**
	 * The port on which the DFS will run
	 */
	int port;
	/**
	 * Chord object that will contain the files.
	 */
	Chord chord;

	/**
	 * Hashes the objectName to create a unique guid
	 * 
	 * @param objectName
	 *            name of the object or chord or peer
	 * @return guid of the object using md5 hashing
	 */
	private long md5(String objectName) {
		try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			m.reset();
			m.update(objectName.getBytes());
			BigInteger bigInt = new BigInteger(1, m.digest());
			return Math.abs(bigInt.longValue());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return 0;
	}

	/**
	 * Constructor for the DFS that initlializes the chord on a port
	 * 
	 * @param port
	 *            port number
	 */
	public DFS(int port) throws Exception {

		this.port = port;
		long guid = md5(Integer.toString(port));
		// long guid = md5("" + port);
		chord = new Chord(port, guid);
		Files.createDirectories(Paths.get(guid + "/repository"));
		try {
			JsonReader jr = readMetaData();
			JsonObject jo = jr.readObject();
			jo.getJsonObject("metadata");
		} catch (Exception e) {
			createMetaData();
		}
	}

	/**
	 * Method that initializes a metadata file for a chord object that has no
	 * current metadata file
	 */
	public void createMetaData() throws Exception {
		JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();
		metadataBuilder.add("metadata", Json.createObjectBuilder().add("files", Json.createArrayBuilder()));
		JsonObject metadataObject = metadataBuilder.build();
		String metaStr = metadataObject.toString();
		InputStream iStream = new ByteArrayInputStream(metaStr.getBytes());
		writeMetaData(iStream);
	}

	/**
	 * Joins a ring as a new peer
	 * 
	 * @param Ip
	 *            the ip address on which the registry is located
	 * @param port
	 *            the port number
	 */
	public void join(String Ip, int port) throws Exception {
		chord.joinRing(Ip, port);
		chord.Print();
	}

	/**
	 * Reads the metadata for the current chord object
	 * 
	 * @return JsonReader with the metadata
	 */
	public JsonReader readMetaData() throws Exception {
		long guid = md5("Metadata");
		ChordMessageInterface peer = chord.locateSuccessor(guid);
		InputStream metadataraw = peer.get(guid);
		JsonReader jr = Json.createReader(metadataraw);
		return jr;
	}

	/**
	 * Writes data into the metadata of the chord
	 * 
	 * @param stream
	 *            Input stream containing the byte information of the new
	 *            metadata
	 */
	public void writeMetaData(InputStream stream) throws Exception {
		// JsonParser jsonParser = null;
		long guid = md5("Metadata");
		ChordMessageInterface peer = chord.locateSuccessor(guid);
		peer.put(guid, stream);
	}

	/**
	 * Changes the name of the the desired filename within the metadata
	 * 
	 * @param oldName
	 *            The current name of the file
	 * @param newName
	 *            The desired new name for the file
	 */
	public void mv(String oldName, String newName) throws Exception {
		// TODO: Change the file name in Metadata
		// Write Metadata
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray fileA = jrMetaData.getJsonArray("files");
		// JsonArrayBuilder filesArrayBuilder = Json.createArrayBuilder();
		if (fileA.size() > 0) {
			JsonArrayBuilder newFilesArrayBuilder = Json.createArrayBuilder();
			for (int i = 0; i < fileA.size(); i++) {
				JsonObject curr = fileA.getJsonObject(i);
				if (curr.getString("name").equals(oldName)) {
					JsonObjectBuilder currBuilder = Json.createObjectBuilder();
					currBuilder.add("name", newName);
					currBuilder.add("numberOfPages", curr.getString("numberOfPages"));
					currBuilder.add("pageSize", curr.getString("pageSize"));
					currBuilder.add("size", curr.getString("size"));
					currBuilder.add("pages", Json.createArrayBuilder().add(curr.getJsonArray("pages")));
					JsonObject updatedObject = currBuilder.build();
					newFilesArrayBuilder.add(updatedObject);
				} else {
					newFilesArrayBuilder.add(curr);
				}
			}
			JsonArray newFilesArray = newFilesArrayBuilder.build();
			JsonObjectBuilder newFilesBuilder = Json.createObjectBuilder();
			newFilesBuilder.add("files", newFilesArray);
			JsonObject newFiles = newFilesBuilder.build();
			JsonObjectBuilder newMetaDataBuilder = Json.createObjectBuilder();
			newMetaDataBuilder.add("metadata", newFiles);
			JsonObject newMetaData = newMetaDataBuilder.build();
			String metaStr = newMetaData.toString();
			InputStream iStream = new BAInputStream(metaStr.getBytes(StandardCharsets.UTF_8));
			writeMetaData(iStream);
		}
	}

	/**
	 * Obtains the file names (only) within the metadata of the chord
	 * 
	 * @return The list of file names as a string
	 */
	public String ls() throws Exception {
		String listOfFiles = "";
		JsonReader jr = readMetaData();
		JsonObject jo = jr.readObject();
		// System.out.println(jo.toString());
		JsonObject mdJO = jo.getJsonObject("metadata");
		JsonArray fileA = mdJO.getJsonArray("files");
		if (fileA.size() <= 0) {
			listOfFiles = "No files";
		} else {
			for (int i = 0; i < fileA.size(); i++) {
				JsonObject currentObject = fileA.getJsonObject(i);
				listOfFiles += "\n" + currentObject.getString("name");
			}
		}
		// TODO: returns all the files in the Metadata
		// JsonParser jp = readMetaData();
		return listOfFiles;
	}

	/**
	 * Creates a file with the specified name within the metadata as a new entry
	 * 
	 * @param fileName
	 *            The name of the file
	 */
	public void touch(String fileName) throws Exception {
		// TODO: Create the file fileName by adding a new entry to the Metadata
		// Write Metadata
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject meta = jrObject.getJsonObject("metadata");
		JsonArray files = meta.getJsonArray("files");
		JsonObjectBuilder newFile = Json.createObjectBuilder();
		newFile.add("name", fileName);
		newFile.add("numberOfPages", "0");
		newFile.add("pageSize", "1024");
		newFile.add("size", "0");
		newFile.add("pages", Json.createArrayBuilder());
		JsonObject temp = newFile.build();
		// System.out.println(temp.toString());
		JsonArrayBuilder filesBuilder = Json.createArrayBuilder();
		for (int i = 0; i < files.size(); i++) {
			filesBuilder.add(files.getJsonObject(i));
		}
		filesBuilder.add(temp);
		JsonArray filesArray = filesBuilder.build();
		JsonObjectBuilder fileObjectBuilder = Json.createObjectBuilder();
		JsonObject filesObject = fileObjectBuilder.add("files", filesArray).build();
		JsonObjectBuilder metadataBuilder = Json.createObjectBuilder();
		metadataBuilder.add("metadata", filesObject);
		JsonObject newMDObject = metadataBuilder.build();
		String metaStr = newMDObject.toString();
		// InputStream iStream = new ByteArrayInputStream(metaStr.getBytes());
		// InputStream stream = new DataInputStream(iStream);
		InputStream stream = new BAInputStream(metaStr.getBytes());
		writeMetaData(stream);
	}

	/**
	 * Deletes the specified file within the metadata if it exists
	 * 
	 * @param fileName
	 *            The name of the file to be deleted
	 */
	public void delete(String fileName) throws Exception {
		// TODO: remove all the pages in the entry fileName
		// in the Metadata and
		// then the entry
		// for each page in Metadata.filename
		// peer = chord.locateSuccessor(page.guid);
		// peer.delete(page.guid)
		// delete Metadata.filename
		// Write Metadata
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFiles = jrMetaData.getJsonArray("files");
		JsonArrayBuilder filesArrayBuilder = Json.createArrayBuilder();
		if (jrFiles.size() > 0) {
			for (int i = 0; i < jrFiles.size(); i++) {
				JsonObject curr = jrFiles.getJsonObject(i);
				if (curr.getString("name").equals(fileName)) {
					System.out.println("file " + fileName + " has been deleted");
				} else {
					filesArrayBuilder.add(curr);
				}
			}
			JsonArray filesArray = filesArrayBuilder.build();
			JsonObjectBuilder filesObjectBuilder = Json.createObjectBuilder();
			filesObjectBuilder.add("files", filesArray);
			JsonObject filesObject = filesObjectBuilder.build();
			JsonObjectBuilder mdObjectBuilder = Json.createObjectBuilder();
			mdObjectBuilder.add("metadata", filesObject);
			JsonObject metadataObject = mdObjectBuilder.build();
			String metaStr = metadataObject.toString();
			InputStream iStream = new BAInputStream(metaStr.getBytes());
			writeMetaData(iStream);
		} else {
			System.out.println("There are no files or that file does not exist");
		}
	}

	/**
	 * Reads the specified page within the filename in the metadata
	 * 
	 * @param fileName
	 *            The name of the file
	 * @param pageNumber
	 *            The number of the page in the file
	 * @return The byte array containing the data of page
	 */
	public byte[] read(String fileName, int pageNumber) throws Exception {
		// TODO: read pageNumber from fileName
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFileArray = jrMetaData.getJsonArray("files");
		String pageNumStr = Integer.toString(pageNumber);
		for (int i = 0; i < jrFileArray.size(); i++) {
			JsonObject current = jrFileArray.getJsonObject(i);
			if (current.getString("name").equals(fileName)) {
				JsonArray pagesArray = current.getJsonArray("pages");
				if (pagesArray.isEmpty()) {
					return null;
				}
				for (int j = 0; j < pagesArray.size(); j++) {
					JsonObject currentPage = pagesArray.getJsonObject(j);
					if (currentPage.getString("pageNumber").equals(pageNumStr)) {
						return currentPage.toString().getBytes();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Reads the last of the file name
	 * 
	 * @param fileName
	 *            The name of the file
	 * @return The byte array containing the data of the last page of the file
	 */
	public byte[] tail(String fileName) throws Exception {
		// TODO: return the last page of the fileName
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFilesArray = jrMetaData.getJsonArray("files");
		for (int i = 0; i < jrFilesArray.size(); i++) {
			JsonObject currentFile = jrFilesArray.getJsonObject(i);
			if (currentFile.getString("name").equals(fileName)) {
				JsonArray currentPages = currentFile.getJsonArray("pages");
				if (currentPages.size() <= 0) {
					return null;
				}
				JsonObject lastPage = currentPages.getJsonObject(currentPages.size() - 1);
				return lastPage.toString().getBytes();
			}
		}
		return null;
	}

	/**
	 * Reads and returns the first page of the file
	 * 
	 * @param fileName
	 *            The name of the file
	 * @return The byte array containing the information of the last page of the
	 *         file
	 */
	public byte[] head(String fileName) throws Exception {
		// TODO: return the first page of the fileName
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFilesArray = jrMetaData.getJsonArray("files");
		for (int i = 0; i < jrFilesArray.size(); i++) {
			JsonObject currentFile = jrFilesArray.getJsonObject(i);
			if (currentFile.getString("name").equals(fileName)) {
				JsonArray currentPages = currentFile.getJsonArray("pages");
				if (currentPages.size() <= 0) {
					return null;
				}
				JsonObject firstPage = currentPages.getJsonObject(0);
				return firstPage.toString().getBytes();
			}
		}
		return null;
	}

	/**
	 * Appends data to the end of the file in the metadata by creating a new
	 * page and converting the data into a JSON object.
	 * 
	 * @param fileName
	 *            The name of the file
	 * @param data
	 *            The byte array containing the data for the new page
	 */
	public void append(String fileName, byte[] data) throws Exception {
		// TODO: append data to fileName. If it is needed, add a new page.
		// Let guid be the last page in Metadata.filename
		// ChordMessageInterface peer = chord.locateSuccessor(guid);
		// peer.put(guid, data);
		// Write Metadata
		BAInputStream bis = new BAInputStream(data);
		JsonReader pageJR = Json.createReader(bis);
		JsonObject pageObject = pageJR.readObject();
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFilesArray = jrMetaData.getJsonArray("files");
		JsonArrayBuilder filesArrayBuilder = Json.createArrayBuilder();
		for (int i = 0; i < jrFilesArray.size(); i++) {
			JsonObject currentFile = jrFilesArray.getJsonObject(i);
			JsonArrayBuilder pagesBuilder = Json.createArrayBuilder();
			JsonObjectBuilder fileBuilder = Json.createObjectBuilder();
			if (currentFile.getString("name").equals(fileName)) {
				JsonArray currentPageArray = currentFile.getJsonArray("pages");
				if (currentPageArray.size() > 0) {
					for (int j = 0; j < currentPageArray.size(); j++) {
						pagesBuilder.add(currentPageArray.getJsonObject(j));
					}
				}
				// pagesBuilder.add(currentPageArray);
				pagesBuilder.add(pageObject);
				JsonArray pagesArray = pagesBuilder.build();
				fileBuilder.add("name", fileName);
				int numPages = Integer.parseInt(currentFile.getString("numberOfPages")) + 1;
				String numPagesStr = Integer.toString(numPages);
				fileBuilder.add("numberOfPages", numPagesStr);
				fileBuilder.add("pageSize", currentFile.getString("pageSize"));
				int size = Integer.parseInt(currentFile.getString("size"))
						+ Integer.parseInt(pageObject.getString("size"));
				String sizeStr = Integer.toString(size);
				fileBuilder.add("size", sizeStr);
				fileBuilder.add("pages", pagesArray);
				JsonObject fileObject = fileBuilder.build();
				filesArrayBuilder.add(fileObject);
				System.out.println("Page added to file " + fileName + ": " + new String(data));
			} else {
				filesArrayBuilder.add(currentFile);
			}
		}
		JsonArray filesArray = filesArrayBuilder.build();
		JsonObjectBuilder filesBuilder = Json.createObjectBuilder();
		filesBuilder.add("files", filesArray);
		JsonObject filesObject = filesBuilder.build();
		JsonObjectBuilder mdBuilder = Json.createObjectBuilder();
		mdBuilder.add("metadata", filesObject);
		JsonObject mdObject = mdBuilder.build();
		String mdString = mdObject.toString();
		InputStream iStream = new BAInputStream(mdString.getBytes());
		writeMetaData(iStream);
	}

}
