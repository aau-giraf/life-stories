package dk.aau.cs.giraf.tortoise.controller;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Point;

import dk.aau.cs.giraf.tortoise.Frame;

public class JSONSerializer {
	
	private final String KEY_SETTINGS_SEQUENCES = "sequences";
	
	private final String KEY_SEQUENCE_TITLE = "title";
	private final String KEY_SEQUENCE_TITLEPICTO = "titlePicto";
	private final String KEY_SEQUENCE_MEDIAFRAMES = "mediaFrames";
	private final String KEY_SEQUENCE_NUMCHOICES = "numChoices";
	
	private final String KEY_MEDIAFRAME_FRAMES = "frames";
	private final String KEY_MEDIAFRAME_PICTOGRAMS = "pictograms";
	private final String KEY_MEDIAFRAME_CHOICEID = "choiceId";
	
	private final String KEY_FRAME_X = "posX";
	private final String KEY_FRAME_Y = "posY";
	private final String KEY_FRAME_HEIGHT = "frameHeight";
	private final String KEY_FRAME_WIDTH = "frameWidth";
	
	private final String KEY_PICTOGRAM_ID = "pictogramId";

	public String serialize(List<SerializableSequence> sequences) throws JSONException {
		final JSONObject jsonSettings = new JSONObject();
		
		final JSONArray jsonSequences = new JSONArray();
		
		for (SerializableSequence s : sequences) {
			final JSONObject jsonSequence = writeSequence(s);
			jsonSequences.put(jsonSequence);
		}
		
		jsonSettings.put(KEY_SETTINGS_SEQUENCES, jsonSequences);
	
		return jsonSettings.toString();
	}
	
	public void saveSettingsToFile(Context context, List<SerializableSequence> stories, Integer profileId) throws IOException, JSONException {
		String content = serialize(stories);
		
		FileOutputStream outputStream;
		outputStream = context.openFileOutput("tortoise_settings_" + profileId, Context.MODE_PRIVATE);
		outputStream.write(content.getBytes());
		outputStream.close();
	}
	
	public List<SerializableSequence> loadSettingsFromFile(Context context, Integer profileId) throws IOException, JSONException {

		BufferedReader reader = new BufferedReader( new FileReader (context.getFilesDir() + "/tortoise_settings_" + profileId));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    while( ( line = reader.readLine() ) != null ) {
	        stringBuilder.append( line );
	        stringBuilder.append( ls );
	    }
	    reader.close();

	    return deserialize(stringBuilder.toString());
	}

	private JSONObject writeSequence(SerializableSequence s) throws JSONException {
		final JSONObject jsonSequence = new JSONObject();
		jsonSequence.put(KEY_SEQUENCE_TITLE, s.getTitle());
		jsonSequence.put(KEY_SEQUENCE_TITLEPICTO, s.getTitlePictoId());
		jsonSequence.put(KEY_SEQUENCE_NUMCHOICES, s.getNumChoices());
		
		JSONArray jsonMediaFrames = writeMediaFrames(s.getMediaFrames());
		jsonSequence.put(KEY_SEQUENCE_MEDIAFRAMES, jsonMediaFrames);
		
		return jsonSequence;
	}
	
	private JSONArray writeMediaFrames(List<SerializableMediaFrame> mediaFrames) throws JSONException {
		final JSONArray jsonMediaFrames = new JSONArray();
		for (SerializableMediaFrame m : mediaFrames) {
			jsonMediaFrames.put(writeMediaFrame(m));
		}
		return jsonMediaFrames;
	}
	
	private JSONObject writeMediaFrame(SerializableMediaFrame m) throws JSONException {
		final JSONObject jsonMediaFrame = new JSONObject();
		
		jsonMediaFrame.put(KEY_MEDIAFRAME_CHOICEID, m.getChoiceNumber());
		
		JSONArray jsonPictograms = writePictograms(m.getContent());
		jsonMediaFrame.put(KEY_MEDIAFRAME_PICTOGRAMS, jsonPictograms);
		
		JSONArray jsonFrames = writeFrames(m.getFrames());
		jsonMediaFrame.put(KEY_MEDIAFRAME_FRAMES, jsonFrames);
		
		return jsonMediaFrame;
	}
	
	private JSONArray writePictograms(List<Integer> pictograms) throws JSONException {
		final JSONArray jsonPictograms = new JSONArray();
		for (Integer p : pictograms) {
			jsonPictograms.put(writePictogram(p));
		}
		return jsonPictograms;
	}

	private JSONObject writePictogram(Integer p) throws JSONException {
		final JSONObject jsonPictogram = new JSONObject();
		jsonPictogram.put(KEY_PICTOGRAM_ID, p);
		
		return jsonPictogram;
	}
	
	private JSONArray writeFrames(List<Frame> frames) throws JSONException {
		final JSONArray jsonFrames = new JSONArray();
		for (Frame f : frames) {
			jsonFrames.put(writeFrame(f));
		}
		return jsonFrames;
	}
	
	private JSONObject writeFrame(Frame f) throws JSONException {
		final JSONObject jsonFrame = new JSONObject();
		jsonFrame.put(KEY_FRAME_HEIGHT, f.getHeight());
		jsonFrame.put(KEY_FRAME_WIDTH, f.getWidth());
		jsonFrame.put(KEY_FRAME_X, f.getPosition().x);
		jsonFrame.put(KEY_FRAME_Y, f.getPosition().y);
		
		return jsonFrame;
	}

	public List<SerializableSequence> deserialize(String jsonData) throws JSONException {
		
		JSONObject jsonSettings = new JSONObject(jsonData);
		
		JSONArray jsonSequences = jsonSettings.getJSONArray(KEY_SETTINGS_SEQUENCES);
		
		List<SerializableSequence> sequences = new ArrayList<SerializableSequence>();
		
		for (int i = 0; i < jsonSequences.length(); i++) {
			JSONObject jsonSequence = jsonSequences.getJSONObject(i);
			SerializableSequence sequence = readSequence(jsonSequence);
			sequences.add(sequence);
		}
		
		return sequences;
	}

	private SerializableSequence readSequence(JSONObject jsonSequence) throws JSONException {
		String title = jsonSequence.getString(KEY_SEQUENCE_TITLE);
		int titlePictoId = jsonSequence.getInt(KEY_SEQUENCE_TITLEPICTO);
		int numChoices = jsonSequence.getInt(KEY_SEQUENCE_NUMCHOICES);
		
		SerializableSequence sequence = new SerializableSequence();
		sequence.setTitle(title);
		sequence.setTitlePictoId(titlePictoId);
		sequence.setNumChoices(numChoices);
		
		JSONArray jsonMediaFrames = jsonSequence.getJSONArray(KEY_SEQUENCE_MEDIAFRAMES);
		List<SerializableMediaFrame> mediaFrames = readMediaFrames(jsonMediaFrames);
		sequence.setMediaFrames(mediaFrames);
		
		return sequence;
	}
	
	private List<SerializableMediaFrame> readMediaFrames(JSONArray jsonMediaFrames) throws JSONException {
		List<SerializableMediaFrame> mediaFrames = new ArrayList<SerializableMediaFrame>();
		
		for (int i = 0; i < jsonMediaFrames.length(); i++) {
			JSONObject jsonMediaFrame = jsonMediaFrames.getJSONObject(i);
			SerializableMediaFrame mediaFrame = readMediaFrame(jsonMediaFrame);
			mediaFrames.add(mediaFrame);
		}
		
		return mediaFrames;
	}
	
	private SerializableMediaFrame readMediaFrame(JSONObject jsonMediaFrame) throws JSONException{
		SerializableMediaFrame mediaFrame = new SerializableMediaFrame();
		mediaFrame.setChoiceNumber(jsonMediaFrame.getInt(KEY_MEDIAFRAME_CHOICEID));
		
		JSONArray jsonFrames = jsonMediaFrame.getJSONArray(KEY_MEDIAFRAME_FRAMES);
		List<Frame> frames = readFrames(jsonFrames);
		mediaFrame.setFrames(frames);
		
		JSONArray jsonPictograms = jsonMediaFrame.getJSONArray(KEY_MEDIAFRAME_PICTOGRAMS);
		List<Integer> pictos = readPictograms(jsonPictograms);
		mediaFrame.setContent(pictos);
		
		return mediaFrame;
	}
	
	private List<Frame> readFrames(JSONArray jsonFrames) throws JSONException {
		List<Frame> frames = new ArrayList<Frame>();
		
		for (int i = 0; i < jsonFrames.length(); i++) {
			JSONObject jsonFrame = jsonFrames.getJSONObject(i);
			Frame frame = readFrame(jsonFrame);
			frames.add(frame);
		}
		
		return frames;
	}
	
	private Frame readFrame(JSONObject jsonFrame) throws JSONException{
		Frame frame = new Frame(jsonFrame.getInt(KEY_FRAME_WIDTH), 
								jsonFrame.getInt(KEY_FRAME_HEIGHT), 
								new Point(jsonFrame.getInt(KEY_FRAME_X), jsonFrame.getInt(KEY_FRAME_Y)));
		
		return frame;
	}

	private List<Integer> readPictograms(JSONArray jsonPictograms) throws JSONException {
		List<Integer> pictograms = new ArrayList<Integer>();
		
		for (int i = 0; i < jsonPictograms.length(); i++) {
			JSONObject jsonPictogram = jsonPictograms.getJSONObject(i);
			Integer pictogram = readPictogram(jsonPictogram);
			pictograms.add(pictogram);
		}
		
		return pictograms;
	}

	private Integer readPictogram(JSONObject jsonPictogram) throws JSONException {
		return jsonPictogram.getInt(KEY_PICTOGRAM_ID);
	}
	
}
