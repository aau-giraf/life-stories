package dk.aau.cs.giraf.tortoise.controller;

import android.content.Context;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.dblib.controllers.PictogramController;
import dk.aau.cs.giraf.dblib.controllers.SequenceController;
import dk.aau.cs.giraf.dblib.models.Sequence.SequenceType;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.helpers.GuiHelper;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;


/**
 * Created by David on 28-03-14.
 */
public class DBController {

    /*********************
     * Singleton pattern *
     *********************/
    private static DBController instance;
    private DBController(){}
    public static DBController getInstance(){
        DBController dbController;
        if(instance != null){
            dbController = instance;
        }
        else{
            instance = new DBController();
            dbController = instance;
        }
        return dbController;
    }

    /*******************************
     * Class variable declarations *
     *******************************/
    private final int frameHeight = 140;
    private final int frameWidth = 140;

    /******************
     * Public methods *
     ******************/

    /**
     * Save a sequence on a specific profile.
     * @param seq
     * @param seqType
     * @param profileID
     * @param con
     * @return boolean
     */
    public boolean saveSequence(Sequence seq, SequenceType seqType, int profileID, Context con){
        boolean success;
        SequenceController sc = new SequenceController(con);
        dk.aau.cs.giraf.dblib.models.Sequence dbSeq = morphSequenceToDBSequence(seq, seqType, profileID);
        if (seq.getId() == 0) {
            success = sc.insertSequenceAndFrames(dbSeq);
            seq.setId(dbSeq.getId());
        } else {
            success = sc.insertSequenceAndFrames(dbSeq);
            seq.setId(dbSeq.getId());
            //success = sc.modifySequenceAndFrames(dbSeq);
        }
        return success;
    }

    public boolean existScheduleSequence(Sequence seq, Context con) {
        SequenceController sc = new SequenceController(con);
        if(sc.getSequenceById(seq.getId()) != null){
            return true;
        }
        return false;
    }

    /**
     * Sets the lifestories of the current citizen
     * @param profileID
     * @param sequenceType
     * @param con
     */
    public void loadCurrentProfileSequences(int profileID, SequenceType sequenceType, Context con){
        SequenceController sc = new SequenceController(con);
        try{
            LifeStory.getInstance().setStories(
                    morphDBSequenceListToSequenceList(
                            sc.getSequencesAndFramesByProfileIdAndType(
                                    profileID, sequenceType), con));
        }catch (NullPointerException e){
            GuiHelper.ShowToast(con, "No sequences found!");
        }

    }

    public List<Sequence> loadCurrentProfileSequencesAndFrames(int profileID, SequenceType sequenceType, Context con){
        SequenceController sc = new SequenceController(con);
        try{
        List<Sequence> items = morphDBSequenceListToSequenceList(
                sc.getSequencesAndFramesByProfileIdAndType(profileID, sequenceType),con);
            return items;
        }catch (NullPointerException e){
            GuiHelper.ShowToast(con, "No sequences found!");
        }
        return null;
    }
    /**
     * Sets the templates of the current guardian
     * @param profileID
     * @param sequenceType
     * @param con
     */
    public void loadCurrentGuardianTemplates(int profileID, SequenceType sequenceType, Context con){
        SequenceController sc = new SequenceController(con);
        LifeStory.getInstance().setTemplates(
                morphDBSequenceListToSequenceList(
                        sc.getSequencesAndFramesByProfileIdAndType(
                                profileID, sequenceType), con));
    }

    /**
     * Delete a sequence from the supplied context
     * @param seq
     * @param con
     */
    public void deleteSequence(Sequence seq, Context con){
        SequenceController sc = new SequenceController(con);
        if (sc.getSequenceById(seq.getId()).getSequenceType() == SequenceType.SCHEDULE){
            for(MediaFrame mf : seq.getMediaFrames()){
                sc.remove(mf.getNestedSequenceID());
            }
        }
        sc.remove(seq.getId());

    }

    /**
     * Get a sequence with frames by ID
     * @param id
     * @param context
     * @return Sequence
     */
    public Sequence getSequenceFromID(int id, Context context)
    {
        SequenceController sc = new SequenceController(context);
        return morphDBSequenceToSequence(sc.getSequenceAndFrames(id), context);
    }

    public List<Sequence> getAllSequences(Context context)
    {
        SequenceController sc = new SequenceController(context);

        return morphDBSequenceListToSequenceList(sc.getSequences(), context);
    }


    /**
     * Morphs a list of DB Sequences to a list of Giraf Sequences
     * @param dbSeqs
     * @param con
     * @return a list of Pictogram Sequences
     */
    private ArrayList<Sequence> morphDBSequenceListToSequenceList(List<dk.aau.cs.giraf.dblib.models.Sequence> dbSeqs, Context con){
        ArrayList<Sequence> seqs = new ArrayList<Sequence>();
        for(dk.aau.cs.giraf.dblib.models.Sequence dbSeq : dbSeqs){
            seqs.add(morphDBSequenceToSequence(dbSeq, con));
        }
        return seqs;
    }

    /**
     * Morphs a DB sequence to a Giraf Sequence
     * @param dbSeq
     * @param con
     * @return Sequence
     */
    private Sequence morphDBSequenceToSequence(dk.aau.cs.giraf.dblib.models.Sequence dbSeq, Context con){
        /*SequenceController sc = new SequenceController(con);
        if(dbSeq.getPictogramId() == 0){
            sc.removeSequence(dbSeq.getId());
            return new Sequence();
        }*/
        return new Sequence(dbSeq.getId(),
                dbSeq.getPictogramId(),
                dbSeq.getName(),
                morphDBFramesToMediaFrames(dbSeq.getFramesList(), con),
                con);
    }


    /**
     * Morphs a list of DB frames to OUR kind of list of frames (MediaFrame-list)
     * @param dbFrames
     * @param con
     * @return a list of MediaFrames
     */
    private ArrayList<MediaFrame> morphDBFramesToMediaFrames(
            List<dk.aau.cs.giraf.dblib.models.Frame> dbFrames, Context con){
        ArrayList<MediaFrame> mediaFrames = new ArrayList<MediaFrame>();
        for(dk.aau.cs.giraf.dblib.models.Frame dbFrame : dbFrames){
            mediaFrames.add(morphDBFrameToMediaFrame(dbFrame, con));
        }
        return mediaFrames;
    }

    /**
     * Morphs a DB frame to OUR kind of frame (MediaFrame)
     * @param dbFrame
     * @param con
     * @return MediaFrame
     */
    private MediaFrame morphDBFrameToMediaFrame(dk.aau.cs.giraf.dblib.models.Frame dbFrame, Context con) {
        MediaFrame mediaFrame = new MediaFrame();
        PictogramController pc = new PictogramController(con);
        if (dbFrame.getPictogramId() > 0) {
            mediaFrame.setPictogramId(dbFrame.getPictogramId());
            mediaFrame.setChoicePictogram(PictoFactory.convertPictogram(con,
                    pc.getPictogramById(dbFrame.getPictogramId())));
        }
        if (!dbFrame.getPictogramList().isEmpty()){
            mediaFrame.setContent(PictoFactory.convertPictograms(con, dbFrame.getPictogramList()));
        }
        mediaFrame.setNestedSequenceID(dbFrame.getNestedSequence());
        mediaFrame.addFrame(new dk.aau.cs.giraf.tortoise.Frame(frameWidth,
                frameHeight,
                new Point(dbFrame.getPosX(), dbFrame.getPosY())));
        return mediaFrame;
    }

    /**
     * Morphs a Giraf Sequence to a DB Sequence
     * @param seq
     * @param seqType
     * @param profileID
     * @return DBSequence
     */
    private dk.aau.cs.giraf.dblib.models.Sequence morphSequenceToDBSequence(
            Sequence seq, SequenceType seqType, int profileID){
        dk.aau.cs.giraf.dblib.models.Sequence dbSeq = new dk.aau.cs.giraf.dblib.models.Sequence();

        dbSeq.setId(seq.getId());
        dbSeq.setName(seq.getTitle());
        dbSeq.setPictogramId(seq.getTitlePictoId());
        dbSeq.setProfileId(profileID);
        dbSeq.setSequenceType(seqType);
        dbSeq.setFramesList(morphMediaFramesToDBFrames(seq.getMediaFrames()));

        return dbSeq;
    }

    /**
     * Morphs a list of Giraf mediaframes to a list of DB medieframes
     * @param mediaFrames
     * @return
     */
    private List<dk.aau.cs.giraf.dblib.models.Frame> morphMediaFramesToDBFrames(List<MediaFrame> mediaFrames) {
        List<dk.aau.cs.giraf.dblib.models.Frame> DBframes = new ArrayList<dk.aau.cs.giraf.dblib.models.Frame>();
        int x = 0;
        for (MediaFrame mf :mediaFrames ){
            DBframes.add(morphMediaFramesToDBFrames(mf, x, 0));
            x++;
        }
        return DBframes;
    }


    private dk.aau.cs.giraf.dblib.models.Frame morphMediaFramesToDBFrames(MediaFrame mf, int x, int y){
        dk.aau.cs.giraf.dblib.models.Frame f = new dk.aau.cs.giraf.dblib.models.Frame();
        if (mf.getChoicePictogram() != null){
            f.setPictogramId(mf.getChoicePictogram().getPictogramID());
        }
        else{
            f.setPictogramId(mf.getPictogramId());
        }
        f.setNestedSequence(mf.getNestedSequenceID());
        f.setPictogramList(morphPictogramsToDBPictograms(mf.getContent()));
        f.setPosX(x);
        f.setPosY(y);
        return f;
    }

    private List<dk.aau.cs.giraf.dblib.models.Pictogram> morphPictogramsToDBPictograms(List<Pictogram> content) {
        List<dk.aau.cs.giraf.dblib.models.Pictogram> DBPictos = new ArrayList<dk.aau.cs.giraf.dblib.models.Pictogram>();
        for (Pictogram p :content ){
            DBPictos.add(morphPictogramsToDBPictograms(p));
        }
        return DBPictos;
    }

    private dk.aau.cs.giraf.dblib.models.Pictogram morphPictogramsToDBPictograms(Pictogram picto) {
        dk.aau.cs.giraf.dblib.models.Pictogram DBPicto = new dk.aau.cs.giraf.dblib.models.Pictogram();
        DBPicto.setId(picto.getPictogramID());
        return DBPicto;
    }
}
