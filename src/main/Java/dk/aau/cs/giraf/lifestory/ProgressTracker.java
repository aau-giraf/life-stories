package dk.aau.cs.giraf.lifestory;


import android.content.Context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import dk.aau.cs.giraf.lifestory.controller.Sequence;
import dk.aau.cs.giraf.lifestory.controller.SerializableSequence;

public class ProgressTracker implements Serializable {
    int[] progress = {-1, -1};
    List<SerializableSequence> changedDays;
    ArrayList<boolean[]> markedActivities;

    private static final long serialVersionUID = 4654897646l;

    public int[] getProgress() {
        return progress;
    }

    public void setProgress(int[] progress) {
        this.progress = progress;
    }

    public List<Sequence> getChangedDays(Context con) {
        if(this.changedDays != null){
            List<Sequence> seqs = new ArrayList<Sequence>();
            for (SerializableSequence s : changedDays) {
                seqs.add(s.getSequence(con));
            }
            return seqs;
        }
        else {
            return null;
        }
    }

    public void setChangedDays(List<Sequence> changedDays){
        this.changedDays = new ArrayList<SerializableSequence>();
        for(Sequence s : changedDays){
            this.changedDays.add(new SerializableSequence(s));
        }
    }

    public ArrayList<boolean[]> getMarkedActivities(){return markedActivities;}

    public void setMarkedActivities(ArrayList<boolean[]> markedActivities){this.markedActivities = markedActivities;}

}
