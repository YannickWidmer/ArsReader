package ch.widmer.yannick.arstechnicafeed;

/**
 * Created by yanni on 27.09.2017.
 */

public class AsyncTaskResponse{

    // asynctasks Retrieve meaning from the internet and Write meaning to the database
    public static final int RETRIEVEENTRIES = 0, RETRIEVEARTICLE = 1, WRITENEWENTRIES = 2, WRITEANDUPDATEENTRIES = 3, WRITEARTICLE = 4;

    //reasons of asynctasks
    public static final int TODISPLAY = 0, TOSAVE = 1;


    public int task,reason;
    public Results result;
    public Long id;
    public AsyncTaskResponse(int task, int reason, Results result, Long id){
        this.task = task;
        this.reason = reason;
        this.result = result;
        this.id = id;
    }

    public enum Results{
        INWORK(R.string.ok), OK(R.string.ok) , FAILED(R.string.failed), NETWORKTIMEOUTERROR(R.string.network_timeout_error) ,
        AUTHFAILERROR(R.string.auth_error) , SERVERERROR(R.string.server_error) , NETWORKERROR(R.string.network_error),
        PARSEERROR(R.string.parse_error);
        public int stringId;
        Results(int stringId){
            this.stringId = stringId;
        }
    }
}