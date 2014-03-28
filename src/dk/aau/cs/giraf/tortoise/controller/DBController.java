package dk.aau.cs.giraf.tortoise.controller;

/**
 * Created by David on 28-03-14.
 */
public class DBController {

    // Singleton pattern
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
}
