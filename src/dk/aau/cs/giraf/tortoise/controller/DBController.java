package dk.aau.cs.giraf.tortoise.controller;

import java.util.ArrayList;
import java.util.List;

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
     * @param sequenceType
     * @param citizenID
     * @return boolean
     */
    public boolean saveSequence(Sequence seq, int sequenceType, int citizenID){
        success = false;
        // This is where the magic should happen
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
     * @return ArrayList of Sequences
     */
    public ArrayList loadCurrentProfileSequences(int citizenID, int sequenceType){
        ArrayList sequences = new ArrayList();
        // This is where the magic should happen
        return sequences;
    }
/*
    private DBSequence morphSequenceToDBSequence(Sequence seq, int sequenceType){
        DBSequence dbSeq = new DBSequence();
        dbSeq.setTitlePictoID(seq.getTitlePictoId());
        dbSeq.setTitle(seq.getTitle());
        dbSeq.setNumChoices(seq.getNumChoices());
        dbSeq.setFrames(seq.getMediaFrames());
        dbSeq.setNestedSequenceID(seq.getNestedSequenceID());
        dbSeq.setSequenceType(sequenceType);
        // TODO: NOT DONE!!!
        return dbSeq;
    }
*/
}
