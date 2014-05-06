package dk.aau.cs.giraf.tortoise.controller;

import android.content.Context;
import android.graphics.Point;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.Helper;
import dk.aau.cs.giraf.oasis.lib.controllers.PictogramController;
import dk.aau.cs.giraf.oasis.lib.controllers.SequenceController;
import dk.aau.cs.giraf.oasis.lib.models.Frame;
import dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType;
import dk.aau.cs.giraf.pictogram.PictoFactory;
import dk.aau.cs.giraf.pictogram.Pictogram;
import dk.aau.cs.giraf.tortoise.helpers.LifeStory;


/**
 * Created by David on 28-03-14.
 */
public class DBController {

    /*********************
     * Singleton pattern *
     *********************/
    private static DBController instance;

    private DBController(){};

    public static DBController getInstance(){
        DBController dbController;
        if(instance != null){
            dbController = instance;
        }
        else{
            dbController = new DBController();
        }
        return dbController;
    }

    /*******************************
     * Class variable declarations *
     *******************************/
    private boolean success;
    Helper oasisLibHelper;
    private final int frameHeight = 140;
    private final int frameWidth = 140;

    /***********************
     * SequenceType ENUMS: *
     ***********************
     * SEQUENCE = 0        *
     * SCHEDULE = 1        *
     * STORY = 2           *
     * PARROT = 3          *
     ***********************/

    /******************
     * Public methods *
     ******************/

    /**
     * Save a sequence on a specific profile.
     *
     * @param seq
     * @param seqType
     * @param profileID
     * @param con
     * @return boolean
     */
    public boolean saveSequence(Sequence seq, SequenceType seqType, int profileID, Context con){
        SequenceController sc = new SequenceController(con);
        success = sc.insertSequenceAndFrames(morphSequenceToDBSequence(seq, seqType, profileID));
        return success;
    }

    /**
     * Sets the lifestories of the current citizen
     *
     * @param profileID
     * @param sequenceType
     * @param con
     */
    public void loadCurrentCitizenSequences(int profileID, SequenceType sequenceType, Context con){
        oasisLibHelper = new Helper(con);
        LifeStory lifeStory = LifeStory.getInstance();
        lifeStory.setStories(morphDBSequenceListToSequenceList(oasisLibHelper.sequenceController.getSequenceByProfileIdAndType(profileID, sequenceType), con));
    }

    /**
     * Sets the templates of the current guardian
     *
     * @param profileID
     * @param sequenceType
     * @param con
     */
    public void loadCurrentGuardianTemplates(int profileID, SequenceType sequenceType, Context con){
        oasisLibHelper = new Helper(con);
        LifeStory lifeStory = LifeStory.getInstance();
        lifeStory.setTemplates(morphDBSequenceListToSequenceList(oasisLibHelper.sequenceController.getSequenceByProfileIdAndType(profileID, sequenceType), con));
    }

    /**
     * Morphs a list of DB sequences to OUR kind of list of sequences
     *
     * @param dbSeqs
     * @param con
     * @return a list of Pictogram Sequences
     */
    private ArrayList<Sequence> morphDBSequenceListToSequenceList(List<dk.aau.cs.giraf.oasis.lib.models.Sequence> dbSeqs, Context con){
        ArrayList<Sequence> seqs = new ArrayList<Sequence>();
        SequenceController sc = new SequenceController(con);
        for(dk.aau.cs.giraf.oasis.lib.models.Sequence dbSeq : dbSeqs){
            dk.aau.cs.giraf.oasis.lib.models.Sequence dbs = sc.getSequenceAndFrames(dbSeq.getId());
            seqs.add(morphDBSequenceToSequence(dbs, con));
        }
        return seqs;
    }

    /**
     * Morphs a DB sequence to OUR kind of sequence
     *
     * @param dbSeq
     * @param con
     * @return Sequence
     */
    private Sequence morphDBSequenceToSequence(dk.aau.cs.giraf.oasis.lib.models.Sequence dbSeq, Context con){
        Sequence seq = new Sequence(dbSeq.getId(), dbSeq.getPictogramId(), dbSeq.getName(), morphDBFramesToMediaFrames(dbSeq.getFramesList(), con), con);
        return seq;
    }

    /**
     * Morphs a list of DB frames to OUR kind of list of frames (MediaFrame-list)
     *
     * @param dbFrames
     * @param con
     * @return a list of MediaFrames
     */
    private ArrayList<MediaFrame> morphDBFramesToMediaFrames(List<dk.aau.cs.giraf.oasis.lib.models.Frame> dbFrames, Context con){
        ArrayList<MediaFrame> mediaFrames = new ArrayList<MediaFrame>();
        for(dk.aau.cs.giraf.oasis.lib.models.Frame dbFrame : dbFrames){
            mediaFrames.add(morphDBFrameToMediaFrame(dbFrame, con));
        }
        return mediaFrames;
    }

    /**
     * Morphs a DB frame to OUR kind of frame (MediaFrame)
     *
     * @param dbFrame
     * @param con
     * @return MediaFrame
     */
    private MediaFrame morphDBFrameToMediaFrame(dk.aau.cs.giraf.oasis.lib.models.Frame dbFrame, Context con) {
        MediaFrame mediaFrame = new MediaFrame();
        PictogramController pictoController = oasisLibHelper.pictogramHelper;
        mediaFrame.setChoicePictogram(PictoFactory.convertPictogram(con, pictoController.getPictogramById(dbFrame.getPictogramId())));
        mediaFrame.setContent(PictoFactory.convertPictograms(con, dbFrame.getPictogramList()));
        mediaFrame.addFrame(new dk.aau.cs.giraf.tortoise.Frame(frameWidth, frameHeight, new Point(dbFrame.getPosX(), dbFrame.getPosY())));
        return mediaFrame;
    }

    private dk.aau.cs.giraf.oasis.lib.models.Sequence morphSequenceToDBSequence(
            Sequence seq, SequenceType seqType, int profileID){
        dk.aau.cs.giraf.oasis.lib.models.Sequence dbSeq = new dk.aau.cs.giraf.oasis.lib.models.Sequence();

        dbSeq.setName(seq.getTitle());
        dbSeq.setPictogramId(seq.getTitlePictoId());
        dbSeq.setProfileId(profileID);
        dbSeq.setSequenceType(seqType);
        dbSeq.setFramesList(morphMediaFramesToDBFrames(seq.getMediaFrames()));

        return dbSeq;
    }

    private List<dk.aau.cs.giraf.oasis.lib.models.Frame> morphMediaFramesToDBFrames(List<MediaFrame> mediaFrames) {
        List<dk.aau.cs.giraf.oasis.lib.models.Frame> DBframes = new ArrayList<dk.aau.cs.giraf.oasis.lib.models.Frame>();
        for (MediaFrame mf :mediaFrames ){
            DBframes.add(morphMediaFramesToDBFrames(mf));
        }
        return DBframes;
    }


    private dk.aau.cs.giraf.oasis.lib.models.Frame morphMediaFramesToDBFrames(MediaFrame mf){
        dk.aau.cs.giraf.oasis.lib.models.Frame f = new dk.aau.cs.giraf.oasis.lib.models.Frame();
        if (mf.getChoicePictogram() != null){
            f.setPictogramId(mf.getChoicePictogram().getPictogramID());
        }
        f.setNestedSequence(mf.getNestedSequenceID());
        f.setPictogramList(morphPictogramsToDBPictograms(mf.getContent()));
        f.setPosX(mf.getFrames().get(0).getPosition().x); //TODO media frames should only
        f.setPosY(mf.getFrames().get(0).getPosition().y); // contain one Frame in future
        return f;
    }

    private List<dk.aau.cs.giraf.oasis.lib.models.Pictogram> morphPictogramsToDBPictograms(List<Pictogram> content) {
        List<dk.aau.cs.giraf.oasis.lib.models.Pictogram> DBPictos = new ArrayList<dk.aau.cs.giraf.oasis.lib.models.Pictogram>();
        for (Pictogram p :content ){
            DBPictos.add(morphPictogramsToDBPictograms(p));
        }
        return DBPictos;
    }

    private dk.aau.cs.giraf.oasis.lib.models.Pictogram morphPictogramsToDBPictograms(Pictogram picto) {
        dk.aau.cs.giraf.oasis.lib.models.Pictogram DBPicto = new dk.aau.cs.giraf.oasis.lib.models.Pictogram();
        DBPicto.setId(picto.getPictogramID());
        return DBPicto;
    }

}
