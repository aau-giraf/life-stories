package dk.aau.cs.giraf.tortoise.controller;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.oasis.lib.controllers.SequenceController;
import dk.aau.cs.giraf.oasis.lib.models.Sequence.SequenceType;
import dk.aau.cs.giraf.pictogram.Pictogram;


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
     * Save Sequence.
     *
     * @param seq
     * @param seqType
     * @param profileID
     * @return boolean
     */
    public boolean saveSequence(Sequence seq, SequenceType seqType, int profileID, Context con){
        SequenceController sc = new SequenceController(con);
        success = sc.insertSequenceAndFrames(morphSequenceToDBSequence(seq, seqType, profileID));
        return success;
    }

    /**
     * Load specific Sequence.
     *
     * @param id
     * @return Sequence
     */
    public Sequence loadSequence(int id){
        Sequence seq = new Sequence();
        // This is where the magic should happen
        return seq;
    }

    /**
     * Returns all sequences of the defined type on the defined citizen.
     *
     * @param citizenID
     * @param sequenceType
        new dk.aau.cs.giraf.oasis.lib.models.Pictogram()
     * @return ArrayList of Sequences
     */
    public ArrayList loadCurrentProfileSequences(int citizenID, int sequenceType){
        ArrayList sequences = new ArrayList();
        // This is where the magic should happen
        return sequences;
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

    private Sequence morphDBSequenceToSequence(dk.aau.cs.giraf.oasis.lib.models.Sequence dbSeq){
        Sequence Seq = new Sequence();
        return Seq;
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
        f.setPictogramId(mf.getChoicePictogram().getPictogramID());
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
        DBPicto.setId(picto.getId());
        return DBPicto;
    }

}
