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
	 * The size for each page in this DFS
	 * */
	public final int PAGE_SIZE = 1024;
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
					currBuilder.add("pages", curr.getJsonArray("pages"));
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
		JsonArrayBuilder newFilePages = Json.createArrayBuilder();
		//the first page in the new file
		long firstPageMD5 = md5(fileName + ":1");
		String newFileName = String.valueOf(firstPageMD5);
		JsonObjectBuilder newPage = Json.createObjectBuilder();
		//{pageNumber, guid, size};
		newPage.add("pageNumber", "1");
		newPage.add("guid", String.valueOf(firstPageMD5));
		newPage.add("size", "0");
		JsonObject firstPage = newPage.build();
		//the page array for the new file
		JsonArrayBuilder pagesArrayBuilder = Json.createArrayBuilder();
		pagesArrayBuilder.add(firstPage);
		JsonArray pagesArray = pagesArrayBuilder.build();
		//the new file
		newFile.add("name", fileName);
		newFile.add("numberOfPages", "1");
		newFile.add("pageSize", "1024");
		newFile.add("size", "0");
		newFile.add("pages", pagesArray);
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
		InputStream stream = new BAInputStream(metaStr.getBytes());
		writeMetaData(stream);
		File addedFile = new File("./" + String.valueOf(chord.getId()) + "/repository/" + newFileName);
		addedFile.createNewFile();
	}

	/**
	 * Creates the output file in the metadata for map reduce files and pages
	 * @param fileName The name of the output file to contain the map reduce pages in the metadata
	 * @param mrPage The guid of the map reduce file
	 * */
	public void touchMapReduce(String fileName, long mrPage) throws Exception{
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject meta = jrObject.getJsonObject("metadata");
		JsonArray files = meta.getJsonArray("files");
		JsonObjectBuilder newFile = Json.createObjectBuilder();
		JsonArrayBuilder newFilePages = Json.createArrayBuilder();
		//the first page in the new file
		//the page array for the new file
		JsonArrayBuilder pagesArrayBuilder = Json.createArrayBuilder();
		//pagesArrayBuilder.add(firstPage);
		JsonArray pagesArray = pagesArrayBuilder.build();
		//the new file
		newFile.add("name", fileName);
		newFile.add("numberOfPages", "0");
		newFile.add("pageSize", String.valueOf(PAGE_SIZE));
		newFile.add("size", "0");
		newFile.add("pages", pagesArray);
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
		InputStream stream = new BAInputStream(metaStr.getBytes());
		writeMetaData(stream);
		File f = new File("./" + String.valueOf(chord.getId()) + "/repository/" + mrPage);
		f.createNewFile();
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
					deleteFile(curr.getJsonArray("pages"));
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
	 * Deletes all the pages in a file within the DFS.
	 * @param pagesArray The array of pages to be deleted
	 * */
	public void deleteFile(JsonArray pagesArray) throws Exception{
		for(int i = 0; i < pagesArray.size(); i++){
			String guidStr = String.valueOf(pagesArray.getJsonObject(i).getString("guid"));
			long pageGuid = Long.parseLong(guidStr);
			chord.delete(pageGuid);
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
						System.out.println(currentPage.toString());
						InputStream is = chord.get(Long.parseLong(currentPage.getString("guid")));
						int bytesSize = Integer.parseInt(currentPage.getString("size"));
						byte[] data = new byte[bytesSize];
						ByteArrayOutputStream bOut = new ByteArrayOutputStream();
						int dataInt = is.read(data, 0, data.length);
						bOut.write(data, 0, data.length);
						bOut.flush();
						is.close();
						return data;
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
				System.out.println(lastPage.toString());
				InputStream is = chord.get(Long.parseLong(lastPage.getString("guid")));
				int bytesSize = Integer.parseInt(lastPage.getString("size"));
				byte[] data = new byte[bytesSize];
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				int dataInt = is.read(data, 0, data.length);
				bOut.write(data, 0, data.length);
				bOut.flush();
				is.close();
				return data;
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
				System.out.println(firstPage.toString());
				InputStream is = chord.get(Long.parseLong(firstPage.getString("guid")));
				int bytesSize = Integer.parseInt(firstPage.getString("size"));
				byte[] data = new byte[bytesSize];
				ByteArrayOutputStream bOut = new ByteArrayOutputStream();
				int dataInt = is.read(data, 0, data.length);
				bOut.write(data, 0, data.length);
				bOut.flush();
				is.close();
				return data;
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
		//Write the input stream data into the last page of the file
		//if the bytes exceed the size of the file, then create a new page
		int dataSize = data.length;
		BAInputStream bis = new BAInputStream(data);

		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFilesArray = jrMetaData.getJsonArray("files");

		JsonArrayBuilder filesArrayBuilder = Json.createArrayBuilder();

		for(int i=0; i<jrFilesArray.size(); i++){
			JsonObject currentFile = jrFilesArray.getJsonObject(i);
			JsonArray currentFilePages = currentFile.getJsonArray("pages");
			JsonObjectBuilder fileObjectBuilder = Json.createObjectBuilder();
			int pageSizeInt = Integer.parseInt(currentFile.getString("size"));

			JsonArrayBuilder pagesArrayBuilder = Json.createArrayBuilder();
			JsonArray pagesArray;
			JsonObjectBuilder currentPageBuilder = Json.createObjectBuilder();
			for(int j = 0; j < currentFilePages.size(); j++){
				JsonObject currentPageObject = null;
				if(currentFile.getString("name").equals(fileName) && j == (currentFilePages.size()-1)){
					JsonObject lastPage = currentFilePages.getJsonObject(j);
					String lastPageSizeStr = lastPage.getString("size");
					int lastPageSizeInt = Integer.parseInt(lastPageSizeStr);
					int maxSizeDifference = PAGE_SIZE - lastPageSizeInt;
					//is the lastPageSizeInt + dataSize bigger than PAGE_SIZE
					if((lastPageSizeInt + dataSize) > PAGE_SIZE){
						byte[] leftoverBytes = Arrays.copyOfRange(data,0, maxSizeDifference);
						currentPageBuilder.add("pageNumber", currentFilePages.getJsonObject(j).getString("pageNumber"));
						currentPageBuilder.add("guid", currentFilePages.getJsonObject(j).getString("guid"));
						currentPageBuilder.add("size", "1024");
						pagesArrayBuilder.add(currentPageBuilder.build());
						byte[] remainingBytes = Arrays.copyOfRange(data, maxSizeDifference, dataSize-1);
						JsonObjectBuilder additionalPageBuilder = Json.createObjectBuilder();
						String currentPageNumberStr = currentFilePages.getJsonObject(j).getString("pageNumber");
						int addPageNumberInt = Integer.parseInt(currentPageNumberStr) + 1;
						additionalPageBuilder.add("pageNumber", String.valueOf(addPageNumberInt));
						long addPageGuid = md5(fileName + ":" + String.valueOf(addPageNumberInt+1));
						additionalPageBuilder.add("guid",String.valueOf(addPageGuid));
						additionalPageBuilder.add("size", String.valueOf(dataSize-maxSizeDifference));
						pagesArrayBuilder.add(additionalPageBuilder.build());
						chord.put(Long.parseLong(currentFilePages.getJsonObject(j).getString("guid")), new ByteArrayInputStream(leftoverBytes));
						chord.put(addPageGuid, new ByteArrayInputStream(remainingBytes));
					}
					else{
						String currPageGuidStr = currentFilePages.getJsonObject(j).getString("guid");
						long currPageGuidLong = Long.parseLong(currPageGuidStr);
						currentPageBuilder.add("pageNumber", currentFilePages.getJsonObject(j).getString("pageNumber"));
						currentPageBuilder.add("guid", currentFilePages.getJsonObject(j).getString("guid"));
						currentPageBuilder.add("size", String.valueOf(lastPageSizeInt + dataSize));
						pagesArrayBuilder.add(currentPageBuilder.build());
						byte[] currPageBytes = chord.getBytes(currPageGuidLong);
						byte[] combined = new byte[currPageBytes.length + data.length];
						System.arraycopy(currPageBytes, 0, combined, 0, currPageBytes.length);
						System.arraycopy(data, 0, combined, currPageBytes.length, data.length);
						chord.put(Long.parseLong(currentFilePages.getJsonObject(j).getString("guid")), new ByteArrayInputStream(combined));
					}
				}
				else{
					currentPageBuilder.add("pageNumber", currentFilePages.getJsonObject(j).getString("pageNumber"));
					currentPageBuilder.add("guid", currentFilePages.getJsonObject(j).getString("guid"));
					currentPageBuilder.add("size", currentFilePages.getJsonObject(j).getString("size"));
					currentPageObject = currentPageBuilder.build();
					pagesArrayBuilder.add(currentPageObject);
				}
			}
			pagesArray = pagesArrayBuilder.build();
			int numberOfPages = pagesArray.size();
			int fileSizeInt = getPageArraySize(pagesArray);
			fileObjectBuilder.add("name", currentFile.getString("name"));
			fileObjectBuilder.add("numberOfPages", String.valueOf(numberOfPages));
			fileObjectBuilder.add("size", String.valueOf(fileSizeInt));
			fileObjectBuilder.add("pages", pagesArray);
			filesArrayBuilder.add(fileObjectBuilder.build());
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

	/**
	 * This method obtains the size of a page Array
	 * @param pageArray The JSON array of pages
	 * @return size of the page array
	 * */
	public int getPageArraySize(JsonArray pageArray){
		int sum = 0;
		for(int i=0; i < pageArray.size(); i++){
			sum += Integer.parseInt(pageArray.getJsonObject(i).getString("size"));
		}
		return sum;
	}

	/**
	 * Map Reduce is executed on a specified file in the DFS
	 * @param filename The name of the file
	 * */
	public long runMapReduce(String filename) throws Exception {
		CounterInterface mapCounter = new Counter();
		CounterInterface reduceCounter = new Counter();
		CounterInterface completedCounter = new Counter();
		MapInterface mapper = new Mapper(chord, mapCounter);
		ReduceInterface reducer = new Mapper(chord, reduceCounter);
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFiles = jrMetaData.getJsonArray("files");
		long mrPage = 0L;
		//obtain the file with name filename
		for (JsonValue j : jrFiles) {
			JsonObject currentFile = j.asJsonObject();
			if(currentFile.getString("name").equals(filename)){
				JsonArray currentPages = currentFile.getJsonArray("pages");
				//run map
				for (JsonValue p : currentPages) {
					JsonObject currentPage = p.asJsonObject();
					String guidStr = currentPage.getString("guid");
					long page = Long.parseLong(guidStr);
					mapCounter.add(page);
					chord.mapContext(page, mapper, mapCounter);
				}
				Timer timeoutTimer = new Timer();
				while (mapCounter.hasCompleted() != true) {
					timeoutTimer.schedule(new TimerTask() {
						@Override
						public void run() {

						}
					}, 5);
				}
				System.out.println("Mapping complete");
				chord.reduceContext(chord.getId(), reducer, reduceCounter);
				while(reduceCounter.hasCompleted() != true){
					//waiting for reduce completion
					timeoutTimer.schedule(new TimerTask(){
						@Override
						public void run(){

						}
					}, 5);
				}
				System.out.println("Reducing complete");
				mrPage = md5(filename + "output");
				if(!fileExists("output")){
					this.touchMapReduce("output", mrPage);
				}
				//createMapReduceFile(filename);
				//chord.completed(chord.getId(), completedCounter, filename);
				chord.completed(chord.getId(), completedCounter, mrPage);
				while (completedCounter.hasCompleted() != true) {
					// waiting for completion
					timeoutTimer.schedule(new TimerTask() {
						@Override
						public void run() {

						}
					}, 10);
				}
				//map reduce is now done
				System.out.println("Map reduce completed");
				//System.out.println("page guid for map reduce: " + mrPage);
				//byte[] mrBytes = chord.getBytes(mrPage);
				//appendMapReduce("output", mrPage);
			}
		}
		/*if(mrPage != 0L){
			byte[] mrBytes = chord.getBytes(mrPage);
			appendMapReduce("output", mrBytes, mrPage);
		}*/
		return mrPage;
	}

	/**
	 * Checks if a file exists in the metadata and chord directory
	 * @param fileName The name of the file.
	 * @return The boolean value of whether or not the file exists
	 * */
	public Boolean fileExists(String fileName) throws Exception{
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFilesArray = jrMetaData.getJsonArray("files");
		for(int i=0; i < jrFilesArray.size(); i++){
			if(jrFilesArray.getJsonObject(i).getString("name").equals(fileName)){
				return true;
			}
		}
		return false;
	}

	/**
	 * Appends a map reduce page into the metadata.
	 * @param fileName The file with which the map reduce was run on
	 * @param pageGuid The page guid of the map reduce file
	 * */
	public void appendMapReduce(String fileName, long pageGuid) throws Exception{
		JsonReader jr = readMetaData();
		JsonObject jrObject = jr.readObject();
		JsonObject jrMetaData = jrObject.getJsonObject("metadata");
		JsonArray jrFilesArray = jrMetaData.getJsonArray("files");

		JsonObjectBuilder mdBuilder = Json.createObjectBuilder();
		JsonArrayBuilder filesArrayBuilder = Json.createArrayBuilder();

		byte[]data = chord.getBytes(pageGuid);

		for(int i=0; i < jrFilesArray.size(); i++){
			JsonObject currFile = jrFilesArray.getJsonObject(i);
			JsonObjectBuilder fileObjectBuilder = Json.createObjectBuilder();
			if(currFile.getString("name").equals(fileName)){
				int fileSize = 0;
				JsonArray currFilePages = currFile.getJsonArray("pages");
				JsonArrayBuilder pageArrayBuilder = Json.createArrayBuilder();
				System.out.println(currFilePages.toString());
				if(!currFilePages.isEmpty()){
					pageArrayBuilder.add(currFilePages);
					for(int j=0; j < currFilePages.size(); j++){
						fileSize += Integer.parseInt(currFilePages.getJsonObject(j).getString("size"));
					}
				}
				JsonObjectBuilder pageObjectBuilder = Json.createObjectBuilder();
				pageObjectBuilder.add("pageNumber", String.valueOf(currFilePages.size()+1));
				pageObjectBuilder.add("guid", String.valueOf(pageGuid));
				pageObjectBuilder.add("size", String.valueOf(data.length));
				pageArrayBuilder.add(pageObjectBuilder.build());
				JsonArray pagesArray = pageArrayBuilder.build();
				fileSize += data.length;
				fileObjectBuilder.add("name", currFile.getString("name"));
				fileObjectBuilder.add("numberOfPages", String.valueOf(pagesArray.size()));
				fileObjectBuilder.add("size", String.valueOf(fileSize));
				fileObjectBuilder.add("pages", pagesArray);
				filesArrayBuilder.add(fileObjectBuilder.build());
			}
			else{
				filesArrayBuilder.add(currFile);
			}
		}
		JsonArray filesArray = filesArrayBuilder.build();
		JsonObjectBuilder filesObjectBuilder = Json.createObjectBuilder();
		filesObjectBuilder.add("files", filesArray);
		JsonObject filesObject = filesObjectBuilder.build();
		JsonObjectBuilder mdObjectBuilder = Json.createObjectBuilder();
		mdObjectBuilder.add("metadata", filesObject);
		JsonObject mdObject = mdObjectBuilder.build();
		String mdStr = mdObject.toString();
		byte[] mdBytes = mdStr.getBytes();
		BAInputStream mdStream = new BAInputStream(mdBytes);
		writeMetaData(mdStream);
	}
}
