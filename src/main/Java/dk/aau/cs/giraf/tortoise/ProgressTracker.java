package dk.aau.cs.giraf.tortoise;


import java.io.Serializable;

public class ProgressTracker implements Serializable {
    int[] progress = new int[2];
    private static final long serialVersionUID = 4654897646l;

    public int[] getProgress() {
        return progress;
    }

    public void setProgress(int[] progress) {
        this.progress = progress;
    }
}
